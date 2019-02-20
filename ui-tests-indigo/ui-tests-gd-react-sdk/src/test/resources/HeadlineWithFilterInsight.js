import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const elementAttributeUri = variables.elementAttributeUri;
const visualizationName = variables.visualizationName;
const attributeUri = variables.attributeUri;

const C = new CatalogHelper(catalogJson);
const visualization = C.visualization(visualizationName);
const testingProjectId = catalogJson.projectId;
const filter = [
    {
        "positiveAttributeFilter" : {
            "displayForm" : {
                "uri" : attributeUri
            },
            "in" : [
                elementAttributeUri
            ]
        }
    }
];
class App extends Component {
    render() {
        return (
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {testingProjectId}
                    identifier = {visualization}
                    filters = {filter}
                />
            </div>
        );
    }
}

export default App;
