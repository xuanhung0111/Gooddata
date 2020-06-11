package com.gooddata.qa.utils.lcm;

import com.gooddata.sdk.service.project.ProjectService;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.google.common.collect.Sets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import static java.util.Objects.isNull;

public class LcmRestUtils {

    public static final String ATT_LCM_DATA_PRODUCT = "att_lcm_default_data_product";

    public static void deleteClient(final RestClient restClient, final String domain, final String clientId,
                                    final String segmentUrl) {
        final String deleteUri = String.format(segmentUrl, domain, clientId);
        restClient.execute(RestRequest.initDeleteRequest(deleteUri), HttpStatus.NO_CONTENT);
    }

    public static void deleteClient(final RestClient restClient, final String domain, final String clientId) {
        deleteClient(restClient, domain, clientId, "/gdc/domains/%s/dataproducts/att_lcm_default_data_product/clients/%s");
    }

    public static void deleteClientDefault(final RestClient restClient, final String domain, final String clientId) {
        deleteClient(restClient, domain, clientId, "/gdc/domains/%s/dataproducts/default/clients/%s");
    }

    public static String getMasterProjectId(final RestClient restClient, final String domain, final String segmentId,
                                            final String segmentUrl) {
        try {
            final String segment = String.format(segmentUrl, domain, segmentId);
            final String masterUri = getJsonObject(restClient, segment).getJSONObject("segment").getString("masterProject");
            return masterUri.split("/gdc/projects/")[1];
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getMasterProjectId(final RestClient restClient, final String domain, final String segmentId) {
        return  getMasterProjectId(restClient, domain, segmentId,
                "/gdc/domains/%s/dataproducts/att_lcm_default_data_product/segments/%s");
    }

    public static String getMasterProjectIdDefault(final RestClient restClient, final String domain, final String segmentId) {
        return  getMasterProjectId(restClient, domain, segmentId,
                "/gdc/domains/%s/dataproducts/default/segments/%s");
    }

    public static Map<String, Set<String>> getClientProjectIds(final RestClient restClient, final String domain,
                                                               final String segmentId, final String segmentUrl) {
        try {
            final String segment = String.format(segmentUrl, domain, segmentId);
            final JSONArray clients = getJsonObject(restClient, segment).getJSONObject("clients").getJSONArray("items");
            Map<String, Set<String>> clientProjects = new HashMap<>();
            clients.forEach(client -> {
                JSONObject obj = (JSONObject) client;
                JSONObject item = obj.getJSONObject("client");
                final String id = item.getString("id");
                final String clientProjectUri = item.getString("project");

                if (clientProjects.containsKey(id)) {
                    clientProjects.get(id).add(clientProjectUri.split("/gdc/projects/")[1]);
                } else {
                    clientProjects.put(id, Sets.newHashSet(clientProjectUri.split("/gdc/projects/")[1]));
                }
            });
            return clientProjects;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Set<String>> getClientProjectIds(final RestClient restClient, final String domain,
                                                               final String segmentId) {
        return getClientProjectIds(restClient, domain, segmentId, "/gdc/domains/%s/dataproducts/att_lcm_default_data_product/clients?segment=%s&limit=1000");
    }

    public static Map<String, Set<String>> getClientProjectIdsDefault(final RestClient restClient, final String domain,
                                                                      final String segmentId) {
        return getClientProjectIds(restClient, domain, segmentId, "/gdc/domains/%s/dataproducts/default/clients?segment=%s&limit=1000");
    }

    /**
     * Delete a segment will also delete all related objects involved into the segment: segment master project,
     * client ids, client projects
     * @param restClient
     * @param segmentId
     */
    public static void deleteSegment(final RestClient restClient, final String domain, final String segmentId) {
        try {
            //delete clients projects
            final ProjectService service = restClient.getProjectService();
            getClientProjectIds(restClient, domain, segmentId).forEach((k, v) -> {
                v.forEach(clientProj -> {
                    service.removeProject(service.getProjectById(clientProj));
                });
                deleteClient(restClient, domain, k);
            });
            //delete master project
            final String masterProject = getMasterProjectId(restClient, domain, segmentId);
            service.removeProject(service.getProjectById(masterProject));
            //delete segment
            final String segment = String.format("/gdc/domains/%s/dataproducts/%s/segments/%s",
                    domain, ATT_LCM_DATA_PRODUCT, segmentId);
            restClient.execute(RestRequest.initDeleteRequest(segment), HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void deleteSegmentDefault(final RestClient restClient, final String domain, final String segmentId) {
        try {
            //delete clients projects
            final ProjectService service = restClient.getProjectService();
            getClientProjectIdsDefault(restClient, domain, segmentId).forEach((k, v) -> {
                v.forEach(clientProj -> {
                    service.removeProject(service.getProjectById(clientProj));
                });
                deleteClientDefault(restClient, domain, k);
            });
            //delete master project
            final String masterProject = getMasterProjectIdDefault(restClient, domain, segmentId);
            service.removeProject(service.getProjectById(masterProject));
            //delete segment
            final String segment = String.format("/gdc/domains/%s/dataproducts/default/segments/%s", domain, segmentId);
            restClient.execute(RestRequest.initDeleteRequest(segment), HttpStatus.NO_CONTENT);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Delete a segment will also delete all related objects involved into the segment: segment master project,
     * client ids, client projects
     * @param restClient
     * @param segments
     */
    public static void deleteSegments(final RestClient restClient, final String domain, final Set<String> segments) {
        segments.forEach(segment -> deleteSegment(restClient, domain, segment));
    }

    private static JSONObject getJsonObject(final RestClient restClient, String resourceUri) throws IOException {
        final String contentString = getResource(
                restClient,
                RestRequest.initGetRequest(resourceUri),
                req -> req.setHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()),
                HttpStatus.OK);

        return new JSONObject(contentString);
    }

    /**
     * Get resource from request with expected status code
     *
     * @param request
     * @param setupRequest        setup request before executing like configure header, ...
     * @param expectedStatusCode
     * @return entity from response in String form
     */
    private static String getResource(final RestClient restClient, final HttpRequestBase request,
                                      final Consumer<HttpRequestBase> setupRequest,
                                      final HttpStatus expectedStatusCode)
            throws ParseException, IOException {
        setupRequest.accept(request);

        try {
            final HttpResponse response = restClient.execute(request, expectedStatusCode);
            final HttpEntity entity = response.getEntity();

            final String ret = isNull(entity) ? "" : EntityUtils.toString(entity);
            EntityUtils.consumeQuietly(entity);
            return ret;

        } finally {
            request.releaseConnection();
        }
    }
}
