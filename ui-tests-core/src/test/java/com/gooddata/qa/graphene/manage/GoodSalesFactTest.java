package com.gooddata.qa.graphene.manage;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = {"GoodSalesFacts"}, description = "Tests for GoodSales project (view and edit fact functionality) in GD platform")
public class GoodSalesFactTest extends ObjectAbstractTest {

    private String factFolder;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-fact";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"object-tests", "tests"})
    public void initialize() throws InterruptedException, JSONException {
        name = "Amount";
        this.factFolder = "Stage History";
        description = "Graphene test on view and modify Fact";
        tagName = "Graphene-test";
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"property-object-tests", "tests"})
    public void factAggregationsTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|facts");
        waitForElementVisible(factsTable.getRoot());
        waitForDataPageLoaded(browser);
        factsTable.selectObject(name);
        for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
            factDetailPage.createSimpleMetric(metricType, name);
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"property-object-tests", "tests"})
    public void changeFactFolderTest() throws InterruptedException {
        initObject(name);
        factDetailPage.changeFactFolder(factFolder);
    }

    @Override
    public void initObject(String factName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|facts");
        waitForDataPageLoaded(browser);
        waitForElementVisible(factsTable.getRoot());
        factsTable.selectObject(factName);
    }
}