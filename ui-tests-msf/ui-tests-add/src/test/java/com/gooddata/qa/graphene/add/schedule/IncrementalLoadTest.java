package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.http.RestUtils.getResource;
import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DatasetDropdown;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog.LoadMode;

public class IncrementalLoadTest extends AbstractDataloadProcessTest {

    private CsvFile opportunity;
    private CsvFile person;

    private Schedule loadPersonSchedule;

    private String lastLSLTS;

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .withDataset(new Dataset(DATASET_PERSON)
                        .withAttributes(ATTR_PERSON)
                        .withFacts(FACT_AGE))
                .buildMaql());

        String time = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        lastLSLTS = parseDateTime(LocalDateTime.now().plusSeconds(5), TIMESTAMP_FORMAT);

        opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("OPP1", "100", time);

        person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("P1", "18", time)
                .rows("P2", "20", lastLSLTS);

        loadPersonSchedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDatasetNotSetLSLTS() {
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(),
                SyncDatasets.custom(DATASET_OPPORTUNITY));

        try {
            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_OPPORTUNITY), "not set");

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"setFirstLSLTS"})
    public void setLSLTSForDataset() {
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        executeSchedule(loadPersonSchedule);

        DatasetDropdown dropdown = initScheduleDetail(loadPersonSchedule)
                .getDatasetDropdown().expand();
        assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), lastLSLTS);
        assertThat(dropdown.getDatasetGroups().get("INCREMENTAL LOAD"), hasItem(DATASET_PERSON));

        Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
        assertThat(getAttributeValues(personAttr),
                containsInAnyOrder(person.getColumnValues(ATTR_PERSON).toArray()));
    }

    @Test(dependsOnGroups = {"setFirstLSLTS"})
    public void checkBasicIncrementalLoad() {
        String time = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        lastLSLTS = parseDateTime(LocalDateTime.now().plusSeconds(5), TIMESTAMP_FORMAT);
        person.rows("P3", "20", time)
                .rows("P4", "20", lastLSLTS);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        executeSchedule(loadPersonSchedule);

        assertEquals(initScheduleDetail(loadPersonSchedule)
                .getDatasetDropdown()
                .expand()
                .getLSLTSOf(DATASET_PERSON), lastLSLTS);

        Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
        assertThat(getAttributeValues(personAttr),
                containsInAnyOrder(person.getColumnValues(ATTR_PERSON).toArray()));
    }

    @Test(dependsOnGroups = {"setFirstLSLTS"})
    public void showUpErrorWithNullTimestamp() {
        opportunity.rows("OPP_ERROR", "100");
        person.rows("P_ERROR", "19");

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getErrorMessage(),
                    format("While trying to load project %s, the following datasets had one or more rows with a "
                    + "null timestamp, which is not allowed: %s: 1 row(s), %s: 1 row(s).",
                    testParams.getProjectId(), DATASET_PERSON, DATASET_OPPORTUNITY));

            DatasetDropdown dropdown = initScheduleDetail(schedule)
                    .selectCustomDatasetsOption().getDatasetDropdown().expand();
            assertEquals(dropdown.getLSLTSOf(DATASET_OPPORTUNITY), "not set");
            assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), lastLSLTS);

        } finally {
            getProcessService().removeSchedule(schedule);
            opportunity.removeLastRowData();
            person.removeLastRowData();
        }
    }

    @Test(dependsOnGroups = {"setFirstLSLTS"})
    public void setLSLTSHasMicroSecond() {
        String datePattern = "yyyy-MM-dd HH:mm:ss.SSSSSS";
        LocalDateTime dateTime = LocalDateTime.now();
        String time1 = parseDateTime(dateTime.withNano(111111), datePattern);
        String time2 = parseDateTime(dateTime.withNano(222222), datePattern);

        LocalDateTime otherDateTime = dateTime.withNano(777777777);
        String time3 = parseDateTime(otherDateTime, "yyyy-MM-dd HH:mm:ss.SSSSSSSSS");
        lastLSLTS = parseDateTime(otherDateTime.withNano(otherDateTime.getNano() + 1000), datePattern);

        person.rows("P5", "18", time1)
                .rows("P6", "20", time2)
                .rows("P7", "25", time3);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));


        executeSchedule(loadPersonSchedule);

        assertEquals(initScheduleDetail(loadPersonSchedule)
                .getDatasetDropdown()
                .expand()
                .getLSLTSOf(DATASET_PERSON), lastLSLTS);

        Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
        assertThat(getAttributeValues(personAttr),
                containsInAnyOrder(person.getColumnValues(ATTR_PERSON).toArray()));
    }

    @Test(dependsOnGroups = {"setFirstLSLTS"})
    public void checkIncrementalLoadWithPrefixTable() throws ParseException, JSONException, IOException {
        getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId(), "", "gDC_");

        String time = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        lastLSLTS = parseDateTime(LocalDateTime.now().plusSeconds(5), TIMESTAMP_FORMAT);

        person.setName("gDc_" + DATASET_PERSON)
                .rows("P8", "20", time)
                .rows("P9", "20", lastLSLTS);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        try {
            executeSchedule(loadPersonSchedule);

            assertEquals(initScheduleDetail(loadPersonSchedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), lastLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr),
                    containsInAnyOrder(person.getColumnValues(ATTR_PERSON).toArray()));

        } finally {
            getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
            person.setName(DATASET_PERSON);
        }
    }

    @Test(dependsOnGroups = {"setFirstLSLTS"})
    public void notShowLSLTSAfterDeleteTimestampColumn() throws ParseException, IOException {
        CsvFile personWithNoTS = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE))
                .rows("P1_NO_TS", "18")
                .rows("P2_NO_TS", "20");

        Parameters parameters = defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(personWithNoTS));
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE, parameters);

        try {
            executeSchedule(loadPersonSchedule);

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(loadPersonSchedule);
            assertFalse(scheduleDetail.getDatasetDropdown().expand().hasLSLTSValueFor(DATASET_PERSON),
                    "LSLTS still show up for dataset" + DATASET_PERSON);
            assertTrue(getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK)
                    .contains(format("The LSLTS for dataset with id dataset.%s is: %s", DATASET_PERSON, lastLSLTS)));

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr),
                    containsInAnyOrder(personWithNoTS.getColumnValues(ATTR_PERSON).toArray()));

        } finally {
            parameters.addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person));
            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE, parameters);

            executeSchedule(loadPersonSchedule, LoadMode.FULL);
        }
    }
}
