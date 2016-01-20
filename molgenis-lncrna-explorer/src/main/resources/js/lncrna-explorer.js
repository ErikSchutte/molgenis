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
			this.setState({
				snp : snp.value
			});
			if (snp.value != null) {
				$.get(
						'http://localhost:8080/api/v2/GeneInfo?attrs=~id,EnsemblGeneID,AssociatedGeneName,GeneType&q=Chromosome=q=' + snp.value.Chromosome + ';GeneStart=le='
								+ (parseInt(snp.value.POS, 10) + this.state.windowSize) + ';GeneEnd=ge=' + (parseInt(snp.value.POS, 10) - this.state.windowSize)).then(
						this._onGenesFound);
			}
			return snp;
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
		_mapGenes : function(inputType) {
			if (inputType == 'geneName') {
				var geneNames = this.state.genesToPlot.map(function(e) {
					return e.AssociatedGeneName;
				})
			} else if (inputType == 'geneID') {
				var geneNames = this.state.genesToPlot.map(function(e) {
					return e.EnsemblGeneID;
				})
			}
			var genes = "";
			for (i = 0; i < geneNames.length; i++) {
				genes += geneNames[i] + ',';
			}
			return genes;
		},
		_getSnp : function() {
			return this.state.snp.SnpRs;
		},
		_getStartLoci : function() {
			return this.state.snp.POS - this.state.windowSize;
		},
		_getEndLoci : function() {
			return this.state.snp.POS + this.state.windowSize;
		},
		_getChr : function() {
			return this.state.snp.Chromosome;
		},
		_getGeneInfo : function(){
			var geneInfo = this.state.genes.map(function(e) {
				console.log(e.AssociatedGeneName + '\t' + e.EnsemblGeneID + '\t' + e.GeneType);
				return e.AssociatedGeneName + '\t' + e.EnsemblGeneID + '\t' + e.GeneType;
			})

		},

		render : function() {

			var genePlots = [];

			if (this.state.genesToPlot.length >= 2) {
				genePlots = [div({
					className : "row col-md-6 col-md-offset-3"
				}, GeneTable({
					genes: this.state.genes
				})), 
				div({
					className : "row"
				}, GenePlot({
					url : 'http://localhost:8080/scripts/' + 'generateExpression%28rpkm%29Heatmap' + '/run?genes=' + this._mapGenes('geneName'),
					title : 'Cell type expression profile',
					inputType : 'geneName'
				}), GenePlot({
					url : 'http://localhost:8080/scripts/' + 'correlationCoexpression' + '/run?genes=' + this._mapGenes('geneID'),
					title : 'Coexpression',
					inputType : 'geneName'
				})),div({
					className : "row"
				}, GenePlot({
					url : 'http://localhost:8080/scripts/' + 'stimuliHeatmap' + '/run?genes=' + this._mapGenes('geneID'),
					title : 'Stimulated PMBC expression',
					inputType : 'geneID'
				}), GenePlot({
					url : 'http://localhost:8080/scripts/' + 'stimuliSignificance' + '/run?genes=' + this._mapGenes('geneID'),
					title : 'Significance of stimulated PMBC expression',
					inputType : 'geneID'
				})),div({
					className : "row"
				}, GenePlot({
					url : 'http://localhost:8080/scripts/' + 'grTcellHeatmap' + '/run?genes=' + this._mapGenes('geneID'),
					title : 'Gene expression in gluten Specific T-cells',
					inputType : 'geneID'
				}), GenePlot({
					url : 'http://localhost:8080/scripts/' + 'timecourse_gs_tcells' + '/run?genes=' + this._mapGenes('geneID'),
					title : 'Time course gene expression in gluten specific T-cells',
					inputType : 'geneID'
				})), div({
					className : "row"
				}, GenePlot({
					url : 'http://localhost:8080/scripts/' + 'gdTcells_means' + '/run?genes=' + this._mapGenes('geneID'),
					title : 'Mean gene expression in gamma delta T-cells',
					inputType : 'geneID'
//				}), GenePlot({
//					url : 'http://localhost:8080/scripts/' + 'gdTcells_medians' + '/run?genes=' + this._mapGenes('geneID'),
//					title : 'Median gene expression in gamma delta T-cells',
//					inputType : 'geneID'
				})) 
				];

				if (this.state.snp) {
					genePlots.splice(0, 0, div({
						className : "row col-md-6 col-md-offset-3"
					}, GenePlot({
						url : 'http://localhost:8080/scripts/' + 'lociPlots' + '/run?gsnp=' + this._getSnp() + '&chrLoci=' + this._getChr() + '&startLoci='
								+ this._getStartLoci() + '&endLoci=' + this._getEndLoci(),
						title : this._getSnp() + ", chr" + this._getChr() + ", " + this._getStartLoci() + "-" + this._getEndLoci(),
					})))
				}
			}

			return div({}, div({
				className : 'row'
			}, 
			div({
				className : 'col-md-4 col-md-offset-4'
				},React.DOM.h4({}, "Set window frame:")), 
				div({
					className : 'well well-sm col-md-2 col-md-offset-5'
				}, 
			div({
				className : 'col-md-1'
			}, molgenis.ui.Button({
				id : 'zoom-out-button',
				type : 'button',
				style : 'info',
				size : 'medium',
				icon : 'zoom-out',
				name : 'zoom-out',
				disabled : false,
				onClick : this._zoomOut
			})), div({
				className : 'col-md-2 col-md-offset-2'
			}, span({}, '' + this.state.windowSize)), div({
				className : 'col-md-1 col-md-offset-2'
			}, molgenis.ui.Button({
				id : 'zoom-in-button',
				type : 'button',
				style : 'info',
				size : 'medium',
				icon : 'zoom-in',
				name : 'zoom-in',
				disabled : false,
				onClick : this._zoomIn
			})))), div({
				className : 'row'
			}, div({
				className : 'col-md-4 col-md-offset-4'
			}, div({}, React.DOM.h4({}, "Select a SNP (optional):"),molgenis.ui.EntitySelectBox({
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
			})), div({},React.DOM.h4({}, "Select genes:"), molgenis.ui.EntitySelectBox({
				entity : 'GeneInfo',
				mode : 'view',
				name : "name",
				disabled : false,
				readOnly : false,
				multiple : true,
				required : true,
				placeholder : 'Please select two or more Genes',
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
			url : React.PropTypes.string.isRequired,
			title : React.PropTypes.string,
			inputType : React.PropTypes.string
		},
		getInitialState : function() {
			return {
				loaded : false
			}
		},

		render : function() {

			if (this.state.loaded) {
				return div({
					className : "col-md-6 col-sm-12"
				}, React.DOM.h3({}, this.props.title), React.DOM.img({
					src : this.props.url
				}));
			} else {
				return div({
					className : "col-md-6 col-sm-12"
				}, React.DOM.h3({}, this.props.title), React.DOM.img({
					src : '/css/select2-spinner.gif'
				}));
			}
		},
		componentDidMount : function() {
			var self = this
			var img = document.createElement('img')

			img.onload = function() {
				self.setState({
					loaded : true
				})
			}

			img.src = this.props.url
		}
	});

	var GenePlot = React.createFactory(GenePlotClass);
	
	function rowClassName(type){
		if( type == 'lincRNA' ) {
			return 'success';
		}
		else if(type == 'antisense' ) {
			return 'success';
		}
		return '';
	}
	
	var GeneTableClass = React.createClass({
		displayName : 'GeneTable',
		propTypes : {
			genes : React.PropTypes.array.isRequired
		},

		render : function() {
			var rows = this.props.genes.map(function(gene) {
				return React.DOM.tr({className:rowClassName(gene.GeneType), key: gene.EnsemblGeneID}, [
				              React.DOM.td({key:'name'}, gene.AssociatedGeneName),
				              React.DOM.td({key: 'ensemblID'}, gene.EnsemblGeneID),
				              React.DOM.td({key: 'type'}, gene.GeneType)
				              ]
				)
			});
			return React.DOM.div({style: {height: '300px', overflow: 'scroll'}}, React.DOM.table({className:'table table-condensed'},
					[React.DOM.thead({key:'header'}, React.DOM.tr(null, [React.DOM.th({key:'name'}, 'Gene Name'), React.DOM.th({key:'ensemblID'}, 'Gene Identifier'), React.DOM.th({key:'type'}, 'Type')])),
					React.DOM.tbody({key: 'body'}, rows)]));
		}
	});

	var GeneTable = React.createFactory(GeneTableClass);
	
	

	$(function() {

		React.render(React.DOM.div(null, LncRNAExplorer({})), $('#explorer')[0]);

	});

}($, window.top.molgenis = window.top.molgenis || {}));