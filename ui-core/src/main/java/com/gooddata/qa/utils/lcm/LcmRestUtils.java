package com.gooddata.qa.utils.lcm;

import com.gooddata.project.ProjectService;
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
    private static final String DEFAULT_DOMAIN_SEGMENT_LINK = "/gdc/domains/default/dataproducts/default/segments";
    private static final String DEFAULT_DOMAIN_CLIENT_LINK = "/gdc/domains/default/dataproducts/default/clients";

    public static void deleteClient(final RestClient restClient, final String clientId) {
        final String deleteUri = DEFAULT_DOMAIN_CLIENT_LINK + "/" + clientId;
        restClient.execute(RestRequest.initDeleteRequest(deleteUri), HttpStatus.NO_CONTENT);
    }

    public static String getMasterProjectId(final RestClient restClient, final String segmentId) {
        try {
            final String segment = DEFAULT_DOMAIN_SEGMENT_LINK + "/" + segmentId;
            final String masterUri = getJsonObject(restClient, segment).getJSONObject("segment").getString("masterProject");
            return masterUri.split("/gdc/projects/")[1];
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Map<String, Set<String>> getClientProjectIds(final RestClient restClient, final String segmentId) {
        try {
            final String segment = String.format("/gdc/domains/default/clients?segment=%s&limit=1000",
                    segmentId);
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

    /**
     * Delete a segment will also delete all related objects involved into the segment: segment master project,
     * client ids, client projects
     * @param restClient
     * @param segmentId
     */
    public static void deleteSegment(final RestClient restClient, final String segmentId) {
        try {
            //delete clients projects
            final ProjectService service = restClient.getProjectService();
            getClientProjectIds(restClient, segmentId).forEach((k, v) -> {
                v.forEach(clientProj -> {
                    service.removeProject(service.getProjectById(clientProj));
                });
                deleteClient(restClient, k);
            });
            //delete master project
            final String masterProject = getMasterProjectId(restClient, segmentId);
            service.removeProject(service.getProjectById(masterProject));
            //delete segment
            final String segment = DEFAULT_DOMAIN_SEGMENT_LINK + "/" + segmentId;
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
    public static void deleteSegments(final RestClient restClient, final Set<String> segments) {
        segments.forEach(segment -> deleteSegment(restClient, segment));
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
