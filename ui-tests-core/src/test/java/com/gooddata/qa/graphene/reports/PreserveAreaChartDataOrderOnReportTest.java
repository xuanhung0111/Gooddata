package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.dashboard.ExportDashboardDefinition;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.utils.PdfUtils;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertFalse;

public class PreserveAreaChartDataOrderOnReportTest extends GoodSalesAbstractTest {
    private ProjectRestRequest projectRestRequest;
    private DashboardRestRequest dashboardRequest;
    private String firstReport;
    private String secondReport;

    @Override
    protected void customizeProject() {
        getMetricCreator().createAmountMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        ExportDashboardDefinition exportDashboardDefinition = new ExportDashboardDefinition()
                .setDashboardName(" ").setTabName(" ").setProjectName(" ").setReportName(" ");
        dashboardRequest.exportDashboardSetting(exportDashboardDefinition);
    }

    @Test(dependsOnGroups = "createProject")
    protected void preserveAreaChartDataOrderOnReportTest() throws IOException {
        setFeatureFlagPreserveAreaChartDataOrder(true);
        try {
            initReportCreation().createReport(new UiReportDefinition()
                    .withName("preserveAreaChartDataOrder")
                    .withWhats("Amount")
                    .withHows(new HowItem("Product", HowItem.Position.TOP, "Grammar Plus", "PhoenixSoft", "WonderKid"))
                    .withHows(new HowItem("Stage Name", HowItem.Position.LEFT)));

            initReportsPage().openReport("preserveAreaChartDataOrder")
                    .selectReportVisualisation(ReportTypes.AREA).waitForReportExecutionProgress().saveReport();
            Screenshots.takeScreenshot(browser, "preserveAreaChartDataOrderTrue", getClass());
            firstReport = reportPage.exportReport(ExportFormat.PDF);

            setFeatureFlagPreserveAreaChartDataOrder(false);

            initReportsPage().openReport("preserveAreaChartDataOrder").waitForReportExecutionProgress()
                    .saveAsReport("preserveAreaChartDataOrderSecondReport")
                    .waitForReportExecutionProgress();
            Screenshots.takeScreenshot(browser, "preserveAreaChartDataOrderFalse", getClass());
            secondReport = reportPage.exportReport(ExportFormat.PDF);

            File firstPDFExport = new File(testParams.getExportFilePath(firstReport + ".pdf"));
            File secondPDFExport = new File(testParams.getExportFilePath(secondReport + ".pdf"));
            assertFalse(PdfUtils.comparePDF(firstPDFExport.getPath(), secondPDFExport.getPath()), "Area Chart data order are changed");

        } finally {
            setFeatureFlagPreserveAreaChartDataOrder(true);
        }
    }

    private void setFeatureFlagPreserveAreaChartDataOrder(boolean value) {
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.PRESERVE_AREA_CHART_DATA_ORDER, value);
    }
}
