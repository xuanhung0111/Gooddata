package com.gooddata.qa.graphene.indigo.analyze;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesTwoAttributeBucketsTest extends AnalyticalDesignerAbstractTest {

    private static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    private static final String BEST_CASE = "Best Case";
    private static final String ACTIVITY_TYPE = "Activity Type";
    private static final String DEPARTMENT = "Department";
    private static final String PRIORITY = "Priority";
    private static final String REGION = "Region";

    @BeforeClass
    public void initialize() {
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle = "Indigo-GoodSales-Demo-Two-Attribute-Buckets-Test";
    }

    @Test(dependsOnGroups = {"init"}, groups = {"sanity"})
    public void dropAttributeToReportHaveOneMetric() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE));
        analysisPage.waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);

        analysisPage.addStackBy(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = {"init"})
    public void dropThirdAttributeToBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.replaceCategory(PRIORITY);
        Collection<String> addedAttributes = analysisPage.getAllAddedCategoryNames();
        assertTrue(addedAttributes.contains(PRIORITY));
        assertFalse(addedAttributes.contains(ACTIVITY_TYPE));

        analysisPage.replaceStackBy(REGION);
        assertEquals(analysisPage.getAddedStackByName(), REGION);
    }

    @Test(dependsOnGroups = {"init"})
    public void disablePopCheckboxOnDroppingNonDateAttribute() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE));
        analysisPage.waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(analysisPage.isShowPercentConfigEnabled());

        analysisPage.addStackBy(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(analysisPage.isShowPercentConfigEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void addStackByIfMoreThanOneMetricInReport() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES, BEST_CASE)
                .withCategories(REGION));

        assertTrue(analysisPage.isStackByDisabled());
        assertEquals(analysisPage.getStackByMessage(), "TO STACK BY, A VISUALIZATION CAN HAVE ONLY ONE SERIES");
    }

    @Test(dependsOnGroups = {"init"})
    public void addSecondMetricIfAttributeInStackBy() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE)).addStackBy(DEPARTMENT);

        assertEquals(analysisPage.getMetricMessage(), "TO ADD ADDITIONAL SERIES, REMOVE FROM STACK BY");
    }

    @Test(dependsOnGroups = {"init"})
    public void removeAttributeOnXBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.removeCategory(ACTIVITY_TYPE);
        Collection<String> addedAttributes = analysisPage.getAllAddedCategoryNames();
        assertFalse(addedAttributes.contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void recommendNextStep() {
        dropAttributeToReportHaveOneMetric();
        assertFalse(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.resetToBlankState();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        assertFalse(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);

        analysisPage.addStackBy(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(analysisPage.isShowPercentConfigEnabled());
        assertFalse(analysisPage.isCompareSamePeriodConfigEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);
    }

    @Test(dependsOnGroups = {"init"})
    public void attributesInFilterMenu() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE));
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));

        analysisPage.addStackBy(DEPARTMENT);
        assertTrue(analysisPage.isFilterVisible(DEPARTMENT));

        analysisPage.replaceCategory(REGION);
        assertFalse(analysisPage.isFilterVisible(ACTIVITY_TYPE));
        assertTrue(analysisPage.isFilterVisible(REGION));
    }

    @Test(dependsOnGroups = {"init"})
    public void applyAttributeFiltersInReport() {
        dropAttributeToReportHaveOneMetric();
        analysisPage.configAttributeFilter(ACTIVITY_TYPE, "Email", "Phone Call");
        analysisPage.configAttributeFilter(DEPARTMENT, "Inside Sales");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void reportVisualization() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertTrue(analysisPage.isStackByBucketEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeReplaceDate() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(analysisPage.isDateFilterVisible());

        analysisPage.addCategory(ACTIVITY_TYPE);
        assertFalse(analysisPage.isDateFilterVisible());
        assertTrue(analysisPage.isFilterVisible(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void applyFilterInReportHasDateAndAttribute() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE))
            .addStackBy(ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.configAttributeFilter(ACTIVITY_TYPE, "Email", "Phone Call");
        analysisPage.configTimeFilter("Last year");
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"init"})
    public void uncheckSelectedPopCheckbox() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES).withCategories(DATE));
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(analysisPage.isCompareSamePeriodConfigEnabled());

        analysisPage.compareToSamePeriodOfYearBefore();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.addCategory(ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertFalse(analysisPage.isCompareSamePeriodConfigEnabled());
        assertFalse(analysisPage.isCompareSamePeriodConfigSelected());
        assertEquals(analysisPage.getAllAddedCategoryNames(), Arrays.asList(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedo() {
        dropAttributeToReportHaveOneMetric();
        analysisPage.undo();
        assertTrue(analysisPage.isStackByBucketEmpty());
        analysisPage.redo();
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);

        analysisPage.addStackBy(REGION);
        assertEquals(analysisPage.getAddedStackByName(), REGION);

        analysisPage.undo();
        assertEquals(analysisPage.getAddedStackByName(), DEPARTMENT);

        analysisPage.undo();
        assertTrue(analysisPage.isStackByBucketEmpty());

        analysisPage.redo().redo();
        assertEquals(analysisPage.getAddedStackByName(), REGION);
    }
}
