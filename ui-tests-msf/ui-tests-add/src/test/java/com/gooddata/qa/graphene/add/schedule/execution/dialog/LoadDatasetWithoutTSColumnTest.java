package com.gooddata.qa.graphene.add.schedule.execution.dialog;

import static com.gooddata.md.Restriction.title;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

import java.io.IOException;
import java.util.Collection;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DatasetDropdown;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog;

public class LoadDatasetWithoutTSColumnTest extends AbstractDataloadProcessTest {

    private static final Collection<String> OPPORTUNITY_ATTR_VALUES = asList("OOP1", "OOP2");
    private static final Collection<String> PERSON_ATTR_VALUES = asList("P1", "P2");

    private CsvFile opportunity;
    private CsvFile person;

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

        opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE))
                .rows("OOP1", "100")
                .rows("OOP2", "200");

        person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE))
                .rows("P1", "18")
                .rows("P2", "20");

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void displaySimpleDialogWithDatasetHasNoTSColumn() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            RunOneOffDialog dialog = initScheduleDetail(schedule).triggerRunOneOffDialog();
            assertTrue(dialog.hasNoMode(), "Dialog shows mode although all mapped ADS tables have no TS column");

            DatasetDropdown dropdown = dialog.getDatasetDropdown().expand();
            assertThat(dropdown.getAvailableDatasets(), hasItems(DATASET_OPPORTUNITY, DATASET_PERSON));
            assertEquals(dropdown.getSelectedDatasets(), singletonList(DATASET_PERSON));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void showErrorWithIncorrectMappingDataset() {
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, "DROP TABLE opportunity;"));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            DatasetDropdown dropdown = initScheduleDetail(schedule).triggerRunOneOffDialog()
                    .getDatasetDropdown().expand();
            assertEquals(dropdown.getDatasetGroups().get("UNMAPPED"), singletonList(DATASET_OPPORTUNITY));
            assertEquals(dropdown.getTooltipFromUnmappedGroup(), "These datasets don't map to any table in ADS. " +
                    "Selecting them can lead to runtime error if mapping is not fixed.");

        } finally {
            getProcessService().removeSchedule(schedule);

            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void loadFailedWithIncorrectMappingDataset() {
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, "DROP TABLE opportunity;"));

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            String errorMessage = initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getLastExecutionHistoryItem()
                    .getErrorMessage();
            assertEquals(errorMessage, "Mapping validation failed, see log for more details.");

        } finally {
            getProcessService().removeSchedule(schedule);

            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void executeDataloadScheduleWithOneDataset() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_OPPORTUNITY));

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

            Attribute opportunity = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
            assertEquals(getAttributeValues(opportunity), OPPORTUNITY_ATTR_VALUES);

            Attribute person = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertEquals(getAttributeValues(person), emptyList());

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnMethods = {"executeDataloadScheduleWithOneDataset"})
    public void executeDataloadScheduleWithAllDatasets() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

            Attribute opportunity = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
            assertEquals(getAttributeValues(opportunity), OPPORTUNITY_ATTR_VALUES);

            Attribute person = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertEquals(getAttributeValues(person), PERSON_ATTR_VALUES);

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }
}
