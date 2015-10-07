package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KpiPopTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkKpiPopInMobile() {
        Kpi amount = initIndigoDashboardsPage().getKpiByHeadline(AMOUNT);
        assertFalse(amount.hasPopSection());

        Kpi lost = initIndigoDashboardsPage().getKpiByHeadline(LOST);
        assertTrue(lost.hasPopSection());
        assertEquals(lost.getPopSection().getChangeTitle(), "change");
        assertEquals(lost.getPopSection().getPeriodTitle(), "prev. year");

        Kpi numberOfActivities = initIndigoDashboardsPage().getKpiByHeadline(NUMBER_OF_ACTIVITIES);
        assertTrue(numberOfActivities.hasPopSection());
        assertEquals(numberOfActivities.getPopSection().getChangeTitle(), "change");
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. month");

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_ALL_TIME);
        takeScreenshot(browser, "checkKpiPopInMobile-allTime", getClass());
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. period");
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasPopSection() {
        Kpi justAddedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .selectLastKpi();

        assertTrue(justAddedKpi.hasPopSection());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        takeScreenshot(browser, "checkNewlyAddedKpiHasPopSection", getClass());
        assertTrue(lastKpi.hasPopSection());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditMode();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiWithoutComparison() {
        Kpi kpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED, Kpi.ComparisonType.NO_COMPARISON)
            .selectLastKpi();

        assertFalse(kpi.hasPopSection());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        takeScreenshot(browser, "checkKpiWithoutComparison", getClass());
        assertFalse(lastKpi.hasPopSection());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditMode();
    }

    @DataProvider(name = "popProvider")
    public Object[][] popProvider() {
        return new Object[][] {
            // comparison type, date filter, prev. title for the date filter
            {Kpi.ComparisonType.LAST_YEAR, DATE_FILTER_THIS_MONTH, "prev. year"},
            {Kpi.ComparisonType.PREVIOUS_PERIOD, DATE_FILTER_THIS_MONTH, "prev. month"},
            {Kpi.ComparisonType.LAST_YEAR, DATE_FILTER_THIS_QUARTER, "prev. year"},
            {Kpi.ComparisonType.PREVIOUS_PERIOD, DATE_FILTER_THIS_QUARTER, "prev. quarter"},
            {Kpi.ComparisonType.LAST_YEAR, DATE_FILTER_ALL_TIME, "prev. year"},
            {Kpi.ComparisonType.PREVIOUS_PERIOD, DATE_FILTER_ALL_TIME, "prev. period"}
        };
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, dataProvider = "popProvider", groups = {"desktop"})
    public void checkKpiPopSection(Kpi.ComparisonType comparisonType, String dateFilter, String expectedPeriodTitle) {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED, comparisonType)
            .saveEditMode();

        Kpi kpi = initIndigoDashboardsPage()
            .getLastKpi();

        indigoDashboardsPage.selectDateFilterByName(dateFilter);

        takeScreenshot(browser, "checkKpiPopSection-" + comparisonType + "-" + dateFilter + "-" + expectedPeriodTitle, getClass());
        assertEquals(kpi.getPopSection().getChangeTitle(), "change");
        assertEquals(kpi.getPopSection().getPeriodTitle(), expectedPeriodTitle);

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditMode();
    }
}
