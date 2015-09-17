(function($, molgenis) {

	$(function() {
		$('[data-toggle="popover"]').popover()
	})

	$(function() {
		$('ul.pager a').on('click', function(e) {
			e.preventDefault();
			if (!$(this).parent().hasClass('disabled')) {
				var a = $(this);
				showSpinner(function() {
					var form = $('#wizardForm');
					form.attr('action', a.attr('href'));
					form.submit();
				});
			}

			return false;
		});

		$('input[type=file]').bootstrapFileInput();
		$('.file-inputs').bootstrapFileInput();

		// $('#upload-file').on('click', function() {
		// submitFile()

		// })

	});

	function submitFile() {

		submittedFile = $('#input-file').val();

		$.ajax({
			type : 'POST',
			url : "http://localhost:8080/menu/main/cellcounts" + "/readFile",
			contentType : 'application/json',
			data : submittedFile,
			success : function(data) {

			}
		})
	}
	;

}($, window.top.molgenis = window.top.molgenis || {}));