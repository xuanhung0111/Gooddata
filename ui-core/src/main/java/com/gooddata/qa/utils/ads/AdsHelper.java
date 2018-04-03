package com.gooddata.qa.utils.ads;

import static com.gooddata.qa.utils.http.RestRequest.initPutRequest;
import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import com.gooddata.GoodDataException;
import com.gooddata.project.Environment;
import com.gooddata.warehouse.Warehouse;
import org.springframework.http.HttpStatus;

public final class AdsHelper extends CommonRestRequest {

    public static final String ADS_DB_CONNECTION_URL = "jdbc:gdc:datawarehouse://%s/gdc/datawarehouse/instances/%s";
    public static final String ADS_INSTANCES_URI = "/gdc/datawarehouse/instances/";
    public static final String ADS_INSTANCE_SCHEMA_URI = ADS_INSTANCES_URI + "%s/schemas/default";
    public static final String OUTPUT_STAGE_URI = "/gdc/dataload/projects/%s/outputStage/";
    public static final String OUTPUT_STAGE_METADATA_URI = OUTPUT_STAGE_URI + "metadata";
    public static final String ADS_INSTANCES_USERS_URI = ADS_INSTANCES_URI + "%s/users";

    private static final String ACCEPT_HEADER_VALUE_WITH_VERSION = "application/json; version=1";
    private static final String ADD_USER_CONTENT_BODY;

    static {
        try {
            ADD_USER_CONTENT_BODY = new JSONObject() {{
                put("user", new JSONObject() {{
                    put("role", "${role}");
                    put("login", "${email}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    public AdsHelper(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    public Warehouse createAds(String adsName, String adsToken) {
        return createAds(adsName, adsToken, Environment.TESTING);
    }

    public Warehouse createAds(String adsName, String adsToken, Environment env) {
        final Warehouse adsInstance = new Warehouse(adsName, adsToken);
        adsInstance.setEnvironment(env);
        return restClient.getWarehouseService().createWarehouse(adsInstance).get();
    }

    public void removeAds(Warehouse adsInstance) throws ParseException, JSONException, IOException {
        if (adsInstance != null) {
            try {
                restClient.getWarehouseService().removeWarehouse(adsInstance);
            } catch (GoodDataException e) {
                if (e.getCause().getMessage().contains("cannot be deleted because projects")) {
                    List<String> pids = findReferencedProjects(e.getCause().getMessage());
                    removeReferencedProjects(pids);
                    restClient.getWarehouseService().removeWarehouse(adsInstance);
                    return;
                }
                throw e;
            }
        }
    }

    public void associateAdsWithProject(Warehouse adsInstance) throws JSONException, ParseException, IOException {
        associateAdsWithProject(adsInstance, projectId, "", "");
    }

    public void associateAdsWithProject(Warehouse adsInstance, String projectId, String clientId, String prefix)
            throws JSONException, ParseException, IOException {
        final JSONObject outputStageObj = new JSONObject() {{
            put("outputStage", new JSONObject() {{
                put("schema", format(ADS_INSTANCE_SCHEMA_URI, adsInstance.getId()));
                put("clientId", clientId);
                put("outputStagePrefix", prefix);
            }});
        }};

        executeRequest(initPutRequest(format(OUTPUT_STAGE_URI, projectId),
                outputStageObj.toString()), HttpStatus.OK);
    }

    /**
     * @param errorMessage for e.g: "Instance 'a45e21452dceca06598e4cb4783e9130' cannot be deleted because
     * projects /gdc/c4/project/njhvfube8fld2wz8zvthntvvbdfwtysc,/gdc/c4/project/j0s8quwgkwwsfs7m60a1c0ay8glf7858
     * reference it"
     * @return List<String> pids
     */
    private List<String> findReferencedProjects(String errorMessage) {
        Pattern p = Pattern.compile("/gdc/c4/project/(?<pid>\\w+)");
        Matcher m = p.matcher(errorMessage);
        List<String> pids = new ArrayList<>();
        while (m.find()) {
            pids.add(m.group("pid"));
        }
        return pids;
    }

    private void removeReferencedProjects(List<String> pids) throws ParseException, JSONException, IOException {
        for (String pid : pids) {
            resetOutputStageOfProject(pid);
        }
    }

    private void resetOutputStageOfProject(String projectId)
            throws JSONException, ParseException, IOException {
        final JSONObject outputStageObj = new JSONObject() {{
            put("outputStage", new JSONObject() {{
                put("schema", JSONObject.NULL);
                put("clientId", JSONObject.NULL);
                put("outputStagePrefix", JSONObject.NULL);
            }});
        }};

        executeRequest(initPutRequest(format(OUTPUT_STAGE_URI, projectId),
                outputStageObj.toString()), HttpStatus.OK);
    }
}
