package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class MetricModifiersTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Metric-Modifiers-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_turned_off_when_second_metric_is_added() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        click(".s-recommendation-metric-with-period .s-apply-recommendation");

        dragFromCatalogue(quotaMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        expectFind(METRICS_BUCKET + " " + activitiesMetric +" .s-show-pop:not(:checked)");
    }
}
