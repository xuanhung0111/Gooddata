package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class MetricsDropdownTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkSearchStringResetAfterDropdownToggle() {
        MetricSelect ms = getMetricSelect();

        ms.searchByName("amount");
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownToggle-searchField_amount", this.getClass());

        assertEquals(3, ms.getValues().size());

        ms.ensureDropdownClosed();
        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownToggle-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkSearchStringResetAfterDropdownClose() {
        MetricSelect ms = getMetricSelect();

        ms.searchByName("amount");
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownClose-searchField_amount", this.getClass());

        assertEquals(3, ms.getValues().size());

        indigoDashboardsPage.clickAddWidget(); // clicking away shall close MetricsDropdown
        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownClose-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkSearchStringResetAfterItemSelect() {
        IndigoDashboardsPage page = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .clickAddWidget();

        MetricSelect ms = page.getConfigurationPanel()
                .selectMetricByName(AMOUNT)
                .getMetricSelect();

        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterItemSelect-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNoMatchingMessage() {
        MetricSelect ms = getMetricSelect();

        ms.searchByName("name-of-nonexisting-metric-&!~$#");
        takeScreenshot(browser, "checkNoMatchingMessage", this.getClass());

        assertTrue(ms.getValues().isEmpty());
        assertTrue(ms.isShowingNoMatchingDataMessage());
    }

    private MetricSelect getMetricSelect() {
        IndigoDashboardsPage page = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .clickAddWidget();

        return page
                .getConfigurationPanel()
                .getMetricSelect();
    }

    private void checkDropdownDidReset() {
        MetricSelect ms = indigoDashboardsPage
                .getConfigurationPanel()
                .getMetricSelect();

        assertTrue(ms.getSearchText().isEmpty());
        assertTrue(ms.getValues().size() > 10);
    }
}
