package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.utils.http.RestUtils.setFeatureFlags;
import static com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption.createFeatureClassOption;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditFilter;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardSettingsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget.SavedViewPopupMenu;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;

@Test(groups = {"dashboardSavedFilters"}, description = "Test saved filters work on dashboard in Portal")
public class DashboardSavedFiltersTest extends AbstractProjectTest{

    private static final String FIRST_DASHBOARD_NAME  = "Dashboard 1";
    private static final String SECOND_DASHBOARD_NAME = "Dashboard 2";

    private static final int    THIS_YEAR             = Calendar.getInstance().get(Calendar.YEAR);
    private static final String LAST_YEAR             = String.valueOf(THIS_YEAR - 1);
    private static final String PENULTIMATE_YEAR      = String.valueOf(THIS_YEAR - 2);

    private static final String DISABLE_SAVED_FILTERS = "disableSavedFilters";

    @BeforeClass
    public void initProjectTitle() {
        projectTitle = "SimpleProject-test-dashboard-saved-filters";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"init-data"})
    public void uploadCsvDataForBlankProject() {
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/payroll.csv"), null, "payroll");
    }

    @Test(dependsOnGroups = {"init-data"}, priority = 1)
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

    @Test(dependsOnGroups = {"init-data"}, priority = 2)
    public void checkDisableSavedFiltersFeatureFlagsTest() throws IOException, JSONException {
        try {
            disableSavedFilters(true);

            initDashboardsPage();
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
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
            disableSavedFilters(false);
    
            browser.navigate().refresh();
            waitForDashboardPageLoaded(browser);
        }
    }

    @Test(dependsOnGroups = {"init-data"}, priority = 3)
    public void createSavedFilterViewTest() throws InterruptedException {
        initNewDashboard_AddFilter_TurnOnSavedView(FIRST_DASHBOARD_NAME);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        assertTrue(savedViewWidget.isDefaultViewButtonPresent(),
                          "Saved filter view does not show as 'Default View'!");

        savedViewWidget.openSavedViewMenu();
        SavedViewWidget.SavedViewPopupMenu viewPopupMenu = savedViewWidget.getSavedViewPopupMenu();
        assertTrue(viewPopupMenu.isNoSavedViewPresent(),
                          "'No Saved Views' is not shown in saved view menu!");

        FilterWidget timeFilter = dashboardsPage.getFilterWidget("date_dimension");
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
    public void renameSavedFilterViewTest() throws InterruptedException {
        initDashboardsPage();
        // change filter value so Selenium can loads all saved views
        dashboardsPage.getFilterWidget("date_dimension").changeTimeFilterValueByClickInTimeLine(PENULTIMATE_YEAR);

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
    public void filterViewNamingUniquenessTest() throws InterruptedException {
        initDashboardsPage();
        // change filter value so Selenium can loads all saved views
        dashboardsPage.getFilterWidget("date_dimension").changeTimeFilterValueByClickInTimeLine(String.valueOf(THIS_YEAR - 3));

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
    public void deleteSavedFilterViewTest() throws InterruptedException {
        initDashboardsPage();
        // change filter value so Selenium can loads all saved views
        FilterWidget filter = dashboardsPage.getFilterWidget("date_dimension");
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

        Thread.sleep(1000);
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
    public void savedFilterAfterSwitchBetweenDashboardsAndPagesTest() throws InterruptedException {
        initDashboardsPage();
        // Add more saved view for first dashboard
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        dashboardsPage.getFilterWidget("date_dimension").changeTimeFilterValueByClickInTimeLine(PENULTIMATE_YEAR);
        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView(PENULTIMATE_YEAR);

        // Create new dashboard SECOND_DASHBOARD_NAME to test
        initNewDashboard_AddFilter_TurnOnSavedView(SECOND_DASHBOARD_NAME);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Department");
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "County");
        dashboardEditBar.saveDashboard();

        // Create saved view 1 "Abundant Foodz"
        Thread.sleep(1000);
        FilterWidget departmentFilter = dashboardsPage.getFilterWidget("department");
        departmentFilter.changeAttributeFilterValue("Abundant Foodz");
        savedViewWidget.openSavedViewMenu();
        savedViewWidget.saveCurrentView("Abundant Foodz");

        assertEquals(dashboardsPage.getFilterWidget("county").getCurrentValue(), "All",
                            "Value of 'County' is not 'All'!");
        Thread.sleep(1000);
        assertEquals(departmentFilter.getCurrentValue(), "Abundant Foodz",
                            "Value of 'Department' is not 'Abundant Foodz'!");

        // Create saved view 2 "Earthy Foodz"
        departmentFilter.changeAttributeFilterValue("Earthy Foodz");
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
        departmentFilter.changeAttributeFilterValue("Family Foodz");
        savedViewWidget.openSavedViewMenu();
        allSavedViewNames = savedViewPopupMenu.getAllSavedViewNames();
        assertEquals(allSavedViewNames.size(), 3);
        for (String name : Arrays.asList("Abundant Foodz", "Earthy Foodz")) {
            assertTrue(allSavedViewNames.contains(name),
                              String.format("'%s' is not in saved view list", name));
        }

        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        initVariablePage();
        initDashboardsPage();
        Thread.sleep(2000);

        assertEquals(waitForElementVisible(savedViewWidget.getRoot()).getText(),
                            PENULTIMATE_YEAR,
                            String.format("Saved view '%s' is not selected!", PENULTIMATE_YEAR));
    }

    @Test(dependsOnMethods = {"savedFilterAfterSwitchBetweenDashboardsAndPagesTest"})
    public void savedViewFilterDoNotApplyOnTimeFilterAfterEditGranularityTest() throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.selectDashboard(FIRST_DASHBOARD_NAME);
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        DashboardEditFilter dashboardEditFilter = dashboardEditBar.getDashboardEditFilter();
        dashboardEditFilter.changeTypeOfTimeFilter("Day");
        dashboardEditBar.saveDashboard();
        assertEquals(waitForElementVisible(dashboardsPage.getSavedViewWidget().getRoot()).getText(),
                            PENULTIMATE_YEAR,
                            String.format("Saved view '%s' is not selected after changing type of time filter!", PENULTIMATE_YEAR));
    }

    @Test(dependsOnMethods = {"savedViewFilterDoNotApplyOnTimeFilterAfterEditGranularityTest"})
    public void savedViewFilterDoNotApplyOnFiltersAfterRemovingTest() throws InterruptedException {
        try {
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            DashboardEditFilter dashboardEditFilter = dashboardEditBar.getDashboardEditFilter();
            dashboardEditFilter.deleteFilter("time");
            dashboardEditBar.saveDashboard();
    
            Thread.sleep(1000);
            // disable checking because of bug: https://jira.intgdc.com/browse/CL-5708
            //assertEquals(waitForElementVisible(SavedViewWidget.SAVED_VIEW_BUTTON, browser).getText(),
            //                    PENULTIMATE_YEAR,
            //                    String.format("Saved view '%s' is not selected after deleting time filter!", PENULTIMATE_YEAR));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"savedViewFilterDoNotApplyOnFiltersAfterRemovingTest"})
    public void dashboardHasManyFiltersInManyTabsTest() throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        dashboardsPage.addNewTab("second tab");
        dashboardEditBar.turnSavedViewOption(true);
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Firstname");
        dashboardEditBar.saveDashboard();

        dashboardsPage.getFilterWidget("firstname").changeAttributeFilterValue("Adam");
        dashboardsPage.getTabs().openTab(0);
        dashboardsPage.getFilterWidget("county").changeAttributeFilterValue("Austin");
        Thread.sleep(1000);

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

    private void initNewDashboard_AddFilter_TurnOnSavedView(String dashboardName) throws InterruptedException {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(dashboardName);

        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addTimeFilterToDashboard(-1, "this");
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

    private void disableSavedFilters(boolean on) throws IOException, JSONException {
        setFeatureFlags(getRestApiClient(), createFeatureClassOption(DISABLE_SAVED_FILTERS, on));
    }
}
