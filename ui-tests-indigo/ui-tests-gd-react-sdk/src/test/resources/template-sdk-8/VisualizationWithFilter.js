import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import catalogJson from './catalog.json';
import { InsightView } from "@gooddata/sdk-ui-ext";
import { newPositiveAttributeFilter, uriRef } from "@gooddata/sdk-model";
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";

const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const variables = require('./testing-variable.json');
const elementAttributeUri = variables.elementAttributeUri;
const visualizationName = variables.visualizationName;
const attributeUri = variables.attributeUri;

const C = new CatalogHelper(catalogJson);
const visualization = C.visualization(visualizationName);
const testingProjectId = catalogJson.projectId;
const filter = [
    newPositiveAttributeFilter(uriRef(attributeUri), {uris: [elementAttributeUri]}),
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
