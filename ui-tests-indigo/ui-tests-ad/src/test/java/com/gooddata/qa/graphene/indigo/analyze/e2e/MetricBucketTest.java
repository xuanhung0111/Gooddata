package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class MetricBucketTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Metric-Bucket-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createNumberOfLostOppsMetric();
        getMetricCreator().createQuotaMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_metric_details() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        hover(".s-bucket-measures .inlineBubbleHelp");
        assertTrue(isElementPresent(cssSelector(".s-catalogue-bubble-loaded"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_fact_details() {
        initAnalysePage().addMetric(FACT_AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + FACT_AMOUNT)
            .expandConfiguration();
        hover(".s-bucket-measures .inlineBubbleHelp");
        assertTrue(isElementPresent(cssSelector(".s-catalogue-bubble-loaded"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_display_attribute_details() {
        initAnalysePage().addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .getMetricsBucket()
            .getMetricConfiguration("Count of " + ATTR_ACTIVITY_TYPE)
            .expandConfiguration();
        hover(".s-bucket-measures .inlineBubbleHelp");
        assertTrue(isElementPresent(cssSelector(".s-catalogue-bubble-loaded"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_open_and_close_configuration() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        assertFalse(isElementPresent(cssSelector(".s-bucket-measures input[type=checkbox]"), browser));

        MetricConfiguration configuration = analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(isElementPresent(cssSelector(".s-bucket-measures input[type=checkbox]"), browser));

        configuration.collapseConfiguration();
        assertFalse(isElementPresent(cssSelector(".s-bucket-measures input[type=checkbox]"), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_able_to_drop_second_metric_into_bucket() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-y"), browser));
        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-color"), browser));

        assertEquals(analysisPage.getChartReport().getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_disable_show_in_percent_correctly() {
        MetricConfiguration configuration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertFalse(configuration.isShowPercentEnabled());

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(configuration.isShowPercentEnabled());

        assertFalse(analysisPage.addMetric(METRIC_QUOTA)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_QUOTA)
            .expandConfiguration()
            .isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_disable_show_PoP_correctly() {
        MetricConfiguration configuration = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertFalse(configuration.isPopEnabled());

        analysisPage.addDate();
        assertTrue(configuration.isPopEnabled());

        assertFalse(analysisPage.addMetric(METRIC_QUOTA)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_QUOTA)
            .expandConfiguration()
            .isPopEnabled());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_remove_PoP_after_second_metric_is_added() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();
        assertEquals(analysisPage.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));

        assertEquals(analysisPage.addMetric(METRIC_QUOTA)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_remove_percent_if_2_metric_is_added() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPercents();

        assertEquals(analysisPage.addMetric(METRIC_QUOTA)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_QUOTA));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_remove_second_metric_if_user_wants() {
        assertEquals(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_NUMBER_OF_LOST_OPPS));

        assertFalse(analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .isLegendVisible());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_allow_to_add_second_instance_of_metric_already_bucket() {
        assertEquals(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getItemNames()
            .size(), 2);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_hide_legend_for_only_one_metric() {
        assertFalse(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .isLegendVisible());
    }

    private void hover(String cssLocator) {
        new Actions(browser)
            .moveToElement(waitForElementVisible(cssSelector(cssLocator), browser))
            .perform();
    }
}
