<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['decon-eqtl.css']>
<#assign js=['deconvolution.js']>

<@header css js/>


<div class="row">
	<div class="col-md-10">
	   	<h2> Deconvoluted eQTLs on immune disease associated SNPs</h2>
	</div>
	<div class="col-md-2">
		<a role="button" class="btn btn-info" id="show-legend" rel="popover" data-content="" >Show tutorial <span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span></a>	
	</div>
</div>
<div id="deconvolution">
</div>

<@footer/>