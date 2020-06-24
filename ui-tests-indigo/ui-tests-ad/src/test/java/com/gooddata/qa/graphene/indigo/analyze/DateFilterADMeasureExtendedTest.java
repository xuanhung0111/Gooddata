package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricFilterByDatePicker;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.stream.IntStream;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;

public class DateFilterADMeasureExtendedTest extends AbstractAnalyseTest {

    private static final String ADD_ATTRIBUTE_FILTER_BUTTON = "Add attribute filter";
    private static final String FILTER_BY_DATE_BUTTON = "Filter by date";
    private static final String TEST_INSIGHT = "Test-Insight";
    private static final String RENAMED_TEST_INSIGHT = "Renamed-Test-Insight";
    private static final String RENAMED_TEST_INSIGHT_AGAIN = "Renamed-Test-Insight-Again";
    private static final String INSIGHT_HAS_MEASURE_APPLY_DATE_FILTER =
        "Insight has 1 measure and applies measure date filter";
    private static final String INSIGHT_HAS_MEASURES_APPLY_DATE_FILTER =
        "Insight has 2+ measures and all measures are applied measure date filters";
    private static final String INSIGHT_HAS_MEASURE_WITHOUT_DATE_FILTER =
        "Insight doesn't apply measure date filter";
    private static final String INSIGHT_HAS_SOME_MEASURES_ARE_APPLIED_DATE_FILTER_AND_ANOTHER_IS_NOT =
        "Insight has 2+ measures, some measures are applied measure date filter and some ones aren’t applied";
    private static final String INSIGHT_HAS_ONLY_ATTRIBUTES = "Insight has only attributes";
    //TODO workaround for bug QA-9139 Update Graphene Test for year 2019
    //Will be changed evert year
    private static final String THREE_YEARS_AGO = "three years ago";
    private static final String DATE_FILTER_ALL_TIME = "All time";
    private static final String DATE_FILTER_THIS_MONTH = "This month";
    //TODO workaround for bug QA-9139 Update Graphene Test for year 2019
    //Will be changed evert year

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Filter-AD-Measures-Test-Part2";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();

        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openDateRangePickerWithTwentiethMeasures() {
        assertTrue(initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration().expandFilterByDate().selectStaticPeriod().isDateRangePickerVisible(),
                "Date range picker should be shown clearly");

        initAnalysePage();
        IntStream.range(0, 20).forEach(i -> analysisPage.addMetric(METRIC_AMOUNT));
        assertTrue(analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .expandFilterByDate().selectStaticPeriod().isDateRangePickerVisible(),
                "Date range picker isn't opened");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openCalendarPickerWithFromAndTo() {
        MetricFilterByDatePicker metricFilterByDatePicker = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().expandFilterByDate().selectStaticPeriod();

        assertTrue(metricFilterByDatePicker.isDateRangePickerVisible(), "Date range picker isn't opened");
        assertTrue(metricFilterByDatePicker.openFromDateCalendar().isFromCalendarPickerVisible(),
                "Calendar picker From isn't opened");
        assertTrue(metricFilterByDatePicker.openToDateCalendar().isToCalendarPickerVisible(),
                "Calendar pickers To isn't opened");
        assertFalse(metricFilterByDatePicker.openToDateCalendar().isFromCalendarPickerVisible(),
                "Calendar picker From isn't closed");
        assertFalse(metricFilterByDatePicker.openFromDateCalendar().isToCalendarPickerVisible(),
                "Calendar pickers To isn't closed");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void setDateByCalendarPicker() throws ParseException {
        int dayChosen = 16;
        Calendar cal = Calendar.getInstance();
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        metricConfiguration.expandFilterByDate().selectStaticPeriod();
        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.fillFromDateRange("08/01/2018")
                .openFromDateCalendar();

        String getFromDateChosen = metricFilterByDatePicker.getFromDate();
        cal.setTime(new SimpleDateFormat("MM/dd/yyyy").parse(getFromDateChosen));
        int monthCurrent = cal.get(Calendar.MONTH) + 1;
        int yearCurrent = cal.get(Calendar.YEAR);

        getFromDateChosen = metricFilterByDatePicker.selectDayInCalendar(String.valueOf(dayChosen)).getFromDate();
        cal.setTime(new SimpleDateFormat("MM/dd/yyyy").parse(getFromDateChosen));
        int monthChosenCalendar = cal.get(Calendar.MONTH) + 1;
        int dayChosenCalendar = cal.get(Calendar.DAY_OF_MONTH);
        int yearChosenCalendar = cal.get(Calendar.YEAR);

        List dateCurrent = asList(monthCurrent, dayChosen, yearCurrent);
        List dateChosenExpected = asList(monthChosenCalendar, dayChosenCalendar, yearChosenCalendar);
        assertEquals(dateCurrent, dateChosenExpected);
        assertFalse(metricFilterByDatePicker.isToCalendarPickerVisible(), "Calendar picker isn't opened");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void setToDateLessThanFromDateByCalendarPicker() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.expandFilterByDate().selectStaticPeriod();
        metricConfiguration.fillFromDateRange("1/1/2011");
        metricConfiguration.fillToDateRange("1/1/2010");

        assertEquals(metricFilterByDatePicker.getFromDate(), "01/01/2010");
        assertEquals(metricFilterByDatePicker.getToDate(), "01/01/2010");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testInvalidDateInput() {
        String invalidDate = "1\1\2011";
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.expandFilterByDate().selectStaticPeriod();

        metricConfiguration.fillFromDateRange("");
        metricConfiguration.fillToDateRange("");
        assertFalse(metricFilterByDatePicker.isApplyButtonEnabled(), "Apply button is enabled");

        metricConfiguration.fillFromDateRange(invalidDate);
        metricConfiguration.fillToDateRange(invalidDate);
        assertFalse(metricFilterByDatePicker.isApplyButtonEnabled(), "Apply button is enabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testValidDateInput() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");

        metricConfiguration.removeFilterByDate();
        metricConfiguration.addFilterByDate(DATE_DATASET_CLOSED, "08/16/2010", "08/16/2010");

        assertEquals(getListDataChartReportRender(), singletonList("$174,797.94"),
                "Chart does not render correctly");

        metricConfiguration.removeFilterByDate();
        metricConfiguration.addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testLimitedDateRange() {
        String fromDateOverLimitClosed = "1/1/1899";
        String toDateOverLimitClosed = "1/1/2051";
        String dateBigRange = "1/1/5000";
        final String MESSAGE_SYSTEM = "SORRY, WE CAN'T DISPLAY THIS INSIGHT" +
                "\nTry applying different filters, or using different measures or attributes." +
                "\nIf this did not help, contact your administrator.";

        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, fromDateOverLimitClosed, toDateOverLimitClosed);
        analysisPage.waitForReportComputing();

        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), MESSAGE_SYSTEM);
        metricConfiguration.removeFilterByDate();

        metricConfiguration.addFilterByDate(DATE_DATASET_CLOSED, dateBigRange, dateBigRange);
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getMainEditor().getCanvasMessage(), MESSAGE_SYSTEM);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void selectCancelButtonOnDateRangePicker() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";

        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .tryToAddFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        assertEquals(getListDataChartReportRender(), singletonList("$116,625,456.54"),
                "Chart does not render correctly");

        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.expandFilterByDate().selectStaticPeriod();
        assertNotEquals(metricFilterByDatePicker.getFromDate(), validFromDate);
        assertNotEquals(metricFilterByDatePicker.getToDate(), validToDate);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void backToOtherPeriodsTest() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";

        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.expandFilterByDate();
        metricFilterByDatePicker.backToOtherPeriods();

        assertFalse(metricFilterByDatePicker.isDateRangePickerVisible(), "System don't close date range picker");
        assertTrue(metricConfiguration.isFilterByDateExpanded(),
                "System should come back to date filter dropdown");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeDateDimensionAfterApplyingStaticPeriod() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.expandFilterByDate();
        metricFilterByDatePicker.backToOtherPeriods().changeDateDimension(DATE_DATASET_SNAPSHOT);
        assertEquals(getListDataChartReportRender(), singletonList("$116,625,456.54"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoButtonAfterAddingDateFilterMeasure() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT);

        assertEquals(getListDataChartReportRender(), singletonList("$116,625,456.54"),
                "Chart does not render correctly");
        metricConfiguration.expandConfiguration().addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");
        analysisPage.undo();

        assertEquals(getListDataChartReportRender(), singletonList("$116,625,456.54"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoButtonAfterChangingDateDimension() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        MetricFilterByDatePicker metricFilterByDatePicker = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate)
                .expandFilterByDate();
        metricFilterByDatePicker.backToOtherPeriods().changeDateDimension(DATE_DATASET_CREATED);
        assertEquals(getListDataChartReportRender(), singletonList("$76,055,152.30"),
                "Chart does not render correctly");

        analysisPage.undo();
        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoButtonAfterChangingFloatingPeriod() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);
        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");

        MetricFilterByDatePicker metricFilterByDatePicker = metricConfiguration.expandFilterByDate();
        metricFilterByDatePicker.backToOtherPeriods();
        metricConfiguration.addFilterByDate(DATE_DATASET_CLOSED, THREE_YEARS_AGO);
        assertEquals(getListDataChartReportRender(), singletonList("$3,644.00"),
                "Chart does not render correctly");

        analysisPage.undo();
        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoButtonAfterDeletingDateFilter() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");

        metricConfiguration.removeFilterByDate();
        analysisPage.undo();
        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void undoButtonAfterReAddingDateFilter() {
        String validFromDate = "01/01/2011";
        String validToDate = "01/01/2018";
        initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate)
                .removeFilterByDate().expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, validFromDate, validToDate);

        assertEquals(getListDataChartReportRender(), singletonList("$97,685,666.02"),
                "Chart does not render correctly");

        analysisPage.undo();
        assertEquals(getListDataChartReportRender(), singletonList("$116,625,456.54"),
                "Chart does not render correctly");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveAndSaveAsWithMeasureAndStackBy() {
        initAnalysePage().addMetric(METRIC_AMOUNT).addDate().addStack(ATTR_DEPARTMENT).waitForReportComputing()
                .saveInsight(TEST_INSIGHT);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();
        metricConfiguration.expandFilterByDate();
        assertEquals(metricConfiguration.getByDateAndAttributeFilterButton(),
                asList(FILTER_BY_DATE_BUTTON, ADD_ATTRIBUTE_FILTER_BUTTON));

        metricConfiguration.addFilterByDate(DateRange.LAST_YEAR.toString());
        analysisPage.saveInsight();
        assertEquals(metricConfiguration.expandConfiguration().getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertThat(analysisPage.getAttributesBucket().getItemNames(), contains(DATE));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);

        //Do not edit anymore, press Save as button
        analysisPage.saveInsightAs(RENAMED_TEST_INSIGHT);
        assertEquals(metricConfiguration.expandConfiguration().getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertThat(analysisPage.getAttributesBucket().getItemNames(), contains(DATE));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);

        //Switch between kind of insights, press Save as button
        analysisPage.openInsight(TEST_INSIGHT).openInsight(RENAMED_TEST_INSIGHT).saveInsightAs(RENAMED_TEST_INSIGHT_AGAIN);
        assertEquals(metricConfiguration.expandConfiguration().getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertThat(analysisPage.getAttributesBucket().getItemNames(), contains(DATE));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveAndSaveAsWithTwoMeasuresAndAttributes() {
        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AMOUNT).addDate()
                .waitForReportComputing().saveInsight(TEST_INSIGHT);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();
        metricConfiguration.expandFilterByDate();
        assertEquals(metricConfiguration.getByDateAndAttributeFilterButton(),
                asList(FILTER_BY_DATE_BUTTON, ADD_ATTRIBUTE_FILTER_BUTTON));

        metricConfiguration.addFilterByDate(DateRange.LAST_YEAR.toString());
        analysisPage.saveInsight();
        assertEquals(metricConfiguration.getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertThat(analysisPage.getAttributesBucket().getItemNames(), contains(DATE));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AMOUNT));

        analysisPage.resetToBlankState().addMetric(METRIC_AMOUNT).saveInsight(RENAMED_TEST_INSIGHT);
        assertEquals(analysisPage.getPageHeader().getInsightTitle(), RENAMED_TEST_INSIGHT);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSaveAndSaveAsButtonWithAttributeOnFilter() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).addFilter(ATTR_DEPARTMENT)
                .waitForReportComputing().saveInsight(TEST_INSIGHT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        assertEquals(metricConfiguration.getByDateAndAttributeFilterButton(),
                asList(FILTER_BY_DATE_BUTTON, ADD_ATTRIBUTE_FILTER_BUTTON));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText(ATTR_DEPARTMENT)),
                asList(ATTR_DEPARTMENT, "All"));

