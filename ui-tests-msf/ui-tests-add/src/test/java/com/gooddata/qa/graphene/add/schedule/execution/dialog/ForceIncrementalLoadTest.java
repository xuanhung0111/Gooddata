package com.gooddata.qa.graphene.add.schedule.execution.dialog;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.IncrementalPeriod;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DatasetDropdown;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog.LoadMode;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.time.LocalDateTime;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.ElementUtils.getBubbleMessage;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;

public class ForceIncrementalLoadTest extends AbstractDataloadProcessTest {

    private CsvFile opportunity;
    private CsvFile person;

    private String opportunityLSLTS;
    private String personLSLTS;

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .withDataset(new Dataset(DATASET_PERSON)
                        .withAttributes(ATTR_PERSON)
                        .withFacts(FACT_AGE))
                .buildMaql());

        opportunityLSLTS = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("OP_INIT", "100", opportunityLSLTS);

        personLSLTS = parseDateTime(LocalDateTime.now().plusSeconds(2), TIMESTAMP_FORMAT);
        person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("P_INIT", "18", personLSLTS);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void forceIncrementalWithInvalidValue() {
        final String fieldsNotDefined = "Define \"From\", \"To\", or both";
        final String wrongDateFormat = "Enter the date in the format:\n\"1969-12-31 23:59:59.999999\"";

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            RunOneOffDialog dialog = initScheduleDetail(schedule).triggerRunOneOffDialog();

            dialog.setMode(LoadMode.INCREMENTAL).clickConfirmButton();
            assertEquals(dialog.getIncrementalLoadHelperText(), fieldsNotDefined);
            assertEquals(getBubbleMessage(browser), fieldsNotDefined);

            dialog.setIncrementalStartTime("not date value");
            assertEquals(getBubbleMessage(browser), wrongDateFormat);

            dialog.setIncrementalStartTime("2017-01-01");
            assertEquals(getBubbleMessage(browser), wrongDateFormat);

            dialog.setIncrementalStartTime(LocalDateTime.now())
                    .setIncrementalEndTime(LocalDateTime.now().minusMinutes(5))
                    .clickConfirmButton();
            assertEquals(getBubbleMessage(browser), "\"From\" is greater than \"To\"");

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void notShowDatasetHaveNoTSColumnInDropdown() {
        CsvFile personNoTS = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE))
                .rows("P_INIT", "18");

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(personNoTS)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            assertEquals(initScheduleDetail(schedule)
                    .triggerRunOneOffDialog()
                    .setMode(LoadMode.INCREMENTAL)
                    .getDatasetDropdown()
                    .expand()
                    .getAvailableDatasets(), singletonList(DATASET_OPPORTUNITY));

        } finally {
            getProcessService().removeSchedule(schedule);
            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"firstIncremental"})
    public void forceIncrementalWhenLSLTSNotSet() {
        String time1 = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        String time2 = parseDateTime(LocalDateTime.now().plusSeconds(3), TIMESTAMP_FORMAT);
        String time3 = personLSLTS = opportunityLSLTS =
                parseDateTime(LocalDateTime.now().plusSeconds(5), TIMESTAMP_FORMAT);

        person.rows("P1", "18", time1).rows("P2", "20", time2).rows("P3", "20", time3);
        opportunity.rows("OP1", "100", time1).rows("OP2", "200", time2).rows("OP3", "200", time3);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            initScheduleDetail(schedule).executeSchedule(IncrementalPeriod.create(time1, time3))
                    .waitForExecutionFinish();
            DatasetDropdown dropdown = initScheduleDetail(schedule)
                    .selectCustomDatasetsOption().getDatasetDropdown().expand();
            assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), personLSLTS);
            assertEquals(dropdown.getLSLTSOf(DATASET_OPPORTUNITY), opportunityLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItems("P1", "P2", "P3"));

            Attribute oppAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
            assertThat(getAttributeValues(oppAttr), hasItems("OP1", "OP2", "OP3"));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void forceIncrementalWithOnlyFROMField() {
        LocalDateTime dateTime = LocalDateTime.now();
        String time1 = parseDateTime(dateTime, TIMESTAMP_FORMAT);
        String time2 = parseDateTime(dateTime.plusSeconds(1), TIMESTAMP_FORMAT);
        personLSLTS = parseDateTime(dateTime.plusSeconds(2), TIMESTAMP_FORMAT);

        person.rows("P4", "18", time1).rows("P5", "18", time2).rows("P6", "20", personLSLTS);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(IncrementalPeriod.from(time2)).waitForExecutionFinish();
            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItems("P5", "P6"));
            assertThat(getAttributeValues(personAttr), not(hasItem("P4")));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void forceIncrementalWithOnlyTOFieldLargerThanLSLTS() {
        LocalDateTime dateTime = LocalDateTime.now();
        String time1 = parseDateTime(dateTime, TIMESTAMP_FORMAT);
        personLSLTS = parseDateTime(dateTime.plusSeconds(1), TIMESTAMP_FORMAT);
        String time2 = parseDateTime(dateTime.plusSeconds(2), TIMESTAMP_FORMAT);

        person.rows("P7", "18", time1).rows("P8", "18", personLSLTS).rows("P9", "20", time2);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(IncrementalPeriod.to(personLSLTS)).waitForExecutionFinish();
            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItems("P7", "P8"));
            assertThat(getAttributeValues(personAttr), not(hasItem("P9")));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void forceIncrementalWithOnlyTOFieldSmallerThanLSLTS() {
        LocalDateTime dateTime = parseDateTime(personLSLTS, TIMESTAMP_FORMAT).minusSeconds(5);
        String time = parseDateTime(dateTime, TIMESTAMP_FORMAT);
        person.rows("P10", "18", time);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(IncrementalPeriod.to(time)).waitForExecutionFinish();
            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItem("P10"));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void forceIncrementalWithBothFieldLargerThanLSLTS() {
        String time = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        personLSLTS = parseDateTime(LocalDateTime.now().plusSeconds(2), TIMESTAMP_FORMAT);
        person.rows("P11", "18", time).rows("P12", "20", personLSLTS);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(IncrementalPeriod.create(time, personLSLTS))
                    .waitForExecutionFinish();
            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItems("P11", "P12"));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void forceIncrementalWithBothFieldSmallerThanLSLTS() {
        LocalDateTime dateTime = parseDateTime(personLSLTS, TIMESTAMP_FORMAT);
        String time1 = parseDateTime(dateTime.minusSeconds(5), TIMESTAMP_FORMAT);
        String time2 = parseDateTime(dateTime.minusSeconds(3), TIMESTAMP_FORMAT);
        person.rows("P13", "18", time1).rows("P14", "20", time2);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(IncrementalPeriod.create(time1, time2))
                    .waitForExecutionFinish();
            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItems("P13", "P14"));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void loadNothingInCaseBothFieldLargerThanMaxTS() {
        person.rows("P15", "18", parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT));

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule)
                    .executeSchedule(IncrementalPeriod
                            .create(LocalDateTime.now().plusSeconds(3), LocalDateTime.now().plusSeconds(5)))
                    .waitForExecutionFinish();

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), not(hasItem("P15")));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"firstIncremental"})
    public void forceFullLoadAfterIncrementalWithTOField() {
        personLSLTS = parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT);
        person.rows("P16", "18", personLSLTS);

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person)));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.executeSchedule(IncrementalPeriod.to(LocalDateTime.now().plusSeconds(5)))
                    .waitForExecutionFinish();
            scheduleDetail.executeSchedule(LoadMode.FULL).waitForExecutionFinish();

            assertEquals(initScheduleDetail(schedule)
                    .getDatasetDropdown()
                    .expand()
                    .getLSLTSOf(DATASET_PERSON), personLSLTS);

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }
}
