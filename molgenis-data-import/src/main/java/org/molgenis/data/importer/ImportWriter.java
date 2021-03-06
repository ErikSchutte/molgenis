package org.molgenis.data.importer;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.i18n.model.I18nString;
import org.molgenis.data.i18n.model.Language;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.semantic.LabeledResource;
import org.molgenis.data.semantic.SemanticTag;
import org.molgenis.data.semanticsearch.service.TagService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.framework.db.EntityImportReport;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.util.DependencyResolver;
import org.molgenis.util.HugeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.i18n.model.I18nStringMetaData.I18N_STRING;
import static org.molgenis.data.i18n.model.LanguageMetaData.LANGUAGE;
import static org.molgenis.data.importer.EmxMetaDataParser.*;
import static org.molgenis.data.meta.model.TagMetaData.TAG;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;

/**
 * Writes the imported metadata and data to target {@link RepositoryCollection}.
 */
public class ImportWriter
{
	private static final Logger LOG = LoggerFactory.getLogger(ImportWriter.class);

	private final DataService dataService;
	private final PermissionSystemService permissionSystemService;
	private final TagService<LabeledResource, LabeledResource> tagService;
	private final MolgenisPermissionService molgenisPermissionService;
	private final TagFactory tagFactory;
	private final EntityManager entityManager;

	/**
	 * Creates the ImportWriter
	 *
	 * @param dataService             {@link DataService} to query existing repositories and transform entities
	 * @param permissionSystemService {@link PermissionSystemService} to give permissions on uploaded entities
	 * @param tagFactory              {@link TagFactory} to create new tags
	 * @param entityManager           entity manager to create new entities
	 */
	public ImportWriter(DataService dataService, PermissionSystemService permissionSystemService,
			TagService<LabeledResource, LabeledResource> tagService,
			MolgenisPermissionService molgenisPermissionService, TagFactory tagFactory, EntityManager entityManager)
	{
		this.dataService = requireNonNull(dataService);
		this.permissionSystemService = requireNonNull(permissionSystemService);
		this.tagService = requireNonNull(tagService);
		this.molgenisPermissionService = requireNonNull(molgenisPermissionService);
		this.tagFactory = requireNonNull(tagFactory);
		this.entityManager = requireNonNull(entityManager);
	}

	@Transactional
	public EntityImportReport doImport(EmxImportJob job)
	{
		// languages first
		importLanguages(job.report, job.parsedMetaData.getLanguages(), job.dbAction, job.metaDataChanges);
		runAsSystem(() -> importTags(job.source));
		runAsSystem(() -> importPackages(job.parsedMetaData));
		runAsSystem(() -> addEntityMetaData(job.parsedMetaData, job.report, job.metaDataChanges));
		addEntityPermissions(job.metaDataChanges);
		runAsSystem(() -> importEntityAndAttributeTags(job.parsedMetaData));
		Iterable<EntityMetaData> existingMetaData = dataService.getMeta().getEntityMetaDatas()::iterator;
		Map<String, EntityMetaData> allEntityMetaDataMap = new HashMap<>();
		for (EntityMetaData emd : job.parsedMetaData.getEntities())
		{
			allEntityMetaDataMap.put(emd.getName(), emd);
		}
		scanMetaDataForSystemEntityMetaData(allEntityMetaDataMap, existingMetaData);
		importData(job.report, DependencyResolver.resolve(Sets.newLinkedHashSet(allEntityMetaDataMap.values())),
				job.source, job.dbAction, job.defaultPackage);
		importI18nStrings(job.report, job.parsedMetaData.getI18nStrings(), job.dbAction);

		return job.report;
	}

	private void importLanguages(EntityImportReport report, Map<String, Language> languages, DatabaseAction dbAction,
			MetaDataChanges metaDataChanges)
	{
		if (!languages.isEmpty())
		{
			Repository<Language> repo = dataService.getRepository(LANGUAGE, Language.class);

			// Find new ones
			languages.values().stream().map(Entity::getIdValue).forEach(id ->
			{
				if (repo.findOneById(id) == null)
				{
					metaDataChanges.addLanguage(languages.get(id));
				}
			});

			int count = update(repo, languages.values(), dbAction);
			report.addEntityCount(LANGUAGE, count);
		}
	}

