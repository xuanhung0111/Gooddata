package com.gooddata.qa.graphene.indigo.analyze.e2e;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

public class AttributeBasedMetricsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Attribute-Based-Metrics-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_attribute_to_the_metrics_bucket() {
        analysisPageReact.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .waitForReportComputing();

        assertFalse(analysisPageReact.getMetricsBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_remove_created_metric() {
        assertTrue(analysisPageReact.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .removeMetric("Count of " + ACTIVITY_TYPE)
            .getMetricsBucket()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drop_same_attribute_multiple_time_to_metrics() {
        assertEquals(analysisPageReact.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .getMetricsBucket()
            .getItemNames()
            .size(), 3);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_create_and_visualize_attribute_based_metrics_with_correct_titles() {
        assertEquals(analysisPageReact.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList("Count of " + ACTIVITY_TYPE, "Count of " + ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_combine_attribute_and_fact_based_metrics() {
        assertEquals(analysisPageReact.addMetric(ACTIVITY_TYPE, FieldType.ATTRIBUTE)
                .addMetric(AMOUNT, FieldType.FACT)
                .waitForReportComputing()
                .getChartReport()
                .getLegends(), asList("Count of " + ACTIVITY_TYPE, "Sum of " + AMOUNT));
    }
}
