package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.google.common.collect.Lists;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class GoodSalesTableReportTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Table-Report-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createNumberOfOpportunitiesMetric();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createNumberOfLostOppsMetric();
        metricCreator.createNumberOfOpenOppsMetric();
        metricCreator.createNumberOfWonOppsMetric();
        metricCreator.createQuotaMetric();
        metricCreator.createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createTableReportWithMoreThan3Metrics() {
        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .addMetric(METRIC_NUMBER_OF_OPEN_OPPS)
            .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
            .addMetric(METRIC_NUMBER_OF_WON_OPPS)
            .addAttribute(ATTR_DEPARTMENT)
            .addAttribute(ATTR_PRODUCT)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(ATTR_DEPARTMENT, ATTR_PRODUCT, METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS,
                METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
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

    @Test(dependsOnGroups = {"createProject"})
    public void checkReportContentWhenAdd3Metrics1Attribute() {
        TableReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_QUOTA)
                .addMetric(METRIC_SNAPSHOT_BOP)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .getTableReport();
        sleepTight(3000);
        List<List<String>> analysisContent = report.getContent();

        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);

        try {
            waitForAnalysisPageLoaded(browser);
            assertEquals(reportPage.getTableReport().getDataContent(), analysisContent);
            checkRedBar(browser);
        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportWithManyAttributes() {
        List<List<String>> adReportContent = analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterReportIncludeManyAttributes() {
        analysisPage.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email")
            .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");

        List<List<String>> adReportContent = analysisPage.waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void orderDataInTableReport() {
        List<List<String>> content = sortReportBaseOnHeader(
                analysisPage.changeReportType(ReportType.TABLE)
                    .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .waitForReportComputing()
                    .getTableReport(),
                METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("154,271")));

        content = sortReportBaseOnHeader(
                analysisPage.addMetric(METRIC_QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                METRIC_QUOTA);
        assertEquals(content, asList(asList("154,271", "$3,300,000")));

        content = sortReportBaseOnHeader(
                analysisPage.resetToBlankState()
                    .changeReportType(ReportType.TABLE)
                    .addAttribute(ATTR_ACTIVITY_TYPE)
                    .waitForReportComputing()
                    .getTableReport(),
                ATTR_ACTIVITY_TYPE);
        assertEquals(content, asList(asList("Web Meeting"), asList("Phone Call"), asList("In Person Meeting"),
                asList("Email")));

        content = sortReportBaseOnHeader(
                analysisPage.addAttribute(ATTR_DEPARTMENT)
                    .waitForReportComputing()
                    .getTableReport(),
                ATTR_DEPARTMENT);
        assertEquals(content, asList(asList("Email", "Direct Sales"), asList("In Person Meeting", "Direct Sales"),
                asList("Phone Call", "Direct Sales"), asList("Web Meeting", "Direct Sales"),
                asList("Email", "Inside Sales"), asList("In Person Meeting", "Inside Sales"),
                asList("Phone Call", "Inside Sales"), asList("Web Meeting", "Inside Sales")));

        content = sortReportBaseOnHeader(
                analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addMetric(METRIC_QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("Phone Call", "Direct Sales", "33,420", "$3,300,000"),
                asList("Web Meeting", "Direct Sales", "23,931", "$3,300,000"),
                asList("In Person Meeting", "Direct Sales", "22,088", "$3,300,000"),
                asList("Email", "Direct Sales", "21,615", "$3,300,000"),
                asList("Phone Call", "Inside Sales", "17,360", "$3,300,000"),
                asList("In Person Meeting", "Inside Sales", "13,887", "$3,300,000"),
                asList("Email", "Inside Sales", "12,305", "$3,300,000"),
                asList("Web Meeting", "Inside Sales", "9,665", "$3,300,000")));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFormat() throws ParseException, JSONException, IOException {
        String oldFormat = initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES)
                .getMetricFormat();
        String metricUri = getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES).getUri();
        DashboardRestRequest dashboardRequest = new DashboardRestRequest(
                getAdminRestClient(), testParams.getProjectId());
        dashboardRequest.changeMetricFormat(metricUri, oldFormat + "[red]");

        try {
            initAnalysePage();

            com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport tableReport =
                    analysisPage.changeReportType(ReportType.TABLE)
                        .addMetric(METRIC_NUMBER_OF_ACTIVITIES).getTableReport();
            assertEquals(tableReport.getFormatFromValue(), "color: rgb(255, 0, 0);");
        } finally {
            dashboardRequest.changeMetricFormat(metricUri, oldFormat);
            assertEquals(initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES)
                    .getMetricFormat(), oldFormat);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void makeSureReportRenderWhenSortingTabular() {
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);

        assertFalse(analysisPage.waitForReportComputing()
            .isExplorerMessageVisible());
    }

    private List<List<String>> getTableContentFromReportPage(
            com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport) {
        List<List<String>> content = Lists.newArrayList();
        List<String> attributes = tableReport.getAttributeValues();
        List<String> metrics = tableReport.getRawMetricValues();
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
            return reportPage.getTableReport().getDataContent();
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }
}
