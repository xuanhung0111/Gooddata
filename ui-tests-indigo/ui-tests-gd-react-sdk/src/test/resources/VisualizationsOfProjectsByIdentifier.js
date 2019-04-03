import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import otherCatalogJson from './other-catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const firstVisualizationName = variables.firstVisualizationName;
const secondVisualizationName = variables.secondVisualizationName;

const catalog = new CatalogHelper(catalogJson);
const firstVisualization = catalog.visualization(firstVisualizationName);
const firstProjectId = catalogJson.projectId;

const otherCatalog = new CatalogHelper(otherCatalogJson);
const secondVisualization = otherCatalog.visualization(secondVisualizationName);
const secondProjectId = otherCatalogJson.projectId;

class App extends Component {
    render() {
        return (
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {firstProjectId}
                    identifier = {firstVisualization}
                />

                <hr className="separator" />

                <Visualization
                    projectId = {secondProjectId}
                    identifier = {secondVisualization}
                />
            </div>

        );
    }
}

export default App;
