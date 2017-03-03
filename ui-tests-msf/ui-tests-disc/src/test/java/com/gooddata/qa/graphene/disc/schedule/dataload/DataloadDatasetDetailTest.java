package com.gooddata.qa.graphene.disc.schedule.dataload;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.utils.ads.AdsHelper.ADS_DB_CONNECTION_URL;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getDatasetModelView;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.executeProcess;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Collection;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.qa.graphene.disc.common.AbstractDataloadScheduleTest;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class DataloadDatasetDetailTest extends AbstractDataloadScheduleTest {

    private static final String ARTIST_DATASET = "artist";
    private static final String TRACK_DATASET = "track";

    private static final String ARTIST_NAME = "artistname";
    private static final Collection<String> ARTIST_NAME_ATTR_VALUES = asList("OOP1", "OOP2", "OOP3",
            "OOP4", "OOP5", "OOP6");

    private static final String TRACK_ID = "trackid";
    private static final Collection<String> TRACK_ID_ATTR_VALUES = asList("1tractID", "2tractID", "3tractID",
            "4tractID", "5tractID", "6tractID");

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        setupMaql(MAQL_FILES.getPath() + TxtFile.CREATE_REFERENCE_LDM.getName());

        Parameters parameters = new Parameters()
                .addParameter("CREATE_TABLE",
                        getResourceAsString(SQL_FILES.getPath() + TxtFile.REFERENCE_ADS_TABLE.getName()))
                .addParameter("ADS_URL", format(ADS_DB_CONNECTION_URL, testParams.getHost(), ads.getId()))
                .addParameter("ADS_USER", testParams.getUser())
                .addSecureParameter("ADS_PASSWORD", testParams.getPassword());

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void executeDataloadScheduleWithOneDataset() {
        String schedule = "Schedule-" + generateHashString();
        ((CreateScheduleForm) __initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectDatasets(ARTIST_DATASET))
                .enterScheduleName(schedule)
                .schedule();

        try {
            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser)
                    .executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

            Attribute artistName = getMdService().getObj(getProject(), Attribute.class, title(ARTIST_NAME));
            assertEquals(getAttributeValues(artistName), ARTIST_NAME_ATTR_VALUES);

            Attribute trackid = getMdService().getObj(getProject(), Attribute.class, title(TRACK_ID));
            assertEquals(getAttributeValues(trackid), singletonList(""));

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnMethods = {"executeDataloadScheduleWithOneDataset"})
    public void executeDataloadScheduleWithAllDatasets() {
        String schedule = "Schedule-" + generateHashString();
        ((CreateScheduleForm) __initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectAllDatasetsOption())
                .enterScheduleName(schedule)
                .schedule();

        try {
            __ScheduleDetailFragment scheduleDetail = __ScheduleDetailFragment.getInstance(browser)
                    .executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

            Attribute artistName = getMdService().getObj(getProject(), Attribute.class, title(ARTIST_NAME));
            assertEquals(getAttributeValues(artistName), ARTIST_NAME_ATTR_VALUES);

            Attribute trackid = getMdService().getObj(getProject(), Attribute.class, title(TRACK_ID));
            assertEquals(getAttributeValues(trackid), TRACK_ID_ATTR_VALUES);

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnMethods = {"executeDataloadScheduleWithAllDatasets"})
    public void checkDatasetsReference() throws ParseException, JSONException, IOException {
        JSONObject trackDataset = getDatasetModelView(getRestApiClient(), testParams.getProjectId(), TRACK_DATASET);
        assertThat(trackDataset.getString("references"), containsString("dataset.artist"));

        Attribute artistName = getMdService().getObj(getProject(), Attribute.class, title(ARTIST_NAME));
        Fact number = getMdService().getObj(getProject(), Fact.class, title("number"));
        Metric sumOfNumber = createMetric("sumOfNumber", format("SELECT SUM([%s])", number.getUri()), "#,##0");

        Report report = createReportViaRest(GridReportDefinitionContent.create("referenceReport",
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(
                        artistName.getDefaultDisplayForm().getUri(), artistName.getTitle())),
                singletonList(new MetricElement(sumOfNumber))));

        TableReport tableReport = initReportsPage().openReport(report.getTitle()).getTableReport();
        takeScreenshot(browser, "report-shows-correctly-for-reference-datasets", getClass());
        assertEquals(tableReport.getAttributeElements(), ARTIST_NAME_ATTR_VALUES);
        assertEquals(tableReport.getMetricElements(), asList(100f, 200f, 300f, 400f, 500f, 600f));
    }

    private Collection<String> getAttributeValues(Attribute attribute) {
        return getMdService().getAttributeElements(attribute)
                .stream().map(attr -> attr.getTitle()).map(String::trim).collect(toList());
    }
}
