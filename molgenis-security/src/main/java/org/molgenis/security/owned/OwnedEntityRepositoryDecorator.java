package org.molgenis.security.owned;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.OwnedEntityMetaData.OWNED;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.OwnedEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.runas.SystemSecurityToken;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.EntityUtils;

/**
 * RepositoryDecorator that works on EntityMetaData that extends OwnedEntityMetaData.
 * 
 * Ensures that when an Entity is created the owner is set to the current user, users can only view, update, delete
 * their own entities.
 * 
 * Admins are not effected.
 */
public class OwnedEntityRepositoryDecorator implements Repository<Entity>
{
	private final Repository<Entity> decoratedRepo;

	public OwnedEntityRepositoryDecorator(Repository<Entity> decoratedRepo)
	{
		this.decoratedRepo = requireNonNull(decoratedRepo);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		if (mustAddRowLevelSecurity()) return findAll(new QueryImpl<Entity>()).iterator();
		return decoratedRepo.iterator();
	}

	@Override
	public void forEachBatched(Fetch fetch, Consumer<List<Entity>> consumer, int batchSize)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
		}
		decoratedRepo.forEachBatched(fetch, entities -> {
			if (mustAddRowLevelSecurity())
			{
				//TODO: This results in smaller batches! Should do a findAll instead!
				consumer.accept(entities.stream().filter(this::currentUserIsOwner).collect(toList()));
			}
			else
			{
				consumer.accept(entities);
			}
		}, batchSize);
	}

	@Override
	public void close() throws IOException
	{
		decoratedRepo.close();
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return decoratedRepo.getCapabilities();
	}

	@Override
	public Set<Operator> getQueryOperators()
	{
		return decoratedRepo.getQueryOperators();
	}

	@Override
	public String getName()
	{
		return decoratedRepo.getName();
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return decoratedRepo.getEntityMetaData();
	}

	@Override
	public long count()
	{
		if (mustAddRowLevelSecurity()) return count(new QueryImpl<Entity>());
		return decoratedRepo.count();
	}

	@Override
	public Query<Entity> query()
	{
		return decoratedRepo.query();
	}

	@Override
	public long count(Query<Entity> q)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(q);
		return decoratedRepo.count(q);
	}

	@Override
	public Stream<Entity> findAll(Query<Entity> q)
	{
		if (mustAddRowLevelSecurity())
		{
			addRowLevelSecurity(q);
		}
		return decoratedRepo.findAll(q);
	}

	@Override
	public Entity findOne(Query<Entity> q)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(q);
		return decoratedRepo.findOne(q);
	}

	@Override
	public Entity findOneById(Object id)
	{
		Entity e = decoratedRepo.findOneById(id);

		if (mustAddRowLevelSecurity())
		{
			if (!currentUserIsOwner(e)) return null;
		}

		return e;
	}

	@Override
	public Entity findOneById(Object id, Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
		}
		Entity e = decoratedRepo.findOneById(id, fetch);

		if (mustAddRowLevelSecurity())
		{
			if (!currentUserIsOwner(e)) return null;
		}

		return e;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids)
	{
		Stream<Entity> entities = decoratedRepo.findAll(ids);
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(this::currentUserIsOwner);
		}
		return entities;
	}

	@Override
	public Stream<Entity> findAll(Stream<Object> ids, Fetch fetch)
	{
		if (fetch != null)
		{
			fetch.field(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
		}
		Stream<Entity> entities = decoratedRepo.findAll(ids, fetch);
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(this::currentUserIsOwner);
		}
		return entities;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		if (mustAddRowLevelSecurity()) addRowLevelSecurity(aggregateQuery.getQuery());
		return decoratedRepo.aggregate(aggregateQuery);
	}

	@Override
	public void update(Entity entity)
	{
		if (isOwnedEntityMetaData()
				&& (mustAddRowLevelSecurity() || entity.get(OwnedEntityMetaData.ATTR_OWNER_USERNAME) == null))
			entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		decoratedRepo.update(entity);
	}

	@Override
	public void update(Stream<Entity> entities)
	{
		if (isOwnedEntityMetaData())
		{
			boolean mustAddRowLevelSecurity = mustAddRowLevelSecurity();
			String currentUsername = SecurityUtils.getCurrentUsername();
			entities = entities.map(entity -> {
				if (mustAddRowLevelSecurity || entity.get(OwnedEntityMetaData.ATTR_OWNER_USERNAME) == null)
				{
					entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, currentUsername);
				}
				return entity;
			});
		}

		decoratedRepo.update(entities);
	}

	@Override
	public void delete(Entity entity)
	{
		if (mustAddRowLevelSecurity() && !currentUserIsOwner(entity)) return;
		decoratedRepo.delete(entity);
	}

	@Override
	public void delete(Stream<Entity> entities)
	{
		if (mustAddRowLevelSecurity())
		{
			entities = entities.filter(this::currentUserIsOwner);
		}

		decoratedRepo.delete(entities);
	}

	@Override
	public void deleteById(Object id)
	{
		if (mustAddRowLevelSecurity())
		{
			Entity entity = findOneById(id);
			if ((entity != null) && !currentUserIsOwner(entity)) return;
		}

		decoratedRepo.deleteById(id);
	}

	@Override
	public void deleteAll(Stream<Object> ids)
	{
		if (mustAddRowLevelSecurity())
		{
			delete(decoratedRepo.findAll(ids));
		}
		else
		{
			decoratedRepo.deleteAll(ids);
		}
	}

	@Override
	public void deleteAll()
	{
		if (mustAddRowLevelSecurity())
		{
			decoratedRepo.forEachBatched(entities -> delete(entities.stream()), 1000);
		}
		else
		{
			decoratedRepo.deleteAll();
		}
	}

	@Override
	public void add(Entity entity)
	{
		if (isOwnedEntityMetaData()
				&& (mustAddRowLevelSecurity() || entity.get(OwnedEntityMetaData.ATTR_OWNER_USERNAME) == null))
		{
			entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, SecurityUtils.getCurrentUsername());
		}

		decoratedRepo.add(entity);
	}

	@Override
	public Integer add(Stream<Entity> entities)
	{
		if (isOwnedEntityMetaData())
		{
			boolean mustAddRowLevelSecurity = mustAddRowLevelSecurity();
			String currentUsername = SecurityUtils.getCurrentUsername();
			entities = entities.map(entity -> {
				if (mustAddRowLevelSecurity || entity.get(OwnedEntityMetaData.ATTR_OWNER_USERNAME) == null)
				{
					entity.set(OwnedEntityMetaData.ATTR_OWNER_USERNAME, currentUsername);
				}
				return entity;
			});
		}

		return decoratedRepo.add(entities);
	}

	@Override
	public void flush()
	{
		decoratedRepo.flush();
	}

	@Override
	public void clearCache()
	{
		decoratedRepo.clearCache();
	}

	private boolean mustAddRowLevelSecurity()
	{
		if (SecurityUtils.currentUserIsSu() || SecurityUtils.currentUserHasRole(SystemSecurityToken.ROLE_SYSTEM))
			return false;
		return isOwnedEntityMetaData();
	}

	private boolean isOwnedEntityMetaData()
	{
		return EntityUtils.doesExtend(getEntityMetaData(), OWNED);
	}

	private void addRowLevelSecurity(Query<Entity> q)
	{
		String user = SecurityUtils.getCurrentUsername();
		if (user != null)
		{
			if (!q.getRules().isEmpty()) q.and();
			q.eq(OwnedEntityMetaData.ATTR_OWNER_USERNAME, user);
		}
	}

	private String getOwnerUserName(Entity questionnaire)
	{
		return questionnaire.getString(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
	}

	@Override
	public void rebuildIndex()
	{
		decoratedRepo.rebuildIndex();
	}

	@Override
	public void addEntityListener(EntityListener entityListener)
	{
		decoratedRepo.addEntityListener(entityListener);
	}

	@Override
	public void removeEntityListener(EntityListener entityListener)
	{
		decoratedRepo.removeEntityListener(entityListener);
	}

	private boolean currentUserIsOwner(Entity entity)
	{
		if (null == entity) return false;
		return SecurityUtils.getCurrentUsername().equals(getOwnerUserName(entity));
	}
}
