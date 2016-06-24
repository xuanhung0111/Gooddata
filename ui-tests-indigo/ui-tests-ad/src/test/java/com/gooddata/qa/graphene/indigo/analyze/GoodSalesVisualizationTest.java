package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricFormat;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeaderReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReportReact;

public class GoodSalesVisualizationTest extends GoodSalesAbstractAnalyseTest {

    private static final String EXPORT_ERROR_MESSAGE = "Insight is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to Open as Report.";

    private static final String PERCENT_OF_GOAL_URI = "/gdc/md/%s/obj/8136";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Visualization-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testWithAttribute() {
        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPageReact.changeReportType(ReportType.BAR_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPageReact.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        TableReportReact report = analysisPageReact.changeReportType(ReportType.TABLE).getTableReport();
        assertThat(report.getHeaders().stream().map(String::toLowerCase).collect(toList()),
                equalTo(asList(ATTR_ACTIVITY_TYPE.toLowerCase())));
        checkingOpenAsReport("testWithAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void testResetFunction() {
        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertThat(report.getTrackersCount(), equalTo(4));

        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void disableExportForUnexportableVisualization() {
        final AnalysisPageHeaderReact pageHeader = analysisPageReact.getPageHeader();
        ChartReportReact report = analysisPageReact.addMetric(METRIC_AMOUNT).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(pageHeader.isExportButtonEnabled());

        analysisPageReact.addAttribute(ATTR_STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPageReact.addStack(ATTR_STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(pageHeader.isExportButtonEnabled());
        assertEquals(pageHeader.getExportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"init"})
    public void resetSpecialReports() {
        analysisPageReact.resetToBlankState();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACCOUNT).waitForReportComputing();
        assertTrue(analysisPageReact.isExplorerMessageVisible());
        assertEquals(analysisPageReact.getExplorerMessage(), "Too many data points to display");
        analysisPageReact.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6401")
    public void gridlinesShouldBeCheckedWhenExportBarChart() {
        analysisPageReact.addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_STAGE_NAME)
                .changeReportType(ReportType.BAR_CHART)
                .waitForReportComputing()
                .exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        checkRedBar(browser);

        reportPage.showConfiguration();
        waitForElementVisible(By.cssSelector(".globalSettings .btnSilver"), browser).click();
        WebElement gridlines = waitForElementVisible(
                By.xpath("//input[./following-sibling::*[@title='Gridlines']]"), browser);
        assertTrue(gridlines.isSelected());

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricData() throws ParseException, JSONException, IOException {
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(METRIC_PERCENT_OF_GOAL);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();

        String uri = format(PERCENT_OF_GOAL_URI, testParams.getProjectId());
        changeMetricFormat(getRestApiClient(), uri, "<script> alert('test'); </script> #,##0.00");

        try {
            initAnalysePage();
            analysisPageReact.addMetric(METRIC_PERCENT_OF_GOAL)
                  .addAttribute(ATTR_IS_WON)
                  .addStack(ATTR_IS_WON)
                  .waitForReportComputing();
            ChartReportReact report = analysisPageReact.getChartReport();
            assertTrue(report.getTrackersCount() >= 1);
            assertEquals(report.getLegends(), asList("true"));

            assertEquals(report.getTooltipTextOnTrackerByIndex(0),
                    asList(asList(ATTR_IS_WON, "true"), asList("true", "<script> alert('test')")));
        } finally {
            changeMetricFormat(getRestApiClient(), uri, oldFormat);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void exportCustomDiscovery() {
        assertTrue(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .getPageHeader()
                .isExportButtonEnabled());
        TableReportReact analysisReport = analysisPageReact.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPageReact.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        sleepTight(2000); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(asList(attributes.next(), metrics.next()));
        }

        assertThat(content, equalTo(analysisContent));

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertThat(reportheaders.next().toLowerCase(), equalTo(analysisHeaders.next().toLowerCase()));
        }
        checkRedBar(browser);

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void exportVisualizationWithOneAttributeInChart() {
        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE).getExplorerMessage(),
                "Now select a measure to display");
        assertFalse(analysisPageReact.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportHasOneMetricManyAttributes() {
        analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPageReact.changeReportType(type);
                takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPageReact.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
                assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
                assertEquals(analysisPageReact.getMetricsBucket().getWarningMessage(), type.getMetricMessage());
                analysisPageReact.undo();
        });

        analysisPageReact.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-backToTable", getClass());
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportHasManyMetricsManyAttributes() {
        analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_QUOTA)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPageReact.changeReportType(type);
                takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
                assertEquals(analysisPageReact.getStacksBucket().getWarningMessage(), type.getStackByMessage());
                analysisPageReact.undo();
        });

        analysisPageReact.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-backToTable", getClass());
        assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportWithDateAttributes() {
        final StacksBucket stacksBucket = analysisPageReact.getStacksBucket();
        final AttributesBucketReact categoriesBucket = analysisPageReact.getAttributesBucket();

        analysisPageReact.changeReportType(ReportType.TABLE)
            .addDate()
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPageReact.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-firstDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
                assertEquals(categoriesBucket.getItemNames(), asList(DATE));
                analysisPageReact.undo();
        });

        analysisPageReact.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addDate()
            .addAttribute(ATTR_DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPageReact.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), asList(ATTR_ACTIVITY_TYPE));
                analysisPageReact.undo();
        });

        analysisPageReact.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), asList(DATE));

        analysisPageReact.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addDate();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPageReact.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), asList(ATTR_ACTIVITY_TYPE));
                analysisPageReact.undo();
        });

        analysisPageReact.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), asList(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void addStackByIfMoreThanOneMetricInReport() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric("Best Case").addAttribute("Region");

        final StacksBucket stacksBucket = analysisPageReact.getStacksBucket();
        assertTrue(stacksBucket.isDisabled());
        assertEquals(stacksBucket.getWarningMessage(), "TO STACK BY, AN INSIGHT CAN HAVE ONLY ONE MEASURE");
    }

    @Test(dependsOnGroups = {"init"})
    public void addSecondMetricIfAttributeInStackBy() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_DEPARTMENT);
        assertEquals(analysisPageReact.getMetricsBucket().getWarningMessage(), "TO ADD ADDITIONAL MEASURE, REMOVE FROM STACK BY");
    }

    @Test(dependsOnGroups = {"init"})
    public void createChartReportWithMoreThan3Metrics() {
        List<String> legends = analysisPageReact.addMetric(METRIC_NUMBER_OF_LOST_OPPS)
                .addMetric(METRIC_NUMBER_OF_OPEN_OPPS)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addMetric(METRIC_NUMBER_OF_WON_OPPS)
                .waitForReportComputing()
                .getChartReport()
                .getLegends();
        assertEquals(legends, asList(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_OPPORTUNITIES,
                METRIC_NUMBER_OF_WON_OPPS));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-chartReport");

        List<String> headers = analysisPageReact.changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS,
                METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
    }
}
