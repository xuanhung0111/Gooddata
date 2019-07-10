package com.gooddata.qa.utils.http.attribute;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.springframework.http.HttpStatus;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

public class AttributeRestRequest extends CommonRestRequest {
    private static final String SET_ATTRIBUTE_PROTECT_TEMPLATE = "{\"setProtected\": {\"items\": [%s]}}";
    private static final String SET_PROTECT_API_TEMPLATE = "/gdc/md/%s/objects/setFlag/protected";

    private static final String UNSET_PROTECT_API_TEMPLATE = "/gdc/md/%s/objects/unsetFlag/protected";
    private static final String UNSET_ATTRIBUTE_PROTECT_TEMPLATE = "{\"unsetProtected\": {\"items\": [%s]}}";

    private static final String QUOTE_TEMPLATE = "\"%s\"";

    public AttributeRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Set flag 'protected' to attribute
     * @param attributesUri
     */
    public void setAttributeProtected(String... attributesUri) {
        restClient.execute(
                RestRequest.initPostRequest(
                        format(SET_PROTECT_API_TEMPLATE, projectId),
                        format(SET_ATTRIBUTE_PROTECT_TEMPLATE, quoteUris(attributesUri))),
                HttpStatus.NO_CONTENT);
    }

    /**
     * Unset flag 'protected' to attribute
     * @param attributesUri
     */
    public void unsetAttributeProtected(String... attributesUri) {
        restClient.execute(
                RestRequest.initPostRequest(
                        format(UNSET_PROTECT_API_TEMPLATE, projectId),
                        format(UNSET_ATTRIBUTE_PROTECT_TEMPLATE, quoteUris(attributesUri))),
                HttpStatus.NO_CONTENT);
    }

    private static String quoteUris (String... uris) {
        return Stream.of(uris)
                .map(uri -> String.format(QUOTE_TEMPLATE, uri))
                .collect(Collectors.joining(","));
    }
}
