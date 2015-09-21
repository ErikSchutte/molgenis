package org.molgenis.cellcounts.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.PackageImpl;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes =
{ CellCountsPredictorControllerTest.Config.class })
public class CellCountsPredictorControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private DataService dataService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	CellCountsPredictorController controller;
	
	@Autowired
	PermissionSystemService permissionSystemService;

	@Autowired
	UuidGenerator generator;

	private MapEntity exprImport;
	private Iterator<AttributeMetaData> numberOfAttributes;

	@BeforeMethod
	public void beforeTest()
	{
		exprImport = new MapEntity();
		exprImport.set("id", "12345");
		exprImport.set("importedEntity", "sample");
		exprImport.set("markerGenesForCounts", Collections.emptyList());
		exprImport.set("markerGenesForPct", Collections.emptyList());
		Mockito.reset(dataService, metaDataService, permissionSystemService, generator);
		when(dataService.findOne("ExprImport", "12345")).thenReturn(exprImport);
		
		DefaultEntityMetaData sampleMetaData = new DefaultEntityMetaData("sample");
		sampleMetaData.addAttribute("Gene");
		sampleMetaData.addAttribute("Sample1");
		sampleMetaData.addAttribute("Sample2");
		sampleMetaData.addAttribute("Sample3");
		when(dataService.getEntityMetaData("sample")).thenReturn(sampleMetaData);
		
	}

	@Test
	public void testReportStillRunning()
	{
		exprImport.set("status", "RUNNING");
		
		Model model = new ExtendedModelMap();
		controller.report("12345", model);

		Assert.assertEquals(model.asMap().get("exprImport"), exprImport);
		Assert.assertEquals(model.asMap().get("numberOfSamplesImported"), 3);
	}

	@Test
	public void testReportDone()
	{
		exprImport.set("status", "FINISHED");

		MapEntity gene1 = new MapEntity();
		MapEntity gene2 = new MapEntity();
		MapEntity gene3 = new MapEntity();
		exprImport.set("markerGenesForCounts", Arrays.asList(gene1, gene2));
		exprImport.set("markerGenesForPct", Arrays.asList(gene1, gene2, gene3));
		AttributeMetaData attributeMetaData1 = new DefaultAttributeMetaData("test_attr_1");
		AttributeMetaData attributeMetaData2 = new DefaultAttributeMetaData("test_attr_2");
		numberOfAttributes = asList(attributeMetaData1, attributeMetaData2)
				.iterator();

		when(dataService.findOne("ExprImport", "12345")).thenReturn(exprImport);
		when(dataService.count("cellcounts_MarkerGenes", QueryImpl.EQ("markerForCounts", true))).thenReturn(15l);
		when(dataService.count("cellcounts_MarkerGenes", QueryImpl.EQ("markerForPct", true))).thenReturn(3l);

		Model model = new ExtendedModelMap();
		controller.report("12345", model);

		assertEquals(model.asMap().get("exprImport"), exprImport);

		assertEquals(model.asMap().get("cellCountsOk"), false);
		assertEquals(model.asMap().get("cellPctOk"), true);
	}

	@Test
	public void testReportDoneAllOk()
	{
		exprImport.set("status", "FINISHED");

		MapEntity gene1 = new MapEntity();
		MapEntity gene2 = new MapEntity();
		MapEntity gene3 = new MapEntity();
		exprImport.set("markerGenesForCounts", Arrays.asList(gene1, gene2));
		exprImport.set("markerGenesForPct", Arrays.asList(gene1, gene2, gene3));

		when(dataService.findOne("ExprImport", "12345")).thenReturn(exprImport);
		when(dataService.count("ModelGene", QueryImpl.EQ("markerForCounts", true))).thenReturn(2l);
		when(dataService.count("ModelGene", QueryImpl.EQ("markerForPct", true))).thenReturn(3l);

		Model model = new ExtendedModelMap();
		controller.report("12345", model);

		assertEquals(model.asMap().get("exprImport"), exprImport);

		assertEquals(model.asMap().get("cellCountsOk"), true);
		assertEquals(model.asMap().get("cellPctOk"), true);
	}

	@Test
	public void testCreateResultsRepository()
	{
		when(dataService.findOne("ExprImport", "12345")).thenReturn(exprImport);
		exprImport.set("status", "FINISHED");
		exprImport.set("importedEntity", "Expression_Data_1234");

		DefaultEntityMetaData importMetaData = new DefaultEntityMetaData("ImportedExpressionCounts");
		importMetaData.setLabel("My First Expression Data");
		when(dataService.getEntityMetaData("Expression_Data_1234")).thenReturn(importMetaData);

		DefaultEntityMetaData resultCellCountMetaData = new DefaultEntityMetaData("ResultCellCounts");
		resultCellCountMetaData.setPackage(new PackageImpl("samplePackage", "The sample package"));
		when(dataService.getEntityMetaData("sample_ResultCellCounts")).thenReturn(resultCellCountMetaData);

		when(generator.generateId()).thenReturn("UniqueID");

		String resultSetName = controller.createResultsRepository("12345");

		assertEquals(resultSetName, "samplePackage_UniqueID");

		ArgumentCaptor<EntityMetaData> argumentCaptor = ArgumentCaptor.forClass(EntityMetaData.class);
		verify(metaDataService).addEntityMeta(argumentCaptor.capture());

		EntityMetaData resultSetEntityMetaData = argumentCaptor.getValue();
		assertEquals(resultSetEntityMetaData.getSimpleName(), "UniqueID");
		assertEquals(resultSetEntityMetaData.getName(), "samplePackage_UniqueID");
		assertTrue(
				resultSetEntityMetaData.getLabel().startsWith("Cell Count Prediction for My First Expression Data "));
	}

	@Configuration
	public static class Config
	{
		@Bean
		DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		UuidGenerator generator()
		{
			return mock(UuidGenerator.class);
		}

		@Bean
		CellCountsPredictorController controller()
		{
			return new CellCountsPredictorController();
		}

		@Bean
		MolgenisPluginRegistry pluginRegistry()
		{
			return mock(MolgenisPluginRegistry.class);
		}

		@Bean
		MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
		}

		@Bean
		MenuReaderService menuReaderService()
		{
			return mock(MenuReaderService.class);
		}
		
		@Bean
		PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

	}
}
