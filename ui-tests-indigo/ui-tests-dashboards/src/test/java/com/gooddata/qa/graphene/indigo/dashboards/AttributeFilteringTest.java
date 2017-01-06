package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
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

import com.gooddata.md.Attribute;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition.AttributeBucket;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFiltersPanel;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AttributeFilteringTest extends GoodSalesAbstractDashboardTest {

    @Override
    protected void prepareSetupProject() throws ParseException, JSONException, IOException {
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop", "mobile"})
    public void checkDashboardWithNoAttributeFilters() {
        final AttributeFiltersPanel attributeFiltersPanel =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();

        takeScreenshot(browser, "checkDashboardWithNoAttributeFilters", getClass());

        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);
    }

    @Test(dependsOnMethods = {"checkDashboardWithNoAttributeFilters"}, groups = {"desktop"})
    public void checkAttributeFilterCancellingAfterDrag() throws JSONException {
        initIndigoDashboardsPageWithWidgets()
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
        initIndigoDashboardsPageWithWidgets().switchToEditMode().addAttributeFilter(ATTR_ACCOUNT)
                .saveEditModeWithWidgets();
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

    @Test(dependsOnMethods = {"setupAttributeFilters"}, groups = {"desktop"})
    public void allowAddingOneFilterPerAttribute() {
        assertEquals(
                initIndigoDashboardsPageWithWidgets()
                        .switchToEditMode()
                        .getAttributeFiltersPanel()
                        .getAttributeFilters().stream().filter(e -> ATTR_ACCOUNT.equals(e.getTitle())).count(),
                1, "There is more than 1 attribute filter named " + ATTR_ACCOUNT);

        indigoDashboardsPage.addAttributeFilter(ATTR_ACCOUNT);
        takeScreenshot(browser, "Allow-Adding-One-Filter-Per-Attribute", getClass());
        assertEquals(
                indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilters().stream()
                        .filter(e -> ATTR_ACCOUNT.equals(e.getTitle())).count(),
                1, "There is more than 1 attribute filter named " + ATTR_ACCOUNT);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"})
    public void testTooltipOnLongAttributeName() {
        String longNameAttribute = "Attribute-Having-Long-Name" + UUID.randomUUID().toString().substring(0, 10);

        // below code is the only way to create an attribute which has long name for now
        // renaming existing one is not a recommended option.
        initAttributePage().createComputedAttribute(new ComputedAttributeDefinition().withName(longNameAttribute)
                .withAttribute(ATTR_SALES_REP).withMetric(METRIC_NUMBER_OF_WON_OPPS)
                .withBucket(new AttributeBucket(0, "Poor", "120"))
                .withBucket(new AttributeBucket(1, "Good", "200"))
                .withBucket(new AttributeBucket(2, "Great", "250")).withBucket(new AttributeBucket(3, "Best")));

        try {
            assertEquals(
                    initIndigoDashboardsPageWithWidgets()
                            .switchToEditMode()
                            .dragAddAttributeFilterPlaceholder()
                            .getAttributeSelect()
                            .getTooltipOnAttribute(longNameAttribute),
                    longNameAttribute, "The attribute name is not shortened or the tooltip is not correct");
        } finally {
            getMdService().removeObjByUri(
                    getMdService().getObjUri(getProject(), Attribute.class, Restriction.title(longNameAttribute)));
        }
    }

    @Test(dependsOnMethods = {"setupAttributeFilters"}, groups = {"desktop"})
    public void deleteAttributeFilterDiscarded() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .deleteAttributeFilter()
                .cancelEditModeWithChanges();

        final AttributeFiltersPanel attributeFiltersPanel =
                indigoDashboardsPage.getAttributeFiltersPanel();
        takeScreenshot(browser, "deleteAttributeFilterDiscarded-Without_Reload", getClass());
        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 1);

        final AttributeFiltersPanel attributeFiltersPanelWithReload =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();
        takeScreenshot(browser, "deleteAttributeFilterDiscarded-With_Reload", getClass());
        assertEquals(attributeFiltersPanelWithReload.getAttributeFilters().size(), 1);
    }

    @Test(dependsOnMethods = {"deleteAttributeFilterDiscarded"}, groups = {"desktop"})
    public void deleteAttributeFilter() {
        initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .deleteAttributeFilter()
                .saveEditModeWithWidgets();

        final AttributeFiltersPanel attributeFiltersPanel =
                indigoDashboardsPage.getAttributeFiltersPanel();
        takeScreenshot(browser, "deleteAttributeFilter-Without_Reload", getClass());
        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);

        final AttributeFiltersPanel attributeFiltersPanelWithReload =
                initIndigoDashboardsPageWithWidgets().getAttributeFiltersPanel();
        takeScreenshot(browser, "deleteAttributeFilter-With_Reload", getClass());
        assertEquals(attributeFiltersPanel.getAttributeFilters().size(), 0);
    }
}
