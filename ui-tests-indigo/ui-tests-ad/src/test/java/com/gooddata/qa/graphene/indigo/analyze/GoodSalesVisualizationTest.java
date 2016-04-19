package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
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
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;

public class GoodSalesVisualizationTest extends AnalyticalDesignerAbstractTest {

    private static final String EXPORT_ERROR_MESSAGE = "Visualization is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to Open as Report.";

    private static final String PERCENT_OF_GOAL_URI = "/gdc/md/%s/obj/8136";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Visualization-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testWithAttribute() {
        initAnalysePage();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "Now select a measure to display");

        TableReport report = analysisPage.changeReportType(ReportType.TABLE).getTableReport();
        assertThat(report.getHeaders().stream().map(String::toLowerCase).collect(toList()),
                equalTo(asList(ACTIVITY_TYPE.toLowerCase())));
        checkingOpenAsReport("testWithAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void testResetFunction() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void disableExportForUnexportableVisualization() {
        initAnalysePage();
        final AnalysisPageHeader pageHeader = analysisPage.getPageHeader();
        ChartReport report = analysisPage.addMetric(AMOUNT).getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(pageHeader.isExportButtonEnabled());

        analysisPage.addAttribute(STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPage.addStack(STAGE_NAME);
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(pageHeader.isExportButtonEnabled());
        assertEquals(pageHeader.getExportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"init"})
    public void resetSpecialReports() {
        initAnalysePage();
        analysisPage.resetToBlankState();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACCOUNT).waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Too many data points to display");
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6401")
    public void gridlinesShouldBeCheckedWhenExportBarChart() {
        initAnalysePage();
        analysisPage.addMetric(AMOUNT)
                .addAttribute(STAGE_NAME)
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
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();

        String uri = format(PERCENT_OF_GOAL_URI, testParams.getProjectId());
        changeMetricFormat(getRestApiClient(), uri, "<script> alert('test'); </script> #,##0.00");

        try {
            initAnalysePage();
            analysisPage.addMetric(PERCENT_OF_GOAL)
                  .addAttribute(IS_WON)
                  .addStack(IS_WON)
                  .waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();
            assertTrue(report.getTrackersCount() >= 1);
            assertEquals(report.getLegends(), asList("true"));

            assertEquals(report.getTooltipTextOnTrackerByIndex(0),
                    asList(asList(IS_WON, "true"), asList("true", "<script> alert('test')")));
        } finally {
            changeMetricFormat(getRestApiClient(), uri, oldFormat);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void exportCustomDiscovery() {
        initAnalysePage();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .getPageHeader()
                .isExportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
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
        initAnalysePage();

        assertEquals(analysisPage.addAttribute(ACTIVITY_TYPE).getExplorerMessage(),
                "Now select a measure to display");
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportHasOneMetricManyAttributes() {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPage.getStacksBucket().getAttributeName(), DEPARTMENT);
                assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
                assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), type.getMetricMessage());
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-backToTable", getClass());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE, DEPARTMENT));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportHasManyMetricsManyAttributes() {
        initAnalysePage();
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(QUOTA)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
                assertEquals(analysisPage.getStacksBucket().getWarningMessage(), type.getStackByMessage());
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-backToTable", getClass());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchReportWithDateAttributes() {
        initAnalysePage();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();

        analysisPage.changeReportType(ReportType.TABLE)
            .addDate()
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-firstDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ACTIVITY_TYPE);
                assertEquals(categoriesBucket.getItemNames(), asList(DATE));
                analysisPage.undo();
        });

        analysisPage.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addDate()
            .addAttribute(DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), asList(ACTIVITY_TYPE));
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), asList(DATE));

        analysisPage.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addDate();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), asList(ACTIVITY_TYPE));
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), asList(DATE));
    }

    @Test(dependsOnGroups = {"init"})
    public void addStackByIfMoreThanOneMetricInReport() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addMetric("Best Case").addAttribute("Region");

        final StacksBucket stacksBucket = analysisPage.getStacksBucket();
        assertTrue(stacksBucket.isDisabled());
        assertEquals(stacksBucket.getWarningMessage(), "TO STACK BY, A VISUALIZATION CAN HAVE ONLY ONE MEASURE");
    }

    @Test(dependsOnGroups = {"init"})
    public void addSecondMetricIfAttributeInStackBy() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE).addStack(DEPARTMENT);
        assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), "TO ADD ADDITIONAL MEASURE, REMOVE FROM STACK BY");
    }

    @Test(dependsOnGroups = {"init"})
    public void createChartReportWithMoreThan3Metrics() {
        initAnalysePage();
        List<String> legends = analysisPage.addMetric(NUMBER_OF_LOST_OPPS)
                .addMetric(NUMBER_OF_OPEN_OPPS)
                .addMetric(NUMBER_OF_OPPORTUNITIES)
                .addMetric(NUMBER_OF_WON_OPPS)
                .waitForReportComputing()
                .getChartReport()
                .getLegends();
        assertEquals(legends, asList(NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS, NUMBER_OF_OPPORTUNITIES,
                NUMBER_OF_WON_OPPS));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-chartReport");

        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS,
                NUMBER_OF_OPPORTUNITIES, NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
    }
}
