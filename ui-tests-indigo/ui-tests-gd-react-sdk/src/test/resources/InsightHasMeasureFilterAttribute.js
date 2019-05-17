import React, { Component } from 'react';
import { ColumnChart, CatalogHelper, Model } from '@gooddata/react-components';
import catalogJson from './catalog.json';

import '@gooddata/react-components/styles/css/main.css';

const variables = require('./testing-variable.json');
const metric = variables.metric;
const displayForm = variables.displayForm;
const attribute = variables.attribute;
const elementOfAttribute = variables.elementOfAttribute;
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
                                positiveAttributeFilter : {
                                    displayForm : {
                                        identifier : C.attributeDisplayForm(attribute, displayForm)
                                    },
                                    in : [
                                        elementOfAttribute,
                                        anotherElementOfAttribute
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

        return (
            <div style={{ height: 600 }} className="s-column-chart">
                <title>Column chart</title>
                <ColumnChart
                    projectId={projectId}
                    measures={[measure]}
                    viewBy={viewBy}
                />
            </div>
        );
    }
}

export default App;
