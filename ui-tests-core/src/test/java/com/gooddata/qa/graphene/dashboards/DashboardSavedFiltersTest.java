package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditFilter;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardSettingsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget.SavedViewPopupMenu;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;

public class DashboardSavedFiltersTest extends AbstractProjectTest{

    private static final String FIRST_DASHBOARD_NAME  = "Dashboard 1";
    private static final String SECOND_DASHBOARD_NAME = "Dashboard 2";
    private static final String DATE_PAYDATE_FILTER = "DATE (PAYDATE)";

    private static final int    THIS_YEAR             = Calendar.getInstance().get(Calendar.YEAR);
    private static final String LAST_YEAR             = String.valueOf(THIS_YEAR - 1);
    private static final String PENULTIMATE_YEAR      = String.valueOf(THIS_YEAR - 2);

    @Override
    protected void initProperties() {
        projectTitle = "SimpleProject-test-dashboard-saved-filters";
    }

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/payroll.csv"));
        createDashboard("Sample Dashboard");
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 1)
    public void notificationWhenSavedViewTurnedOffTest() {
        DashboardEditBar dashboardEditBar = null;
        try {
            initDashboardsPage();
            dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            WebElement savedViewDisabledNotification = dashboardEditBar.getSavedViewDisabledNotification();
            waitForElementVisible(savedViewDisabledNotification);
            checkSavedViewisDisableByNotification(savedViewDisabledNotification);

            savedViewDisabledNotification.click();
            DashboardSettingsDialog dashboardSettingsDialog = dashboardEditBar.getDashboardSettingsDialog();
            waitForElementVisible(dashboardSettingsDialog.getRoot());
            dashboardSettingsDialog.turnSavedViewOption(true);
            waitForElementNotVisible(savedViewDisabledNotification);
            dashboardEditBar.cancelDashboard();
            dashboardsPage.editDashboard();
            checkSavedViewisDisableByNotification(savedViewDisabledNotification);

            dashboardEditBar.turnSavedViewOption(true);
            dashboardEditBar.saveDashboard();
            waitForElementNotVisible(savedViewDisabledNotification);
        } finally {
            dashboardsPage.editDashboard();
            if (dashboardEditBar == null) return;
            dashboardEditBar.turnSavedViewOption(false);
            dashboardEditBar.saveDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 2)
    public void checkDisableSavedFiltersFeatureFlagsTest() throws IOException, JSONException {
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        try {
            projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.DISABLE_SAVED_FILTERS, true);

            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
            WebElement savedViewDisabledNotification = dashboardEditBar.getSavedViewDisabledNotification();
            waitForElementNotVisible(savedViewDisabledNotification);
            dashboardEditBar.openDashboardSettingsDialog();
            DashboardSettingsDialog dashboardSettingsDialog = dashboardEditBar.getDashboardSettingsDialog();
            waitForElementVisible(dashboardSettingsDialog.getRoot());
            waitForElementNotVisible(dashboardSettingsDialog.getSavedViewsCheckbox());
            waitForElementVisible(dashboardSettingsDialog.getFiltersCheckbox());
            waitForElementVisible(dashboardSettingsDialog.getCancelButton()).click();
            waitForElementNotVisible(dashboardSettingsDialog.getRoot());
            dashboardEditBar.cancelDashboard();
        } finally {
            projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.DISABLE_SAVED_FILTERS, false);
    
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, priority = 3)
    public void createSavedFilterViewTest() {
        initNewDashboard_AddFilter_TurnOnSavedView(FIRST_DASHBOARD_NAME);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        assertTrue(savedViewWidget.isDefaultViewButtonPresent(),
                          "Saved filter view does not show as 'Default View'!");

        savedViewWidget.openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu viewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        assertTrue(viewPopupMenu.isNoSavedViewPresent(),
                          "'No Saved Views' is not shown in saved view menu!");

        FilterWidget timeFilter = dashboardsPage.getFilterWidgetByName(DATE_PAYDATE_FILTER);
        timeFilter.changeTimeFilterValueByClickInTimeLine(LAST_YEAR);
        assertTrue(savedViewWidget.isUnsavedViewButtonPresent(),
                          "Saved filter view does not show as 'Unsaved View'!");

        savedViewWidget.openSavedViewMenu();
        SavedViewWidget.DashboardSaveActiveViewDialog dashboardSaveActiveViewDialog = savedViewWidget.getDashboardSaveActiveViewDialog();
        waitForElementVisible(viewPopupMenu.getSavedCurrentViewButton()).click();
        waitForElementVisible(dashboardSaveActiveViewDialog.getRoot());
        assertNotNull(waitForElementVisible(dashboardSaveActiveViewDialog.getNameField()),
                             "'Name' text field is not shown in 'Save active view' dialog!");

        assertEquals(dashboardSaveActiveViewDialog.getFilters().size(), 1,
                            "Expected filters in 'Save active view' dialog is 1, actual: " + dashboardSaveActiveViewDialog.getFilters().size());

        String filterNameInDialog = dashboardSaveActiveViewDialog.getFilters().get(0).getText();
                    //name in dashboard include its value at the end of its name
        String filterNameInDashboard = timeFilter.getRoot().getText().split("\n")[0];
        assertTrue(filterNameInDialog.equalsIgnoreCase(filterNameInDashboard),
                          "Name of filter in dashboard content and filter in 'Save active view' dialog is not matched!");

        assertTrue(dashboardSaveActiveViewDialog.isAllFiltersAreChecked(),
                          "All filters in 'Save active view' dialog is not selected!");
        
        waitForElementVisible(dashboardSaveActiveViewDialog.getCancelButton()).click();
        assertTrue(savedViewWidget.isUnsavedViewButtonPresent(),
                          "Saved filter view does not show as 'Unsaved View'!");

        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView("last year");
        assertEquals(waitForElementVisible(savedViewWidget.getRoot()).getText(),
                            "last year",
                            "New saved view is not saved as named 'last year'!");
    }

    @Test(dependsOnMethods = {"createSavedFilterViewTest"})
    public void renameSavedFilterViewTest() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        // change filter value so Selenium can loads all saved views
        dashboardsPage.getFilterWidgetByName(DATE_PAYDATE_FILTER).changeTimeFilterValueByClickInTimeLine(PENULTIMATE_YEAR);

        // try to rename a saved view but canceling at the end.
        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView(PENULTIMATE_YEAR);
        savedViewWidget.openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        savedViewPopupMenu.openContextMenuOfSavedView("last year");
        savedViewPopupMenu.getSavedFiltersContextMenu().openRenameDialog();
        SavedViewWidget.DashboardSaveActiveViewDialog saveActiveViewDialog = savedViewWidget.getDashboardSaveActiveViewDialog();
        waitForElementVisible(saveActiveViewDialog.getRoot());
        waitForElementVisible(saveActiveViewDialog.getNameField()).sendKeys("previous year");
        waitForElementVisible(saveActiveViewDialog.getCancelButton()).click();
        waitForElementNotVisible(saveActiveViewDialog.getRoot());

        savedViewWidget.openSavedViewMenu();
        List<String> allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertTrue(allSavedViewNames.contains("last year"),
                          "Saved view 'last year' is renamed!");
        assertFalse(allSavedViewNames.contains("previous year"),
                           "Saved view 'previous year' is created!");

        // try to rename a saved view
        savedViewPopupMenu.openContextMenuOfSavedView("last year");
        savedViewPopupMenu.getSavedFiltersContextMenu().openRenameDialog();
        waitForElementVisible(saveActiveViewDialog.getRoot());
        saveActiveViewDialog.rename("previous year");

        savedViewWidget.openSavedViewMenu();
        allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertFalse(allSavedViewNames.contains("last year"),
                          "Saved view 'last year' is still exist!");
        assertTrue(allSavedViewNames.contains("previous year"),
                          "Saved view 'previous year' is not created!");
    }
    
    @Test(dependsOnMethods = {"renameSavedFilterViewTest"})
    public void filterViewNamingUniquenessTest() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        // change filter value so Selenium can loads all saved views
        dashboardsPage.getFilterWidgetByName(DATE_PAYDATE_FILTER).changeTimeFilterValueByClickInTimeLine(String.valueOf(THIS_YEAR - 3));

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView("previous year", false);
        waitForElementVisible(SavedViewWidget.DashboardSaveActiveViewDialog.NAME_ALREADY_IN_USE_ERROR, browser);

        SavedViewWidget.DashboardSaveActiveViewDialog saveActiveViewDialog = savedViewWidget.getDashboardSaveActiveViewDialog();
        waitForElementVisible(saveActiveViewDialog.getCancelButton()).click();
        waitForElementNotVisible(saveActiveViewDialog.getRoot());

        savedViewWidget.openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        savedViewPopupMenu.openContextMenuOfSavedView("previous year");
        savedViewPopupMenu.getSavedFiltersContextMenu().openRenameDialog();
        waitForElementVisible(saveActiveViewDialog.getRoot());
        saveActiveViewDialog.rename(PENULTIMATE_YEAR, false);

        // disable checking because of bug: https://jira.intgdc.com/browse/CL-5847
        //waitForElementVisible(SavedViewWidget.DashboardSaveActiveViewDialog.FIELD_IS_REQUIRED_ERROR, browser);

        waitForElementVisible(saveActiveViewDialog.getCancelButton()).click();
        waitForElementNotVisible(saveActiveViewDialog.getRoot());
    }

    @Test(dependsOnMethods = {"filterViewNamingUniquenessTest"})
    public void deleteSavedFilterViewTest() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        // change filter value so Selenium can loads all saved views
        FilterWidget filter = dashboardsPage.getFilterWidgetByName(DATE_PAYDATE_FILTER);
        filter.changeTimeFilterValueByClickInTimeLine(String.valueOf(THIS_YEAR - 4));

        // verify things in delete dialog and canceling at the end
        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        savedViewPopupMenu.openContextMenuOfSavedView(PENULTIMATE_YEAR);
        savedViewPopupMenu.getSavedFiltersContextMenu().openDeleteDialog();
        SavedViewWidget.SavedViewDeleteConfirmDialog deleteConfirmDialog = savedViewWidget.getSavedViewDeleteConfirmDialog();
        waitForElementVisible(deleteConfirmDialog.getRoot());
        assertEquals(deleteConfirmDialog.getConfirmMessage(), "Are you sure you want to delete this view?",
                            "Confirm message is not correct!");
        
        waitForElementVisible(deleteConfirmDialog.getCancelButton()).click();
        savedViewWidget.openSavedViewMenu();
        List<String> allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertTrue(allSavedViewNames.contains(PENULTIMATE_YEAR),
                          String.format("Saved view '%s' is disappeared!", PENULTIMATE_YEAR));

        // try to delete a saved view and check status after delete
        savedViewPopupMenu.openContextMenuOfSavedView(PENULTIMATE_YEAR);
        savedViewPopupMenu.getSavedFiltersContextMenu().openDeleteDialog();
        waitForElementVisible(deleteConfirmDialog.getRoot());
        deleteConfirmDialog.deleteSavedView();

        sleepTightInSeconds(1);
        // disable checking because of bug: https://jira.intgdc.com/browse/CL-5800
        //assertTrue(savedViewWidget.isUnsavedViewButtonPresent(),
        //                  String.format("Saved view '%s' is not deleted!", PENULTIMATE_YEAR));

        filter.changeTimeFilterValueByClickInTimeLine(String.valueOf(THIS_YEAR - 3));
        savedViewWidget.openSavedViewMenu();
        allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertFalse(allSavedViewNames.contains(PENULTIMATE_YEAR),
                           String.format("Saved view '%s' is not deleted!", PENULTIMATE_YEAR));
    }

    @Test(dependsOnMethods = {"deleteSavedFilterViewTest"})
    public void savedFilterAfterSwitchBetweenDashboardsAndPagesTest() {
        initDashboardsPage();
        // Add more saved view for first dashboard
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        dashboardsPage.getFilterWidgetByName(DATE_PAYDATE_FILTER).changeTimeFilterValueByClickInTimeLine(PENULTIMATE_YEAR);
        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView(PENULTIMATE_YEAR);

        // Create new dashboard SECOND_DASHBOARD_NAME to test
        initNewDashboard_AddFilter_TurnOnSavedView(SECOND_DASHBOARD_NAME);
        dashboardsPage.editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Department")
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "County")
                .saveDashboard();

        // Create saved view 1 "Abundant Foodz"
        sleepTightInSeconds(1);
        FilterWidget departmentFilter = dashboardsPage.getFilterWidget("department");
        departmentFilter.changeAttributeFilterValues("Abundant Foodz");
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView("Abundant Foodz");

        assertEquals(dashboardsPage.getFilterWidget("county").getCurrentValue(), "All",
                            "Value of 'County' is not 'All'!");
        sleepTightInSeconds(1);
        assertEquals(departmentFilter.getCurrentValue(), "Abundant Foodz",
                            "Value of 'Department' is not 'Abundant Foodz'!");

        // Create saved view 2 "Earthy Foodz"
        departmentFilter.changeAttributeFilterValues("Earthy Foodz");
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView("Earthy Foodz");

        // start testing
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        SavedViewWidget.SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        assertEquals(waitForElementVisible(savedViewWidget.getRoot()).getText(),
                            PENULTIMATE_YEAR,
                            String.format("Saved view '%s' is not selected!", PENULTIMATE_YEAR));

        savedViewWidget.openSavedViewMenu();
        List<String> allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertEquals(allSavedViewNames.size(), 3);
        for (String name : Arrays.asList(PENULTIMATE_YEAR, "previous year")) {
            assertTrue(allSavedViewNames.contains(name),
                              String.format("'%s' is not in saved view list", name));
        }

        dashboardsPage.selectDashboard(SECOND_DASHBOARD_NAME);
        // disable checking because of bug: https://jira.intgdc.com/browse/CL-5334 
        //assertEquals(waitForElementVisible(SavedViewWidget.SAVED_VIEW_BUTTON, browser).getText(),
        //                    "Earthy Foodz",
        //                    String.format("Saved view '%s' is not selected!", "Earthy Foodz"));
        departmentFilter.changeAttributeFilterValues("Family Foodz");
        savedViewWidget.openSavedViewMenu();
        allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertEquals(allSavedViewNames.size(), 3);
        for (String name : Arrays.asList("Abundant Foodz", "Earthy Foodz")) {
            assertTrue(allSavedViewNames.contains(name),
                              String.format("'%s' is not in saved view list", name));
        }

        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        initVariablePage();
        initDashboardsPage().selectDashboard(FIRST_DASHBOARD_NAME);
        sleepTightInSeconds(2);

        assertEquals(waitForElementVisible(savedViewWidget.getRoot()).getText(),
                            PENULTIMATE_YEAR,
                            String.format("Saved view '%s' is not selected!", PENULTIMATE_YEAR));
    }

    @Test(dependsOnMethods = {"savedFilterAfterSwitchBetweenDashboardsAndPagesTest"})
    public void savedViewFilterDoNotApplyOnTimeFilterAfterEditGranularityTest() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        DashboardEditFilter dashboardEditFilter = dashboardEditBar.getDashboardEditFilter();
        dashboardEditFilter.changeTypeOfTimeFilter("Day");
        dashboardEditBar.saveDashboard();
        assertEquals(waitForElementVisible(dashboardsPage.getSavedViewWidget().getRoot()).getText(),
                            PENULTIMATE_YEAR,
                            String.format("Saved view '%s' is not selected after changing type of time filter!", PENULTIMATE_YEAR));
    }

    @Test(dependsOnMethods = {"savedViewFilterDoNotApplyOnTimeFilterAfterEditGranularityTest"})
    public void savedViewFilterDoNotApplyOnFiltersAfterRemovingTest() {
        try {
            initDashboardsPage();
            dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
            DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
            DashboardEditFilter dashboardEditFilter = dashboardEditBar.getDashboardEditFilter();
            dashboardEditFilter.deleteTimeFilter();
            dashboardEditBar.saveDashboard();
    
            sleepTightInSeconds(1);
            // disable checking because of bug: https://jira.intgdc.com/browse/CL-5708
            //assertEquals(waitForElementVisible(SavedViewWidget.SAVED_VIEW_BUTTON, browser).getText(),
            //                    PENULTIMATE_YEAR,
            //                    String.format("Saved view '%s' is not selected after deleting time filter!", PENULTIMATE_YEAR));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"savedViewFilterDoNotApplyOnFiltersAfterRemovingTest"})
    public void dashboardHasManyFiltersInManyTabsTest() {
        initDashboardsPage();
        dashboardsPage.selectDashboard(SECOND_DASHBOARD_NAME)
                .addNewTab("second tab")
                .turnSavedViewOption(true)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Firstname")
                .saveDashboard();

        dashboardsPage.getFilterWidget("firstname").changeAttributeFilterValues("Adam");
        dashboardsPage.getTabs().openTab(0);
        dashboardsPage.getFilterWidget("county").changeAttributeFilterValues("Austin");
        sleepTightInSeconds(1);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        SavedViewPopupMenu savedViewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        waitForElementVisible(savedViewPopupMenu.getSavedCurrentViewButton()).click();
        SavedViewWidget.DashboardSaveActiveViewDialog dashboardSaveActiveViewDialog = savedViewWidget.getDashboardSaveActiveViewDialog();
        waitForElementVisible(dashboardSaveActiveViewDialog.getRoot());

        List<String> filterNames = Arrays.asList("County", "Firstname", "Department");
        {
            String filterName = null;
            for (WebElement filter : dashboardSaveActiveViewDialog.getFilters()) {
                filterName = filter.getText();
                assertTrue(filterNames.contains(filterName),
                                  String.format("'%s' is not in filter list in 'Save active view' dialog!", filterName));
            }
        }

        waitForElementVisible(dashboardSaveActiveViewDialog.getCancelButton()).click();
        waitForElementNotVisible(dashboardSaveActiveViewDialog.getRoot());
    }

    private void initNewDashboard_AddFilter_TurnOnSavedView(String dashboardName) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(dashboardName);

        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addTimeFilterToDashboard(DateGranularity.YEAR, "this");
        dashboardEditBar.saveDashboard();

        dashboardsPage.editDashboard();
        dashboardEditBar.turnSavedViewOption(true);
        dashboardEditBar.saveDashboard();

    }

    private void checkSavedViewisDisableByNotification(WebElement notification) {
        assertTrue(notification.isDisplayed(),
                "Notification 'Saved Views disabled' is not shown!");
        assertEquals(notification.getText(), "Saved Views disabled",
                  "Notification text is not correct!");
    }
}
