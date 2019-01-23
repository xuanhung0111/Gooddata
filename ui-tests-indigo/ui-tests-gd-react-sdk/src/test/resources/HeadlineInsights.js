import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import { Visualization } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const firstVisualizationName = variables.firstVisualizationName;
const secondVisualizationName = variables.secondVisualizationName;

const C = new CatalogHelper(catalogJson);
const firstVisualization = C.visualization(firstVisualizationName);
const secondVisualization = C.visualization(secondVisualizationName);
const testingProjectId = catalogJson.projectId;

class App extends Component {
    render() {
        return (
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {testingProjectId}
                    identifier = {firstVisualization}
                />

                <Visualization
                    projectId = {testingProjectId}
                    identifier = {secondVisualization}
                />
            </div>
        );
    }
}

export default App;
