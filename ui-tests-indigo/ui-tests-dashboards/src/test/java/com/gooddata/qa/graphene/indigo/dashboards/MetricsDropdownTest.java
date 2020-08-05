package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.MetricSelect;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MetricsDropdownTest extends AbstractDashboardTest {

    @Override
    protected void customizeProject() throws Throwable {
        //add more metrics to display search metric
        Metrics metricCreator = getMetricCreator();
        metricCreator.createAmountMetric();
        metricCreator.createAmountBOPMetric();
        metricCreator.createAvgAmountMetric();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createNumberOfLostOppsMetric();
        metricCreator.createNumberOfOpportunitiesMetric();
        metricCreator.createNumberOfOpportunitiesBOPMetric();
        metricCreator.createTimelineBOPMetric();
        metricCreator.createTimelineEOPMetric();
        metricCreator.createPercentOfGoalMetric();
        metricCreator.createProbabilityMetric();
        metricCreator.createQuotaMetric();
        new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .createAnalyticalDashboard(singletonList(createAmountKpi()));
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

        waitForFragmentVisible(indigoDashboardsPage).dragAddKpiPlaceholderNext(); // dragging addKpiPlaceholder shall close MetricsDropdown
        ms.ensureDropdownOpen();
        takeScreenshot(browser, "checkSearchStringResetAfterDropdownClose-searchField_<empty>", this.getClass());

        checkDropdownDidReset();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkSearchStringResetAfterItemSelect() {
        IndigoDashboardsPage page = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .dragAddKpiPlaceholderNext();

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

        assertTrue(ms.getValues().isEmpty(), "There is selected metric");
        assertTrue(ms.isShowingNoMatchingDataMessage(), "No matching data message should display");
    }

    private MetricSelect getMetricSelect() {
        return initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .dragAddKpiPlaceholderNext()
                .getConfigurationPanel()
                .getMetricSelect();
    }

    private void checkDropdownDidReset() {
        MetricSelect ms = indigoDashboardsPage
                .getConfigurationPanel()
                .getMetricSelect();
        assertTrue(ms.getSearchText().isEmpty(), "Search box should be empty");
        Screenshots.takeScreenshot(browser, "checkDropdownSearchEmpty", MetricsDropdownTest.class);

        waitForCollectionIsNotEmpty(ms, 12);
        assertTrue(ms.getValues().size() > 10, "Missing some selected metrics");
        Screenshots.takeScreenshot(browser, "checkDropdownValues", MetricsDropdownTest.class);
    }

    private void waitForCollectionIsNotEmpty(MetricSelect metricSelect, int size) {
        int i = 0;
        while (metricSelect.getValues().isEmpty() || metricSelect.getValues().size() != size && i < 5) {
            sleepTightInSeconds(2);
            i++;
        }
    }
}
