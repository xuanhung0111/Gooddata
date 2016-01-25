package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class DropZonesTest extends AbstractAdE2ETest {

    private static final By ACTIVE_CATEGORIES_SELECTOR = cssSelector(".s-bucket-categories .adi-droppable-active .adi-bucket-invitation");
    private static final By ACTIVE_METRICS_SELECTOR = cssSelector(".s-bucket-metrics .adi-droppable-active .adi-bucket-invitation");
    private static final By ACTIVE_REPLACABLE_SELECTOR = cssSelector(".adi-replace-invitation.adi-droppable-active");

    private static final Point NON_DROPPABLE_POSITION = new Point(-1, -1);

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Drag-Recommendations-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_highlight_attribute_dropzones() {
        initAnalysePageByUrl();

        assertFalse(isElementPresent(ACTIVE_CATEGORIES_SELECTOR, browser));

        analysisPage.startDrag(analysisPage.getCataloguePanel().getDate());

        try {
            assertTrue(isElementPresent(ACTIVE_CATEGORIES_SELECTOR, browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }

        assertFalse(isElementPresent(ACTIVE_CATEGORIES_SELECTOR, browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_highlight_metric_dropzones() {
        initAnalysePageByUrl();

        assertFalse(isElementPresent(ACTIVE_METRICS_SELECTOR, browser));

        analysisPage.startDrag(analysisPage.getCataloguePanel().searchAndGet(NUMBER_OF_ACTIVITIES, FieldType.METRIC));

        try {
            assertTrue(isElementPresent(ACTIVE_METRICS_SELECTOR, browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }

        assertFalse(isElementPresent(ACTIVE_METRICS_SELECTOR, browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_drag_attribute_from_filters_to_shortcut() {
        initAnalysePageByUrl();

        WebElement filter = analysisPage.addFilter(ACTIVITY_TYPE)
            .getFilterBuckets()
            .getFilter(ACTIVITY_TYPE);

        analysisPage.startDrag(filter);

        try {
            assertTrue(isElementPresent(cssSelector(".s-blank-canvas-message"), browser));
            assertFalse(isElementPresent(cssSelector(".s-recommendation-attribute-canvas"), browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_titles_for_fact_based_metric_dropzones() {
        initAnalysePageByUrl();

        analysisPage.startDrag(analysisPage.getCataloguePanel().searchAndGet(AMOUNT, FieldType.FACT));

        try {
            assertTrue(isElementPresent(cssSelector(".s-recommendation-metric-canvas"), browser));
            assertThat(waitForElementVisible(cssSelector(".s-recommendation-metric-canvas"), browser)
                    .getText(), containsString("Sum of " + AMOUNT));

            assertTrue(isElementPresent(cssSelector(".s-recommendation-metric-over-time-canvas"), browser));
            assertThat(waitForElementVisible(cssSelector(".s-recommendation-metric-over-time-canvas"), browser)
                    .getText(), containsString("Sum of " + AMOUNT));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void should_highlight_possible_attribute_replacement() {
        initAnalysePageByUrl();

        analysisPage.addAttribute(ACTIVITY_TYPE);

        assertFalse(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));

        analysisPage.startDrag(analysisPage.getCataloguePanel().searchAndGet(ACCOUNT, FieldType.ATTRIBUTE));

        try {
            assertTrue(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }

        assertFalse(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_highlight_possible_metric_replacement() {
        initAnalysePageByUrl();

        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);

        assertFalse(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));

        analysisPage.startDrag(analysisPage.getCataloguePanel().searchAndGet(NUMBER_OF_LOST_OPPS, FieldType.METRIC));

        try {
            assertTrue(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }

        assertFalse(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_highlight_attribute_when_already_present_in_the_bucket() {
        initAnalysePageByUrl();

        WebElement source = analysisPage.addAttribute(ACTIVITY_TYPE)
            .getAttributesBucket()
            .getFirst();

        analysisPage.startDrag(source);

        try {
            assertFalse(isElementPresent(ACTIVE_REPLACABLE_SELECTOR, browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_highlight_date_when_already_present_in_the_bucket() {
        initAnalysePageByUrl();

        WebElement date = analysisPage.addDate()
            .getAttributesBucket()
            .getFirst();
        analysisPage.startDrag(date);

        try {
            assertFalse(isElementPresent(cssSelector(".s-bucket-categories .adi-droppable-active"), browser));
        } finally {
            analysisPage.stopDrag(NON_DROPPABLE_POSITION);
        }
    }
}
