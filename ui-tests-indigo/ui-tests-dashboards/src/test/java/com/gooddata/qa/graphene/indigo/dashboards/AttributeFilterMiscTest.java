package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.AttributeFilter;
import com.gooddata.qa.graphene.indigo.dashboards.common.GoodSalesAbstractDashboardTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AttributeFilterMiscTest extends GoodSalesAbstractDashboardTest {

    @BeforeClass(alwaysRun = true)
    public void setTitle() {
        projectTitle += "Attribute-Filter-Misc-Test";
    }

    @Test(dependsOnGroups = "dashboardsInit", groups = {"desktop"})
    public void addFilterToEmptyDashboard() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addAttributeFilter(ATTR_ACCOUNT)
                .getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT).selectAllValues();

        assertEquals(indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter(ATTR_ACCOUNT)
                        .getSelectedItems(),
                "All", "Can't add attribute filter to empty dashboard ");
    }

    @DataProvider
    public Object[][] searchValues() {
        return new Object[][]{
                {"Non-Existence-Attribute"},
                {"<button>abc</button>"}
        };
    }

    @Test(dependsOnGroups = "dashboardsInit", groups = {"desktop"}, dataProvider = "searchValues")
    public void searchNonExistenceAttribute(String searchValue) {
        assertFalse(
                initIndigoDashboardsPage().getSplashScreen().startEditingWidgets()
                        .dragAddAttributeFilterPlaceholder()
                        .getAttributeSelect().hasAttribute(searchValue),
                "Found attribute named " + searchValue);
    }

    @Test(dependsOnGroups = "dashboardsInit", groups = {"desktop"})
    public void searchOnListOfAttributes() {
        assertTrue(
                initIndigoDashboardsPage().getSplashScreen().startEditingWidgets()
                        .dragAddAttributeFilterPlaceholder()
                        .getAttributeSelect().hasAttribute(ATTR_STAGE_NAME),
                "Can't not find attribute named " + ATTR_STAGE_NAME);
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"desktop"},
            description = "ONE-2019: Arrow icon of attribute filter sometimes show wrong when open/close " +
                    "drop-down")
    public void expandAndCollapseFilter() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addAttributeFilter(ATTR_OPPORTUNITY);

        AttributeFilter filter = indigoDashboardsPage.getAttributeFiltersPanel().getAttributeFilter
                (ATTR_OPPORTUNITY);

        // these checks are based on the fix applied for this bug
        // see https://github.com/gooddata/gdc-dashboards/pull/651
        filter.ensureDropdownOpen();
        assertTrue(filter.isActive(), "The attribute filter is not active");

        filter.ensureDropdownClosed();
        assertFalse(filter.isActive(), "The attribute filter is active");
    }
}
