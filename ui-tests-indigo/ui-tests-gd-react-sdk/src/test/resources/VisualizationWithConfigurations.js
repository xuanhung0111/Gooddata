import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const visualizationName = variables.visualizationName;
const firstColor = variables.firstColor;
const secondColor = variables.secondColor;

const C = new CatalogHelper(catalogJson);
const visualization = C.visualization(visualizationName);
const testingProjectId = catalogJson.projectId;

class App extends Component {
    render() {
        return (
            <div style = {{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {testingProjectId}
                    identifier = {visualization}
                    config = {{
                        colors: [firstColor, secondColor],
                        legend: {
                            enabled: (variables.hasLegend === 'true'),
                            position: 'left'
                        }
                    }}
                />

            </div>
        );
    }
}

export default App;
