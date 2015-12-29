package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class MetricBucketTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Metric-Bucket-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_metric_details() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();

        hover(".s-bucket-metrics .inlineBubbleHelp");
        assertTrue(isElementPresent(cssSelector(".s-catalogue-bubble-loaded"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_fact_details() {
        initAnalysePageByUrl();

        analysisPage.addMetric(AMOUNT, FieldType.FACT)
            .getMetricsBucket()
            .getMetricConfiguration("Sum of " + AMOUNT)
            .expandConfiguration();
        hover(".s-bucket-metrics .inlineBubbleHelp");
        assertTrue(isElementPresent(cssSelector(".s-catalogue-bubble-loaded"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_attribute_details() {
        initAnalysePageByUrl();

        analysisPage.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .getMetricsBucket()
            .getMetricConfiguration("Count of " + ACTIVITY_TYPE)
            .expandConfiguration();
        hover(".s-bucket-metrics .inlineBubbleHelp");
        assertTrue(isElementPresent(cssSelector(".s-catalogue-bubble-loaded"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_open_and_close_configuration() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertFalse(isElementPresent(cssSelector(".s-bucket-metrics input[type=checkbox]"), browser));

        MetricConfiguration configuration = analysisPage.getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(isElementPresent(cssSelector(".s-bucket-metrics input[type=checkbox]"), browser));

        configuration.collapseConfiguration();
        assertFalse(isElementPresent(cssSelector(".s-bucket-metrics input[type=checkbox]"), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_able_to_drop_second_metric_into_bucket() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .waitForReportComputing();

        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-y"), browser));
        assertTrue(isElementPresent(cssSelector(".adi-components .visualization-column .s-property-color"), browser));

        assertEquals(analysisPage.getChartReport().getLegends(), asList(NUMBER_OF_ACTIVITIES, NUMBER_OF_LOST_OPPS));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_show_in_percent_correctly() {
        initAnalysePageByUrl();

        MetricConfiguration configuration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertFalse(configuration.isShowPercentEnabled());

        analysisPage.addAttribute(ACTIVITY_TYPE);
        assertTrue(configuration.isShowPercentEnabled());

        assertFalse(analysisPage.addMetric(QUOTA)
            .getMetricsBucket()
            .getMetricConfiguration(QUOTA)
            .expandConfiguration()
            .isShowPercentEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_show_PoP_correctly() {
        initAnalysePageByUrl();

        MetricConfiguration configuration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertFalse(configuration.isPopEnabled());

        analysisPage.addDate();
        assertTrue(configuration.isPopEnabled());

        assertFalse(analysisPage.addMetric(QUOTA)
            .getMetricsBucket()
            .getMetricConfiguration(QUOTA)
            .expandConfiguration()
            .isPopEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_remove_PoP_after_second_metric_is_added() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();
        assertEquals(analysisPage.waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));

        assertEquals(analysisPage.addMetric(QUOTA)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(NUMBER_OF_ACTIVITIES, QUOTA));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_remove_percent_if_2_metric_is_added() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPercents();

        assertEquals(analysisPage.addMetric(QUOTA)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(NUMBER_OF_ACTIVITIES, QUOTA));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_remove_second_metric_if_user_wants() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList(NUMBER_OF_ACTIVITIES, NUMBER_OF_LOST_OPPS));

        assertFalse(analysisPage.removeMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .isLegendVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_allow_to_add_second_instance_of_metric_already_bucket() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getItemNames()
            .size(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_hide_legend_for_only_one_metric() {
        initAnalysePageByUrl();

        assertFalse(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
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
