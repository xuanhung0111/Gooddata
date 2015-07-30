package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import static java.util.Arrays.asList;
import java.util.List;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class DateDimensionTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkAvailableDateDimensions() {
        ConfigurationPanel cp = initIndigoDashboardsPage()
            .switchToEditMode()
            .clickAddWidget()
            .getConfigurationPanel();

        cp.selectMetricByName(LOST);

        List<String> availableDimsForLost = asList(DATE_CREATED, DATE_CLOSED, DATE_SNAPSHOT);
        assertTrue(isEqualCollection(cp.getDateDimensions(), availableDimsForLost));

        cp.selectMetricByName(NUMBER_OF_ACTIVITIES);

        List<String> availableDimsForNumberOfActivities = asList(DATE_CREATED, DATE_ACTIVITY);
        assertTrue(isEqualCollection(cp.getDateDimensions(), availableDimsForNumberOfActivities));
    }

    @Test(dependsOnMethods = {"initIndigoDashboardWithWidgets"}, groups = {"adminTests"})
    public void checkDateDimensionConfigured() {
        initIndigoDashboardsPage()
            .switchToEditMode()
            .selectKpi(0);

        indigoDashboardsPage
            .getConfigurationPanel()
            .selectDateDimensionByName(DATE_CLOSED);

        indigoDashboardsPage
            .leaveEditMode()
            .switchToEditMode()
            .selectKpi(0);

        String selectedDateDimension = indigoDashboardsPage
            .getConfigurationPanel()
            .getSelectedDateDimension();

        assertEquals(selectedDateDimension, DATE_CLOSED);
    }
}
