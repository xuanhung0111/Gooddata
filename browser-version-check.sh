#!/bin/bash
browser_name=${1}

docker_file_path="Dockerfile_$browser_name"
BROWSER_CURRENT_VERSION=$(awk -F "=" '/^ARG BROWSER_VERSION/{print $NF}' $docker_file_path)
DRIVER_CURRENT_VERSION=$(awk -F "=" '/^ARG DRIVER_VERSION/{print $NF}' $docker_file_path)

UPDATE_BROWSER=true
if [ $browser_name == 'firefox' ]; then
  BROWSER_LATEST_VERSION=$(curl -s "https://download.mozilla.org/?product=firefox-latest&os=linux&lang=en-US" | sed 's/.*releases\///' | sed 's/\/.*//')
  DRIVER_LATEST_VERSION=$(curl -s https://github.com/mozilla/geckodriver/releases/latest | sed 's/.*tag\/v//' | sed 's/".*//')
else
  BROWSER_LATEST_VERSION=$(echo $(curl -s https://www.ubuntuupdates.org/package/google_chrome/stable/main/base/google-chrome-stable) | sed 's/.*\(Latest version.*Release\).*/\1/' | sed 's/.*>\([[:digit:]].*[[:digit:]]\)-.*/\1/')
  DRIVER_LATEST_VERSION=$(curl -s https://chromedriver.storage.googleapis.com/LATEST_RELEASE)
fi

echo "$browser_name latest version: $BROWSER_LATEST_VERSION"
echo "$browser_name current version: $BROWSER_CURRENT_VERSION"
# compare main version
if [[ $(echo $BROWSER_LATEST_VERSION | sed 's/\..*//') > $(echo $BROWSER_CURRENT_VERSION | sed 's/\..*//') ]]; then
  echo "New $browser_name version: $BROWSER_LATEST_VERSION is released"
# compare sub version
elif [[ $(echo $BROWSER_LATEST_VERSION | sed -E 's/\.+//g') > $(echo $BROWSER_CURRENT_VERSION | sed -E 's/\.+//g') ]]; then
  echo "New $browser_name version: $BROWSER_LATEST_VERSION is released"
else
  echo "No new $browser_name version found"
  UPDATE_BROWSER=false
fi

export UPDATE_BROWSER=$UPDATE_BROWSER
export BROWSER_CURRENT_VERSION=$BROWSER_CURRENT_VERSION
export DRIVER_CURRENT_VERSION=$DRIVER_CURRENT_VERSION
export BROWSER_NEW_VERSION=$BROWSER_LATEST_VERSION
export DRIVER_NEW_VERSION=$DRIVER_LATEST_VERSION
