import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import catalogJson from './catalog.json';
import { InsightView } from "@gooddata/sdk-ui-ext";
import { newAbsoluteDateFilter } from "@gooddata/sdk-model";
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";

const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const variables = require('./testing-variable.json');
const visualizationName = variables.visualizationName;
const from = variables.from;
const to = variables.to;
const dateAttributeName = variables.dateAttributeName;

const C = new CatalogHelper(catalogJson);
const visualization = C.visualization(visualizationName);
const dateAttribute = C.dateDataSet(dateAttributeName);
const testingProjectId = catalogJson.projectId;
const filter = [
    newAbsoluteDateFilter(dateAttribute, from, to),
];

class App extends Component {
    render() {
        return (
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {testingProjectId}>
                    <div style={{ height: 400, width: 600 }}>
                        <InsightView insight= {visualization} filters = {filter}/>
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;
