package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.setFeatureFlagInProject;
import static java.lang.String.join;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttributeFilteringTest extends GoodSalesAbstractDashboardTest {

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "mobile"})
    public void setupAttributeFiltersFeatureFlag() throws JSONException {
        setFeatureFlagInProject(getGoodDataClient(), testParams.getProjectId(),
                ProjectFeatureFlags.ENABLE_ATTRIBUTE_FILTERS, true);
    }

    @Test(dependsOnMethods = {"setupAttributeFiltersFeatureFlag"}, groups = {"desktop", "mobile"})
    public void checkDashboardWithNoAttributeFilters() {
        final AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();

        takeScreenshot(browser, "checkDashboardWithNoAttributeFilters", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);
    }

    @Test(dependsOnMethods = {"checkDashboardWithNoAttributeFilters"}, groups = {"desktop"})
    public void checkAttributeFilterCancellingAfterDrag() throws JSONException {
        initIndigoDashboardsPage()
                .switchToEditMode()
                .dragAddAttributeFilterPlaceholder()
                .clickDashboardBody();
        
        final AttributeFiltersPanel attributeFiltersPanel =
                indigoDashboardsPage.getAttributeFiltersPanel();
        
        takeScreenshot(browser, "checkAttributeFilterCancellingAfterDrag", getClass());
        
        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);
    }

    @Test(dependsOnMethods = {"checkAttributeFilterCancellingAfterDrag"}, groups = {"desktop"})
    public void setupAttributeFilters() throws JSONException {
        initIndigoDashboardsPage().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT).saveEditModeWithWidgets();
    }

    @Test(dependsOnMethods = {"setupAttributeFilters"}, groups = {"desktop"})
    public void checkAttributeFilterDefaultState() {
        final AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();

        takeScreenshot(browser, "checkAttributeFilterDefaultState-All", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 1);
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelection(), "All");
    }

    @Test(dependsOnMethods = {"checkAttributeFilterDefaultState"}, groups = {"desktop"})
    public void checkAttributeFilterChangeValue() {
        String attributeFilterSourceConsulting = "14 West";
        String attributeFilterAgileThought = "1-800 Postcards";
        String attributeFilterVideo = "1-800 We Answer";
        String attributeFilterShoppingCart = "1-888-OhioComp";

        AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();

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

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 1);
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItems(), join(", ", attributeElements));
        assertEquals(attributeFiltersPanel.getAttributeFilter(ATTR_ACCOUNT).getSelectedItemsCount(), "(4)");
    }
}
