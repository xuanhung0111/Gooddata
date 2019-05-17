import React, { Component } from 'react';
import { ColumnChart, CatalogHelper, Model } from '@gooddata/react-components';
import catalogJson from './catalog.json';

import '@gooddata/react-components/styles/css/main.css';

const variables = require('./testing-variable.json');
const metric = variables.metric;
const displayForm = variables.displayForm;
const attribute = variables.attribute;
const elementOfAttribute = variables.elementOfAttribute;
const fromNow = variables.fromNow;
const toNow = variables.toNow;
const dataSet = variables.dataSet;
const from = variables.from;
const to = variables.to;
const anotherElementOfAttribute = variables.anotherElementOfAttribute;
const C = new CatalogHelper(catalogJson);
const projectId = catalogJson.projectId;

class App extends Component {
    render() {
        const measure = {
            measure: {
                localIdentifier: 'measures',
                title: metric,
                definition: {
                    measureDefinition: {
                        item: {
                            identifier : C.measure(metric)
                        },
                        filters : [
                            {
                                absoluteDateFilter : {
                                    dataSet : {
                                        identifier : C.dateDataSet(dataSet)
                                    },
                                    from : from,
                                    to : to
                                }
                            },
                            {
                                positiveAttributeFilter : {
                                    displayForm : {
                                        identifier : C.attributeDisplayForm(attribute, displayForm)
                                    },
                                    in : [
                                        elementOfAttribute, anotherElementOfAttribute
                                    ]
                                }
                            }
                        ]
                    }
                }
            }
        };

        const viewBy = {
            visualizationAttribute : {
                displayForm : {
                    identifier : C.attributeDisplayForm(attribute, displayForm)
                },
                localIdentifier : 'view'
            }
        };

        const attributeFilter = {
                    negativeAttributeFilter : {
                        displayForm : {
                            identifier : C.attributeDisplayForm(attribute, displayForm)
                        },
                        notIn : [
                            anotherElementOfAttribute
                        ]
                    }
        }

        const dateFilter = {
            relativeDateFilter : {
                dataSet : {
                    identifier : C.dateDataSet(dataSet)
                },
                granularity: 'GDC.time.year',
                from : parseInt(fromNow),
                to : parseInt(toNow)
            }
        }

        return (
            <div style={{ height: 600 }} className="s-column-chart">
                <h1>Filter test</h1>

                <p>This case using test for filtering insight with measure be added many type filters.</p>

                <hr className="separator" />
                <title>Column chart</title>
                <ColumnChart
                    projectId={projectId}
                    measures={[measure]}
                    viewBy={viewBy}
                    filters={[attributeFilter, dateFilter]}
                />
            </div>
        );
    }
}

export default App;
