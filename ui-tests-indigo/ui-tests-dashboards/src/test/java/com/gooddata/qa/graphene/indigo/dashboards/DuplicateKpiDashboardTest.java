package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.dashboards.menu.OptionalHeaderMenu;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.*;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.sdk.model.md.Metric;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;

import org.apache.http.ParseException;
import org.json.JSONException;

import java.io.IOException;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class DuplicateKpiDashboardTest extends AbstractDashboardTest {
    private IndigoRestRequest indigoRestRequest;
    private static final String INSIGHT_ACTIVITIES = "Insight Activities";
    private static final String DASHBOARD_ACTIVITIES = "Dashboard Activities";
    private static final String DASHBOARD_DUPLICATE = "Duplicate Dashboard Activities";
    private static final String KPI_ALERT_THRESHOLD = "200";
    private static final String DASHBOARD_CHANGE = "Change Dashboard Activities";
    private static final String DASHBOARD_DUPLICATE_LOCK_MODE = "Duplicate Dashboard On Lock Mode";

    @Override
    protected void customizeProject() {
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        String insightWidget = createInsightWidget(new InsightMDConfiguration(INSIGHT_ACTIVITIES, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMdService().getObj(getProject(),
                        Metric.class, title(METRIC_AMOUNT))))));
        indigoRestRequest.createAnalyticalDashboard(singletonList(insightWidget), DASHBOARD_ACTIVITIES);
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .addKpi(new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED).build())
                .saveEditModeWithWidgets();
        setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);
        initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT).saveEditModeWithWidgets();
        initIndigoDashboardsPage().scheduleEmailing().submit();
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
}
