package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class MetricFiltersTest extends AbstractGoodSalesE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Metric-Filters-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_filter_metric_by_attribute() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        //  Create attribute filter
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        createAttributeFilter(".s-activity_type");

        selectFirstElementFromAttributeFilter();

        assertThat(waitForElementVisible(cssSelector(METRICS_BUCKET + " .s-bucket-item-header label"), browser)
                .getText(), containsString("Activity Type: Email"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_filter_metric_by_unavailable_attribute() {
        visitEditor();

        dragFromCatalogue(lostOppsMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + lostOppsMetric);

        click(".s-btn-add_attribute_filter");

        expectFind(".adi-filter-picker .s-department");
        expectMissing(".adi-filter-picker .s-activity_type");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_remove_filter() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        //  Create attribute filter
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        createAttributeFilter(".s-activity_type");

        selectFirstElementFromAttributeFilter();

        // Delete it
       click(".s-bucket-item .s-remove-attribute-filter");
       expectMissing(".s-filter-button");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_show_tooltip() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        click(".s-btn-add_attribute_filter");

        hover(".s-bubble-id-attr_activity_activitytype");

        assertThat(waitForElementVisible(cssSelector(".s-catalogue-bubble h3"), browser).getText(),
                containsString("Activity Type"));
        assertThat(waitForElementVisible(cssSelector(".s-catalogue-bubble .adi-item-type"), browser).getText(),
                containsString("Attribute"));
        expectFind(".s-catalogue-bubble .s-attribute-element");
        assertThat(browser.findElements(cssSelector(".s-catalogue-bubble .s-attribute-element"))
            .stream()
            .map(WebElement::getText)
            .collect(toList()), contains("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_restore_filter_creation() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);

        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        createAttributeFilter(".s-activity_type");

        undo();

        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        expectMissing(".s-filter-button");

        redo();

        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);

        // Check filter existence
        assertThat(waitForElementVisible(cssSelector(".s-filter-button"), browser).getText(),
                containsString("Activity Type: All"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_restore_attribute_elements_settings() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        createAttributeFilter(".s-activity_type");
        selectFirstElementFromAttributeFilter();

        undo();
        redo();

        toggleBucketItemConfig(METRICS_BUCKET + " .s-bucket-item");

        click(".s-filter-button");

        // Check the attribute filter dropdown status
        // is revived correctly. i.e check 2nd attribute element
        // is not selected.
        expectFind(".s-filter-item[title='In Person Meeting']:not(.is-selected)");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_total_count_in_attribute_filter_label_correctly() {
        visitEditor();

        String labelCount = ".s-attribute-filter-label .s-total-count";

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        toggleBucketItemConfig(METRICS_BUCKET + " " + activitiesMetric);
        createAttributeFilter(".s-activity_type");
        expectMissing(labelCount);

        selectFirstElementFromAttributeFilter();
        expectFind(labelCount);

        click(".s-filter-button");
        selectAllElementsFromAttributeFilter();
        expectMissing(labelCount);
    }
}
