# Welcome to Gooddata Integration UI tests with Graphene

This maven project consists of following technologies/frameworks:
 - TestNG
 - Arquillian (testng-standalone)
 - Graphene/Drone
 
This combination allows you to easily write UI tests in Java.

## Documentation
Basic documentation is available on Confluence page [Graphene UI Tests](https://confluence.intgdc.com/display/Quality/Graphene+UI+Tests).
 
## Test variables
Variables are listed at properties file ui-tests-core/src/test/resources/variables-env-test.properties, verify the variables before running the test.
You can also created a new properties file (ideally copy of original one with changed values) and then use parameter ```-DpropertiesPath=/path_to_file```

Most important variables are:
```
host=staging.getgooddata.com
user=bear@gooddata.com (shouldn't be used on staging anymore, use qa+test@gooddata.com instead)
password=xxx
projectId=GoodSalesDemo
projectName=GoodSales
```
See the file for more details, test accounts are taken from [Test accounts](https://confluence.intgdc.com/display/plat/Test+Accounts)

## Build
### Local build
Build can be triggered from any ui-tests-* folder with the following command
```
mvn clean install -Pselenium
```
Or you can run all tests together from parent project.

### Running single test suite
Specify test suite you want to run by _test_ parameter.
```
mvn -Dtest=SchedulesTest integration-test -Pselenium
```
Please note test suite name needs to be suffixed by Test not Tests. Otherwise it's not possible to run it separately.

### Debugging
Run integration test with [Surefire](http://maven.apache.org/surefire/maven-surefire-plugin/) plugin debug option.
```
mvn -Dmaven.surefire.debug integration-test -Pselenium
```
Test execution should wait for attaching debugger to 5005 port. See [example configuration](https://www.dropbox.com/s/v0h86cwkrf6j4n4/maven-debugger.png?dl=0) of maven-debug inside IntelliJ IDEA.

### Jenkins build
Done - see more details on above confluence page.

## UI Tests modules
Currently there are following modules available (use them for tests of specific area and feel free to add new):
 * ui-fragments - includes all Graphene page/object fragmnets (used across all tests)
 * ui-tests-connectors - for running connectors tests
 * ui-tests-core - includes common classes for all tests and basic tests
 * ui-tests-dashboards - for running dashboard tests
 * ui-tests-dss - for running DSS tests
 * ui-tests-tools - for running extra tools tests (specific usage, no tests by default)
 * ui-tests-upload - for running csv upload tests (experimental)

## Git workflow
This repository is switched to the standard gitflow model (as you are used to on other component repositories).

Summary:
 * happy path: open PR to `develop` branch (for new features/tests or bugfixes related to `develop` stage)
 * open PR to `release/xxx` branch in case of fixing any test at the `release` stage
  * following merge from `release` to `develop` branch should be done (auto-merging is missing at the time of writing)
 * open PR to `master` branch in case of hotfixing tests on production
  * following merge from the `master` to `release` and `develop` should be done (auto-merging is missing at the time of writing)
 * `release/xxx` branch is created automatically from `develop` during the Code Drop
 * merge of `release` branch to `master` happens automatically before the production release

More details about the [GitFlow](https://confluence.intgdc.com/display/plat/Gitflow).
