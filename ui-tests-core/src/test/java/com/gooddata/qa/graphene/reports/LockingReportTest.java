package com.gooddata.qa.graphene.reports;

import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.dashboards.ReportInfoViewPanel;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class LockingReportTest extends AbstractEmbeddedModeTest {

    private final static String ADMIN_HELP_TEXT = "Only Admins can modify this report.";
    private final static String VIEWER_HELP_TEXT = "View only. You don't have permission to edit this report.";

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Override
    protected void customizeProject() throws Throwable {
        getReportCreator().createAmountByStageNameReport();
        getMetricCreator().createWonMetric();
        initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME).setPermission(PermissionType.ADMIN);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkLockedReportByAdmin() {
        initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME);
        assertTrue(reportPage.isVisibleLockIcon(), "Lock icon should be displayed");
        assertEquals(reportPage.getTooltipFromLockIcon(), ADMIN_HELP_TEXT);
        reportPage.clickLockIcon();
    }

    @Test(dependsOnGroups = "createProject")
    public void checkLockedReportByEditor() {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME);
            assertTrue(reportPage.isVisibleLockIcon(), "Lock icon should be displayed");
            assertEquals(reportPage.getTooltipFromLockIcon(), VIEWER_HELP_TEXT);
            assertFalse(reportPage.isVisibleSaveButton());
            SimpleMenu simpleMenu = reportPage.openOptionsMenu();
            assertFalse(simpleMenu.contains("Setting"));
            assertFalse(simpleMenu.contains("Delete"));
            assertTrue(simpleMenu.contains("Save as..."));

            reportPage.openHowPanel().selectAttribtues(singletonList(new HowItem(ATTR_DEPARTMENT))).done();
            assertEquals(reportPage.waitForReportExecutionProgress().saveAs().getTextOnSaveButton(), "Save As");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void checkLockedEmbeddedReportByAdmin() {
        embeddedReportUri = initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME).openReportEmbedDialog().getEmbedUri();
        initEmbeddedReport().openReportInfoViewPanel().clickViewReportButton();

        switchToPopUpWindow(REPORT_AMOUNT_BY_STAGE_NAME);
        waitForFragmentVisible(reportPage);
        reportPage.waitForReportExecutionProgress();
        waitForAnalysisPageLoaded(browser);
        assertTrue(reportPage.isVisibleLockIcon(), "Lock icon should be displayed");
        assertEquals(reportPage.getTooltipFromLockIcon(), ADMIN_HELP_TEXT);
        reportPage.clickLockIcon();
    }

    @Test(dependsOnGroups = "createProject")
    public void checkLockedEmbeddedReportByEditor() {
        embeddedReportUri = initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME).openReportEmbedDialog().getEmbedUri();
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initEmbeddedReport().openReportInfoViewPanel().clickViewReportButton();
            switchToPopUpWindow(REPORT_AMOUNT_BY_STAGE_NAME);
            waitForFragmentVisible(reportPage);
            reportPage.waitForReportExecutionProgress();
            waitForAnalysisPageLoaded(browser);
            assertTrue(reportPage.isVisibleLockIcon(), "Lock icon should be displayed");
            assertEquals(reportPage.getTooltipFromLockIcon(), VIEWER_HELP_TEXT);
            assertFalse(reportPage.isVisibleSaveButton());

            SimpleMenu simpleMenu = reportPage.openOptionsMenu();
            assertFalse(simpleMenu.contains("Setting"));
            assertFalse(simpleMenu.contains("Delete"));
            assertTrue(simpleMenu.contains("Save as..."));
            assertFalse(reportPage.openWhatPanel().selectMetrics(singletonList(new WhatItem(METRIC_WON))).isEditable(),
                    "editor can't edit/remove metric in embedded mode");

            reportPage.openHowPanel().selectAttribtues(singletonList(new HowItem(ATTR_DEPARTMENT))).done();
            assertEquals(reportPage.waitForReportExecutionProgress().saveAs().getTextOnSaveButton(), "Save As");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "reportType")
    public Object[][] getReportType() {
        return new Object[][] {
                {ReportTypes.HEADLINE},
                {ReportTypes.TABLE},
                {ReportTypes.LINE},
                {ReportTypes.AREA},
                {ReportTypes.STACKED_AREA},
                {ReportTypes.BAR},
                {ReportTypes.STACKED_BAR},
                {ReportTypes.BULLET},
                {ReportTypes.WATERFALL},
                {ReportTypes.FUNNEL},
                {ReportTypes.PIE},
                {ReportTypes.DONUT},
                {ReportTypes.SCATTER},
                {ReportTypes.BUBBLE}
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "reportType")
    public void checkAdminPermissionLockedEmbeddedReport(ReportTypes type) {
        String reportName = REPORT_AMOUNT_BY_STAGE_NAME + " " + type;
        initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME)
                .selectReportVisualisation(type).waitForReportExecutionProgress()
                .saveAsReport(reportName).waitForReportExecutionProgress();

        embeddedReportUri = reportPage.openReportEmbedDialog().getEmbedUri();
        ReportInfoViewPanel reportInfoViewPanel = initEmbeddedReport().openReportInfoViewPanel();
        assertEquals(reportInfoViewPanel.getTitlesOfActionButtons(), asList("View This Report", "Download As..."));

        reportInfoViewPanel.clickViewReportButton();
        switchToPopUpWindow(reportName);
        waitForFragmentVisible(reportPage);
        reportPage.waitForReportExecutionProgress();
        waitForAnalysisPageLoaded(browser);
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "reportType")
    public void checkViewerPermissionLockedEmbeddedReport(ReportTypes type) {
        String reportName = REPORT_AMOUNT_BY_STAGE_NAME + type;
        initReportsPage().openReport(REPORT_AMOUNT_BY_STAGE_NAME)
                .selectReportVisualisation(type).waitForReportExecutionProgress()
                .saveAsReport(reportName).waitForReportExecutionProgress();

        embeddedReportUri = reportPage.openReportEmbedDialog().getEmbedUri();
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            ReportInfoViewPanel reportInfoViewPanel = initEmbeddedReport().openReportInfoViewPanel();
            assertEquals(reportInfoViewPanel.getTitlesOfActionButtons(), singletonList("Download As..."));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void viewLockedReportInDomainPageByAdmin() {
        ReportsPage reportsPage = initReportsPage();
        assertEquals(reportsPage.getTitlesOfActionButtons(), asList("Move...", "Delete...", "Permissions..."));
        assertTrue(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Should display lock icon");
        assertTrue(reportsPage.isEdiTableReport(REPORT_AMOUNT_BY_STAGE_NAME), "Admin can edit report");
        reportsPage.selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME)
                .setEditingPermission(PermissionType.ADMIN)
                .setVisibility(true).cancel();
        assertTrue(reportsPage.openReport(REPORT_AMOUNT_BY_STAGE_NAME).isVisibleSaveButton(), "Admin can edit report");
    }

    @Test(dependsOnGroups = "createProject")
    public void viewLockedReportInDomainPageByEditor() {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            ReportsPage reportsPage = initReportsPage();
            assertEquals(reportsPage.getTitlesOfActionButtons(), asList("Move...", "Delete...", "Permissions..."));
            assertTrue(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Should display lock icon");
            assertFalse(reportsPage.isEdiTableReport(REPORT_AMOUNT_BY_STAGE_NAME), "Editor can't edit report");
            assertFalse(reportsPage.openReport(REPORT_AMOUNT_BY_STAGE_NAME).isVisibleSaveButton(),
                    "Editor can't edit report");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void viewLockedReportInEmbeddedDomainPageByAdmin() {
        embeddedUri = initDashboardsPage().addNewDashboard(generateHashString()).openEmbedDashboardDialog().getPreviewURI();
        ReportsPage reportsPage = initEmbeddedDashboard().openEmbeddedReportsPage();
        assertEquals(reportsPage.getTitlesOfActionButtons(), asList("Move...", "Delete...", "Permissions..."));
        assertTrue(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Should display lock icon");
        assertTrue(reportsPage.isEdiTableReport(REPORT_AMOUNT_BY_STAGE_NAME), "Admin can edit report");

        reportsPage.selectReportsAndOpenPermissionDialog(REPORT_AMOUNT_BY_STAGE_NAME)
                .setEditingPermission(PermissionType.ADMIN)
                .setVisibility(true).cancel();
        assertTrue(reportsPage.openReport(REPORT_AMOUNT_BY_STAGE_NAME).waitForReportExecutionProgress().isVisibleSaveButton(),
                "Admin can edit report");
    }

    @Test(dependsOnGroups = "createProject")
    public void viewLockedReportInEmbeddedDomainPageByEditor() {
        final String favoritesFolder = "Favorites";
        embeddedUri = initDashboardsPage().addNewDashboard(generateHashString()).openEmbedDashboardDialog().getPreviewURI();
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            ReportsPage reportsPage = initEmbeddedDashboard().openEmbeddedReportsPage();
            assertEquals(reportsPage.getTitlesOfActionButtons(), asList("Move...", "Delete...", "Permissions..."));
            assertTrue(reportsPage.isReportLocked(REPORT_AMOUNT_BY_STAGE_NAME), "Should display lock icon");
            assertFalse(reportsPage.isEdiTableReport(REPORT_AMOUNT_BY_STAGE_NAME), "Editor can't edit report");

            String numberReport = reportsPage.getNumberOfReportsInFolder(favoritesFolder);
            reportsPage.tryToMoveReportsToFolderByDragDrop(favoritesFolder, REPORT_AMOUNT_BY_STAGE_NAME);
            assertThat(reportsPage.getNumberOfReportsInFolder(favoritesFolder), equalTo(numberReport));

            reportsPage.openReport(REPORT_AMOUNT_BY_STAGE_NAME).waitForReportExecutionProgress();
            assertFalse(reportPage.isVisibleSaveButton(), "Editor can't edit report");
            assertFalse(reportPage.openWhatPanel().selectMetrics(singletonList(new WhatItem(METRIC_WON))).isEditable(),
                    "editor can't edit/remove metric in embedded mode");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }
}
