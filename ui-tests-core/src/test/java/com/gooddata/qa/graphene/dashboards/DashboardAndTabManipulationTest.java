package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.OneNumberReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.dashboard.PublishType;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardTabs;
import com.gooddata.qa.graphene.fragments.dashboards.PermissionsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.logging.Level;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;


public class DashboardAndTabManipulationTest extends AbstractProjectTest {

    private final static String AMOUNT = "Amount";
    private final static String POSITION = "Position";
    private final static String SIMPLE_REPORT_1 = "Simple-Report-1";
    private final static String SIMPLE_REPORT_2 = "Simple-Report-2";
    private final static String SIMPLE_REPORT_3 = "Simple-Report-3";
    private final static String SIMPLE_REPORT_4 = "Headline-Report-4";

    private final static String DASHBOARD_NAME_1 = "Dashboard1";
    private final static String TAB_NAME_FIRST = "First Tab";
    private final static String TAB_NAME_1 = "tab name 1";
    private final static String DASHBOARD_NAME_2 = "Dashboard2";
    private final static String TAB_NAME_2 = "tab name 2";
    private final static String TAB_NAME_3 = "tab name 3";
    private final static String DASHBOARD_NAME_DEFAULT = "Default dashboard";
    private final static String NEW_DASHBOARD_NAME_DEFAULT = "Untitled dashboard";
    private final static String SAVE_AS_DASHBOARD_NAME_DEFAULT = "New save as dashboard";

    private final static String AMOUNT_METRIC = "Amount-Metric";
    private final static String DEFAULT_TAB_TITLE = "First Tab";
    private Metric amountSum;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle += "SimpleProject-test-dashboard-and-tab";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();

        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll", getClass());

        final String amountUri = getMdService()
                .getObj(getProject(), Fact.class, title(AMOUNT))
                .getUri();

        amountSum = getMdService()
                .createObj(getProject(), new Metric(AMOUNT_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        MetricFormatterDialog.Formatter.DEFAULT.toString()));

        final Attribute position = getMdService().getObj(getProject(), Attribute.class, title(POSITION));

