package com.gooddata.qa.graphene.indigo.analyze.e2e;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class DateGranularitySwitchTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Date-Granularity-Switch-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_change_date_granularity_appropriately() {
        beforeEach();

        select(".s-date-granularity-switch", "GDC.time.month");
        expectFind(".adi-components .visualization-column .s-property-x" + monthYearActivityLabel);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_use_default_date_granularity_after_switching_and_resetting() {
        beforeEach();

        // switch granularity to non-default value
        select(".s-date-granularity-switch", "GDC.time.month");
        resetReport();

        // expect the default granularity again
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        expectFind(".adi-components .visualization-column .s-property-x" + yearActivityLabel);
    }

    private void beforeEach() {
        visitEditor();

        // D&D the first metric/attribute to configuration
        switchVisualization("column");
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
    }
}