	private void importI18nStrings(EntityImportReport report, Map<String, I18nString> i18nStrings,
			DatabaseAction dbAction)
	{
		if (!i18nStrings.isEmpty())
		{
			Repository<I18nString> repo = dataService.getRepository(I18N_STRING, I18nString.class);
			int count = update(repo, i18nStrings.values(), dbAction);
			report.addEntityCount(I18N_STRING, count);
		}
	}

	private void importEntityAndAttributeTags(ParsedMetaData parsedMetaData)
	{
		for (SemanticTag<EntityMetaData, LabeledResource, LabeledResource> tag : parsedMetaData.getEntityTags())
		{
			tagService.addEntityTag(tag);
		}

		for (EntityMetaData emd : parsedMetaData.getAttributeTags().keySet())
		{
			for (SemanticTag<AttributeMetaData, LabeledResource, LabeledResource> tag : parsedMetaData
					.getAttributeTags().get(emd))
			{
				tagService.addAttributeTag(emd, tag);
			}
		}
	}

	/**
	 * Imports entity data for all entities in resolved from source
	 */
	private void importData(EntityImportReport report, Iterable<EntityMetaData> resolved, RepositoryCollection source,
			DatabaseAction dbAction, String defaultPackage)
	{
		for (final EntityMetaData entityMetaData : resolved)
		{
			String name = entityMetaData.getName();

			// Languages and i18nstrings are already done
			if (!name.equalsIgnoreCase(LANGUAGE) && !name.equalsIgnoreCase(I18N_STRING) && dataService
					.hasRepository(name))
			{
				Repository<Entity> repository = dataService.getRepository(name);
				Repository<Entity> emxEntityRepo = source.getRepository(entityMetaData.getName());

				// Try without default package
				if ((emxEntityRepo == null) && (defaultPackage != null) && entityMetaData.getName().toLowerCase()
						.startsWith(defaultPackage.toLowerCase() + "_"))
				{
					emxEntityRepo = source
							.getRepository(entityMetaData.getName().substring(defaultPackage.length() + 1));
				}

				// check to prevent nullpointer when importing metadata only
				if (emxEntityRepo != null)
				{
					// transforms entities so that they match the entity meta data of the output repository
					Iterable<Entity> entities = Iterables
							.transform(emxEntityRepo, emxEntity -> toEntity(entityMetaData, emxEntity));
					int count = update(repository, entities, dbAction);
					report.addEntityCount(name, count);
				}
			}
		}
	}

	/**
	 * Create an entity from the EMX entity
	 *
	 * @param entityMeta entity meta data
	 * @param emxEntity  EMX entity
	 * @return MOLGENIS entity
	 */
	private Entity toEntity(EntityMetaData entityMeta, Entity emxEntity)
	{
		Entity entity = entityManager.create(entityMeta, POPULATE);
		for (AttributeMetaData attr : entityMeta.getAtomicAttributes())
		{
			if (attr.getExpression() == null)
			{
				String attrName = attr.getName();
				Object emxValue = emxEntity.get(attrName);

				Object value;
				AttributeType attrType = attr.getDataType();
				switch (attrType)
				{
					case BOOL:
					case DATE:
					case DATE_TIME:
					case DECIMAL:
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case INT:
					case LONG:
					case SCRIPT:
					case STRING:
					case TEXT:
						value = emxValue != null ? DataConverter.convert(emxValue, attr) : null;
						break;
					case CATEGORICAL:
					case FILE:
					case XREF:
						// DataConverter.convert performs no conversion for reference types
						if (emxValue != null)
						{
							if (emxValue instanceof Entity)
							{
								value = toEntity(attr.getRefEntity(), (Entity) emxValue);
							}
							else
							{
								EntityMetaData xrefEntity = attr.getRefEntity();
								Object entityId = DataConverter.convert(emxValue, xrefEntity.getIdAttribute());
								value = entityManager.getReference(xrefEntity, entityId);
							}
						}
						else
						{
							value = null;
						}
						break;
					case CATEGORICAL_MREF:
					case MREF:
						// DataConverter.convert performs no conversion for reference types
						if (emxValue != null)
						{
							if (emxValue instanceof Iterable<?>)
							{
								List<Entity> mrefEntities = new ArrayList<>();
								for (Object emxValueItem : (Iterable<?>) emxValue)
								{
									Entity entityValue;
									if (emxValueItem instanceof Entity)
									{
										entityValue = toEntity(attr.getRefEntity(), (Entity) emxValueItem);
									}
									else
									{
										EntityMetaData xrefEntity = attr.getRefEntity();
										Object entityId = DataConverter
												.convert(emxValueItem, xrefEntity.getIdAttribute());
										entityValue = entityManager.getReference(xrefEntity, entityId);
									}
									mrefEntities.add(entityValue);
								}
								value = mrefEntities;
							}
							else
							{
								EntityMetaData mrefEntity = attr.getRefEntity();
								AttributeMetaData refIdAttr = mrefEntity.getIdAttribute();

								String[] tokens = StringUtils.split(emxValue.toString(), ',');
								List<Entity> mrefEntities = new ArrayList<>();
								for (String token : tokens)
								{
									Object entityId = DataConverter.convert(token.trim(), refIdAttr);
									mrefEntities.add(entityManager.getReference(mrefEntity, entityId));
								}
								value = mrefEntities;
							}
						}
						else
						{
							value = emptyList();
						}
						break;
					case COMPOUND:
						throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
					default:
						throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
				}
				entity.set(attrName, value);
			}
		}
		return entity;
	}

