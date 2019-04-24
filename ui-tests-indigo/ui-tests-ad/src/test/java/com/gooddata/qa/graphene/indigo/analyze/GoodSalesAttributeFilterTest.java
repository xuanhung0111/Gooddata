package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
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
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

import java.util.Arrays;

public class GoodSalesAttributeFilterTest extends AbstractAnalyseTest {

    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Filter-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterOnAttribute() {
        final FiltersBucket filtersBucketReact = initAnalysePage().getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE)), Arrays.asList(ATTR_ACTIVITY_TYPE, "All"));

        WebElement filter = filtersBucketReact.getFilter(ATTR_ACTIVITY_TYPE);
        filter.click();
        AttributeFilterPickerPanel attributePanel = AttributeFilterPickerPanel.getInstance(browser);
        attributePanel.assertPanel();
        attributePanel.discard();

        filtersBucketReact.configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Web Meeting");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE)), Arrays.asList(ATTR_ACTIVITY_TYPE, "Email, Web Meeting"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addFilterNotHideRecommendation() {
        assertEquals(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS), "Percents recommendation should display");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");

        analysisPage.addFilter(ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS), "Percents recommendation should display");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");
        checkingOpenAsReport("addFilterNotHideRecommendation");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void addAttributeToFilterBucket() {
        final FiltersBucket filtersBucketReact = initAnalysePage().getFilterBuckets();

        assertEquals(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 4);
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(ATTR_ACTIVITY_TYPE)), Arrays.asList(ATTR_ACTIVITY_TYPE, "All"));

        analysisPage.addFilter(ATTR_DEPARTMENT);
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(ATTR_DEPARTMENT)), Arrays.asList(ATTR_DEPARTMENT, "All"));
        checkingOpenAsReport("addAttributeToFilterBucket");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void openReportAppliedAttributeFilter() {
        initAnalysePage().changeReportType(ReportType.TABLE)
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
        setExtendedStackingFlag(false);
        try {
            final FiltersBucket filtersBucketReact = initAnalysePage().getFilterBuckets();

            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
            assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1,
                    "Number of Trackers should be greater or equal 1");
            assertTrue(filtersBucketReact.isDateFilterVisible(), "Date filter should display");

            analysisPage.replaceAttribute(DATE, ATTR_ACTIVITY_TYPE);
            assertFalse(filtersBucketReact.isDateFilterVisible(), "Date filter shouldn't display");
            assertTrue(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE),
                    ATTR_ACTIVITY_TYPE + " filter should display");
        } finally {
            setExtendedStackingFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testReplaceAttribute() {
        setExtendedStackingFlag(false);
        try {
            final FiltersBucket filtersBucketReact = initAnalysePage().getFilterBuckets();

            analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE);
            assertTrue(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE),
                    ATTR_ACTIVITY_TYPE + " filter should display");

            analysisPage.addStack(ATTR_DEPARTMENT);
            assertTrue(filtersBucketReact.isFilterVisible(ATTR_DEPARTMENT), ATTR_DEPARTMENT + " filter should display");

            analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, "Region");
            assertFalse(filtersBucketReact.isFilterVisible(ATTR_ACTIVITY_TYPE),
                    ATTR_ACTIVITY_TYPE + " filter shouldn't display");
            assertTrue(filtersBucketReact.isFilterVisible("Region"), "Region filter should display");
            checkingOpenAsReport("testReplaceAttribute");
        } finally {
            setExtendedStackingFlag(true);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkRelatedDateShownCorrectly() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();

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

    @Test(dependsOnGroups = {"createProject"})
    public void checkAllValueInSearchPanel() {
        initAnalysePage().addFilter(ATTR_ACTIVITY);
        analysisPage.getFilterBuckets().getFilter(ATTR_ACTIVITY).click();

        AttributeFilterPickerPanel attributeFilterPickerPanel = AttributeFilterPickerPanel.getInstance(browser);
        attributeFilterPickerPanel.searchForText("Email");
        assertFalse(attributeFilterPickerPanel.isUncheckAll(), "Should check All");
        attributeFilterPickerPanel
                .scrollElementIntoView("Email with 3dCart Shopping Cart Software on Jul-04-09")
                .uncheckAllCheckbox();
        assertEquals(attributeFilterPickerPanel.getLimitedWarningText(), "Sorry, you have exceeded the limit (500).");
        assertTrue(attributeFilterPickerPanel.isUncheckAll(), "Should uncheck All");

        attributeFilterPickerPanel
                .scrollElementIntoView("Email with 1 Source Consulting on Feb-03-10")
                .checkAllCheckbox();
        assertFalse(attributeFilterPickerPanel.isUncheckAll(), "Should check All");
    }

    private void setExtendedStackingFlag(boolean status) {
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_EXTENDED_STACKING, status);
    }
}
