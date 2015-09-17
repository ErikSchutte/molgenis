package org.molgenis.cellcounts.controller;

import java.awt.Image;

import javax.swing.ImageIcon;

import org.springframework.web.bind.annotation.RequestMapping;

public class DeconvolutionController
{

	Image tutorial = new ImageIcon(this.getClass().getResource("/img/Tutorial_deconvolution.png")).getImage();

	@RequestMapping(value = "/deconvolution")
	public Image deconvolutionPlots()
	{
		return tutorial;
	}

}
