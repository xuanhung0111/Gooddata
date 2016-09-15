package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascase;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

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
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class DateFilteringTest extends GoodSalesAbstractDashboardTest {

    private static final String DEFAULT_METRIC_FORMAT = "#,##0";

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
        
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "mobile"})
    public void checkDateFilterDefaultState() {
        // Dashboard created via REST api has no date filter settings which
        // is identical to the stored "All time" date filter
        DateFilter dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();
        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterDefaultState-all-time", getClass());

        assertEquals(dateFilterSelection, DATE_FILTER_ALL_TIME);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "mobile"})
    public void checkDateFilterChangeValue() {
        DateFilter dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        dateFilter.selectByName(DATE_FILTER_THIS_YEAR);

        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterChangeValue-this-year", getClass());

        assertEquals(dateFilterSelection, DATE_FILTER_THIS_YEAR);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = "desktop")
    public void testInfoMessage() {
        DateFilter dateFilter = initIndigoDashboardsPage()
                .waitForDateFilter();

        dateFilter.ensureDropdownOpen();

        takeScreenshot(browser, "testInfoMessage-hidden", getClass());
        assertFalse(dateFilter.isInfoMessageDisplayed());

        dateFilter = waitForFragmentVisible(indigoDashboardsPage)
                .switchToEditMode()
                .waitForDateFilter();

        dateFilter.ensureDropdownOpen();

        takeScreenshot(browser, "testInfoMessage-displayed", getClass());
        assertTrue(dateFilter.isInfoMessageDisplayed());
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = "desktop")
    public void testDateFilterSwitchToEditMode() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .waitForDateFilter()
                .selectByName(DATE_FILTER_THIS_YEAR);

        waitForFragmentVisible(indigoDashboardsPage)
            .leaveEditMode()
            .waitForWidgetsLoading()
            .waitForDateFilter()
            .selectByName(DATE_FILTER_ALL_TIME);

        String selectionAfterEditModeSwitch = indigoDashboardsPage
                .switchToEditMode()
                .waitForDateFilter()
                .getSelection();

        takeScreenshot(browser, "testDateFilterSwitchToEditMode", getClass());
        assertEquals(selectionAfterEditModeSwitch, DATE_FILTER_THIS_YEAR);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = "desktop")
    public void testFilterDateByPreset() throws JSONException, IOException {
        Metric attributeFilterMetric = createAttributeFilterMetric();
        Metric timeMacrosMetric = createTimeMacrosMetric();
        Metric filteredOutMetric = createFilteredOutMetric();

        String attributeFilterKpiUri = addWidgetToWorkingDashboard(
                createKpiUsingRest(createDefaultKpiConfiguration(attributeFilterMetric, DATE_CREATED)));
        String timeMacrosKpiUri = addWidgetToWorkingDashboard(
                createKpiUsingRest(createDefaultKpiConfiguration(timeMacrosMetric, DATE_SNAPSHOT)));
        String filteredOutKpiUri = addWidgetToWorkingDashboard(
                createKpiUsingRest(createDefaultKpiConfiguration(filteredOutMetric, DATE_SNAPSHOT)));

        try {
            DateFilter dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();
            dateFilter.getValues().stream()
                    .forEach(filter -> {
                        dateFilter.selectByName(filter);
                        indigoDashboardsPage.waitForWidgetsLoading();
                        takeScreenshot(browser, "testFilterDateByPreset-" + filter, getClass());
                        assertTrue(indigoDashboardsPage
                                .getWidgetByHeadline(Kpi.class, filteredOutMetric.getTitle()).isEmptyValue());
                    });
        } finally {
            deleteWidgetsUsingCascase(getRestApiClient(), testParams.getProjectId(), attributeFilterKpiUri,
                    timeMacrosKpiUri, filteredOutKpiUri);
        }
    }

    @DataProvider(name = "dateFilterProvider")
    public Object[][] dateFilterProvider() {
        return new Object[][]{
            {DATE_FILTER_ALL_TIME},
            {DATE_FILTER_THIS_MONTH},
            {DATE_FILTER_THIS_YEAR}
        };
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = "desktop", dataProvider = "dateFilterProvider")
    public void checkDefaultDateInterval(String dateFilterValue) throws JSONException {
        setDefaultDateFilter(dateFilterValue);

        DateFilter dateFilter = waitForFragmentVisible(indigoDashboardsPage).waitForDateFilter();
        takeScreenshot(browser, "Date interval applied", getClass());
        assertEquals(dateFilter.getSelection(), dateFilterValue);

        initDashboardsPage();
        dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        takeScreenshot(browser, "Default date interval when switch to another page then go back", getClass());
        assertEquals(dateFilter.getSelection(), dateFilterValue);

        dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        takeScreenshot(browser, "Default date interval when refresh Indigo dashboard page", getClass());
        assertEquals(dateFilter.getSelection(), dateFilterValue);

        logout();
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

        dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        takeScreenshot(browser, "Default date interval after logout then sign in again", getClass());
        assertEquals(dateFilter.getSelection(), dateFilterValue);
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

    private void setDefaultDateFilter(String dateFilterValue) {
        DateFilter dateFilter = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .waitForDateFilter();

        dateFilter.selectByName(dateFilterValue);
        waitForFragmentVisible(indigoDashboardsPage).leaveEditMode();
    }
}
