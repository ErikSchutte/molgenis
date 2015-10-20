$.when($, window.top.molgenis = window.top.molgenis || {}, molgenis.getPluginSettings()).then(function($, molgenis, settingsXhr) {

	console.log(settingsXhr[0]);
	$(function() {
		$('#get-counts-test').on('click', function() {
			window.location = '/menu/main/dataexplorer?entity=' + settingsXhr[0].sampleCellcountPrediction
		});
	})

	$(function() {
		$('#get-counts').on('click', function() {
			createResultsSetRepository($(this).data('upload-id'), $(this).data('sample-name'));
		});
	})

	function createResultsSetRepository(uploadID, importedEntity) {
		// alert(uploadID);

		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + "/createResultsRepository",
			data : {
				'uploadID' : uploadID
			},
			success : function(data) {
				startPrediction(data, importedEntity);
			}
		});
	}

	function startPrediction(resultSetRepositoryName, importedEntity) {
		window.location = '/menu/main/deconCell/startPrediction?resultSetRepositoryName=' + resultSetRepositoryName + '&importedEntity=' + importedEntity;
	}

});
