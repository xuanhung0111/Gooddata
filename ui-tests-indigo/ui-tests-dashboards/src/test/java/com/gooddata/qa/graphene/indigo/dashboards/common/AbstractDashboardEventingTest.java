package com.gooddata.qa.graphene.indigo.dashboards.common;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.md.Restriction;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Collections.singletonList;

public class AbstractDashboardEventingTest extends AbstractDashboardTest {

    protected IndigoRestRequest indigoRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createAmountMetric();
        metrics.createNumberOfOpportunitiesMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
    }

    protected Pair<Integer, Integer> getColumnPosition(ChartReport chartReport, String legend, String attributeValue) {
        int row = chartReport.getLegendIndex(legend);
        List<String> labels = chartReport.getAxisLabels();
        int col = labels.indexOf(attributeValue);
        return Pair.of(row, col);
    }

    protected Pair<Integer, Integer> getColumnPosition(ChartReport chartReport, String legend) {
        int row = chartReport.getLegendIndex(legend);
        return Pair.of(row, 0);
    }

    protected String getInsightUriFromBrowserUrl() {
        String hasIdPart = browser.getCurrentUrl().split(testParams.getProjectId() + "/")[1];
        return "/gdc/md/" + testParams.getProjectId() + "/obj/" + hasIdPart.split("/")[0];
    }

    protected String createInsight(String title, ReportType reportType,
                                   List<String> metrics, List<String> attributes, String stack) {
        InsightMDConfiguration insightMDConfiguration = new InsightMDConfiguration(title, reportType);
        List<MeasureBucket> measureBuckets = metrics.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(Collectors.toList());
        insightMDConfiguration.setMeasureBucket(measureBuckets);

        if (reportType == ReportType.TABLE) {
            List<CategoryBucket> categoryBuckets = attributes.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), Type.ATTRIBUTE))
                    .collect(Collectors.toList());
            if (StringUtils.isNotEmpty(stack)) {
                categoryBuckets.add(CategoryBucket.createCategoryBucket(getAttributeByTitle(stack), Type.ATTRIBUTE));
            }
            insightMDConfiguration.setCategoryBucket(categoryBuckets);
        } else {
            List<CategoryBucket> categoryBuckets = attributes.stream()
                    .map(attribute -> CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), Type.VIEW))
                    .collect(Collectors.toList());
            if (StringUtils.isNotEmpty(stack)) {
                categoryBuckets.add(CategoryBucket.createCategoryBucket(getAttributeByTitle(stack), Type.STACK));
            }
            insightMDConfiguration.setCategoryBucket(categoryBuckets);
        }
        return indigoRestRequest.createInsight(insightMDConfiguration);
    }

    protected String createAnalyticalDashboard(String dashboardTitle, String insightUri) throws IOException {
        return indigoRestRequest.createAnalyticalDashboard(
                singletonList(
                        indigoRestRequest.createVisualizationWidget(
                                insightUri,
                                dashboardTitle
                        )
                ), dashboardTitle);
    }

    protected String createSimpleTableInsightWithPercentage(String title, String metric, String attribute) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TABLE)
                        .setMeasureBucket(singletonList(MeasureBucket.createMeasureBucketWithShowInPercent(
                                getMetricByTitle(metric), true)))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        Type.ATTRIBUTE))));
    }

    protected String createSimpleColumnInsightWithPercentage(String title, String metric, String attribute) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.COLUMN_CHART)
                        .setMeasureBucket(singletonList(MeasureBucket.createMeasureBucketWithShowInPercent(
                                getMetricByTitle(metric), true)))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        Type.VIEW))));
    }

    protected String createInsight(String title, ReportType reportType,
                                   List<String> metrics, List<String> attributes) {
        return createInsight(title, reportType, metrics, attributes, "");
    }

    protected String createSimpleTableInsight(String title, String metric, String attribute,
                                              String stack) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, ReportType.TABLE)
                        .setMeasureBucket(singletonList(MeasureBucket
                                .createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(Arrays.asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack), Type.ATTRIBUTE))));
    }

    protected String createTemplateHtmlFile(String insightObjectId, String uris, String identifiers, String pathFile) throws IOException {
        final String content = ResourceUtils.getResourceAsString(pathFile);
        final String replacedContent = content
                .replace("{host}", testParams.getHost())
                .replace("{project}", testParams.getProjectId())
                .replace("{reportId}", insightObjectId)
                .replace("{uris}", uris)
                .replace("{identifiers}", identifiers);
        return ResourceUtils.createTempFileFromString(replacedContent);
    }

    protected String createTemplateHtmlFile(String kpiId) throws IOException {
        return createTemplateHtmlFile(kpiId, "[]", "[]", "/eventing/frame_KD.html");
    }

    protected String createTemplateHtmlFile(String kpiId, String uris) throws IOException {
        return createTemplateHtmlFile(kpiId, uris, "[]", "/eventing/frame_KD.html");
    }

    protected String createTemplateHtmlFile(String kpiId, String uris, String identifier) throws IOException {
        return createTemplateHtmlFile(kpiId, uris, identifier, "/eventing/frame_KD.html");
    }

    protected String getObjectIdFromUri(String uri) {
        return uri.split("/obj/")[1];
    }

    protected IndigoDashboardsPage openEmbeddedPage(final String url) {
        browser.get("file://" + url);
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        return IndigoDashboardsPage.getInstance(browser).waitForDashboardLoad().waitForWidgetsLoading();
    }

    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
        waitForElementNotVisible(By.id("logger"));
        browser.switchTo().frame("iframe");
    }

    protected String getLoggerContent() {
        String content = "";
        try {
            browser.switchTo().defaultContent();
            content = waitForElementVisible(By.id("logger"), browser, 5).getText().trim();
        } catch (TimeoutException te) {
            // there no drill event fired, logger would not be visible
        } finally {
            browser.switchTo().frame("iframe");
            return content;
        }
    }

    protected JSONObject getLatestPostMessageObj() {
        String contentStr = getLoggerContent();
        log.info(contentStr);
        return Stream.of(contentStr.split("\n"))
                .map(JSONObject::new).filter(jsonObject -> jsonObject.getString("name").equals("drill")).findFirst().get();
    }

    protected String createAnalyticalDashboardWithWidget(String dashboardTitle, String widget) throws IOException {
        return indigoRestRequest.createAnalyticalDashboard(singletonList(widget), dashboardTitle);
    }

    protected Pair<String, Metric> createNodataKpi() {
        return createNodataKpi(null, null);
    }

    protected Pair<String, Metric> createNodataKpi(String oldDashBoardUri, String tabIdentifier) {
        String amountUri = getMdService().getObjUri(getProject(), Metric.class, Restriction.title(METRIC_AMOUNT));
        String kpi = "widget_1_" + generateHashString();
        Metric metric = getMdService().createObj(getProject(),
                new Metric("nodata_metric", "SELECT [" + amountUri + "] WHERE 2 = 1", "#,##0.00"));
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createInvalidKpi() {
        return createInvalidKpi(null, null);
    }

    protected Pair<String, Metric> createInvalidKpi(String oldDashBoardUri, String tabIdentifier) {
        String variableUri = getVariableCreator().createQuoteVariable();
        String kpi = "widget_2_" + generateHashString();
        Metric metric = getMdService().createObj(getProject(),
                new Metric("invalid_metric", String.format("SELECT SUM([%s])", variableUri), "#,##0.00"));
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasAttributeOnMaql() {
        return createKpiHasAttributeOnMaql(null, null);
    }

    protected Pair<String, Metric> createKpiHasAttributeOnMaql(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMetricCreator().createNumberOfActivitiesMetric();
        String kpi = "widget_3_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasDate() {
        return createKpiHasDate(null, null);
    }

    protected Pair<String, Metric> createKpiHasDate(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMetricCreator().createAmountMetric();
        String kpi = "widget_4_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasVarible() {
        return createKpiHasVarible(null, null);
    }

    protected Pair<String, Metric> createKpiHasVarible(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMetricCreator().createQuotaMetric();
        String kpi = "widget_5_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasNegativeMetric() {
        return createKpiHasNegativeMetric(null, null);
    }

    protected Pair<String, Metric> createKpiHasNegativeMetric(String oldDashBoardUri, String tabIdentifier) {
        String amountMetricUri = getMetricByTitle(METRIC_AMOUNT).getUri();
        String kpi = "widget_6_" + generateHashString();
        Metric metric = getMdService().createObj(getProject(),
                new Metric("metric_in_negative", "SELECT [" + amountMetricUri + "] * -1", "#,##0.00"));
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasZeroMetric() {
        return createKpiHasZeroMetric(null, null);
    }

    protected Pair<String, Metric> createKpiHasZeroMetric(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMdService().createObj(getProject(),
                new Metric("metri_zero", "SELECT 0", "#,##0.00"));
        String kpi = "widget_7_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);

        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasMetricInShare() {
        return createKpiHasMetricInShare(null, null);
    }

    protected Pair<String, Metric> createKpiHasMetricInShare(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMetricCreator().createPercentOfGoalMetric();
        String kpi = "widget_8_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasMetricInDifference() {
        return createKpiHasMetricInDifference(null, null);
    }

    protected Pair<String, Metric> createKpiHasMetricInDifference(String oldDashBoardUri, String tabIdentifier) {
        String metricNumberOfActivitiesUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String emailUri = getAttributeElementUri(ATTR_ACTIVITY_TYPE, "Email");
        Attribute activityTypeAttr = getAttributeByTitle(ATTR_ACTIVITY_TYPE);
        String maql = String.format("SELECT [%s] - (SELECT [%s] BY ALL [%s] WHERE [%s] IN ([%s]))",
                metricNumberOfActivitiesUri, metricNumberOfActivitiesUri, activityTypeAttr.getUri(),
                activityTypeAttr.getUri(), emailUri);

        Metric metric = getMdService().createObj(getProject(),
                new Metric("metric_in_diff", maql, "#,##0.00"));
        String kpi = "widget_9_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasMetricInRatio() {
        return createKpiHasMetricInRatio(null, null);
    }

    protected Pair<String, Metric> createKpiHasMetricInRatio(String oldDashBoardUri, String tabIdentifier) {
        String numberActivitiesUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        String numberOfOpportunities = getMetricByTitle(METRIC_NUMBER_OF_OPPORTUNITIES).getUri();
        String maql = String.format("SELECT [%s] / [%s]", numberActivitiesUri, numberOfOpportunities);
        Metric metric = getMdService().createObj(getProject(),
                new Metric("metric_in_ratio", maql, "#,##0.00"));
        String kpi = "widget_10_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasNoDate() {
        return createKpiHasNoDate(null, null);
    }

    protected Pair<String, Metric> createKpiHasNoDate(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMetricCreator().createAmountMetric();
        String kpi = "widget_11_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasAlert() {
        return createKpiHasAlert(null, null);
    }

    protected Pair<String, Metric> createKpiHasAlert(String oldDashBoardUri, String tabIdentifier) {
        Metric metric = getMetricCreator().createAmountMetric();
        String kpi = "widget_12_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasPoP() {
        Metric metric = getMetricCreator().createAmountMetric();
        String kpi = "widget_13_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(ComparisonDirection.GOOD);
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasSamePoP() {
        Metric metric = getMetricCreator().createAmountMetric();
        String kpi = "widget_14_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
                .title(kpi)
                .metric(metric.getUri())
                .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
                .comparisonType(ComparisonType.LAST_YEAR)
                .comparisonDirection(ComparisonDirection.GOOD);
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected Pair<String, Metric> createKpiHasAlert(Metric metric) {
        return createKpiHasAlert(metric, null, null);
    }

    protected Pair<String, Metric> createKpiHasAlert(Metric metric, String oldDashBoardUri, String tabIdentifier) {
        String kpi = "widget_15_" + generateHashString();
        KpiMDConfiguration.Builder configuration = new KpiMDConfiguration.Builder()
            .title(kpi)
            .metric(metric.getUri())
            .dateDataSet(getDateDatasetUri(DATE_DATASET_CREATED))
            .comparisonType(ComparisonType.NO_COMPARISON)
            .comparisonDirection(ComparisonDirection.NONE);
        if (oldDashBoardUri != null) {
            configuration.drillToDashboard(oldDashBoardUri);
            configuration.drillToDashboardTab(tabIdentifier);
        }
        String kpiUri = createKpiUsingRest(configuration.build());
        return Pair.of(kpiUri, metric);
    }

    protected String generateAnalyticalDashboardName() {
        return "Analytical-Dashboard-" + UUID.randomUUID().toString().substring(0, 6);
    }
}
