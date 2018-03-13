package com.gooddata.qa.utils.http.fact;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.springframework.http.HttpStatus;

import java.util.stream.*;

import static java.lang.String.format;

public class FactRestRequest extends CommonRestRequest{

    private static final String RESTRICT_API_TEMPLATE = "/gdc/md/%s/objects/setFlag/restricted";
    private static final String FACT_RESTRICT_TEMPLATE = "{\"setRestricted\": {\"items\": [%s]}}";

    private static final String UNSET_RESTRICT_API_TEMPLATE = "/gdc/md/%s/objects/unsetFlag/restricted";
    private static final String UNSET_FACT_RESTRICT_TEMPLATE = "{\"unsetRestricted\": {\"items\": [%s]}}";

    private static final String QUOTE_TEMPLATE = "\"%s\"";

    public FactRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Set flag 'restricted' to fact
     * @param factsUri
     */
    public void setFactRestricted(String... factsUri) {
        restClient.execute(
                RestRequest.initPostRequest(
                        format(RESTRICT_API_TEMPLATE, projectId),
                        format(FACT_RESTRICT_TEMPLATE, quoteUris(factsUri))),
                HttpStatus.NO_CONTENT);
    }

    /**
     * Unset flag 'restricted' to fact
     * @param factsUri
     */
    public void unsetFactRestricted(String... factsUri) {
        restClient.execute(
                RestRequest.initPostRequest(
                        format(UNSET_RESTRICT_API_TEMPLATE, projectId),
                        format(UNSET_FACT_RESTRICT_TEMPLATE, quoteUris(factsUri))),
                HttpStatus.NO_CONTENT);
    }

    private static String quoteUris (String... uris) {
        return Stream.of(uris)
                .map(uri -> String.format(QUOTE_TEMPLATE, uri))
                .collect(Collectors.joining(","));
    }
}
