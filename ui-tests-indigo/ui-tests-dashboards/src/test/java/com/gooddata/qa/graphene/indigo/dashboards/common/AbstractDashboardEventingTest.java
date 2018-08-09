package com.gooddata.qa.graphene.indigo.dashboards.common;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.openqa.selenium.By;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    protected String createTemplateHtmlFile(String kpiId, String uris, String identifiers) throws IOException {
        final String content = ResourceUtils.getResourceAsString("/eventing/frame_KD.html");
        final String replacedContent = content
                .replace("{host}", testParams.getHost())
                .replace("{project}", testParams.getProjectId())
                .replace("{reportId}", kpiId)
                .replace("{uris}", uris)
                .replace("{identifiers}", identifiers);
        return ResourceUtils.createTempFileFromString(replacedContent);
    }

    protected String createTemplateHtmlFile(String kpiId) throws IOException {
        return createTemplateHtmlFile(kpiId, "[]", "[]");
    }

    protected String createTemplateHtmlFile(String kpiId, String uris) throws IOException {
        return createTemplateHtmlFile(kpiId, uris, "[]");
    }

    protected String getObjectIdFromUri(String uri) {
        return uri.split("/obj/")[1];
    }

    protected IndigoDashboardsPage openEmbeddedPage(final String url) {
        browser.get("file://" + url);
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        return IndigoDashboardsPage.getInstance(browser);
    }

    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
        browser.switchTo().frame("iframe");
    }

    protected String getLoggerContent() {
        browser.switchTo().defaultContent();
        String content = waitForElementVisible(By.id("logger"), browser).getText();
        browser.switchTo().frame("iframe");
        return content.trim();
    }

    protected JSONObject getLatestPostMessageObj() {
        String contentStr = getLoggerContent();
        log.info(contentStr);
        return new JSONObject(contentStr);
    }
}
