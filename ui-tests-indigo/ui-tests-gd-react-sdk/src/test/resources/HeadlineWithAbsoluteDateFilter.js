import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Headline } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const primaryMeasureTitle = variables.primaryMeasureTitle;
const from = variables.from;
const to = variables.to;
const dateAttributeName = variables.dateAttributeName;

const C = new CatalogHelper(catalogJson);
const dateAttribute = C.dateDataSet(dateAttributeName);
const firstMeasure = C.measure(primaryMeasureTitle);

const testingProjectId = catalogJson.projectId;
const primaryMeasure = {
    measure : {
        localIdentifier : 'primaryMeasure',
        definition : {
            measureDefinition : {
                item : {
                    identifier : firstMeasure
                }
            }
        }
    }
};

const filter = [
    {
        "absoluteDateFilter" : {
            "dataSet" : {
                "identifier" : dateAttribute
            },
            "from" : from,
            "to" : to
        }
    }
];

class App extends Component {
    render() {
        return (
            <div style={{ height : 400, width : 600 }}>
                <Headline
                    projectId = {testingProjectId}
                    primaryMeasure = {primaryMeasure}
                    filters = {filter}
                />
            </div>
        );
    }
}

export default App;
