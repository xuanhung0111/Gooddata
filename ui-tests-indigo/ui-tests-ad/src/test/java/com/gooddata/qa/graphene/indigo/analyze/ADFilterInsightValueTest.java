package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.ExportXLSXDialog;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration.OperatorCalculated;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.OptionalStacking;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FilterBarPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.CSVUtils;
import com.gooddata.qa.utils.XlsxUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.AD_MEASURE_VALUE_FILTER_NULL_AS_ZERO_OPTION;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
    .LogicalOperator.GREATER_THAN;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
    .LogicalOperator.LESS_THAN;
import static com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel
    .LogicalOperator.EQUAL_TO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SUM_OF_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.SP_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_CLOSE_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_EOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForExporting;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class ADFilterInsightValueTest extends AbstractAnalyseTest {

    private final String METRIC_AMOUNT_PERCENT = "metricAmountPercent";
    private final String METRIC_CLOSE_EOP_PERCENT = "metricCloseEOPPercent";
    private final String TIME_RANGE_FROM = "01/01/2000";
    private final String TIME_RANGE_TO = "01/01/2020";
    private final String INSIGHT_TEST_REMOVING = "Removing";
    private final String ADVANCED_INSIGHT = "Advanced Insight";
    private static final String CALCULATED_RATIO_OF = "Ratio of …";
    private static final String FOUR_DATE_DATA = "4dates";
    private static final String DISABLED = "Disabled";
    private static final String ENABLED_CHECKED_BY_DEFAULT = "EnabledCheckedByDefault";
    private static final String ENABLED_UNCHECKED_BY_DEFAULT = "EnabledUncheckedByDefault";
    private static final String FOUR_DATE_CSV_PATH = "/" + UPLOAD_CSV + "/4dates.csv";
    private static final String FACT_NUMBER = "Number";
    private static final String SUM_OF_FACT_NUMBER = "Sum of Number";
    private static final String ATTR_NAME = "Name";
    private static String TREAT_NULL_VALUES_AS_ZERO_INSIGHT = "TreatNull";

    private ProjectRestRequest projectRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private FactRestRequest factRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Updated AD filter Insight Value";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createTimelineEOPMetric();
        metrics.createCloseEOPMetric();
        metrics.createSnapshotEOPMetric();
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        // TODO: BB-1675 enableNewADFilterBar FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_NEW_AD_FILTER_BAR, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_MEASURE_VALUE_FILTERS, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addMeasureValueFilterViaFilterBarDropdown() throws NoSuchFieldException {
        prepareMetricTypes();
        addMeasureIntoMeasureBucket();
        cannotAddMeasureIntoFilterBar();
        addMeasureValueFilterFromSimpleMeasure();
        addMeasureValueFilterFromPercentageMeasure();
    }

    public void prepareMetricTypes() throws NoSuchFieldException {
        createMetric(METRIC_AMOUNT_PERCENT, format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))),
            DEFAULT_METRIC_FORMAT + "%");

        initAnalysePage().addMetricToRecommendedStepsPanelOnCanvas(METRIC_AMOUNT_PERCENT);

        analysisPage.addMetric(METRIC_AMOUNT).addMetric(FACT_AMOUNT, FieldType.FACT)
            .addDateFilter().addAttribute(ATTR_DEPARTMENT);

        createDerivedMeasure(METRIC_AMOUNT);

        createCalculatedMeasure(Pair.of(METRIC_AMOUNT, 3), Pair.of(METRIC_SUM_OF_AMOUNT, 4), OperatorCalculated.SUM);
        analysisPage.getStacksBucket().checkOption(OptionalStacking.MEASURES);

        MetricConfiguration metricConfigurationAmount = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration();
        metricConfigurationAmount.addFilter(ATTR_DEPARTMENT, "Direct Sales");
        analysisPage.waitForReportComputing();
        analysisPage.addMetric(ATTR_DEPARTMENT, FieldType.ATTRIBUTE);
    }

    public void addMeasureIntoMeasureBucket() {
        FilterBarPicker filterBarPicker = analysisPage.openFilterBarPicker();
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT, 3), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT + SP_YEAR_AGO, 2),
            "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_SUM_OF_AMOUNT, 4), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_SUM_OF_AMOUNT + " and " + METRIC_SUM_OF_AMOUNT, 5),
            "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked(METRIC_AMOUNT_PERCENT, 1), "Default state should be unticked checkbox.");
        assertFalse(filterBarPicker.isItemChecked("Count of " + ATTR_DEPARTMENT, 6),
            "Default state should be unticked checkbox.");

        filterBarPicker.checkItem(METRIC_AMOUNT_PERCENT, 1).apply();
        assertThat(analysisPage.getFilterBuckets()
            .getFilterText(METRIC_AMOUNT_PERCENT + " (M1)"), containsString("All"));

        analysisPage.openFilterBarPicker().checkItem("Count of " + ATTR_DEPARTMENT, 6).apply();
        assertThat(analysisPage.getFilterBuckets()
            .getFilterText("Count of " + ATTR_DEPARTMENT + " (M6)"), containsString("All"));

        assertThat(analysisPage.getChartReport().getTotalsStackedColumn(),
            hasItems("1,202,226,735,706%", "507,269,274,598%"));
        analysisPage.clear();
    }

    public void cannotAddMeasureIntoFilterBar() {
        WebElement metric = analysisPage.getCatalogPanel()
            .searchAndGet(METRIC_AMOUNT, FieldType.METRIC);

        analysisPage.tryToDrag(metric, analysisPage.getFilterBuckets().getInvitation());
        assertFalse(analysisPage.getFilterBuckets().isFilterVisible(METRIC_AMOUNT));
    }

    public void addMeasureValueFilterFromSimpleMeasure() throws NoSuchFieldException {
        analysisPage.addMetric(METRIC_AMOUNT).addMetric(METRIC_AMOUNT, FieldType.FACT).addDateFilter()
            .addAttribute(ATTR_DEPARTMENT);

        createDerivedMeasure(METRIC_AMOUNT);

        createCalculatedMeasure(Pair.of(METRIC_AMOUNT, 2), Pair.of(METRIC_SUM_OF_AMOUNT, 3), OperatorCalculated.SUM);
        createDerivedMeasure("Sum of " + METRIC_AMOUNT + " and " + METRIC_SUM_OF_AMOUNT);

        analysisPage.openFilterBarPicker().checkItem(METRIC_AMOUNT, 1).apply();
        analysisPage.openMeasureFilterPanel(METRIC_AMOUNT, 1)
            .addMeasureValueFilter(EQUAL_TO, "80,406,324.96");

        analysisPage.waitForReportComputing();
        takeScreenshot(browser, "Simple-Measure", getClass());

        assertEquals(analysisPage.getChartReport().getDataLabels(),
            asList("$80,406,324.96", "3,927,016,127.06", "4,007,422,452.02", "4,007,422,452.02"));

        List<ReportType> reportsSupported =
            asList(ReportType.TABLE, ReportType.LINE_CHART, ReportType.BAR_CHART, ReportType.COMBO_CHART);

        for (ReportType reportType : reportsSupported) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertThat(analysisPage.getMetricsBucket().getItemNames(), hasItems(METRIC_AMOUNT,
                METRIC_SUM_OF_AMOUNT, "Sum of Amount and Sum of Amount" + SP_YEAR_AGO,
                "Sum of Amount and Sum of Amount"));
        }

        List<ReportType> reportsUnSupported = asList(ReportType.PIE_CHART, ReportType.DONUT_CHART, ReportType.HEAT_MAP);
        for (ReportType reportType : reportsUnSupported) {
            analysisPage.changeReportType(reportType).waitForReportComputing();
            assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_AMOUNT));
        }
        analysisPage.clear();
    }

    public void addMeasureValueFilterFromPercentageMeasure() throws NoSuchFieldException {
        createMetric(METRIC_CLOSE_EOP_PERCENT, format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Metric.class, title(METRIC_CLOSE_EOP))),
            DEFAULT_METRIC_FORMAT + "%");

        String percentageInsight = "PERCENTAGE MEASURE";
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(percentageInsight, ReportType.COLUMN_CHART)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_PERCENT)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_CLOSE_EOP_PERCENT))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(percentageInsight);

        createCalculatedMeasure(
            Pair.of(METRIC_AMOUNT_PERCENT, 1), Pair.of(METRIC_CLOSE_EOP_PERCENT, 2), OperatorCalculated.SUM);
        createCalculatedMeasure(
            Pair.of(METRIC_AMOUNT_PERCENT, 1), Pair.of(METRIC_CLOSE_EOP_PERCENT, 2), OperatorCalculated.CHANGE);

        analysisPage.addDateFilter().waitForReportComputing();
        createDerivedMeasure("Change from " + METRIC_CLOSE_EOP_PERCENT + " to " + METRIC_AMOUNT_PERCENT);

        analysisPage.openFilterBarPicker().checkItem(METRIC_AMOUNT_PERCENT, 1).apply();
        takeScreenshot(browser, "Percentage-Measure", getClass());

        analysisPage.openMeasureFilterPanel(METRIC_AMOUNT_PERCENT, 1)
            .addMeasureValueFilter(GREATER_THAN, "170,000,000,000");

        analysisPage.saveInsight().waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getDataLabels(),
            asList("392,701,612,706%", "4,261,300%", "3,927,058,740.06", "9,215,435.46%", "9,215,435.46%"));
        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();
        assertEquals(analysisPage.getPivotTableReport().getValueMeasuresPresent(),
            asList("392,701,612,706%", "4,261,300%", "3,927,058,740.06", "9,215,435.46%", "9,215,435.46%"));

        analysisPage.clear();
        analysisPage.addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT);
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_AMOUNT).expandConfiguration().showPercents();

        analysisPage.openFilterBarPicker().checkItem("% " + METRIC_AMOUNT, 1).apply();
        assertEquals(ElementUtils.getTooltipFromElement(
            analysisPage.getFilterBuckets().getFilter("% " + METRIC_AMOUNT, 1), browser),
            "The filter uses actual measure values, not percentages.");

        analysisPage.getFilterBuckets().getFilter("% " + METRIC_AMOUNT, 1).click();
        MeasureValueFilterPanel measureValueFilterPanel = MeasureValueFilterPanel.getInstance(browser);

        assertEquals(MeasureValueFilterPanel.getInstance(browser).getWarningMessage(),
            "The filter uses actual measure values, not percentages.");

        measureValueFilterPanel.addMeasureValueFilter(GREATER_THAN, "4,000,000,000");

        assertEquals(chartReport.getDataLabels(), asList("100.00%"));
        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();

        assertEquals(chartReport.getDataLabels(), asList("$80,406,324.96"));

        analysisPage.clear();
        analysisPage.addMetric(METRIC_CLOSE_EOP).addAttribute(ATTR_DEPARTMENT).addMetric(METRIC_SNAPSHOT_EOP)
            .getStacksBucket().checkOption(OptionalStacking.MEASURES).checkOption(OptionalStacking.PERCENT);

        analysisPage.openFilterBarPicker().checkItem(METRIC_CLOSE_EOP, 1).apply();
        analysisPage.openMeasureFilterPanel(METRIC_CLOSE_EOP, 1)
            .addMeasureValueFilter(EQUAL_TO, "42,613");

        assertEquals(chartReport.getDataLabels(), asList("50.93%", "49.07%"));

        analysisPage.changeReportType(ReportType.HEAT_MAP).waitForReportComputing();
        assertEquals(chartReport.getDataLabels(), asList("42,613.00"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addMeasureValueFilterWithAdvancedFunction() throws IOException {
        saveAndReopenInsightMUFAndRestrictedFact();
        saveAndReopenInsightCalculatedMeasure();
    }

    public void saveAndReopenInsightMUFAndRestrictedFact() throws IOException{
        final String productValues = format("[%s]",
            getMdService().getAttributeElements(getAttributeByTitle(ATTR_DEPARTMENT)).get(1).getUri());
        final String expression = format("[%s] IN (%s)", getAttributeByTitle(ATTR_DEPARTMENT).getUri(), productValues);
        DashboardRestRequest dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams
            .getProjectId());
        final String mufUri = dashboardRestRequest.createMufObjectByUri("muf", expression);
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
            new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        String assignedMufUserId = userManagementRestRequest
            .getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());
        dashboardRestRequest.addMufToUser(assignedMufUserId, mufUri);

        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        factRestRequest.setFactRestricted(factUri);

        String insight = "Insight" + generateHashString();
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT, FieldType.FACT)
            .addMetric(METRIC_AMOUNT, FieldType.FACT).addAttribute(ATTR_DEPARTMENT).getPivotTableReport()
            .addTotal(AggregationItem.SUM, METRIC_SUM_OF_AMOUNT, 0);

        analysisPage.openFilterBarPicker().checkItem(METRIC_SUM_OF_AMOUNT, 1).apply();
        analysisPage.openMeasureFilterPanel(METRIC_SUM_OF_AMOUNT, 1)
            .addMeasureValueFilter(GREATER_THAN, "1,700,000,000");
        analysisPage.saveInsight(insight).waitForReportComputing();

        addUsersWithOtherRolesToProject();
        logoutAndLoginAs(true, UserRoles.EDITOR);

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(insight).waitForReportComputing()
            .getPivotTableReport();

        assertEquals(pivotTableReport.getGrandTotalsContent(), asList(asList("Sum", "3,927,016,127.06", "")));

        analysisPage.saveInsightAs(ADVANCED_INSIGHT).openInsight(ADVANCED_INSIGHT).waitForReportComputing();
        assertEquals(pivotTableReport.getGrandTotalsContent(), asList(asList("Sum", "3,927,016,127.06", "")));
    }

    public void saveAndReopenInsightCalculatedMeasure() {
        analysisPage.changeReportType(ReportType.COLUMN_CHART).waitForReportComputing();
        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("3,927,016,127.06", "3,927,016,127.06"));

        createCalculatedMeasure(Pair.of(METRIC_SUM_OF_AMOUNT, 1), Pair.of(METRIC_SUM_OF_AMOUNT, 2),
            OperatorCalculated.SUM);
        analysisPage.saveInsightAs(ADVANCED_INSIGHT).waitForReportComputing();

        assertEquals(chartReport.getDataLabels(), asList("3,927,016,127.06", "3,927,016,127.06", "7,854,032,254.12"));

        initAnalysePage().addMetric(METRIC_AMOUNT, FieldType.FACT).addAttribute(ATTR_ACCOUNT).waitForReportComputing();
        assertEquals(analysisPage.getExplorerMessage(), "TOO MANY DATA POINTS TO DISPLAY");

        analysisPage.changeReportType(ReportType.TABLE).waitForReportComputing();

        analysisPage.openFilterBarPicker().checkItem(METRIC_SUM_OF_AMOUNT, 1).apply();
        analysisPage.openMeasureFilterPanel(METRIC_SUM_OF_AMOUNT, 1)
            .addMeasureValueFilter(EQUAL_TO, "1,872,000.00");
        analysisPage.saveInsight(ADVANCED_INSIGHT).waitForReportComputing();

        assertEquals(analysisPage.getPivotTableReport()
            .getValueMeasuresPresent(), asList("1,872,000.00", "1,872,000.00"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeOrHiddenMeasureValue() throws NoSuchFieldException{
        removeFromFilterBarOrUncheckMeasure();
        removeMasterMeasureHasDerivedAndCalculated();
        hiddenMeasureOrAttributeWhenSwitchingInsight();
    }

    public void removeFromFilterBarOrUncheckMeasure() {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(INSIGHT_TEST_REMOVING, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket
                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(singletonList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.ATTRIBUTE))));

        initAnalysePage().openInsight(INSIGHT_TEST_REMOVING).waitForReportComputing();

        analysisPage.openFilterBarPicker().checkItem(METRIC_AMOUNT, 1).apply();
        analysisPage.openMeasureFilterPanel(METRIC_AMOUNT, 1)
            .addMeasureValueFilter(GREATER_THAN, "50,000,000");
        analysisPage.saveInsight().waitForReportComputing();

        initAnalysePage().openInsight(INSIGHT_TEST_REMOVING).removeMeasureFilter(METRIC_AMOUNT, 1)
            .waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("$48,932,639.59", "$67,692,816.95"));

        analysisPage.undo();

        analysisPage.openFilterBarPicker().uncheckItem(METRIC_AMOUNT, 1).apply();
        analysisPage.waitForReportComputing();
        assertEquals(chartReport.getDataLabels(), asList("$48,932,639.59", "$67,692,816.95"));
    }

    public void removeMasterMeasureHasDerivedAndCalculated() throws NoSuchFieldException{
        analysisPage.clear();
        analysisPage.addMetric(METRIC_CLOSE_EOP).addMetric(METRIC_AMOUNT).addAttribute(ATTR_FORECAST_CATEGORY)
            .addDateFilter();
        createDerivedMeasure(METRIC_AMOUNT);

        analysisPage.openFilterBarPicker().checkItem(METRIC_AMOUNT + SP_YEAR_AGO, 2).apply();
        analysisPage.openMeasureFilterPanel(METRIC_AMOUNT + SP_YEAR_AGO, 2)
            .addMeasureValueFilter(GREATER_THAN, "50,000,000");
        analysisPage.removeMetric(METRIC_AMOUNT).waitForReportComputing();

        ChartReport chartReport = analysisPage.getChartReport();
        assertEquals(chartReport.getDataLabels(), asList("42,794.00", "41,990.00"));

        analysisPage.addMetric(METRIC_TIMELINE_EOP);
        createCalculatedMeasure(Pair.of(METRIC_CLOSE_EOP, 1), Pair.of(METRIC_TIMELINE_EOP, 2), OperatorCalculated.SUM);
        analysisPage.openFilterBarPicker()
            .checkItem("Sum of " + METRIC_CLOSE_EOP + " and " + METRIC_TIMELINE_EOP, 3).apply();
        analysisPage.openMeasureFilterPanel("Sum of _Close …ine [EOP]", 3)
            .addMeasureValueFilter(EQUAL_TO, "86,185");
        analysisPage.removeMetric(METRIC_CLOSE_EOP).waitForReportComputing();

        assertEquals(chartReport.getDataLabels(), asList("44,195", "44,195"));
    }

    public void hiddenMeasureOrAttributeWhenSwitchingInsight() throws NoSuchFieldException{
        analysisPage.changeReportType(ReportType.HEAD_LINE).waitForReportComputing();
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        assertEquals(filtersBucket.getFiltersCount(), 1);

        analysisPage.clear();
        analysisPage.addMetric(METRIC_CLOSE_EOP).addMetric(METRIC_TIMELINE_EOP).addMetric(METRIC_AMOUNT)
            .addAttribute(ATTR_FORECAST_CATEGORY).waitForReportComputing();
        analysisPage.openFilterBarPicker().checkItem(METRIC_CLOSE_EOP, 1)
            .checkItem(METRIC_TIMELINE_EOP, 2).checkItem(METRIC_AMOUNT, 3).apply();
        analysisPage.changeReportType(ReportType.SCATTER_PLOT).waitForReportComputing();
        assertFalse(filtersBucket.isFilterVisible(METRIC_AMOUNT + " (M3)"));

        analysisPage.clear();
        analysisPage.addMetric(METRIC_CLOSE_EOP).addMetric(METRIC_TIMELINE_EOP).addAttribute(ATTR_FORECAST_CATEGORY)
            .addDateFilter();
        createDerivedMeasure(METRIC_CLOSE_EOP);
        analysisPage.openFilterBarPicker().checkItem(METRIC_CLOSE_EOP + SP_YEAR_AGO, 1).apply();
        analysisPage.openMeasureFilterPanel(METRIC_CLOSE_EOP + SP_YEAR_AGO, 1)
            .addMeasureValueFilter(EQUAL_TO, "41,990");

        analysisPage.changeReportType(ReportType.PIE_CHART).waitForReportComputing();

        assertFalse(filtersBucket.isFilterVisible(METRIC_CLOSE_EOP + SP_YEAR_AGO + " (M1)"));
        assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(METRIC_CLOSE_EOP));
    }

    private void createDerivedMeasure(String metric) throws NoSuchFieldException{
        FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        DateFilterPickerPanel dateFilterPickerPanel = filtersBucket.openDatePanelOfFilter(filtersBucket.getDateFilter());
        dateFilterPickerPanel.configTimeFilterByRangeHelper(TIME_RANGE_FROM, TIME_RANGE_TO)
            .changeCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR)
            .openCompareApplyMeasures().selectByNames(metric).apply();
        dateFilterPickerPanel.apply();
        analysisPage.waitForReportComputing();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareInsightWithTreatNullValuesAsZero() {
        uploadCSV(getFilePathFromResource(FOUR_DATE_CSV_PATH));
        takeScreenshot(browser, "uploaded-fourdate", getClass());

        initAnalysePage().getCatalogPanel().changeDataset(FOUR_DATE_DATA);
        analysisPage.changeReportType(ReportType.TABLE).addMetric(FACT_NUMBER, FieldType.FACT).addAttribute(ATTR_NAME)
            .openFilterBarPicker().checkItem(SUM_OF_FACT_NUMBER, 1).apply();
        analysisPage.saveInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT);
    }

    @Test(dependsOnMethods = "prepareInsightWithTreatNullValuesAsZero")
    public void testADMeasureValueFilterNullAsZeroOptionFeatureFlag() throws IOException {
        try {
            projectRestRequest.updateProjectConfiguration(
                AD_MEASURE_VALUE_FILTER_NULL_AS_ZERO_OPTION.getFlagName(), DISABLED);

            initAnalysePage().openInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT).waitForReportComputing();
            MeasureValueFilterPanel measureValueFilterPanel = analysisPage.openMeasureFilterPanel(SUM_OF_FACT_NUMBER, 1)
                .selectLogicalOperator(GREATER_THAN);

            assertFalse(measureValueFilterPanel.isTreatNullValuesCheckboxPresent(),
                "Checkbox should not be visible");

            projectRestRequest.updateProjectConfiguration(
                AD_MEASURE_VALUE_FILTER_NULL_AS_ZERO_OPTION.getFlagName(), ENABLED_UNCHECKED_BY_DEFAULT);

            initAnalysePage().openInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT).waitForReportComputing();
            measureValueFilterPanel = analysisPage.openMeasureFilterPanel(SUM_OF_FACT_NUMBER, 1)
                .selectLogicalOperator(GREATER_THAN);

            assertFalse(measureValueFilterPanel.isTreatNullValuesCheckboxChecked(), "Checkbox should not be checked");
            assertEquals(measureValueFilterPanel.getTreatNullValuesAsZero(), "Treat blank values as 0");

            projectRestRequest.updateProjectConfiguration(
                AD_MEASURE_VALUE_FILTER_NULL_AS_ZERO_OPTION.getFlagName(), ENABLED_CHECKED_BY_DEFAULT);

            initAnalysePage().openInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT).waitForReportComputing();
            measureValueFilterPanel = analysisPage.openMeasureFilterPanel(SUM_OF_FACT_NUMBER, 1)
                .selectLogicalOperator(GREATER_THAN);

            assertTrue(measureValueFilterPanel.isTreatNullValuesCheckboxChecked(), "Checkbox should be checked");
            assertEquals(measureValueFilterPanel.getTreatNullValuesAsZero(), "Treat blank values as 0");
        } finally {
            projectRestRequest.updateProjectConfiguration(
                AD_MEASURE_VALUE_FILTER_NULL_AS_ZERO_OPTION.getFlagName(), ENABLED_CHECKED_BY_DEFAULT);
        }
    }

    @Test(dependsOnMethods = "testADMeasureValueFilterNullAsZeroOptionFeatureFlag")
    public void addMeasureValueFilterWithTreatNullValueAsZero() {
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT)
            .waitForReportComputing().getPivotTableReport();

        analysisPage.openMeasureFilterPanel(SUM_OF_FACT_NUMBER, 1)
            .addMeasureValueFilter(LESS_THAN, "10");
        analysisPage.saveInsight().waitForReportComputing();

        log.info("Body Content :" + pivotTableReport.getBodyContent());
        assertThat(pivotTableReport.getBodyContent(),
            hasItems(asList("HongDao", "–"), asList("HongNga", "–"), asList("HungCao", "–"), asList("TrucXinh", "–"),
                asList("PhucNguyen", "–"), asList("VinhPham", "–")));
    }

    @Test(dependsOnMethods = "addMeasureValueFilterWithTreatNullValueAsZero")
    public void exportInsightWithTreatNullValueAsZeroToXSLX() throws IOException {
        initAnalysePage().openInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT).exportTo(OptionalExportMenu.File.XLSX);
        ExportXLSXDialog.getInstance(browser).checkOption(ExportXLSXDialog.OptionalExport.CELL_MERGED)
            .checkOption(ExportXLSXDialog.OptionalExport.FILTERS_CONTEXT).confirmExport();

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + TREAT_NULL_VALUES_AS_ZERO_INSIGHT + "." + ExportFormat.EXCEL_XLSX.getName());

        waitForExporting(exportFile);
        log.info("XSLX :" + XlsxUtils.excelFileToRead(exportFile.getPath(), 0));

        assertThat(XlsxUtils.excelFileToRead(exportFile.getPath(), 0), hasItems(singletonList("HongDao"),
            singletonList("HongNga"), singletonList("HungCao"), singletonList("TrucXinh"), singletonList("PhucNguyen"),
            singletonList("VinhPham")));
    }

    @Test(dependsOnMethods = "addMeasureValueFilterWithTreatNullValueAsZero")
    public void exportInsightWithTreatNullValueAsZeroToCSV() throws IOException {
        initAnalysePage().openInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT).exportTo(OptionalExportMenu.File.CSV);

        final File exportFile = new File(testParams.getDownloadFolder() + testParams.getFolderSeparator()
            + TREAT_NULL_VALUES_AS_ZERO_INSIGHT + "." + ExportFormat.CSV.getName());

        waitForExporting(exportFile);
        log.info("CSV: "+ CSVUtils.readCsvFile(exportFile));

        assertThat(CSVUtils.readCsvFile(exportFile), hasItems(asList("HongDao", null), asList("HongNga", null),
            asList("HungCao", null), asList("TrucXinh", null), asList("PhucNguyen", null), asList("VinhPham", null)));
    }

    @Test(dependsOnMethods = "addMeasureValueFilterWithTreatNullValueAsZero")
    public void exportInsightWithTreatNullValueAsZeroToPDF() {
        String dashboardTitle = generateHashString();
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().changeDashboardTitle(dashboardTitle)
            .addInsight(TREAT_NULL_VALUES_AS_ZERO_INSIGHT).openExtendedDateFilterPanel()
            .selectPeriod(DateRange.ALL_TIME).apply();

        indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading()
            .exportDashboardToPDF();

        List<String> contents = asList(getContentFrom(dashboardTitle).split("\n"));

        log.info("PDF: " + contents);
        assertThat(contents, hasItems("HongDao –", "HongNga –", "HungCao –"));
    }

    private void createCalculatedMeasure(
        Pair<String, Integer> metricA, Pair<String, Integer> metricB, OperatorCalculated operatorCalculated) {

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket().createCalculatedMeasure()
            .getMetricConfiguration(CALCULATED_RATIO_OF);
        metricConfiguration.chooseArithmeticMeasureA(metricA.getLeft(), metricA.getRight());
        metricConfiguration.chooseArithmeticMeasureB(metricB.getLeft(), metricB.getRight());
        metricConfiguration.chooseOperator(operatorCalculated);
    }
}
