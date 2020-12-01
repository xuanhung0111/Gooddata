package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.DateRange.ALL_TIME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.testng.Assert.assertFalse;

import com.gooddata.qa.graphene.enums.indigo.AggregationItem;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.attribute.AttributeRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public class ResizingColumnTest extends AbstractAnalyseTest {

    private static final String INSIGHT_TWO_COLUMNS = "Resize follow lowest header";
    private static final String INSIGHT_HAS_SUM = "Resize follow by the longest data";
    private AttributeRestRequest attributeRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Resizing Column Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        attributeRestRequest = new AttributeRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_TABLE_COLUMN_AUTO_RESIZING, true);
        getMetricCreator().createNumberOfOpportunitiesMetric();
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void autoResizeColumn() throws IOException {
        initAnalysePage().changeReportType(ReportType.TABLE).addColumnsAttribute(ATTR_STAGE_NAME).addColumnsAttribute(ATTR_IS_WON);
        PivotTableReport tableReport = analysisPage.waitForReportComputing().getPivotTableReport();
        isAutoResizingColumn(tableReport, "false", "Should auto resize %s follow by the lowest title");

        analysisPage.saveInsight(INSIGHT_TWO_COLUMNS);
        analysisPage.addMetric(METRIC_AMOUNT).addAttribute(ATTR_DEPARTMENT);
        PivotTableReport tableReportWithSum = analysisPage.waitForReportComputing().getPivotTableReport();
        tableReportWithSum.addTotal(AggregationItem.SUM, METRIC_AMOUNT, 0);
        analysisPage.waitForReportComputing();
        isAutoResizingColumn(tableReportWithSum, "Interest", "Should auto resize %s follow by the longest data");
        analysisPage.saveInsightAs(INSIGHT_HAS_SUM);

        analysisPage.removeMetric(METRIC_AMOUNT).removeAttribute(ATTR_DEPARTMENT);
        tableReport = analysisPage.waitForReportComputing().getPivotTableReport();
        isAutoResizingColumn(tableReport, "false", "Should auto resize %s after modifying insight");

        String longestTitle = "This is the longest title";
        attributeRestRequest.setAttributeName(ATTR_DEPARTMENT, longestTitle);
        try {
            initDashboardsPage(); //Refresh browser
            initAnalysePage().openInsight(INSIGHT_HAS_SUM);
            tableReport = analysisPage.waitForReportComputing().getPivotTableReport();
            isAutoResizingColumn(tableReport, longestTitle, "Should auto resize %s follow by new title");
        } finally {
            attributeRestRequest.setAttributeName(longestTitle, ATTR_DEPARTMENT);
        }
    }

    @Test(dependsOnMethods = {"autoResizeColumn"})
    public void autoResizeColumnOnEmbeddedMode() {
        initEmbeddedAnalysisPage().openInsight(INSIGHT_TWO_COLUMNS);
        PivotTableReport tableReport = analysisPage.waitForReportComputing().getPivotTableReport();
        isAutoResizingColumn(tableReport, "false", "Should auto resize %s follow by the lowest title");

        analysisPage.openInsight(INSIGHT_HAS_SUM);
        tableReport = analysisPage.waitForReportComputing().getPivotTableReport();
        isAutoResizingColumn(tableReport, "Interest", "Should auto resize %s follow by the longest data");
    }

    @Test(dependsOnMethods = {"autoResizeColumn"})
    public void autoResizeColumnOnDashboard() {
        String dashboardTitle = "Dashboard";
        IndigoDashboardsPage indigoDashboardsPage =
                initIndigoDashboardsPage().addDashboard().changeDashboardTitle(dashboardTitle).addInsight(INSIGHT_TWO_COLUMNS).waitForWidgetsLoading();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(ALL_TIME).apply();
        PivotTableReport tableReport = indigoDashboardsPage.getFirstWidget(Insight.class).getPivotTableReport();
        isAutoResizingColumn(tableReport, "false", "Should auto resize %s follow by the lowest title");

        indigoDashboardsPage.addInsightNext(INSIGHT_HAS_SUM).waitForWidgetsLoading();
        tableReport = indigoDashboardsPage.getLastWidget(Insight.class).getPivotTableReport();
        isAutoResizingColumn(tableReport, "false", "Should auto resize %s follow by the lowest title");

        indigoDashboardsPage.saveEditModeWithWidgets().exportDashboardToPDF();
        List<String> contents = asList(getContentFrom(dashboardTitle).split("\n"));
        takeScreenshot(browser, dashboardTitle, getClass());
        assertThat(contents, hasItems("false false false false false false true false",
                "$38,310,753.45 $42,470,571.16Sum $18,447,266.14 $4,249,027.88 $5,612,062.60 $2,606,293.46 $3,067,466.12 $1,862,015.73"));
    }

    private void isAutoResizingColumn(PivotTableReport report, String followResize, String failMessage) {
        waitForCollectionIsNotEmpty(report.getHeaders()).stream()
                .filter(header -> !header.equals(followResize) && !header.equals("Stage Name › Is Won?"))
                .forEach(header -> assertFalse(report.getHeaderElement(header,0).getSize().getWidth() >
                        report.getHeaderElement(followResize,0).getSize().getWidth(), format(failMessage, header)));
    }
}
