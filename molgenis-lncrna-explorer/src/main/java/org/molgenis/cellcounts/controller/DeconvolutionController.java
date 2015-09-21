package org.molgenis.cellcounts.controller;


import static org.molgenis.cellcounts.controller.DeconvolutionController.URI;

import org.molgenis.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping(URI)
public class DeconvolutionController extends MolgenisPluginController
{
	public static final String ID = "decon-eqtl";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

	
	public DeconvolutionController()
	{
		super(URI);
	}

	@RequestMapping
	public String init(Model model)
	{
		return "view-decon-eqtl";
	}

}
