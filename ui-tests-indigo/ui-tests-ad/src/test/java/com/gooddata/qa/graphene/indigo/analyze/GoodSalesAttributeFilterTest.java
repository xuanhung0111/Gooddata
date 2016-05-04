package com.gooddata.qa.graphene.indigo.analyze;

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

public class GoodSalesAttributeFilterTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");

        WebElement filter = filtersBucket.getFilter(ACTIVITY_TYPE);
        filter.click();
        AttributeFilterPickerPanel attributePanel = AttributeFilterPickerPanel.getInstance(browser);
        attributePanel.assertPanel();
        attributePanel.discard();

        filtersBucket.configAttributeFilter(ACTIVITY_TYPE, "Email", "Web Meeting");
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": Email, Web Meeting\n(2)");
    }

    @Test(dependsOnGroups = {"init"})
    public void addFilterNotHideRecommendation() {
        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .getChartReport()
                .getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.addFilter(DEPARTMENT);
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("addFilterNotHideRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnDateAndAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
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

        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .getChartReport()
                .getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");

        analysisPage.addFilter(DEPARTMENT);
        assertEquals(filtersBucket.getFilterText(DEPARTMENT), DEPARTMENT + ": All");
        checkingOpenAsReport("addAttributeToFilterBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeReplaceDate() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDate();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(filtersBucket.isDateFilterVisible());

        analysisPage.replaceAttribute(DATE, ACTIVITY_TYPE);
        assertFalse(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.isFilterVisible(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testReplaceAttribute() {
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE);
        assertTrue(filtersBucket.isFilterVisible(ACTIVITY_TYPE));

        analysisPage.addStack(DEPARTMENT);
        assertTrue(filtersBucket.isFilterVisible(DEPARTMENT));

        analysisPage.replaceAttribute(ACTIVITY_TYPE, "Region");
        assertFalse(filtersBucket.isFilterVisible(ACTIVITY_TYPE));
        assertTrue(filtersBucket.isFilterVisible("Region"));
        checkingOpenAsReport("testReplaceAttribute");
    }
}
