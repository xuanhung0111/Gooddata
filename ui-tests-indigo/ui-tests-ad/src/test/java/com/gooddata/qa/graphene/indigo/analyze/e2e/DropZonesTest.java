package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class DropZonesTest extends AbstractGoodSalesE2ETest {

    private static final String ACTIVE_CATEGORIES_SELECTOR = CATEGORIES_BUCKET + " .adi-droppable-active .adi-bucket-invitation";
    private static final String ACTIVE_METRICS_SELECTOR = METRICS_BUCKET + " .adi-droppable-active .adi-bucket-invitation";

    private static final int[] NON_DROPPABLE_POSITION = {-1, -1};

    private String activeReplaceableMetric;
    private String activeReplacableAttribute;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Drag-Recommendations-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_highlight_attribute_dropzones() {
        visitEditor();

        expectMissing(ACTIVE_CATEGORIES_SELECTOR);

        startDrag(DATE);

        try {
            expectFind(ACTIVE_CATEGORIES_SELECTOR);
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }

        expectMissing(ACTIVE_CATEGORIES_SELECTOR);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_highlight_metric_dropzones() {
        visitEditor();

        expectMissing(ACTIVE_METRICS_SELECTOR);

        startDrag(activitiesMetric);

        try {
            expectFind(ACTIVE_METRICS_SELECTOR);
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }

        expectMissing(ACTIVE_METRICS_SELECTOR);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_drag_attribute_from_filters_to_shortcut() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, FILTERS_BUCKET);
        startDrag(FILTERS_BUCKET + " " + activityTypeAttrLabel);

        try {
            expectFind(".s-blank-canvas-message");
            expectMissing(".s-recommendation-attribute-canvas");
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_titles_for_fact_based_metric_dropzones() {
        visitEditor();

        startDrag(amountFact);

        try {
            expectFind(".s-recommendation-metric-canvas");
            assertThat(waitForElementVisible(cssSelector(".s-recommendation-metric-canvas"), browser)
                    .getText(), containsString("Sum of Amount"));

            expectFind(".s-recommendation-metric-over-time-canvas");
            assertThat(waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser)
                    .getText(), containsString("Sum of Amount"));
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void createActiveReplaceElements() {
        activeReplaceableMetric = activitiesMetric + ".adi-droppable-active";
        activeReplacableAttribute = activityTypeAttrLabel + ".adi-droppable-active";
    }

    @Test(dependsOnMethods = {"createActiveReplaceElements"})
    public void should_highlight_possible_attribute_replacement() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        expectMissing(activeReplacableAttribute);

        startDrag(accountAttr);

        try {
            expectFind(activeReplacableAttribute);
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }

        expectMissing(activeReplacableAttribute);
    }

    @Test(dependsOnMethods = {"createActiveReplaceElements"})
    public void should_highlight_possible_metric_replacement() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        expectMissing(activeReplaceableMetric);

        startDrag(lostOppsMetric);

        try {
            expectFind(activeReplaceableMetric);
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }

        expectMissing(activeReplaceableMetric);
    }

    @Test(dependsOnMethods = {"createActiveReplaceElements"})
    public void should_not_highlight_attribute_when_already_present_in_the_bucket() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);

        startDrag(activityTypeAttr);

        try {
            expectMissing(activeReplacableAttribute);
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_highlight_date_when_already_present_in_the_bucket() {
        visitEditor();

        dragFromCatalogue(DATE, CATEGORIES_BUCKET);
        startDrag(DATE);

        try {
            expectMissing(CATEGORIES_BUCKET + " .adi-droppable-active");
        } finally {
            stopDrag(NON_DROPPABLE_POSITION);
        }
    }
}
