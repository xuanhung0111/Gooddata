import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Headline } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const primaryMeasureTitle = variables.primaryMeasureTitle;
const dateDataSetTitle = variables.dateDataSetTitle;
const popAttributeTitle = variables.popAttributeTitle;

const C = new CatalogHelper(catalogJson);
const firstMeasure = C.measure(primaryMeasureTitle);
const popAttribute = C.dateDataSetAttribute(dateDataSetTitle, popAttributeTitle);

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

const popMeasure = {
    measure : {
        localIdentifier : 'popMeasure',
        definition : {
            popMeasureDefinition : {
                measureIdentifier : 'primaryMeasure',
                popAttribute : {
                    identifier : popAttribute
                }
            }
        },
        alias : primaryMeasureTitle + ' - SP year ago'
    }
};

class App extends Component {
    render() {
        return (
            <div style = {{ height : 400, width : 600 }}>
                <Headline
                    projectId = {testingProjectId}
                    primaryMeasure = {primaryMeasure}
                    secondaryMeasure = {popMeasure}
                />
            </div>
        );
    }
}

export default App;
