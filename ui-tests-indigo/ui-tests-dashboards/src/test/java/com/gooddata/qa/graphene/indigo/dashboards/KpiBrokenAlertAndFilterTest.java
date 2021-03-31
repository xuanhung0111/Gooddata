package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.BY_ERROR_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFilter;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class KpiBrokenAlertAndFilterTest extends AbstractDashboardTest {

    private static final String KPI_ALERT_THRESHOLD = "100";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Override
    protected void customizeProject() throws Throwable {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),testParams.getProjectId());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkBrokenAlertRemoveAttributeFilter() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createNumOfActivitiesKpi(), 0);
        try {
            initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT)
                .addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales").saveEditModeWithWidgets();

            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_ACCOUNT)
                .deleteAttributeFilter(ATTR_DEPARTMENT).saveEditModeWithWidgets();

            KpiAlertDialog kpiAlertDialog = getLastKpiAfterAlertsLoaded().openAlertDialog();
            assertEquals(kpiAlertDialog.getFilterSectionBrokenDialogHeader(), "FILTERS REMOVED FROM DASHBOARD");

            browser.navigate().refresh();
            waitForFragmentVisible(indigoDashboardsPage);
            assertEquals(waitForElementVisible(BY_ERROR_MESSAGE_BAR, browser).getText(), "Someone disabled or removed filters" +
                " that you use to watch for changes to your KPIs. To see the correct KPI values, remove the broken alerts, or update" +
                " the KPIs individually. Alternatively, enter edit mode and add the removed filters back, or re-enable the disabled filters.");

            getLastKpiAfterAlertsLoaded().openAlertDialog().updateFiltersOnBrokenAlert();
            getLastKpiAfterAlertsLoaded().openAlertDialog().deleteAlert();
            
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkBrokenAlertUncheckAttributeAndDateFilter() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createNumOfActivitiesKpi(), 0);

        try {
            initIndigoDashboardsPage().switchToEditMode()
                .addAttributeFilter(ATTR_ACCOUNT)
                .addAttributeFilter(ATTR_DEPARTMENT, "Direct Sales")
                .selectDateFilterByName(DATE_FILTER_THIS_MONTH)
                .saveEditModeWithWidgets();

            waitForFragmentVisible(indigoDashboardsPage);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            indigoDashboardsPage.switchToEditMode().getLastWidget(Kpi.class).clickOnContent();
            indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilter(ATTR_ACCOUNT).setChecked(false);
            indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilter(ATTR_DEPARTMENT).setChecked(false);
            indigoDashboardsPage.getConfigurationPanel().getFilterByDateFilter().setChecked(false);
            indigoDashboardsPage.saveEditModeWithWidgets();

            KpiAlertDialog kpiAlertDialog = getLastKpiAfterAlertsLoaded().openAlertDialog();
            assertEquals(kpiAlertDialog.getFilterSectionBrokenDialogHeader(), "FILTERS IGNORED FOR THIS KPI");

            getLastKpiAfterAlertsLoaded().openAlertDialog().updateFiltersOnBrokenAlert();
            getLastKpiAfterAlertsLoaded().openAlertDialog().setAlert();

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiAlertResetFilters() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboardFluidLayout(createAmountKpi(), 0);

        try {
            initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_DEPARTMENT)
                .selectDateFilterByName(DATE_FILTER_THIS_MONTH).saveEditModeWithWidgets();

            waitForFragmentVisible(indigoDashboardsPage);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, KPI_ALERT_THRESHOLD);

            indigoDashboardsPage.switchToEditMode()
                .getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT).clearAllCheckedValues().selectByName("Direct Sales");
            indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_THIS_QUARTER).saveEditModeWithWidgets();

            indigoDashboardsPage.getLastWidget(Kpi.class).openAlertDialog().applyAlertFilters();
            indigoDashboardsPage.waitForWidgetsLoading();

            DateRange dateFilterSelection = indigoDashboardsPage.openExtendedDateFilterPanel().getSelectedDateFilter();
            assertEquals(dateFilterSelection, DateRange.THIS_MONTH);

            AttributeFilter departmentFilter = indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT);
            assertEquals(departmentFilter.getSelectedItems(), "All");

        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(kpiUri);
        }
    }

    private Kpi getLastKpiAfterAlertsLoaded() {
        return waitForFragmentVisible(indigoDashboardsPage)
            .waitForWidgetsLoading()
            .waitForAlertsLoaded()
            .getLastWidget(Kpi.class);
    }
}
