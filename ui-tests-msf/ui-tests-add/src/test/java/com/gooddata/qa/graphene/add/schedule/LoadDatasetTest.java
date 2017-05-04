package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.executeProcess;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.AdsTable;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.model.SqlBuilder;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class LoadDatasetTest extends AbstractDataloadProcessTest {

    private static final String CLIENT_ID = "GoodData-Client-ID";

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void setupLdm() throws JSONException, IOException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .buildMaql());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void testAdsHasClientIdColumn() throws ParseException, JSONException, IOException {
        getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId(), CLIENT_ID);

        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_CLIENT_ID_COLUMN))
                .rows("OOP1", "100", CLIENT_ID)
                .rows("OOP2", "200", CLIENT_ID)
                .rows("OPP3", "300", "");

        AdsTable opportunityTable = new AdsTable(DATASET_OPPORTUNITY)
                .withAttributes(ATTR_OPPORTUNITY)
                .withFacts(FACT_PRICE)
                .hasClientId(true)
                .withDataFile(opportunity);

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                new SqlBuilder().withAdsTable(opportunityTable).build());

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage().openCreateScheduleForm().enterScheduleName(schedule).schedule();

        try {
            ScheduleDetail.getInstance(browser).executeSchedule().waitForExecutionFinish();

            Attribute opportunityAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
            assertEquals(getAttributeValues(opportunityAttr), asList("OOP1", "OOP2"));

        } finally {
            getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void testAdsHasClientIdColumnButNotInOutputStage() throws IOException {
        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_CLIENT_ID_COLUMN))
                .rows("OOP1", "100", CLIENT_ID)
                .rows("OOP2", "200", CLIENT_ID);

        AdsTable opportunityTable = new AdsTable(DATASET_OPPORTUNITY)
                .withAttributes(ATTR_OPPORTUNITY)
                .withFacts(FACT_PRICE)
                .hasClientId(true)
                .withDataFile(opportunity);

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                new SqlBuilder().withAdsTable(opportunityTable).build());

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage().openCreateScheduleForm().enterScheduleName(schedule).schedule();

        try {
            ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser)
                    .executeSchedule().waitForExecutionFinish();

            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getErrorMessage(),
                    format("client_id for project %s is not set although output stage table associated"
                    + " with dataset dataset.%s has discriminator column.",
                    testParams.getProjectId(), DATASET_OPPORTUNITY));

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @DataProvider
    public Object[][] setClientIdInOutputStageProvider() {
        return new Object[][] {{true}, {false}};
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "setClientIdInOutputStageProvider")
    public void testAdsNotHaveClientIdColumn(boolean shouldSetClientId) throws ParseException, JSONException, IOException {
        if (shouldSetClientId) {
            getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId(), CLIENT_ID);
        }

        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE))
                .rows("OOP11", "100")
                .rows("OOP22", "200");

        AdsTable opportunityTable = new AdsTable(DATASET_OPPORTUNITY)
                .withAttributes(ATTR_OPPORTUNITY)
                .withFacts(FACT_PRICE)
                .withDataFile(opportunity);

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                new SqlBuilder().withAdsTable(opportunityTable).build());

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage().openCreateScheduleForm().enterScheduleName(schedule).schedule();

        try {
            ScheduleDetail.getInstance(browser).executeSchedule().waitForExecutionFinish();

            Attribute opportunityAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
            assertEquals(getAttributeValues(opportunityAttr), asList("OOP11", "OOP22"));

        } finally {
            if (shouldSetClientId) {
                getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
            }
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void setOutputStateWithPrefix() throws ParseException, JSONException, IOException {
        final String prefix = "gDC_";
        getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId(), "", prefix);

        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE))
                .rows("OOP111", "100")
                .rows("OOP222", "200");

        AdsTable opportunityTable = new AdsTable(prefix + DATASET_OPPORTUNITY)
                .withAttributes(ATTR_OPPORTUNITY)
                .withFacts(FACT_PRICE)
                .withDataFile(opportunity);

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                new SqlBuilder().withAdsTable(opportunityTable).build());

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage().openCreateScheduleForm().enterScheduleName(schedule).schedule();

        try {
            ScheduleDetail.getInstance(browser).executeSchedule().waitForExecutionFinish();

            Attribute opportunityAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_OPPORTUNITY));
            assertEquals(getAttributeValues(opportunityAttr), asList("OOP111", "OOP222"));

        } finally {
            getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }
}
