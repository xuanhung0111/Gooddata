package com.gooddata.qa.graphene.indigo.dashboards.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.List;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;

public abstract class DashboardsTest extends DashboardsGeneralTest {

    public static final String DASH_WIDGET_PREV_DROPZONE_CLASS = "prev";
    public static final String DASH_WIDGET_NEXT_DROPZONE_CLASS = "next";

    public static final String DATE_CREATED = "Created";
    public static final String DATE_CLOSED = "Closed";
    public static final String DATE_ACTIVITY = "Activity";
    public static final String DATE_SNAPSHOT = "Snapshot";

    public static final String DATE_FILTER_ALL_TIME = "All time";
    public static final String DATE_FILTER_THIS_MONTH = "This month";
    public static final String DATE_FILTER_LAST_MONTH = "Last month";
    public static final String DATE_FILTER_THIS_QUARTER = "This quarter";
    public static final String DATE_FILTER_LAST_QUARTER = "Last quarter";
    public static final String DATE_FILTER_THIS_YEAR = "This year";
    public static final String DATE_FILTER_LAST_YEAR = "Last year";

    protected void setupKpisFromSplashScreen(List<KpiConfiguration> configs) {
        if (configs.isEmpty()) {
            throw new IllegalArgumentException("Cannot setup dashboard with no KPIs.");
        }

        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets();

        waitForFragmentVisible(indigoDashboardsPage).waitForDashboardLoad();
        configs.forEach(config -> indigoDashboardsPage.addWidget(config));
        indigoDashboardsPage.saveEditModeWithKpis();
    }

    protected void teardownKpiWithDashboardDelete() {
        if (initIndigoDashboardsPageWithWidgets().isEditButtonVisible()) {
            waitForFragmentVisible(indigoDashboardsPage).switchToEditMode();
        }

        waitForFragmentVisible(indigoDashboardsPage)
            .clickLastKpiDeleteButton()
            .waitForDialog()
            .submitClick();

        indigoDashboardsPage
                .saveEditModeWithoutKpis()
                .getSplashScreen();
    }
}
