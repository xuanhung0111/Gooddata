#!/usr/bin/env sh

xvfb-run -a -s "-screen 0 1680x1050x24 -noreset" mvn integration-test "$@"
