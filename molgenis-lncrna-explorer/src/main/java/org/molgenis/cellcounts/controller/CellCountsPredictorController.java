package org.molgenis.cellcounts.controller;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Iterables.transform;
import static org.molgenis.cellcounts.controller.CellCountsPredictorController.URI;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisInvalidFormatException;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCollection;
import org.molgenis.data.csv.CsvRepositoryCollection;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.script.SavedScriptRunner;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.ui.MolgenisPluginController;
import org.molgenis.ui.menu.MenuReaderService;
import org.molgenis.util.FileUploadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Iterables;

import autovalue.shaded.com.google.common.common.base.Preconditions;
import autovalue.shaded.com.google.common.common.collect.ImmutableMap;

@Controller
@RequestMapping(URI)
public class CellCountsPredictorController extends MolgenisPluginController
{
	private static final String GENES_REPOSITORY_ASSOCIATED_GENE_NAME = "AssociatedGeneName";
	private static final String GENES_REPOSITORY_ENSEMBL_GENE_ID_NAME = "EnsemblGeneID";
	private static final String GENES_REPOSITORY_NAME = "BioMartGenes";
	private static final String EXPR_IMPORT_REPOSITORY_NAME = "ExprImport";
	public static final String ID = "decon-cell";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	private static final Logger LOG = LoggerFactory.getLogger(CellCountsPredictorController.class);

	@Autowired
	MetaDataService metaDataService;

	@Autowired
	UuidGenerator generator;

	@Autowired
	private DataService dataService;

	@Autowired
	private MenuReaderService menuReaderService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private SavedScriptRunner scriptRunner;

	ExecutorService executorService = Executors.newFixedThreadPool(5);

	public CellCountsPredictorController()
	{
		super(URI);
	}

	@RequestMapping
	public String init(Model model)
	{
		 Entity runtimeProperty = dataService.findOne("RuntimeProperty", "AAAACUAABCEA3NXYJRHRKNIAAE");
		 model.addAttribute("runtimeProperty", runtimeProperty);
		 String menuUrl = getMenuUrl();
		 model.addAttribute("menuUrl", menuUrl);
		return "view-decon-cell";
	}

	@RequestMapping("/startPrediction")
	public String startPrediction(@RequestParam String resultSetRepositoryName, @RequestParam String importedEntity)
	{
		Preconditions.checkNotNull(resultSetRepositoryName);
		Preconditions.checkNotNull(importedEntity);
		LOG.info("Start cell count prediction for {} to {}...", importedEntity, resultSetRepositoryName);
		Callable<Void> task = () -> runAsSystem(() -> {
			try
			{
				scriptRunner.runScript("cellCountsPrediction", ImmutableMap.of("resultSetRepositoryName",
						resultSetRepositoryName, "importedEntity", importedEntity));
				LOG.info("Cell count prediction DONE. Results written to {}.", resultSetRepositoryName);

			}
			catch (Exception ex)
			{
				LOG.error("Error running cellCountsPrediction script for repositoryName {} and importedEntity {}.", ex);
			}
			return (Void) null;
		});
		executorService.submit(task);
		return "redirect:" + getMenuUrl() + "/stillRunning/" + resultSetRepositoryName;
	}

	@RequestMapping("/stillRunning/{resultSetRepositoryName}")
	public String stillRunning(@PathVariable String resultSetRepositoryName, Model model) throws InterruptedException
	{
		if (dataService.count(resultSetRepositoryName, new QueryImpl()) > 0)
		{
			Thread.sleep(1000);
			return "redirect:/menu/main/dataexplorer?entity=" + resultSetRepositoryName;
		}
		model.addAttribute("resultSetRepositoryName", resultSetRepositoryName);
		return "view-stillRunning";
	}

	@RequestMapping("/report/{id}")
	public String report(@PathVariable String id, Model model)
	{
		Entity exprImport = dataService.findOne(EXPR_IMPORT_REPOSITORY_NAME, id);
		long totalNumberOfMarkerGenesForCounts = dataService.count("cellcounts_MarkerGenes",
				QueryImpl.EQ("markerForCounts", true));
		long totalNumberOfMarkerGenesForPct = dataService.count("cellcounts_MarkerGenes",
				QueryImpl.EQ("markerForPct", true));

		int numberOfMarkerGenesForCountsImported = Iterables.size(exprImport.getEntities("markerGenesForCounts"));
		int numberOfMarkerGenesForPctImported = Iterables.size(exprImport.getEntities("markerGenesForPct"));

		String inputData = exprImport.getString("importedEntity");
		int numberOfSamplesImported = Iterables.size(dataService.getEntityMetaData(inputData).getAtomicAttributes())
				- 1;

		model.addAttribute("exprImport", exprImport);
		model.addAttribute("numberOfSamplesImported", numberOfSamplesImported);
		model.addAttribute("nrOfMarkerGenesForCounts", totalNumberOfMarkerGenesForCounts);
		model.addAttribute("nrOfMarkerGenesForPcts", totalNumberOfMarkerGenesForPct);
		model.addAttribute("numberOfMarkerGenesForCountsImported", numberOfMarkerGenesForCountsImported);
		model.addAttribute("numberOfMarkerGenesForPctImported", numberOfMarkerGenesForPctImported);
		model.addAttribute("cellCountsOk",
				numberOfMarkerGenesForCountsImported >= totalNumberOfMarkerGenesForCounts * 0.8);
		model.addAttribute("cellPctOk", numberOfMarkerGenesForPctImported >= totalNumberOfMarkerGenesForPct * 0.8);

		return "view-cellcounts-report";
	}

