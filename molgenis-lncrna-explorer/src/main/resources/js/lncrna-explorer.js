(function($, molgenis) {

	var div = React.DOM.div;

	var LncRNAExplorerClass = React.createClass({
		displayName : 'LncRNAExplorer',
		propTypes : {},
		getInitialState : function() {
			return {
				genes : [],
			};
		},
		_handleSearch : function(genes) {
			console.log(genes);
			this.setState({
				genes : genes
			});
		},
		render : function() {

			var genePlots = [];

			if (this.state.genes.length >= 2) {
				genePlots = [ 
				GenePlot({
					genes : this.state.genes,
					scriptName : 'generateExpression%28rpkm%29Heatmap',
					title : 'Cell type expression profile',
					inputType: 'geneName'
				}), 
				GenePlot({
					genes : this.state.genes,
					scriptName : 'correlationCoexpression',
					title : 'Coexpression',
					inputType: 'geneName'
				}),	
				GenePlot({
					genes : this.state.genes,
					scriptName : 'grTcellHeatmap',
					title : 'gr-Tcell',
					inputType: 'geneID'
				})
				];
			}

			return div({}, div({
				className : 'row'
			}, div({
				className : 'col-md-4 col-md-offset-4'
			}, GenesSelectBox({
				onSearch : this._handleSearch
			}))), genePlots);
		}
	});

	var LncRNAExplorer = React.createFactory(LncRNAExplorerClass);

	var GenesSelectBoxClass = React.createClass({
		displayName : 'GenesSelectBox',
		propTypes : {
			onSearch : React.PropTypes.func
		},
		getInitialState : function() {
			return {
				genes : [],
			};
		},
		_onGenesSelection : function(event) {
			this.setState({
				genes : event.value
			});
		},
		_onButtonPress : function() {
			this.props.onSearch(this.state.genes);
		},
		render : function() {
			return React.DOM.div({}, molgenis.ui.EntitySelectBox({
				entity : 'BioMartGenes',
				mode : 'view',
				name : "name",
				disabled : false,
				readOnly : false,
				multiple : true,
				required : true,
				placeholder : 'Please select one or more Genes',
				focus : false,
				value : [],
				onValueChange : this._onGenesSelection
			}), molgenis.ui.Button({
				id : 'search-button',
				type : 'button',
				style : 'info',
				size : 'medium',
				text : 'Search',
				name : 'Search',
				disabled : false,
				onClick : this._onButtonPress,
			}));
		}
	});

	var GenesSelectBox = React.createFactory(GenesSelectBoxClass);

	var GenePlotClass = React.createClass({
		displayName : 'GenePlot',
		propTypes : {
			genes : React.PropTypes.array.isRequired,
			scriptName : React.PropTypes.string,
			title : React.PropTypes.string,
			inputType: React.PropTypes.string
		},
		_mapGenes : function() {
			if (this.props.inputType == 'geneName'){
				var geneNames = this.props.genes.map(function(e) {
					return e.AssociatedGeneName;
				})
			}
			else if (this.props.inputType == 'geneID'){
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
			return React.DOM.div({}, React.DOM.h3({}, this.props.title), React.DOM.img({
				src : 'http://localhost:8080/scripts/' + this.props.scriptName + '/run?genes=' + this._mapGenes()
			}));
		}
	});

	var GenePlot = React.createFactory(GenePlotClass);

	$(function() {

		React.render(LncRNAExplorer({}), $('#explorer')[0]);

	});
	/*
	 * + '<h3>Cell type expression profile</h3></br>' + '<img
	 * src="http://localhost:8080/scripts/generateExpression%28rpkm%29Heatmap/run?genes=' +
	 * genes + '"></br></br>' + '<h3>Coexpression</h3></br>' + '<img
	 * src="http://localhost:8080/scripts/correlationCoexpression/run?genes=' +
	 * genes + '">' + "")
	 * 
	 */

}($, window.top.molgenis = window.top.molgenis || {}));