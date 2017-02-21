package com.gooddata.qa.graphene.reports;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUserProfileUri;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.AttributeFilterItem;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.FloatingTime;
import com.gooddata.qa.graphene.entity.filter.FloatingTime.Time;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.Ranking;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.fragments.reports.filter.AttributeFilterFragment;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.FilterFragment;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.utils.http.RestApiClient;
import com.google.common.collect.Lists;

public class GoodSalesBasicFilterReportTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Basic Filter";
    private static final String VARIABLE_NAME = "FVariable";

    private static final String NO_ATTRIBUTE_VALUE_MESSAGE = "No values selected for filtering.\n"
            + "Select some values or cancel the filter.";

    private List<String> listAttributeValues = Lists
            .newArrayList("Interest", "Discovery", "Short List", "Negotiation");

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-basic-filter-report-test";
    }

    @Test(dependsOnGroups = "createProject")
    public void addNewVariable() {
        initVariablePage().createVariable(new AttributeVariable(VARIABLE_NAME)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(listAttributeValues));
    }

    @Test(dependsOnGroups = "createProject")
    public void addFilterBySpecifyingAttributeValues() {
        AttributeFilterFragment filterFragment = initReport(REPORT_NAME + System.currentTimeMillis())
                .openFilterPanel()
                .clickAddFilter()
                .openAttributeFilterFragment();

        AttributeFilterItem filterItem = FilterItem.Factory.createAttributeFilter(ATTR_STAGE_NAME, "Interest",
                "Discovery", "Short List", "Negotiation", "Closed Won");

        filterFragment.searchAndSelectAttribute(filterItem.getAttribute())
                .selectAllValues();
        assertTrue(filterFragment.areAllValuesSelected(), "All the attribute values are not selected");

        filterFragment.deselectAllValues()
                .apply();
        assertEquals(filterFragment.getErrorMessage(), NO_ATTRIBUTE_VALUE_MESSAGE);

        filterFragment.searchAndSelectAttributeValues(filterItem.getValues())
                .apply();
        reportPage.waitForReportExecutionProgress();
        assertTrue(reportPage.isReportContains(filterItem.getValues()),
                "Attribute filter is not applied successfully");

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = "createProject")
    public void addFilterBySpecifyingFloatingTime() {
        int rangeNumber = getCurrentYear() - 2012;

        initReport(REPORT_NAME + System.currentTimeMillis()).openFilterPanel()
                .clickAddFilter()
                .openAttributeFilterFragment()
                .searchAndSelectAttribute(ATTR_YEAR_SNAPSHOT)
                .selectFloatingTime(Time.THIS_YEAR)
                .apply();
        reportPage.waitForReportExecutionProgress();
        waitForAnalysisPageLoaded(browser);

        reportPage.saveReport();
        checkRedBar(browser);

        String filterName = ATTR_YEAR_SNAPSHOT + " is " + Time.THIS_YEAR;
        assertThat(reportPage.getFilters(), hasItem(filterName));

        reportPage.<AttributeFilterFragment> openExistingFilter(filterName, FilterFragment.ATTRIBUTE_FILTER)
                .selectFloatingTime(new FloatingTime(Time.YEARS_AGO)
                        .withRangeNumber(rangeNumber), new FloatingTime(Time.THIS_YEAR))
                .apply();
        reportPage.waitForReportExecutionProgress();

        filterName = String.format(ATTR_YEAR_SNAPSHOT + " is the last %s years", String.valueOf(rangeNumber + 1));
        assertThat(reportPage.getFilters(), hasItem(filterName));

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = "addNewVariable")
    public void addPromptFilter() throws ParseException, JSONException, IOException {
        String reportName = REPORT_NAME + System.currentTimeMillis();

        initReport(reportName)
                .addFilter(FilterItem.Factory.createPromptFilter(VARIABLE_NAME));
        assertTrue(reportPage.isReportContains(listAttributeValues),
                "Prompt filter is not applied successfully");

        reportPage.saveReport();
        checkRedBar(browser);

        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        listAttributeValues.add("Closed Won");

        initVariablePage()
                .openVariableFromList(VARIABLE_NAME)
                .selectUserSpecificAttributeValues(
                        getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getUser()),
                        listAttributeValues)
                .saveChange();

        initReportsPage().openReport(reportName);
        waitForAnalysisPageLoaded(browser);

        assertTrue(reportPage.isReportContains(listAttributeValues),
                "Prompt filter is not applied successfully");
    }

    @Test(dependsOnGroups = "createProject")
    public void addRankingFilter() {
        initReport(REPORT_NAME + System.currentTimeMillis())
                .addFilter(FilterItem.Factory
                        .createRankingFilter(Ranking.BOTTOM, 2, METRIC_AMOUNT, ATTR_STAGE_NAME));

        String filterName = "Bottom 2 Stage Name by Amount";
        assertThat(reportPage.getFilters(), hasItem(filterName));

        reportPage.saveReport();
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkFilterAppliedInOrder() {
        initReport(REPORT_NAME + System.currentTimeMillis())
                .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_YEAR_SNAPSHOT, "2010"));

        String filterName = "Year (Snapshot) is 2010";
        assertThat(reportPage.getFilters(), hasItem(filterName));

        reportPage.addFilter(FilterItem.Factory
                .createRankingFilter(Ranking.BOTTOM, 3, METRIC_AMOUNT, ATTR_STAGE_NAME));
        assertTrue(reportPage.isRankingFilterApplied(Arrays.asList(494341.51f, 647612.26f, 1185127.28f)),
                "Order of filters are not applied successfully");

        reportPage.saveReport();
        checkRedBar(browser);
    }

    private ReportPage initReport(String reportName) {
        createReport(reportName);

        initReportsPage().openReport(reportName);
        waitForAnalysisPageLoaded(browser);

        return waitForFragmentVisible(reportPage);
    }

    private int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    private void createReport(String reportName) {
        GoodData goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        MetadataService mdService = goodDataClient.getMetadataService();

        Metric amountMetric = mdService.getObj(project, Metric.class, title(METRIC_AMOUNT));
        Attribute stageName = mdService.getObj(project, Attribute.class, title(ATTR_STAGE_NAME));
        Attribute year = mdService.getObj(project, Attribute.class, title(ATTR_YEAR_SNAPSHOT));

        ReportDefinition definition = GridReportDefinitionContent.create(
                reportName,
                singletonList(METRIC_GROUP),
                asList(new AttributeInGrid(stageName.getDefaultDisplayForm().getUri(), stageName.getTitle()),
                        new AttributeInGrid(year.getDefaultDisplayForm().getUri(), year.getTitle())),
                singletonList(new MetricElement(amountMetric)));

        definition = mdService.createObj(project, definition);
        mdService.createObj(project, new Report(definition.getTitle(), definition));
    }
}
