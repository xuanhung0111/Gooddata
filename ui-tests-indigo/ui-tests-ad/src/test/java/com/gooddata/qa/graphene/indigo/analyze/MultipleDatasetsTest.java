package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForStringInUrl;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import org.json.JSONException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CatalogPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MultipleDatasetsTest extends AbstractAnalyseTest {

    private static final String MAQL_PATH = "/quotes/quotes.maql";
    private static final String QUOTES_CSV_PATH = "/quotes/quotes.csv";
    private static final String UPLOAD_INFO_PATH = "/quotes/upload_info.json";
    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";
    private static final String GEO_CHART_CSV_PATH = "/" + UPLOAD_CSV + "/geo_chart.csv";

    private static final String PRODUCTION_DATASET = "Production data";
    private static final String PAYROLL_DATASET = "Payroll";
    private static final String GEO_CHART_DATASET = "Geo Chart";

    private static final String AMOUNT = "Amount";
    private static final String HIGH_PRICE = "High Price";
    private static final String MARKET = "Market";

    @Override
    public void initProperties() {
        // create empty project and customized data
        projectTitle = "Multiple-Datasets-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        setupProductionData();
        uploadPayrollDataset();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void analyzeReportOnProductionData() {
        if (testParams.isClientDemoEnvironment()) {
            log.info("client-demo does not support upload Payroll Dataset");
            return;
        }

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).getCatalogPanel().changeDataset(PRODUCTION_DATASET);

        ChartReport report = analysisPage.addMetric("Close Price", FieldType.FACT)
                .addDate()
                .addStack("Industry")
                .waitForReportComputing()
                .getChartReport();
        takeScreenshot(browser, "analyzeReportOnProductionData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();
        filtersBucketReact.configAttributeFilter("Industry", "Apparel Stores", "Consumer Services");
        analysisPage.waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnProductionData - apply attribute filter", getClass());
        assertEquals(parseFilterText(filtersBucketReact.getFilterText("Industry")), Arrays.asList("Industry", "Apparel Stores, Consumer Services\n(2)"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void analyzeReportOnPayrollData() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).getCatalogPanel().changeDataset(PAYROLL_DATASET);

        ChartReport report = analysisPage.addMetric(AMOUNT, FieldType.FACT)
            .addDate()
            .addStack("County")
            .waitForReportComputing()
            .getChartReport();
        takeScreenshot(browser, "analyzeReportOnPayrollData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();
        filtersBucketReact.configAttributeFilter("County", "Austin", "Clover");
        analysisPage.waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnPayrollData - apply attribute filter", getClass());
        assertEquals(parseFilterText(filtersBucketReact.getFilterText("County")), Arrays.asList("County", "Austin, Clover"));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchDataAfterSelectDataset() {
        final CatalogPanel catalogPanel = initAnalysePage().getCatalogPanel();

        catalogPanel.changeDataset(PRODUCTION_DATASET).search(AMOUNT);
        takeScreenshot(browser, "searchDataAfterSelectDataset - search in production data", getClass());
        assertFalse(catalogPanel.getFieldNamesInViewPort().contains(AMOUNT), "Amount shouldn't be in view port");

        catalogPanel.search("County");
        assertFalse(catalogPanel.getFieldNamesInViewPort().contains("County"), "County shouldn't be in view port");

        catalogPanel.search("Id");
        assertThat(catalogPanel.getFieldNamesInViewPort(), hasItem("Id"));

        catalogPanel.changeDataset(PAYROLL_DATASET).search(AMOUNT);
        takeScreenshot(browser, "searchDataAfterSelectDataset - search in payroll data", getClass());
        assertThat(catalogPanel.getFieldNamesInViewPort(), hasItem(AMOUNT));

        catalogPanel.search("County");
        assertThat(catalogPanel.getFieldNamesInViewPort(), hasItem("County"));

        catalogPanel.search("Id");
        assertFalse(catalogPanel.getFieldNamesInViewPort().contains("Id"), "ID shouldn't be in view port");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-9815: Filter for metric is undefined after switching dataset")
    public void addMetricFilterAfterSwitchingDataset() {
        if (testParams.isClientDemoEnvironment()) {
            log.info("client-demo does not support upload Payroll Dataset");
            return;
        }

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(HIGH_PRICE, FieldType.FACT)
            .addAttribute(MARKET).waitForReportComputing();

        analysisPage.getCatalogPanel().changeDataset(PAYROLL_DATASET);
        analysisPage.removeAttribute(MARKET)
                .addMetric(AMOUNT, FieldType.FACT)
                .getMetricsBucket()
                .getMetricConfiguration("Sum of " + AMOUNT)
                .expandConfiguration()
                .addFilter("Education", "Bachelors Degree", "Partial High School");
        analysisPage.waitForReportComputing();
        takeScreenshot(browser, "add-metric-filter-after-switching-dataset", getClass());
        assertEquals(analysisPage.getChartReport().getDataLabels(), asList("171,741.48", "2,553,804.80"),
                "Metric filter was not applied");
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-9957: Get error when working on viz containing unrelated date")
    public void showPercentOnInsightContainingUnrelatedDate() {
        if (testParams.isClientDemoEnvironment()) {
            log.info("client-demo does not support upload Payroll Dataset");
            return;
        }
        //need a dataset containing no date
        uploadCSV(getFilePathFromResource(GEO_CHART_CSV_PATH));
        takeScreenshot(browser, "uploaded-geochart", getClass());

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(HIGH_PRICE, FieldType.FACT)
                .addDate()
                .waitForReportComputing()
                .getCatalogPanel()
                .changeDataset(GEO_CHART_DATASET);

        //ensure working dataset is geo_chart before adding percent to chart
        waitForStringInUrl("csv_geo_chart");
        analysisPage.getMetricsBucket().getMetricConfiguration("Sum of " + HIGH_PRICE).expandConfiguration()
                .showPercents();
        analysisPage.waitForReportComputing();
        takeScreenshot(browser, "show-percent-on-insight-containing-unrelated-date", getClass());
        assertEquals(analysisPage.getChartReport().getDataLabels(), singletonList("1.00"));
    }

    private void setupProductionData() throws JSONException {
        postMAQL(getResourceAsString(MAQL_PATH), DEFAULT_PROJECT_CHECK_LIMIT);
        setupData(QUOTES_CSV_PATH, UPLOAD_INFO_PATH);
    }

    private void uploadPayrollDataset() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-payroll", getClass());
    }
}
