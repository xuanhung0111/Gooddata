package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;

public class EmbeddedDashboardManagePageLocalizationTest extends GoodSalesAbstractLocalizationTest {

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-embeded-dashboard-manage-page-localization-test";
    }

    @Test(dependsOnMethods = {"createAndUsingTestUser"}, groups = {"precondition"})
    public void initEmbeddedDashboardUri() {
        embeddedUri = initDashboardsPage()
            .openEmbedDashboardDialog()
            .getPreviewURI()
            .replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkMoveDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .selectReportsAndOpenMoveDialog(REPORT_ACTIVITIES_BY_TYPE);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDeleteDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .selectReportsAndOpenDeleteDialog(REPORT_ACTIVITIES_BY_TYPE);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkPermissionDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .selectReportsAndOpenPermissionDialog(REPORT_ACTIVITIES_BY_TYPE);
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkFolderDialog() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .clickAddFolderButton();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkOpenExistingReport() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage()
            .getReportsList()
            .openReport(REPORT_ACTIVITIES_BY_TYPE);
        EmbeddedReportPage.waitForPageLoaded(browser);
        checkLocalization(browser);
    }
}
