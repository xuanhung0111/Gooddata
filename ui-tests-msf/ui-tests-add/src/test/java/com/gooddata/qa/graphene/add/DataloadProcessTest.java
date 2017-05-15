package com.gooddata.qa.graphene.add;

import static com.gooddata.qa.utils.http.process.ProcessRestUtils.executeProcess;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.getExecutionLog;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.FutureResult;
import com.gooddata.GoodData;
import com.gooddata.GoodDataException;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.AdsTable;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.model.SqlBuilder;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.warehouse.Warehouse;

public class DataloadProcessTest extends AbstractDataloadProcessTest {

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void setupLdm() throws JSONException, IOException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .buildMaql());

        CsvFile opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE))
                .rows("OOP1", "100")
                .rows("OOP2", "200");

        AdsTable opportunityTable = new AdsTable(DATASET_OPPORTUNITY)
                .withAttributes(ATTR_OPPORTUNITY)
                .withFacts(FACT_PRICE)
                .withDataFile(opportunity);

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                new SqlBuilder().withAdsTable(opportunityTable).build());

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void autoCreateDataloadProcess() {
        assertTrue(hasDataloadProcess(), "Dataload process not found!");
        assertFalse(canCreateDataloadProcess(), "Can create more than one Dataload process!");
    }

    @Test(dependsOnGroups = {"precondition"})
    public void changeAdsInstanceWhenHavingDataloadProcess() throws ParseException, JSONException, IOException {
        final Warehouse newAds = getAdsHelper().createAds("anotherAds", getAdsToken());

        try {
            getAdsHelper().associateAdsWithProject(newAds, testParams.getProjectId());
            assertTrue(hasDataloadProcess(), "Dataload process not found after associate with other ads");

        } finally {
            getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
            getAdsHelper().removeAds(newAds);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void changeAdsInstanceAfterDeleteDataloadProcess() throws ParseException, JSONException, IOException {
        final Warehouse newAds = getAdsHelper().createAds("anotherAds", getAdsToken());

        try {
            getProcessService().removeProcess(getDataloadProcess());
            getAdsHelper().associateAdsWithProject(newAds, testParams.getProjectId());
            assertTrue(hasDataloadProcess(), "Dataload process not created after associate with other ads");

        } finally {
            getAdsHelper().associateAdsWithProject(ads, testParams.getProjectId());
            getAdsHelper().removeAds(newAds);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkProcessOwner() throws ParseException, IOException, JSONException {
        ProcessExecutionDetail executionDetail = executeProcess(getGoodDataClient(), getDataloadProcess(),
                Parameters.SYNCHRONIZE_ALL_DATASETS);
        assertThat(getExecutionLog(getGoodDataClient(), executionDetail),
                containsString("user=" + testParams.getUser()));

        final String otherUser = createAndAddUserToProject(UserRoles.ADMIN);
        final GoodData otherGoodDataClient = getGoodDataClient(otherUser, testParams.getPassword());

        ProcessExecutionDetail otherExecutionDetail = executeProcess(otherGoodDataClient, getDataloadProcess(),
                Parameters.SYNCHRONIZE_ALL_DATASETS);
        assertThat(getExecutionLog(getGoodDataClient(), otherExecutionDetail),
                containsString("user=" + testParams.getUser()));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkConcurrentDataLoadViaRestAPI() {
        FutureResult<ProcessExecutionDetail> executionDetail = tryToExecuteProcess(getDataloadProcess(),
                Parameters.SYNCHRONIZE_ALL_DATASETS);

        try {
            executeProcess(getGoodDataClient(), getDataloadProcess(), Parameters.SYNCHRONIZE_ALL_DATASETS);
        } catch (GoodDataException e) {
            assertThat(e.getCause().getMessage(), containsString("The schedule did not run because one or "
                    + "more of the datasets in this schedule is already synchronizing."));
        }

        assertTrue(executionDetail.get().isSuccess(), "The first execution is failed!");
    }

    private FutureResult<ProcessExecutionDetail> tryToExecuteProcess(DataloadProcess process,
            Map<String, String> params) {
        return getProcessService().executeProcess(new ProcessExecution(process, "", params));
    }
}
