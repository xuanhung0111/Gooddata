package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForReportsPageLoaded;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.NumericRangeFilterItem.Range;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.ResultSize;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.enums.VariableTypes;

@Test(groups = {"GoodSalesReportFilters"}, description = "Tests for GoodSales project (report filters functionality) in GD platform")
public class GoodSalesReportFilterTest extends GoodSalesAbstractTest {

    private String reportName;
    private String variableName;
    private String promptElements;
    private Map<String, String> data;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-filter";
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
        createReport(new ReportDefinition().withName(reportName)
                                           .withWhats("Amount")
                                           .withHows("Stage Name"),
                    "Simple filter report");
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"tests"})
    public void createVariableTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        data.put("variableName", this.variableName);
        data.put("userValueFlag", "false");
        data.put("attrElements", promptElements);
        variablePage.createVariable(VariableTypes.ATTRIBUTE, data);
    }

    @Test(dependsOnMethods = {"createReportTest"}, groups = {"filter-tests"})
    public void attributeFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createListValuesFilter("Stage Name", "Interest", 
                "Discovery", "Short List", "Negotiation", "Closed Won", "Closed Lost"));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest"}, groups = {"filter-tests"})
    public void rankingFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createRankingFilter(ResultSize.TOP.withSize(3), "Stage Name", "Amount"));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest"}, groups = {"filter-tests"})
    public void rangeFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createRangeFilter("Stage Name", "Amount",
                Range.IS_GREATER_THAN_OR_EQUAL_TO.withNumber(100000)));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest", "createVariableTest"}, groups = {"filter-tests"})
    public void promptFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createVariableFilter(variableName,
                "Interest", "Discovery", "Short List", "Negotiation"));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"filter-tests"}, groups = {"tests"})
    public void finalTest() throws InterruptedException {
        successfulTest = true;
    }

    private void initReport() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|domainPage|");
        waitForReportsPageLoaded(browser);
        reportsPage.getReportsList().openReport(this.reportName);
    }
}