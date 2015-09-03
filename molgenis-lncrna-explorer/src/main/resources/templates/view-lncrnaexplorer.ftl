<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
				"lncrna-explorer.css",
			 	"jquery.molgenis.tree.css",
				"ui.fancytree.min.css"]>
<#assign js=[
				"lncrna-explorer.js",
				"file-upload.js",
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
			<li role="tab"><a href="#cell-counts" aria-controls="cell-counts" role="presentation" data-toggle="tab">Cell Counts Predictor</a></li>
		</ul>

		<div class="tab-content">
			<div  role="tabpanel" class="tab-pane active" id="lncrna-explorer">
				<div class="row">
					<div class="col-md-11">
	   					 <h1> LncRNA Explorer </h1>
					</div>
				</div>


				<div class="row">
					<div class="col-md-4 col-md-offset-4">
						<div id="entitySelectBox">
						<#--<div class="log"></div>-->
							<div class="component" id="entitySelectBoxComponent"></div>
								<span class="input-group-btn">
      	 							<button type="btn" class="btn btn-default" id="submit-input">Search</button>
     	 						</span>
							<div class="source"></div>			
						</div>
					</div>
				</div>


<#--
<div class="row">
	<div class="col-md-4 col-md-offset-4">

    	<div class="input-group">
		
      		<input type="text" class="form-control" name="searchTerm" id="search-input" placeholder="GeneName">	
		<#--<i class="glyphicon glyphicon-search form-control-feedback" aria-hidden="true"></i>--><#--
      		<span class="input-group-btn">
      	 		<button type="btn" class="btn btn-default" id="submit-input">Search</button>
     	 	</span>
   	 	</div>
	</div>
</div>
-->


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
<#--
<div class="row">
	<div role="tabpanel" class="col-md-10 col-md-offset-1">
		<ul class="nav nav-tabs" role="tablist">
  			<li role="tab" class="active"><a href="#expression" aria-controls="expression" role="tab" data-toggle="tab">Expression Data</a></li>
  			<li role="tab"><a href="#test" aria-controls="test" role="tab" data-toggle="tab">Test</a></li>
		</ul>

	<div class="tab-content">
		<div class="tab-pane active" id="expression">
			<div class="col-md-10" id="ajaxResponse"></div>
		</div>

		<div class="tab-pane" id="test">
			Test test test
		</div>
	</div>
</div>
-->

<#--
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
 	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title">Information</h4>
			</div>
    		<div class="modal-body">
    			Information about the plot and the data...<br>
				Test test test<br>
				...
    		</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
  		</div>
	</div>
</div>
-->

		
			<div role="tabpanel" class="tab-pane" id="cell-counts">		
				<form method="post" id="wizardForm" name="wizardForm" enctype="multipart/form-data" action="http://localhost:8080/menu/main/cellcounts/readFile" role="form">	
					<input type="file" name="upload" data-filename-placement="inside" title="Select a file..." id="input-file">

					<button type="submit" class="btn btn-success" id="upload-file">Upload</button>
				</form>
			</div>
		</div>
	</div>
</div>

<@footer/>