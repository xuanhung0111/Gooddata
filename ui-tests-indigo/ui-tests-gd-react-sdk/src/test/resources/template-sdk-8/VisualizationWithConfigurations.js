import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import catalogJson from './catalog.json';
import { InsightView } from "@gooddata/sdk-ui-ext";
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";

const variables = require('./testing-variable.json');
const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const visualizationName = variables.visualizationName;
const firstColor = variables.firstColor;
const secondColor = variables.secondColor;

const C = new CatalogHelper(catalogJson);
const visualization = C.visualization(visualizationName);
const testingProjectId = catalogJson.projectId;

class App extends Component {
    render() {
        return (
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {testingProjectId}>
                    <div style={{ height: 400, width: 600 }}>
                        <InsightView
                            insight= {visualization}
                            config = {{
                                colors: [firstColor, secondColor],
                                legend: {
                                    enabled: (variables.hasLegend === 'true'),
                                    position: 'left'
                                }
                            }}
                        />
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;