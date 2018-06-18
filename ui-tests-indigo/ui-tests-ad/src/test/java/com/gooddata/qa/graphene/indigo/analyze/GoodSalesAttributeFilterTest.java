package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesAttributeFilterTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Filter-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterOnAttribute() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ":\nAll");

        WebElement filter = filtersBucketReact.getFilter(ATTR_ACTIVITY_TYPE);
        filter.click();
        AttributeFilterPickerPanel attributePanel = AttributeFilterPickerPanel.getInstance(browser);
        attributePanel.assertPanel();
        attributePanel.discard();

        filtersBucketReact.configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ":\nEmail, Web Meeting");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addFilterNotHideRecommendation() {
        assertEquals(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
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

        analysisPage.addFilter(ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("addFilterNotHideRecommendation");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void addAttributeToFilterBucket() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        assertEquals(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ":\nAll");

        analysisPage.addFilter(ATTR_DEPARTMENT);
        assertEquals(filtersBucketReact.getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ":\nAll");
        checkingOpenAsReport("addAttributeToFilterBucket");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openReportAppliedAttributeFilter() {
        analysisPage.changeReportType(ReportType.TABLE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_DEPARTMENT)
                .setFilterIsntValues(ATTR_DEPARTMENT, "Direct Sales")
                .addAttribute(ATTR_STATUS)
                .setFilterIsValues(ATTR_STATUS, "Completed", "Deferred")
                .waitForReportComputing().exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            takeScreenshot(browser, "Insight-open-as-report", getClass());
            assertEquals(reportPage.waitForReportExecutionProgress().getFilters(),
                    asList("Department isn't Direct Sales", "Status is Completed, Deferred"));
            reportPage.finishCreateReport();
        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAttributeReplaceDate() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(filtersBucketReact.isDateFilterVisible());

        analysisPage.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
        assertFalse(filtersBucketReact.isDateFilterVisible());
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testReplaceAttribute() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE));

        analysisPage.addStack(ATTR_DEPARTMENT);
        assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT));

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, "Region");
        assertFalse(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE));
        assertTrue(filtersBucketReact.isFilterVisible("Region"));
        checkingOpenAsReport("testReplaceAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkRelatedDateShownCorrectly() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();

        AttributesBucket attributesBucket = analysisPage.getAttributesBucket();

        takeScreenshot(browser, "Related-date-shows-correctly-with-metric-activity", getClass());
        assertEquals(attributesBucket.getSelectedDimensionSwitch(), "Activity");
        assertEquals(attributesBucket.getSelectedGranularity(), "Year");

        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES).removeAttribute(DATE);
        analysisPage.addMetric(METRIC_AMOUNT).addDate();

        takeScreenshot(browser, "Relate-date-shows-correctly-with-metric-amount", getClass());
        assertEquals(attributesBucket.getSelectedDimensionSwitch(), "Closed");
        assertEquals(attributesBucket.getSelectedGranularity(), "Year");
    }
}
