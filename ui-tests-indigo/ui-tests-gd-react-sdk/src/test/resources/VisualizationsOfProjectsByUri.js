import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import otherCatalogJson from './other-catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const firstVisualizationUri = variables.firstVisualizationUri;
const secondVisualizationUri = variables.secondVisualizationUri;

const firstProjectId = catalogJson.projectId;
const secondProjectId = otherCatalogJson.projectId;

class App extends Component {
    render() {
        return (
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {firstProjectId}
                    uri = {firstVisualizationUri}
                />

                <hr className="separator" />

                <Visualization
                    projectId = {secondProjectId}
                    uri = {secondVisualizationUri}
                />
            </div>

        );
    }
}

export default App;
