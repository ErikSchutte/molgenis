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
			 startPrediction(data, importedEntity);
		 }
	});
	}

	function startPrediction(resultSetRepositoryName, importedEntity) {
		$.get('http://localhost:8080/scripts/cellCountsPrediction/run', {"resultSetRepositoryName" : resultSetRepositoryName, "importedEntity" : importedEntity}, function(data, status) {
			 window.location = "http://localhost:8080/menu/main/dataexplorer?entity=" + resultSetRepositoryName;
		}, 'html');

//		$("#response").html("");
//		$("#response").append("" + '<a href="http://localhost:8080/menu/main/dataexplorer?entity=sample_ResultCellCounts" target="_blank">Show output</a>' + "")
	}

}($, window.top.molgenis = window.top.molgenis || {}));
