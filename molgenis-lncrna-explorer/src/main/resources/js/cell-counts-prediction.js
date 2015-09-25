(function($, molgenis) {
	
	
	$(function() {
		$('#get-counts-test').on('click', function() {
			window.location = '/menu/main/dataexplorer?entity=sample_AAAACUADTASIFNXYJRHRKNIAAE'
		});
	})
	

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
		window.location = '/menu/main/decon-cell/startPrediction?resultSetRepositoryName=' + resultSetRepositoryName + '&importedEntity=' + importedEntity;
	}

}($, window.top.molgenis = window.top.molgenis || {}));
