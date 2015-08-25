package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class KpiPopTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasPopSection() {
        Kpi justAddedKpi = initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED)
            .selectLastKpi();

        Assert.assertTrue(justAddedKpi.hasPopSection());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        takeScreenshot(browser, "checkNewlyAddedKpiHasPopSection", getClass());
        Assert.assertTrue(lastKpi.hasPopSection());

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
            .addWidget(AMOUNT, DATE_CREATED, COMPARISON_NO_COMPARISON)
            .selectLastKpi();

        Assert.assertFalse(kpi.hasPopSection());

        indigoDashboardsPage
            .saveEditMode();

        Kpi lastKpi = initIndigoDashboardsPage()
            .getLastKpi();

        takeScreenshot(browser, "checkKpiWithoutComparison", getClass());
        Assert.assertFalse(lastKpi.hasPopSection());

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
            {COMPARISON_LAST_YEAR, DATE_FILTER_THIS_MONTH, "prev. year"},
            {COMPARISON_PREVIOUS_PERIOD, DATE_FILTER_THIS_MONTH, "prev. month"},
            {COMPARISON_LAST_YEAR, DATE_FILTER_THIS_QUARTER, "prev. year"},
            {COMPARISON_PREVIOUS_PERIOD, DATE_FILTER_THIS_QUARTER, "prev. quarter"},
            {COMPARISON_LAST_YEAR, DATE_FILTER_ALL_TIME, "prev. year"},
            {COMPARISON_PREVIOUS_PERIOD, DATE_FILTER_ALL_TIME, "prev. period"}
        };
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, dataProvider = "popProvider", groups = {"desktop"})
    public void checkKpiPopSection(String comparisonType, String dateFilter, String expectedPeriodTitle) {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .addWidget(AMOUNT, DATE_CREATED, comparisonType)
            .saveEditMode();

        Kpi kpi = initIndigoDashboardsPage()
            .getLastKpi();

        indigoDashboardsPage.selectDateFilterByName(dateFilter);

        takeScreenshot(browser, "checkKpiPopSection-" + comparisonType + "-" + dateFilter + "-" + expectedPeriodTitle, getClass());
        Assert.assertEquals(kpi.getPopSection().getChangeTitle(), "change");
        Assert.assertEquals(kpi.getPopSection().getPeriodTitle(), expectedPeriodTitle);

        indigoDashboardsPage
            .switchToEditMode()
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
            .saveEditMode();
    }
}
