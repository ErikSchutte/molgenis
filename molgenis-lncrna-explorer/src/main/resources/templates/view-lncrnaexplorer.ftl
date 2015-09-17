<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
				"lncrna-explorer.css",
			 	"jquery.molgenis.tree.css",
				"ui.fancytree.min.css"]>
<#assign js=[
				"lncrna-explorer.js",
				"file-upload.js",
				"deconvolution.js"
				"bootstrap.file-input.js",
				"jquery.bootstrap.wizard.min.js",
				"jquery.fancytree.min.js",
				"jquery.molgenis.tree.js",
				"jquery.molgenis.xrefmrefsearch.js",
				"bootbox.min.js",
				"jQEditRangeSlider-min.js"]>

<@header css js/>

<div class="col-md-12">
	<div role= "tabpanel">
		<ul class="nav nav-tabs" role="tablist">
			<li role="tab" class="active"> <a href="#lncrna-explorer" aria-controls="lncrna-explorer" role="presentation" data-toggle="tab">LncRNA Explorer</a></li>
			<li role="tab"><a href="#cell-counts" aria-controls="cell-counts" role="presentation" data-toggle="tab">Decon-cell</a></li>
			<li role="tab"><a href="#deconvolution-plots" aria-controls="deconvolution-plots" role="presentation" data-toggle="tab">Decon-eQTL</a></li>
		</ul>

		<div class="tab-content">
			<div role="tabpanel" class="tab-pane active" id="lncrna-explorer">
				<div class="row">
					<div class="col-md-11">
	   					 <h2> LncRNA Explorer</h2>
					</div>
				</div>


				<div class="row">
					<div class="col-md-4 col-md-offset-4">
						<div id="entitySelectBox">
							<div class="component" id="entitySelectBoxComponent"></div>
								<span class="input-group-btn">
      	 							<button type="btn" class="btn btn-default" id="submit-input">Search</button>
     	 						</span>
							<div class="source"></div>			
						</div>
					</div>
				</div>



				<div class="row">
					<div class="col-md-11">
						<p>
			
						</p>
					</div>		
				</div>

				<div class="row">
					<div class="col-md-10" id="ajaxResponse"></div>
				</div>
			</div>


		
			<div role="tabpanel" class="tab-pane" id="cell-counts">		
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
					<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="http://localhost:8080/menu/main/cellcounts/readFile" role="form">	
						<input type="file" name="upload" data-filename-placement="inside" title="Select a file..." id="input-file">
						<button type="submit" class="btn btn-success" id="upload-file">Upload</button>
						<a href="http://localhost:8080/menu/main/cellcounts/report/AAAACT6LDTKHHWFCL2LYKRIAAE"> Or start prediction with test data!</a>
					</form> 
				</div>
				</div>
			</div>


			<div role="tabpanel" class="tab-pane" id="deconvolution-plots">		
				<div class="row">
				<div class="col-md-10">
	   				<h2> Deconvoluted eQTLs on immune disease associated SNPs</h2>
				</div>
				<div class="col-md-2">
					<button type="btn" class="btn btn-info" id="show-legend" rel="popover" data-content="">Show tutorial <span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span></button>
				</div>
				</div>
				<div id="deconvolution">
				</div>
			</div>


		</div>
	</div>
</div>

<@footer/>