package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;

public class TableTest extends AbstractAdE2ETest {

    private String emptyMetricUri;

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Table-E2E-Test";
    }

    @Override
    public void prepareSetupProject() {
        Metric metric;

        if (testParams.isReuseProject()) {
            metric = getMdService().getObj(getProject(), Metric.class, title("__EMPTY__"));
        } else {
            String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
            metric = createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0");
        }

        emptyMetricUri = metric.getUri();
    }

    @Test(dependsOnGroups = {"init"})
    public void it_should_be_blank_by_default() throws ParseException, JSONException, IOException {
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), emptyMetricUri, "#,##0");

        analysisPageReact.addMetric("__EMPTY__")
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertTrue(waitForElementPresent(cssSelector(".s-cell-0-0"), browser).getText().trim().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void it_should_be_empty_if_formatted() throws ParseException, JSONException, IOException {
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), emptyMetricUri, "[=null] empty");

        analysisPageReact.addMetric("__EMPTY__")
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "empty");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_zeros_as_usual() throws ParseException, JSONException, IOException {
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), emptyMetricUri, "[=null] 0.00 $");

        analysisPageReact.addMetric("__EMPTY__")
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.TABLE)
            .waitForReportComputing();
        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "0.00 $");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_table_correctly_when_filter_is_removed() {
        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addFilter(ATTR_ACTIVITY_TYPE)
            .getFilterBuckets()
            .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email");

        analysisPageReact.changeReportType(ReportType.TABLE)
            .getFilterBuckets()
            .configAttributeFilter(ATTR_ACTIVITY_TYPE, "All");
        analysisPageReact.waitForReportComputing();
        assertFalse(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_ordered_by_first_column_in_asc_by_default() {
        beforeOrderingTable();

        assertTrue(analysisPageReact.getTableReport().isHeaderSortedUp(ATTR_ACTIVITY_TYPE));

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_by_attribute() {
        beforeOrderingTable();

        analysisPageReact.getTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPageReact.waitForReportComputing();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Web Meeting");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "Phone Call");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_by_metric() {
        beforeOrderingTable();

        analysisPageReact.getTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPageReact.waitForReportComputing();

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

        analysisPageReact.getTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPageReact.removeMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
        assertTrue(analysisPageReact.getTableReport().isHeaderSortedUp(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_shift_keep_sorting_on_metric_if_attribute_added() {
        beforeOrderingTable();

        analysisPageReact.getTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPageReact.addAttribute(ATTR_DEPARTMENT)
            .waitForReportComputing();
        assertTrue(analysisPageReact.getTableReport().isHeaderSortedDown(METRIC_NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_in_asc_order_if_same_column_clicked_twice() {
        beforeOrderingTable();

        analysisPageReact.getTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPageReact.waitForReportComputing();
        analysisPageReact.getTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPageReact.waitForReportComputing();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_order_the_table_only_by_one_column_at_the_time() {
        beforeOrderingTable();

        analysisPageReact.getTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPageReact.waitForReportComputing();
        analysisPageReact.getTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPageReact.waitForReportComputing();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_work_with_undo_redo() {
        beforeOrderingTable();

        analysisPageReact.getTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPageReact.waitForReportComputing();
        analysisPageReact.getTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);

        analysisPageReact.waitForReportComputing().undo();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Web Meeting");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "Phone Call");

        analysisPageReact.redo();

        assertEquals(waitForElementVisible(cssSelector(".s-cell-0-0"), browser).getText().trim(), "Email");
        assertEquals(waitForElementVisible(cssSelector(".s-cell-1-0"), browser).getText().trim(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"init"})
    public void should_be_possible_to_drag_more_than_one_attribute_to_category() {
        assertEquals(analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_ACCOUNT)
            .addAttribute("Activity")
            .getAttributesBucket()
            .getItemNames()
            .size(), 3);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_not_be_possible_to_drag_more_than_one_attribute_to_bar__view_by() {
        assertEquals(analysisPageReact.changeReportType(ReportType.BAR_CHART)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .drag(analysisPageReact.getCataloguePanel().searchAndGet(ATTR_ACCOUNT, FieldType.ATTRIBUTE),
                    analysisPageReact.getAttributesBucket().getRoot())
            .getAttributesBucket()
            .getItemNames()
            .size(), 1);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_move_stacks_to_categories_when_switching_to_table() {
        assertEquals(analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .changeReportType(ReportType.TABLE)
            .getAttributesBucket()
            .getItemNames()
            .size(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_move_second_category_to_stacks_and_remove_to_rest_when_switching_to_chart() {
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .changeReportType(ReportType.TABLE)
            .changeReportType(ReportType.BAR_CHART);

        assertEquals(analysisPageReact.getAttributesBucket().getItemNames().size(), 1);
        assertFalse(analysisPageReact.getStacksBucket().isEmpty());
    }

    private void beforeOrderingTable() {
        analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();
    }

    private Integer unformatNumber(String number) {
        return new Integer(number.replace(",", ""));
    }
}
