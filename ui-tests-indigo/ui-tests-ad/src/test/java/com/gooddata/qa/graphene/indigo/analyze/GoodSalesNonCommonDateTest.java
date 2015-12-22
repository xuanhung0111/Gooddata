package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CategoriesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesNonCommonDateTest extends AnalyticalDesignerAbstractTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";
    private static final String ACTIVITY_DATE = "Activity (Date)";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Non-Common-Date-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnFilter() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric("_Snapshot [BOP]")
            .addDateFilter();
        assertEquals(filtersBucket.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);

        WebElement filter = filtersBucket.getFilter(ACTIVITY);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertTrue(isEqualCollection(panel.getAllDimensionSwitchs(),
                asList(ACTIVITY, "Closed", CREATED, "Snapshot", "Timeline")));

        panel.select("This year");
        analysisPage.waitForReportComputing();
        assertEquals(filtersBucket.getFilterText(ACTIVITY), ACTIVITY + ": This year");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyOnFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBucket() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDate();
        assertEquals(filtersBucket.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        analysisPage.getCategoriesBucket().changeGranularity("Month");
        analysisPage.waitForReportComputing();

        filtersBucket.configTimeFilter("Last 90 days");
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyOnBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBothFilterAndBucket() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDateFilter();
        assertEquals(filtersBucket.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        filtersBucket.changeDimensionSwitchInFilter(ACTIVITY, CREATED);
        assertEquals(filtersBucket.getFilterText(CREATED), CREATED + ": All time");

        analysisPage.addDate();
        WebElement filter = filtersBucket.getFilter(CREATED);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
              waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertFalse(panel.isDimensionSwitcherEnabled());

        analysisPage.getCategoriesBucket().changeDimensionSwitchInBucket(ACTIVITY);
        assertEquals(filtersBucket.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        checkingOpenAsReport("applyOnBothFilterAndBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void showPercent() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                    .addDate()
                    .getFilterBuckets()
                    .configTimeFilter("Last 90 days");
        analysisPage.getMetricsBucket()
                    .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                    .expandConfiguration()
                    .showPercents();
        analysisPage.waitForReportComputing();
        // wait for data labels rendered
        sleepTight(2000);

        ChartReport report = analysisPage.getChartReport();
        assertTrue(Iterables.all(report.getDataLabels(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.endsWith("%");
            }
        }));
        checkingOpenAsReport("showPercent");
    }

    @Test(dependsOnGroups = {"init"})
    public void periodOverPeriod() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                    .addDate()
                    .getFilterBuckets()
                    .configTimeFilter("Last 90 days");

        analysisPage.getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();

        assertTrue(isEqualCollection(report.getLegends(),
                asList(NUMBER_OF_ACTIVITIES + " - previous year", NUMBER_OF_ACTIVITIES)));
        checkingOpenAsReport("periodOverPeriod");
    }

    @Test(dependsOnGroups = {"init"})
    public void switchBetweenPresetsAndDataRange() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDate().getFilterBuckets().configTimeFilter("Last 90 days");
        analysisPage.waitForReportComputing();

        WebElement dateFilter = analysisPage.getFilterBuckets().getFilter(ACTIVITY);
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
        checkingOpenAsReport("switchBetweenPresetsAndDataRange");
    }

    @Test(dependsOnGroups = {"init"})
    public void undoRedoOnBucket() {
        initAnalysePage();
        final CategoriesBucket categoriesBucket = analysisPage.getCategoriesBucket();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDate().undo();
        assertTrue(categoriesBucket.isEmpty());
        analysisPage.redo();
        assertFalse(categoriesBucket.isEmpty());

        categoriesBucket.changeDimensionSwitchInBucket(CREATED);
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
        analysisPage.undo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), ACTIVITY);
        analysisPage.redo();
        assertEquals(categoriesBucket.getSelectedDimensionSwitch(), CREATED);
    }

    @Test(dependsOnGroups = {"init"})
    public void undoRedoOnFilter() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDateFilter().undo();
        assertFalse(filtersBucket.isFilterVisible(ACTIVITY));

        analysisPage.redo();
        assertTrue(filtersBucket.isFilterVisible(ACTIVITY));

        WebElement filter = filtersBucket.getFilter(ACTIVITY);
        filtersBucket.changeDimensionSwitchInFilter(ACTIVITY, CREATED);

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

    @Test(dependsOnGroups = {"init"})
    public void exploreFact() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(ACTIVITY_DATE).append("\n")
                .append("Field Type\n")
                .append("Measure\n")
                .append("Dataset\n")
                .append("Activity\n");
        assertEquals(analysisPage.getCataloguePanel().getFactDescription(ACTIVITY_DATE), expected.toString());
    }
}
