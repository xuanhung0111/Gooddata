import React, { Component } from 'react';
import { ColumnChart } from '@gooddata/sdk-ui-charts';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import catalogJson from './catalog.json';
import { newAttribute, newMeasure, newRelativeDateFilter, newAbsoluteDateFilter } from "@gooddata/sdk-model";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";

const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const variables = require('./testing-variable.json');
const metric = variables.metric;
const displayForm = variables.displayForm;
const attribute = variables.attribute;
const fromNow = variables.fromNow;
const toNow = variables.toNow;
const dataSet = variables.dataSet;
const from = variables.from;
const to = variables.to;
const C = new CatalogHelper(catalogJson);
const projectId = catalogJson.projectId;

class App extends Component {
    render() {
        const measure = newMeasure(C.measure(metric),
            f =>f.filters(newRelativeDateFilter(C.dateDataSet(dataSet), 'GDC.time.year', parseInt(fromNow), parseInt(toNow))).alias(metric)
        );

        const viewBy = newAttribute(C.attributeDisplayForm(attribute, displayForm));

        const filters = [newAbsoluteDateFilter(C.dateDataSet(dataSet), from, to)];

        return (
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {projectId}>
                    <div style={{ height: 600 }} className="s-column-chart">
                        <h1>Column chart</h1>
                        <ColumnChart
                            measures={[measure]}
                            viewBy={viewBy}
                            filters={filters}
                        />
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;
