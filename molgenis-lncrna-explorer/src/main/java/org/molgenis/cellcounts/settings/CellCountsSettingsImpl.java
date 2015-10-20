package org.molgenis.cellcounts.settings;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.cellcounts.controller.CellCountsPredictorController.ID;

import org.molgenis.data.settings.DefaultSettingsEntity;
import org.molgenis.data.settings.DefaultSettingsEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class CellCountsSettingsImpl extends DefaultSettingsEntity implements CellCountsSettings
{
	private static final long serialVersionUID = 1L;

	public CellCountsSettingsImpl()
	{
		super(ID);
	}

	@Component
	private static class Meta extends DefaultSettingsEntityMetaData
	{
		private static final String SAMPLE_DATASET_IMPORT_ID = "sampleDatasetImportId";
		private static final String SAMPLE_CELLCOUNT_PREDICTION_ENTITY = "sampleCellcountPrediction";

		public Meta()
		{
			super(ID);
			setLabel("Decon2 settings");
			setDescription("Settings for the Decon2 plugin.");

			addAttribute(SAMPLE_DATASET_IMPORT_ID).setDataType(STRING).setLabel("Sample dataset import id")
					.setDescription("Id of the ExprImport row that describes the use of the sample data.");
			addAttribute(SAMPLE_CELLCOUNT_PREDICTION_ENTITY).setDataType(STRING)
					.setLabel("Sample cellcounts prediction entity name")
					.setDescription("Name of the cell counts prediction entity for the sample dataset.");
		}
	}

	@Override
	public String getSampleDatasetImportId()
	{
		return getString(Meta.SAMPLE_DATASET_IMPORT_ID);
	}

	@Override
	public void setSampleDatasetImportId(String id)
	{
		set(Meta.SAMPLE_DATASET_IMPORT_ID, id);

	}

	@Override
	public String getSampleCellcountPredictionEntity()
	{
		return getString(Meta.SAMPLE_CELLCOUNT_PREDICTION_ENTITY);
	}

	@Override
	public void setSampleCellcountPredictionEntity(String value)
	{
		set(Meta.SAMPLE_CELLCOUNT_PREDICTION_ENTITY, value);

	}

}
