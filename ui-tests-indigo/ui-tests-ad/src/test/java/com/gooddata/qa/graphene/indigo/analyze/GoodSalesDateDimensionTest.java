package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;

public class GoodSalesDateDimensionTest extends GoodSalesAbstractAnalyseTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Date-Dimension-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnFilter() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric("_Snapshot [BOP]")
            .addDateFilter()
            .waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);

        WebElement filter = filtersBucketReact.getFilter(ACTIVITY);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertTrue(isEqualCollection(panel.getDimensionSwitchs(), asList(ACTIVITY, CREATED)));

        panel.select("This year");
        analysisPage.waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": This year");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyOnFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBucket() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        analysisPage.getAttributesBucket().changeGranularity("Month");
        analysisPage.waitForReportComputing();

        filtersBucketReact.configDateFilter("Last 90 days");
        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyOnBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBothFilterAndBucket() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        filtersBucketReact.changeDateDimension(ACTIVITY, CREATED);
        assertEquals(filtersBucketReact.getFilterText(CREATED), CREATED + ": All time");

        analysisPage.addDate();
        WebElement filter = filtersBucketReact.getFilter(CREATED);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
              waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertFalse(panel.isDimensionSwitcherEnabled());

        analysisPage.getAttributesBucket().changeDateDimension(ACTIVITY);
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        checkingOpenAsReport("applyOnBothFilterAndBucket");
    }
}
