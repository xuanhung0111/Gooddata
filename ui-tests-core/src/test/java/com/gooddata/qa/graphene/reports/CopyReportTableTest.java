package com.gooddata.qa.graphene.reports;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricFormat;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

public class CopyReportTableTest extends AbstractProjectTest {

    private final static String AMOUNT = "Amount";
    private final static String POSITION = "Position";
    private final static String SIMPLE_REPORT = "Simple-Report";
    private final static String AMOUNT_METRIC = "Amount-Metric";
    private final static String DEFAULT_FORMAT_VALUE = "476,640.00";
    private final static String CONDITION_FORMAT_VALUE = "$476,640";

    private Metric amountSum;

    @BeforeTest
    private void clearClipboard() {
        getClipboard().setContents(new StringSelection(""), null);
    }

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.PAYROLL_CSV + "/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll-file", getClass());

        final String amountUri = getMdService()
                .getObj(getProject(), Fact.class, title(AMOUNT))
                .getUri();

        amountSum = getMdService()
                .createObj(getProject(), new Metric(AMOUNT_METRIC,
                        MetricTypes.SUM.getMaql().replaceFirst("__fact__", format("[%s]", amountUri)),
                        Formatter.DEFAULT.toString()));

        final Attribute position = getMdService().getObj(getProject(), Attribute.class, title(POSITION));

        ReportDefinition definition = GridReportDefinitionContent.create(SIMPLE_REPORT, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(position.getDefaultDisplayForm().getUri(), position.getTitle())),
                singletonList(new MetricElement(amountSum)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void copySingleCell() throws HeadlessException, UnsupportedFlavorException, IOException {
        initReportsPage().openReport(SIMPLE_REPORT).getTableReport().copyMetricValue(DEFAULT_FORMAT_VALUE);
        takeScreenshot(browser, "copy-single-cell-on-report-page", getClass());
        assertEquals(getClipboardContent(), DEFAULT_FORMAT_VALUE);
        clearClipboard();

        initDashboardsPage().editDashboard()
                .addReportToDashboard(SIMPLE_REPORT)
                .saveDashboard();
        final TableReport table = dashboardsPage.getContent().getReport(SIMPLE_REPORT, TableReport.class);
        table.waitForLoaded();
        table.copyMetricValue(DEFAULT_FORMAT_VALUE);
        takeScreenshot(browser, "copy-single-cell-on-dahboard-page", getClass());
        assertEquals(getClipboardContent(), DEFAULT_FORMAT_VALUE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void copyFormattedCell()
            throws HeadlessException, UnsupportedFlavorException, IOException, ParseException, JSONException {

        changeMetricFormat(getRestApiClient(), amountSum.getUri(), Formatter.COLORS.toString());
        try {
            initReportsPage().openReport(SIMPLE_REPORT).getTableReport().copyMetricValue(CONDITION_FORMAT_VALUE);
            takeScreenshot(browser, "copy-formatted-cell", getClass());
            assertEquals(getClipboardContent(), CONDITION_FORMAT_VALUE);
        } finally {
            changeMetricFormat(getRestApiClient(), amountSum.getUri(), Formatter.DEFAULT.toString());
        }
    }

    private Clipboard getClipboard() {
        return Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    private String getClipboardContent() throws HeadlessException, UnsupportedFlavorException, IOException {
        return (String) getClipboard().getData(DataFlavor.stringFlavor); 
    }
}