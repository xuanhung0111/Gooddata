package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class DataSetTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkAvailableDataSets() {
        ConfigurationPanel cp = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .dragAddKpiPlaceholder()
            .getConfigurationPanel();

        cp.selectMetricByName(METRIC_LOST);

        List<String> availableDataSetsForLost = asList(DATE_CLOSED, DATE_CREATED, DATE_SNAPSHOT);
        assertTrue(isEqualCollection(cp.getDataSets(), availableDataSetsForLost));

        takeScreenshot(browser, "checkAvailableDataSets-lostMetric-dateClosed-dateCreated-dateSnapshot", getClass());

        cp.selectMetricByName(METRIC_NUMBER_OF_ACTIVITIES);

        List<String> availableDimsForNumberOfActivities = asList(DATE_ACTIVITY, DATE_CREATED);
        assertTrue(isEqualCollection(cp.getDataSets(), availableDimsForNumberOfActivities));

        takeScreenshot(browser, "checkAvailableDataSets-numOfActivitiesMetric-dateActivity-dateCreated", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDateDataSetConfigured() {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectKpi(0);

        waitForFragmentVisible(indigoDashboardsPage)
            .getConfigurationPanel()
            .selectDataSetByName(DATE_CLOSED);

        indigoDashboardsPage
            .leaveEditMode()
            .switchToEditMode()
            .selectKpi(0);

        String selectedDateDataSet = indigoDashboardsPage
            .getConfigurationPanel()
            .getSelectedDataSet();

        assertEquals(selectedDateDataSet, DATE_CLOSED);

        takeScreenshot(browser, "checkDateDataSetConfigured-dateClosed", getClass());
    }
}
