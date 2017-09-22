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
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class GoodSalesDropAttributeTest extends AbstractAnalyseTest {

    private static final String PRIORITY = "Priority";
    private static final String REGION = "Region";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Drop-Attribute-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dropAttributeToReportHaveOneMetric() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);

        analysisPage.addStack(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dropThirdAttributeToBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, PRIORITY);
        Collection<String> addedAttributes = analysisPage.getAttributesBucket().getItemNames();
        assertTrue(addedAttributes.contains(PRIORITY));
        assertFalse(addedAttributes.contains(ATTR_ACTIVITY_TYPE));

        analysisPage.replaceStack(REGION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), REGION);
        checkingOpenAsReport("dropThirdAttributeToBucket");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeAttributeOnXBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE);
        Collection<String> addedAttributes = analysisPage.getAttributesBucket().getItemNames();
        assertFalse(addedAttributes.contains(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void recommendNextStep() {
        dropAttributeToReportHaveOneMetric();

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.resetToBlankState();
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        metricConfiguration.expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);

        analysisPage.addStack(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled());
        assertFalse(metricConfiguration.isPopEnabled());
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0);
        checkingOpenAsReport("recommendNextStep");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyAttributeFiltersInReport() {
        dropAttributeToReportHaveOneMetric();
        analysisPage.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email", "Phone Call")
            .configAttributeFilter(ATTR_DEPARTMENT, "Inside Sales");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 2);
        checkingOpenAsReport("applyAttributeFiltersInReport");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void reportVisualization() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.changeReportType(ReportType.TABLE);
        assertFalse(isElementPresent(className(StacksBucket.CSS_CLASS), browser));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testUndoRedo() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.undo();
        assertTrue(stacksBucket.isEmpty());
        analysisPage.redo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.replaceStack(REGION);
        assertEquals(stacksBucket.getAttributeName(), REGION);

        analysisPage.undo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.undo();
        assertTrue(stacksBucket.isEmpty());

        analysisPage.redo().redo();
        assertEquals(stacksBucket.getAttributeName(), REGION);
    }
}
