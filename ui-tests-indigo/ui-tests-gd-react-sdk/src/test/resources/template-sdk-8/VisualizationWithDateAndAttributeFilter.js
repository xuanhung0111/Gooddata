import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/sdk-ui-all';
import { InsightView } from "@gooddata/sdk-ui-ext";
import { isPositiveAttributeFilter, isAttributeElementsByRef, uriRef } from "@gooddata/sdk-model";
import catalogJson from './catalog.json';
import { DateFilter, DateFilterHelpers, AttributeFilter } from "@gooddata/sdk-ui-filters";
import { BackendProvider, WorkspaceProvider } from "@gooddata/sdk-ui";
import bearFactory, { ContextDeferredAuthProvider } from "@gooddata/sdk-backend-bear";
import "@gooddata/sdk-ui-charts/styles/css/main.css";
import "@gooddata/sdk-ui-ext/styles/css/insightView.css";
import "@gooddata/sdk-ui-filters/styles/css/dateFilter.css";
import "@gooddata/sdk-ui-filters/styles/css/attributeFilter.css";

const variables = require('./testing-variable.json');
const C = new CatalogHelper(catalogJson);
const backend = bearFactory().withAuthentication(new ContextDeferredAuthProvider());
const projectId = catalogJson.projectId;
const visualizationUri = variables.visualizationUri;
const dataSet = variables.dataSet;
const attribute = variables.attribute;
const attributeDisplayFormId = C.attributeDisplayForm(attribute, attribute);


let dateFrom = new Date();
dateFrom.setMonth(dateFrom.getMonth() - 1);

const availableGranularities = [
    "GDC.time.date",
    "GDC.time.month",
    "GDC.time.quarter",
    "GDC.time.year",
    "GDC.time.week_us",
];

const defaultDateFilterOptions = {
    allTime: {
        localIdentifier: "ALL_TIME",
        type: "allTime",
        name: "All time",
        visible: true,
    },
    absoluteForm: {
        localIdentifier: "ABSOLUTE_FORM",
        type: "absoluteForm",
        from: dateFrom.toISOString().substr(0, 10), // 'YYYY-MM-DD'
        to: new Date().toISOString().substr(0, 10), // 'YYYY-MM-DD'
        name: "Static period",
        visible: true,
    },
    relativeForm: {
        localIdentifier: "RELATIVE_FORM",
        type: "relativeForm",
        granularity: "GDC.time.month",
        name: "Floating range",
        visible: true,
        availableGranularities,
        from: 0,
        to: -1,
    },
    relativePreset: {
        "GDC.time.date": [
            {
                from: -6,
                to: 0,
                granularity: "GDC.time.date",
                localIdentifier: "fae8aca1-6bcf-456e-8547-e10656859f4d",
                type: "relativePreset",
                visible: true,
                name: "Last 7 days",
            },
            {
                from: -29,
                to: 0,
                granularity: "GDC.time.date",
                localIdentifier: "29bd0e2d-51b0-42f7-9355-70aa610ac06c",
                type: "relativePreset",
                visible: true,
                name: "Last 30 days",
            },
            {
                from: -89,
                to: 0,
                granularity: "GDC.time.date",
                localIdentifier: "9370c647-8cbe-4850-82c2-0783513e4fe3",
                type: "relativePreset",
                visible: true,
                name: "Last 90 days",
            },
        ],
        "GDC.time.month": [
            {
                from: 0,
                to: 0,
                granularity: "GDC.time.month",
                localIdentifier: "ec54d656-bbea-4559-b6b2-9de80951eb20",
                type: "relativePreset",
                visible: true,
                name: "This month",
            },
            {
                from: -1,
                to: -1,
                granularity: "GDC.time.month",
                localIdentifier: "0787513c-ec02-439f-9781-7da80db91a27",
                type: "relativePreset",
                visible: true,
                name: "Last month",
            },
            {
                from: -11,
                to: 0,
                granularity: "GDC.time.month",
                localIdentifier: "b2790ff0-48ba-402f-a3b3-6722e325042b",
                type: "relativePreset",
                visible: true,
                name: "Last 12 months",
            },
        ],
        "GDC.time.quarter": [
            {
                from: 0,
                to: 0,
                granularity: "GDC.time.quarter",
                localIdentifier: "cdf546a5-4394-4583-9a7d-ab35e880f54b",
                type: "relativePreset",
                visible: true,
                name: "This quarter",
            },
            {
                from: -1,
                to: -1,
                granularity: "GDC.time.quarter",
                localIdentifier: "bb5364ba-0c0e-44d9-8cfc-f8ee995dcb53",
                type: "relativePreset",
                visible: true,
                name: "Last quarter",
            },
            {
                from: -3,
                to: 0,
                granularity: "GDC.time.quarter",
                localIdentifier: "9b838d14-c88d-4652-bf79-08b895688cd8",
                type: "relativePreset",
                visible: true,
                name: "Last 4 quarters",
            },
        ],
        "GDC.time.year": [
            {
                from: 0,
                to: 0,
                granularity: "GDC.time.year",
                localIdentifier: "d5e444df-67f6-4034-8b80-0bb0f6c6a210",
                type: "relativePreset",
                visible: true,
                name: "This year",
            },
            {
                from: -1,
                to: -1,
                granularity: "GDC.time.year",
                localIdentifier: "eecbe244-8560-466d-876c-4d3cc96ea61a",
                type: "relativePreset",
                visible: true,
                name: "Last year",
            },
        ],
    },
};

