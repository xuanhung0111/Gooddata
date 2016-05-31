package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;

public class GoodSalesAttributeBasedMetricTest extends GoodSalesAbstractAnalyseTest {

    private static final String COUNT_OF_ACTIVITY = "Count of " + ATTR_ACTIVITY;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Attribute-Based-Metric-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createSimpleMetricFromAttribute() {
        final MetricsBucket metricsBucket = analysisPageReact.getMetricsBucket();

        assertTrue(analysisPageReact.addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPageReact.undo();
        assertTrue(metricsBucket.isEmpty());

        analysisPageReact.redo();
        assertFalse(metricsBucket.isEmpty());

        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getChartReport()
            .getYaxisTitle(), COUNT_OF_ACTIVITY);

        checkingOpenAsReport("createSimpleMetricFromAttribute");
    }

    @Test(dependsOnGroups = {"init"})
    public void showInPercent() {
        assertTrue(analysisPageReact.addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);

        analysisPageReact.getMetricsBucket()
            .getMetricConfiguration(COUNT_OF_ACTIVITY)
            .expandConfiguration()
            .showPercents();

        assertTrue(analysisPageReact.waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);

        checkingOpenAsReport("showInPercent");
    }

    @Test(dependsOnGroups = {"init"})
    public void dragSameAttributeBasedMetrics() {
        assertTrue(analysisPageReact.addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() > 0);
    }

    @Test(dependsOnGroups = {"init"})
    public void shouldNotCreateDuplicateMetricFromAttribute() {
        final String identifier = Stream.of(analysisPageReact.addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getMetricsBucket()
            .get(COUNT_OF_ACTIVITY)
            .getAttribute("class")
            .split(" "))
            .filter(e -> e.startsWith("s-id-"))
            .findFirst()
            .get()
            .split("-")[2];

        assertTrue(analysisPageReact.removeMetric(COUNT_OF_ACTIVITY)
            .waitForReportComputing()
            .addMetric(ATTR_ACTIVITY, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getMetricsBucket()
            .get(COUNT_OF_ACTIVITY)
            .getAttribute("class")
            .contains("s-id-" + identifier));
    }
}
