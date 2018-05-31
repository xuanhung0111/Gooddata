package com.gooddata.qa.graphene.add;

import static org.testng.Assert.assertEquals;
import static java.lang.String.format;

import java.io.IOException;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.InvalidStatusCodeException;
import com.gooddata.warehouse.Warehouse;

public class SqlDiffTest extends AbstractDataloadProcessTest {

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void setupLdm() throws JSONException, IOException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .buildMaql());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void verifyInCaseNoOutputStagePrefix() throws ParseException, JSONException, IOException {
        assertEquals(getSqlDiffFromOutputStage(),
                getExpectedSqlDiff().replace("${table}", DATASET_OPPORTUNITY));
    }

    @Test(dependsOnGroups = {"precondition"})
    public void verifyInCaseOutputStagePrefix() throws ParseException, JSONException, IOException {
        final String prefix = "gDc_";
        adsHelper.associateAdsWithProject(ads, testParams.getProjectId(), "", prefix);

        try {
            assertEquals(getSqlDiffFromOutputStage(),
                    getExpectedSqlDiff().replace("${table}", prefix + DATASET_OPPORTUNITY));
        } finally {
            adsHelper.associateAdsWithProject(ads);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void setupWrongAdsInstance() throws ParseException, JSONException, IOException {
        String anotherUser = createAndAddUserToProject(UserRoles.ADMIN);

        final AdsHelper anotherAdsHelper = new AdsHelper(new RestClient(new RestClient.RestProfile(testParams
                .getHost(), anotherUser, testParams.getPassword(), true)), testParams.getProjectId());
        final Warehouse anotherAdsInstance = anotherAdsHelper.createAds("Another DDP - ADS instance", getAdsToken());
        try {
            adsHelper.associateAdsWithProject(anotherAdsInstance);
        } catch (InvalidStatusCodeException e) {
            assertEquals(e.getStatusCode(), 403);
        } finally {
            anotherAdsHelper.removeAds(anotherAdsInstance);
        }
    }

    private String getSqlDiffFromOutputStage() throws JSONException, IOException {
        final String uri = format("/gdc/dataload/projects/%s/outputStage/sqlDiff", testParams.getProjectId());
        final CommonRestRequest restRequest = new CommonRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        final String pollingUri = restRequest.getJsonObject(uri, HttpStatus.ACCEPTED)
                .getJSONObject("asyncTask")
                .getJSONObject("link")
                .getString("poll");

        CommonRestRequest request = new CommonRestRequest(
                new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        request.waitingForAsyncTask(pollingUri);
        return request.getResource(pollingUri, HttpStatus.OK).trim();
    }

    private String getExpectedSqlDiff() {
        return new StringBuilder()
                .append("--------------------------------------------------\n")
                .append("-- ${table} --\n")
                .append("--------------------------------------------------\n")
                .append("CREATE TABLE \"${table}\" (\n")
                .append("    \"a__opportunity\" VARCHAR(512),\n")
                .append("    \"f__price\" NUMERIC(12,2)\n")
                .append("\n")
                .append("-- Uncomment only if you really know the consequences of your decision\n")
                .append("-- ,    \"x__client_id\" VARCHAR(128) ENCODING RLE\n")
                .append("-- ,    \"x__timestamp\" TIMESTAMP ENCODING RLE\n")
                .append("-- ,    \"x__deleted\" BOOLEAN DEFAULT false ENCODING RLE\n")
                .append(");\n")
                .append("--if Vertica optimization is required, then: 1) replace \");\" with \"\"; 2) "
                        + "replace \"--verticaOpt \" with \"\".\n")
                .append("--Align the usage of x__client_id and x__timestamp columns in the statements below "
                        + "with their real presence in the table.\n")
                .append("--verticaOpt )\n")
                .append("--verticaOpt order by \"x__client_id\", \"x__timestamp\"\n")
                .append("--verticaOpt segmented by hash(\"a__opportunity\",\"f__price\",\"x__client_id\",\"x__deleted\","
                        + "\"x__timestamp\") all nodes;")
                .toString();
    }
}
