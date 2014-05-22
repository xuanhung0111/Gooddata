package com.gooddata.qa.graphene.manage;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;

public class SimpleFactTest extends ObjectAbstractTest {

    private String factFolder;

    @BeforeClass
    public void setProjectTitle() {
	projectTitle = "Simple-fact-test";
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
    public void initialize() throws InterruptedException, JSONException {
	name = "Amount";
	this.factFolder = "Stage History";
	description = "Graphene test on view and modify Fact";
	tagName = "Graphene-test";
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "object-tests" })
    public void factAggregationsTest() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|facts");
	waitForElementVisible(factsTable.getRoot());
	waitForDataPageLoaded();
	factsTable.selectObject(name);
	for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
	    factDetailPage.createSimpleMetric(metricType, name);
	}
    }

    @Override
    public void initObject(String factName) {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|facts");
	waitForElementVisible(factsTable.getRoot());
	waitForDataPageLoaded();
	factsTable.selectObject(factName);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "object-tests" })
    public void changeFactFolderTest() throws InterruptedException {
	initObject(name);
	factDetailPage.changeFactFolder(factFolder);
    }

    @Test(dependsOnGroups = { "final-tests" }, groups = { "tests" })
    public void finalTest() {
	successfulTest = true;
    }
}
