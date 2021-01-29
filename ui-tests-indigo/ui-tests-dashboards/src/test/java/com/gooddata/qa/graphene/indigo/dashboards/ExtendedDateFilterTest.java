package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.enums.DateRange.ALL_TIME;
import static com.gooddata.qa.graphene.enums.DateRange.LAST_7_DAYS;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ExtendedDateFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ExtendedDateFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiPopSection;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

public class ExtendedDateFilterTest extends AbstractDashboardTest {

    private ProjectRestRequest projectRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        IndigoRestRequest indigoRestRequest = new IndigoRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));

        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_EXTENDED_DATE_FILTERS, true);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void keepDefaultFilter() {
        ExtendedDateFilterPanel extendedDateFilterPanel = initIndigoDashboardsPage().switchToEditMode().openExtendedDateFilterPanel();

        extendedDateFilterPanel.selectPeriod(LAST_7_DAYS).apply();
        assertEquals(indigoDashboardsPage.saveEditModeWithWidgets().getDateFilterSelection(), LAST_7_DAYS.toString());

        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(ALL_TIME).apply();
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), "All time");

        initIndigoDashboardsPage();
        assertEquals(indigoDashboardsPage.getDateFilterSelection(), LAST_7_DAYS.toString());

        indigoDashboardsPage.switchToEditMode().openExtendedDateFilterPanel().selectStaticPeriod("01/01/2018", "01/01/2019").apply();
        assertEquals(indigoDashboardsPage.saveEditModeWithWidgets().getDateFilterSelection(), "01/01/2018–01/01/2019");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void checkKpiAlertWithExtendedDateFilter() {
        initIndigoDashboardsPage().openExtendedDateFilterPanel().selectPeriod(LAST_7_DAYS).apply();
        Kpi kpi = indigoDashboardsPage.getLastWidget(Kpi.class).hoverAndClickKpiAlertButton();

        takeScreenshot(browser, "check-Kpi-Alert-Dialog", getClass());
        assertTrue(kpi.hasAlertDialogOpen(), "Alert dialog should open");

        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2018", "01/01/2019").apply();
        kpi.hoverAndClickKpiAlertButton();
        assertEquals(getBubbleMessage(browser), "Alerts are not supported for the static date period");

        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(LAST_7_DAYS).checkExcludeCurrent().apply();
        kpi.hoverAndClickKpiAlertButton();
        assertTrue(kpi.hasAlertDialogOpen(), "Alert dialog should open");

        indigoDashboardsPage.openExtendedDateFilterPanel()
                .selectFloatingRange(DateGranularity.MONTHS, "2 months ago", "this month").apply();
        kpi.hoverAndClickKpiAlertButton();
        takeScreenshot(browser, "check-Kpi-Alert-Dialog-check", getClass());
        assertTrue(kpi.hasAlertDialogOpen(), "Alert dialog should open");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop", "mobile"})
    public void recheckKpiAlertWithExtendedDateFilter() {
        initIndigoDashboardsPage().openExtendedDateFilterPanel().selectPeriod(LAST_7_DAYS).apply();
        Kpi kpi = indigoDashboardsPage.getLastWidget(Kpi.class);
        kpi.openAlertDialog().selectTriggeredWhen(TRIGGERED_WHEN_GOES_ABOVE).setThreshold("1").setAlert();

        takeScreenshot(browser, "check-Kpi-Alert-Dialog", getClass());
        try {
            indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2018", "01/01/2019").apply();
            assertTrue(kpi.hasSetAlert(), "Kpi alert should keep active status");

            indigoDashboardsPage.openExtendedDateFilterPanel()
                    .selectFloatingRange(DateGranularity.QUARTERS, "2 quarters ago", "this quarter").apply();
            takeScreenshot(browser, "check-Kpi-Alert-Dialog-check-xxxxxxxxxx", getClass());
            assertTrue(kpi.hasSetAlert(), "Kpi alert should keep active status");

            indigoDashboardsPage.switchToEditMode().openExtendedDateFilterPanel()
                    .selectPeriod(LAST_7_DAYS).checkExcludeCurrent().apply();
            indigoDashboardsPage.saveEditModeWithWidgets();

            //Covering issue RAIL-1477
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_EXTENDED_DATE_FILTERS, false);
            initIndigoDashboardsPage();
            assertTrue(kpi.hasSetAlert(), "Kpi alert should keep active status");
        } finally {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_EXTENDED_DATE_FILTERS, true);
            deleteAlertForLastKpi();
        }
    }

    @DataProvider(name = "dateFilterProvider")
    public Object[][] dateFilterProvider() {
        return new Object[][] {
                {DateRange.LAST_7_DAYS, "prev. 7d"},
                {DateRange.LAST_30_DAYS, "prev. 30d"},
                {DateRange.LAST_90_DAYS, "prev. 90d"},
                {DateRange.LAST_12_MONTHS, "prev. 12m"},
                {DateRange.LAST_4_QUARTERS, "prev. 4q"},
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "dateFilterProvider")
    public void checkCompareWithExtended(DateRange dateRange, String periodTitle) {
        Kpi kpi = initIndigoDashboardsPage().switchToEditMode().selectLastWidget(Kpi.class);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel()
                .selectComparisonByName(ComparisonType.PREVIOUS_PERIOD.toString());
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(dateRange).checkExcludeCurrent().apply();
        kpi.waitForContentLoading();

        assertEquals(kpi.getPopSection().getPeriodTitle(), periodTitle);
        takeScreenshot(browser, "checkCompareWithExtended", getClass());

        indigoDashboardsPage.selectLastWidget(Kpi.class);
        configurationPanel.selectComparisonByName(ComparisonType.LAST_YEAR.toString());
        kpi.waitForContentLoading();
        assertEquals(kpi.getPopSection().getPeriodTitle(), "prev. year");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkCompareWithAllTimeFilter() {
        Kpi kpi = initIndigoDashboardsPage().switchToEditMode().selectLastWidget(Kpi.class);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel()
                .selectComparisonByName(ComparisonType.PREVIOUS_PERIOD.toString());
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        kpi.waitForContentLoading();
        KpiPopSection kpiPopSection = kpi.getPopSection();
        assertEquals(kpiPopSection.getPeriodTitle(), "prev. period");

        indigoDashboardsPage.selectLastWidget(Kpi.class);
        configurationPanel.selectComparisonByName(ComparisonType.LAST_YEAR.toString());
        kpi.waitForContentLoading();
        assertEquals(kpiPopSection.getPeriodTitle(), "prev. year");
        indigoDashboardsPage.saveEditModeWithWidgets();
        assertEquals(kpi.openAlertDialog().getAlertDialogText(), "");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkCompareWithStaticPeriodFilter() {
        Kpi kpi = initIndigoDashboardsPage().switchToEditMode().selectLastWidget(Kpi.class);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel()
                .selectComparisonByName(ComparisonType.PREVIOUS_PERIOD.toString());
        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/2018", "01/01/2019").apply();
        kpi.waitForContentLoading();
        KpiPopSection kpiPopSection = kpi.getPopSection();
        assertEquals(kpiPopSection.getPeriodTitle(), "prev. period");

        indigoDashboardsPage.selectLastWidget(Kpi.class);
        configurationPanel.selectComparisonByName(ComparisonType.LAST_YEAR.toString());
        kpi.waitForContentLoading();
        assertEquals(kpiPopSection.getPeriodTitle(), "prev. year");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkCompareWithFloatingRangeFilter() {
        Kpi kpi = initIndigoDashboardsPage().switchToEditMode().selectLastWidget(Kpi.class);
        ConfigurationPanel configurationPanel = indigoDashboardsPage.getConfigurationPanel()
                .selectComparisonByName(ComparisonType.PREVIOUS_PERIOD.toString());
        indigoDashboardsPage.openExtendedDateFilterPanel().selectFloatingRange(DateGranularity.DAYS, "yesterday", "4 days ahead").apply();
        kpi.waitForContentLoading();
        KpiPopSection kpiPopSection = kpi.getPopSection();
        assertEquals(kpiPopSection.getPeriodTitle(), "prev. 6d");

        indigoDashboardsPage.selectLastWidget(Kpi.class);
        configurationPanel.selectComparisonByName(ComparisonType.LAST_YEAR.toString());
        kpi.waitForContentLoading();
        assertEquals(kpiPopSection.getPeriodTitle(), "prev. year");
    }

    @Test(dependsOnGroups = "createProject")
    public void switchDataPresets() {
        initIndigoDashboardsPage().switchToEditMode().openExtendedDateFilterPanel()
                .selectFloatingRange(DateGranularity.MONTHS, "this month", "this month").apply();
        assertEquals(indigoDashboardsPage.saveEditModeWithWidgets()
                .openExtendedDateFilterPanel().getSelectedDateFilter(), DateRange.FLOATING_RANGE);
    }

    @Test(dependsOnGroups = "createProject")
    public void checkDatePresets() {
        List<String> currentDatePresets = initIndigoDashboardsPage()
                .addDashboard().openExtendedDateFilterPanel().getDateRangeOptions();
        List<String> expectedDatePresets = Stream.of(DateRange.values()).map(dateRange -> dateRange.toString())
                .collect(Collectors.toList());
        assertEquals(currentDatePresets, expectedDatePresets);

        initIndigoDashboardsPage().addDashboard().openExtendedDateFilterPanel()
                .selectStaticPeriod("01/01/2018", "01/01/2019").apply();
        currentDatePresets = indigoDashboardsPage.openExtendedDateFilterPanel().getDateRangeOptions();
        assertEquals(currentDatePresets, expectedDatePresets);

        initIndigoDashboardsPage().addDashboard().openExtendedDateFilterPanel()
                .selectFloatingRange(DateGranularity.MONTHS, "this month", "this month").apply();
        currentDatePresets = indigoDashboardsPage.openExtendedDateFilterPanel().getDateRangeOptions();
        assertEquals(currentDatePresets, expectedDatePresets);

        initIndigoDashboardsPage().addDashboard().openExtendedDateFilterPanel().selectPeriod(ALL_TIME).apply();
        currentDatePresets = indigoDashboardsPage.openExtendedDateFilterPanel().getDateRangeOptions();
        assertEquals(currentDatePresets, expectedDatePresets);

        initIndigoDashboardsPage().addDashboard().openExtendedDateFilterPanel().selectPeriod(LAST_7_DAYS)
                .checkExcludeCurrent().apply();
        currentDatePresets = indigoDashboardsPage.openExtendedDateFilterPanel().getDateRangeOptions();
        assertEquals(currentDatePresets, expectedDatePresets);
    }
}
