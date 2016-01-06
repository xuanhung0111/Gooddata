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
import java.net.URL;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class FilterAdCatalogToWholeProjectTest extends AnalyticalDesignerAbstractTest {

    private static final String MAQL_PATH = "/quotes/quotes.maql";
    private static final String QUOTES_CSV_PATH = "/quotes/quotes.csv";
    private static final String UPLOAD_INFO_PATH = "/quotes/upload_info.json";
    private static final String PAYROLL_CSV_PATH = "/" + UPLOAD_CSV + "/payroll.csv";

    private static final String PRODUCTION_DATASET = "Production data";
    private static final String PAYROLL_DATASET = "Payroll";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = "";
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-Filter-Catalog-Whole-Project-Test";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupProject"})
    public void setupProductionData() throws JSONException, URISyntaxException {
        postMAQL(getResourceAsString(MAQL_PATH), DEFAULT_PROJECT_CHECK_LIMIT);
        setupData(QUOTES_CSV_PATH, UPLOAD_INFO_PATH);
    }

    @Test(dependsOnMethods = {"setupProductionData"}, groups = {"setupProject"})
    public void uploadPayrollDataset() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-payroll", getClass());
    }

    @Test(dependsOnGroups = {"init"})
    public void analyzeReportOnProductionData() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric("Close Price", FieldType.FACT)
                .addDate()
                .addStack("Industry")
                .waitForReportComputing()
                .getChartReport();
        takeScreenshot(browser, "analyzeReportOnProductionData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        filtersBucket.configAttributeFilter("Industry", "Apparel Stores", "Consumer Services");
        analysisPage.waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnProductionData - apply attribute filter", getClass());
        assertEquals(filtersBucket.getFilterText("Industry"), "Industry: Apparel Stores, Consumer Services\n(2)");
    }

    @Test(dependsOnGroups = {"init"})
    public void analyzeReportOnPayrollData() {
        initAnalysePage();
        analysisPage.getCataloguePanel().changeDataset(PAYROLL_DATASET);

        ChartReport report = analysisPage.addMetric(AMOUNT, FieldType.FACT)
            .addDate()
            .addStack("County")
            .waitForReportComputing()
            .getChartReport();
        takeScreenshot(browser, "analyzeReportOnPayrollData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        final FiltersBucket filtersBucket = analysisPage.getFilterBuckets();
        filtersBucket.configAttributeFilter("County", "Austin", "Clover");
        analysisPage.waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnPayrollData - apply attribute filter", getClass());
        assertEquals(filtersBucket.getFilterText("County"), "County: Austin, Clover");
    }

    @Test(dependsOnGroups = {"init"})
    public void searchDataAfterSelectDataset() {
        initAnalysePage();
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

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

    private void setupData(String csvPath, String uploadInfoPath) throws JSONException, URISyntaxException {
        URL csvResource = getClass().getResource(csvPath);
        String webdavURL = uploadFileToWebDav(csvResource, null);

        URL uploadInfoResource = getClass().getResource(uploadInfoPath);
        uploadFileToWebDav(uploadInfoResource, webdavURL);

        postPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()),
                DEFAULT_PROJECT_CHECK_LIMIT);
    }
}
