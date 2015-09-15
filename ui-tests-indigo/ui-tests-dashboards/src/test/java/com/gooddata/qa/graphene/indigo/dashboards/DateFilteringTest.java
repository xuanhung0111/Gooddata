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
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class DateFilteringTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkDateFilterDefaultState() {
        DateFilter dateFilter = initIndigoDashboardsPage().waitForDateFilter();
        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterDefaultState-this-month", getClass());

        assertEquals(dateFilterSelection, "This month");
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkDateFilterChangeValue() {
        DateFilter dateFilter = initIndigoDashboardsPage().waitForDateFilter();
        String dateFilterThisYear = "This year";

        dateFilter.selectByName(dateFilterThisYear);

        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterChangeValue-this-year", getClass());

        assertEquals(dateFilterSelection, dateFilterThisYear);
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = "desktop")
    public void testFilterDateByPreset() {
        String attributeFilterMetric = createAttributeFilterMetric();
        String timeMacrosMetric = createTimeMacrosMetric();
        String filteredOutMetric = createFilteredOutMetric();

        setupKpi(attributeFilterMetric, DATE_CREATED);
        setupKpi(timeMacrosMetric, DATE_SNAPSHOT);
        setupKpi(filteredOutMetric, DATE_SNAPSHOT);

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
        return createMetric("Filtered Out Metric", format("SELECT SUM([%s]) WHERE [%s] = 3000",
                getAmountAttributeUri(), getYearSnapshotUri()));
    }

    private String createTimeMacrosMetric() {
        return createMetric("Time Macros Metric", format("SELECT SUM([%s]) WHERE [%s] = THIS - 4",
                getAmountAttributeUri(), getYearSnapshotUri()));
    }

    private String createAttributeFilterMetric() {
        String accountAttribute = getMdService().getObjUri(getProject(), Attribute.class, title(ACCOUNT));

        return createMetric("Attribute Filter Metric", format("SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])",
                getAmountAttributeUri(), accountAttribute, accountAttribute + "/elements?id=961040",
                accountAttribute + "/elements?id=961042", accountAttribute + "/elements?id=958077"));
    }

    private String getAmountAttributeUri() {
        return getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));
    }

    private String getYearSnapshotUri() {
        return getMdService().getObjUri(getProject(), Attribute.class, identifier("snapshot.year"));
    }

    private String createMetric(String name, String expression) {
        getMdService().createObj(getProject(), new Metric(name, expression, "#,##0"));
        return name;
    }
}
