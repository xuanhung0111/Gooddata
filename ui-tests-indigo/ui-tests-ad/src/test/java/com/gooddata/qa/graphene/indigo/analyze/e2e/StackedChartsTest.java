package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class StackedChartsTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Stacked-Charts-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_put_stack_by_attribute_into_color_series() {
        assertEquals(analysisPage.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_totals_for_stacked_columns() {
        analysisPage.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertEquals(browser.findElements(cssSelector(".highcharts-stack-labels text")).size(), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_stack_warn_msg_when_there_is_something_in_stack_by_bucket() {
        assertFalse(analysisPage.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getWarningMessage()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_stack_warn_msg_if_there_is_more_than_1_metrics() {
        assertFalse(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .addAttribute(ATTR_ACCOUNT)
            .getStacksBucket()
            .getWarningMessage()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_disappear_when_visualization_is_switched_to_table_and_should_be_empty_when_going_back() {
        analysisPage.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.TABLE);

        assertFalse(isElementPresent(cssSelector(StacksBucket.CSS_SELECTOR), browser));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(analysisPage.getMetricsBucket().isEmpty());
        assertFalse(analysisPage.getStacksBucket().isEmpty());
        assertFalse(analysisPage.getAttributesBucket().isEmpty());
    }

    // Unstable https://jira.intgdc.com/browse/CL-9774
    @Test(dependsOnGroups = {"createProject"}, enabled = false)
    public void should_disappear_when_switched_to_table_via_result_too_large_link() {
        analysisPage.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_ACCOUNT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES);

        waitForElementVisible(cssSelector(".s-error-too-many-data-points .s-switch-to-table"), browser).click();

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(analysisPage.getMetricsBucket().isEmpty());
        assertFalse(analysisPage.getStacksBucket().isEmpty());
        assertFalse(analysisPage.getAttributesBucket().isEmpty());
    }
}
