<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign js=[
				"file-upload.js",
				"bootstrap.file-input.js",
				"jquery.bootstrap.wizard.min.js",
				"jquery.fancytree.min.js",
				"jquery.molgenis.tree.js",
				"jquery.molgenis.xrefmrefsearch.js",
				"bootbox.min.js",
				"jQEditRangeSlider-min.js"]>

<@header css js/>

				<div class="row">
				<div class="col-md-11">
	   				 <h2> Decon-cell of human blood</h2>
				</div>
				<div class="col-md-12">
					Enter your expression table.  
					<span class="glyphicon glyphicon-question-sign" aria-hidden="true" style="font-size:1.2em;" data-toggle="popover" data-content="Enter a input matrix in .csv format, with expression counts of one or more samples, library size corrected (number of expression counts/number of total expression counts). The selected human reference assembly is GRCh37, version 71."</span>					


				</div>
				</div>

				<div class="row">
				<div class="col-md-6 col-md-offset-2">	
					<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="/menu/main/decon-cell/readFile" role="form">	
						<input type="file" name="upload" data-filename-placement="inside" title="Select a file..." id="input-file">
						<button type="submit" class="btn btn-success" id="upload-file">Upload</button></br>
						<#--<a href="${menuUrl}/report/${runtimeProperty.get("value")}"> Or start prediction with test data!</a>-->
					</form> 
				</div>
				</div>



<@footer/>