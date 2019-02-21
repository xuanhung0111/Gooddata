import React, { Component } from 'react';
import catalogJson from './catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

const testingProjectId = catalogJson.projectId;

class App extends Component {
    render() {
        return (
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {testingProjectId}
                    identifier = "12345@#"
                />
            </div>
        );
    }
}

export default App;
