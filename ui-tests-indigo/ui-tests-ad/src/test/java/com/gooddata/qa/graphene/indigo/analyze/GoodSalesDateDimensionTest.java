package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;

public class GoodSalesDateDimensionTest extends AbstractAnalyseTest {

    private static final String ACTIVITY = "Activity";
    private static final String CREATED = "Created";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Dimension-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyOnFilter() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_SNAPSHOT_BOP)
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-9980: Date filter isn't remained when adding trending from recommendation panel, " +
                    "covered by TestCafe")
    public void keepDateDimensionAfterApplyingSeeTrendRecommendation() {
        final String newDateDimension = CREATED;
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT).addDateFilter().getFilterBuckets()
                .changeDateDimension("Closed", newDateDimension);

        assertTrue(analysisPage.waitForReportComputing().getFilterBuckets().getDateFilterText()
                .startsWith(newDateDimension), "Date dimension was not changed to " + newDateDimension);

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();
        takeScreenshot(browser, "keep-date-dimension-after-applying-seetrend-recommendation", getClass());
        assertTrue(analysisPage.getFilterBuckets().getDateFilterText().startsWith(newDateDimension),
                "Date dimension was changed after user applied see trend recommendation");
    }
}
