package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_LAST_YEAR;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_THIS_YEAR;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.util.Collections.singletonList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsGeneralTest;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

public class KpiPopChangeValueExceedLimitTest extends DashboardsGeneralTest {

    private static final String METRIC_NUMBER = "Number[Sum]";

    private static final String CHANGE_VALUE_EXCEED_LIMIT_OR_INFINITY = ">999%";

    private static final String KPI_ERROR_DATA_RESOURCE = "/kpi-error-data/";

    private MetadataService mdService;
    private Project project;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTitle = "Kpi-pop-change-value-exceed-limit-or-infinity-test";
        // Create data from blank project
        projectTemplate = "";
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"precondition"})
    public void uploadDatasetFromCsv() throws IOException, JSONException, URISyntaxException {
        // The fixed data in CSV file will be invalid and make this test failed
        // when the time pass to another period.
        // So updating Csv file with new date data every time the test is executing
        // to make sure the data is always valid.
        String updatedCsvFilename = "data.csv";
        String updatedResourceName = KPI_ERROR_DATA_RESOURCE + updatedCsvFilename;
        updateCsvFile(getResourceAsFile(KPI_ERROR_DATA_RESOURCE + "user.csv"), updatedCsvFilename);

        uploadDatasetFromCsv(updatedResourceName);
    }

    @Test(dependsOnMethods = {"initDashboardTests"}, groups = {"precondition"})
    public void initializeGoodDataSDK() {
        goodDataClient = getGoodDataClient();
        mdService = goodDataClient.getMetadataService();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
    }

    @Test(dependsOnMethods = {"uploadDatasetFromCsv", "initializeGoodDataSDK"}, groups = {"precondition"})
    public void setupDashboardWithKpi() throws JSONException, IOException {
        String numberFactUri = mdService.getObjUri(project, Fact.class, title("number"));
        String firstNameAttributeUri = mdService.getObjUri(project, Attribute.class, title("firstname"));
        String firstNameValueUri = firstNameAttributeUri + "/elements?id=2";

        String dateDatasetUri = mdService.getObjUri(project, Dataset.class, identifier("user_date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttributeUri, firstNameValueUri);

        String numberMetricUri = mdService.createObj(project, new Metric(METRIC_NUMBER, maqlExpression, "#,##0"))
                .getUri();

        String sumOfNumberKpi = createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                new KpiMDConfiguration.Builder()
                .title("Number")
                .metric(numberMetricUri)
                .dateDataSet(dateDatasetUri)
                .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(ComparisonDirection.GOOD)
                .build());

        createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(sumOfNumberKpi));
    }

    @Test(dependsOnGroups = "precondition", groups = {"desktop", "mobile"})
    public void testChangeValueExceedLimitOrInfinity() {
        Kpi kpi = initIndigoDashboardsPageWithWidgets().getLastKpi();

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_THIS_YEAR);

        takeScreenshot(browser, "Change value exceeds limit", getClass());
        assertEquals(kpi.getPopSection().getChangeValue(), CHANGE_VALUE_EXCEED_LIMIT_OR_INFINITY);

        indigoDashboardsPage.selectDateFilterByName(DATE_FILTER_LAST_YEAR);

        takeScreenshot(browser, "Change value is infinity", getClass());
        assertEquals(kpi.getPopSection().getChangeValue(), CHANGE_VALUE_EXCEED_LIMIT_OR_INFINITY);
    }

    private String getCsvData(File csvFile) throws IOException {
        StringBuilder csvData = new StringBuilder();
        String record;

        try (BufferedReader file = new BufferedReader(new FileReader(csvFile))) {
            while ((record = file.readLine()) != null) {
                csvData.append(record + "\n");
            }
        }

        return csvData.toString();
    }

    private String changeDateFromCsvData(String csvData) {
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd");

        String thisYear = dateFormat.print(new DateTime(DateTimeZone.forID("America/Chicago")));
        String lastYear = dateFormat.print(new DateTime(DateTimeZone.forID("America/Chicago")).minusYears(1));

        return csvData
                .replace("${thisYear}", thisYear)
                .replace("${lastYear}", lastYear);
    }

    private void updateCsvFile(File csvFile, String updatedFileName) throws IOException {
        String csvData = getCsvData(csvFile);
        String updatedCsvFilePath = csvFile.getParent() + "/" + updatedFileName;

        try (FileWriter fw = new FileWriter(updatedCsvFilePath)) {
            fw.append(changeDateFromCsvData(csvData));
            fw.flush();
        }
    }

    private void uploadDatasetFromCsv(String csvFilePath) throws JSONException, URISyntaxException, IOException {
        setupMaql(KPI_ERROR_DATA_RESOURCE + "user.maql");
        setupData(csvFilePath, KPI_ERROR_DATA_RESOURCE + "upload_info.json");
    }
}
