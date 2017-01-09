package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;
import static com.gooddata.md.Restriction.title;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.FilterByItem;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteWidgetsUsingCascade;
import java.util.Arrays;
import static java.util.Collections.singletonList;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class FilteringWidgetsTest extends GoodSalesAbstractDashboardTest {

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"setupFilters", "desktop"})
    public void setupFilters() {
        initIndigoDashboardsPage()
                .switchToEditMode()
                .addAttributeFilter(ATTR_ACCOUNT)
                .addAttributeFilter(ATTR_DEPARTMENT)
                .saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"setupFilters"}, groups = {"desktop"})
    public void testNumberOfAttributeFiltersMatchesNumberOfIgnoreCheckboxes()
            throws JSONException, IOException {

        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            List<FilterByItem> filters = indigoDashboardsPage
                    .getConfigurationPanel()
                    .getFilterByAttributeFilters();

            takeScreenshot(browser, "attribute-filter-ignore-checkboxes-default", getClass());
            assertEquals(filters.size(), 2);

            indigoDashboardsPage.leaveEditMode();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"setupFilters"}, groups = {"desktop"})
    public void testIgnoreAttributeFilterIsPersisted() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            // all checked at the beginning
            List<Boolean> expected = Arrays.asList(new Boolean[] {true, true});
            assertThat(getFilterByCheckValues(), is(expected));

            // uncheck first filter ( = is ignored)
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .getFilterByAttributeFilters()
                    .get(0)
                    .setChecked(false);

            indigoDashboardsPage.saveEditModeWithWidgets();

            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            List<Boolean> expectedAfterPersist = Arrays.asList(new Boolean[] {false, true});
            List<Boolean> actualValues = getFilterByCheckValues();

            takeScreenshot(browser, "kpi-attribute-filter-ignore-persisted", getClass());
            assertThat(actualValues, is(expectedAfterPersist));

            indigoDashboardsPage.leaveEditMode();

            //refresh page and check again the ignored attribute filters settings
            browser.navigate().refresh();
            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            actualValues = getFilterByCheckValues();

            takeScreenshot(browser, "kpi-attribute-filter-ignore-persisted-after-refreshing-page", getClass());
            assertThat(actualValues, is(expectedAfterPersist));

            indigoDashboardsPage.leaveEditMode();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"setupFilters"}, groups = {"desktop"})
    public void testIgnoreAttributeFilterIsNotPersistedAfterCancelSavingDashboard()
            throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            // all checked at the beginning
            List<Boolean> expected = Arrays.asList(new Boolean[] {true, true});
            assertThat(getFilterByCheckValues(), is(expected));

            // uncheck first filter ( = is ignored)
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .getFilterByAttributeFilters()
                    .get(0)
                    .setChecked(false);

            indigoDashboardsPage.cancelEditModeWithChanges();

            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            List<Boolean> expectedAfterPersist = Arrays.asList(new Boolean[] {true, true});
            List<Boolean> actualValues = getFilterByCheckValues();

            takeScreenshot(browser, "kpi-attribute-filter-ignore-not-persisted-if-cancel-dashboard-saving",
                    getClass());
            assertThat(actualValues, is(expectedAfterPersist));

            indigoDashboardsPage.leaveEditMode();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    @Test(dependsOnGroups = {"setupFilters"}, groups = {"desktop"})
    public void testIgnoreDateFilterIsPersisted() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());

        try {
            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            // uncheck date
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .getFilterByDateFilter()
                    .setChecked(false);

            indigoDashboardsPage.saveEditModeWithWidgets();

            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);

            // check that date is unchecked
            boolean isDateChecked = indigoDashboardsPage
                    .getConfigurationPanel()
                    .getFilterByDateFilter()
                    .isChecked();

            takeScreenshot(browser, "kpi-date-filter-ignore-persisted", getClass());
            assertFalse(isDateChecked);

            indigoDashboardsPage.leaveEditMode();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }


    @Test(dependsOnGroups = {"setupFilters"}, groups = {"desktop"})
    public void testIgnoreAttributeFiltersForVisualizationIsPersisted() throws JSONException, IOException {

        Metric metric = getMdService().getObj(getProject(), Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES));
        String widgetUri = createInsightWidget(new InsightMDConfiguration("Modified-Date-Filter-Insight",
                ReportType.COLUMN_CHART).setMeasureBucket(
                        singletonList(MeasureBucket.getSimpleInstance(metric))));

        addWidgetToWorkingDashboard(widgetUri);

        try {
            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Insight.class);

            // all checked at the beginning
            List<Boolean> expected = Arrays.asList(new Boolean[] {true, true});
            assertThat(getFilterByCheckValues(), is(expected));

            // uncheck second filter ( = is ignored)
            indigoDashboardsPage
                    .getConfigurationPanel()
                    .getFilterByAttributeFilters()
                    .get(1)
                    .setChecked(false);

            indigoDashboardsPage.saveEditModeWithWidgets();

            initIndigoDashboardsPage()
                    .switchToEditMode()
                    .selectLastWidget(Insight.class);

            List<Boolean> expectedAfterPersist = Arrays.asList(new Boolean[] {true, false});
            List<Boolean> actualValues = getFilterByCheckValues();

            takeScreenshot(browser, "insight-attribute-filter-ignore-persisted", getClass());
            assertThat(actualValues, is(expectedAfterPersist));

            indigoDashboardsPage.leaveEditMode();
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), widgetUri);
        }

    }

    @Test(dependsOnGroups = {"setupFilters"}, groups = {"desktop"})
    public void testAttributeFiltersForKpiCorrectlyApplied() throws JSONException, IOException {
        String kpiUri = addWidgetToWorkingDashboard(createAmountKpi());
        String notFiltered = "$116,625,456.54";
        String filtered = "$80,406,324.96";
        String DIRECT_SALES_DEPARTMENT = "Direct Sales";

        try {
            initIndigoDashboardsPageWithWidgets();

            takeScreenshot(browser, "testAttributeFiltersForKpiCorrectlyApplied-valueNotFiltered", getClass());
            assertEquals(getLastKpiValue(), notFiltered);

            setFilterValues(ATTR_DEPARTMENT, DIRECT_SALES_DEPARTMENT);

            takeScreenshot(browser, "testAttributeFiltersForKpiCorrectlyApplied-valueFiltered", getClass());
            assertEquals(getLastKpiValue(), filtered);

            indigoDashboardsPage
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);
            setFilterByChecked(ATTR_DEPARTMENT, false);
            indigoDashboardsPage.saveEditModeWithWidgets();

            setFilterValues(ATTR_DEPARTMENT, DIRECT_SALES_DEPARTMENT);

            // check value is not filtered
            takeScreenshot(browser, "testAttributeFiltersForKpiCorrectlyApplied-valueNotFiltered-filterIgnored", getClass());
            assertEquals(getLastKpiValue(), notFiltered);

            indigoDashboardsPage
                    .switchToEditMode()
                    .selectLastWidget(Kpi.class);
            setFilterByChecked(ATTR_DEPARTMENT, true);
            indigoDashboardsPage.saveEditModeWithWidgets();

            setFilterValues(ATTR_DEPARTMENT, DIRECT_SALES_DEPARTMENT);

            // check value is filtered again
            takeScreenshot(browser, "testAttributeFiltersForKpiCorrectlyApplied-valueNotFiltered-valueFilteredAgain", getClass());
            assertEquals(getLastKpiValue(), filtered);
        } finally {
            deleteWidgetsUsingCascade(getRestApiClient(), testParams.getProjectId(), kpiUri);
        }
    }

    private List<Boolean> getFilterByCheckValues() {
        return indigoDashboardsPage
                .getConfigurationPanel()
                .getFilterByAttributeFilters()
                .stream()
                .map(FilterByItem::isChecked)
                .collect(toList());
    }

    private String getLastKpiValue() {
        return indigoDashboardsPage
                .getLastWidget(Kpi.class)
                .getValue();
    }

    private void setFilterValues(String attributeName, String... values) {
        indigoDashboardsPage
                .getAttributeFiltersPanel()
                .getAttributeFilter(attributeName)
                .clearAllCheckedValues()
                .selectByNames(values);
    }

    private void setFilterByChecked(String attributeName, boolean checked) {
        indigoDashboardsPage
                .getConfigurationPanel()
                .getFilterByAttributeFilter(attributeName)
                .setChecked(checked);
    }
}
