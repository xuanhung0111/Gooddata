package com.gooddata.qa.graphene.project;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.gooddata.qa.graphene.enums.FilterTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.VariableTypes;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;

@Test(groups = {"GoodSalesReportFilters"}, description = "Tests for GoodSales project (report filters functionality) in GD platform")
public class FilterReportTest extends GoodSalesAbstractTest {

    private String reportName;
    private String variableName;
    private String promptElements;
    private Map<String, String> data;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-filter-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void initialize() throws InterruptedException, JSONException {
        reportName = "Test Filter";
        variableName = "F Stage Name";
        data = new HashMap<String, String>();
        data.put("attribute", "Stage Name");
        data.put("attributeElements", "Interest, Discovery, Short List, Negotiation, Closed Won, Closed Lost");
        data.put("metric", "Amount");
        data.put("type", "Top"); // (valid elements: "Top", "Bottom")
        data.put("size", "3"); // (valid elements: "1", "3", "5" , "10")
        data.put("number", "100000");
        promptElements = "Interest, Discovery, Short List, Negotiation";
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"tests"})
    public void createReportTest() throws InterruptedException {
        List<String> what = Arrays.asList("Amount");
        List<String> how = Arrays.asList("Stage Name");
        ui.createReport(reportName, ReportTypes.TABLE, what, how, "Simple filter report");
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"tests"})
    public void createVariableTest() throws InterruptedException {
        openUrl(ui.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        data.put("variableName", this.variableName);
        data.put("userValueFlag", "false");
        data.put("attrElements", promptElements);
        ui.variablePage.createVariable(VariableTypes.ATTRIBUTE, data);
    }

    @Test(dependsOnMethods = {"createReportTest"}, groups = {"filter-tests"})
    public void attributeFilterTest() throws InterruptedException {
        initReport();
        ui.reportPage.addFilter(FilterTypes.ATTRIBUTE, data);
        ui.reportPage.saveReport();
        checkUtils.checkRedBar();
    }

    @Test(dependsOnMethods = {"createReportTest"}, groups = {"filter-tests"})
    public void rankingFilterTest() throws InterruptedException {
        initReport();
        ui.reportPage.addFilter(FilterTypes.RANK, data);
        ui.reportPage.saveReport();
        checkUtils.checkRedBar();
    }

    @Test(dependsOnMethods = {"createReportTest"}, groups = {"filter-tests"})
    public void rangeFilterTest() throws InterruptedException {
        initReport();
        ui.reportPage.addFilter(FilterTypes.RANGE, data);
        ui.reportPage.saveReport();
        checkUtils.checkRedBar();
    }

    @Test(dependsOnMethods = {"createReportTest", "createVariableTest"}, groups = {"filter-tests"})
    public void promptFilterTest() throws InterruptedException {
        initReport();
        data.put("variable", this.variableName);
        data.put("promptElements", promptElements);
        ui.reportPage.addFilter(FilterTypes.PROMPT, data);
        ui.reportPage.saveReport();
        checkUtils.checkRedBar();
    }

    @Test(dependsOnGroups = {"filter-tests"}, groups = {"tests"})
    public void finalTest() throws InterruptedException {
        successfulTest = true;
    }

    private void initReport() {
        openUrl(ui.PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage|");
        checkUtils.waitForReportsPageLoaded();
        ui.reportsPage.getReportsList().openReport(this.reportName);
    }
}