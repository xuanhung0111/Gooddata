package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardWithWidgetsTest;

public class MetricsDropdownTest extends DashboardWithWidgetsTest {

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkSearchStringResetAfterDropdownToggle() {
        MetricSelect ms = getMetricSelect();

        ms.searchForText("amount");
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

        ms.searchForText("amount");
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownClose-searchField_amount", this.getClass());

        assertEquals(3, ms.getValues().size());

        waitForFragmentVisible(indigoDashboardsPage).clickAddWidget(); // clicking away shall close MetricsDropdown
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
                .selectMetricByName(METRIC_AMOUNT)
                .getMetricSelect();

        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterItemSelect-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnMethods = {"initDashboardWithWidgets"}, groups = {"desktop"})
    public void checkNoMatchingMessage() {
        MetricSelect ms = getMetricSelect();

        ms.searchForText("name-of-nonexisting-metric-&!~$#");
        takeScreenshot(browser, "checkNoMatchingMessage", this.getClass());

        assertTrue(ms.getValues().isEmpty());
        assertTrue(ms.isShowingNoMatchingDataMessage());
    }

    private MetricSelect getMetricSelect() {
        return initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .clickAddWidget()
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
