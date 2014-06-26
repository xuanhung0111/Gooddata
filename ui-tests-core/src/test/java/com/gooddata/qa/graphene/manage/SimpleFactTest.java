package com.gooddata.qa.graphene.manage;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;

@Test(groups = {"GoodSalesFacts"}, description = "Tests for GoodSales project (view and edit fact functionality) in GD platform")
public class SimpleFactTest extends ObjectAbstractTest {

    private String factFolder;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "Simple-fact-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests", "object-tests"})
    public void initialize() throws InterruptedException, JSONException {
        name = "Amount";
        this.factFolder = "Stage History";
        description = "Graphene test on view and modify Fact";
        tagName = "Graphene-test";
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"property-object-tests"})
    public void factAggregationsTest() throws InterruptedException {
        openUrl(ui.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|facts");
        waitForElementVisible(ui.factsTable.getRoot());
        checkUtils.waitForDataPageLoaded();
        ui.factsTable.selectObject(name);
        for (SimpleMetricTypes metricType : SimpleMetricTypes.values()) {
            ui.factDetailPage.createSimpleMetric(metricType, name);
        }
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"property-object-tests"})
    public void changeFactFolderTest() throws InterruptedException {
        initObject(name);
        ui.factDetailPage.changeFactFolder(factFolder);
    }

    @Override
    public void initObject(String factName) {
        openUrl(ui.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|facts");
        checkUtils.waitForDataPageLoaded();
        waitForElementVisible(ui.factsTable.getRoot());
        ui.factsTable.selectObject(factName);
    }
}