//← Chart Config DrillableItems →

class App extends Component {
    constructor(props) {
        super(props);
        this.state = {
            selectedFilterOption: defaultDateFilterOptions.allTime,
            excludeCurrentPeriod: true,
            attributeFilter: null,

        };
    }

    onLoadingChanged(...params) {
        // tslint:disable-next-line:no-console
        console.info("AttributeFilterExample onLoadingChanged", ...params);
    }

    onApplyAttributeFilter = (filter) => {
        // tslint:disable-next-line:no-console
        console.log("AttributeFilterExample onApply", filter);

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
            filters =
                {
                    positiveAttributeFilter: {
                        displayForm,
                        in: inElements,
                    },
                }
        }
        this.setState({
            attributeFilter: filters,
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
            filters =
                {
                    negativeAttributeFilter: {
                        displayForm,
                        notIn,
                    },
                }
        }

        this.setState({
            attributeFilter: filters,
        });
    }

    onApply = (dateFilterOption, excludeCurrentPeriod) => {

        this.setState({
            selectedFilterOption: dateFilterOption,
            excludeCurrentPeriod,
        });
        console.log(
            "DateFilterExample onApply",
            "selectedFilterOption:",
            dateFilterOption,
            "excludeCurrentPeriod:",
            excludeCurrentPeriod,
        );
    };

    render(){

        const dateFilter = DateFilterHelpers.mapOptionToAfm(
            this.state.selectedFilterOption,
            {
                identifier: C.dateDataSet(dataSet),
            },
            this.state.excludeCurrentPeriod,
        );
        const attributeFilter = this.state.attributeFilter;

        return (
            <BackendProvider backend={backend}>
                <WorkspaceProvider workspace= {projectId}>
                    <div className="App ">
                        <header>
                            <div className={"dash-filters-all"}>
                                <div className={"s-attribute-filter"} style={{ width: 150, height: 50  }}>
                                    <AttributeFilter
                                        identifier={attributeDisplayFormId}
                                        fullscreenOnMobile={false}
                                        onApply={this.onApplyAttributeFilter}
                                    />
                                </div>
                                <div className={"dash-filters-date"} style={{ width: 160, height: 50  }}>
                                    <DateFilter
                                        excludeCurrentPeriod={this.state.excludeCurrentPeriod}
                                        selectedFilterOption={this.state.selectedFilterOption}
                                        filterOptions={defaultDateFilterOptions}
                                        availableGranularities={availableGranularities}
                                        customFilterName="Date filter"
                                        dateFilterMode="active"
                                        onApply={this.onApply}
                                    />
                                </div>
                            </div>
                            <div style={{ height: 600, width: 600 }}>
                                <InsightView
                                    insight={uriRef(visualizationUri)}
                                    onLoadingChanged={this.onLoadingChanged}
                                    filters={(attributeFilter || dateFilter) ? [attributeFilter, dateFilter] : []}
                                />
                            </div>

                        </header>
                    </div>
                </WorkspaceProvider>
            </BackendProvider>
        );
    }
}

export default App;
