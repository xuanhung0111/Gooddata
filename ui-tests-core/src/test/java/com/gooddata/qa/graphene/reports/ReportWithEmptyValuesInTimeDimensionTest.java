package com.gooddata.qa.graphene.reports;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.testng.annotations.Test;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

public class ReportWithEmptyValuesInTimeDimensionTest extends AbstractProjectTest {
    
    private final static String AMOUNT_METRIC = "Amount-Metric";
    private final static String SIMPLE_REPORT = "Simple-Report";
    private final static String EDUCATION = "Education";
    private final static String BACHELORS_DEGREE = "Bachelors Degree";
    private final static String OF_ALL_ROWS = "of All Rows";

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.PAYROLL_CSV + "/payroll_null_date.csv"));
        takeScreenshot(browser, "uploaded-payroll-file", getClass());

        final String amountUri = getMdService().getObj(getProject(), Fact.class, title("Amount")).getUri();

        getMdService().createObj(getProject(),
                new Metric(AMOUNT_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        "#,##0.00"));

        //creating report using UI due to adding attribute position
        initReportCreation().createReport(new UiReportDefinition()
                .withName(SIMPLE_REPORT)
                .withWhats(AMOUNT_METRIC)
                .withHows(new HowItem(EDUCATION, HowItem.Position.TOP))
                .withHows(new HowItem("Year (Paydate)", HowItem.Position.LEFT)));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testRollupAggregation() {
        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        table.openContextMenuFrom(BACHELORS_DEGREE, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(ContextMenu.AggregationType.ROLLUP, OF_ALL_ROWS);

        reportPage.waitForReportExecutionProgress();

        takeScreenshot(browser, "rollup-aggregation", getClass());
        assertTrue(isEqualCollection(table.getTotalHeaders(), singletonList(ContextMenu.AggregationType.ROLLUP.getType())),
                "The total is not displayed");

        //use List.equals() to test that total values are computed correctly and have correct order
        assertTrue(table.getTotalValues().equals(asList(2099164.09f, 1548205.18f, 1281463.35f, 1869069.31f, 454640.69f)),
                "Rollup values are not correct");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testAverageAggregation() {
        final TableReport table = initReportsPage().openReport(SIMPLE_REPORT).getTableReport();

        table.openContextMenuFrom(BACHELORS_DEGREE, CellType.ATTRIBUTE_VALUE)
                .aggregateTableData(ContextMenu.AggregationType.AVERAGE, OF_ALL_ROWS);
        reportPage.waitForReportExecutionProgress();

        takeScreenshot(browser, "average-aggregation", getClass());
        assertTrue(
                isEqualCollection(table.getTotalHeaders(), singletonList(ContextMenu.AggregationType.AVERAGE.getType())),
                "The total is not displayed");

        //use List.equals() to test that total values are computed correctly and have correct order
        assertTrue(
                table.getTotalValues().equals(asList(1049582.04f, 516068.39f, 640731.68f, 934534.66f, 227320.35f)),
                "Average values are not correct");
    }

}