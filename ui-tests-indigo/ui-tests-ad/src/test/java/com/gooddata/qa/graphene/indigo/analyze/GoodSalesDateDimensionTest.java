package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;

import java.util.Arrays;

public class GoodSalesDateDimensionTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Date-Dimension-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyOnFilter() {
        final FiltersBucket filtersBucketReact = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .getFilterBuckets();

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_SNAPSHOT_BOP)
                .addDateFilter()
                .waitForReportComputing();
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(DATE_DATASET_ACTIVITY)),
                Arrays.asList(DATE_DATASET_ACTIVITY, DateRange.ALL_TIME.toString()));
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 2);

        WebElement filter = filtersBucketReact.getFilter(DATE_DATASET_ACTIVITY);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertEquals(panel.getDimensionSwitchs(), asList(DATE_DATASET_ACTIVITY, DATE_DATASET_CREATED));

        panel.changePeriodWithScrollbar(DateRange.THIS_YEAR.toString()).apply();
        analysisPage.waitForReportComputing();
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(DATE_DATASET_ACTIVITY)),
                Arrays.asList(DATE_DATASET_ACTIVITY, DateRange.THIS_YEAR.toString()));
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1, "Tracker should display");
        checkingOpenAsReport("applyOnFilter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyOnBucket() {
        final FiltersBucket filtersBucketReact = initAnalysePage().getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate();
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(DATE_DATASET_ACTIVITY)),
                Arrays.asList(DATE_DATASET_ACTIVITY, DateRange.ALL_TIME.toString()));

        analysisPage.getAttributesBucket().changeGranularity(DateGranularity.MONTH);
        analysisPage.waitForReportComputing();

        filtersBucketReact.configDateFilter("Last 90 days");
        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1, "Tracker should display");
        checkingOpenAsReport("applyOnBucket");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyOnBothFilterAndBucket() {
        final FiltersBucket filtersBucketReact = initAnalysePage().getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDateFilter();
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(DATE_DATASET_ACTIVITY)),
                Arrays.asList(DATE_DATASET_ACTIVITY, DateRange.ALL_TIME.toString()));

        filtersBucketReact.changeDateDimension(DATE_DATASET_ACTIVITY, DATE_DATASET_CREATED);
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(DATE_DATASET_CREATED)),
                Arrays.asList(DATE_DATASET_CREATED, DateRange.ALL_TIME.toString()));

        analysisPage.addDate();
        WebElement filter = filtersBucketReact.getFilter(DATE_DATASET_CREATED);
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertFalse(panel.isDimensionSwitcherEnabled(), "Dimension switcher shouldn't be enabled");

        analysisPage.getAttributesBucket().changeDateDimension(DATE_DATASET_ACTIVITY);
        assertEquals(parseFilterText(filtersBucketReact.getFilterText(DATE_DATASET_ACTIVITY)),
                Arrays.asList(DATE_DATASET_ACTIVITY, DateRange.ALL_TIME.toString()));
        checkingOpenAsReport("applyOnBothFilterAndBucket");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-9980: Date filter isn't remained when adding trending from recommendation panel, " +
                    "covered by TestCafe")
    public void keepDateDimensionAfterApplyingSeeTrendRecommendation() {
        final String newDateDimension = DATE_DATASET_CREATED;
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(FACT_AMOUNT, FieldType.FACT)
                .addDateFilter().getFilterBuckets().changeDateDimension(DATE_DATASET_CLOSED, newDateDimension);

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
