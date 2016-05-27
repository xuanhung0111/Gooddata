package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class GoodSalesDropAttributeTest extends GoodSalesAbstractAnalyseTest {

    private static final String PRIORITY = "Priority";
    private static final String REGION = "Region";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Drop-Attribute-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void dropAttributeToReportHaveOneMetric() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 4);

        analysisPageReact.addStack(ATTR_DEPARTMENT);
        analysisPageReact.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = {"init"})
    public void dropThirdAttributeToBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPageReact.replaceAttribute(ATTR_ACTIVITY_TYPE, PRIORITY);
        Collection<String> addedAttributes = analysisPageReact.getAttributesBucket().getItemNames();
        assertTrue(addedAttributes.contains(PRIORITY));
        assertFalse(addedAttributes.contains(ATTR_ACTIVITY_TYPE));

        analysisPageReact.replaceStack(REGION);
        assertEquals(analysisPageReact.getStacksBucket().getAttributeName(), REGION);
        checkingOpenAsReport("dropThirdAttributeToBucket");
    }

    @Test(dependsOnGroups = {"init"})
    public void removeAttributeOnXBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPageReact.removeAttribute(ATTR_ACTIVITY_TYPE);
        Collection<String> addedAttributes = analysisPageReact.getAttributesBucket().getItemNames();
        assertFalse(addedAttributes.contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void recommendNextStep() {
        dropAttributeToReportHaveOneMetric();

        MetricConfiguration metricConfiguration = analysisPageReact.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPageReact.resetToBlankState();
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        metricConfiguration.expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);

        analysisPageReact.addStack(ATTR_DEPARTMENT);
        analysisPageReact.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);
        checkingOpenAsReport("recommendNextStep");
    }

    @Test(dependsOnGroups = {"init"})
    public void applyAttributeFiltersInReport() {
        dropAttributeToReportHaveOneMetric();
        analysisPageReact.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call")
            .configAttributeFilter(ATTR_DEPARTMENT, "Inside Sales");
        ChartReportReact report = analysisPageReact.getChartReport();
        assertEquals(report.getTrackersCount(), 2);
        checkingOpenAsReport("applyAttributeFiltersInReport");
    }

    @Test(dependsOnGroups = {"init"})
    public void reportVisualization() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPageReact.getStacksBucket();

        analysisPageReact.changeReportType(ReportType.BAR_CHART);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPageReact.changeReportType(ReportType.LINE_CHART);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPageReact.changeReportType(ReportType.TABLE);
        assertFalse(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void testUndoRedo() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPageReact.getStacksBucket();

        analysisPageReact.undo();
        assertTrue(stacksBucket.isEmpty());
        analysisPageReact.redo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPageReact.replaceStack(REGION);
        assertEquals(stacksBucket.getAttributeName(), REGION);

        analysisPageReact.undo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPageReact.undo();
        assertTrue(stacksBucket.isEmpty());

        analysisPageReact.redo().redo();
        assertEquals(stacksBucket.getAttributeName(), REGION);
    }
}
