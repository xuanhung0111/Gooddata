import React, { Component } from 'react';
import { ColumnChart, CatalogHelper, Model } from '@gooddata/react-components';
import catalogJson from './catalog.json';

import '@gooddata/react-components/styles/css/main.css';

const variables = require('./testing-variable.json');
const metric = variables.metric;
const displayForm = variables.displayForm;
const attribute = variables.attribute;
const elementOfAttribute = variables.elementOfAttribute;
const dataSet = variables.dataSet;
const from = variables.from;
const to = variables.to;
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
                                positiveAttributeFilter : {
                                    displayForm : {
                                        identifier : C.attributeDisplayForm(attribute, displayForm)
                                    },
                                    in : [
                                        elementOfAttribute
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

        const filter = {
            absoluteDateFilter : {
                dataSet : {
                    identifier : C.dateDataSet(dataSet)
                },
                from : from,
                to : to
            }
        }

        return (
            <div style={{ height: 600 }} className="s-column-chart">
                <title>Column chart</title>
                <ColumnChart
                    projectId={projectId}
                    measures={[measure]}
                    viewBy={viewBy}
                    filters={[filter]}
                />
            </div>
        );
    }
}

export default App;
