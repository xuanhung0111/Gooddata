package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class TrashTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Trash-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_clear_all_items_by_dragging_them_to_the_trash() {
        assertTrue(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .removeMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .isEmpty());

        assertTrue(analysisPageReact.removeAttribute(ACTIVITY_TYPE)
            .getAttributesBucket()
            .isEmpty());

        assertFalse(analysisPageReact.getPageHeader().isResetButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_trash_item_by_throwing_it_anyplace_other_than_trash() {
        assertTrue(analysisPageReact.addMetric(NUMBER_OF_ACTIVITIES)
            .addAttribute(ACTIVITY_TYPE)
            .waitForReportComputing()
            .getPageHeader()
            .isResetButtonEnabled());

        assertEquals(analysisPageReact.drag(analysisPageReact.getMetricsBucket().get(NUMBER_OF_ACTIVITIES),
                analysisPageReact.getPageHeader().getResetButton())
            .getMetricsBucket()
            .getItemNames(), asList(NUMBER_OF_ACTIVITIES));
        assertTrue(analysisPageReact.getPageHeader().isResetButtonEnabled());
    }
}
