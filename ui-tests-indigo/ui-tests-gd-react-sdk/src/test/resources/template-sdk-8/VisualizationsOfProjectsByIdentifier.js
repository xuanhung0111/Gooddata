import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import { InsightView } from "@gooddata/sdk-ui-ext";
import catalogJson from './catalog.json';
import otherCatalogJson from './other-catalog.json';
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";

const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const variables = require('./testing-variable.json');
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
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {firstProjectId}>
                    <div style={{ height: 400, width: 600 }}>
                        <InsightView insight= {firstVisualization} />
                    </div>
                </WorkspaceProvider>

                <hr className="separator" />

                <WorkspaceProvider workspace= {secondProjectId}>
                    <div style={{ height: 400, width: 600 }}>
                        <InsightView insight= {secondVisualization} />
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;
