package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class AttributeBasedMetricsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Attribute-Based-Metrics-E2E-Test";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_drop_attribute_to_the_metrics_bucket() {
        analysisPage.addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .waitForReportComputing();

        assertFalse(analysisPage.getMetricsBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_remove_created_metric() {
        assertTrue(analysisPage.addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .removeMetric("Count of " + ATTR_ACTIVITY_TYPE)
            .getMetricsBucket()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_drop_same_attribute_multiple_time_to_metrics() {
        assertEquals(analysisPage.addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .getMetricsBucket()
            .getItemNames()
            .size(), 3);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_create_and_visualize_attribute_based_metrics_with_correct_titles() {
        assertEquals(analysisPage.addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList("Count of " + ATTR_ACTIVITY_TYPE, "Count of " + ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_combine_attribute_and_fact_based_metrics() {
        assertEquals(analysisPage.addMetric(ATTR_ACTIVITY_TYPE, FieldType.ATTRIBUTE)
                .addMetric(FACT_AMOUNT, FieldType.FACT)
                .waitForReportComputing()
                .getChartReport()
                .getLegends(), asList("Count of " + ATTR_ACTIVITY_TYPE, "Sum of " + FACT_AMOUNT));
    }
}
