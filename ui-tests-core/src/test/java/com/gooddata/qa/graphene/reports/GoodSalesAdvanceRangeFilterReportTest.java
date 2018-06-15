package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.FloatingTime.Time;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem.RangeType;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.fragments.reports.filter.RangeFilterFragment;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.FilterFragment;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

public class GoodSalesAdvanceRangeFilterReportTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Advance-numeric-range-filter";
    private static final String VARIABLE_NAME = "FVariable";

    private static final String RANGE_FILTER_DESCRIPTION = "Opportunity where Amount is greater than or "
            + "equal to 19000";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Advance-numeric-range-filter-report-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        createReport(new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats(METRIC_AMOUNT)
                .withHows(ATTR_OPPORTUNITY)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP)),
                "Advance-numeric-range-filter-report");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addNewVariable() {
        initVariablePage().createVariable(new AttributeVariable(VARIABLE_NAME)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues("Interest", "Discovery", "Short List", "Negotiation"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addRangeFilter() {
        initReport().addFilter(FilterItem.Factory.
                createRangeFilter(RangeType.IS_GREATER_THAN_OR_EQUAL_TO, 19000, METRIC_AMOUNT, ATTR_OPPORTUNITY));
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        assertThat(reportPage.getFilters(), hasItem(RANGE_FILTER_DESCRIPTION));
    }

    @Test(dependsOnMethods = "addRangeFilter")
    public void addSubFilterByAttributeValues() {
        initReport()
                .<RangeFilterFragment> openExistingFilter(RANGE_FILTER_DESCRIPTION, FilterFragment.RANGE_FILTER)
                .addSubFilterByAttributeValues(ATTR_STAGE_NAME, "Interest", "Discovery", "Short List")
                .apply();
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        String filterDescription = RANGE_FILTER_DESCRIPTION + " and Stage Name is Interest, Discovery, Short List";
        assertThat(reportPage.getFilters(), hasItem(filterDescription));

        reportPage.<RangeFilterFragment> openExistingFilter(filterDescription, FilterFragment.RANGE_FILTER)
                .deleteLatestSubFilter()
                .apply();
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        assertThat(reportPage.getFilters(), hasItem(RANGE_FILTER_DESCRIPTION));
    }

    @Test(dependsOnMethods = "addSubFilterByAttributeValues")
    public void addSubFilterByDateRange() {
        initReport()
                .<RangeFilterFragment> openExistingFilter(RANGE_FILTER_DESCRIPTION, FilterFragment.RANGE_FILTER)
                .addSubFilterByDateRange(ATTR_YEAR_SNAPSHOT, Time.LAST_YEAR)
                .apply();
        reportPage.getTableReport().waitForLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        String filterDescription = RANGE_FILTER_DESCRIPTION + " and Year (Snapshot) is last year";
        assertThat(reportPage.getFilters(), hasItem(filterDescription));

        reportPage.<RangeFilterFragment> openExistingFilter(filterDescription, FilterFragment.RANGE_FILTER)
                .changeLatestSubFilterOperator("isn't")
                .apply();
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        filterDescription = RANGE_FILTER_DESCRIPTION + " and Year (Snapshot) isn't last year";
        assertThat(reportPage.getFilters(), hasItem(filterDescription));
    }

    @Test(dependsOnMethods = "addSubFilterByDateRange")
    public void addSubFilterByMultipleAttributes() {
        final String rangeFilterDescription = "Opportunity where Amount is greater than or equal to 19000 " +
                "and Year (Snapshot) isn't last year";
        initReport()
                .<RangeFilterFragment> openExistingFilter(rangeFilterDescription, FilterFragment.RANGE_FILTER)
                .addSubFilterByAttributeValues(ATTR_STAGE_NAME, "Interest", "Discovery", "Short List")
                .apply();
        waitForReportLoaded();

        Screenshots.takeScreenshot(browser, "add_sub_filter_by_multiple_attributes", getClass());
        final String filterDescription = "Opportunity where Amount is greater than or equal to 19000 " +
                "and Year (Snapshot) isn't last year, Stage Name is Interest, Discovery, Short List";
        assertThat(reportPage.getFilters(), hasItem(filterDescription));

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.<RangeFilterFragment> openExistingFilter(filterDescription, FilterFragment.RANGE_FILTER)
                .deleteLatestSubFilter()
                .apply();
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        assertThat(reportPage.getFilters(), hasItem(rangeFilterDescription));
    }

    @Test(dependsOnMethods = {"addNewVariable", "addSubFilterByMultipleAttributes"})
    public void addSubFilterByVariable() {
        String filterDescription = RANGE_FILTER_DESCRIPTION + " and Year (Snapshot) isn't last year";

        initReport()
                .<RangeFilterFragment> openExistingFilter(filterDescription, FilterFragment.RANGE_FILTER)
                .addSubFilterByVariable(VARIABLE_NAME)
                .apply();
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        filterDescription += ", %s variable";
        assertThat(reportPage.getFilters(), hasItem(String.format(filterDescription, VARIABLE_NAME)));
    }

    private ReportPage initReport() {
        initReportsPage().openReport(REPORT_NAME);
        waitForAnalysisPageLoaded(browser);

        return waitForFragmentVisible(reportPage);
    }

    private void waitForReportLoaded() {
        reportPage.getTableReport().waitForLoaded();
    }
}
