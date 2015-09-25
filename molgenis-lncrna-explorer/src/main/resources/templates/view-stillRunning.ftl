<#include "molgenis-header.ftl">
<#include "molgenis-footer.ftl">

<@header css js/>

<div class="col-md-12">
<div class="row">
	<div class="col-md-6 col-md-offset-3">
		<div class="progress">
  			<div class="progress-bar progress-bar-info progress-bar-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" style="width: 100%">
    			<span>Cell count prediction still running...</span>
  			</div>
		</div>
	</div>
</div>
<script>
	setTimeout(function() {location.reload();}, 10000);
</script>
</div>

<@footer/>