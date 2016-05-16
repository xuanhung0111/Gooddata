package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_LAST_YEAR;
import static com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsTest.DATE_FILTER_THIS_YEAR;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.DashboardsGeneralTest;

public class KpiPopChangeValueExceedLimitTest extends DashboardsGeneralTest {

    private static final String METRIC_NUMBER = "Number[Sum]";

    private static final String CHANGE_VALUE_EXCEED_LIMIT_OR_INFINITY = ">999%";

    private static final String KPI_ERROR_DATA_RESOURCE = "/kpi-error-data/";

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
        try (InputStream stream =
                updateCsvFile(getResourceAsFile(KPI_ERROR_DATA_RESOURCE + "user.csv"), "data.csv")) {
            uploadDatasetFromCsv(stream);
        }
    }

    @Test(dependsOnMethods = {"uploadDatasetFromCsv"}, groups = {"precondition"})
    public void setupDashboardWithKpi() throws JSONException, IOException {
        String numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("number"));
        String firstNameAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title("firstname"));
        String firstNameValueUri = firstNameAttributeUri + "/elements?id=2";

        String dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class, identifier("user_date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                numberFactUri, firstNameAttributeUri, firstNameValueUri);

        String numberMetricUri = getMdService().createObj(getProject(), new Metric(METRIC_NUMBER, maqlExpression, "#,##0"))
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

        waitForFragmentVisible(indigoDashboardsPage).selectDateFilterByName(DATE_FILTER_THIS_YEAR);

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

    private InputStream updateCsvFile(File csvFile, String updatedFileName) throws IOException {
        String csvData = getCsvData(csvFile);
        String updatedCsvFilePath = csvFile.getParent() + "/" + updatedFileName;

        try (FileWriter fw = new FileWriter(updatedCsvFilePath)) {
            fw.append(changeDateFromCsvData(csvData));
            fw.flush();
        }

        return new FileInputStream(updatedCsvFilePath);
    }

    private void uploadDatasetFromCsv(InputStream inputStream) throws JSONException, URISyntaxException, IOException {
        setupMaql(KPI_ERROR_DATA_RESOURCE + "user.maql");

        // Grey page cannot be accessed on mobile, should use Rest to setup dataset for project here
        setupDataViaRest("dataset.user", inputStream);
    }
}
