import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import catalogJson from './catalog.json';
import { InsightView } from "@gooddata/sdk-ui-ext";
import { isAttributeElementsByRef, isPositiveAttributeFilter, uriRef } from "@gooddata/sdk-model";
import { AttributeFilter } from "@gooddata/sdk-ui-filters";
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";
import "@gooddata/sdk-ui-filters/styles/css/attributeFilter.css";

const variables = require('./testing-variable.json');
const C = new CatalogHelper(catalogJson);
const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const visualizationUri = variables.visualizationUri;
const attribute = variables.attribute;
const testingProjectId = catalogJson.projectId;
const attributeDisplayFormId = C.attributeDisplayForm(attribute, attribute);

class App extends Component {
    constructor(props) {
        super(props);
        this.onApply = this.onApply.bind(this);
        this.state = {
            filters: [],
        };
    }

    onLoadingChanged(...params) {
        // tslint:disable-next-line:no-console
        console.info("AttributeFilterExample onLoadingChanged", ...params);
    }

    onApply(filter) {
        // tslint:disable-next-line:no-console
        console.log("AttributeFilterExample onApply", filter);
        this.setState({
            filters: [],
        });

        if (isPositiveAttributeFilter(filter)) {
            this.filterPositiveAttribute(filter);
        } else {
            this.filterNegativeAttribute(filter);
        }
    }

    filterPositiveAttribute(filter) {
        let filters;
        const {
            positiveAttributeFilter,
            positiveAttributeFilter: { displayForm },
        } = filter;
        const inElements = filter.positiveAttributeFilter.in;
        const checkLengthOfFilter = isAttributeElementsByRef(positiveAttributeFilter.in)
            ? positiveAttributeFilter.in.uris.length !== 0
            : positiveAttributeFilter.in.values.length !== 0;

        if (checkLengthOfFilter) {
            filters = [
                {
                    positiveAttributeFilter: {
                        displayForm,
                        in: inElements,
                    },
                },
            ];
        }
        this.setState({
            filters,
        });
    }

    filterNegativeAttribute(filter) {
        let filters;
        const {
            negativeAttributeFilter: { notIn, displayForm },
        } = filter;
        const checkLengthOfFilter = isAttributeElementsByRef(notIn)
            ? notIn.uris.length !== 0
            : notIn.values.length !== 0;

        if (checkLengthOfFilter) {
            filters = [
                {
                    negativeAttributeFilter: {
                        displayForm,
                        notIn,
                    },
                },
            ];
        }

        this.setState({
            filters,
        });
    }

    render(){

        const { filters } = this.state;

        return (
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {testingProjectId}>
                    <div className="App ">
                        <header>
                            <div className={"dash-filters-all"}>
                                <div className={"s-attribute-filter"} style={{ width: 150, height: 50  }}>
                                    <AttributeFilter
                                        identifier={attributeDisplayFormId}
                                        fullscreenOnMobile={false}
                                        onApply={this.onApply}
                                    />
                                </div>
                            </div>
                            <div style={{ height: 400, width: 600 }}>
                                <InsightView
                                    insight= {uriRef(visualizationUri)}
                                    onLoadingChanged={this.onLoadingChanged}
                                    filters={filters} />
                            </div>
                        </header>
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;
