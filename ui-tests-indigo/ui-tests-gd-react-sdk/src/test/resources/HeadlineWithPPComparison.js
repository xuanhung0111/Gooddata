import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Headline } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const primaryMeasureTitle = variables.primaryMeasureTitle;
const dateDataSetTitle = variables.dateDataSetTitle;
const from = variables.from;
const to = variables.to;

const C = new CatalogHelper(catalogJson);
const firstMeasure = C.measure(primaryMeasureTitle);
const dateDataSet = C.dateDataSet(dateDataSetTitle);

const testingProjectId = catalogJson.projectId;
const primaryMeasure = {
    measure: {
        localIdentifier : 'primaryMeasure',
        definition : {
            measureDefinition : {
                item : {
                    identifier : firstMeasure
                }
            }
        }
    }
};

const previousPeriodMeasure = {
    measure : {
        localIdentifier : 'previousPeriodMeasure',
        definition : {
            previousPeriodMeasure : {
                measureIdentifier : 'primaryMeasure',
                dateDataSets : [{
                    dataSet : {
                        identifier : dateDataSet
                    },
                    periodsAgo: 1
                }]
            }
        },
        alias : primaryMeasureTitle + ' - period ago'
    }
};

const filter = [
    {
        absoluteDateFilter : {
            dataSet : {
                identifier : dateDataSet
            },
            from : from,
            to : to
        }
    }
];

class App extends Component {
    render() {
        return (
            <div style = {{ height : 400, width : 600 }}>
                <Headline
                    projectId = {testingProjectId}
                    primaryMeasure = {primaryMeasure}
                    secondaryMeasure = {previousPeriodMeasure}
                    filters = {filter}
                />
            </div>
        );
    }
}

export default App;