	/**
	 * Keeps the entities that have: 1. A reference to themselves. 2. Minimal one value.
	 *
	 * @param entities
	 * @return Iterable<Entity> - filtered entities;
	 */
	private Iterable<Entity> keepSelfReferencedEntities(Iterable<Entity> entities)
	{
		return Iterables.filter(entities, entity ->
		{
			Iterator<AttributeMetaData> attributes = entity.getEntityMetaData().getAttributes().iterator();
			while (attributes.hasNext())
			{
				AttributeMetaData attribute = attributes.next();
				if (attribute.getRefEntity() != null && attribute.getRefEntity().getName()
						.equals(entity.getEntityMetaData().getName()))
				{
					List<String> ids = DataConverter.toList(entity.get(attribute.getName()));
					Iterable<Entity> refEntities = entity.getEntities(attribute.getName());
					if (ids != null && ids.size() != Iterators.size(refEntities.iterator()))
					{
						throw new UnknownEntityException(
								"One or more values [" + ids + "] from " + attribute.getDataType().toString()
										+ " field " + attribute.getName() + " could not be resolved");
					}
					return true;
				}
			}

			return false;
		});
	}

	/**
	 * Gives the user permission to see and edit his imported entities, unless the user is admin since admins can do
	 * that anyways.
	 */
	private void addEntityPermissions(MetaDataChanges metaDataChanges)
	{
		if (!SecurityUtils.currentUserIsSu())
		{
			permissionSystemService
					.giveUserEntityPermissions(SecurityContextHolder.getContext(), metaDataChanges.getAddedEntities());
		}
	}

	/**
	 * Adds the parsed {@link ParsedMetaData}, creating new repositories where necessary.
	 */
	private void addEntityMetaData(ParsedMetaData parsedMetaData, EntityImportReport report,
			MetaDataChanges metaDataChanges)
	{
		Iterable<EntityMetaData> existingMetaData = dataService.getMeta().getEntityMetaDatas()::iterator;
		Map<String, EntityMetaData> allEntityMetaDataMap = new HashMap<>();
		for (EntityMetaData emd : parsedMetaData.getEntities())
		{
			allEntityMetaDataMap.put(emd.getName(), emd);
		}
		scanMetaDataForSystemEntityMetaData(allEntityMetaDataMap, existingMetaData);
		List<EntityMetaData> resolve = DependencyResolver
				.resolve(new HashSet<EntityMetaData>(Sets.newLinkedHashSet(allEntityMetaDataMap.values())));
		for (EntityMetaData entityMetaData : resolve)
		{
			String name = entityMetaData.getName();
			if (!EMX_ENTITIES.equals(name) && !EMX_ATTRIBUTES.equals(name) && !EMX_PACKAGES.equals(name) && !EMX_TAGS
					.equals(name) && !EMX_LANGUAGES.equals(name) && !EMX_I18NSTRINGS.equals(name))
			{
				EntityMetaData existingEntityMeta = dataService.getMeta().getEntityMetaData(entityMetaData.getName());
				if (existingEntityMeta == null)
				{
					LOG.debug("trying to create: " + name);
					metaDataChanges.addEntity(name);
					dataService.getMeta().addEntityMeta(entityMetaData);
					if (!entityMetaData.isAbstract())
					{
						report.addNewEntity(name);
					}
				}
				else if (!entityMetaData.isAbstract())
				{
					// inject identifiers
					Map<String, String> attrNameIdentifierMap = stream(
							existingEntityMeta.getOwnAllAttributes().spliterator(), false)
							.collect(toMap(AttributeMetaData::getName, AttributeMetaData::getIdentifier));
					entityMetaData.getOwnAllAttributes().forEach(attr ->
					{
						String identifier = attrNameIdentifierMap.get(attr.getName());
						if (identifier != null)
						{
							attr.setIdentifier(identifier);
						}
					});

					dataService.getMeta().updateEntityMeta(entityMetaData);
				}
			}
		}
	}

