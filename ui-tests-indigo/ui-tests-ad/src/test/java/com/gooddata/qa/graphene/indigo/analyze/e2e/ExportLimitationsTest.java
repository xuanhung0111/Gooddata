package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ExportLimitationsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Export-Limitations-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_export_button_if_stacking_and_slicing_by_the_same_attribute() {
        assertFalse(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            // Drag exactly the same attribute to the category and stack
            .addAttribute(ACTIVITY_TYPE)
            .addStack(ACTIVITY_TYPE)
            .waitForReportComputing()
            .getPageHeader()
            .isExportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disable_export_if_the_same_metric_is_in_metric_bucket_twice() {
        assertFalse(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getPageHeader()
            .isExportButtonEnabled());
    }
}
