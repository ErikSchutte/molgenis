<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<#assign css=[
				"lncrna-explorer.css"]>
<#assign js=[
				"lncrna-explorer.js"]>
			<#--	"bootstrap.file-input.js"]> -->

<@header css js/>

<div class="col-md-12">

	<div class="row">
		<div class="col-md-11">
	 		 <h2> LncRNA Explorer</h2>
		</div>
	</div>	
	<div class="col-md-6" style="font-size:11pt; text-align:justify">
		<p>&nbsp;</p> 
		<p><span>Some introduction to the LncRNA Explorer...<br /></span></p> 
		<p style="text-align: left;">&nbsp;</p> 
    </div>

	<div class="row">
		<div class="col-md-11">
			<hr>
		</div>
	</div>

	<div id="explorer"></div>

</div>

<@footer/>