	@RequestMapping(value = "/readFile", method = POST)
	public String upload(HttpServletRequest request)
			throws IOException, ServletException, MolgenisInvalidFormatException, Exception
	{
		// System.out.println("upload");
		Part part = request.getPart("upload");
		if (part != null)
		{
			File file = FileUploadUtils.saveToTempFolder(part);
			RepositoryCollection repositoryCollection = new CsvRepositoryCollection(file);
			if (Iterables.size(repositoryCollection) == 1)
			{
				Repository r = Iterables.getFirst(repositoryCollection, null);
				// System.out.println(r.getEntityMetaData());
				// System.out.println(r.getEntityMetaData().getAtomicAttributes());
				String exprImportID = importCsvRepository(r);
				return "redirect:" + getMenuUrl() + "/report/" + exprImportID;
			}
		}
		throw new ServletException("Is iets misgegaan");
	}

	private String getMenuUrl()
	{
		return menuReaderService.getMenu().findMenuItemPath(ID);
	}

	private String importCsvRepository(Repository r)
	{
		Repository target = createImportRepository(r);
		Entity reportEntity = createReportEntity(target);

		// TODO put runAsSystem() back without exception X killing everything
		try
		{
			Callable<Void> task = () -> runAsSystem(() -> {
				Void importToTargetRepository = importToTargetRepository(r, target, reportEntity);
				return importToTargetRepository;
			});
			executorService.submit(task);
		}
		catch (Throwable e)
		{
			System.out.println(e);
		}
		return reportEntity.getIdValue().toString();
	}

	private Void importToTargetRepository(Repository r, Repository target, Entity report)
	{
		try
		{
			Iterable<Entity> transformedRows = transform(r,
					entityRow -> transformEntity(r.getEntityMetaData(), entityRow, target.getEntityMetaData()));
			target.add(filter(transformedRows, row -> row != null));
			updateImportEntityWithStatistics(target, report);
		}
		catch (RuntimeException e)
		{
			LOG.warn("Failed to import to target repository", e);
			updateReportEntityWithException(e, report);
		}
		return null;
	}

	private void updateReportEntityWithException(RuntimeException e, Entity report)
	{
		report.set("status", "FAILED");
		report.set("errorMessage", e.getMessage());
		dataService.update(EXPR_IMPORT_REPOSITORY_NAME, report);
	}

	/**
	 * Creates a new instance of ExprImport that describes how the current import went.
	 * 
	 * @param target
	 *            the repository that the expression data got imported to
	 * @return ID of the created instance
	 */
	private Entity createReportEntity(Repository target)
	{
		// maak import rapportje
		EntityMetaData exprImportMetaData = dataService.getEntityMetaData(EXPR_IMPORT_REPOSITORY_NAME);
		Entity exprImportEntity = new DefaultEntity(exprImportMetaData, dataService);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		exprImportEntity.set("importDate", sdf.format(new Date()));
		exprImportEntity.set("importedEntity", target.getEntityMetaData().getName());
		exprImportEntity.set("status", "RUNNING");
		dataService.add(EXPR_IMPORT_REPOSITORY_NAME, exprImportEntity);
		return exprImportEntity;
	}

	private void updateImportEntityWithStatistics(Repository target, Entity exprImportEntity)
	{
		// en de genen tellen welke marker genes hoeveel
		ArrayList<String> markerGenesForCounts = new ArrayList<String>();
		ArrayList<String> markerGenesForPct = new ArrayList<String>();
		Repository markerGenes = dataService.getRepository("cellcounts_MarkerGenes");

		// Check if gene is marker gene for counts and/or percentages.
		markerGenes.forEach(entity -> {
			if (entity.get("markerForCounts").equals(true))
			{
				markerGenesForCounts.add(entity.getString("ensemblId"));
			}
			if (entity.get("markerForPct").equals(true))
			{
				markerGenesForPct.add(entity.getString("ensemblId"));
			}
		});

		ArrayList<String> uploadedMarkerGenesForCounts = new ArrayList<String>();
		ArrayList<String> uploadedMarkerGenesForPct = new ArrayList<String>();
		target.forEach(entity -> {
			if (markerGenesForCounts.contains(entity.getString("gene")))
			{
				uploadedMarkerGenesForCounts.add(entity.getString("gene"));
			}
			if (markerGenesForPct.contains(entity.getString("gene")))
			{
				uploadedMarkerGenesForPct.add(entity.getString("gene"));
			}
		});

		exprImportEntity.set("markerGenesForCounts", uploadedMarkerGenesForCounts);
		exprImportEntity.set("markerGenesForPct", uploadedMarkerGenesForPct);
		exprImportEntity.set("status", "FINISHED");
		dataService.update("ExprImport", exprImportEntity);
	}

