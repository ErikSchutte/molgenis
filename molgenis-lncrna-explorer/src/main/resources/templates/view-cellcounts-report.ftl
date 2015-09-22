<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=["lncrna-explorer.css"]>
<#assign js=["cell-counts-prediction.js"]>

<@header css js/>

<div class="col-md-12">

<#if exprImport.get('status') == 'FINISHED'>

<#--<div class="alert alert-success  alert-dismissible" role="alert">-->
<#--<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>-->
<#if (exprImport.getEntities("markerGenesForCounts")?size * 100 / nrOfMarkerGenesForCounts >= 80  && exprImport.getEntities("markerGenesForPct")?size * 100 / nrOfMarkerGenesForPcts >= 80)>

<div class="well">
<h3>Congratulations!</h3> We've successfully received your data.
</div>
<#else>
<div class="well">
We have succesfully received your data, but you have not uploaded the required number of marker genes. </br>
Please check the input and try again. 
</div>
</#if>

<div class="row">
	<div class="col-sm-4 col-sm-offset-2">
		Number of imported samples:
	</div>
	<div class="col-sm-1">
		${numberOfSamplesImported}
	</div>
</div>

<div class="row">
<div class="col-sm-4 col-sm-offset-2">
Marker genes for cell counts:</div>
<div class="col-sm-1">${exprImport.getEntities("markerGenesForCounts")?size} / ${nrOfMarkerGenesForCounts} </div>
<div class="col-sm-1">${(exprImport.getEntities("markerGenesForCounts")?size * 100 / nrOfMarkerGenesForCounts)?round}% </div>
<div class="col-sm-2">
	<#if (exprImport.getEntities("markerGenesForCounts")?size * 100 / nrOfMarkerGenesForCounts >= 80)><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span><#else> <span class="glyphicon glyphicon-remove" style="color:red" aria-hidden="true"></span></#if> </div>
	
	<#--<#if (cellCountsOk)!true><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span> <#else> <span class="glyphicon glyphicon-ok" style="color:red" aria-hidden="true"></span> </#if></div>
-->
</div>
<div class="row">
<div class="col-sm-4 col-sm-offset-2">
Marker genes for cell percentages: </div>
<div class="col-sm-1">${exprImport.getEntities("markerGenesForPct")?size} / ${nrOfMarkerGenesForPcts} </div>
<div class="col-sm-1">${(exprImport.getEntities("markerGenesForPct")?size * 100 / nrOfMarkerGenesForPcts)?round}% </div>
<div class="col-sm-2">
	<#if (exprImport.getEntities("markerGenesForPct")?size * 100 / nrOfMarkerGenesForPcts >= 80)><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span><#else> <span class="glyphicon glyphicon-remove" style="color:red" aria-hidden="true"></span></#if> </div>
	
	<#--<#if (cellPctOk)!true><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span> <#else> <span class="glyphicon glyphicon-ok" style="color:red" aria-hidden="true"></span> </#if></div>-->
</div>
<#if (exprImport.getEntities("markerGenesForCounts")?size * 100 / nrOfMarkerGenesForCounts >= 80  && exprImport.getEntities("markerGenesForPct")?size * 100 / nrOfMarkerGenesForPcts >= 80)>
<div class="row">
	<button data-upload-id="${exprImport.get("id")}" data-sample-name="${exprImport.getString("importedEntity")}" data-menu-url="${menuUrl}" type="btn" class="btn btn-success" id="get-counts">Start cell count prediction!</button>
</div>
</#if>


<#elseif exprImport.get('status') == 'RUNNING'>
<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="progress">
  			<div class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuenow="20" aria-valuemin="0" aria-valuemax="100" style="width: 100%">
    			<span>Import still running...</span>
  			</div>
		</div>
	</div>
</div>
Started at  ${exprImport.getUtilDate('importDate')?datetime}
<script>
	setTimeout(function() {location.reload();}, 10000);
</script>

<#elseif exprImport.get('status') == 'FAILED'>
Sorry, import failed.
Error: ${exprImport.get('errorMessage')?html}
</#if>
</div>



<@footer/>