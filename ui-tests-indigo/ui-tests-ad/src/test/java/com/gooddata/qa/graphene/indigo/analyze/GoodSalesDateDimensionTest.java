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

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanelReact;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;

public class GoodSalesDateDimensionTest extends GoodSalesAbstractAnalyseTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Date-Dimension-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnFilter() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric("_Snapshot [BOP]")
            .addDateFilter()
            .waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        assertEquals(analysisPageReact.getChartReport().getTrackersCount(), 2);

        WebElement filter = filtersBucketReact.getFilter(ACTIVITY);
        filter.click();
        DateFilterPickerPanelReact panel = Graphene.createPageFragment(DateFilterPickerPanelReact.class,
                waitForElementVisible(DateFilterPickerPanelReact.LOCATOR, browser));
        assertTrue(isEqualCollection(panel.getDimensionSwitchs(), asList(ACTIVITY, CREATED)));

        panel.select("This year");
        analysisPageReact.waitForReportComputing();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": This year");
        assertTrue(analysisPageReact.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyOnFilter");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBucket() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        analysisPageReact.getAttributesBucket().changeGranularity("Month");
        analysisPageReact.waitForReportComputing();

        filtersBucketReact.configDateFilter("Last 90 days");
        analysisPageReact.waitForReportComputing();
        if (analysisPageReact.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPageReact.getExplorerMessage());
            return;
        }
        assertTrue(analysisPageReact.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyOnBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyOnBothFilterAndBucket() {
        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter();
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");

        filtersBucketReact.changeDateDimension(ACTIVITY, CREATED);
        assertEquals(filtersBucketReact.getFilterText(CREATED), CREATED + ": All time");

        analysisPageReact.addDate();
        WebElement filter = filtersBucketReact.getFilter(CREATED);
        filter.click();
        DateFilterPickerPanelReact panel = Graphene.createPageFragment(DateFilterPickerPanelReact.class,
              waitForElementVisible(DateFilterPickerPanelReact.LOCATOR, browser));
        assertFalse(panel.isDimensionSwitcherEnabled());

        analysisPageReact.getAttributesBucket().changeDateDimension(ACTIVITY);
        assertEquals(filtersBucketReact.getFilterText(ACTIVITY), ACTIVITY + ": All time");
        checkingOpenAsReport("applyOnBothFilterAndBucket");
    }
}
