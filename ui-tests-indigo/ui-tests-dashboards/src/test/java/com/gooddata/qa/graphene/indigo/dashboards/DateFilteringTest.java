package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DateFilter;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.annotations.Test;

public class DateFilteringTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkDateFilterDefaultState() {
        DateFilter dateFilter = initIndigoDashboardsPage().waitForDateFilter();
        String dateFilterSelection = dateFilter.getSelection();

        takeScreenshot(browser, "checkDateFilterDefaultState-all-time", getClass());

        assertEquals(dateFilterSelection, "All time");
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

}
