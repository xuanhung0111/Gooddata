package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.UPLOAD_CSV;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URISyntaxException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanelReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucketReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;
import java.io.IOException;
import java.lang.reflect.Method;

public class GoodSalesMultipleDatasetsTest extends AbstractAnalyseTest {

    private static final String MAQL_PATH = "/quotes/quotes.maql";
    private static final String QUOTES_CSV_PATH = "/quotes/quotes.csv";
    private static final String UPLOAD_INFO_PATH = "/quotes/upload_info.json";
    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";

    private static final String PRODUCTION_DATASET = "Production data";
    private static final String PAYROLL_DATASET = "Payroll";

    private static final String AMOUNT = "Amount";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Multiple-Datasets-Test";
    }

    @Override
    public void prepareSetupProject() throws JSONException, URISyntaxException, IOException {
        setupProductionData();
        uploadPayrollDataset();
    }

    //force FF to refresh after each test
    //because FF is only refreshed when url is different from previous one
    @BeforeMethod
    public void refreshStartPage(Method m) {
        if (!m.getDeclaringClass().equals(this.getClass())) return;

        if (m.getAnnotation(Test.class) != null) {
            browser.navigate().refresh();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void analyzeReportOnProductionData() {
        ChartReportReact report = analysisPageReact.addMetric("Close Price", FieldType.FACT)
                .addDate()
                .addStack("Industry")
                .waitForReportComputing()
                .getChartReport();
        takeScreenshot(browser, "analyzeReportOnProductionData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();
        filtersBucketReact.configAttributeFilter("Industry", "Apparel Stores", "Consumer Services");
        analysisPageReact.waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnProductionData - apply attribute filter", getClass());
        assertEquals(filtersBucketReact.getFilterText("Industry"), "Industry: Apparel Stores, Consumer Services\n(2)");
    }

    @Test(dependsOnGroups = {"init"})
    public void analyzeReportOnPayrollData() {
        analysisPageReact.getCataloguePanel().changeDataset(PAYROLL_DATASET);

        ChartReportReact report = analysisPageReact.addMetric(AMOUNT, FieldType.FACT)
            .addDate()
            .addStack("County")
            .waitForReportComputing()
            .getChartReport();
        takeScreenshot(browser, "analyzeReportOnPayrollData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        final FiltersBucketReact filtersBucketReact = analysisPageReact.getFilterBuckets();
        filtersBucketReact.configAttributeFilter("County", "Austin", "Clover");
        analysisPageReact.waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnPayrollData - apply attribute filter", getClass());
        assertEquals(filtersBucketReact.getFilterText("County"), "County: Austin, Clover");
    }

    @Test(dependsOnGroups = {"init"})
    public void searchDataAfterSelectDataset() {
        final CataloguePanelReact cataloguePanel = analysisPageReact.getCataloguePanel();

        assertFalse(cataloguePanel.changeDataset(PRODUCTION_DATASET)
            .search(AMOUNT));
        takeScreenshot(browser, "searchDataAfterSelectDataset - search in production data", getClass());
        assertFalse(cataloguePanel.search("County"));
        assertTrue(cataloguePanel.search("Id"));

        assertTrue(cataloguePanel.changeDataset(PAYROLL_DATASET)
                .search(AMOUNT));
        takeScreenshot(browser, "searchDataAfterSelectDataset - search in payroll data", getClass());
        assertTrue(cataloguePanel.search("County"));
        assertFalse(cataloguePanel.search("Id"));
    }

    private void setupProductionData() throws JSONException, URISyntaxException, IOException {
        postMAQL(getResourceAsString(MAQL_PATH), DEFAULT_PROJECT_CHECK_LIMIT);
        setupData(QUOTES_CSV_PATH, UPLOAD_INFO_PATH);
    }

    private void uploadPayrollDataset() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-payroll", getClass());
    }
}
