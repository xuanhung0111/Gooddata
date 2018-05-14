package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFilter;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class DeleteAttributeFilterTest extends AbstractDashboardTest {

    private static final String TEST_INSIGHT = "Test-Insight";
    private static final String ALL_VALUE = "All";
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Delete-Attribute-Filter-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        String insightWidget = createInsightWidget(new InsightMDConfiguration(TEST_INSIGHT, ReportType.COLUMN_CHART)
                .setMeasureBucket(singletonList(MeasureBucket.createSimpleMeasureBucket(getMdService().getObj(getProject(),
                        Metric.class, title(METRIC_NUMBER_OF_ACTIVITIES))))));
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest.createAnalyticalDashboard(asList(createAmountKpi(), insightWidget));

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(ATTR_ACTIVITY, ALL_VALUE);
        hashMap.put(ATTR_DEPARTMENT, "Direct Sales");
        hashMap.put(ATTR_PRIORITY, ALL_VALUE);

        addAttributeFilters(hashMap);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void displayTrashWhenPerformingFilterDrag() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();

        Actions actions = new Actions(browser);
        try {
            actions.clickAndHold(indigoDashboardsPage
                    .getAttributeFiltersPanel().getAttributeFilter(ATTR_ACTIVITY).getRoot()).perform();
            Dimension bodySize = indigoDashboardsPage.getDashboardBodySize();
            actions.moveByOffset(bodySize.getWidth() / 2, bodySize.getHeight() / 2).perform();
            takeScreenshot(browser, "displayTrashWhenPerformingFilterDrag", getClass());
            assertTrue(indigoDashboardsPage.hasAttributeFilterTrash(), "area for deleting filter is not displayed");
        } finally {
            actions.release().perform();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteFilterWhenHavingMultipleAttributeFilters() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .deleteAttributeFilter(ATTR_DEPARTMENT).waitForWidgetsLoading();

        indigoDashboardsPage.selectFirstWidget(Kpi.class);
        assertFalse(indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilters().stream()
                .anyMatch(e -> ATTR_DEPARTMENT.equals(e.getTitle())), "deleted filter is still applied on kpi");

        indigoDashboardsPage.selectLastWidget(Insight.class);
        assertFalse(indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilters().stream()
                .anyMatch(e -> ATTR_DEPARTMENT.equals(e.getTitle())), "deleted filter is still applied on insight");

        assertEquals(indigoDashboardsPage.saveEditModeWithWidgets().waitForWidgetsLoading().getFirstWidget(Kpi.class)
                .getValue(), "$116,625,456.54", "Kpi value is not changed after removing att filter");
        assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getChartReport().getDataLabels(),
                singletonList("154,271"), "Insight value is not changed after removing att filter");
    }

    @Test(dependsOnMethods = {"deleteFilterWhenHavingMultipleAttributeFilters"})
    public void addDeletedAttributeFilter() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_DEPARTMENT)
                .getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT).apply();
        indigoDashboardsPage.waitForWidgetsLoading();
        takeScreenshot(browser, "addDeletedAttributeFilter", getClass());
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_DEPARTMENT).getSelection(),
                ALL_VALUE, "deleted attribute filter has not been added to dashboard");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyFilterDeletingToOtherUserRoles() throws JSONException {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(ATTR_REGION, ALL_VALUE);
        addAttributeFilters(hashMap);

        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .deleteAttributeFilter(ATTR_REGION).saveEditModeWithWidgets();
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            assertFalse(initIndigoDashboardsPageWithWidgets()
                    .getAttributeFiltersPanel().isFilterVisible(ATTR_REGION), "Attribute filter is not deleted");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void discardFilterDeletingAppliedToOtherUserRole() throws JSONException {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(ATTR_STAGE_NAME, ALL_VALUE);
        addAttributeFilters(hashMap);

        initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .deleteAttributeFilter(ATTR_STAGE_NAME).cancelEditModeWithChanges();
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            assertTrue(initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel()
                    .isFilterVisible(ATTR_STAGE_NAME), "Attribute filter is not reverted back");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "ONE-2028: Deleted filter does not reappear after discarding changes")
    public void discardChangesRelatingToDeleteFilter() throws IOException, JSONException {
        String filterValue = "East Coast";
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(ATTR_REGION, filterValue);
        addAttributeFilters(hashMap);

        try {
            indigoDashboardsPage.switchToEditMode().deleteAttributeFilter(ATTR_REGION);
            String kpiValue = indigoDashboardsPage.getFirstWidget(Kpi.class).getValue();

            indigoDashboardsPage.cancelEditModeWithChanges().waitForWidgetsLoading();
            takeScreenshot(browser, "discardChangesRelatingToDeleteFilter", getClass());

            assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_REGION)
                    .getSelection(), filterValue, "Cancelling change is not applied");
            assertFalse(indigoDashboardsPage.getFirstWidget(Kpi.class).getValue().equals(kpiValue),
                    "Kpi value is not re-executed");
        } finally {
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(ATTR_REGION));
        }
    }

    private void addAttributeFilters(HashMap<String, String> hashMap) {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();

        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            AttributeFilter filter = indigoDashboardsPage.addAttributeFilter(entry.getKey())
                    .getAttributeFiltersPanel()
                    .getAttributeFilter(entry.getKey());

            if (entry.getValue().equals(ALL_VALUE))
                filter.selectAllValues();
            else
                filter.clearAllCheckedValues().selectByNames(entry.getValue());
        }

        indigoDashboardsPage.saveEditModeWithWidgets();
    }
}
