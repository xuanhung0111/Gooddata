package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TIMELINE_BOP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WIN_RATE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_FORECAST_CATEGORY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DATE_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AVG_AMOUNT;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;
import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class PivotTableTest extends AbstractAnalyseTest {

    private static final String INSIGHT_HAS_A_ROW_ATTRIBUTE_AND_SOME_MEASURES = "A row attribute and some measures";
    private static final String INSIGHT_HAS_SOME_ATTRIBUTES_AND_SOME_MEASURES = "Some attributes and some measures";
    private static final String INSIGHT_HAS_ATTRIBUTE_AND_MEASURE = "Attribute and measure";
    private static final String INSIGHT_HAS_METRICS = "Metrics";
    private static final String INSIGHT_HAS_ATTRIBUTES = "Attributes";
    private static final String INSIGHT_HAS_A_METRIC = "A metric";
    private static final String INSIGHT_HAS_A_ATTRIBUTE = "A attribute";
    private static final String INSIGHT_HAS_A_COLUMN_ATTRIBUTE = "A column attribute";
    private static final String INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES = "A metric and attributes";
    private static final String INSIGHT_TEST_DASHBOARD = "Test dashboard";
    private static final String INSIGHT_HAS_RESTRICTED_FACT = "Restricted fact";
    private static final String INSIGHT_HAS_PROTECTED_ATTRIBUTE = "Protected attribute";
    private static final String DEFAULT_METRIC_NUMBER_FORMAT = "#,##0.00";
    private static final String GOODDATA_DATE_CREATED_DATASET_ID = "created.dataset.dt";
    private IndigoRestRequest indigoRestRequest;
    private FactRestRequest factRestRequest;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "New Table Grid, Paging and Pivoting";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createAmountMetric();
        metrics.createAvgAmountMetric();
        metrics.createWonMetric();
        metrics.createWinRateMetric();
        metrics.createTimelineBOPMetric();
        metrics.createPercentOfGoalMetric();
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        setPivotFlag(true);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disablePivot_ForOldTableIsRenderedWell() {
        try {
            setPivotFlag(false);
            indigoRestRequest.createInsight(
                    new InsightMDConfiguration(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES, ReportType.TABLE)
                            .setMeasureBucket(singletonList(MeasureBucket
                                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                            .setCategoryBucket(asList(
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                            CategoryBucket.Type.ATTRIBUTE),
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                            CategoryBucket.Type.ATTRIBUTE))));
            AttributesBucket attributesBucket = initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES)
                    .waitForReportComputing().getAttributesBucket();
            assertEquals(attributesBucket.getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
        } finally {
            setPivotFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void enabledPivot_ForAttributesBucketMappedToAttributesOnRowsBucket() {
        try {
            setPivotFlag(true);
            indigoRestRequest.createInsight(
                    new InsightMDConfiguration(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES, ReportType.TABLE)
                            .setMeasureBucket(singletonList(MeasureBucket
                                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                            .setCategoryBucket(asList(
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                            CategoryBucket.Type.ATTRIBUTE),
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                            CategoryBucket.Type.ATTRIBUTE))));
            AttributesBucket attributesBucket = initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES)
                    .waitForReportComputing().getAttributesBucket();
            assertEquals(attributesBucket.getItemNames(), asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY));
            assertEquals(analysisPage.getAttributesColumnsBucket().getItemNames(), emptyList());
        } finally {
            setPivotFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disablePivot_ForAttributesOnRowsAndColumnsMappedToAttributesBucket() {
        try {
            setPivotFlag(true);
            indigoRestRequest.createInsight(
                    new InsightMDConfiguration(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES, ReportType.TABLE)
                            .setMeasureBucket(singletonList(MeasureBucket
                                    .createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                            .setCategoryBucket(asList(
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                            CategoryBucket.Type.ATTRIBUTE),
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                            CategoryBucket.Type.ATTRIBUTE),
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_STAGE_NAME),
                                            CategoryBucket.Type.COLUMNS),
                                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_OPP_SNAPSHOT),
                                            CategoryBucket.Type.COLUMNS))));
            setPivotFlag(false);
            AttributesBucket attributesBucket = initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC_AND_ATTRIBUTES)
                    .waitForReportComputing().getAttributesBucket();
            assertEquals(attributesBucket.getItemNames(),
                    asList(ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY, ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT));
        } finally {
            setPivotFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void chooseTableOnVisualizationBucket() {
        String computedAttribute = createComputedAttributeUsing(ATTR_IS_WON);
        MetricsBucket metricsBucket = initAnalysePage().changeReportType(ReportType.TABLE)
                .addMetric(METRIC_AMOUNT).addMetric(FACT_AMOUNT, FieldType.FACT)
                .addMetric(ATTR_DEPARTMENT, FieldType.ATTRIBUTE).waitForReportComputing()
                .getMetricsBucket();
        assertEquals(metricsBucket.getItemNames(),
                 asList(METRIC_AMOUNT, "Sum of " + METRIC_AMOUNT, "Count of " + ATTR_DEPARTMENT));

        analysisPage.addAttribute(ATTR_DEPARTMENT).addAttribute(computedAttribute).addDate().waitForReportComputing();

        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
        assertEquals(attributesBucket.getItemNames(), asList(ATTR_DEPARTMENT, computedAttribute, DATE));

        analysisPage.addColumnsAttribute(ATTR_IS_CLOSED).addColumnsAttribute(computedAttribute)
                .addDateToColumnsAttribute().waitForReportComputing();

        AttributesBucket attributesColumnsBucket = analysisPage.getAttributesColumnsBucket();
        assertEquals(attributesColumnsBucket.getItemNames(), asList(ATTR_IS_CLOSED, computedAttribute, DATE));

        ElementUtils.scrollElementIntoView(attributesColumnsBucket.getRoot(), browser);
        analysisPage.drag(attributesBucket.get(ATTR_DEPARTMENT), attributesColumnsBucket.getInvitation());
        assertEquals(attributesBucket.getItemNames(), asList(computedAttribute, DATE));
        assertEquals(attributesColumnsBucket.getItemNames(),
                asList(ATTR_IS_CLOSED, computedAttribute, DATE, ATTR_DEPARTMENT));

        analysisPage.resetToBlankState().changeReportType(ReportType.TABLE);
        AnalysisPageHeader analysisPageHeader = analysisPage.getPageHeader();
        assertTrue(analysisPageHeader.isUndoButtonEnabled(), "Undo button should be enabled");
        assertTrue(analysisPageHeader.isOpenButtonEnabled(), "Open button should be enabled");
        assertTrue(analysisPageHeader.isResetButtonEnabled(), "Clear button should be enabled");
        assertFalse(analysisPageHeader.isRedoButtonEnabled(), "Undo button should be disabled");
        assertFalse(analysisPageHeader.isSaveButtonEnabled(), "Open button should be disabled");
        assertFalse(analysisPageHeader.isExportButtonEnabled(), "Open as report should be disabled");
        assertEquals(getTooltipFromElement(ReportType.TABLE.getLocator(), browser), "Table");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSomeCommonPivotTablesHaveOnlyAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_A_COLUMN_ATTRIBUTE, ReportType.TABLE)
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));
        initAnalysePage().openInsight(INSIGHT_HAS_A_COLUMN_ATTRIBUTE).waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertThat(pivotTableReport.getHeadersColumn(), hasItem(ATTR_DEPARTMENT));

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_A_ATTRIBUTE, ReportType.TABLE)
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));
        initAnalysePage().openInsight(INSIGHT_HAS_A_ATTRIBUTE).waitForReportComputing();
        assertThat(pivotTableReport.getHeadersRow(), hasItem(ATTR_DEPARTMENT));

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES, ReportType.TABLE)
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTES).waitForReportComputing();
        assertThat(pivotTableReport.getHeadersRow(), hasItem(ATTR_FORECAST_CATEGORY));
        assertThat(analysisPage.getPivotTableReport().getHeadersColumn(), hasItem(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSomeCommonPivotTablesHaveOnlyMeasures() {
        createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_AMOUNT);
        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC)
                .waitForReportComputing().getPivotTableReport();
        assertEquals(pivotTableReport.getHeadersMeasure(), asList(METRIC_AMOUNT));

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_METRICS, ReportType.TABLE)
                        .setMeasureBucket(asList(
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON)),
                                MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_TIMELINE_BOP)))));
        pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_METRICS).waitForReportComputing().getPivotTableReport();
        assertEquals(pivotTableReport.getHeadersMeasure(), asList(METRIC_AMOUNT, METRIC_WON, METRIC_TIMELINE_BOP));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSomeCommonPivotTablesHaveMeasuresAndAttributes() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertThat(pivotTableReport.getHeadersMeasure(), hasItem(METRIC_AMOUNT));
        assertThat(pivotTableReport.getHeadersColumn(), hasItems(ATTR_DEPARTMENT));

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        assertThat(pivotTableReport.getHeadersMeasure(), hasItem(METRIC_AMOUNT));
        assertEquals(pivotTableReport.getHeadersRow(), asList(ATTR_DEPARTMENT));

        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_AMOUNT, ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).waitForReportComputing();
        assertThat(pivotTableReport.getHeadersMeasure(), hasItem(METRIC_AMOUNT));
        assertEquals(pivotTableReport.getHeadersRow(), asList(ATTR_FORECAST_CATEGORY));
        assertThat(pivotTableReport.getHeadersColumn(), hasItem(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesHaveSameObjects() {
        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, METRIC_WON, ATTR_DEPARTMENT, ATTR_DEPARTMENT);
        initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).addMetric(METRIC_WON).waitForReportComputing();
        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();

        assertThat(pivotTableReport.getHeadersMeasure(), hasItems(METRIC_WON));
        assertEquals(pivotTableReport.getHeadersRow(), asList(ATTR_DEPARTMENT));
        assertThat(pivotTableReport.getHeadersColumn(), hasItem(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesNoData() {
        createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_WON);
        String date = "01/01/2019";
        final String MESSAGE_SYSTEM = "NO DATA FOR YOUR FILTER SELECTION" +
                "\nTry adjusting or removing some of the filters.";
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC).waitForReportComputing().getMetricsBucket()
                .getMetricConfiguration(METRIC_WON).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, date, date);
        assertEquals(analysisPage.waitForReportComputing().getMainEditor().getCanvasMessage(), MESSAGE_SYSTEM);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesIncomputedOrInvalid() {
        String dateBigRange = "1/1/5000";
        final String MESSAGE_SYSTEM = "SORRY, WE CAN'T DISPLAY THIS INSIGHT" +
                "\nTry applying different filters, or using different measures or attributes." +
                "\nIf this did not help, contact your administrator.";
        createSimpleInsight(INSIGHT_HAS_A_METRIC, METRIC_AMOUNT);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC).addDate().waitForReportComputing().getMetricsBucket()
                .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
                .addFilterByDate(DATE_DATASET_CLOSED, dateBigRange, dateBigRange);
        assertEquals(analysisPage.waitForReportComputing().getMainEditor().getCanvasMessage(), MESSAGE_SYSTEM);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesPlatformLimited() {
        final String MESSAGE_SYSTEM = "TOO MANY DATA POINTS TO DISPLAY";
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(FACT_AMOUNT, FieldType.FACT)
                .addAttribute(ATTR_ACCOUNT).addAttribute(ATTR_OPP_SNAPSHOT).waitForReportComputing();
        assertEquals(analysisPage.getExplorerMessage(), MESSAGE_SYSTEM);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesViaRecommendedStepsPanelOnCanvas() {
        initAnalysePage().addMetricToRecommendedStepsPanelOnCanvas(METRIC_AMOUNT).changeReportType(ReportType.TABLE)
                .waitForNonEmptyBuckets().waitForReportComputing();
        assertThat(analysisPage.getPivotTableReport().getHeadersMeasure(), hasItems(METRIC_AMOUNT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesHasMetricWithMAQL() {
        String metricWithMAQL = "Sum of Amount" + generateHashString();
        createMetric(metricWithMAQL, format("SELECT SUM([%s])", getFactByTitle("Amount").getUri()), "#,##0.00");
        createSimpleInsight(INSIGHT_HAS_A_METRIC, metricWithMAQL);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC).waitForReportComputing();
        assertThat(analysisPage.getPivotTableReport().getHeadersMeasure(), hasItems(metricWithMAQL));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesHasNullValue() {
        final String metricExpression = format("SELECT SUM([%s]) WHERE 1 = 0",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount")));
        final String metricNullValue = "EMPTY_SHOW_NULL_STRING";
        createMetric(metricNullValue, metricExpression, "#'##0,00 formatted; [=null] null value!");
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(metricNullValue).addMetric(METRIC_AMOUNT)
                .waitForReportComputing();
        assertThat(analysisPage.getPivotTableReport().getHeadersMeasure(),
                hasItems(METRIC_AMOUNT, "EMPTY_SHOW_NULL_STRING"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialPivotTablesHasNegativeValue() {
        final String metricNegativeValue = "Min of Amount";
        createMetric(metricNegativeValue, format("SELECT MIN([%s])",
                getMdService().getObjUri(getProject(), Fact.class, title("Amount"))), "#,##0.00");
        createSimpleInsight(INSIGHT_HAS_A_METRIC, metricNegativeValue);
        initAnalysePage().openInsight(INSIGHT_HAS_A_METRIC).waitForReportComputing();
        assertThat(analysisPage.getPivotTableReport().getHeadersMeasure(), hasItems(metricNegativeValue));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSpecialXSSPivotTables() throws IOException {
        final String xssHeader = "<script>alert('testing')</script>";
        final String xssFormatMetricName = "<button>" + METRIC_PERCENT_OF_GOAL + "</button>";
        final String xssFormatMetricMaQL = "SELECT 1";
        final String GOOD_DATA_CREATED_FILTER_XSS_NAME = "<script>alert(\"testing\")</script>";

        final Metric xssFormatMetric = getMdService().createObj(getProject(),
                new Metric(xssFormatMetricName, xssFormatMetricMaQL, "<button>#,##0.00</button>"));

        createInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, xssFormatMetricName, ATTR_DEPARTMENT, ATTR_FORECAST_CATEGORY);
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(xssFormatMetric)))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));
        try {
            changeDatasetTitleByRest(GOODDATA_DATE_CREATED_DATASET_ID, GOOD_DATA_CREATED_FILTER_XSS_NAME);
            initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).addColumnsAttribute(ATTR_FORECAST_CATEGORY)
                    .waitForReportComputing();
            PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
            AttributesBucket attributesColumnsBucket = analysisPage.getAttributesColumnsBucket();
            AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
            MetricsBucket metricsBucket = analysisPage.getMetricsBucket();

            //date dimension
            assertEquals(metricsBucket.getMetricConfiguration(xssFormatMetricName).expandConfiguration()
                    .addFilterByDate("_testing_", DateRange.THIS_YEAR.toString())
                    .getFilterByDate(), "\"testing\": " + DateRange.THIS_YEAR.toString());
            metricsBucket.getMetricConfiguration(xssFormatMetricName).removeFilterByDate();

            //metric name
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getHeadersMeasure(), hasItem(xssFormatMetricName));

            //attribute name/value
            attributesColumnsBucket.setTitleItemBucket(ATTR_FORECAST_CATEGORY, xssHeader);
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getHeadersColumn(), hasItem(xssHeader));

            //header
            attributesBucket.setTitleItemBucket(ATTR_DEPARTMENT, xssHeader);
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getHeadersRow(), hasItem(xssHeader));
        } finally {
            changeDatasetTitleByRest(GOODDATA_DATE_CREATED_DATASET_ID, ATTR_DATE_CREATED);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createPivotTablesHaveLongNames() throws IOException {
        final String longNameMetric = "Having-Long-Name" + UUID.randomUUID().toString().substring(0, 10);
        final String formatMetricMaQL = "SELECT 1";
        final Metric formatMetric = getMdService().createObj(getProject(),
                new Metric(longNameMetric, formatMetricMaQL, DEFAULT_METRIC_NUMBER_FORMAT));
        final String GOOD_DATA_FISCAL_CREATED_FILTER_LONG_NAME = "Date-Dimension-Having-Long-Name" +
                UUID.randomUUID().toString().substring(0, 10);

        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(formatMetric)))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.ATTRIBUTE))));
        try {
            changeDatasetTitleByRest(GOODDATA_DATE_CREATED_DATASET_ID, GOOD_DATA_FISCAL_CREATED_FILTER_LONG_NAME);
            initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTE_AND_MEASURE).addColumnsAttribute(ATTR_FORECAST_CATEGORY)
                    .waitForReportComputing();
            PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
            AttributesBucket attributesColumnsBucket = analysisPage.getAttributesColumnsBucket();
            AttributesBucket attributesBucket = analysisPage.getAttributesBucket();
            MetricsBucket metricsBucket = analysisPage.getMetricsBucket();

            //date dimension
            assertEquals(metricsBucket.getLastMetricConfiguration().expandConfiguration()
                    .addFilterByDate(GOOD_DATA_FISCAL_CREATED_FILTER_LONG_NAME.toLowerCase(),
                            DateRange.THIS_YEAR.toString())
                    .getFilterByDate(), "Date-Dime…This year");
            metricsBucket.getLastMetricConfiguration().removeFilterByDate();

            //metric name
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getHeadersMeasure(), hasItem(longNameMetric));

            //attribute name/value
            attributesColumnsBucket.setTitleItemBucket(ATTR_FORECAST_CATEGORY, longNameMetric);
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getHeadersColumn(), hasItem(longNameMetric));

            //header
            attributesBucket.setTitleItemBucket(ATTR_DEPARTMENT, longNameMetric);
            analysisPage.waitForReportComputing();
            assertThat(pivotTableReport.getHeadersRow(), hasItem(longNameMetric));
        } finally {
            changeDatasetTitleByRest(GOODDATA_DATE_CREATED_DATASET_ID, ATTR_DATE_CREATED);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createPivotTableHasRestrictedFact() throws IOException {
        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        try {
            factRestRequest.setFactRestricted(factUri);
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT, FieldType.FACT)
                    .waitForReportComputing().saveInsight(INSIGHT_HAS_RESTRICTED_FACT);

            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_RESTRICTED_FACT).waitForReportComputing()
                    .getPivotTableReport().getHeadersMeasure(), asList("Sum of " + METRIC_AMOUNT));
        } finally {
            factRestRequest.unsetFactRestricted(factUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createPivotTableHasProtectedAttribute() throws IOException {
        final String MESSAGE_SYSTEM = "YOU ARE NOT AUTHORIZED TO SEE THIS REPORT\nContact your administrator.";
        String attributeUri = factRestRequest.getAttributeByTitle(ATTR_STAGE_NAME).getUri();
        try {
            factRestRequest.setFactProtected(attributeUri);
            initAnalysePage().changeReportType(ReportType.TABLE).addAttribute(ATTR_STAGE_NAME)
                    .waitForReportComputing().saveInsight(INSIGHT_HAS_PROTECTED_ATTRIBUTE);

            addUsersWithOtherRolesToProject();
            logoutAndLoginAs(true, UserRoles.EDITOR);

            assertEquals(initAnalysePage().openInsight(INSIGHT_HAS_PROTECTED_ATTRIBUTE).waitForReportComputing()
                    .getMainEditor().getCanvasMessage(), MESSAGE_SYSTEM);

        } finally {
            factRestRequest.unsetFactProtected(attributeUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkStructurePivotTableOnMetadata() throws IOException {
        final String INSIGHT = "INSIGHT_CHECK_METADATA";
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT).addMetric(METRIC_WON)
                .addAttribute(ATTR_DEPARTMENT).addColumnsAttribute(ATTR_STAGE_NAME).waitForReportComputing()
                .saveInsight(INSIGHT);

        assertEquals(getTitlesMetricFromInsightMetadata(INSIGHT), asList(METRIC_AMOUNT, METRIC_WON));
        assertEquals(getTitlesAttributeFromInsightMetadata(INSIGHT, "attribute"), asList(ATTR_DEPARTMENT));
        assertEquals(getTitlesAttributeFromInsightMetadata(INSIGHT, "columns"), asList(ATTR_STAGE_NAME));
        assertEquals(getFiltersFromInsightMetadata(INSIGHT), asList(ATTR_DEPARTMENT, ATTR_STAGE_NAME));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void placePivotTableOnDashboard() {
        List<List<String>> expectedValues = asList(asList("Exclude", "$33,562,482.51", "$15,370,157.08"),
                                                   asList("Include", "$46,843,842.45", "$20,848,974.50"));
        createInsight(INSIGHT_TEST_DASHBOARD, METRIC_AMOUNT, ATTR_FORECAST_CATEGORY, ATTR_DEPARTMENT);

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage().addDashboard()
                .addInsight(INSIGHT_TEST_DASHBOARD).selectDateFilterByName("All time");
        PivotTableReport pivotTableReport = indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport();

        List<List<String>> contentsTable = pivotTableReport.getContent();
        for(int i = 0; i < contentsTable.size(); i++) {
            arrayContainingInAnyOrder(contentsTable.get(i), expectedValues.get(i));
        }
        assertThat(pivotTableReport.getHeadersMeasure(), hasItems(METRIC_AMOUNT));
        assertEquals(pivotTableReport.getHeadersRow(), asList(ATTR_FORECAST_CATEGORY));
        assertThat(pivotTableReport.getHeadersColumn(), hasItem(ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addATotalIntoColumnMetricHeader_ForInsightHasARowAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_A_ROW_ATTRIBUTE_AND_SOME_MEASURES, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_A_ROW_ATTRIBUTE_AND_SOME_MEASURES)
                .waitForReportComputing().getPivotTableReport()
                .addNewTotals(AggregationItem.SUM, METRIC_AMOUNT, 0);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getCellElementText(METRIC_AMOUNT, 0, 2), "$116,625,456.54");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AVG_AMOUNT, 0, 2), "–");

        analysisPage.addFilter(ATTR_FORECAST_CATEGORY).waitForReportComputing().getFilterBuckets()
                .configAttributeFilter(ATTR_FORECAST_CATEGORY, "Exclude");
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getCellElementText(METRIC_AMOUNT, 0, 1), "$48,932,639.59");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AVG_AMOUNT, 0, 1), "–");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addATotalIntoColumnMetricHeader_ForInsightHasSomeAttributes() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_ATTRIBUTES_AND_SOME_MEASURES, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_SOME_ATTRIBUTES_AND_SOME_MEASURES)
                .waitForReportComputing().getPivotTableReport()
                .addNewTotals(AggregationItem.SUM, METRIC_AMOUNT, 0);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getCellElementText(METRIC_AMOUNT, 0, 2), "$80,406,324.96");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AVG_AMOUNT, 0, 2), "–");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AMOUNT, 1, 2), "$36,219,131.58");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AVG_AMOUNT, 1, 2), "–");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addATotalIntoAttributeValueColumnHeader_ForInsightHasSomeAttributes() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_SOME_ATTRIBUTES_AND_SOME_MEASURES, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AVG_AMOUNT))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_SOME_ATTRIBUTES_AND_SOME_MEASURES)
                .waitForReportComputing().getPivotTableReport()
                .addNewTotals(AggregationItem.SUM, "Direct Sales", 0);
        analysisPage.waitForReportComputing();

        assertEquals(pivotTableReport.getCellElementText(METRIC_AMOUNT, 0, 2), "$80,406,324.96");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AVG_AMOUNT, 0, 2), "$42,598.21");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AMOUNT, 1, 2), "$36,219,131.58");
        assertEquals(pivotTableReport.getCellElementText(METRIC_AVG_AMOUNT, 1, 2), "$36,751.70");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotAddTotalIntoPivotTable_ForInsightHasOnlyColumnsAttributes() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_ATTRIBUTES, ReportType.TABLE)
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                                        CategoryBucket.Type.COLUMNS),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_ATTRIBUTES)
                .waitForReportComputing().getPivotTableReport().hoverOnBurgerMenuColumn("Direct Sales", 0);

        assertFalse(pivotTableReport.isBurgerMenuPresent(), "Burger Menu shouldn't be visible");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotAddTotalIntoPivotTable_ForInsightHasAMeasureAndAnAttribute() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_A_COLUMN_ATTRIBUTE, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                                        CategoryBucket.Type.COLUMNS))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_A_COLUMN_ATTRIBUTE)
                .waitForReportComputing().getPivotTableReport().hoverOnBurgerMenuColumn("Direct Sales", 0);

        assertFalse(pivotTableReport.isBurgerMenuPresent(), "Burger Menu shouldn't be visible");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cannotAddTotalIntoPivotTable_ForInsightHasOnlyMeasures() {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(INSIGHT_HAS_METRICS, ReportType.TABLE)
                        .setMeasureBucket(
                                asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)),
                                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT)))));

        PivotTableReport pivotTableReport = initAnalysePage().openInsight(INSIGHT_HAS_METRICS)
                .waitForReportComputing().getPivotTableReport().hoverOnBurgerMenuColumn(METRIC_AMOUNT, 0);

        assertFalse(pivotTableReport.isBurgerMenuPresent(), "Burger Menu shouldn't be visible");
    }

    private List<String> getTitlesAttributeFromInsightMetadata(String insight, String localIdentifierOfBucket)
            throws JSONException, IOException {
        JSONArray buckets = indigoRestRequest.getJsonObject(indigoRestRequest
                .getInsightUri(insight))
                .getJSONObject("visualizationObject")
                .getJSONObject("content")
                .getJSONArray("buckets");
        List<String> listAttributes = new ArrayList<>();
        for (int i = 0; i < buckets.length(); i++) {
            if (localIdentifierOfBucket.equals(buckets.getJSONObject(i).getString("localIdentifier"))) {
                JSONArray itemsArray = (JSONArray) buckets.getJSONObject(i).get("items");
                for (int j = 0; j < itemsArray.length(); j++) {
                    String uriAttribute = itemsArray.getJSONObject(j).getJSONObject("visualizationAttribute")
                            .getJSONObject("displayForm").get("uri").toString();
                    String titleAttribute = factRestRequest.getJsonObject(uriAttribute)
                            .getJSONObject("attributeDisplayForm").getJSONObject("meta").get("title").toString();
                    listAttributes.add(titleAttribute);
                }
            }
        }
        return listAttributes;
    }

    private List<String> getTitlesMetricFromInsightMetadata(String insight) throws JSONException, IOException {
        JSONArray buckets = indigoRestRequest.getJsonObject(indigoRestRequest
                .getInsightUri(insight))
                .getJSONObject("visualizationObject")
                .getJSONObject("content")
                .getJSONArray("buckets");
        List<String> listMeasures = new ArrayList<>();
        for (int i = 0; i < buckets.length(); i++) {
            if ("measures".equals(buckets.getJSONObject(i).getString("localIdentifier"))) {
                JSONArray itemsArray = (JSONArray) buckets.getJSONObject(i).get("items");
                for (int j = 0; j < itemsArray.length(); j++) {
                    String titleMeasure = itemsArray.getJSONObject(j).getJSONObject("measure").get("title").toString();
                    listMeasures.add(titleMeasure);
                }
            }
        }
        return listMeasures;
    }

    private List<String> getFiltersFromInsightMetadata(String insight) throws JSONException, IOException {
        JSONArray filters = indigoRestRequest.getJsonObject(indigoRestRequest
                .getInsightUri(insight))
                .getJSONObject("visualizationObject")
                .getJSONObject("content")
                .getJSONArray("filters");
        List<String> listFilters = new ArrayList<>();
        for (int i = 0; i < filters.length(); i++) {
            String uriFilters = filters.getJSONObject(i).getJSONObject("negativeAttributeFilter")
                    .getJSONObject("displayForm").get("uri").toString();
            String titleFilter = factRestRequest.getJsonObject(uriFilters)
                    .getJSONObject("attributeDisplayForm").getJSONObject("meta").get("title").toString();
            listFilters.add(titleFilter);
        }
        return listFilters;
    }

    private String createComputedAttributeUsing(String attributeTitle) {
        String name = "CA " + System.currentTimeMillis();
        initAttributePage()
                .moveToCreateAttributePage()
                .createComputedAttribute(new ComputedAttributeDefinition().withAttribute(attributeTitle)
                        .withMetric(METRIC_WIN_RATE)
                        .withName(name));
        return name;
    }

    private void createSimpleInsight(String title, String metric) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }

    private void createInsight(String title, String metric, String attributeRow, String attributeColumn) {
        indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TABLE)
                        .setMeasureBucket(
                                singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attributeRow),
                                        CategoryBucket.Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attributeColumn),
                                        CategoryBucket.Type.COLUMNS))));
    }

    private void changeDatasetTitleByRest(final String datasetIdentifier, final String newTitle)
            throws IOException, JSONException {
        String datasetUri = getMdService().getObjUri(getProject(), com.gooddata.md.Dataset.class,
                identifier(datasetIdentifier));
        final CommonRestRequest restRequest = new CommonRestRequest(new RestClient(getProfile(ADMIN)),
                testParams.getProjectId());
        JSONObject json = restRequest.getJsonObject(datasetUri);
        json.getJSONObject("dataSet").getJSONObject("meta").put("title", newTitle);
        restRequest.executeRequest(RestRequest.initPostRequest(datasetUri + "?mode=edit", json.toString()),
                HttpStatus.OK);
    }

    private void setPivotFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_PIVOT_TABLE, status);
    }
}
