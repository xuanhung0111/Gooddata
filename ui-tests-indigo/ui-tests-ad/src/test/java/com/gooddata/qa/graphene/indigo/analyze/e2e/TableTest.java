package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.sort;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;
import com.gooddata.qa.utils.http.RestUtils;

public class TableTest extends AbstractGoodSalesE2ETest {

    private String emptyMetric;
    private String emptyMetricUri;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Table-E2E-Test";
    }

    @Test(dependsOnGroups = {"turnOffWalkme"}, groups = {"init"})
    public void createEmptyMetric() {
        Metric metric;

        if (testParams.isReuseProject()) {
            metric = getMdService().getObj(getProject(), Metric.class, title("__EMPTY__"));
        } else {
            String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
            metric = createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0");
        }

        emptyMetricUri = metric.getUri();
        emptyMetric = ".s-id-" + metric.getIdentifier().toLowerCase();
    }

    @Test(dependsOnGroups = {"init"})
    public void it_should_be_blank_by_default() throws ParseException, JSONException, IOException {
        RestUtils.changeMetricFormat(getRestApiClient(), emptyMetricUri, "#,##0");

        visitEditor();

        dragFromCatalogue(emptyMetric, METRICS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        switchVisualization("table");
        assertTrue(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void it_should_be_empty_if_formatted() throws ParseException, JSONException, IOException {
        RestUtils.changeMetricFormat(getRestApiClient(), emptyMetricUri, "[=null] empty");

        visitEditor();

        dragFromCatalogue(emptyMetric, METRICS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        switchVisualization("table");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "empty");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_zeros_as_usual() throws ParseException, JSONException, IOException {
        RestUtils.changeMetricFormat(getRestApiClient(), emptyMetricUri, "[=null] 0.00 $");

        visitEditor();

        dragFromCatalogue(emptyMetric, METRICS_BUCKET);
        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        switchVisualization("table");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "0.00 $");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_table_correctly_when_filter_is_removed() {
        visitEditor();

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, FILTERS_BUCKET);
        click(".s-filter-button");
        selectFirstElementFromAttributeFilter();
        switchVisualization("table");
        click(".s-filter-button");
        selectAllElementsFromAttributeFilter();
        assertFalse(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_ordered_by_first_column_in_asc_by_default() {
        beforeOrderingTable();

        expectFind(".adi-component " + activityTypeAttrLabel + " .gd-table-arrow-up");

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_by_attribute() {
        beforeOrderingTable();

        orderBy(activityTypeAttrLabel);

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Web Meeting");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "Phone Call");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_by_metric() {
        beforeOrderingTable();

        orderBy(activitiesMetric);

        List<Integer> values = newArrayList();
        IntStream.rangeClosed(0, 3).forEach(i -> values.add(unformatNumber(
                waitForElementVisible(cssSelector(".s-cell-" + i + "-1"), browser).getText().trim())));

        List<Integer> sortedValues = newArrayList(values);
        sort(sortedValues, (a, b) -> b- a);

        assertEquals(values, sortedValues);
    }

    @Test(dependsOnGroups = {"init"})
    public void clean_sorting_if_column_removed() {
        beforeOrderingTable();

        orderBy(activitiesMetric);
        drag(METRICS_BUCKET + " " + activitiesMetric, TRASH);
        expectFind(".adi-component " + activityTypeAttrLabel + " .gd-table-arrow-up");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_shift_keep_sorting_on_metric_if_attribute_added() {
        beforeOrderingTable();

        orderBy(activitiesMetric);
        dragFromCatalogue(departmentAttr, CATEGORIES_BUCKET);
        expectFind(".adi-component " + activitiesMetric +" .gd-table-arrow-down");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_in_asc_order_if_same_column_clicked_twice() {
        beforeOrderingTable();

        orderBy(activityTypeAttrLabel);
        orderBy(activityTypeAttrLabel);

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_only_by_one_column_at_the_time() {
        beforeOrderingTable();

        orderBy(activitiesMetric);
        orderBy(activityTypeAttrLabel);

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_work_with_undo_redo() {
        beforeOrderingTable();

        orderBy(activityTypeAttrLabel);
        orderBy(activityTypeAttrLabel);

        undo();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Web Meeting");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "Phone Call");

        redo();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_more_than_one_attribute_to_category() {
        visitEditor();

        switchVisualization("table");

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(activityAttr, CATEGORIES_BUCKET);

        expectElementCount(CATEGORIES_BUCKET + " .s-bucket-item", 3);
    }

    @Test(dependsOnGroups = {"init"}, expectedExceptions = {MoveTargetOutOfBoundsException.class})
    public void should_not_be_possible_to_drag_more_than_one_attribute_to_bar__view_by() {
        visitEditor();

        switchVisualization("bar");

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, CATEGORIES_BUCKET);

        expectElementCount(CATEGORIES_BUCKET + " .s-bucket-item", 1);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_move_stacks_to_categories_when_switching_to_table() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);

        switchVisualization("table");
        expectElementCount(CATEGORIES_BUCKET + " .s-bucket-item", 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_move_second_category_to_stacks_and_remove_to_rest_when_switching_to_chart() {
        visitEditor();

        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
        dragFromCatalogue(accountAttr, STACKS_BUCKET);

        switchVisualization("table");
        switchVisualization("bar");

        expectElementCount(CATEGORIES_BUCKET + " .s-bucket-item", 1);
        expectElementCount(STACKS_BUCKET + " .s-bucket-item", 1);
    }

    private void beforeOrderingTable() {
        visitEditor();
        switchVisualization("table");

        dragFromCatalogue(activitiesMetric, METRICS_BUCKET);
        dragFromCatalogue(activityTypeAttr, CATEGORIES_BUCKET);
    }

    private void orderBy(String attr) {
        click(".adi-component " + attr + " a");
    }

    private Integer unformatNumber(String number) {
        return new Integer(number.replace(",", ""));
    }
}
