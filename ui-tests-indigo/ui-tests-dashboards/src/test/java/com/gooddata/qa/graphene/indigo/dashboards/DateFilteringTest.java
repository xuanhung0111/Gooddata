package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ExtendedDateFilterPanel;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class DateFilteringTest extends AbstractDashboardTest {

    private static final String DEFAULT_METRIC_FORMAT = "#,##0";
    private IndigoRestRequest indigoRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkDateFilterDefaultState() {
        // Dashboard created via REST api has no date filter settings which
        // is identical to the stored "All time" date filter
        ExtendedDateFilterPanel dateFilter = initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel();
        DateRange dateFilterSelection = dateFilter.getSelectedDateFilter();

        takeScreenshot(browser, "checkDateFilterDefaultState-all-time", getClass());

        assertEquals(dateFilterSelection, DateRange.ALL_TIME);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkDateFilterChangeValue() {
        initIndigoDashboardsPageWithWidgets()
            .selectDateFilterByName(DATE_FILTER_THIS_YEAR);

        DateRange dateFilterSelection = indigoDashboardsPage.openExtendedDateFilterPanel().getSelectedDateFilter();

        takeScreenshot(browser, "checkDateFilterChangeValue-this-year", getClass());

        assertEquals(dateFilterSelection, DateRange.THIS_YEAR);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = "desktop")
    public void testInfoMessage() {
        ExtendedDateFilterPanel dateFilter = initIndigoDashboardsPage().openExtendedDateFilterPanel();

        takeScreenshot(browser, "testInfoMessage-hidden", getClass());
        assertFalse(dateFilter.isInfoMessageDisplayed());

        dateFilter = waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode().openExtendedDateFilterPanel();

        indigoDashboardsPage.openExtendedDateFilterPanel();

        takeScreenshot(browser, "testInfoMessage-displayed", getClass());
        assertTrue(dateFilter.isInfoMessageDisplayed());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = "desktop")
    public void testDateFilterSwitchToEditMode() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode().selectDateFilterByName(DATE_FILTER_THIS_YEAR);

        waitForFragmentVisible(indigoDashboardsPage)
            .leaveEditMode()
            .waitForWidgetsLoading()
            .selectDateFilterByName(DATE_FILTER_ALL_TIME);

        DateRange selectionAfterEditModeSwitch = indigoDashboardsPage
                .switchToEditMode().openExtendedDateFilterPanel().getSelectedDateFilter();

        takeScreenshot(browser, "testDateFilterSwitchToEditMode", getClass());
        assertEquals(selectionAfterEditModeSwitch, DateRange.THIS_YEAR);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = "desktop")
    public void testFilterDateByPreset() throws JSONException, IOException {
        Metric attributeFilterMetric = createAttributeFilterMetric();
        Metric timeMacrosMetric = createTimeMacrosMetric();
        Metric filteredOutMetric = createFilteredOutMetric();

        String attributeFilterKpiUri = addWidgetToWorkingDashboardFluidLayout(
                createKpiUsingRest(createDefaultKpiConfiguration(attributeFilterMetric, DATE_DATASET_CREATED)), 0);
        String timeMacrosKpiUri = addWidgetToWorkingDashboardFluidLayout(
                createKpiUsingRest(createDefaultKpiConfiguration(timeMacrosMetric, DATE_DATASET_SNAPSHOT)), 0);
        String filteredOutKpiUri = addWidgetToWorkingDashboardFluidLayout(
                createKpiUsingRest(createDefaultKpiConfiguration(filteredOutMetric, DATE_DATASET_SNAPSHOT)), 0);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode();
                List<DateRange> dateRanges = Arrays.asList(DateRange.ALL_TIME, DateRange.LAST_7_DAYS,
                    DateRange.LAST_30_DAYS, DateRange.LAST_90_DAYS, DateRange.THIS_MONTH, DateRange.LAST_MONTH,
                    DateRange.LAST_12_MONTHS, DateRange.THIS_QUARTER, DateRange.LAST_QUARTER, DateRange.LAST_4_QUARTERS,
                    DateRange.THIS_YEAR, DateRange.LAST_YEAR);
                    dateRanges.forEach(filter -> {
                        indigoDashboardsPage.selectDateFilterByName(filter.toString());
                        indigoDashboardsPage.waitForWidgetsLoading();
                        takeScreenshot(browser, "testFilterDateByPreset-" + filter, getClass());
                        assertTrue(indigoDashboardsPage
                                .getWidgetByHeadline(Kpi.class, filteredOutMetric.getTitle()).isEmptyValue());
                    });
        } finally {
            indigoRestRequest.deleteWidgetsUsingCascade(attributeFilterKpiUri,
                    timeMacrosKpiUri, filteredOutKpiUri);
        }
    }

    @DataProvider(name = "dateFilterProvider")
    public Object[][] dateFilterProvider() {
        return new Object[][]{
            {DateRange.ALL_TIME},
            {DateRange.THIS_MONTH},
            {DateRange.THIS_YEAR}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = "desktop", dataProvider = "dateFilterProvider")
    public void checkDefaultDateInterval(DateRange dateFilterValue) throws JSONException {
        setDefaultDateFilter(dateFilterValue);

        ExtendedDateFilterPanel dateFilter = waitForFragmentVisible(indigoDashboardsPage).openExtendedDateFilterPanel();
        takeScreenshot(browser, "Date interval applied", getClass());
        assertEquals(dateFilter.getSelectedDateFilter(), dateFilterValue);

        initDashboardsPage();
        dateFilter = initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel();

        takeScreenshot(browser, "Default date interval when switch to another page then go back", getClass());
        assertEquals(dateFilter.getSelectedDateFilter(), dateFilterValue);

        dateFilter = initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel();

        takeScreenshot(browser, "Default date interval when refresh Indigo dashboard page", getClass());
        assertEquals(dateFilter.getSelectedDateFilter(), dateFilterValue);

        logout();
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

        dateFilter = initIndigoDashboardsPageWithWidgets().openExtendedDateFilterPanel();

        takeScreenshot(browser, "Default date interval after logout then sign in again", getClass());
        assertEquals(dateFilter.getSelectedDateFilter(), dateFilterValue);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void testDateFilterStatusWithEditor() throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().openExtendedDateFilterPanel()
                .selectPeriod(DateRange.THIS_YEAR).apply();
            
            Kpi kpi = indigoDashboardsPage.selectLastWidget(Kpi.class);
            assertEquals(kpi.getValue(), "–");

            ConfigurationPanel panel = indigoDashboardsPage.getConfigurationPanel();
            assertTrue(panel.getFilterByDateFilter().isChecked(), "The date filter is not checked by default");

            panel.disableDateFilter();

            assertTrue(kpi.waitForContentLoading().getValue().matches("^\\$(\\d{1,3})(,\\d{3})*(\\.\\d{1,})?$"),
                    "The value format of kpi is not a valid currency format");

            indigoDashboardsPage.cancelEditModeWithChanges().switchToEditMode().selectLastWidget(Kpi.class);
            assertTrue(panel.getFilterByDateFilter().isChecked(), "The date filter is not checked by default");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private Metric createFilteredOutMetric() {
        return createMetric("Filtered Out Metric", format("SELECT SUM([%s]) WHERE [%s] = 3000",
                getAmountAttributeUri(), getYearSnapshotUri()),
                DEFAULT_METRIC_FORMAT);
    }

    private Metric createTimeMacrosMetric() {
        return createMetric("Time Macros Metric", format("SELECT SUM([%s]) WHERE [%s] = THIS - 4",
                getAmountAttributeUri(), getYearSnapshotUri()),
                DEFAULT_METRIC_FORMAT);
    }

    private Metric createAttributeFilterMetric() {
        String accountAttribute = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_ACCOUNT));

        return createMetric("Attribute Filter Metric", format("SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])",
                getAmountAttributeUri(), accountAttribute, accountAttribute + "/elements?id=961040",
                accountAttribute + "/elements?id=961042", accountAttribute + "/elements?id=958077"),
                DEFAULT_METRIC_FORMAT);
    }

    private String getAmountAttributeUri() {
        return getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));
    }

    private String getYearSnapshotUri() {
        return getMdService().getObjUri(getProject(), Attribute.class, identifier("snapshot.year"));
    }

    private void setDefaultDateFilter(DateRange dateFilterValue) {
        ExtendedDateFilterPanel dateFilter = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .openExtendedDateFilterPanel();

        dateFilter.selectPeriod(dateFilterValue).apply();
        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();
    }
}
