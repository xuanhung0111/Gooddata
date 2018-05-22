package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class TrashTest extends AbstractAdE2ETest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Trash-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_clear_all_items_by_dragging_them_to_the_trash() {
        assertTrue(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .removeMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .isEmpty());

        assertTrue(analysisPage.removeAttribute(ATTR_ACTIVITY_TYPE)
            .getAttributesBucket()
            .isEmpty());

        assertFalse(analysisPage.getPageHeader().isResetButtonEnabled());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_not_be_possible_to_trash_item_by_throwing_it_anyplace_other_than_trash() {
        assertTrue(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .waitForReportComputing()
            .getPageHeader()
            .isResetButtonEnabled());

        assertEquals(analysisPage.drag(analysisPage.getMetricsBucket().get(METRIC_NUMBER_OF_ACTIVITIES),
                analysisPage.getPageHeader().getResetButton())
            .getMetricsBucket()
            .getItemNames(), asList(METRIC_NUMBER_OF_ACTIVITIES));
        assertTrue(analysisPage.getPageHeader().isResetButtonEnabled());
    }
}
