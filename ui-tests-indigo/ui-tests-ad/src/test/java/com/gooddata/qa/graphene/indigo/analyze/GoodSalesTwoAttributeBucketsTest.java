package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesTwoAttributeBucketsTest extends AnalyticalDesignerAbstractTest {

    private static final String BEST_CASE = "Best Case";
    private static final String PRIORITY = "Priority";
    private static final String REGION = "Region";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Two-Attribute-Buckets-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void dropAttributeToReportHaveOneMetric() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE).waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);

        analysisPage.addStack(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = {"init"})
    public void dropThirdAttributeToBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.replaceAttribute(ACTIVITY_TYPE, PRIORITY);
        Collection<String> addedAttributes = analysisPage.getAttributesBucket().getItemNames();
        assertTrue(addedAttributes.contains(PRIORITY));
        assertFalse(addedAttributes.contains(ACTIVITY_TYPE));

        analysisPage.replaceStack(REGION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), REGION);
        checkingOpenAsReport("dropThirdAttributeToBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void disablePopCheckboxOnDroppingNonDateAttribute() {
        initAnalysePage();
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(metricConfiguration.isShowPercentEnabled());

        analysisPage.addStack(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        checkingOpenAsReport("disablePopCheckboxOnDroppingNonDateAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void addStackByIfMoreThanOneMetricInReport() {
        initAnalysePage();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addMetric(BEST_CASE).addAttribute(REGION);

        final StacksBucket stacksBucket = analysisPage.getStacksBucket();
        assertTrue(stacksBucket.isDisabled());
        assertEquals(stacksBucket.getWarningMessage(), "TO STACK BY, A VISUALIZATION CAN HAVE ONLY ONE MEASURE");
    }

    @Test(dependsOnGroups = {"init"})
    public void addSecondMetricIfAttributeInStackBy() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE).addStack(DEPARTMENT);
        assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), "TO ADD ADDITIONAL MEASURE, REMOVE FROM STACK BY");
    }

    @Test(dependsOnGroups = {"init"})
    public void removeAttributeOnXBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.removeAttribute(ACTIVITY_TYPE);
        Collection<String> addedAttributes = analysisPage.getAttributesBucket().getItemNames();
        assertFalse(addedAttributes.contains(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void recommendNextStep() {
        dropAttributeToReportHaveOneMetric();

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.resetToBlankState();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        metricConfiguration.expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);

        analysisPage.addStack(DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);
        checkingOpenAsReport("recommendNextStep");
    }

    @Test(dependsOnGroups = {"init"})
    public void attributesInFilterMenu() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addAttribute(ACTIVITY_TYPE);
        assertTrue(filtersBucket.isFilterVisible(ACTIVITY_TYPE));

        analysisPage.addStack(DEPARTMENT);
        assertTrue(filtersBucket.isFilterVisible(DEPARTMENT));

        analysisPage.replaceAttribute(ACTIVITY_TYPE, REGION);
        assertFalse(filtersBucket.isFilterVisible(ACTIVITY_TYPE));
        assertTrue(filtersBucket.isFilterVisible(REGION));
        checkingOpenAsReport("attributesInFilterMenu");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyAttributeFiltersInReport() {
        dropAttributeToReportHaveOneMetric();
        analysisPage.getFilterBuckets().configAttributeFilter(ACTIVITY_TYPE, "Email", "Phone Call")
            .configAttributeFilter(DEPARTMENT, "Inside Sales");
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 2);
        checkingOpenAsReport("applyAttributeFiltersInReport");
    }

    @Test(dependsOnGroups = {"init"})
    public void reportVisualization() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertFalse(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeReplaceDate() {
        initAnalysePage();
        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDate();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(filtersBucket.isDateFilterVisible());

        analysisPage.replaceAttribute(DATE, ACTIVITY_TYPE);
        assertFalse(filtersBucket.isDateFilterVisible());
        assertTrue(filtersBucket.isFilterVisible(ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void applyFilterInReportHasDateAndAttribute() {
        initAnalysePage();
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES).addDate().addStack(ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.getFilterBuckets().configAttributeFilter(ACTIVITY_TYPE, "Email", "Phone Call")
            .configDateFilter("Last year");
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("applyFilterInReportHasDateAndAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void uncheckSelectedPopCheckbox() {
        initAnalysePage();
        MetricConfiguration metricConfiguration = analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addDate()
            .getMetricsBucket()
            .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
            .expandConfiguration();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertTrue(metricConfiguration.isPopEnabled());

        metricConfiguration.showPop();
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);

        analysisPage.replaceAttribute(DATE, ACTIVITY_TYPE);
        assertTrue(analysisPage.waitForReportComputing().getChartReport().getTrackersCount() >= 1);
        assertFalse(metricConfiguration.isPopEnabled());
        assertFalse(metricConfiguration.isPopSelected());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), Arrays.asList(ACTIVITY_TYPE));
        checkingOpenAsReport("uncheckSelectedPopCheckbox");
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedo() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.undo();
        assertTrue(stacksBucket.isEmpty());
        analysisPage.redo();
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);

        analysisPage.replaceStack(REGION);
        assertEquals(stacksBucket.getAttributeName(), REGION);

        analysisPage.undo();
        assertEquals(stacksBucket.getAttributeName(), DEPARTMENT);

        analysisPage.undo();
        assertTrue(stacksBucket.isEmpty());

        analysisPage.redo().redo();
        assertEquals(stacksBucket.getAttributeName(), REGION);
    }
}
