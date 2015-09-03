<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
				"lncrna-explorer.css"
			 	]>
<#assign js=[
				"cell-counts-prediction.js"
				]>

<@header css js/>

<div class="col-md-12">

<#if exprImport.get('status') == 'FINISHED'>

<#--<div class="alert alert-success  alert-dismissible" role="alert">-->
<#--<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>-->
<div class="well">
<h3>Congratulations!</h3> We've successfully received your data.
</div>

<div class="row">
	<div class="col-sm-4 col-sm-offset-2">
		Number of imported samples:
	</div>
	<div class="col-sm-1">
		?
	</div>
</div>

<div class="row">
<div class="col-sm-4 col-sm-offset-2">
Gene Expression for marker genes for cell counts:</div>
<div class="col-sm-1">${exprImport.getEntities("markerGenesForCounts")?size} / ${nrOfMarkerGenesForCounts} </div>
<div class="col-sm-1">${(exprImport.getEntities("markerGenesForCounts")?size * 100 / nrOfMarkerGenesForCounts)?round}% </div>
<div class="col-sm-2">
	<#if (exprImport.getEntities("markerGenesForCounts")?size * 100 / nrOfMarkerGenesForCounts >= 80)><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span><#else> <span class="glyphicon glyphicon-remove" style="color:red" aria-hidden="true"></span></#if> </div>
	
	<#--<#if (cellCountsOk)!true><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span> <#else> <span class="glyphicon glyphicon-ok" style="color:red" aria-hidden="true"></span> </#if></div>
-->
</div>
<div class="row">
<div class="col-sm-4 col-sm-offset-2">
Gene Expression for marker genes for cell percentages: </div>
<div class="col-sm-1">${exprImport.getEntities("markerGenesForPct")?size} / ${nrOfMarkerGenesForPcts} </div>
<div class="col-sm-1">${(exprImport.getEntities("markerGenesForPct")?size * 100 / nrOfMarkerGenesForPcts)?round}% </div>
<div class="col-sm-2">
	<#if (exprImport.getEntities("markerGenesForPct")?size * 100 / nrOfMarkerGenesForPcts >= 80)><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span><#else> <span class="glyphicon glyphicon-remove" style="color:red" aria-hidden="true"></span></#if> </div>
	
	<#--<#if (cellPctOk)!true><span class="glyphicon glyphicon-ok" style="color:green" aria-hidden="true"></span> <#else> <span class="glyphicon glyphicon-ok" style="color:red" aria-hidden="true"></span> </#if></div>-->
</div>
<div class="row">
	<button data-upload-id="${exprImport.get("id")}" type="btn" class="btn btn-success" id="get-counts">Start cell count prediction!</button>
</div>
<div class="row">
	<div class="col-md-10" id="response"></div>
</div>

<#elseif exprImport.get('status') == 'RUNNING'>
	Import still running.
Started at  ${exprImport.getUtilDate('importDate')?datetime}

<#elseif exprImport.get('status') == 'FAILED'>
Sorry, import failed.
Error: ${exprImport.get('errorMessage')?html}
</#if>
</div>



<@footer/>