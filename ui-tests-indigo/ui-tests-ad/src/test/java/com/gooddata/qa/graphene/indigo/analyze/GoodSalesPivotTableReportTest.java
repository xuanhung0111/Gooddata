package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
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
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class GoodSalesPivotTableReportTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Pivot-Table-Report-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
            new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        // TODO: BB-1448 enablePivot FF should be removed
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_PIVOT_TABLE, true);

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
    public void createReportWithMoreThan3Metrics() {
        analysisPage = initAnalysePage().changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .addMetric(METRIC_NUMBER_OF_OPEN_OPPS)
            .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
            .addMetric(METRIC_NUMBER_OF_WON_OPPS)
            .addAttribute(ATTR_DEPARTMENT)
            .addAttribute(ATTR_PRODUCT)
            .waitForReportComputing();

        List<String> headers = analysisPage.getPivotTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());

        assertEquals(headers, Stream.of(ATTR_DEPARTMENT, ATTR_PRODUCT, METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS,
            METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-pivotTableReport");

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "createReportWithMoreThan3Metrics-switchFromTableTo-" + type.name(),
                    getClass());
                assertFalse(analysisPage.waitForReportComputing().isExplorerMessageVisible(),
                    "Explorer message shouldn't be visible");
                analysisPage.undo();
            });
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkContentWhenAdd3Metrics1Attribute() {
        PivotTableReport report = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_QUOTA)
            .addMetric(METRIC_SNAPSHOT_BOP)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getPivotTableReport();
        sleepTight(3000);

        List<List<String>> expectedValues = asList(
            asList("Email", "33,920", "$3,300,000", "40,334"),
            asList("In Person Meeting", "35,975", "$3,300,000", "40,334"),
            asList("Phone Call", "50,780", "$3,300,000", "40,334"),
            asList("Web Meeting", "33,596", "$3,300,000", "40,334")
        );

        assertEquals(report.getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createReportWithManyAttributes() {
        List<List<String>> adReportContent = initAnalysePage().changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getPivotTableReport()
            .getBodyContent();

        List<List<String>> expectedValues = asList(
            asList("Email", "Direct Sales", "21,615"),
            asList("Email", "Inside Sales", "12,305"),
            asList("In Person Meeting", "Direct Sales", "22,088"),
            asList("In Person Meeting", "Inside Sales", "13,887"),
            asList("Phone Call", "Direct Sales", "33,420"),
            asList("Phone Call", "Inside Sales", "17,360"),
            asList("Web Meeting", "Direct Sales", "23,931"),
            asList("Web Meeting", "Inside Sales", "9,665")
        );

        assertEquals(adReportContent, expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterReportIncludeManyAttributes() {
        initAnalysePage().changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email")
            .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");

        List<List<String>> adReportContent = analysisPage.waitForReportComputing()
            .getPivotTableReport()
            .getBodyContent();

        List<List<String>> expectedValues = singletonList(
            asList("Email", "Direct Sales", "21,615")
        );

        assertEquals(adReportContent, expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void orderDataInReport() {
        List<List<String>> content = sortReportBaseOnHeader(
            initAnalysePage().changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getPivotTableReport(),
            METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("154,271")));

        content = sortReportBaseOnHeader(
            analysisPage.addMetric(METRIC_QUOTA)
                .waitForReportComputing()
                .getPivotTableReport(),
            METRIC_QUOTA);
        assertEquals(content, asList(asList("154,271", "$3,300,000")));

        content = sortReportBaseOnHeader(
            analysisPage.resetToBlankState()
                .changeReportType(ReportType.TABLE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getPivotTableReport(),
            ATTR_ACTIVITY_TYPE);
        assertEquals(content, asList(asList("Web Meeting"), asList("Phone Call"), asList("In Person Meeting"),
            asList("Email")));

        content = sortReportBaseOnHeader(
            analysisPage.addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing()
                .getPivotTableReport(),
            ATTR_DEPARTMENT);
        assertEquals(content, asList(asList("Email", "Direct Sales"), asList("In Person Meeting", "Direct Sales"),
            asList("Phone Call", "Direct Sales"), asList("Web Meeting", "Direct Sales"),
            asList("Email", "Inside Sales"), asList("In Person Meeting", "Inside Sales"),
            asList("Phone Call", "Inside Sales"), asList("Web Meeting", "Inside Sales")));

        content = sortReportBaseOnHeader(
            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_QUOTA)
                .waitForReportComputing()
                .getPivotTableReport(),
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

            PivotTableReport pivotTableReport =
                analysisPage.changeReportType(ReportType.TABLE)
                    .addMetric(METRIC_NUMBER_OF_ACTIVITIES).getPivotTableReport();

            WebElement attributeElement = pivotTableReport.getCellElement(METRIC_NUMBER_OF_ACTIVITIES, 0);
            String attributeStyle = attributeElement.getAttribute("style");
            assertEquals(attributeStyle, "width: 200px; left: 0px; color: rgb(255, 0, 0);");
        } finally {
            dashboardRequest.changeMetricFormat(metricUri, oldFormat);
            assertEquals(initMetricPage().openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES)
                .getMetricFormat(), oldFormat);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void makeSureReportRenderWhenSortingTabular() {
        initAnalysePage().addMetric(FACT_AMOUNT, FieldType.FACT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getPivotTableReport()
            .sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);

        assertFalse(analysisPage.waitForReportComputing().isExplorerMessageVisible(),
            "Explorer message shouldn't be visible");
    }

    private List<List<String>> sortReportBaseOnHeader(PivotTableReport report, String name) {
        report.sortBaseOnHeader(name);
        analysisPage.waitForReportComputing();
        return report.getBodyContent();
    }
}
