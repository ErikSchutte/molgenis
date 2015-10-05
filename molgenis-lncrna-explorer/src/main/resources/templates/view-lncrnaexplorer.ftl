<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
				"lncrna-explorer.css",
			 	"jquery.molgenis.tree.css",
				"ui.fancytree.min.css"]>
<#assign js=[
				"lncrna-explorer.js",
				"bootstrap.file-input.js",
				"jquery.bootstrap.wizard.min.js",
				"jquery.fancytree.min.js",
				"jquery.molgenis.tree.js",
				"jquery.molgenis.xrefmrefsearch.js",
				"bootbox.min.js",
				"jQEditRangeSlider-min.js"]>

<@header css js/>

<div class="col-md-12">
	<#--<div role= "tabpanel">
		<ul class="nav nav-tabs" role="tablist">
			<li role="tab" class="active"> <a href="#lncrna-explorer" aria-controls="lncrna-explorer" role="presentation" data-toggle="tab">LncRNA Explorer</a></li>
			<li role="tab"><a href="#cell-counts" aria-controls="cell-counts" role="presentation" data-toggle="tab">Decon-cell</a></li>
			<li role="tab"><a href="#deconvolution-plots" aria-controls="deconvolution-plots" role="presentation" data-toggle="tab">Decon-eQTL</a></li>
		</ul>

		<div class="tab-content">
			<div role="tabpanel" class="tab-pane active" id="lncrna-explorer">-->
				<div class="row">
					<div class="col-md-11">
	   					 <h2> LncRNA Explorer</h2>
					</div>
				</div>


				<div id="explorer">
				</div>

</div>

<@footer/>