        metricConfiguration.addFilterByDate(DateRange.LAST_YEAR.toString());
        analysisPage.saveInsight();
        assertEquals(metricConfiguration.getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText(ATTR_DEPARTMENT)),
                asList(ATTR_DEPARTMENT, "All"));
        analysisPage.addMetric(METRIC_AMOUNT).waitForReportComputing().saveInsightAs(RENAMED_TEST_INSIGHT);
        assertEquals(metricConfiguration.getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT, METRIC_AMOUNT));
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText(ATTR_DEPARTMENT)),
                asList(ATTR_DEPARTMENT, "All"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSaveAndSaveAsButtonWithAttributeFiltersToMeasures() {
        initAnalysePage().addMetric(METRIC_AMOUNT).getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().addFilterWithAllValue(ATTR_ACCOUNT);

        analysisPage.waitForReportComputing().saveInsight(TEST_INSIGHT);
        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT)
                .expandConfiguration();
        assertEquals(metricConfiguration.getByDateAndAttributeFilterButton(),
                asList(FILTER_BY_DATE_BUTTON, ATTR_ACCOUNT + ":\nAll", ADD_ATTRIBUTE_FILTER_BUTTON));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));

        metricConfiguration.addFilterByDate(DateRange.LAST_YEAR.toString());
        analysisPage.saveInsight();
        assertEquals(metricConfiguration.getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
        assertEquals(metricConfiguration.getFilterText(), ATTR_ACCOUNT + ": All");

        metricConfiguration.removeFilterByDate();
        analysisPage.waitForReportComputing().saveInsightAs(RENAMED_TEST_INSIGHT);
        assertEquals(metricConfiguration.getByDateAndAttributeFilterButton(),
                asList(FILTER_BY_DATE_BUTTON, ATTR_ACCOUNT + ":\nAll", ADD_ATTRIBUTE_FILTER_BUTTON));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSaveAndSaveAsButtonWithShowInPercent() {
        MetricConfiguration metricConfiguration = initAnalysePage().addMetric(METRIC_AMOUNT).addDate().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().showPercents();
        analysisPage.waitForReportComputing().saveInsight(TEST_INSIGHT);
        assertEquals(metricConfiguration.getByDateAndAttributeFilterButton(),
                asList(FILTER_BY_DATE_BUTTON, ADD_ATTRIBUTE_FILTER_BUTTON));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList("% " + METRIC_AMOUNT));

        metricConfiguration.addFilterByDate(DateRange.LAST_YEAR.toString());
        analysisPage.saveInsight();
        assertEquals(metricConfiguration.getFilterByDate(),
                DATE_DATASET_CLOSED + ": " + DateRange.LAST_YEAR.toString());
        assertThat(analysisPage.getAttributesBucket().getItemNames(), contains(DATE));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList("% " + METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareInsightsApplyDateFilter() {
        initAnalysePage().addMetric(METRIC_AMOUNT).waitForReportComputing().getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().addFilterByDate(THREE_YEARS_AGO);
        analysisPage.saveInsight(INSIGHT_HAS_MEASURE_APPLY_DATE_FILTER);

        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilterByDate(THREE_YEARS_AGO);
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AVG_AMOUNT)
            .expandConfiguration().addFilterByDate(THREE_YEARS_AGO);
        analysisPage.saveInsight(INSIGHT_HAS_MEASURES_APPLY_DATE_FILTER);
    }

    @DataProvider(name = "insightApplyDateFilter")
    public Object[][] getSavedInsightDateFilter() {
        return new Object[][]{
            {INSIGHT_HAS_MEASURE_APPLY_DATE_FILTER},
            {INSIGHT_HAS_MEASURES_APPLY_DATE_FILTER},
        };
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareInsightsNotApplyDateFilter() {
        initAnalysePage().addMetric(METRIC_AMOUNT).waitForReportComputing()
            .saveInsight(INSIGHT_HAS_MEASURE_WITHOUT_DATE_FILTER);

        initAnalysePage().addMetric(METRIC_AMOUNT).addMetric(METRIC_AVG_AMOUNT).getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilterByDate(DateRange.LAST_YEAR.toString());
        analysisPage.saveInsight(INSIGHT_HAS_SOME_MEASURES_ARE_APPLIED_DATE_FILTER_AND_ANOTHER_IS_NOT);

        initAnalysePage().addMetric(ATTR_ACCOUNT, FieldType.ATTRIBUTE).saveInsight(INSIGHT_HAS_ONLY_ATTRIBUTES);
    }

    @Test(dependsOnMethods = {"prepareInsightsApplyDateFilter"}, dataProvider = "insightApplyDateFilter")
    public void testKDDateFilterCheckBoxWithMeasureDateFilter(String insight) {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().addInsight(insight).waitForWidgetsLoading();

        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_THIS_MONTH)
            .selectWidgetByHeadline(Insight.class, insight);

        assertFalse(panel.isDateFilterCheckboxChecked(), "Date checkbox on right panel is checked");
        assertFalse(panel.isDateFilterCheckboxEnabled(),
            "Date checkbox on right panel isn't disabled, changeable");

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_ALL_TIME).selectWidgetByHeadline(Insight.class, insight);
        assertFalse(panel.isDateFilterCheckboxChecked(), "Date checkbox on right panel is checked");
        assertFalse(panel.isDateFilterCheckboxEnabled(),
            "Date checkbox on right panel isn't disabled, changeable");

        indigoDashboardsPage.saveEditModeWithWidgets();
        waitForOpeningIndigoDashboard();
        checkRedBar(browser);
    }

    @DataProvider
    public Object[][] getSavedInsightNotDateFilter() {
        return new Object[][]{
            {INSIGHT_HAS_MEASURE_WITHOUT_DATE_FILTER},
            {INSIGHT_HAS_SOME_MEASURES_ARE_APPLIED_DATE_FILTER_AND_ANOTHER_IS_NOT},
            {INSIGHT_HAS_ONLY_ATTRIBUTES},
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "getSavedInsightNotDateFilter")
    public void testKDDateFilterCheckBoxNotApplyingMeasureDateFilter(String insight) {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().addInsight(insight).waitForWidgetsLoading();

        ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_THIS_MONTH).selectWidgetByHeadline(Insight.class, insight);

        assertTrue(panel.isDateFilterCheckboxChecked(), "Date checkbox on right panel isn't checked");
        assertTrue(panel.isDateFilterCheckboxEnabled(),
            "Date checkbox on right panel is disabled, unchangeable");

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_ALL_TIME).selectWidgetByHeadline(Insight.class, insight);
        assertTrue(panel.isDateFilterCheckboxChecked(), "Date checkbox on right panel isn't checked");
        assertTrue(panel.isDateFilterCheckboxEnabled(),
            "Date checkbox on right panel is disabled, unchangeable");

        indigoDashboardsPage.saveEditModeWithWidgets();
        waitForOpeningIndigoDashboard();
        checkRedBar(browser);
    }

    private List<String> getListDataChartReportRender() {
        return analysisPage.waitForReportComputing().getChartReport().getDataLabels();
    }
}
