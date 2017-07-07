package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.md.Restriction.title;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;

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

public class DatasetDetailTest extends AbstractDataloadProcessTest {

    private static final String DATASET_OPPORTUNITY = "opportunity";
    private static final String DATASET_PERSON = "person";
    private static final String ATTR_OPPORTUNITY = "opportunity";
    private static final String ATTR_PERSON = "person";
    private static final String FACT_AGE = "age";
    private static final String FACT_PRICE = "price";

    private static final Collection<String> OPPORTUNITY_ATTR_VALUES = asList("OOP1", "OOP2");
    private static final Collection<String> PERSON_ATTR_VALUES = asList("P1", "P2");

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

        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE))
                .rows("OOP1", "100")
                .rows("OOP2", "200");

        CsvFile person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE))
                .rows("P1", "18")
                .rows("P2", "20");

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultParameters.get().addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person)));
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
