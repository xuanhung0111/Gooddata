package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_CLOSED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import static org.openqa.selenium.By.cssSelector;

public class GoodSalesDropAttributeTest extends AbstractAnalyseTest {

    private static final String PRIORITY = "Priority";
    private static final String REGION = "Region";
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Drop-Attribute-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dropAttributeToReportHaveOneMetric() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing();
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 4);

        analysisPage.addStack(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dropFourthAttributeToBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.addAttribute(ATTR_IS_CLOSED).replaceAttribute(ATTR_ACTIVITY_TYPE, PRIORITY);
        Collection<String> addedAttributes = analysisPage.getAttributesBucket().getItemNames();
        assertThat(addedAttributes, hasItem(PRIORITY));
        assertThat(addedAttributes, not(hasItem(ATTR_ACTIVITY_TYPE)));

        analysisPage.replaceStack(REGION);
        assertEquals(analysisPage.getStacksBucket().getAttributeName(), REGION);
        checkingOpenAsReport("dropThirdAttributeToBucket");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void removeAttributeOnXBucket() {
        dropAttributeToReportHaveOneMetric();

        analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE);
        Collection<String> addedAttributes = analysisPage.getAttributesBucket().getItemNames();
        assertThat(addedAttributes, not(hasItem(ATTR_ACTIVITY_TYPE)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void recommendNextStep() {
        dropAttributeToReportHaveOneMetric();

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent shouldn't be enabled");
        assertEquals(browser.findElements(RecommendationContainer.LOCATOR).size(), 0);

        analysisPage.resetToBlankState();
        analysisPage.changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing();
        metricConfiguration.expandConfiguration();
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent shouldn't be enabled");
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0, "Recommendation should display");

        analysisPage.addStack(ATTR_DEPARTMENT);
        analysisPage.waitForReportComputing();
        assertFalse(metricConfiguration.isShowPercentEnabled(), "Show percent shouldn't be enabled");
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() > 0, "Recommendation should display");
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
        assertFalse(isElementPresent(cssSelector(StacksBucket.CSS_SELECTOR), browser),
                "Stack bucket shouldn't be present");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testUndoRedo() {
        dropAttributeToReportHaveOneMetric();
        final StacksBucket stacksBucket = analysisPage.getStacksBucket();

        analysisPage.undo();
        assertTrue(stacksBucket.isEmpty(), "Stacks bucket should be empty");
        analysisPage.redo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.replaceStack(REGION);
        assertEquals(stacksBucket.getAttributeName(), REGION);

        analysisPage.undo();
        assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);

        analysisPage.undo();
        assertTrue(stacksBucket.isEmpty(), "Stacks bucket should be empty");

        analysisPage.redo().redo();
        assertEquals(stacksBucket.getAttributeName(), REGION);
    }
}
