package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class MetricsDropdownTest extends AbstractDashboardTest {

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        //add more metrics to display search metric
        createAmountMetric();
        createAmountBOPMetric();
        createAvgAmountMetric();
        createNumberOfActivitiesMetric();
        createNumberOfLostOppsMetric();
        createNumberOfOpportunitiesMetric();
        createNumberOfOpportunitiesBOPMetric();
        createTimelineBOPMetric();
        createTimelineEOPMetric();
        createPercentOfGoalMetric();
        createProbabilityMetric();
        createQuotaMetric();
        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(createAmountKpi()));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
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

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkSearchStringResetAfterDropdownClose() {
        MetricSelect ms = getMetricSelect();

        ms.searchForText("amount");
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownClose-searchField_amount", this.getClass());

        assertEquals(3, ms.getValues().size());

        waitForFragmentVisible(indigoDashboardsPage).dragAddKpiPlaceholder(); // dragging addKpiPlaceholder shall close MetricsDropdown
        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownClose-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkSearchStringResetAfterItemSelect() {
        IndigoDashboardsPage page = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .dragAddKpiPlaceholder();

        MetricSelect ms = page.getConfigurationPanel()
                .selectMetricByName(METRIC_AMOUNT)
                .getMetricSelect();

        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterItemSelect-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
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
                .dragAddKpiPlaceholder()
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
