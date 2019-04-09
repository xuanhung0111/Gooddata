package com.gooddata.qa.graphene.indigo.analyze.common;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket.Type;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.tagName;

public class AbstractEventingTest extends AbstractAnalyseTest {
    protected IndigoRestRequest indigoRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();

        Metrics metrics = getMetricCreator();
        metrics.createNumberOfActivitiesMetric();
        metrics.createAmountMetric();
        metrics.createNumberOfOpportunitiesMetric();

        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());

        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        // TODO: BB-1448 enablePivot FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_PIVOT_TABLE, true);
    }

    protected Pair<Integer, Integer> getColumnPosition(ChartReport chartReport, String legend, String attributeValue) {
        int row = chartReport.getLegendIndex(legend);
        List<String> labels = chartReport.getXaxisLabels();
        int col = labels.indexOf(attributeValue);
        return Pair.of(row, col);
    }

    protected Pair<Integer, Integer> getColumnPosition(ChartReport chartReport, String legend) {
        int row = chartReport.getLegendIndex(legend);
        return Pair.of(row, 0);
    }

    protected String createTemplateHtmlFile(String insightObjectId, String uris, String identifiers) throws IOException {
        final String content = ResourceUtils.getResourceAsString("/eventing/frame_AD.html");
        final String replacedContent = content
                .replace("{host}", testParams.getHost())
                .replace("{project}", testParams.getProjectId())
                .replace("{reportId}", insightObjectId)
                .replace("{uris}", uris)
                .replace("{identifiers}", identifiers);
        return ResourceUtils.createTempFileFromString(replacedContent);
    }

    protected String createTemplateHtmlFile(String insightObjectId) throws IOException {
        return createTemplateHtmlFile(insightObjectId, "[]", "[]");
    }

    protected String createTemplateHtmlFile(String insightObjectId, String uris) throws IOException {
        return createTemplateHtmlFile(insightObjectId, uris, "[]");
    }

    protected EmbeddedAnalysisPage openEmbeddedPage(final String url) {
        browser.get("file://" + url);
        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
        return EmbeddedAnalysisPage.getInstance(browser);
    }

    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
        browser.switchTo().frame("iframe");
    }

    protected void setDrillableItems(String... uris) {
        browser.switchTo().defaultContent();
        WebElement urisElement = waitForElementVisible(By.id("uris"), browser);
        urisElement.sendKeys(String.join(";", uris));
        waitForElementVisible(By.id("setDrillItemsBtn"), browser).click();
        try {
            //wait for the role text is updated
            Function<WebDriver, Boolean> waitUntil = browser -> urisElement.getText().trim().isEmpty();
            Graphene.waitGui(browser).withTimeout(10, TimeUnit.SECONDS).until(waitUntil);
        } catch (TimeoutException e) {
            //ignore TimeoutException
        }
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

    protected String getObjectIdFromUri(String uri) {
        return uri.split("/obj/")[1];
    }

    protected String getObjectIdFromBrowserUrl() {
        return browser.getCurrentUrl().split(testParams.getProjectId() + "/")[1];
    }

    protected String createInsight(String title, ReportType reportType,
                                   List<String> metrics, List<String> attributes, String stack) {
        InsightMDConfiguration insightMDConfiguration = new InsightMDConfiguration(title, reportType);
        List<MeasureBucket> measureBuckets = metrics.stream()
                .map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))
                .collect(Collectors.toList());
        insightMDConfiguration.setMeasureBucket(measureBuckets);

        List<CategoryBucket> categoryBuckets = attributes.stream()
                .map(attribute -> CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), Type.VIEW))
                .collect(Collectors.toList());
        if (StringUtils.isNotEmpty(stack)) {
            categoryBuckets.add(CategoryBucket.createCategoryBucket(getAttributeByTitle(stack), Type.STACK));
        }
        insightMDConfiguration.setCategoryBucket(categoryBuckets);
        return indigoRestRequest.createInsight(insightMDConfiguration);
    }

    protected String createInsight(String title, ReportType reportType,
                                   List<String> metrics, List<String> attributes) {
        return createInsight(title, reportType, metrics, attributes, "");
    }

    protected String createSimpleInsight(String title, ReportType reportType, String metric, String attribute,
                                         String stack) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(singletonList(MeasureBucket
                                .createSimpleMeasureBucket(getMetricByTitle(metric))))
                        .setCategoryBucket(Arrays.asList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute), Type.ATTRIBUTE),
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(stack), Type.STACK))));
    }

    protected String createSimpleInsightWithPercentation(String title, ReportType reportType,
                                                         String metric, String attribute) {
        return indigoRestRequest.createInsight(
                new InsightMDConfiguration(title, reportType)
                        .setMeasureBucket(singletonList(MeasureBucket.createMeasureBucketWithShowInPercent(
                                getMetricByTitle(metric), true)))
                        .setCategoryBucket(singletonList(
                                CategoryBucket.createCategoryBucket(getAttributeByTitle(attribute),
                                        Type.VIEW))));
    }

    protected JSONObject getLatestPostMessageObj() {
        String contentStr = getLoggerContent();
        log.info(contentStr);
        return new JSONObject(contentStr);
    }
}
