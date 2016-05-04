package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.collect.Lists;

public class GoodSalesTableReportTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Table-Report-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createTableReportWithMoreThan3Metrics() {
        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .addMetric(NUMBER_OF_OPEN_OPPS)
            .addMetric(NUMBER_OF_OPPORTUNITIES)
            .addMetric(NUMBER_OF_WON_OPPS)
            .addAttribute(DEPARTMENT)
            .addAttribute(PRODUCT)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(DEPARTMENT, PRODUCT, NUMBER_OF_LOST_OPPS, NUMBER_OF_OPEN_OPPS,
                NUMBER_OF_OPPORTUNITIES, NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-tableReport");

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "createReportWithMoreThan3Metrics-switchFromTableTo-" + type.name(),
                        getClass());
                assertFalse(analysisPage.waitForReportComputing()
                    .isExplorerMessageVisible());
                analysisPage.undo();
            });
    }

    @Test(dependsOnGroups = {"init"})
    public void checkReportContentWhenAdd3Metrics1Attribute() {
        TableReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addMetric(QUOTA)
                .addMetric(SNAPSHOT_BOP)
                .addAttribute(ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .getTableReport();
        sleepTight(3000);
        List<List<String>> analysisContent = report.getContent();

        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);

        assertEquals(analysisContent, getTableContentFromReportPage(Graphene.createPageFragment(
                com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                waitForElementVisible(id("gridContainerTab"), browser))));
        checkRedBar(browser);

        browser.close();
        BrowserUtils.switchToFirstTab(browser);
    }

    @Test(dependsOnGroups = {"init"})
    public void createReportWithManyAttributes() {
        List<List<String>> adReportContent = analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"init"})
    public void filterReportIncludeManyAttributes() {
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES);
        analysisPage.getFilterBuckets().configAttributeFilter(ACTIVITY_TYPE, "Email")
            .configAttributeFilter(DEPARTMENT, "Direct Sales");

        List<List<String>> adReportContent = analysisPage.waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"init"})
    public void orderDataInTableReport() {
        List<List<String>> content = sortReportBaseOnHeader(
                analysisPage.changeReportType(ReportType.TABLE)
                    .addMetric(NUMBER_OF_ACTIVITIES)
                    .waitForReportComputing()
                    .getTableReport(),
                NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("154,271")));

        content = sortReportBaseOnHeader(
                analysisPage.addMetric(QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                QUOTA);
        assertEquals(content, asList(asList("154,271", "$3,300,000")));

        content = sortReportBaseOnHeader(
                analysisPage.resetToBlankState()
                    .changeReportType(ReportType.TABLE)
                    .addAttribute(ACTIVITY_TYPE)
                    .waitForReportComputing()
                    .getTableReport(),
                ACTIVITY_TYPE);
        assertEquals(content, asList(asList("Web Meeting"), asList("Phone Call"), asList("In Person Meeting"),
                asList("Email")));

        content = sortReportBaseOnHeader(
                analysisPage.addAttribute(DEPARTMENT)
                    .waitForReportComputing()
                    .getTableReport(),
                DEPARTMENT);
        assertEquals(content, asList(asList("Email", "Direct Sales"), asList("In Person Meeting", "Direct Sales"),
                asList("Phone Call", "Direct Sales"), asList("Web Meeting", "Direct Sales"),
                asList("Email", "Inside Sales"), asList("In Person Meeting", "Inside Sales"),
                asList("Phone Call", "Inside Sales"), asList("Web Meeting", "Inside Sales")));

        content = sortReportBaseOnHeader(
                analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                    .addMetric(QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("Phone Call", "Direct Sales", "33,420", "$3,300,000"),
                asList("Web Meeting", "Direct Sales", "23,931", "$3,300,000"),
                asList("In Person Meeting", "Direct Sales", "22,088", "$3,300,000"),
                asList("Email", "Direct Sales", "21,615", "$3,300,000"),
                asList("Phone Call", "Inside Sales", "17,360", "$3,300,000"),
                asList("In Person Meeting", "Inside Sales", "13,887", "$3,300,000"),
                asList("Email", "Inside Sales", "12,305", "$3,300,000"),
                asList("Web Meeting", "Inside Sales", "9,665", "$3,300,000")));
    }

    @Test(dependsOnGroups = {"init"})
    public void testFormat() throws ParseException, JSONException, IOException {
        initMetricPage();

        waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();
        String metricUri = format("/gdc/md/%s/obj/14636", testParams.getProjectId());
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), metricUri, oldFormat + "[red]");

        try {
            initAnalysePage();

            com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport tableReport =
                    analysisPage.changeReportType(ReportType.TABLE)
                        .addMetric(NUMBER_OF_ACTIVITIES).getTableReport();
            assertEquals(tableReport.getFormatFromValue(), "color: rgb(255, 0, 0);");
        } finally {
            DashboardsRestUtils.changeMetricFormat(getRestApiClient(), metricUri, oldFormat);
            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(NUMBER_OF_ACTIVITIES);
            assertEquals(metricDetailPage.getMetricFormat(), oldFormat);
        }
    }

    private List<List<String>> getTableContentFromReportPage(
            com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport) {
        List<List<String>> content = Lists.newArrayList();
        List<String> attributes = tableReport.getAttributeElements();
        List<String> metrics = tableReport.getRawMetricElements();
        int totalAttributes = attributes.size();
        int i = 0;
        for (String attr: attributes) {
            List<String> row = Lists.newArrayList(attr);
            for (int k = i; k < metrics.size(); k += totalAttributes) {
                row.add(metrics.get(k));
            }
            content.add(row);
            i++;
        }

        return content;
    }

    private List<List<String>> sortReportBaseOnHeader(TableReport report, String name) {
        report.sortBaseOnHeader(name);
        analysisPage.waitForReportComputing();
        return report.getContent();
    }

    private List<List<String>> getTableReportContentInReportPage() {
        analysisPage.exportReport();

        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);

        try {
            com.gooddata.qa.graphene.fragments.reports.report.TableReport report = reportPage.getTableReport();
            List<List<String>> attributesByRow = report.getAttributeElementsByRow();
            List<String> metrics = report.getRawMetricElements();

            for (int i = 0; i < metrics.size(); i++) {
                attributesByRow.get(i).add(metrics.get(i));
            }

            return attributesByRow;
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }
}
