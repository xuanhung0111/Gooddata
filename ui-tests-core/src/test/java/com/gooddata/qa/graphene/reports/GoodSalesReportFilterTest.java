package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.AttributeFilterItem;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.PromptFilterItem;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem.RangeType;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;

@Test(groups = {"GoodSalesReportFilters"},
        description = "Tests for GoodSales project (report filters functionality)")
public class GoodSalesReportFilterTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Test Filter";
    private static final String VARIABLE_NAME = "F Stage Name";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-filter";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportTest() {
        createReport(new UiReportDefinition().withName(REPORT_NAME)
                        .withWhats("Amount")
                        .withHows("Stage Name"),
                "Simple filter report");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createVariableTest() {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable(VARIABLE_NAME)
                .withAttribute("Stage Name")
                .withAttributeValues("Interest", "Discovery", "Short List", "Negotiation"));
    }

    @Test(dependsOnMethods = {"createReportTest"})
    public void attributeFilterTest() {
        initReport();

        AttributeFilterItem filterItem = FilterItem.Factory
                .createAttributeFilter("Stage Name", "Interest", "Discovery", "Short List",
                        "Negotiation", "Closed Won", "Closed Lost");

        reportPage.addFilter(filterItem);
        assertTrue(reportPage.isReportContains(filterItem.getValues()),
                "Attribute filter is not apply successfully");

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest"})
    public void rankingFilterTest() {
        initReport();

        reportPage.addFilter(FilterItem.Factory.createRankingFilter("Amount", "Stage Name"));
        assertTrue(reportPage.isRankingFilterApplied(Arrays.
                asList(4249027.88f, 5612062.60f, 18447266.14f)),
                "Ranking filter is not applied successfully");

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest"})
    public void rangeFilterTest() {
        initReport();

        RangeFilterItem filterItem = FilterItem.Factory
                .createRangeFilter(RangeType.IS_GREATER_THAN_OR_EQUAL_TO, 100000, "Amount", "Stage Name");

        reportPage.addFilter(filterItem);
        assertTrue(reportPage.isRangeFilterApplied(filterItem),
                "Range filter is not applied successfully");

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest", "createVariableTest"})
    public void promptFilterTest() {
        initReport();

        PromptFilterItem filterItem = FilterItem.Factory.createPromptFilter(VARIABLE_NAME,
                "Interest", "Discovery", "Short List", "Negotiation");

        reportPage.addFilter(filterItem);
        assertTrue(reportPage.isReportContains(filterItem.getPromptElements()),
                "Prompt filter is not applied successfully");

        reportPage.saveReport();
        checkRedBar(browser);
    }

    private void initReport() {
        initReportsPage();
        reportsPage.getReportsList().openReport(REPORT_NAME);
        waitForAnalysisPageLoaded(browser);
    }
}
