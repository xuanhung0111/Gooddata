import React, { Component } from 'react';
import { Headline } from '@gooddata/sdk-ui-charts';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import catalogJson from './catalog.json';
import { newAttribute } from "@gooddata/sdk-model";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";

const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const variables = require('./testing-variable.json');
const firstAttributeTitle = variables.firstAttributeTitle;

const C = new CatalogHelper(catalogJson);
const firstAttribute = C.measure(firstAttributeTitle);
const testingProjectId = catalogJson.projectId;
const primaryAttribute = newAttribute(firstAttribute);

class App extends Component {
    render() {
        return (
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {testingProjectId}>
                    <div style={{ height : 400, width : 600 }}>
                        <Headline
                            primaryMeasure = {primaryAttribute}
                        />
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;
