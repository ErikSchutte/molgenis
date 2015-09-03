(function($, molgenis) {

	var submittedValue;

	$(function() {

		$('#submit-input').on('click', function() {
			submitUserInput()

		})

		$('#search-input').keypress(function(e) {
			if (e.which == 13) {
				submitUserInput()
			}

		})

		$('#myModal').on('shown.bs.modal', function() {
			$('#myInput').focus()
		})

		$("#pop").on("click", function() {
			$('#imagepreview').attr('src', $('.pop img').attr('src'));
			$('#imagemodal').modal('show');

		});

		// For the cell counts predictor
		$('#upload-data').on('click', function() {
			// todo
			alert('test');

		})

		var component = React.render(molgenis.ui.EntitySelectBox({
			entity : 'genes',
			mode : 'view',
			name : "name",
			disabled : false,
			readOnly : false,
			multiple : true,
			required : true,
			placeholder : 'Please select one or more Gene Names',
			focus : false,
			value : [],
			onValueChange : function(event) {
				console.log('onValueChange', event);
				// React.render(React.DOM.textarea({
				// placeholder : 'Component logging comes here..',
				// readOnly : true,
				// rows: 10,
				// cols: 100,
				// value: JSON.stringify(event)
				// }), $('#entitySelectBox div.log')[0]);
				submittedValue = event;
			}
		}), $('#entitySelectBox div.component')[0]);

	})

	function submitUserInput() {

		var genes = '';

		for (i = 0; i < submittedValue.value.length; i++) {
			genes += submittedValue.value[i].GeneNames + ',';
			console.log(submittedValue.value[i].GeneNames);
		}

		console.log(genes);

		$("#ajaxResponse").html("");
		$("#ajaxResponse").append(""
		// + '<div role="tabpanel" class="col-md-12 col-md-offset-1"><ul
		// class="nav nav-tabs" role="tablist"><li role="tab" class="active"><a
		// href="#expression" aria-controls="expression" role="tab"
		// data-toggle="tab">Expression Data</a></li><li role="tab"><a
		// href="#test" aria-controls="test" role="tab"
		// data-toggle="tab">Test</a></li></ul><div class="tab-content"><div
		// class="tab-pane active" id="expression">'
		+ '<img src="http://localhost:8080/scripts/generateExpression%28rpkm%29Heatmap/run?genes=' + genes + '">'
		// + '<a data-toggle="modal" data-target="#myModal"><span id="info"
		// class="glyphicon glyphicon-info-sign" aria-hidden="true"
		// style="font-size:1.5em;"></span> </a>'
		// + '</div><div class="tab-pane" id="test">Test test test</div></div>'
		// + "")
		+ "")

	}

	// function submitUserInput() {
	//
	// var submittedValue = $('#search-input').val();
	// var failed = '';
	// var success = '';
	//
	// $
	// .ajax({
	// type : 'POST',
	// url : molgenis.getContextUrl() + "/validate",
	// contentType : 'application/json',
	// data : JSON.stringify(submittedValue),
	// success : function(data) {
	//
	// if (data.search('Fail') != -1) {
	// var genes = data.split(",");
	// for (el in genes) {
	// if (genes[el].search('Fail') != -1) {
	// console.log(genes[el].split(':'));
	// failed += genes[el].split(':')[1] + ' ';
	// }
	//
	// }
	//
	// molgenis.createAlert([ {
	// message : 'Could not find gene(s) with gene name: ' + failed + ''
	// } ], 'warning');
	//
	// } else {
	// var genes = data.split(",");
	// for (el in genes) {
	// success += genes[el].split(':')[1] + ',';
	// }
	// console.log(success);
	// $("#ajaxResponse").html("");
	// // $("#ajaxResponse")
	// // .append(
	// // ""
	// //// + '<div class="col-sm-6 col-md-4"> <div class="thumbnail">'
	// // + '<a href="#" class="pop"> <img id="imageresource"
	// src="http://localhost:8080/scripts/generateExpression%28rpkm%29Heatmap/run?genes='
	// + success + '"></a>'
	// //// + '<div class="caption"> <h3>Thumbnail label</h3> <p>...</p> <p>'
	// //// + '<a href="#" class="btn btn-primary" role="button">Button</a> <a
	// href="#" class="btn btn-default" role="button">Button</a></p>'
	// //// + '</div> </div> </div>');
	// // + "");
	//							
	//							
	// $("#ajaxResponse")
	// .append(
	// ""
	// + '<div role="tabpanel" class="col-md-12 col-md-offset-1"><ul class="nav
	// nav-tabs" role="tablist"><li role="tab" class="active"><a
	// href="#expression" aria-controls="expression" role="tab"
	// data-toggle="tab">Expression Data</a></li><li role="tab"><a href="#test"
	// aria-controls="test" role="tab" data-toggle="tab">Test</a></li></ul><div
	// class="tab-content"><div class="tab-pane active" id="expression">'
	// + '<img
	// src="http://localhost:8080/scripts/generateExpression%28rpkm%29Heatmap/run?genes='
	// + success + '">'
	// // + '<a data-toggle="modal" data-target="#myModal"><span id="info"
	// class="glyphicon glyphicon-info-sign" aria-hidden="true"
	// style="font-size:1.5em;"></span> </a>'
	// + '</div><div class="tab-pane" id="test">Test test test</div></div>' +
	// "")
	//
	// }
	// }
	//
	// })
	//
	// }

}($, window.top.molgenis = window.top.molgenis || {}));