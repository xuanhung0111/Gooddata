package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ConfigurationPanel;
import static java.util.Arrays.asList;
import java.util.List;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import org.testng.annotations.Test;

public class DateDimensionTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkAvailableDateDimensions() {
        ConfigurationPanel cp = initIndigoDashboardsPageWithWidgets()
            .switchToEditMode()
            .clickAddWidget()
            .getConfigurationPanel();

        cp.selectMetricByName(LOST);

        List<String> availableDimsForLost = asList(DATE_CREATED, DATE_CLOSED, DATE_SNAPSHOT);
        assertTrue(isEqualCollection(cp.getDateDimensions(), availableDimsForLost));

        takeScreenshot(browser, "checkAvailableDateDimensions-lostMetric-dateCreated-dateClosed-dateSnapshot", getClass());

        cp.selectMetricByName(NUMBER_OF_ACTIVITIES);

        List<String> availableDimsForNumberOfActivities = asList(DATE_CREATED, DATE_ACTIVITY);
        assertTrue(isEqualCollection(cp.getDateDimensions(), availableDimsForNumberOfActivities));

        takeScreenshot(browser, "checkAvailableDateDimensions-numOfActivitiesMetric-dateCreated-dateActivity", getClass());
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkDateDimensionConfigured() {
        initIndigoDashboardsPageWithWidgets()
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

        takeScreenshot(browser, "checkDateDimensionConfigured-dateClosed", getClass());
    }
}
