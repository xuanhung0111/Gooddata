import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const visualizationName = variables.visualizationName;
const from = variables.from;
const to = variables.to;
const dateAttributeName = variables.dateAttributeName;

const C = new CatalogHelper(catalogJson);
const visualization = C.visualization(visualizationName);
const dateAttribute = C.dateDataSet(dateAttributeName);
const testingProjectId = catalogJson.projectId;
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
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {testingProjectId}
                    identifier ={visualization}
                    filters = {filter}
                />
            </div>
        );
    }
}

export default App;
