(function($, molgenis) {

	var div = React.DOM.div;
	var span = React.DOM.span;

	var LncRNAExplorerClass = React.createClass({
		displayName : 'LncRNAExplorer',
		propTypes : {},
		getInitialState : function() {
			return {
				genes : [],
				snp : null,
				genesToPlot : [],
				windowSize : 250000
			};
		},
		_onGenesSelection : function(genes) {
			this.setState({
				genes : genes.value,
				genesToPlot : []
			});
		},
		_onSearch : function() {
			this.setState({
				genesToPlot : this.state.genes
			});
		},
		_onSnpSelect : function(snp) {
			console.log(snp);
			this.setState({
				snp : snp.value
			});
			if (snp.value != null) {
				$.get(
						'http://localhost:8080/api/v2/GenePos?attrs=~id,EnsemblGeneID,AssociatedGeneName&q=Chromosome=q=' + snp.value.Chromosome + ';GeneStart=le='
								+ (parseInt(snp.value.POS, 10) + this.state.windowSize) + ';GeneEnd=ge=' + (parseInt(snp.value.POS, 10) - this.state.windowSize)).then(
						this._onGenesFound);
			}
		},
		_onGenesFound : function(data) {
			this.setState({
				genes : data.items,
				genesToPlot : []
			});

		},
		_zoomIn : function() {
			this.setState({
				windowSize : this.state.windowSize / 2
			});
		},
		_zoomOut : function() {
			this.setState({
				windowSize : this.state.windowSize * 2
			});

		},

		render : function() {

			var genePlots = [];

			if (this.state.genesToPlot.length >= 2) {
				genePlots = [ div({
					className : "row"
				}, GenePlot({
					genes : this.state.genesToPlot,
					scriptName : 'generateExpression%28rpkm%29Heatmap',
					title : 'Cell type expression profile',
					inputType : 'geneName'
				})), div({
					className : "row"
				}, GenePlot({
					genes : this.state.genesToPlot,
					scriptName : 'correlationCoexpression',
					title : 'Coexpression',
					inputType : 'geneName'
				})), div({
					className : "row"
				}, GenePlot({
					genes : this.state.genesToPlot,
					scriptName : 'grTcellHeatmap',
					title : 'Gluten Specific T-cells',
					inputType : 'geneID'
				}), GenePlot({
					genes : this.state.genesToPlot,
					scriptName : 'timecourse_gs_tcells',
					title : 'Time course gluten specific T-cells',
					inputType : 'geneID'
				})), div({
					className : "row"
				}, GenePlot({
					genes : this.state.genesToPlot,
					scriptName : 'stimuliHeatmap',
					title : 'Stimulated PMBC expression',
					inputType : 'geneID'
				})) ];
			}

			return div({}, div({
				className : 'row'
			}, div({
				className : 'col-md-1 col-md-offset-4'
			}, molgenis.ui.Button({
				id : 'zoom-in-button',
				type : 'button',
				style : 'info',
				size : 'medium',
				text : '+',
				name : 'zoom-in',
				disabled : false,
				onClick : this._zoomIn
			})), div({
				className : 'col-md-1'
			}, span({}, '' + this.state.windowSize)), div({
				className : 'col-md-1'
			}, molgenis.ui.Button({
				id : 'zoom-out-button',
				type : 'button',
				style : 'info',
				size : 'medium',
				text : '-',
				name : 'zoom-out',
				disabled : false,
				onClick : this._zoomOut
			}))), div({
				className : 'row'
			}, div({
				className : 'col-md-4 col-md-offset-4'
			}, React.DOM.div({}, molgenis.ui.EntitySelectBox({
				entity : 'SnpsToPlot',
				mode : 'view',
				name : "name",
				disabled : false,
				readOnly : false,
				multiple : false,
				required : false,
				placeholder : 'Please select a SNP',
				focus : false,
				value : [],
				onValueChange : this._onSnpSelect
			})), React.DOM.div({}, molgenis.ui.EntitySelectBox({
				entity : 'GenePos',
				mode : 'view',
				name : "name",
				disabled : false,
				readOnly : false,
				multiple : true,
				required : true,
				placeholder : 'Please select one or more Genes',
				focus : false,
				value : this.state.genes,
				onValueChange : this._onGenesSelection
			})), molgenis.ui.Button({
				id : 'plot-button',
				type : 'button',
				style : 'info',
				size : 'medium',
				text : 'Plot',
				name : 'Plot',
				disabled : false,
				onClick : this._onSearch,
			}))), genePlots);
		}
	});

	var LncRNAExplorer = React.createFactory(LncRNAExplorerClass);

	var GenePlotClass = React.createClass({
		displayName : 'GenePlot',
		propTypes : {
			genes : React.PropTypes.array.isRequired,
			scriptName : React.PropTypes.string,
			title : React.PropTypes.string,
			inputType : React.PropTypes.string
		},
		_mapGenes : function() {
			if (this.props.inputType == 'geneName') {
				var geneNames = this.props.genes.map(function(e) {
					return e.AssociatedGeneName;
				})
			} else if (this.props.inputType == 'geneID') {
				var geneNames = this.props.genes.map(function(e) {
					return e.EnsemblGeneID;
				})
			}
			var genes = "";
			for (i = 0; i < geneNames.length; i++) {
				genes += geneNames[i] + ',';
			}
			return genes;
		},
		render : function() {
			return React.DOM.div({
				className : "col-md-6 col-sm-12"
			}, React.DOM.h3({}, this.props.title), React.DOM.img({
				src : 'http://localhost:8080/scripts/' + this.props.scriptName + '/run?genes=' + this._mapGenes(),
				lowsrc : "css/select2-spinner.gif"
			}));
		}
	});

	var GenePlot = React.createFactory(GenePlotClass);

	$(function() {

		React.render(LncRNAExplorer({}), $('#explorer')[0]);

	});

}($, window.top.molgenis = window.top.molgenis || {}));