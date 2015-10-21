<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=['decon-eqtl.css']>
<#assign js=['deconvolution.js']>

<@header css js/>


<div class="row">
	<div class="col-md-11">
	   	<h2> Deconvoluted eQTLs on immune disease associated SNPs</h2>

			<div class="col-md-4" style="font-size:11pt; text-align:justify">
				<p>&nbsp;</p> <p>&nbsp;</p> 
				<p><span>Decon-eQTL is a computational approach for eQTL deconvolution into cell types using measurements from heterogeneous samples.<br />
				Here we show the significant deconvoluted eQTLs on 518 SNPs associated with 13 immuno-related diseases using 626 whole blood human RNA-seq samples.</span></p> 
				<p style="text-align: left;">&nbsp;</p> 
   			 </div>

			<div class="col-sm-6 col-md-4 col-md-offset-1">
				<p>&nbsp;</p> 
      			<img src="https://molgenis04.target.rug.nl/files/AAAACUAY2NW6RNXYJRHRKNIAAE" alt="Decon-eQTL"  style="min-height:280px;height:280px;">
				<p style="text-align: left;">&nbsp;</p> 
			</div>
		</div>
	</div>
</div>
	
	<div class="row">
		<div class="col-md-12">
			<hr>
	<div class="col-md-2 col-md-offset-10">
		<a role="button" class="btn btn-info" id="show-legend" rel="popover" data-content="" >Show tutorial <span class="glyphicon glyphicon-chevron-down" aria-hidden="true"></span></a>	
		<p style="text-align: left;">&nbsp;</p>	
	</div>
		</div>
	</div>

</div>
<div id="deconvolution">
</div>

<@footer/>