        ReportDefinition definition = GridReportDefinitionContent.create(SIMPLE_REPORT_1, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(position.getDefaultDisplayForm().getUri(), position.getTitle())),
                singletonList(new MetricElement(amountSum)));

        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        definition = GridReportDefinitionContent.create(SIMPLE_REPORT_2, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(position.getDefaultDisplayForm().getUri(), position.getTitle())),
                singletonList(new MetricElement(amountSum)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        definition = GridReportDefinitionContent.create(SIMPLE_REPORT_3, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(position.getDefaultDisplayForm().getUri(), position.getTitle())),
                singletonList(new MetricElement(amountSum)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        definition = OneNumberReportDefinitionContent.create(SIMPLE_REPORT_4, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(position.getDefaultDisplayForm().getUri(), position.getTitle())),
                singletonList(new MetricElement(amountSum)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveAsDashBoardInViewMode() {
        initDashboardsPage();
        try {
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_DEFAULT);

            dashboardsPage.openEditExportEmbedMenu();
            assertFalse(dashboardsPage.isSettingSaveAsButtonVisible(),
                    "Save as button should be disabled for default dashboard");
            dashboardsPage.closeEditExportEmbedMenu();

            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);

            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_1);
            dashboardsPage.saveAsDashboard(DASHBOARD_NAME_2, SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_2);
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void rememberSpecifiedTab() {
        initDashboardsPage();
        try {
            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1).addNewTab(TAB_NAME_1).saveDashboard();
            DashboardTabs tabs = dashboardsPage.getTabs();
            assertEquals(tabs.getNumberOfTabs(), 2);
            assertTrue(tabs.isTabSelected(1), "tab:" + TAB_NAME_1 + " must be selected");

            initReportsPage();
            initDashboardsPage();

            assertEquals(tabs.getNumberOfTabs(), 2);
            assertTrue(tabs.isTabSelected(1), "tab:" + TAB_NAME_1 + " must be selected");
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void saveAsDashboardInEditMode() {
        initDashboardsPage();
        try {
            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);
            dashboardsPage.addReportToDashboard(SIMPLE_REPORT_4);
            dashboardsPage.saveDashboard();

            assertEquals(dashboardsPage.getDashboardsCount(), 1);
            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().saveAsDashboard(DASHBOARD_NAME_1, false,
                    SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);

            assertEquals(dashboardsPage.getDashboardsCount(), 2);
            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().saveAsDashboard(SAVE_AS_DASHBOARD_NAME_DEFAULT, false,
                    SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);
            assertEquals(dashboardsPage.getDashboardsCount(), 3);

            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().saveAsDashboard("", false,
                    SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);
            assertEquals(dashboardsPage.getDashboardsCount(), 4);
            assertEquals(dashboardsPage.getDashboardName(), "Copy of " + SAVE_AS_DASHBOARD_NAME_DEFAULT);
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cancelSavingDashBoard() {
        initDashboardsPage();
        try {
            assertEquals(dashboardsPage.getContent().getGeoCharts().size(), 0);
            dashboardsPage.editDashboard();

            DashboardEditBar editBar = dashboardsPage.getDashboardEditBar();
            editBar.addReportToDashboard(SIMPLE_REPORT_2);
            editBar.addLineToDashboard();
            editBar.addGeoChart(AMOUNT_METRIC, null);

            dashboardsPage.addNewTab(TAB_NAME_1);
            dashboardsPage.addNewTab(TAB_NAME_2);

            editBar.cancelDashboard();

            assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), 1);
            assertEquals(dashboardsPage.getTabs().getTab(0).getLabel(), DEFAULT_TAB_TITLE);
            assertEquals(dashboardsPage.getContent().getNumberOfWidgets(), 0);
            assertEquals(dashboardsPage.getContent().getGeoCharts().size(), 1);
            assertFalse(dashboardsPage.getContent().getGeoCharts().get(0).isMetricNameDisplayed(),
                    "dashboard should not contain chart");
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addressDashboardTab() throws JSONException {
        initDashboardsPage();
        try {
            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1).addNewTab(TAB_NAME_1).saveDashboard();
            final String currentDashboardUri = browser.getCurrentUrl();
            initReportsPage();

            browser.get(currentDashboardUri);
            waitForDashboardPageLoaded(browser);
            waitForFragmentVisible(dashboardsPage);
            DashboardTabs tabs = dashboardsPage.getTabs();
            assertEquals(tabs.getNumberOfTabs(), 2);
            assertTrue(tabs.isTabSelected(1), "tab:" + TAB_NAME_1 + " must be selected");
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAndViewDashboard() {
        initDashboardsPage();
        try {
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_DEFAULT);

            dashboardsPage.addNewDashboard("");
            assertEquals(dashboardsPage.getDashboardName(), NEW_DASHBOARD_NAME_DEFAULT);
            dashboardsPage.editDashboard();

            dashboardsPage.getDashboardEditBar().addReportToDashboard(SIMPLE_REPORT_2);
            dashboardsPage.renameDashboard(DASHBOARD_NAME_1).saveDashboard();

            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_1);

            initReportsPage().openReport(SIMPLE_REPORT_3);
            reportPage.waitForReportExecutionProgress();
            reportPage.selectReportVisualisation(ReportTypes.BAR).waitForReportExecutionProgress().saveReport();

            initDashboardsPage();
            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().addReportToDashboard(SIMPLE_REPORT_3);

            dashboardsPage.saveDashboard();
            assertTrue(dashboardsPage.getReport(SIMPLE_REPORT_3, ChartReport.class).isChart(),
                    "Report visualisation should be chart");
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchBetweenDashboards() {
        initDashboardsPage();
        try {
            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);
            dashboardsPage.editDashboard().addReportToDashboard(SIMPLE_REPORT_1);

            dashboardsPage.saveDashboard();

            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_1);
            assertTrue(dashboardsPage.getContent().getReport(SIMPLE_REPORT_1, TableReport.class).isReportTitleVisible(),
                    "Dashboard does not contains report: " + SIMPLE_REPORT_1);

            dashboardsPage.addNewDashboard(DASHBOARD_NAME_2);
            dashboardsPage.editDashboard().addReportToDashboard(SIMPLE_REPORT_2);
            DashboardEditBar editBar = dashboardsPage.getDashboardEditBar();
            editBar.addReportToDashboard(SIMPLE_REPORT_2);
            editBar.addLineToDashboard();
            editBar.addGeoChart(AMOUNT_METRIC, null);

            dashboardsPage.saveDashboard();

            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_2);
            assertTrue(dashboardsPage.getContent().getReport(SIMPLE_REPORT_2, TableReport.class).isReportTitleVisible(),
                    "Dashboard does not contains report: " + SIMPLE_REPORT_2);

            dashboardsPage.selectDashboard(DASHBOARD_NAME_1);
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_1);
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeDashboard() {
        initDashboardsPage();
        try {
            prepareSomeDashboards();
            
            assertEquals(dashboardsPage.getDashboardsCount(), 2);

            dashboardsPage.selectDashboard(DASHBOARD_NAME_2);
            dashboardsPage.editDashboard();
            waitForFragmentVisible(dashboardsPage.getDashboardEditBar().deleteDashboardWithConfirm()
                    .confirmDeleteDashboard(false));

            dashboardsPage.saveDashboard();
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_2);

            dashboardsPage.selectDashboard(DASHBOARD_NAME_1);
            dashboardsPage.deleteDashboard();
            dashboardsPage.selectDashboard(DASHBOARD_NAME_2);
            dashboardsPage.deleteDashboard();

            initDashboardsPage();
            assertEquals(dashboardsPage.getDashboardsCount(), 1);
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_DEFAULT);

            //test delete last dashboard

            dashboardsPage.deleteDashboard();
            waitForFragmentVisible(dashboardsPage);
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_DEFAULT);
            assertTrue(dashboardsPage.isPrintButtonDisabled(), "Dashboard button print should be disabled");
            dashboardsPage.openEditExportEmbedMenu();
            assertFalse(dashboardsPage.isSettingExportToPdfButtonVisible(),
                    "Dashboard setting export to pdf should be disabled");
            assertFalse(dashboardsPage.isSettingEmbedButtonVisible(), "Dashboard setting embed should be disabled");
            dashboardsPage.closeEditExportEmbedMenu();
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void manipulateTab() {
        initDashboardsPage();
        try {
            assertEquals(dashboardsPage.getDashboardsCount(), 1);

            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);
            assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), 1);
            assertTrue(dashboardsPage.isDeleteTabDisabled(0),
                    "delete a tab should not be enabled if there's only one tab");
            dashboardsPage.addNewTab(TAB_NAME_1);
            dashboardsPage.addNewTab(TAB_NAME_2);
            dashboardsPage.addNewTab(TAB_NAME_3);

            assertEquals(dashboardsPage.getTabs().getTab(1).getLabel(), TAB_NAME_1);
            dashboardsPage.renameTab(1, TAB_NAME_1 + " renamed");
            assertEquals(dashboardsPage.getTabs().getTab(1).getLabel(), TAB_NAME_1 + " renamed");

            //test reorder tabs
            assertEquals(dashboardsPage.getTabs().getTab(0).getLabel(), TAB_NAME_FIRST);
            assertEquals(dashboardsPage.getTabs().getTab(3).getLabel(), TAB_NAME_3);
            dashboardsPage.moveTab(0, 2);

            assertEquals(dashboardsPage.getTabs().getTabLabel(0), TAB_NAME_1 + " renamed");
            assertEquals(dashboardsPage.getTabs().getTabLabel(3), TAB_NAME_FIRST);
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void copyWidgetBetweenTabs() {
        initDashboardsPage();
        try {
            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);
            dashboardsPage.addNewTab(TAB_NAME_1);
            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().addLineToDashboard().addReportToDashboard(SIMPLE_REPORT_4)
                    .addGeoChart(AMOUNT_METRIC, null);
            dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, POSITION);
            dashboardsPage.saveDashboard();

            assertEquals(dashboardsPage.getFilters().size(), 1);
            assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), 2);

            dashboardsPage.addNewTab(TAB_NAME_2);
            assertEquals(dashboardsPage.getTabs().getNumberOfTabs(), 3);

            dashboardsPage.copyTabContent(1, 2);
            dashboardsPage.saveDashboard();

            dashboardsPage.openTab(2);
            assertFalse(dashboardsPage.getContent().getReport(SIMPLE_REPORT_4,
                    OneNumberReport.class).getValue().isEmpty(),
                    "Dashboard does not contains report: " + SIMPLE_REPORT_4);
            assertEquals(dashboardsPage.getFilters().size(), 0);
        } finally {
            deleteAllDashboards();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void loginToDeletedDashboard() throws ParseException, JSONException, IOException {
        initDashboardsPage();
        try {
            dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);
            PermissionsDialog permissionsDialog = dashboardsPage.openPermissionsDialog();
            permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
            permissionsDialog.submit();

            dashboardsPage.addNewDashboard(DASHBOARD_NAME_2);
            permissionsDialog = dashboardsPage.openPermissionsDialog();
            permissionsDialog.publish(PublishType.EVERYONE_CAN_ACCESS);
            permissionsDialog.submit();

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(DASHBOARD_NAME_1);

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);
            initDashboardsPage().selectDashboard(DASHBOARD_NAME_1);
            dashboardsPage.deleteDashboard();

            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_2);
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);
            initDashboardsPage();
            assertEquals(dashboardsPage.getDashboardName(), DASHBOARD_NAME_2);

            dashboardsPage.selectDashboard(DASHBOARD_NAME_2);
            dashboardsPage.deleteDashboard();

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        } finally {
            deleteAllDashboards();
        }
    }

    private void prepareSomeDashboards (){
        dashboardsPage.addNewDashboard(DASHBOARD_NAME_1);
        dashboardsPage.editDashboard().addReportToDashboard(SIMPLE_REPORT_1);

        dashboardsPage.saveDashboard();

        dashboardsPage.addNewDashboard(DASHBOARD_NAME_2);
        dashboardsPage.editDashboard().addReportToDashboard(SIMPLE_REPORT_2);
        DashboardEditBar editBar = dashboardsPage.getDashboardEditBar();
        editBar.addReportToDashboard(SIMPLE_REPORT_2);
        editBar.addLineToDashboard();
        editBar.addGeoChart(AMOUNT_METRIC, null);

        dashboardsPage.saveDashboard();
    }

    private void deleteAllDashboards() {
        try {
            DashboardsRestUtils.deleteAllDashboards(getRestApiClient(), testParams.getProjectId());
        } catch (RuntimeException ex) {
            log.log(Level.WARNING, ex.getMessage());
        }
    }
}
