#!/usr/bin/env bash
########################################
# Create and setup a React application #
########################################
RESOURCE_DIR=$(dirname "$(realpath $0)")
REACT_PROJECT_NAME=$1
TESTING_HOST=$2
UI_SDK_VERSION=$3

# Create React application
mkdir /tmp/react
cd /tmp/react
yarn create react-app $REACT_PROJECT_NAME
cd $REACT_PROJECT_NAME

# Configure HTTPS on NodeJs
jq --arg enableHTTPs "HTTPS=true forever start -c 'react-scripts start' ./" '.scripts.start=$enableHTTPs' package.json > enableHTTPsNodeJs.json
cp enableHTTPsNodeJs.json package.json
rm -rf enableHTTPsNodeJs.json

# Add "yarn stop" to force to stop NodeJs
jq --arg stopNodeJs "pkill -9 node" '.scripts.stop = $stopNodeJs' package.json > addYarnStop.json
cp addYarnStop.json package.json
rm -rf addYarnStop.json

# Install the latest @gooddata/react-components
if [ ${UI_SDK_VERSION:0:1} == "8" ]; then
   yarn add @gooddata/sdk-ui-all@$UI_SDK_VERSION @gooddata/sdk-backend-bear@$UI_SDK_VERSION
else
   yarn add @gooddata/react-components@$UI_SDK_VERSION
fi

# Install node-saas as https://jira.intgdc.com/browse/ONE-3381
yarn add node-sass

if [ -n "$TESTING_HOST" ]; then
    # Setup proxy to prevent cross-origin issues
    sed s/replaceWithTestingHost/$TESTING_HOST/g $RESOURCE_DIR/setupProxy.js > ./src/setupProxy.js
fi

# Install the latest forever
yarn global add forever

# Start react-sdk-app project up
echo "Starting react project"
export BROWSER='none'
yarn start
echo "Done"


################################################################################################
# NOTE: Stand at $REACT_PROJECT_NAME .Use command "yarn start/stop" to start/stop React project#
################################################################################################
