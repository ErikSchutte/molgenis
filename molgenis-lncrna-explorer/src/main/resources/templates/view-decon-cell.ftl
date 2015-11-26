<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
				"decon-cell.css"]>

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
		

			<div class="col-md-6" style="font-size:11pt; text-align:justify">
				<p>&nbsp;</p> 
				<p><span>Decon-cell is an algorithm to predict cell proportions, or counts, in a heterogeneous sample using genome-wide molecular expression profiles.<br /></span>
				<span>We have built models for cell counts/percentages prediction of five White Blood Cells (<strong>neutrophils</strong>, <strong>lymphocytes</strong>, <strong>monocytes</strong>, <strong>eosinophils</strong>, <strong>basophils</strong>) using RNA-seq data and cell counts data from two Dutch cohorts: 626 and 680 samples from LLDeep and LLS cohorts.<br />
				Users can input their own expression data of samples with unknown cell counts and this web tool will provide the predicted cell counts/percentage within a few minutes.</span></p> 
				<p style="text-align: left;">&nbsp;</p> 
   			 </div>

			<div class="col-sm-6 col-md-4 col-md-offset-1">
      			<img src="https://molgenis04.target.rug.nl/files/AAAACUAY2MLAFNXYJRHRKNIAAE" alt="Decon-cell" style="min-height:280px;height:280px;">
				<p style="text-align: left;">&nbsp;</p> 
			</div>
		</div>
	</div>
	
	<div class="row">
		<div class="col-md-11">
			<hr>
		</div>
	</div>



	<div class="row">
		<div class="col-md-3 col-md-offset-1"  style="font-size:12pt;">
			Enter your expression table.  
			<span class="glyphicon glyphicon-question-sign" aria-hidden="true" style="font-size:1.0em;" data-toggle="popover" data-content="Enter a input matrix in .csv format, with expression counts of one or more samples, library size corrected (number of expression counts/number of total expression counts). The selected human reference assembly is GRCh37, version 71."</span>					

		</div>
		<div class="col-md-6" style="font-size:12pt">	
			<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="/menu/main/decon-cell/readFile" role="form">	
				<input type="file" name="upload" data-filename-placement="inside" title="Select a file..." id="input-file">
				<button type="submit" class="btn btn-success" id="upload-file">Upload</button></br>
				<#if plugin_settings.sampleDatasetImportId?has_content><a href="${menuUrl}/report/${plugin_settings.sampleDatasetImportId}"> Or start prediction with test data!</a></#if>
			</form> 
		</div>
		<p style="text-align: left;">&nbsp;</p> 
		<p style="text-align: left;">&nbsp;</p> 
	</div>



<@footer/>