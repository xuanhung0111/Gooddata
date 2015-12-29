package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class KpiPopTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"mobile"})
    public void checkKpiPopInMobile() {
        Kpi amount = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(AMOUNT);
        assertFalse(amount.hasPopSection());

        Kpi lost = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(LOST);
        assertTrue(lost.hasPopSection());

        // "All time" is the default filter
        assertEquals(lost.getPopSection().getChangeTitle(), "change");
        assertEquals(lost.getPopSection().getPeriodTitle(), "prev. year");

        Kpi numberOfActivities = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(NUMBER_OF_ACTIVITIES);
        assertTrue(numberOfActivities.hasPopSection());

        assertEquals(numberOfActivities.getPopSection().getChangeTitle(), "change");
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. period");

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_THIS_MONTH);
        takeScreenshot(browser, "checkKpiPopInMobile-thisMonth", getClass());
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. month");
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasPopSection() {
        Kpi justAddedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .build())
            .selectLastKpi();

        assertTrue(justAddedKpi.hasPopSection());

        indigoDashboardsPage
            .saveEditModeWithKpis();

        Kpi lastKpi = initIndigoDashboardsPageWithWidgets()
            .getLastKpi();

        takeScreenshot(browser, "checkNewlyAddedKpiHasPopSection", getClass());
        assertTrue(lastKpi.hasPopSection());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithKpis();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkKpiWithoutComparison() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .build())
            .selectLastKpi();

        assertFalse(kpi.hasPopSection());

        indigoDashboardsPage
            .saveEditModeWithKpis();

        Kpi lastKpi = initIndigoDashboardsPageWithWidgets()
            .getLastKpi();

        takeScreenshot(browser, "checkKpiWithoutComparison", getClass());
        assertFalse(lastKpi.hasPopSection());

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditModeWithKpis();
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
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addWidget(new KpiConfiguration.Builder()
                .metric(AMOUNT)
                .dateDimension(DATE_CREATED)
                .comparison(comparisonType.toString())
                .build())
            .saveEditModeWithKpis();

        Kpi kpi = initIndigoDashboardsPageWithWidgets()
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
            .saveEditModeWithKpis();
    }
}
