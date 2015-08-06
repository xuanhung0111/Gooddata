package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.NumericRangeFilterItem.Range;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.ResultSize;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;

@Test(groups = {"GoodSalesReportFilters"},
        description = "Tests for GoodSales project (report filters functionality)")
public class GoodSalesReportFilterTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Test Filter";
    private static final String VARIABLE_NAME = "F Stage Name";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-filter";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createReportTest() throws InterruptedException {
        createReport(new ReportDefinition().withName(REPORT_NAME)
                        .withWhats("Amount")
                        .withHows("Stage Name"),
                "Simple filter report");
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createVariableTest() throws InterruptedException {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable(VARIABLE_NAME)
                .withAttribute("Stage Name")
                .withAttributeElements("Interest", "Discovery", "Short List", "Negotiation"));
    }

    @Test(dependsOnMethods = {"createReportTest"})
    public void attributeFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createListValuesFilter("Stage Name", "Interest",
                "Discovery", "Short List", "Negotiation", "Closed Won", "Closed Lost"));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest"})
    public void rankingFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createRankingFilter(ResultSize.TOP.withSize(3),
                "Stage Name", "Amount"));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest"})
    public void rangeFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createRangeFilter("Stage Name", "Amount",
                Range.IS_GREATER_THAN_OR_EQUAL_TO.withNumber(100000)));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createReportTest", "createVariableTest"})
    public void promptFilterTest() throws InterruptedException {
        initReport();
        reportPage.addFilter(FilterItem.Factory.createVariableFilter(VARIABLE_NAME,
                "Interest", "Discovery", "Short List", "Negotiation"));
        reportPage.saveReport();
        checkRedBar(browser);
    }

    private void initReport() {
        initReportsPage();
        reportsPage.getReportsList().openReport(REPORT_NAME);
    }
}
