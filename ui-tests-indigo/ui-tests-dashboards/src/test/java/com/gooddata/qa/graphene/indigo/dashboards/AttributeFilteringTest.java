package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.join;
import static org.testng.Assert.assertEquals;

public class AttributeFilteringTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkAttributeFilterDefaultState() {
        final AttributeFiltersPanel attributeFiltersPanel = initIndigoDashboardsPage().waitForAttributeFilters();

        takeScreenshot(browser, "checkAttributeFilterDefaultState-All", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 2);
        assertEquals(attributeFiltersPanel.getAttributeFilter("stat_region").getSelection(), "All");
        assertEquals(attributeFiltersPanel.getAttributeFilter("Account").getSelection(), "All");
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop", "mobile"})
    public void checkAttributeFilterChangeValue() {
        String attributeFilterWestCoast = "West Coast";
        String attributeFilterSourceConsulting = "1 Source Consulting";
        String attributeFilterAgileThought = "AgileThought";
        String attributeFilterVideo = "1st in Video - Music World";
        String attributeFilterShoppingCart = "3dCart Shopping Cart Software";

        AttributeFiltersPanel attributeFiltersPanel = initIndigoDashboardsPage().waitForAttributeFilters();

        attributeFiltersPanel.getAttributeFilter("stat_region").selectByName(attributeFilterWestCoast, true);

        attributeFiltersPanel.getAttributeFilter("Account").selectByName(attributeFilterSourceConsulting, true);
        attributeFiltersPanel.getAttributeFilter("Account").selectByName(attributeFilterAgileThought, false);
        attributeFiltersPanel.getAttributeFilter("Account").selectByName(attributeFilterVideo, false);
        attributeFiltersPanel.getAttributeFilter("Account").selectByName(attributeFilterShoppingCart, false);

        takeScreenshot(browser, "checkAttributeFilterDefaultState-West_Coast", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 2);
        assertEquals(attributeFiltersPanel.getAttributeFilter("stat_region").getSelection(), attributeFilterWestCoast);
        assertEquals(attributeFiltersPanel.getAttributeFilter("Account").getSelectedItems(), join(", ", attributeFilterSourceConsulting, attributeFilterVideo, attributeFilterShoppingCart, attributeFilterAgileThought));
        assertEquals(attributeFiltersPanel.getAttributeFilter("Account").getSelectedItemsCount(), "(4)");
    }

}
