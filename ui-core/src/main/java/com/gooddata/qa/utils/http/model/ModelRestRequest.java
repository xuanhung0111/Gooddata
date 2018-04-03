package com.gooddata.qa.utils.http.model;

import com.gooddata.qa.graphene.enums.DatasetElements;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import static java.lang.String.format;

public class ModelRestRequest extends CommonRestRequest{

    private static final String LDM_LINK = "/gdc/projects/%s/ldm";
    private static final String PROJECT_MODEL_VIEW_LINK = "/gdc/projects/%s/model/view";

    private static final Logger log = Logger.getLogger(ModelRestRequest.class.getName());

    public ModelRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Get LDM image uri
     *
     * @param host
     * @return LDM image uri
     */
    public String getLDMImageURI(String host) throws ParseException, IOException, JSONException {
        final String ldmUri = format(LDM_LINK, projectId);

        String uri = getJsonObject(RestRequest.initGetRequest(ldmUri)).getString("uri");
        // TODO fix on resource rather then here
        if (uri.matches("^/gdc_img.*")) {
            uri = "https://" + host + uri;
        }
        return uri;
    }

    /**
     * Get dataset element from model view
     *
     * @param dataset
     * @param element
     * @param returnType
     * @return dataset element
     */
    public <T> T getDatasetElementFromModelView(String dataset, DatasetElements element, Class<T> returnType)
            throws ParseException, JSONException, IOException {
        Object object = getDatasetModelView(dataset).get(element.toString().toLowerCase());
        log.info(format("Get %s of dataset %s...", element, dataset));
        if(returnType.isInstance(object)) {
            return returnType.cast(object);
        }

        throw new NoSuchElementException("Dataset element not found!");
    }

    public JSONObject getDatasetModelView(String dataset) throws ParseException, JSONException, IOException {
        final JSONArray datasets = getProjectModelView()
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

    public JSONObject getProductionProjectModelView(boolean includeNonProduction) throws ParseException, JSONException, IOException {
        return getProjectModelViewByModelLink(
                format(PROJECT_MODEL_VIEW_LINK + "?includeNonProduction=%s", projectId, includeNonProduction));
    }

    private JSONObject getProjectModelView()throws ParseException, JSONException, IOException {
        return getProjectModelViewByModelLink(format(PROJECT_MODEL_VIEW_LINK, projectId));
    }

    private JSONObject getProjectModelViewByModelLink(String projectModelViewLink) throws IOException, JSONException {
        final String pollingUri = getJsonObject(projectModelViewLink, HttpStatus.ACCEPTED)
                .getJSONObject("asyncTask")
                .getJSONObject("link")
                .getString("poll");

        if (waitingForAsyncTask(pollingUri) == HttpStatus.OK.value()) {
            return getJsonObject(pollingUri);
        }

        return null;
    }
}