	/**
	 * Adds the packages from the packages sheet to the {@link org.molgenis.data.meta.MetaDataService}.
	 */
	private void importPackages(ParsedMetaData parsedMetaData)
	{
		ImmutableCollection<Package> packages = parsedMetaData.getPackages().values();
		dataService.getMeta().upsertPackages(packages.stream().filter(package_ -> package_ != null));
	}

	/**
	 * Imports the tags from the tag sheet.
	 */
	// FIXME: can everybody always update a tag?
	private void importTags(RepositoryCollection source)
	{
		Repository<Entity> tagRepository = source.getRepository(EMX_TAGS);
		if (tagRepository != null)
		{
			for (Entity tagEntity : tagRepository)
			{
				Entity existingTag = dataService.findOneById(TAG, tagEntity.getString(EMX_TAG_IDENTIFIER));
				if (existingTag == null)
				{
					Tag tag = entityToTag(tagEntity.getString(EMX_TAG_IDENTIFIER), tagEntity);
					dataService.add(TAG, tag);
				}
				else
				{
					dataService.update(TAG, existingTag);
				}
			}
		}
	}

	/**
	 * Transforms an {@link Entity} to a {@link Tag}
	 *
	 * @param id
	 * @param tagEntity
	 * @return
	 */
	// FIXME: Duplicated with EmxMetaDataParser
	public Tag entityToTag(String id, Entity tagEntity)
	{
		Tag tag = tagFactory.create(id);
		tag.setObjectIri(tagEntity.getString(EMX_TAG_OBJECT_IRI));
		tag.setLabel(tagEntity.getString(EMX_TAG_LABEL));
		tag.setRelationLabel(tagEntity.getString(EMX_TAG_RELATION_LABEL));
		tag.setCodeSystem(tagEntity.getString(EMX_TAG_CODE_SYSTEM));
		tag.setRelationIri(tagEntity.getString(EMX_TAG_RELATION_IRI));

		return tag;
	}

