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

### Jenkins build
Done - see more details on above confluence page.

## UI Tests modules
Currently there are following modules available (use them for tests of specific area and feel free to add new):
 * ui-fragments - includes all Graphene page/object fragmnets (used across all tests)
 * ui-tests-ccc - for running CCC tests
 * ui-tests-connectors - for running connectors tests
 * ui-tests-core - includes common classes for all tests and basic tests
 * ui-tests-dashboards - for running dashboard tests
 * ui-tests-hds - for running HDS tests
 * ui-tests-reports - for running report tests
 * ui-tests-tools - for running extra tools tests (specific usage, no tests by default)
 * ui-tests-upload - for running csv upload tests (experimental)