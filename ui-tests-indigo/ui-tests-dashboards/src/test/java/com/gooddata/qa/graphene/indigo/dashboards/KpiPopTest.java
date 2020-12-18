package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class KpiPopTest extends AbstractDashboardTest {

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        final List<String> kpiUris = asList(createAmountKpi(), createLostKpi(), createNumOfActivitiesKpi());
        new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .createAnalyticalDashboard(kpiUris);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"mobile"})
    public void checkKpiPopInMobile() {
        Kpi amount = initIndigoDashboardsPageWithWidgets().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT);
        assertFalse(amount.hasPopSection(), METRIC_AMOUNT + " KPI shouldn't have pop section");

        Kpi lost = waitForFragmentVisible(indigoDashboardsPage).getWidgetByHeadline(Kpi.class, METRIC_LOST);
        assertTrue(lost.hasPopSection(), METRIC_LOST + " KPI should have pop section");

        Kpi numberOfActivities = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(numberOfActivities.hasPopSection(), METRIC_NUMBER_OF_ACTIVITIES + " KPI should have pop section");

        assertEquals(numberOfActivities.getPopSection().getChangeTitle(), "change");
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. period");

        // When project is created by REST API (and not using SplashScreen)
        // "All time" is the initial filter --> switch to "This month"
        indigoDashboardsPage
                .openExtendedDateFilterPanel().selectPeriod(DateRange.THIS_MONTH).apply();

        takeScreenshot(browser, "checkKpiPopInMobile-thisMonth", getClass());

        assertEquals(lost.getPopSection().getChangeTitle(), "change");
        assertEquals(lost.getPopSection().getPeriodTitle(), "prev. year");

        numberOfActivities = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_NUMBER_OF_ACTIVITIES);
        assertTrue(numberOfActivities.hasPopSection(), METRIC_NUMBER_OF_ACTIVITIES + " KPI should have pop section");

        assertEquals(numberOfActivities.getPopSection().getChangeTitle(), "change");
        assertEquals(numberOfActivities.getPopSection().getPeriodTitle(), "prev. month");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkNewlyAddedKpiHasPopSection() {
        Kpi justAddedKpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addKpi(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_DATASET_CREATED)
                .build())
            .selectLastWidget(Kpi.class);

        assertTrue(justAddedKpi.hasPopSection(), "KPI should have pop section");

        Kpi lastKpi = indigoDashboardsPage.saveEditModeWithWidgets().getLastWidget(Kpi.class);

        takeScreenshot(browser, "checkNewlyAddedKpiHasPopSection", getClass());
        assertTrue(lastKpi.hasPopSection(), "Newly added KPI should have pop section");

        indigoDashboardsPage.switchToEditMode().getLastWidget(Kpi.class).delete();
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiWithoutComparison() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addKpi(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_DATASET_CREATED)
                .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                .build())
            .selectLastWidget(Kpi.class);

        assertFalse(kpi.hasPopSection(), "KPI shouldn't have pop section");

        waitForFragmentVisible(indigoDashboardsPage)
            .saveEditModeWithWidgets();

        Kpi lastKpi = indigoDashboardsPage.getLastWidget(Kpi.class);

        takeScreenshot(browser, "checkKpiWithoutComparison", getClass());
        assertFalse(lastKpi.hasPopSection(), "Newly added KPI shouldn't have pop section");

        indigoDashboardsPage.switchToEditMode().getLastWidget(Kpi.class).delete();
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @DataProvider(name = "popProvider")
    public Object[][] popProvider() {
        return new Object[][] {
            // comparison type, date filter, prev. title for the date filter
            {ComparisonType.PREVIOUS_PERIOD, DateRange.THIS_MONTH, "prev. month"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.THIS_QUARTER, "prev. quarter"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.ALL_TIME, "prev. period"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.LAST_7_DAYS, "prev. 7d"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.LAST_12_MONTHS, "prev. 12m"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.LAST_4_QUARTERS, "prev. 4q"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.THIS_YEAR, "prev. year"},
            {ComparisonType.PREVIOUS_PERIOD, DateRange.LAST_YEAR, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.THIS_MONTH, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.THIS_QUARTER, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.ALL_TIME, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.LAST_7_DAYS, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.LAST_12_MONTHS, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.LAST_4_QUARTERS, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.THIS_YEAR, "prev. year"},
            {ComparisonType.LAST_YEAR, DateRange.LAST_YEAR, "prev. year"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "popProvider", groups = {"desktop"})
    public void checkKpiPopSection(ComparisonType comparisonType, DateRange dateFilter, String expectedPeriodTitle) {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .addKpi(new KpiConfiguration.Builder()
                .metric(METRIC_AMOUNT)
                .dataSet(DATE_DATASET_CREATED)
                .comparison(comparisonType.toString())
                .build())
            .saveEditModeWithWidgets();

        Kpi kpi = waitForFragmentVisible(indigoDashboardsPage).getLastWidget(Kpi.class);

        indigoDashboardsPage.selectDateFilterByName(dateFilter.toString());

        takeScreenshot(browser, "checkKpiPopSection-" + comparisonType + "-" + dateFilter + "-" + expectedPeriodTitle, getClass());
        assertEquals(kpi.getPopSection().getChangeTitle(), "change");
        assertEquals(kpi.getPopSection().getPeriodTitle(), expectedPeriodTitle);

        indigoDashboardsPage.switchToEditMode().getLastWidget(Kpi.class).delete();
        indigoDashboardsPage.saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkKpiPopSectionWhenFilteringByFloatingRange() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addKpi(new KpiConfiguration.Builder()
                        .metric(METRIC_AMOUNT)
                        .dataSet(DATE_DATASET_CREATED)
                        .comparison(ComparisonType.LAST_YEAR.toString())
                        .build())
                .selectLastWidget(Kpi.class);
        indigoDashboardsPage.openExtendedDateFilterPanel().selectFloatingRange(ExtendedDateFilterPanel.DateGranularity.YEARS,"9 years ago","2 years ago").apply();
        takeScreenshot(browser, "addFloatingRange", getClass());
        indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading();
        takeScreenshot(browser, "checkSaveKDWithFloatingDate", getClass());
        Kpi lastKpi = indigoDashboardsPage.getLastWidget(Kpi.class);
        assertTrue(lastKpi.hasPopSection(), "Newly added KPI should have pop section");
        assertEquals(indigoDashboardsPage.getDateFilter().getSelection(), "From 9 to 2 years ago");
    }
}
