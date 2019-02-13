import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Headline } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const firstAttributeTitle = variables.firstAttributeTitle;

const C = new CatalogHelper(catalogJson);
const firstAttribute = C.measure(firstAttributeTitle);
const testingProjectId = catalogJson.projectId;
const primaryAttribute = {
    attribute : {
        localIdentifier : 'firstAttribute',
        definition : {
            attributeDefinition : {
                item : {
                    identifier : firstAttribute
                }
            }
        }
    }
};

class App extends Component {
    render() {
        return (
            <div style = {{ height : 400, width : 600 }}>
                <Headline
                    projectId = {testingProjectId}
                    primaryMeasure = {primaryAttribute}
                />
            </div>
        );
    }
}

export default App;
