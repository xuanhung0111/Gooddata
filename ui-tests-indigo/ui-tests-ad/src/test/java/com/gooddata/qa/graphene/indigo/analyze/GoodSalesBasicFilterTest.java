package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesBasicFilterTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Basic-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnDateAttribute() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .addDateFilter()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: All time");

        assertEquals(filtersBucket.configTimeFilter("This year")
                .getFilterText("Activity"), "Activity: This year");
        assertEquals(report.getTrackersCount(), 3);
        checkingOpenAsReport("filterOnDateAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void testDateInCategoryAndDateInFilter() {
        initAnalysePage();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addDate()
                .getChartReport()
                .getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: All time");
        assertEquals(analysisPage.getCategoriesBucket().getAllGranularities(),
                Arrays.asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year"));
        checkingOpenAsReport("testDateInCategoryAndDateInFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void trendingRecommendationOverrideDateFilter() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDateFilter();
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: All time");
        filtersBucket.configTimeFilter("Last 12 months");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_TREND).apply();;
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(report.getTrackersCount() >= 1);
        checkingOpenAsReport("trendingRecommendationOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void dragAndDropAttributeToFilterBucket() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .getChartReport()
                .getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");

        analysisPage.addFilter(DEPARTMENT);
        assertEquals(filtersBucket.getFilterText(DEPARTMENT), DEPARTMENT + ": All");
        checkingOpenAsReport("dragAndDropAttributeToFilterBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void addFilterDoesNotHideRecommendation() {
        initAnalysePage();

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
        checkingOpenAsReport("addFilterDoesNotHideRecommendation");
    }

    @Test(dependsOnGroups = {"init"})
    public void compararisonRecommendationOverrideDateFilter() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .addDateFilter()
            .getFilterBuckets()
            .configTimeFilter("Last year");
        ChartReport report = analysisPage.getChartReport();
        assertTrue(report.getTrackersCount() >= 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select("This month").apply();
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: This month");
        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            System.out.print("Error message: ");
            System.out.println(analysisPage.getExplorerMessage());
            System.out.println("Stop testing because of no data in [This month]");
            return;
        }
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES));
        checkingOpenAsReport("compararisonRecommendationOverrideDateFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void checkDefaultValueInDateRange() {
        initAnalysePage();

        analysisPage.addDateFilter()
            .getFilterBuckets()
            .getFilter("Activity").click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));

        panel.changeToDateRangeSection();

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT-7:00"));
        assertEquals(panel.getToDate(), getTimeString(date));

        date.add(Calendar.DAY_OF_MONTH, -29);
        assertEquals(panel.getFromDate(), getTimeString(date));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchingDateRangeNotComputeReport() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .addDateFilter()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: All time");

        WebElement dateFilter = filtersBucket.getFilter("Activity");
        dateFilter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changeToDateRangeSection();
        assertFalse(analysisPage.isReportComputing());
        panel.changeToPresetsSection();
        assertFalse(analysisPage.isReportComputing());
        dateFilter.click();
        waitForFragmentNotVisible(panel);
    }

    @Test(dependsOnGroups = {"init"})
    public void allowDateFilterByRange() throws ParseException {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .addDateFilter()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: All time");
        filtersBucket.configTimeFilterByRangeButNotApply("Activity", "01/12/2014", "01/12/2015");
        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForFragmentVisible(reportPage);
        takeScreenshot(browser, "allowDateFilterByRange-emptyFilters", getClass());
        assertTrue(reportPage.getFilters().isEmpty());
        browser.close();
        browser.switchTo().window(currentWindowHandle);

        filtersBucket.configTimeFilterByRange("Activity", "01/12/2014", "01/12/2015");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 4);
        analysisPage.exportReport();
        currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForFragmentVisible(reportPage);
        List<String> filters = reportPage.getFilters();
        takeScreenshot(browser, "allowDateFilterByRange-dateFilters", getClass());
        assertEquals(filters.size(), 1);
        assertEquals(filters.get(0), "Date (Activity) is between 01/12/2014 and 01/12/2015");
        checkRedBar(browser);
        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOnAttribute() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addAttribute(ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": All");

        WebElement filter = filtersBucket.getFilter(ACTIVITY_TYPE);
        filter.click();
        AttributeFilterPickerPanel attributePanel =
                Graphene.createPageFragment(AttributeFilterPickerPanel.class,
                        waitForElementVisible(AttributeFilterPickerPanel.LOCATOR, browser));
        attributePanel.assertPanel();
        attributePanel.discard();

        filtersBucket.configAttributeFilter(ACTIVITY_TYPE, "Email", "Web Meeting");
        assertEquals(report.getTrackersCount(), 2);
        assertEquals(filtersBucket.getFilterText(ACTIVITY_TYPE), ACTIVITY_TYPE + ": Email, Web Meeting\n(2)");
    }

    private String getTimeString(Calendar date) {
        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(String.format("%02d", date.get(Calendar.MONTH) + 1)).append("/");
        timeBuilder.append(String.format("%02d", date.get(Calendar.DAY_OF_MONTH))).append("/");
        timeBuilder.append(date.get(Calendar.YEAR));
        return timeBuilder.toString();
    }
}
