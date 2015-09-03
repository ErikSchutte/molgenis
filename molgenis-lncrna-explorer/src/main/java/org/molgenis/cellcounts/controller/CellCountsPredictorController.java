package org.molgenis.cellcounts.controller;

import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Iterables.transform;
import static org.molgenis.cellcounts.controller.CellCountsPredictorController.URI;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
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
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.util.FileUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Iterables;

@Controller
@RequestMapping(URI)
public class CellCountsPredictorController extends MolgenisPluginController
{
	private static final String EXPR_IMPORT_REPOSITORY_NAME = "ExprImport";
	public static final String ID = "cellcounts";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	@Autowired
	MetaDataService metaDataService;

	@Autowired
	UuidGenerator generator;

	@Autowired
	private DataService dataService;

	// @Autowired

	public CellCountsPredictorController()
	{
		super(URI);
	}

	@RequestMapping
	public String init(Model model)
	{
		return "view-lncrnaexplorer";
	}

	@RequestMapping("/report/{id}")
	public String report(@PathVariable String id, Model model)
	{
		Entity exprImport = dataService.findOne(EXPR_IMPORT_REPOSITORY_NAME, id);
		long totalNumberOfMarkerGenesForCounts = dataService.count("ModelGene", QueryImpl.EQ("markerForCounts", true));
		long totalNumberOfMarkerGenesForPct = dataService.count("ModelGene", QueryImpl.EQ("markerForPct", true));

		int numberOfMarkerGenesForCountsImported = Iterables.size(exprImport.getEntities("markerGenesForCounts"));
		int numberOfMarkerGenesForPctImported = Iterables.size(exprImport.getEntities("markerGenesForPct"));
		int numberOfSamplesImported = 0; // TODO

		model.addAttribute("exprImport", exprImport);
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
	public @ResponseBody String upload(HttpServletRequest request)
			throws IOException, ServletException, MolgenisInvalidFormatException
	{
		System.out.println("upload");
		Part part = request.getPart("upload");
		if (part != null)
		{
			System.out.println("part != null");
			File file = FileUploadUtils.saveToTempFolder(part);
			RepositoryCollection repositoryCollection = new CsvRepositoryCollection(file);
			if (Iterables.size(repositoryCollection) == 1)
			{
				Repository r = Iterables.getFirst(repositoryCollection, null);
				System.out.println(r.getEntityMetaData());
				System.out.println(r.getEntityMetaData().getAtomicAttributes());
				String exprImportID = importCsvRepository(r);
				return exprImportID;
			}
		}
		throw new ServletException("Is iets misgegaan");
	}

	String importCsvRepository(Repository r)
	{
		Repository target = createImportRepository(r);
		target.add(Iterables.transform(r, entityRow -> transformEntity(r.getEntityMetaData(), entityRow, target.getEntityMetaData())));
		return createExpressionImport(r);
	}

	/**
	 * Creates a new instance of ExprImport that describes how the current import went.
	 * 
	 * @param target
	 *            the repository that the expression data got imported to
	 * @return ID of the created instance
	 */
	String createExpressionImport(Repository target)
	{
		// maak import rapportje
		EntityMetaData exprImportMetaData = dataService.getEntityMetaData(EXPR_IMPORT_REPOSITORY_NAME);
		Entity exprImportEntity = new DefaultEntity(exprImportMetaData, dataService);

		// en nou de samples tellen die in target zitten
		// en de genen tellen welke marker genes hoeveel

		dataService.add(EXPR_IMPORT_REPOSITORY_NAME, exprImportEntity);
		return exprImportEntity.getString("id");
	}

	Entity transformEntity(EntityMetaData entityMetaData, Entity entityRow, EntityMetaData targetEntityMetaData)
	{
		Entity result = new DefaultEntity(targetEntityMetaData, dataService);

		boolean firstAttribute = true;
		for (AttributeMetaData sourceAttribute : entityMetaData.getAtomicAttributes())
		{
			if (firstAttribute)
			{
				String ensemblID = convertToEnsemblID(entityRow.getString(sourceAttribute.getName()));

				result.set("gene", ensemblID);
				firstAttribute = false;
			}
			else
			{

				result.set(sourceAttribute.getName(), convertToDecimal(entityRow.getString(sourceAttribute.getName())));
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
		Entity gene = dataService.findOne("genes",
				QueryImpl.EQ("EnsemblGeneID", geneIdentifier).or().eq("GeneName", geneIdentifier));
		if (gene == null)
		{
			throw new MolgenisDataException("Unknown gene identiefier: " + geneIdentifier);
		}

		return gene.getString("EnsemblGeneID");

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
