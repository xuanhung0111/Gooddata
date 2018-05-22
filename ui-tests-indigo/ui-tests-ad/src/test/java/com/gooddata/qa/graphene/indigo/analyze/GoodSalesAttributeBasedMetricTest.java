package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

public class GoodSalesAttributeBasedMetricTest extends AbstractAnalyseTest {

    private static final String COUNT_OF_ACTIVITY = "Count of " + ATTR_ACTIVITY;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Attribute-Based-Metric-Test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createSimpleMetricFromAttribute() {
        final MetricsBucket metricsBucket = initAnalysePage().getMetricsBucket();

        assertTrue(analysisPage.addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.undo();
        assertTrue(metricsBucket.isEmpty());

        analysisPage.redo();
        assertFalse(metricsBucket.isEmpty());

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
                .getTrackersCount() > 0);

        analysisPage.getMetricsBucket()
            .getMetricConfiguration(COUNT_OF_ACTIVITY)
            .expandConfiguration()
            .showPercents();

        assertTrue(analysisPage.waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);

        checkingOpenAsReport("showInPercent");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dragSameAttributeBasedMetrics() {
        assertTrue(initAnalysePage().addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);
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

        assertTrue(analysisPage.removeMetric(COUNT_OF_ACTIVITY)
            .waitForReportComputing()
            .addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getMetricsBucket()
            .get(COUNT_OF_ACTIVITY)
            .getAttribute("class")
            .contains("s-id-" + identifier));
    }
}
