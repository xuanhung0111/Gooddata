import React, { Component } from 'react';
import catalogJson from './catalog.json';
import { AttributeFilter, CatalogHelper, Visualization  } from "@gooddata/react-components";
import "@gooddata/react-components/styles/css/dateFilter.css";
import '@gooddata/react-components/styles/css/main.css';

const C = new CatalogHelper(catalogJson);
const variables = require('./testing-variable.json');
const visualizationUri = variables.visualizationUri;
const attribute = variables.attribute;
const attributeUri = variables.attributeUri;
const projectId = catalogJson.projectId;
const attributeDisplayFormId = C.attributeDisplayForm(attribute, attribute);

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            attributeFilter: {},
        };
    }

    onApplyAttributeFilter = (filter) => {
        console.log('AttributeFilterExample filter', filter);

        const isPositive = !!filter.in;
        const elementsProp = isPositive ? 'in' : 'notIn';

        const filters = {
            [isPositive ? 'positiveAttributeFilter' : 'negativeAttributeFilter']: {
                displayForm: {
                    identifier: filter.id
                },
                [elementsProp]: filter[elementsProp].map(element => (`${attributeUri}/elements?id=${element}`))
            }
        };

        this.setState({
            attributeFilter: filters,
        });
    };

    render(){

        const attributeFilter = this.state.attributeFilter;

        return (

            <div className="App ">
                <header>
                    <div className={"dash-filters-all"}>
                        <div className={"s-attribute-filter"} style={{ width: 150, height: 50  }}>
                            <AttributeFilter
                                identifier={attributeDisplayFormId}
                                projectId={projectId}
                                fullscreenOnMobile={false}
                                onApply={this.onApplyAttributeFilter}
                            />
                        </div>
                    </div>
                    <div style={{ height: 600, width: 600 }}>
                        <Visualization
                            projectId={projectId}
                            uri={visualizationUri}
                            filters={[attributeFilter]}
                        />
                    </div>

                </header>
            </div>

        );
    }
}
export default App;

