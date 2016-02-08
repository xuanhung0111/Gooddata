package com.gooddata.qa.utils.http.model;

import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.waitingForAsyncTask;
import static java.lang.String.format;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.utils.http.RestApiClient;

public final class ModelRestUtils {

    private static final Logger log = Logger.getLogger(ModelRestUtils.class.getName());

    private ModelRestUtils() {
    }

    private static final String LDM_LINK = "/gdc/projects/%s/ldm";
    private static final String PROJECT_MODEL_VIEW_LINK = "/gdc/projects/%s/model/view";

    public static String getLDMImageURI(final RestApiClient restApiClient, final String projectId,
            final String host) throws ParseException, IOException, JSONException {
        final String ldmUri = format(LDM_LINK, projectId);

        String uri = getJsonObject(restApiClient, restApiClient.newGetMethod(ldmUri)).getString("uri");
        // TODO fix on resource rather then here
        if (uri.matches("^/gdc_img.*")) {
            uri = "https://" + host + uri;
        }
        return uri;
    }

    public static JSONObject getProductionProjectModelView(final RestApiClient restApiClient, final String projectId, 
            final boolean includeProduction) throws ParseException, JSONException, IOException {
        final String nonProductionURL = PROJECT_MODEL_VIEW_LINK + "?includeNonProduction=%s";
        return getProjectModelViewByModelLink(restApiClient, format(nonProductionURL, projectId, includeProduction));
       }

    public static <T> T getDatasetElementFromModelView(final RestApiClient restApiClient, final String projectId,
            final String dataset, final DatasetElements element, final Class<T> returnType)
                    throws ParseException, JSONException, IOException {
        final Object object = getDatasetModelView(restApiClient, projectId, dataset).get(element.toString().toLowerCase());
        log.info(format("Get %s of dataset %s...", element, dataset));
        if(returnType.isInstance(object)) {
            return returnType.cast(object);
        }
        throw new NoSuchElementException("Dataset element not found!");
    }

    private static JSONObject getProjectModelView(final RestApiClient restApiClient, final String projectId) 
            throws ParseException, JSONException, IOException {
        return getProjectModelViewByModelLink(restApiClient, format(PROJECT_MODEL_VIEW_LINK, projectId));
    }

    private static JSONObject getDatasetModelView(final RestApiClient restApiClient, final String projectId,
            final String dataset) throws ParseException, JSONException, IOException {
        final JSONArray datasets = getProjectModelView(restApiClient, projectId)
                .getJSONObject("projectModelView")
                .getJSONObject("model")
                .getJSONObject("projectModel")
                .getJSONArray("datasets");

        for (int i = 0, n = datasets.length(); i < n; i++) {
            final JSONObject object = datasets.getJSONObject(i).getJSONObject("dataset");
            if (!dataset.equals(object.getString("title")))
                continue;
            return object;
        }
        throw new NoSuchElementException("Dataset json object not found!");
    }

    private static JSONObject getProjectModelViewByModelLink(final RestApiClient restApiClient,
            final String projectModelViewLink) throws ParseException, JSONException, IOException {
        final String pollingUri = getJsonObject(restApiClient, projectModelViewLink, HttpStatus.ACCEPTED)
            .getJSONObject("asyncTask")
            .getJSONObject("link")
            .getString("poll");

        if (waitingForAsyncTask(restApiClient, pollingUri) == HttpStatus.OK.value()) {
            return getJsonObject(restApiClient, pollingUri);
        }

        return null;
    }
}