	/**
	 * Updates a repository with entities.
	 *
	 * @param repo     the {@link Repository} to update
	 * @param entities the entities to
	 * @param dbAction {@link DatabaseAction} describing how to merge the existing entities
	 * @return number of updated entities
	 */
	private <E extends Entity> int update(Repository<E> repo, Iterable<E> entities, DatabaseAction dbAction)
	{
		if (entities == null) return 0;

		if (!molgenisPermissionService.hasPermissionOnEntity(repo.getName(), Permission.WRITE))
		{
			throw new MolgenisDataAccessException("No WRITE permission on entity '" + repo.getName()
					+ "'. Is this entity already imported by another user who did not grant you WRITE permission?");
		}
		String idAttributeName = repo.getEntityMetaData().getIdAttribute().getName();
		AttributeType dataType = repo.getEntityMetaData().getIdAttribute().getDataType();
		FieldType idFieldType = MolgenisFieldTypes.getType(AttributeType.getValueString(dataType));
		HugeSet<Object> existingIds = new HugeSet<>();
		HugeSet<Object> ids = new HugeSet<>();
		try
		{
			for (Entity entity : entities)
			{
				Object id = entity.get(idAttributeName);
				if (id != null)
				{
					ids.add(id);
				}
			}

			if (!ids.isEmpty())
			{
				// Check if the ids already exist
				if (repo.count() > 0)
				{
					int batchSize = 100;
					Query<E> q = new QueryImpl<>();
					Iterator<Object> it = ids.iterator();
					int batchCount = 0;
					while (it.hasNext())
					{
						Object id = it.next();
						q.eq(idAttributeName, id);
						batchCount++;
						if (batchCount == batchSize || !it.hasNext())
						{
							repo.findAll(q).forEach(existing -> existingIds.add(existing.getIdValue()));
							q = new QueryImpl<>();
							batchCount = 0;
						}
						else
						{
							q.or();
						}
					}
				}
			}

			int count = 0;
			switch (dbAction)
			{
				case ADD:
					if (!existingIds.isEmpty())
					{
						StringBuilder msg = new StringBuilder();
						msg.append("Trying to add existing ").append(repo.getName())
								.append(" entities as new insert: ");

						int i = 0;
						Iterator<?> it = existingIds.iterator();
						while (it.hasNext() && i < 5)
						{
							if (i > 0)
							{
								msg.append(",");
							}
							msg.append(it.next());
							i++;
						}

						if (it.hasNext())
						{
							msg.append(" and more.");
						}
						throw new MolgenisDataException(msg.toString());
					}

					count = repo.add(stream(entities.spliterator(), false));
					break;
				case ADD_IGNORE_EXISTING:
					int batchSize = 1000;
					List<E> existingEntities;
					List<E> newEntities = newArrayList();

					Iterator<E> it = entities.iterator();
					while (it.hasNext())
					{
						E entity = it.next();
						count++;
						Object id = idFieldType.convert(entity.get(idAttributeName));
						if (!existingIds.contains(id))
						{
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								repo.add(newEntities.stream());
								newEntities.clear();
							}
						}
					}

					if (!newEntities.isEmpty())
					{
						repo.add(newEntities.stream());
					}

					break;
				case ADD_UPDATE_EXISTING:
					batchSize = 1000;
					existingEntities = new ArrayList<>(batchSize);
					List<Integer> existingEntitiesRowIndex = new ArrayList<>(batchSize);
					newEntities = new ArrayList<>(batchSize);
					List<Integer> newEntitiesRowIndex = new ArrayList<>(batchSize);

					it = entities.iterator();
					while (it.hasNext())
					{
						E entity = it.next();
						count++;
						Object id = idFieldType.convert(entity.get(idAttributeName));
						if (existingIds.contains(id))
						{
							existingEntitiesRowIndex.add(count);
							existingEntities.add(entity);
							if (existingEntities.size() == batchSize)
							{
								updateInRepo(repo, existingEntities, existingEntitiesRowIndex);
							}
						}
						else
						{
							newEntitiesRowIndex.add(count);
							newEntities.add(entity);
							if (newEntities.size() == batchSize)
							{
								insertIntoRepo(repo, newEntities, newEntitiesRowIndex);
							}
						}
					}

					if (!existingEntities.isEmpty())
					{
						updateInRepo(repo, existingEntities, existingEntitiesRowIndex);
					}
					if (!newEntities.isEmpty())
					{
						insertIntoRepo(repo, newEntities, newEntitiesRowIndex);
					}
					break;
				case UPDATE:
					int errorCount = 0;
					StringBuilder msg = new StringBuilder();
					msg.append("Trying to update non-existing ").append(repo.getName()).append(" entities:");

					for (Entity entity : entities)
					{
						count++;
						Object id = idFieldType.convert(entity.get(idAttributeName));
						if (!existingIds.contains(id))
						{
							if (++errorCount == 6)
							{
								break;
							}

							if (errorCount > 0)
							{
								msg.append(", ");
							}
							msg.append(id);
						}
					}

					if (errorCount > 0)
					{
						if (errorCount == 6)
						{
							msg.append(" and more.");
						}
						throw new MolgenisDataException(msg.toString());
					}
					repo.update(stream(entities.spliterator(), false));
					break;

				default:
					break;

			}

			return count;
		}
		finally
		{
			IOUtils.closeQuietly(existingIds);
			IOUtils.closeQuietly(ids);
		}
	}

	private <E extends Entity> void updateInRepo(Repository<E> repo, List<E> existingEntities,
			List<Integer> existingEntitiesRowIndex)
	{
		try
		{
			repo.update(existingEntities.stream());
		}
		catch (MolgenisValidationException mve)
		{
			mve.renumberViolationRowIndices(existingEntitiesRowIndex);
			throw mve;
		}
		existingEntities.clear();
		existingEntitiesRowIndex.clear();
	}

	private <E extends Entity> void insertIntoRepo(Repository<E> repo, List<E> newEntities,
			List<Integer> newEntitiesRowIndex)
	{
		try
		{
			repo.add(newEntities.stream());
		}
		catch (MolgenisValidationException mve)
		{
			mve.renumberViolationRowIndices(newEntitiesRowIndex);
			throw mve;
		}
		newEntities.clear();
		newEntitiesRowIndex.clear();
	}
}