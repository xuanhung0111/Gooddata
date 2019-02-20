import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Headline } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const primaryMeasureTitle = variables.primaryMeasureTitle;
const secondaryMeasureTitle = variables.secondaryMeasureTitle;
const aliasMeasureTitle = variables.aliasMeasureTitle;

const C = new CatalogHelper(catalogJson);
const firstMeasure = C.measure(primaryMeasureTitle);
const secondMeasure = C.measure(secondaryMeasureTitle);
const testingProjectId = catalogJson.projectId;
const primaryMeasure = {
    measure : {
        localIdentifier : 'primaryMeasure',
        definition : {
            measureDefinition: {
                item : {
                    identifier : firstMeasure
                }
            }
        }
    }
};

const secondaryMeasure = {
    measure: {
        localIdentifier : 'secondaryMeasure',
        definition : {
            measureDefinition : {
                item : {
                    identifier : secondMeasure
                }
            }
        },
        alias : aliasMeasureTitle
    }
};

class App extends Component {
    render() {
        return (
            <div style = {{ height : 400, width : 600 }}>
                <Headline
                    projectId = {testingProjectId}
                    primaryMeasure = {primaryMeasure}
                    secondaryMeasure = {secondaryMeasure}
                />
            </div>
        );
    }
}

export default App;
