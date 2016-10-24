(function ($, molgenis) {
    React.DOM.style({div: {width: "500px"}});
    var div = React.DOM.div;
    var span = React.DOM.span;
    var Select2 = molgenis.ui.wrapper.Select2;

    var LncRNAExplorerClass = React.createClass({
        displayName: 'LncRNAExplorer',
        propTypes: {
            expression_plots: React.PropTypes.array
                    },
        getInitialState: function () {
            return {
                genes: [],
                snp: null,
                genesToPlot: [],
                windowSize: 250000,
                datasets: [], // the selected dataset type
                qtl: null,
                qtlToPlot: []
            };
        },
        _onDatasetSelect: function (data) {
            // console.log('_onDatasetSelect', data.value.map(function (datasetType) {return datasetType.id;}));
            var datasets = data.value && data.value.map(function (datasetType) {return datasetType.id;});
            this.setState({
                datasets: datasets || []
            });
        },
        _onGenesSelection: function (genes) {
            this.setState({
                genes: genes.value,
                genesToPlot: []
            });
        },
        _onSearch: function () {
            this.setState({
                genesToPlot: this.state.genes
            });
        },
        _onSnpSelect: function (snp) {
            this.setState({
                snp: snp.value
            });
            if (snp.value != null) {
                $.get(
                    '/api/v2/lncrna_GeneInfo?attrs=~id,EnsemblGeneID,AssociatedGeneName,GeneType&q=ChromosomeName=q='
                    + snp.value.Chromosome + ';GeneStart=le=' + (parseInt(snp.value.POS, 10) + this.state.windowSize)
                    + ';GeneEnd=ge=' + (parseInt(snp.value.POS, 10) - this.state.windowSize)).then(
                    this._onGenesFound);
            }
            return snp;
        },
        _onGenesFound: function (data) {
            this.setState({
                genes: data.items,
                genesToPlot: []

            });
            this._getQTLs();
        },
        _getQTLs: function() {
            var geneNames;
            geneNames = this.state.genes.map(function (gene) {
                return gene.EnsemblGeneID
            });
            var genes = "";
            for (i = 0; i < geneNames.length; i++) {
                if ( i === (geneNames.length -1 )) {
                    genes += "EnsembleGeneID==" + geneNames[i];
                } else {
                    genes += "EnsembleGeneID==" + geneNames[i] + ',';
                }
            }
            // console.log(genes);

            $.get('/api/v2/lncrna_eqtl_basic?q='+genes+'&num=10000').then(
                this._updateQTLs);
        },
        _updateQTLs: function(eqtls) {
            // console.log(eqtls);
            var geneNames;
            var match = [];
            geneNames = this.state.genes.map(function (gene) {
                return gene.EnsemblGeneID
            });
            // console.log(geneNames);
            for (i = 0; i < geneNames.length; i++) {
                eqtls.items.map(function (eqtl) {
                    if (geneNames[i] === eqtls.EnsembleGeneID) {
                        match.push(eqtl);
                    }
                });
            }

            if ( match.length === 0 ) {
                this.setState({
                    qtl: "No Match Found"
                });
            } else {
                this.setState({
                    qtl: match
                });
            }


        },
        _zoomIn: function () {
            this.setState({
                windowSize: this.state.windowSize / 2
            });
        },
        _zoomOut: function () {
            this.setState({
                windowSize: this.state.windowSize * 2
            });

        },
        _mapGenes: function (inputType) {
            var geneNames;
            if (inputType == 'geneName') {
                geneNames = this.state.genesToPlot.map(function (e) {
                    return e.AssociatedGeneName;
                })
            } else if (inputType == 'geneID') {
                geneNames = this.state.genesToPlot.map(function (e) {
                    return e.EnsemblGeneID;
                })
            }
            var genes = "";
            for (i = 0; i < geneNames.length; i++) {
                genes += geneNames[i] + ',';
            }
            return genes;
        },
        _getSnp: function () {
            return this.state.snp.SnpRs;
        },
        _getStartLoci: function () {
            return this.state.snp.POS - this.state.windowSize;
        },
        _getEndLoci: function () {
            return this.state.snp.POS + this.state.windowSize;
        },
        _getChr: function () {
            return this.state.snp.Chromosome;
        },
        _getGeneInfo: function () {
            var geneInfo = this.state.genes.map(function (e) {
                console.log(e.AssociatedGeneName + '\t' + e.EnsemblGeneID + '\t' + e.GeneType);
                return e.AssociatedGeneName + '\t' + e.EnsemblGeneID + '\t' + e.GeneType;
            })

        },
        _createOptions: function () {

            return this.props.expression_plots.map(function (data) {
                return data
            });

        },
        _filterForPlotTypes: function() {
            var self = this;
            console.log('selected_dataset', this.state.datasets);
            console.log('expression_plots', this.props.expression_plots);
            var result = this.props.expression_plots.filter( function(plot) {
                return self.state.datasets.indexOf(plot.DataType) >= 0;
            });
            console.log('result of _filterForPlotTypes', result);
            return result;

        },
        _chunk: function  (arr, len) {
            var chunks = [],
                i = 0,
                n = arr.length;
            while (i < n) {
                chunks.push(arr.slice(i, i += len));
            }
            return chunks;
        },
        render: function () {

            console.log('render() datasets=', this.state.datasets);
            var self = this;
            var genePlots = [];
            var qtlPlots = [];
            var plots;

            if (this.state.genesToPlot.length >= 2) {
                //console.log(this.props.expression_plots);
                // console.log(this.state.dataset);
                plots = this._filterForPlotTypes();
                console.log(plots);
                genePlots = this._chunk(plots.map(function(plot) {
                    console.log(plot);
                   return GenePlot({
                        url: '/scripts/' + plot.Scripts[0].name + '/run?genes=' + self._mapGenes('geneID')
                        + '&data=' + plot.Dataset.fullName,
                        title:  plot.Dataset.simpleName + ' ' + plot.Scripts[0].name,
                        inputType:'geneName'
                    })
                }), 1).map(function(chunk){
                       return div({
                           style: {width:"90vw", marginLeft: "5vw", marginRight: "5vw"},
                           className: "row"
                       }, chunk);
               });

                genePlots = [div({
                    style: {width:"90vw", marginLeft: "5vw", marginRight: "5vw"},
                    className: "row col-md-6 col-md-offset-3"
                }, GeneTable({
                    genes: this.state.genes
                }))].concat(genePlots);


                // if (this.state.snp) {
                // 	genePlots.splice(0, 0, div({
                // 		className: "row col-md-6 col-md-offset-3"
                // 	}, GenePlot({
                // 		url: '/scripts/' + 'lociPlots' + '/run?gsnp=' + this._getSnp() + '&chrLoci=' + this._getChr() + '&startLoci='
                // 		+ this._getStartLoci() + '&endLoci=' + this._getEndLoci(),
                // 		title: this._getSnp() + ", chr" + this._getChr() + ", " + this._getStartLoci() + "-" + this._getEndLoci() +
                // 		'&data=',
                // 	})))
                // }
                // this.state.qtl.map(function (gene) {
                    // console.log(gene);
                    // var genes = this._mapGenes('geneID');
                    // console.log(genes);
                    // if ( gene === genes.value ) {
                    //     this._updateQTLs(gene);
                    // }
                // });

                qtlPlots = [div({
                    style: {width:"90vw", marginLeft: "5vw", maringRight:"5vw"},
                            className: "row col-md-6 col-md-offset-3"
                }, QTLTable({
                    qtl: this.state.qtl
                }))];
                // qtlPlots = this._chunk(plots.map(function(plot) {
                //     return qtlPlot({
                //         url: '/scripts/' + plot.Scripts[0].name + '/run?genes=' + self._mapGenes('geneID')
                //         + '&data=' + plot.Dataset.fullName,
                //         title:  plot.Dataset.simpleName + ' ' + plot.Scripts[0].name,
                //         inputType:'geneName'
                //     })
                // }), 1).map(function(chunk){
                //     return div({
                //         style: {width:"90vw", marginLeft: "5vw", marginRight: "5vw"},
                //         className: "row"
                //     }, chunk);
                // });
                //
                // qtlPlots = [div({
                //         style: {width:"90vw", marginLeft: "5vw", maringRight:"5vw"},
                //         className: "row col-md-6 col-md-offset-3"
                //     }, QTLTable({
                //         qtl: this.state.qtl
                //     }))].concat(qtlPlots);
            }

            return div({}, div({
                    className: 'row'
                }, div({
                    className: 'col-md-4 col-md-offset-4'
                }, React.DOM.h4({}, "Set window frame:")),
                div({
                        className: 'well well-sm col-md-2 col-md-offset-5'
                    },
                    div({
                        className: 'col-md-1'
                    }, molgenis.ui.Button({
                        id: 'zoom-out-button',
                        type: 'button',
                        style: 'info',
                        size: 'medium',
                        icon: 'zoom-out',
                        name: 'zoom-out',
                        disabled: false,
                        onClick: this._zoomOut
                    })), div({
                        className: 'col-md-2 col-md-offset-2'
                    }, span({}, '' + this.state.windowSize)), div({
                        className: 'col-md-1 col-md-offset-2'
                    }, molgenis.ui.Button({
                        id: 'zoom-in-button',
                        type: 'button',
                        style: 'info',
                        size: 'medium',
                        icon: 'zoom-in',
                        name: 'zoom-in',
                        disabled: false,
                        onClick: this._zoomIn
                    })))), div({
                    className: 'row'
                }, div({
                    className: 'col-md-4 col-md-offset-4'
                }, React.DOM.h4({}, "Select Dataset:"), molgenis.ui.EntitySelectBox({
                    entity: 'lncrna_datasets_datasetType',
                    mode: 'view',
                    name: "dataset",
                    disabled: false,
                    readOnly: false,
                    multiple: true,
                    required: true,
                    placeholder: 'Please select a dataset..',
                    focus: false,
                    onValueChange: this._onDatasetSelect
                }),
                div({}, React.DOM.h4({}, "Select a SNP (optional):"), molgenis.ui.EntitySelectBox({
                    entity: 'lncrna_SnpsToPlot',
                    mode: 'view',
                    name: "name",
                    disabled: false,
                    readOnly: false,
                    multiple: false,
                    required: false,
                    placeholder: 'Please select a SNP',
                    focus: false,
                    value: [],
                    onValueChange: this._onSnpSelect
                })),
                div({}, React.DOM.h4({}, "Select genes:"),
                    molgenis.ui.EntitySelectBox({
                        entity: 'lncrna_GeneInfo',
                        mode: 'view',
                        name: "name",
                        disabled: false,
                        readOnly: false,
                        multiple: true,
                        required: true,
                        placeholder: 'Please select two or more Genes',
                        focus: false,
                        value: this.state.genes,
                        onValueChange: this._onGenesSelection
                    })),
                molgenis.ui.Button({
                    id: 'plot-button',
                    type: 'button',
                    style: 'info',
                    size: 'medium',
                    text: 'Plot',
                    name: 'Plot',
                    disabled: false,
                    onClick: this._onSearch,
                }))),
                genePlots,
                qtlPlots);
        }
    });

    var LncRNAExplorer = React.createFactory(LncRNAExplorerClass);

    var GenePlotClass = React.createClass({
        displayName: 'GenePlot',
        propTypes: {
            url: React.PropTypes.string.isRequired,
            title: React.PropTypes.string,
            inputType: React.PropTypes.string
        },
        getInitialState: function () {
            return {
                loaded: false
            }
        },

        render: function () {

            if (this.state.loaded) {
                return div({
                    className: "col-md-6 col-sm-12",
                    style: this.props.style
                }, React.DOM.h3({}, this.props.title), React.DOM.img({
                    style: this.props.style,
                    src: this.props.url
                }));
            } else {
                return div({
                    className: "col-md-6 col-sm-12"
                }, React.DOM.h3({}, this.props.title), React.DOM.img({
                    src: '/img/select2-spinner.gif'
                }));
            }
        },
        componentDidMount: function () {
            var self = this;
            var img = document.createElement('img');

            img.onload = function () {
                self.setState({
                    loaded: true
                })
            };
            img.src = this.props.url
        }
    });

    var GenePlot = React.createFactory(GenePlotClass);

    function rowClassName(type) {
        if (type == 'lincRNA') {
            return 'success';
        }
        else if (type == 'antisense') {
            return 'success';
        }
        return 'fail';
    }

    var GeneTableClass = React.createClass({
        displayName: 'GeneTable',
        propTypes: {
            genes: React.PropTypes.array.isRequired
        },

        render: function () {
            var rows = this.props.genes.map(function (gene) {
                return React.DOM.tr({className: rowClassName(gene.GeneType), key: gene.EnsemblGeneID}, [
                        React.DOM.td({key: 'ensemblID'}, gene.EnsemblGeneID),
                        React.DOM.td({key: 'name'}, gene.AssociatedGeneName),
                        React.DOM.td({key: 'type'}, gene.GeneType)
                    ]
                )
            });
            return React.DOM.div({style: {height: '300px', overflow: 'scroll'}},
                React.DOM.table({className: 'table table-condensed'}, [React.DOM.thead({key: 'header'},
                    React.DOM.tr(null, [
                        React.DOM.th({key: 'ensemblID'}, 'EnsemblGeneID'),
                        React.DOM.th({key: 'name'}, 'Gene Name'),
                        React.DOM.th({key: 'type'}, 'Type')])),
                    React.DOM.tbody({key: 'body'}, rows)]));
        }
    });

    var GeneTable = React.createFactory(GeneTableClass);

    var QTLPlotClass = React.createClass({
        displayName: 'QTLPlot',
        propTypes: {
            url: React.PropTypes.string.isRequired,
            title: React.PropTypes.string,
            inputType: React.PropTypes.string
        },
        getInitialState: function () {
            return {
                loaded: false
            }
        },

        render: function () {

            if (this.state.loaded) {
                return div({
                    className: "col-md-6 col-sm-12",
                    style: this.props.style
                }, React.DOM.h3({}, this.props.title), React.DOM.img({
                    style: this.props.style,
                    src: this.props.url
                }));
            } else {
                return div({
                    className: "col-md-6 col-sm-12"
                }, React.DOM.h3({}, this.props.title), React.DOM.img({
                    src: '/img/select2-spinner.gif'
                }));
            }
        },
        componentDidMount: function () {
            var self = this;
            var img = document.createElement('img');

            img.onload = function () {
                self.setState({
                    loaded: true
                })
            };
            img.src = this.props.url
        }
    });

    var QTLPlot = React.createFactory(QTLPlotClass);

    var QTLTableClass = React.createClass({
        displayName: 'QTLTable',
        propTypes: {
            qtl: React.PropTypes.array.isRequired
        },


        render: function () {
            var rows;
            if(QTLTableClass.hasOwnProperty("qtl")){
                rows = this.props.qtl.map(function (qtl) {
                    return React.DOM.tr({className: rowClassName(qtl.qtlType), key: qtl.qtlType}, [
                        React.DOM.td({key: 'SNPs'}, qtl.SNP),
                        React.DOM.td({key: 'EnsembleGeneID'}, qtl.EnsembleGeneID),
                        React.DOM.td({key: 'Statistic'}, qtl.stat),
                        React.DOM.td({key: 'P_Value'}, qtl.pvalue),
                        React.DOM.td({key: 'FDR'}, qtl.FDR),
                        React.DOM.td({key: 'Beta_Score'}, qtl.beta)
                    ])
                });


            } else {
                rows =  React.DOM.tr([
                    React.DOM.td({key: 'SNPs'}, 'EMPTY')
                ])
            }
            return React.DOM.div({style: {height: '300px', overflow: 'scroll'}},
                React.DOM.table({className: 'table table-condensed'}, [React.DOM.thead({key: 'header'},
                    React.DOM.tr(null, [
                        React.DOM.th({key: 'SNPs'}, 'SNP'),
                        React.DOM.th({key: 'EnsembleGeneID'}, 'Ensemble Gene ID'),
                        React.DOM.th({key: 'Statistic'}, 'Statistic'),
                        React.DOM.th({key: 'P_Value'}, 'P-Value'),
                        React.DOM.th({key: 'FDR'}, 'FDR'),
                        React.DOM.th({key: 'Beta_Score'}, 'Beta_Score')])),
                    React.DOM.tbody({key: 'body'}, rows)]));
    }});

    var QTLTable = React.createFactory(QTLTableClass);

    $(function () {

        $.get('/api/v2/lncrna_datasets_plots', function(datasettypes) {
            var expression_plots = datasettypes.items;
            React.render(React.DOM.div(null, LncRNAExplorer({expression_plots: expression_plots})), $('#explorer')[0]);
        })


    });

}($, window.top.molgenis = window.top.molgenis || {}));