package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
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

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesDateFilterTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Date-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void checkDefaultValueInDateRange() {
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
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText("Activity"), "Activity: All time");

        WebElement dateFilter = filtersBucketReact.getFilter("Activity");
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
    public void allowFilterByRange() throws ParseException {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText("Activity"), "Activity: All time");

        filtersBucketReact.configDateFilterByRangeButNotApply("01/12/2014", "01/12/2015");
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        waitForFragmentVisible(reportPage);
        takeScreenshot(browser, "allowDateFilterByRange-emptyFilters", getClass());
        assertTrue(reportPage.getFilters().isEmpty());
        browser.close();
        BrowserUtils.switchToFirstTab(browser);

        filtersBucketReact.configDateFilter("01/12/2014", "01/12/2015");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 4);
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        waitForFragmentVisible(reportPage);
        List<String> filters = reportPage.getFilters();
        takeScreenshot(browser, "allowDateFilterByRange-dateFilters", getClass());
        assertEquals(filters.size(), 1);
        assertEquals(filters.get(0), "Date (Activity) is between 01/12/2014 and 01/12/2015");
        checkRedBar(browser);
        browser.close();
        BrowserUtils.switchToFirstTab(browser);
    }

    @Test(dependsOnGroups = {"init"})
    public void testDateInCategoryAndDateInFilter() {
        assertTrue(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: All time");
        assertEquals(analysisPage.getAttributesBucket().getAllGranularities(),
                Arrays.asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year"));
        checkingOpenAsReport("testDateInCategoryAndDateInFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchBetweenPresetsAndDataRange() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().getFilterBuckets().configDateFilter("Last 90 days");
        analysisPage.waitForReportComputing();

        WebElement dateFilter = analysisPage.getFilterBuckets().getFilter("Activity");
        dateFilter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changeToDateRangeSection();
        assertFalse(analysisPage.isReportComputing());
        panel.configTimeFilter("01/14/2015", "04/13/2015");
        analysisPage.waitForReportComputing();

        dateFilter.click();
        panel.changeToPresetsSection();
        assertFalse(analysisPage.isReportComputing());
        panel.select("This month");
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("switchBetweenPresetsAndDataRange");
    }

    @Test(dependsOnGroups = {"init"})
    public void showPercentAfterConfigDate() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addDate()
                    .getFilterBuckets()
                    .configDateFilter("Last 90 days");
        analysisPage.getMetricsBucket()
                    .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                    .expandConfiguration()
                    .showPercents();
        analysisPage.waitForReportComputing();
        // wait for data labels rendered
        sleepTight(2000);

        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }

        ChartReport report = analysisPage.getChartReport();
        assertTrue(Iterables.all(report.getDataLabels(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.endsWith("%");
            }
        }));
        checkingOpenAsReport("showPercentAfterConfigDate");
    }

    @Test(dependsOnGroups = {"init"})
    public void popAfterConfigDate() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addDate()
                    .getFilterBuckets()
                    .configDateFilter("Last 90 days");

        analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();

        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }

        ChartReport report = analysisPage.getChartReport();

        assertTrue(isEqualCollection(report.getLegends(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES)));
        checkingOpenAsReport("popAfterConfigDate");
    }

    @Test(dependsOnGroups = {"init"}, description = "CL-9807: Problems with export of date filters")
    public void exportDateFilter() {
        final String dateFilterValue = "Last 4 quarters";
        analysisPage.addDateFilter()
                .getFilterBuckets()
                .configDateFilter(dateFilterValue);
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            final ReportFilter reportFilter = reportPage.openFilterPanel();
            takeScreenshot(browser, "export-date-filter", getClass());
            assertTrue(reportFilter.getFilterElement("Quarter/Year (Activity) is the last 4 quarters").isDisplayed(),
                    dateFilterValue + " filter is not displayed");
            browser.close();
        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    private String getTimeString(Calendar date) {
        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(String.format("%02d", date.get(Calendar.MONTH) + 1)).append("/");
        timeBuilder.append(String.format("%02d", date.get(Calendar.DAY_OF_MONTH))).append("/");
        timeBuilder.append(date.get(Calendar.YEAR));
        return timeBuilder.toString();
    }
}
