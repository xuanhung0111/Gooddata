#!/usr/bin/env bash
########################################
# Create and setup a React application #
########################################
RESOURCE_DIR=$HOME/ui-tests-indigo/ui-tests-gd-react-sdk/src/test/resources
REACT_PROJECT_NAME=$1
TESTING_HOST=$2

# Create React application
mkdir /tmp/react
cd /tmp/react
create-react-app $REACT_PROJECT_NAME
cd $REACT_PROJECT_NAME

# Configure HTTPS on NodeJs
jq --arg enableHTTPs "HTTPS=true react-scripts start" '.scripts.start=$enableHTTPs' package.json > enableHTTPsNodeJs.json
cp enableHTTPsNodeJs.json package.json
rm -rf enableHTTPsNodeJs.json

# Add "yarn stop" to force to stop NodeJs
jq --arg stopNodeJs "pkill -9 node" '.scripts.stop = $stopNodeJs' package.json > addYarnStop.json
cp addYarnStop.json package.json
rm -rf addYarnStop.json

# Install the latest @gooddata/react-components
yarn add @gooddata/react-components

# Install node-saas as https://jira.intgdc.com/browse/ONE-3381
yarn add node-sass

if [ -n "$TESTING_HOST" ]; then
    # Setup proxy to prevent cross-origin issues
    sed s/replaceWithTestingHost/$TESTING_HOST/g $RESOURCE_DIR/scripts/setupProxy.js > ./src/setupProxy.js
fi

# Start react-sdk-app project up
nohup yarn start >> $REACT_PROJECT_NAME.log 2>&1 &

################################################################################################
# NOTE: Stand at $REACT_PROJECT_NAME .Use command "yarn start/stop" to start/stop React project#
################################################################################################