	Entity transformEntity(EntityMetaData entityMetaData, Entity entityRow, EntityMetaData targetEntityMetaData)
	{
		Entity result = new DefaultEntity(targetEntityMetaData, dataService);

		boolean firstAttribute = true;
		for (AttributeMetaData sourceAttribute : entityMetaData.getAtomicAttributes())
		{
			String cellValue = entityRow.getString(sourceAttribute.getName());
			if (firstAttribute)
			{
				String ensemblID = convertToEnsemblID(cellValue);
				if (ensemblID == null)
				{
					LOG.warn("unknown gene: " + cellValue);
					ensemblID = cellValue;
				}
				result.set("gene", ensemblID);
				firstAttribute = false;
			}
			else
			{

				result.set(sourceAttribute.getName(), convertToDecimal(cellValue));
			}
		}

		return result;
	}

	Double convertToDecimal(String expression)
	{
		if (StringUtils.isEmpty(expression))
		{
			return null;
		}
		return Double.valueOf(expression);

	}

	private String convertToEnsemblID(String geneIdentifier)
	{
		Entity gene = dataService.findOne(GENES_REPOSITORY_NAME,
				QueryImpl.EQ(GENES_REPOSITORY_ENSEMBL_GENE_ID_NAME, geneIdentifier).or()
						.eq(GENES_REPOSITORY_ASSOCIATED_GENE_NAME, geneIdentifier));
		if (gene == null)
		{
			return null;
			// TODO: get proper version of the source table
			// throw new MolgenisDataException("Unknown gene identifier: " + geneIdentifier);
		}

		return gene.getString(GENES_REPOSITORY_ENSEMBL_GENE_ID_NAME);

	}

	Repository createImportRepository(Repository r)
	{

		String importedExpressionData = generator.generateId();
		DefaultEntityMetaData emd = new DefaultEntityMetaData(importedExpressionData);
		emd.setPackage(new PackageImpl("expression", "Uploaded expression data"));
		emd.addAttribute("gene").setIdAttribute(true).setNillable(false);
		emd.addAllAttributeMetaData(transform(skip(r.getEntityMetaData().getAtomicAttributes(), 1),
				attr -> new DefaultAttributeMetaData(attr.getName(), FieldTypeEnum.DECIMAL)));

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		emd.setLabel("Uploaded Expression data " + r.getName() + " " + sdf.format(new Date()));

		permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
				Arrays.asList(emd.getName()));

		return metaDataService.addEntityMeta(emd);
	}

	/**
	 * 
	 * @param uploadID
	 * @return
	 */
	@RequestMapping(value = "/createResultsRepository", method = POST)
	public @ResponseBody String createResultsRepository(@RequestParam String uploadID)
	{
		String resultSetRepositoryName = generator.generateId();
		DefaultEntityMetaData newMetaData = createResultsSetEntityMetaData(resultSetRepositoryName, uploadID);
		permissionSystemService.giveUserEntityPermissions(SecurityContextHolder.getContext(),
				Arrays.asList(newMetaData.getName()));
		metaDataService.addEntityMeta(newMetaData);
		return newMetaData.getName();
	}

	/**
	 * 
	 * @param resultSetRepositoryName
	 * @param uploadID
	 * @return
	 */
	private DefaultEntityMetaData createResultsSetEntityMetaData(String resultSetRepositoryName, String uploadID)
	{
		String resultsSetLabel = generateResultsSetLabel(uploadID);
		EntityMetaData resultSetMetaData = dataService.getEntityMetaData("sample_ResultCellCounts");
		DefaultEntityMetaData newMetaData = new DefaultEntityMetaData(resultSetRepositoryName, resultSetMetaData);
		newMetaData.setLabel(resultsSetLabel);
		return newMetaData;
	}

	/**
	 * 
	 * @param uploadID
	 * @return
	 */
	private String generateResultsSetLabel(String uploadID)
	{
		Entity upload = dataService.findOne(EXPR_IMPORT_REPOSITORY_NAME, uploadID);
		String importedEntity = upload.getString("importedEntity"); // this is SampleData
		EntityMetaData importedEntityMetaData = dataService.getEntityMetaData(importedEntity);
		String uploadedEntityLabel = importedEntityMetaData.getLabel();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		String label = "Cell Count Prediction for " + uploadedEntityLabel + " " + sdf.format(new Date());
		return label;
	}

}
