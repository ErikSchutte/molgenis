(function($, molgenis) {
	

	$(function() {
		$('#get-counts').on('click', function() {
			createResultsSetRepository($(this).data('upload-id'),$(this).data('sample-name'));
		});
	})
	
	function createResultsSetRepository(uploadID, importedEntity){
//		alert(uploadID);
		
		$.ajax({
		 type : 'POST',
		 url : molgenis.getContextUrl() + "/createResultsRepository",
		 data : {'uploadID': uploadID},
		 success : function(data) {
			 startPrediction(data, importedEntity, menuUrl);
		 }
	});
	}

	function startPrediction(resultSetRepositoryName, importedEntity) {
		$.get('https://molgenis04.target.rug.nl/menu/plugins/scripts/cellCountsPrediction/run', {"resultSetRepositoryName" : resultSetRepositoryName, "importedEntity" : importedEntity}, function(data, status) {
			 window.location = "https://molgenis04.target.rug.nl/menu/main/dataexplorer?entity=" + resultSetRepositoryName;
		}, 'html');

//		$("#response").html("");
//		$("#response").append("" + '<a href="http://localhost:8080/menu/main/dataexplorer?entity=sample_ResultCellCounts" target="_blank">Show output</a>' + "")
	}

}($, window.top.molgenis = window.top.molgenis || {}));
