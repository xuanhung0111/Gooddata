#!/usr/bin/env sh

xvfb-run -a -s "-screen 0 1680x1050x24 -noreset" mvn integration-test -Duser.home=/tmp -DscreenResolution=1680x1050 \
    -Dlanguage=en-US -Dtest.execution.env=local-chrome -Dtimeout=90 -Dvideo.recording=true -DtestIdentification=CI#$BUILD_NUMBER#$JOB_NAME#$BUILD_ID \
    -DchromeDriverBinaryPath=/bin/chromedriver -DchromeDriverArguments=no-sandbox -Pselenium -am "$@"
