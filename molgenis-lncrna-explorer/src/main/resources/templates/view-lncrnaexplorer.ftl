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

				<div class="row">
					<div class="col-md-11">
	   					 <h2> LncRNA Explorer</h2>
					</div>
				</div>


				<div id="explorer">
				</div>

</div>

<@footer/>