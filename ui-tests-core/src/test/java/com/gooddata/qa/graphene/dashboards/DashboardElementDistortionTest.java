package com.gooddata.qa.graphene.dashboards;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.dashboard.ExportDashboardDefinition;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.csvuploader.Dataset;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.utils.UrlParserUtils;
import com.gooddata.qa.utils.PdfUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertFalse;

public class DashboardElementDistortionTest extends AbstractProjectTest {
    private DashboardRestRequest dashboardRequest;
    private ProjectRestRequest projectRestRequest;
    private String firsrDashboard;
    private String secondDashboard;

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle += "Dashboard Element Distortion Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        ExportDashboardDefinition exportDashboardDefinition = new ExportDashboardDefinition()
                .setDashboardName(" ").setTabName(" ").setProjectName(" ").setReportName(" ");
        dashboardRequest.exportDashboardSetting(exportDashboardDefinition);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDashBoardHasElementDistortion() throws IOException {
        setFeatureFlagAccurate(true);
        try {
            CsvFile csvFile = elementDistortionCsv()
                    .rows(asList("1", "Chi1", "Le", "Le Thi Phuong Chi", "Binh Duong", "19108000", "169238", "2018-01-01"))
                    .rows(asList("2", "Huong", "Nguyen", "Nguyen Thi Thu Huong", "DOng Nai", "23228705", "75423", "2019-01-01"));
            String filePath = csvFile.saveToDisc(testParams.getCsvFolder());

            initDataUploadPage().uploadFile(filePath)
                    .triggerIntegration();
            Dataset.waitForDatasetLoaded(browser);

            createMetric("Metric1 [Sum]",
                    format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Metric1"))),
                    DEFAULT_METRIC_FORMAT);

            createMetric("Metric2 [Sum]",
                    format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Metric2"))),
                    DEFAULT_METRIC_FORMAT);

            createReport("CL-13501", ReportTypes.DONUT);

            createDashboard("DashBoard Has Distortion Reports");
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard("CL-13501");
            waitForOpeningIndigoDashboard();
            takeScreenshot(browser, "DashBoard Has Distortion Reports", getClass());
            dashboardEditBar.saveDashboard();
            dashboardsPage.renameTab(0, "firstDashboard").saveDashboard();
            dashboardRequest.adjustReportSize(UrlParserUtils.getObjId(browser.getCurrentUrl()), 300, 300);
            firsrDashboard = dashboardsPage.exportDashboardTabToPDF();

            takeScreenshot(browser, "firsrDashboard", getClass());
            initDashboardsPage().selectDashboard("DashBoard Has Distortion Reports")
                    .renameTab(0, "secondDashboard").saveDashboard();

            setFeatureFlagAccurate(false);

            secondDashboard = dashboardsPage.exportDashboardTabToPDF();
            takeScreenshot(browser, "secondDashboard", getClass());

            File firstPDFExport = new File(testParams.getExportFilePath(firsrDashboard));
            File secondPDFExport = new File(testParams.getExportFilePath(secondDashboard));
            assertFalse(PdfUtils.comparePDF(firstPDFExport.getPath(), secondPDFExport.getPath()), "Dashboards has report chart is distortion");
        } finally {
            setFeatureFlagAccurate(true);
        }
    }

    private static CsvFile elementDistortionCsv() {
        return new CsvFile("Distortion").columns(new CsvFile.Column("user"),
                new CsvFile.Column("firstname"), new CsvFile.Column("lastname"),
                new CsvFile.Column("name"), new CsvFile.Column("address"), new CsvFile.Column("metric1"),
                new CsvFile.Column("metric2"), new CsvFile.Column("Date"));
    }

    private void createReport(String reportName, ReportTypes reportTypes) {
        initReportsPage().startCreateReport().initPage()
                .openWhatPanel().selectItems("Metric1 [Sum]", "Metric2 [Sum]").done();
        reportPage.waitForReportExecutionProgress();
        reportPage.openHowPanel().selectItems("Name").done();
        reportPage.waitForReportExecutionProgress();
        reportPage.selectReportVisualisation(reportTypes).setReportName(reportName)
                .finishCreateReport().waitForReportExecutionProgress();
        takeScreenshot(browser, reportName, getClass());
    }

    private void setFeatureFlagAccurate(Boolean value) {
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ACCURATE_PIE_CHART, value);
    }
}
