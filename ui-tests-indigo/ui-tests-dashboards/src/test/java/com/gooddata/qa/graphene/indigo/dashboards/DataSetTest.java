package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_OPP_FIRST_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class DataSetTest extends AbstractDashboardTest {

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createLostMetric();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createOppFirstSnapshotMetric();
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkAvailableDataSets() {
        ConfigurationPanel cp = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .dragAddKpiPlaceholder()
            .getConfigurationPanel();

        cp.selectMetricByName(METRIC_LOST);

        List<String> availableDataSetsForLost = asList(DATE_DATASET_CLOSED, DATE_DATASET_CREATED, DATE_DATASET_SNAPSHOT);

        assertTrue(isEqualCollection(cp.getDataSets(), availableDataSetsForLost));

        takeScreenshot(browser, "checkAvailableDataSets-lostMetric-dateClosed-dateCreated-dateSnapshot", getClass());

        cp.selectMetricByName(METRIC_NUMBER_OF_ACTIVITIES);

        List<String> availableDimsForNumberOfActivities = asList(DATE_DATASET_ACTIVITY, DATE_DATASET_CREATED);
        assertTrue(isEqualCollection(cp.getDataSets(), availableDimsForNumberOfActivities));

        takeScreenshot(browser, "checkAvailableDataSets-numOfActivitiesMetric-dateActivity-dateCreated", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkDateDataSetConfigured() {
        initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        waitForFragmentVisible(indigoDashboardsPage)
            .getConfigurationPanel()
            .selectDateDataSetByName(DATE_DATASET_CLOSED);

        indigoDashboardsPage
            .leaveEditMode()
            .switchToEditMode()
            .selectFirstWidget(Kpi.class);

        String selectedDateDataSet = indigoDashboardsPage
            .getConfigurationPanel()
            .getSelectedDataSet();

        assertEquals(selectedDateDataSet, DATE_DATASET_CLOSED);

        takeScreenshot(browser, "checkDateDataSetConfigured-dateClosed", getClass());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"},
            description = "CL-10355: No data available when adding/deleting many kpi")
    public void renderDateDatasetAfterAddingMultipleKPIs() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addKpi(
                new KpiConfiguration.Builder().metric(METRIC_OPP_FIRST_SNAPSHOT).dataSet(DATE_DATASET_SNAPSHOT).build())
                .waitForWidgetsLoading();

        indigoDashboardsPage.addKpi(
                new KpiConfiguration.Builder().metric(METRIC_NUMBER_OF_ACTIVITIES).dataSet(DATE_DATASET_CREATED).build())
                .waitForWidgetsLoading().saveEditModeWithWidgets();

        indigoDashboardsPage.switchToEditMode().selectWidgetByHeadline(Kpi.class, METRIC_NUMBER_OF_ACTIVITIES);
        DateDimensionSelect dropDown = indigoDashboardsPage.getConfigurationPanel().openDateDataSet();
        takeScreenshot(browser, "Dataset-After-Adding-Multiple-KPIs", getClass());
        assertTrue(isEqualCollection(dropDown.getValues(), asList(DATE_DATASET_CREATED, DATE_DATASET_ACTIVITY)),
                "Date dataset renders well");
    }
}
