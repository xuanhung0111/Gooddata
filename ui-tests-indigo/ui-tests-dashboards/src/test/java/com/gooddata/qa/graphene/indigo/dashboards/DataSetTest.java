package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;

public class DataSetTest extends GoodSalesAbstractDashboardTest {

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
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

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void checkDateDataSetConfigured() {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        waitForFragmentVisible(indigoDashboardsPage)
            .getConfigurationPanel()
            .selectDateDataSetByName(DATE_CLOSED);

        indigoDashboardsPage
            .leaveEditMode()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        String selectedDateDataSet = indigoDashboardsPage
            .getConfigurationPanel()
            .getSelectedDataSet();

        assertEquals(selectedDateDataSet, DATE_CLOSED);

        takeScreenshot(browser, "checkDateDataSetConfigured-dateClosed", getClass());
    }
}
