package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.fixture.utils.GoodSales.Reports;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.report.ReportRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITY_LEVEL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_NO_DATA;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BulkPermissionReportSetting extends AbstractEmbeddedModeTest {

    private ReportRestRequest reportRequest;

    @Override
    protected void customizeProject() throws Throwable {
        reportRequest = new ReportRestRequest(getAdminRestClient(), testParams.getProjectId());
        Reports reports = getReportCreator();
        reports.createAmountByStageNameReport();
        reports.createEmptyReport();
        reports.createAmountByProductReport();
        reports.createActiveLevelReport();

        reportRequest.setPrivateReport(REPORT_AMOUNT_BY_PRODUCT, true);
        reportRequest.setLockedReport(REPORT_ACTIVITY_LEVEL, true);

        reportRequest.setPrivateReport(REPORT_NO_DATA, true);
        reportRequest.setLockedReport(REPORT_NO_DATA, true);
    }

    @DataProvider(name = "privateReport")
    public Object[][] getPrivateReport() {
        return new Object[][] {
                {REPORT_AMOUNT_BY_PRODUCT},
                {REPORT_NO_DATA}
        };
    }
    @Test(dependsOnGroups = "createProject", dataProvider = "privateReport")
    public void mixedVisibilitySetting(String privateReport) throws IOException {
        ReportsPage reportsPage = initReportsPage();
        PermissionSettingDialog permissionSettingDialog = reportsPage
                .selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, privateReport);
        assertEquals(permissionSettingDialog.getVisibilityDescription(), "Mixed selection (change)");
        assertEquals(permissionSettingDialog.getVisibilityLinkText(), "change");

        permissionSettingDialog.save();
        try {
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isPrivateReport(privateReport), "Report shouldn't change");
            assertFalse(reportsPage.isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME), "Report shouldn't change");

            reportsPage.selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, privateReport)
                    .clickVisibilityLink().save();
            waitForFragmentNotVisible(permissionSettingDialog);
            reportsPage.waitForListReportLoading();
            Screenshots.takeScreenshot(browser, "mixed visibility Setting with " + privateReport, getClass());
            assertFalse(reportsPage.isPrivateReport(privateReport), "Report should be public");
            assertFalse(reportsPage.isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be public");

            reportRequest.setPrivateReport(privateReport, true);
            initReportsPage().selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, privateReport)
                    .clickVisibilityLink().setVisibility(false).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isPrivateReport(privateReport), "Report should be private");
            assertTrue(reportsPage.isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be private");
        } finally {
            reportRequest.setPrivateReport(privateReport, true);
            reportRequest.setPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME, false);
        }
    }

    @DataProvider(name = "lockedReport")
    public Object[][] getLockedReport() {
        return new Object[][] {
                {REPORT_ACTIVITY_LEVEL},
                {REPORT_NO_DATA}
        };
    }
    @Test(dependsOnGroups = "createProject", dataProvider = "lockedReport")
    public void mixedLockSetting(String lockedReport) throws IOException {
        ReportsPage reportsPage = initReportsPage();
        PermissionSettingDialog permissionSettingDialog = reportsPage
                .selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, lockedReport);
        assertTrue(permissionSettingDialog.getEditDescriptions().contains("No change (mixed selection)"),
                "Mixed selection should display");
        assertEquals(permissionSettingDialog.getSelectedEditSection(), PermissionType.MIXED);
        permissionSettingDialog.save();
        try {
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isReportLocked(lockedReport), "Report shouldn't change");
            assertFalse(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Report shouldn't change");

            reportsPage.selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, lockedReport)
                    .setEditingPermission(PermissionType.ALL).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            reportsPage.waitForListReportLoading();
            Screenshots.takeScreenshot(browser, "mixed lock setting with " + lockedReport, getClass());
            assertFalse(reportsPage.isReportLocked(lockedReport), "Report should be not locked");
            assertFalse(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be not locked");

            reportRequest.setLockedReport(lockedReport, true);
            initReportsPage().selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, lockedReport)
                    .setEditingPermission(PermissionType.ADMIN).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isReportLocked(lockedReport), "Report should be locked");
            assertTrue(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be locked");
        } finally {
            reportRequest.setLockedReport(lockedReport, true);
            reportRequest.setLockedReport(REPORT_AMOUNT_BY_STAGE_NAME, false);
        }
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "privateReport")
    public void mixedVisibilitySettingOnEmbeddedMode(String privateReport) throws IOException {
        embeddedUri = initDashboardsPage().addNewDashboard(generateHashString())
                .openEmbedDashboardDialog().getPreviewURI();
        ReportsPage reportsPage = initEmbeddedDashboard().openEmbeddedReportsPage();
        PermissionSettingDialog permissionSettingDialog = reportsPage
                .selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, privateReport);
        assertEquals(permissionSettingDialog.getVisibilityDescription(), "Mixed selection (change)");
        assertEquals(permissionSettingDialog.getVisibilityLinkText(), "change");

        permissionSettingDialog.save();
        try {
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isPrivateReport(privateReport), "Report shouldn't change");
            assertFalse(reportsPage.isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME), "Report shouldn't change");

            reportsPage.selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, privateReport)
                    .clickVisibilityLink().save();
            waitForFragmentNotVisible(permissionSettingDialog);
            Screenshots.takeScreenshot(browser,
                    "mixed visibility setting on embedded mode with " + privateReport, getClass());
            reportsPage.waitForListReportLoading();
            assertFalse(reportsPage.isPrivateReport(privateReport), "Report should be public");
            assertFalse(reportsPage.isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be public");

            reportRequest.setPrivateReport(privateReport, true);
            browser.navigate().refresh();
            waitForFragmentVisible(reportsPage).waitForListReportLoading()
                    .selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, privateReport)
                    .clickVisibilityLink().setVisibility(false).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isPrivateReport(privateReport), "Report should be private");
            assertTrue(reportsPage.isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be private");
        } finally {
            reportRequest.setPrivateReport(privateReport, true);
            reportRequest.setPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME, false);
        }
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "lockedReport")
    public void mixedLockSettingOnEmbeddedMode(String lockedReport) throws IOException {
        embeddedUri = initDashboardsPage().addNewDashboard(generateHashString())
                .openEmbedDashboardDialog().getPreviewURI();
        ReportsPage reportsPage = initEmbeddedDashboard().openEmbeddedReportsPage();
        PermissionSettingDialog permissionSettingDialog = reportsPage
                .selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, lockedReport);
        assertTrue(permissionSettingDialog.getEditDescriptions().contains("No change (mixed selection)"),
                "Mixed selection should display");
        assertEquals(permissionSettingDialog.getSelectedEditSection(), PermissionType.MIXED);
        permissionSettingDialog.save();
        try {
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isReportLocked(lockedReport), "Report shouldn't change");
            assertFalse(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Report shouldn't change");

            reportsPage.selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, lockedReport)
                    .setEditingPermission(PermissionType.ALL).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            reportsPage.waitForListReportLoading();
            Screenshots.takeScreenshot(browser,
                    "mixed lock setting on embedded mode with " + lockedReport, getClass());
            assertFalse(reportsPage.isReportLocked(lockedReport), "Report should be not locked");
            assertFalse(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be not locked");

            reportRequest.setLockedReport(lockedReport, true);
            browser.navigate().refresh();
            waitForFragmentVisible(reportsPage)
                    .selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME, lockedReport)
                    .setEditingPermission(PermissionType.ADMIN).save();
            waitForFragmentNotVisible(permissionSettingDialog);
            assertTrue(reportsPage.waitForListReportLoading().isReportLocked(lockedReport), "Report should be locked");
            assertTrue(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Report should be locked");
        } finally {
            reportRequest.setLockedReport(lockedReport, true);
            reportRequest.setLockedReport(REPORT_AMOUNT_BY_STAGE_NAME, false);
        }
    }
}
