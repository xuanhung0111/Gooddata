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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

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

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupProject"})
    public void enableAccessingDataSection() throws IOException, JSONException {
        RestUtils.setFeatureFlags(getRestApiClient(), FeatureFlagOption.createFeatureClassOption(
                ProjectFeatureFlags.ENABLE_CSV_UPLOADER.getFlagName(), true));
    }

    @Test(dependsOnMethods = {"enableAccessingDataSection"}, groups = {"setupProject"})
    public void uploadPayrollDataset() {
        uploadCSV(getFilePathFromResource(PAYROLL_CSV_PATH));
        takeScreenshot(browser, "uploaded-payroll", getClass());
    }

    @Test(dependsOnGroups = {"init"})
    public void analyzeReportOnProductionData() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetricFromFact("Close Price")
                .addCategory(DATE)
                .addStackBy("Industry")
                .waitForReportComputing()
                .getChartReport();
        takeScreenshot(browser, "analyzeReportOnProductionData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        analysisPage.configAttributeFilter("Industry", "Apparel Stores", "Consumer Services")
            .waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnProductionData - apply attribute filter", getClass());
        assertEquals(analysisPage.getFilterText("Industry"), "Industry: Apparel Stores, Consumer Services\n(2)");
    }

    @Test(dependsOnGroups = {"init"})
    public void analyzeReportOnPayrollData() {
        initAnalysePage();

        ChartReport report = analysisPage.changeDataset(PAYROLL_DATASET)
            .addMetricFromFact(AMOUNT)
            .addCategory(DATE)
            .addStackBy("County")
            .waitForReportComputing()
            .getChartReport();
        takeScreenshot(browser, "analyzeReportOnPayrollData", getClass());
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));

        analysisPage.configAttributeFilter("County", "Austin", "Clover")
            .waitForReportComputing();
        assertThat(report.getTrackersCount(), greaterThanOrEqualTo(1));
        takeScreenshot(browser, "analyzeReportOnPayrollData - apply attribute filter", getClass());
        assertEquals(analysisPage.getFilterText("County"), "County: Austin, Clover");
    }

    @Test(dependsOnGroups = {"init"})
    public void searchDataAfterSelectDataset() {
        initAnalysePage();

        assertFalse(analysisPage.changeDataset(PRODUCTION_DATASET)
            .searchBucketItem(AMOUNT));
        takeScreenshot(browser, "searchDataAfterSelectDataset - search in production data", getClass());
        assertFalse(analysisPage.searchBucketItem("County"));
        assertTrue(analysisPage.searchBucketItem("Id"));

        assertTrue(analysisPage.changeDataset(PAYROLL_DATASET)
                .searchBucketItem(AMOUNT));
        takeScreenshot(browser, "searchDataAfterSelectDataset - search in payroll data", getClass());
        assertTrue(analysisPage.searchBucketItem("County"));
        assertFalse(analysisPage.searchBucketItem("Id"));
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
