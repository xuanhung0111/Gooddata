package com.gooddata.qa.graphene.manage;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;

@Test(groups = { "GoodSalesFacts" }, description = "Tests for GoodSales project (view and edit fact functionality) in GD platform")
public class SimpleFactTest extends GoodSalesAbstractTest {

    private String factName;
    private String description;
    private String factFolder;
    private String tagName;

    @BeforeClass
    public void setProjectTitle() {
	projectTitle = "Simple-fact-test";
    }

    @Test(dependsOnMethods = { "createProject" }, groups = { "tests" })
    public void initialize() throws InterruptedException, JSONException {
	this.factName = "Amount";
	this.factFolder = "Stage History";
	this.description = "Graphene test on view and modify Fact";
	this.tagName = "Graphene-test";

    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "fact-tests" })
    public void factAggregationsTest() throws InterruptedException {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|facts");
	waitForElementVisible(factsTable.getRoot());
	waitForDataPageLoaded();
	factsTable.selectObject(factName);
	for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
	    factDetailPage.createSimpleMetric(metricType, factName);
	}
    }

    private void initFact(String factName) {
	openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|facts");
	waitForElementVisible(factsTable.getRoot());
	waitForDataPageLoaded();
	factsTable.selectObject(factName);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "fact-tests" })
    private void changeFactFolderTest() throws InterruptedException {
	initFact(factName);
	factDetailPage.changeFactFolder(factFolder);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "fact-tests" })
    private void changeFactNameTest() {
	initFact(factName);
	String newFactName = this.factName + "changed";
	factName = factDetailPage.changeFactName(newFactName);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "fact-tests" })
    private void changeFactDescriptionTest() {
	initFact(factName);
	factDetailPage.addDescription(this.description);
    }

    @Test(dependsOnMethods = { "initialize" }, groups = { "fact-tests" })
    private void addFactTagTest() throws InterruptedException {
	initFact(factName);
	factDetailPage.addTag(this.tagName);
	String tagNameWithSpaces = "graphene test adding tag";
	factDetailPage.addTag(tagNameWithSpaces);
    }

    @Test(dependsOnGroups = { "fact-tests" }, groups = { "tests" })
    public void finalTest() {
	successfulTest = true;
    }
}
