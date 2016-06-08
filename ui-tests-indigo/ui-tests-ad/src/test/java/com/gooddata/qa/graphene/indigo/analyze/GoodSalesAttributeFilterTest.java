package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesAttributeFilterTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": All");

        WebElement filter = filtersBucket.getFilter(ATTR_ACTIVITY_TYPE);
        filter.click();
        AttributeFilterPickerPanel attributePanel = AttributeFilterPickerPanel.getInstance(browser);
        attributePanel.assertPanel();
        attributePanel.discard();

        filtersBucket.configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(filtersBucket.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": Email, Web Meeting\n(2)");
    }

    @Test(dependsOnGroups = {"init"})
    public void addFilterNotHideRecommendation() {
        assertEquals(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .getChartReport()
                .getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addFilter(ATTR_DEPARTMENT);
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("addFilterNotHideRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnDateAndAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: All time");

        assertEquals(filtersBucket.configDateFilter("This year")
                .getFilterText("Activity"), "Activity: This year");
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("filterOnDateAndAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void addAttributeToFilterBucket() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        assertEquals(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .getChartReport()
                .getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ": All");

        analysisPage.addFilter(ATTR_DEPARTMENT);
        assertEquals(filtersBucket.getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ": All");
        checkingOpenAsReport("addAttributeToFilterBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeReplaceDate() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(filtersBucket.isDateFilterVisible());

        analysisPage.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
        assertFalse(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.isFilterVisible(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testReplaceAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(filtersBucket.isFilterVisible(ATTR_ACTIVITY_TYPE));

        analysisPage.addStack(ATTR_DEPARTMENT);
        assertTrue(filtersBucket.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, "Region");
        assertFalse(filtersBucket.isFilterVisible(ATTR_ACTIVITY_TYPE));
        assertTrue(filtersBucket.isFilterVisible("Region"));
        checkingOpenAsReport("testReplaceAttribute");
    }
}
