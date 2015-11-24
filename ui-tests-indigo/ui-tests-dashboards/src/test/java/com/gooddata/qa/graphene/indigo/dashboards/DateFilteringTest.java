package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static org.testng.Assert.assertFalse;

public class DateFilteringTest extends DashboardWithWidgetsTest {
    private static final String DEFAULT_METRIC_FORMAT = "#,##0";

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkDateFilterDefaultState() {
        DateFilter dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();
        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterDefaultState-all-time", getClass());

        assertEquals(dateFilterSelection, DATE_FILTER_ALL_TIME);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkDateFilterChangeValue() {
        DateFilter dateFilter = initIndigoDashboardsPageWithWidgets().waitForDateFilter();

        dateFilter.selectByName(DATE_FILTER_THIS_YEAR);

        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterChangeValue-this-year", getClass());

        assertEquals(dateFilterSelection, DATE_FILTER_THIS_YEAR);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = "desktop")
    public void testInfoMessage() {
        DateFilter dateFilter = initIndigoDashboardsPage()
                .waitForDateFilter();

        dateFilter.ensureDropdownOpen();

        takeScreenshot(browser, "testInfoMessage-hidden", getClass());
        assertFalse(dateFilter.isInfoMessageDisplayed());

        dateFilter = indigoDashboardsPage
                .switchToEditMode()
                .waitForDateFilter();

        dateFilter.ensureDropdownOpen();

        takeScreenshot(browser, "testInfoMessage-displayed", getClass());
        assertTrue(dateFilter.isInfoMessageDisplayed());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = "desktop")
    public void testDefaultDateFilterStored() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .waitForDateFilter()
                .selectByName(DATE_FILTER_THIS_YEAR);

        indigoDashboardsPage.leaveEditMode();

        String selectionAfterReload = initIndigoDashboardsPageWithWidgets()
                .waitForDateFilter()
                .getSelection();

        takeScreenshot(browser, "testDefaultDateFilterStored", getClass());
        assertEquals(selectionAfterReload, DATE_FILTER_THIS_YEAR);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = "desktop")
    public void testDateFilterSwitchToEditMode() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .waitForDateFilter()
                .selectByName(DATE_FILTER_THIS_YEAR);

        indigoDashboardsPage.leaveEditMode();

        initIndigoDashboardsPageWithWidgets()
                .waitForDateFilter()
                .selectByName(DATE_FILTER_ALL_TIME);

        indigoDashboardsPage.switchToEditMode();

        String selectionAfterEditModeSwitch = indigoDashboardsPage
                .waitForDateFilter()
                .getSelection();

        takeScreenshot(browser, "testDateFilterSwitchToEditMode", getClass());
        assertEquals(selectionAfterEditModeSwitch, DATE_FILTER_THIS_YEAR);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = "desktop")
    public void testFilterDateByPreset() {
        String attributeFilterMetric = createAttributeFilterMetric();
        String timeMacrosMetric = createTimeMacrosMetric();
        String filteredOutMetric = createFilteredOutMetric();

        setupKpi(new KpiConfiguration.Builder()
            .metric(attributeFilterMetric)
            .dateDimension(DATE_CREATED)
            .build()
        );
        setupKpi(new KpiConfiguration.Builder()
            .metric(timeMacrosMetric)
            .dateDimension(DATE_SNAPSHOT)
            .build()
        );
        setupKpi(new KpiConfiguration.Builder()
            .metric(filteredOutMetric)
            .dateDimension(DATE_SNAPSHOT)
            .build()
        );

        try {
            DateFilter dateFilter = indigoDashboardsPage.waitForDateFilter();
            dateFilter.getValues().stream()
                .forEach(filter -> {
                    dateFilter.selectByName(filter);
                    indigoDashboardsPage.waitForAllKpiWidgetContentLoaded();
                    takeScreenshot(browser, "testFilterDateByPreset-" + filter, getClass());
                    assertTrue(indigoDashboardsPage.getKpiByHeadline(filteredOutMetric).isEmptyValue());
                });
        } finally {
            teardownKpi();
            teardownKpi();
            teardownKpi();
        }
    }

    private String createFilteredOutMetric() {
        String metricName = "Filtered Out Metric";

        createMetric(metricName, format("SELECT SUM([%s]) WHERE [%s] = 3000",
                getAmountAttributeUri(), getYearSnapshotUri()),
                DEFAULT_METRIC_FORMAT);

        return metricName;
    }

    private String createTimeMacrosMetric() {
        String metricName = "Time Macros Metric";

        createMetric(metricName, format("SELECT SUM([%s]) WHERE [%s] = THIS - 4",
                getAmountAttributeUri(), getYearSnapshotUri()),
                DEFAULT_METRIC_FORMAT);

        return metricName;
    }

    private String createAttributeFilterMetric() {
        String metricName = "Attribute Filter Metric";
        String accountAttribute = getMdService().getObjUri(getProject(), Attribute.class, title(ACCOUNT));

        createMetric(metricName, format("SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])",
                getAmountAttributeUri(), accountAttribute, accountAttribute + "/elements?id=961040",
                accountAttribute + "/elements?id=961042", accountAttribute + "/elements?id=958077"),
                DEFAULT_METRIC_FORMAT);

        return metricName;
    }

    private String getAmountAttributeUri() {
        return getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));
    }

    private String getYearSnapshotUri() {
        return getMdService().getObjUri(getProject(), Attribute.class, identifier("snapshot.year"));
    }
}
