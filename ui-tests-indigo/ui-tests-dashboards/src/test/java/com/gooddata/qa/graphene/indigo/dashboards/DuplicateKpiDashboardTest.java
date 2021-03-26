package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.dashboards.menu.OptionalHeaderMenu;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.*;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.postMessage.PostMessageKPIDashboardPage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardEventingTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.sdk.model.md.Metric;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_SUCCESS_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;

import org.apache.http.ParseException;
import org.json.JSONException;

import java.io.IOException;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class DuplicateKpiDashboardTest extends AbstractDashboardEventingTest {
    private IndigoRestRequest indigoRestRequest;
    private static final String INSIGHT_ACTIVITIES = "Insight Activities";
    private static final String DASHBOARD_ACTIVITIES = "Dashboard Activities";
    private static final String DASHBOARD_DUPLICATE = "Duplicate Dashboard Activities";
    private static final String KPI_ALERT_THRESHOLD = "200";
    private static final String DASHBOARD_CHANGE = "Change Dashboard Activities";
    private static final String DASHBOARD_DUPLICATE_LOCK_MODE = "Duplicate Dashboard On Lock Mode";
    private static final String DASHBOARD_EMBEDDED = "Embedded Dashboard Activities";
    private static final String FRAME_KD_POST_MESSAGE_PATH_FILE = "/postMessage/frame_KD_post_message.html";
    private static final String INSIGHT_RENAMED = "Insight Activities Renamed";
    private static final String KPI_RENAMED = "KPI Amount Renamed";
    private static final String DASHBOARD_DUPLICATE_IN_CASE_EDIT = "Duplicate Dashboard In Case Edit";
    private String uriActivities;
    private String identifierActivities;
    private String dashboardActivities;
    private ProjectRestRequest projectRestRequest;
    private String originalDateFilter;
    private String originalAttributeFilter;
    private String originalInsightName;
    private String originalKPIName;

    @Override
    protected void customizeProject() {
        getMetricCreator().createAmountMetric();
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EDIT_INSIGHTS_FROM_KD, false);
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        String insightWidget = createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMdService().getObj(getProject(),
                        Metric.class, title(METRIC_AMOUNT))))));
        dashboardActivities = indigoRestRequest.createAnalyticalDashboard(singletonList(insightWidget), DASHBOARD_ACTIVITIES);
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addKpi(new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED).build())
                .saveEditModeWithWidgets();
        setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);
        initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT).saveEditModeWithWidgets();
        initIndigoDashboardsPage().scheduleEmailing().submit();
        uriActivities = getMetricByTitle(METRIC_AMOUNT).getUri();
        identifierActivities = getMetricByTitle(METRIC_AMOUNT).getIdentifier();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Administrator user"})
    public void verifySaveAsNewButtonOnViewAndEditMode() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        Boolean saveAsNewOnViewMode = indigoDashboardsPage.openHeaderOptionsButton().isSaveAsNewItemVisible();
        assertTrue(saveAsNewOnViewMode, "Save as new is invisible on view mode");
        Boolean saveAsNewOnEditMode = indigoDashboardsPage.switchToEditMode().openHeaderOptionsButton().isSaveAsNewItemVisible();
        assertTrue(saveAsNewOnEditMode, "Save as new is invisible on edit mode");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Administrator user"})
    public void saveAsDashboardDialog() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        SaveAsDialog saveAsDialog = indigoDashboardsPage.saveAsDialog();
        assertTrue(saveAsDialog.isCreateDashboardButtonDisplay(), "Create dashboard button is not exist");
        assertTrue(saveAsDialog.isCancelButtonDisplay(), "Cancel button is not exist");
        assertTrue(saveAsDialog.getTitle().equals("Save dashboard as new"), "Title is incorrect");
        assertTrue(saveAsDialog.getNameDialog().equals("Copy of Dashboard Activities"), "Name dialog is incorrect");
        assertTrue(saveAsDialog.getTextContent().equals("Alerts and email schedules will not be duplicated"), "Content text is incorrect");
        saveAsDialog.enterName(DASHBOARD_DUPLICATE).clickSubmitButton();
        String successMessage = DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText();
        assertTrue(successMessage.equals("Great. We saved your dashboard."), "Alert success message is not exist");
        assertEquals(saveAsDialog.getTitleDuplicateDashboard(), DASHBOARD_DUPLICATE, "Title of dashboard is incorrect");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Administrator user"})
    public void verifyContentOnDuplicatedDashboard() {
        boolean isNotSetAlert = indigoDashboardsPage.getLastWidget(Kpi.class).hasSetAlert();
        assertFalse(isNotSetAlert, "Dashboard has set alert");
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "All time", "Period is not matched");
        indigoDashboardsPage.switchToEditMode().changeDashboardTitle(DASHBOARD_CHANGE).saveEditModeWithWidgets();
        assertEquals(indigoDashboardsPage.saveAsDialog().getTitleDuplicateDashboard(), DASHBOARD_CHANGE, "Title of dashboard is incorrect");
        EmailSchedulePage emailSchedulePage = initEmailSchedulesPage();
        assertTrue(emailSchedulePage.getKPIPrivateScheduleTitles().get(0).getAttribute("title")
                .contains(DASHBOARD_ACTIVITIES), "Dashboard_activities is not scheduling email");
        assertFalse(emailSchedulePage.getKPIPrivateScheduleTitles().get(0).getAttribute("title")
                .contains(DASHBOARD_DUPLICATE), "Dashboard_duplicate has scheduling email");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Editor user"})
    public void setLockAnalyticalDashboard() throws IOException{
        indigoRestRequest.setLockedAttribute(DASHBOARD_ACTIVITIES, 1);
        int lockedValue = indigoRestRequest.getLockedAttribute(DASHBOARD_ACTIVITIES);
        assertEquals(lockedValue, 1, DASHBOARD_ACTIVITIES + " should be locked");
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnMethods = {"setLockAnalyticalDashboard"}, groups = {"Editor user"})
    public void verifyKpiDashboardInLockMode() {
        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertTrue(indigoDashboardsPage.isNavigationBarVisible(), "Navigation bar is not display");
            indigoDashboardsPage.selectKpiDashboard(DASHBOARD_ACTIVITIES);
            waitForOpeningIndigoDashboard();
            assertFalse(indigoDashboardsPage.isEditButtonVisible(), "Edit button is visible");
            SaveAsDialog saveAsDialog = indigoDashboardsPage.saveAsNewOnLockMode();
            saveAsDialog.enterName(DASHBOARD_DUPLICATE_LOCK_MODE).clickSubmitButton();
            String successMessage = DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText();
            assertTrue(successMessage.equals("Great. We saved your dashboard."), "Alert success message is not exist");
            assertEquals(saveAsDialog.getTitleDuplicateDashboard(), DASHBOARD_DUPLICATE_LOCK_MODE, "Title of dashboard is incorrect");
            assertTrue(indigoDashboardsPage.isEditButtonVisible(), "Edit button is invisible");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }

    }

    @Test(dependsOnGroups = "createProject", groups = {"Viewer user"})
    public void VerifyViewerRoleCannotSaveAsDashboard() {
        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
        try {
            initIndigoDashboardsPageWithWidgets();
            assertTrue(indigoDashboardsPage.isNavigationBarVisible(), "Navigation bar is not display");
            indigoDashboardsPage.selectKpiDashboard(DASHBOARD_ACTIVITIES);
            waitForOpeningIndigoDashboard();
            OptionalHeaderMenu optionalHeaderMenu = initIndigoDashboardsPage().openHeaderOptionsButton();
            Boolean saveAsNewOnViewMode = optionalHeaderMenu.isSaveAsNewItemVisible();
            assertFalse(saveAsNewOnViewMode, "Save as new button is visible");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject", groups = {"Embedded"})
    public void verifyDuplicatedKpiDashboardOnEmbeddedPage() throws IOException {
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getPassword());
        try {
            final JSONArray uris = new JSONArray() {{
                put(uriActivities);
            }};
            final String file = createTemplateHtmlFile(getObjectIdFromUri(dashboardActivities),
                    uris.toString(), identifierActivities, FRAME_KD_POST_MESSAGE_PATH_FILE);
            log.info(file);
            IndigoDashboardsPage indigoDashboardsPage = openEmbeddedPage(file);
            cleanUpLogger();
            PostMessageKPIDashboardPage postMessageApiPage = PostMessageKPIDashboardPage.getInstance(browser);
            postMessageApiPage.saveAsNew(DASHBOARD_EMBEDDED);
            indigoDashboardsPage.waitForWidgetsLoading();
            log.info(indigoDashboardsPage.getDashboardTitle());
            assertTrue(indigoDashboardsPage.getDashboardTitle().equals(DASHBOARD_EMBEDDED));
            assertFalse(indigoDashboardsPage.getLastWidget(Kpi.class).hasSetAlert());
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"Administrator user"},
        description = "Cover RAIL-3095 Duplicate dashboard ignores widget configuration after editing and saving")
    public void verifyDifferenceBetweenDuplicateAndOriginalDashboards() {
        initIndigoDashboardsPage().selectKpiDashboard(DASHBOARD_ACTIVITIES).switchToEditMode();
        originalDateFilter = indigoDashboardsPage.getDateFilterSelection();
        AttributeFiltersPanel panel = indigoDashboardsPage.getAttributeFiltersPanel();
        originalAttributeFilter = panel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems();
        originalInsightName = indigoDashboardsPage.getWidgetByIndex(Insight.class, 0).getHeadline();
        originalKPIName = indigoDashboardsPage.getWidgetByIndex(Kpi.class, 1).getHeadline();

        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.THIS_YEAR).apply();
        panel.getAttributeFilter(ATTR_ACCOUNT).clearAllCheckedValues().selectByNames("101 Financial", "123 Exteriors", "14 West");
        indigoDashboardsPage.getWidgetByIndex(Kpi.class, 1).setHeadline(KPI_RENAMED);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel();
        configurationPanel.selectComparisonByName(Kpi.ComparisonType.NO_COMPARISON.toString());
        indigoDashboardsPage.selectWidgetByHeadline(Insight.class, INSIGHT_ACTIVITIES).setHeadline(INSIGHT_RENAMED);
        configurationPanel.drillIntoInsight(METRIC_AMOUNT, INSIGHT_ACTIVITIES);
        waitForElementNotPresent(BY_SUCCESS_MESSAGE_BAR);

        indigoDashboardsPage.saveAsDialog().enterName(DASHBOARD_DUPLICATE_IN_CASE_EDIT).clickSubmitButton();
        String successMessage = DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar().getText();
        assertTrue(successMessage.equals("Great. We saved your dashboard."), "Alert success message is not exist");
        indigoDashboardsPage.waitForWidgetsLoading();

        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "This year");
        assertEquals(panel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), "101 Financial, 123 Exteriors, 14 West");
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0).getHeadline(), INSIGHT_RENAMED);
        ChartReport chartReport = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_RENAMED).getChartReport();
        assertTrue(chartReport.isColumnHighlighted(Pair.of(0, 0)), "Insight should be highlighted");
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Kpi.class, 1).getHeadline(), KPI_RENAMED);
        assertFalse(indigoDashboardsPage.getWidgetByIndex(Kpi.class, 1).hasPopSection(), "KPI shouldn't have POP section");

        indigoDashboardsPage.selectKpiDashboard(DASHBOARD_ACTIVITIES).waitForWidgetsLoading();
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), originalDateFilter);
        assertEquals(panel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), originalAttributeFilter);
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Insight.class, 0).getHeadline(), originalInsightName);
        assertEquals(indigoDashboardsPage.getWidgetByIndex(Kpi.class, 1).getHeadline(), originalKPIName);
    }

    @Override
    protected void cleanUpLogger() {
        browser.switchTo().defaultContent();
        waitForElementVisible(By.id("loggerBtn"), browser).click();
    }
}
