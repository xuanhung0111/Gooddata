package com.gooddata.qa.utils.http.fact;

import com.gooddata.project.Project;
import com.gooddata.qa.utils.http.RestApiClient;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static java.lang.String.format;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;

/**
 * REST utilities for manipulating facts
 */
public class FactRestUtils {

    private static final String RESTRICT_API_TEMPLATE = "/gdc/md/%s/objects/setFlag/restricted";
    private static final String FACT_RESTRICT_TEMPLATE = "{\"setRestricted\": {\"items\": [%s]}}";

    private static final String UNSET_RESTRICT_API_TEMPLATE = "/gdc/md/%s/objects/unsetFlag/restricted";
    private static final String UNSET_FACT_RESTRICT_TEMPLATE = "{\"unsetRestricted\": {\"items\": [%s]}}";

    private static final String QUOTE_TEMPLATE = "\"%s\"";

    /**
     * Set flag 'restricted' to fact
     * @param restApiClient
     * @param project
     * @param factsUri
     */
    public static void setFactRestricted(final RestApiClient restApiClient, final Project project, final String... factsUri) {

        String facts = quoteUris(factsUri);
        final String content = format(FACT_RESTRICT_TEMPLATE, facts);
        final String uri = format(RESTRICT_API_TEMPLATE, project.getId());
        executeRequest(restApiClient, restApiClient.newPostMethod(uri, content), HttpStatus.NO_CONTENT);
    }

    /**
     * Unset flag 'restricted' to fact
     * @param restApiClient
     * @param project
     * @param factsUri
     */
    public static void unsetFactRestricted(final RestApiClient restApiClient, final Project project, final String... factsUri) {

        String facts = quoteUris(factsUri);
        final String content = format(UNSET_FACT_RESTRICT_TEMPLATE, facts);
        final String uri = format(UNSET_RESTRICT_API_TEMPLATE, project.getId());
        executeRequest(restApiClient, restApiClient.newPostMethod(uri, content), HttpStatus.NO_CONTENT);
    }

    private static String quoteUris (String... uris) {
        return Stream.of(uris)
            .map(uri -> String.format(QUOTE_TEMPLATE, uri))
            .collect(Collectors.joining(","));
    }
}
