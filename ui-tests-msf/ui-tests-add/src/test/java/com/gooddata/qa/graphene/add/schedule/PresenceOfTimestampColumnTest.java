package com.gooddata.qa.graphene.add.schedule;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.md.MetadataService;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DatasetDropdown;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.gooddata.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class PresenceOfTimestampColumnTest extends AbstractDataloadProcessTest {

    private static final String NON_EXISTING_DATASETS_MESSAGE = "This schedule is set to load data to datasets " +
            "that are not in the project anymore: %s. Remove the datasets from schedule or add datasets " +
            "back to project.";

    private String maql;

    private CsvFile opportunity;
    private CsvFile person;

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        maql = new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .withDataset(new Dataset(DATASET_PERSON)
                        .withAttributes(ATTR_PERSON)
                        .withFacts(FACT_AGE))
                .buildMaql();
        setupMaql(maql);

        opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE))
                .rows("OP_INIT", "100");

        person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("P_INIT", "18", parseDateTime(LocalDateTime.now(), TIMESTAMP_FORMAT));

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void listDatasetBelowSpecificGroup() {
        Map<String, List<String>> datasetGroups = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectCustomDatasetsOption()
                .getDatasetDropdown()
                .expand()
                .getDatasetGroups();
        assertEquals(datasetGroups.get("FULL LOAD"), singletonList(DATASET_OPPORTUNITY));
        assertEquals(datasetGroups.get("INCREMENTAL LOAD"), singletonList(DATASET_PERSON));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void showTooltipForSpecificItem() {
        DatasetDropdown dropdown = initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectCustomDatasetsOption()
                .getDatasetDropdown()
                .expand();
        assertEquals(dropdown.getTooltipFromIncrementalGroup(), "Only the data with a timestamp older than the " +
                "timestamp of the last successfully loaded data (noted next to the dataset name) will be extracted " +
                "from the source table and loaded to the datasets. The data will be loaded in incremental mode.");

        assertEquals(dropdown.getTooltipFromUnloadedDataset(DATASET_PERSON), "Incremental mode has not been used " +
                "yet for loading data into this dataset. All the data from the source table will be loaded " +
                "into this dataset in full mode.");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkErrorOnUmmapedDatasets() {
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, "DROP TABLE opportunity;DROP TABLE person;"));

        try {
            DatasetDropdown dropdown = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .selectCustomDatasetsOption()
                    .getDatasetDropdown()
                    .expand();
            assertEquals(dropdown.getTooltipFromUnmappedGroup(), "These datasets don't map to any table in ADS. " +
                    "Selecting them can lead to runtime error if mapping is not fixed.");
            assertThat(dropdown.getDatasetGroups().get("UNMAPPED"),
                    hasItems(DATASET_OPPORTUNITY, DATASET_PERSON));

        } finally {
            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void autoSelectMappedDatasetsWhenClickOnLink() {
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, "DROP TABLE opportunity;"));

        try {
            DatasetDropdown dropdown = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                    .selectCustomDatasetsOption()
                    .getDatasetDropdown()
                    .expand()
                    .selectMappedDatasetsByLink();
            assertEquals(dropdown.getSelectedDatasets(), singletonList(DATASET_PERSON));

        } finally {
            executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void updateScheduleWhenSelectedDatasetIsDeleted() throws IOException, JSONException {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(),
                SyncDatasets.custom(DATASET_OPPORTUNITY, DATASET_PERSON));
        deleteDatasets(DATASET_OPPORTUNITY);

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            assertEquals(scheduleDetail.getNonExistingDatasetsMessage(),
                    format(NON_EXISTING_DATASETS_MESSAGE, "dataset." + DATASET_OPPORTUNITY));

            DatasetDropdown dropdown = scheduleDetail.getDatasetDropdown();
            assertTrue(dropdown.isDisabled(), "Dataset dropdown is not disabled");
            assertEquals(dropdown.getButtonText(), "2 datasets");

            scheduleDetail.removeNonExistingDatasets();
            assertFalse(dropdown.isDisabled(), "Dataset dropdown is not enabled after remove non existing dataset");
            assertEquals(dropdown.getButtonText(), "1 of 1 datasets");
            assertEquals(dropdown.expand().getAvailableDatasets(), singletonList(DATASET_PERSON));

        } finally {
            getProcessService().removeSchedule(schedule);
            setupMaql(new LdmModel()
                    .withDataset(new Dataset(DATASET_OPPORTUNITY)
                            .withAttributes(ATTR_OPPORTUNITY)
                            .withFacts(FACT_PRICE))
                    .buildMaql());
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void updateScheduleWhenAllSelectedDatasetsAreDeleted() throws IOException, JSONException {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(),
                SyncDatasets.custom(DATASET_OPPORTUNITY, DATASET_PERSON));
        deleteDatasets(DATASET_OPPORTUNITY, DATASET_PERSON);

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            assertEquals(scheduleDetail.getNonExistingDatasetsMessage(),
                    format(NON_EXISTING_DATASETS_MESSAGE,
                            format("dataset.%s,dataset.%s", DATASET_OPPORTUNITY, DATASET_PERSON)));

            DatasetDropdown dropdown = scheduleDetail.getDatasetDropdown();
            assertTrue(dropdown.isDisabled(), "Dataset dropdown is not disabled");
            assertEquals(dropdown.getButtonText(), "2 datasets");

            scheduleDetail.removeNonExistingDatasets();
            assertFalse(dropdown.isDisabled(), "Dataset dropdown is not enabled after remove non existing dataset");
            assertEquals(dropdown.getButtonText(), "0 of 0 datasets");
            assertEquals(dropdown.expand().getAvailableDatasets(), EMPTY_LIST);

        } finally {
            getProcessService().removeSchedule(schedule);
            setupMaql(maql);
        }
    }

    private void deleteDatasets(String... datasets) {
        MetadataService mdService = getMdService();
        Stream.of(datasets).forEach(dataset -> mdService.removeObj(
                mdService.getObj(getProject(), com.gooddata.md.Dataset.class, title(dataset))));
    }
}
