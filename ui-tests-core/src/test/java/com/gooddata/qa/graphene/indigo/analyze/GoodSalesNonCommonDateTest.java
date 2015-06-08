package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.*;

import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesNonCommonDateTest extends AnalyticalDesignerAbstractTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";
    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Demo-Non-Common-Date-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnFilter() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition()
            .withMetrics(NUMBER_OF_ACTIVITIES, "_Snapshot [BOP]").withFilters(DATE));
        assertEquals(analysisPage.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);

        WebElement filter = analysisPage.getFilter(ACTIVITY);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertTrue(CollectionUtils.isEqualCollection(panel.getAllDimensionSwitchs(),
                Arrays.asList(ACTIVITY, "Closed", CREATED, "Snapshot", "Timeline")));

        panel.select("This year");
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getFilterText(ACTIVITY), ACTIVITY + ": This year");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBucket() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        assertEquals(analysisPage.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        analysisPage.changeGranularity("Month");
        analysisPage.waitForReportComputing();

        analysisPage.configTimeFilter("Last 90 days");
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBothFilterAndBucket() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withFilters(DATE));
        assertEquals(analysisPage.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        analysisPage.changeDimensionSwitchInFilter(ACTIVITY, CREATED);
        assertEquals(analysisPage.getFilterText(CREATED), CREATED + ": All time");

        analysisPage.addCategory(DATE);
        WebElement filter = analysisPage.getFilter(CREATED);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
              waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertFalse(panel.isDimensionSwitcherEnabled());

        analysisPage.changeDimensionSwitchInBucket(ACTIVITY);
        assertEquals(analysisPage.getFilterText(ACTIVITY), ACTIVITY + ": All time");
    }

    @Test(dependsOnGroups = {"init"})
    public void greyOutMetricAttribute() throws InterruptedException {
        initAnalysePage();

        analysisPage.addCategory(DATE);
        // AD needs time to calculate not available attributes/metrics
        Thread.sleep(3000);
        assertTrue(analysisPage.isInapplicableAttributeMetricInViewPort());
    }

    @Test(dependsOnGroups = {"init"})
    public void showPercent() throws InterruptedException {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        analysisPage.configTimeFilter("Last 90 days")
                    .turnOnShowInPercents()
                    .waitForReportComputing();
        // wait for data labels rendered
        Thread.sleep(2000);

        ChartReport report = analysisPage.getChartReport();
        assertTrue(Iterables.all(report.getDataLabels(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.endsWith("%");
            }
        }));
    }

    @Test(dependsOnGroups = {"init"})
    public void periodOverPeriod() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        analysisPage.configTimeFilter("Last 90 days")
                    .compareToSamePeriodOfYearBefore()
                    .waitForReportComputing();

        ChartReport report = analysisPage.getChartReport();
        analysisPage.waitForReportComputing();
        assertTrue(CollectionUtils.isEqualCollection(report.getLegends(),
                Arrays.asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES)));
    }

    @Test(dependsOnGroups = {"init"})
    public void switchBetweenPresetsAndDataRange() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        analysisPage.configTimeFilter("Last 90 days");
        analysisPage.waitForReportComputing();

        WebElement dateFilter = analysisPage.getFilter(ACTIVITY);
        dateFilter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changeToDateRangeSection();
        assertFalse(analysisPage.isReportComputing());
        panel.configTimeFilterByRange("01/14/2015", "04/13/2015");
        analysisPage.waitForReportComputing();

        dateFilter.click();
        panel.changeToPresetsSection();
        assertFalse(analysisPage.isReportComputing());
        panel.select("This month");
        analysisPage.waitForReportComputing();
    }

    @Test(dependsOnGroups = {"init"})
    public void undoRedoOnBucket() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        assertTrue(analysisPage.undo().isCategoryBucketEmpty());
        assertFalse(analysisPage.redo().isCategoryBucketEmpty());

        analysisPage.changeDimensionSwitchInBucket(CREATED);
        assertEquals(analysisPage.getSelectedDimensionSwitch(), CREATED);
        assertEquals(analysisPage.undo().getSelectedDimensionSwitch(), ACTIVITY);
        assertEquals(analysisPage.redo().getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"init"})
    public void undoRedoOnFilter() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withFilters(DATE));
        assertFalse(analysisPage.undo().isFilterVisible(ACTIVITY));
        assertTrue(analysisPage.redo().isFilterVisible(ACTIVITY));

        WebElement filter = analysisPage.getFilter(ACTIVITY);
        analysisPage.changeDimensionSwitchInFilter(ACTIVITY, CREATED);

        analysisPage.undo();
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
              waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertEquals(panel.getSelectedDimensionSwitch(), ACTIVITY);

        analysisPage.redo();
        filter.click();
        waitForElementVisible(panel.getRoot());
        assertEquals(panel.getSelectedDimensionSwitch(), CREATED);
    }
}
