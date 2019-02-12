package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;

public class GoodSalesAttributeBasedMetricTest extends AbstractAnalyseTest {

    private static final String COUNT_OF_ACTIVITY = "Count of " + ATTR_ACTIVITY;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Based-Metric-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSimpleMetricFromAttribute() {
        final MetricsBucket metricsBucket = initAnalysePage().getMetricsBucket();

        assertTrue(analysisPage.addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0, "Tracker should display");

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "Recommendation should be visible");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Recommendation should be visible");

        analysisPage.undo();
        assertTrue(metricsBucket.isEmpty(), "Metric bucket should be empty");

        analysisPage.redo();
        assertFalse(metricsBucket.isEmpty(), "Metric bucket is empty");

        assertEquals(analysisPage.addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getChartReport()
            .getYaxisTitle(), COUNT_OF_ACTIVITY);

        checkingOpenAsReport("createSimpleMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showInPercent() {
        assertTrue(initAnalysePage().addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0, "Tracker should display");

        analysisPage.getMetricsBucket()
            .getMetricConfiguration(COUNT_OF_ACTIVITY)
            .expandConfiguration()
            .showPercents();

        assertTrue(analysisPage.waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0, "Tracker should display");

        checkingOpenAsReport("showInPercent");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dragSameAttributeBasedMetrics() {
        assertTrue(initAnalysePage().addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0,"Tracker should display");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void shouldNotCreateDuplicateMetricFromAttribute() {
        final String identifier = Stream.of(initAnalysePage().addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getMetricsBucket()
            .get(COUNT_OF_ACTIVITY)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];

        assertThat(analysisPage.removeMetric(COUNT_OF_ACTIVITY)
            .waitForReportComputing()
            .addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getMetricsBucket()
            .get(COUNT_OF_ACTIVITY)
            .getAttribute("class"), containsString("s-id-" + identifier));
    }
}
