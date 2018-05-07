package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.join;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AttributeFilterManipulationTest extends AbstractDashboardTest {
    private IndigoRestRequest indigoRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Filter-Manipulation-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        getMetricCreator().createAmountMetric();
        indigoRestRequest.createAnalyticalDashboard(singletonList(createAmountKpi()));
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT)
                .saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, description =
            "ONE-1990: KPI/Insight apply with all filter value except selected value when selecting by only option")
    public void testFilterUsingOneValue() {
        initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames("1000Bulbs.com");

        indigoDashboardsPage.waitForWidgetsLoading();
        takeScreenshot(browser, "testFilterUsingOneValue", getClass());
        assertEquals(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$18,000.00", "The kpi value is not correct");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void allowAddingOneFilterPerAttribute() {
        assertTrue(initIndigoDashboardsPageWithWidgets().switchToEditMode().getAttributeFiltersPanel()
                .isFilterVisible(ATTR_ACCOUNT), "Attribute filter named " + ATTR_ACCOUNT + "does not exist");

        indigoDashboardsPage.addAttributeFilter(ATTR_ACCOUNT);

        indigoDashboardsPage.waitForWidgetsLoading();
        takeScreenshot(browser, "Allow-Adding-One-Filter-Per-Attribute", getClass());

        assertEquals(
                indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilters().stream()
                        .filter(e -> ATTR_ACCOUNT.equals(e.getTitle())).count(),
                1, "There is more than 1 attribute filter named " + ATTR_ACCOUNT);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkAttributeFilterCancellingAfterDrag() throws JSONException {
        int expectedFiltersSize = initIndigoDashboardsPageWithWidgets().switchToEditMode()
                .getAttributeFiltersPanel()
                .getAttributeFilters().size();

        indigoDashboardsPage.dragAddAttributeFilterPlaceholder().clickDashboardBody();

        takeScreenshot(browser, "checkAttributeFilterCancellingAfterDrag", getClass());
        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilters().size(),
                expectedFiltersSize);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"},
            description = "ONE-1993: Value of widget doesn't refresh when refreshing attribute filter" +
                    "ONE-1991: Attribute filter doesn't refresh to All value on edit mode/view mode")
    public void updateValuesOnEditModeAfterMakingChangeOnViewMode() throws IOException, JSONException {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_STAGE_NAME);
        AttributeFiltersPanel panel = indigoDashboardsPage.getAttributeFiltersPanel();

        panel.getAttributeFilter(ATTR_STAGE_NAME)
                .clearAllCheckedValues()
                .selectByNames("Short List", "Risk Assessment");

        assertEquals(
                indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$8,218,356.06", "The kpi value is not correct after adding filters");

        indigoDashboardsPage.saveEditModeWithWidgets();
        try {
            panel.getAttributeFilter(ATTR_STAGE_NAME).selectAllValues();
            indigoDashboardsPage.waitForWidgetsLoading();

            assertEquals(panel.getAttributeFilter(ATTR_STAGE_NAME).getSelectedItems(),
                    "All", "The filter is not updated");
            assertEquals(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                    "$116,625,456.54", "The kpi value is not correct after changing value");

            indigoDashboardsPage.switchToEditMode().waitForWidgetsLoading();
            takeScreenshot(browser, "updateValuesOnEditModeAfterMakingChangeOnViewMode", getClass());
            assertEquals(panel.getAttributeFilter(ATTR_STAGE_NAME).getSelectedItems(),
                    "Risk Assessment, Short List", "The filter value is not correct after switching to edit mode");
            assertEquals(
                    indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                    "$8,218,356.06", "The kpi value is not correct after switching to edit mode");
        } finally {
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(ATTR_STAGE_NAME));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"},
            description = "ONE-1996: Widgets won't re-execute when discarding edited attribute filters changes")
    public void discardChangesWhenEditingFilter() throws IOException, JSONException {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_ACCOUNT).clearAllCheckedValues().selectByNames("Zther Interactive");
        assertEquals(indigoDashboardsPage.waitForWidgetsLoading().getWidgetByHeadline(Kpi.class, METRIC_AMOUNT)
                .getValue(), "$1,911.52", "The kpi value is not filtered");

        indigoDashboardsPage.cancelEditModeWithChanges().waitForWidgetsLoading();
        takeScreenshot(browser, "discardChangesWhenEditingFilter", getClass());
        assertEquals(indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AMOUNT).getValue(),
                "$116,625,456.54", "The kpi value is not correct when user cancels changes");
    }

    @DataProvider
    public Object[][] enabledEditMode() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"}, dataProvider = "enabledEditMode")
    public void checkAttributeFilterChangeValue(boolean enabledEditMode) {
        String attributeFilterSourceConsulting = "14 West";
        String attributeFilterAgileThought = "1-800 Postcards";
        String attributeFilterVideo = "1-800 We Answer";
        String attributeFilterShoppingCart = "1-888-OhioComp";

        initIndigoDashboardsPageWithWidgets();
        if (enabledEditMode) indigoDashboardsPage.switchToEditMode();

        AttributeFiltersPanel attributeFiltersPanel = indigoDashboardsPage.getAttributeFiltersPanel();

        attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT)
                .clearAllCheckedValues()
                .selectByNames(
                        attributeFilterSourceConsulting,
                        attributeFilterAgileThought,
                        attributeFilterVideo,
                        attributeFilterShoppingCart
                );

        takeScreenshot(browser, "checkAttributeFilterDefaultState-West_Coast", getClass());

        List<String> attributeElements = new ArrayList<>();

        attributeElements.add(attributeFilterSourceConsulting);
        attributeElements.add(attributeFilterVideo);
        attributeElements.add(attributeFilterShoppingCart);
        attributeElements.add(attributeFilterAgileThought);

        Collections.sort(attributeElements);

        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), join(", ",
                attributeElements));
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItemsCount(), "(4)");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void deleteAttributeFilterDiscarded() throws IOException, JSONException {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_PRODUCT)
                .saveEditModeWithWidgets();
        try {
            indigoDashboardsPage.switchToEditMode()
                    .deleteAttributeFilter(ATTR_PRODUCT).cancelEditModeWithChanges().waitForWidgetsLoading();
            takeScreenshot(browser, "deleteAttributeFilterDiscarded-Without_Reload", getClass());
            assertTrue(indigoDashboardsPage.getAttributeFiltersPanel().isFilterVisible(ATTR_PRODUCT),
                    "The attribute filter named " + ATTR_PRODUCT + "is removed when user cancels changes");


            initIndigoDashboardsPage().waitForWidgetsLoading();
            takeScreenshot(browser, "deleteAttributeFilterDiscarded-With_Reload", getClass());
            assertTrue(indigoDashboardsPage.getAttributeFiltersPanel().isFilterVisible(ATTR_PRODUCT),
                    "The attribute filter named " + ATTR_PRODUCT + "is removed after user reloads page");
        } finally {
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(ATTR_PRODUCT));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void deleteAttributeFilter() throws IOException, JSONException {
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_PRIORITY)
                .saveEditModeWithWidgets();
        try {
            indigoDashboardsPage.switchToEditMode()
                    .deleteAttributeFilter(ATTR_PRIORITY).saveEditModeWithWidgets();

            takeScreenshot(browser, "deleteAttributeFilter-Without_Reload", getClass());
            assertFalse(indigoDashboardsPage.getAttributeFiltersPanel().isFilterVisible(ATTR_PRIORITY),
                    "The attribute filter" + " named " + ATTR_PRIORITY + " is not removed");

            initIndigoDashboardsPage().waitForWidgetsLoading();
            takeScreenshot(browser, "deleteAttributeFilter-With_Reload", getClass());
            assertFalse(
                    initIndigoDashboardsPageWithWidgets()
                            .getAttributeFiltersPanel().isFilterVisible(ATTR_PRIORITY),
                    "The attribute filter" + " named " + ATTR_PRIORITY + " is displayed after user reloads page");
        } finally {
            indigoRestRequest.deleteAttributeFilterIfExist(getAttributeDisplayFormUri(ATTR_PRIORITY));
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void cancelAttributeFilterDrag() {
        initIndigoDashboardsPageWithWidgets().switchToEditMode();
        Dimension bodySize = indigoDashboardsPage.getDashboardBodySize();
        new Actions(browser).dragAndDropBy(indigoDashboardsPage.getAttributeFiltersPanel()
                .getAttributeFilter(ATTR_ACCOUNT).getRoot(), bodySize.getWidth() / 2, bodySize.getHeight() / 2)
                .perform();

        indigoDashboardsPage.selectFirstWidget(Kpi.class);
        assertTrue(indigoDashboardsPage.getAttributeFiltersPanel().isFilterVisible(ATTR_ACCOUNT));
        assertTrue(indigoDashboardsPage.getConfigurationPanel().getFilterByAttributeFilters().stream()
                .anyMatch(e -> e.getTitle().equals(ATTR_ACCOUNT)), "Selected kpi is not applied attribute filter");

    }
}
