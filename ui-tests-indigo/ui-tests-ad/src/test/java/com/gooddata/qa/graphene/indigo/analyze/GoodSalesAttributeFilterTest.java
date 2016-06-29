package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class GoodSalesAttributeFilterTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnAttribute() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": All");

        WebElement filter = filtersBucketReact.getFilter(ATTR_ACTIVITY_TYPE);
        filter.click();
        AttributeFilterPickerPanel attributePanel = AttributeFilterPickerPanel.getInstance(browser);
        attributePanel.assertPanel();
        attributePanel.discard();

        filtersBucketReact.configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");
        analysisPageReact.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": Email, Web Meeting\n(2)");
    }

    @Test(dependsOnGroups = {"init"})
    public void addFilterNotHideRecommendation() {
        assertEquals(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.addFilter(ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("addFilterNotHideRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnDateAndAttribute() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        ChartReportReact report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText("Activity"), "Activity: All time");

        assertEquals(filtersBucketReact.configDateFilter("This year")
                .getFilterText("Activity"), "Activity: This year");
        analysisPageReact.waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("filterOnDateAndAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void addAttributeToFilterBucket() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        assertEquals(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": All");

        analysisPageReact.addFilter(ATTR_DEPARTMENT);
        assertEquals(filtersBucketReact.getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ": All");
        checkingOpenAsReport("addAttributeToFilterBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeReplaceDate() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        assertTrue(analysisPageReact.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(filtersBucketReact.isDateFilterVisible());

        analysisPageReact.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
        assertFalse(filtersBucketReact.isDateFilterVisible());
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testReplaceAttribute() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE));

        analysisPageReact.addStack(ATTR_DEPARTMENT);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPageReact.replaceAttribute(ATTR_ACTIVITY_TYPE, "Region");
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE));
        assertTrue(filtersBucketReact.isFilterVisible("Region"));
        checkingOpenAsReport("testReplaceAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void checkRelatedDateShownCorrectly() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();

        AttributesBucketReact attributesBucket = analysisPageReact.getAttributesBucket();

        takeScreenshot(browser, "Related-date-shows-correctly-with-metric-activity", getClass());
        assertEquals(attributesBucket.getSelectedDimensionSwitch(), "Activity");
        assertEquals(attributesBucket.getSelectedGranularity(), "Year");

        analysisPageReact.removeMetric(METRIC_NUMBER_OF_ACTIVITIES).removeAttribute(DATE);
        analysisPageReact.addMetric(METRIC_AMOUNT).addDate();

        takeScreenshot(browser, "Relate-date-shows-correctly-with-metric-amount", getClass());
        assertEquals(attributesBucket.getSelectedDimensionSwitch(), "Closed");
        assertEquals(attributesBucket.getSelectedGranularity(), "Year");
    }
}
