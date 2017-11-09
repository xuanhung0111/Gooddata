package com.gooddata.qa.utils.http.variable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gooddata.md.AbstractObj;
import com.gooddata.md.Meta;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.md.Queryable;
import com.gooddata.md.Updatable;
import com.gooddata.qa.utils.http.RestApiClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

public final class VariableRestUtils {

    private static final String VARIABLE_LINK = "/gdc/md/%s/variables/item";
    private static final String GET_VARIABLE_LINK = "/gdc/md/%s/query/prompts";

    private VariableRestUtils() {
        // do nothing
    }


    /**
     * Create filter variable, default value is all values
     *
     * @param restApiClient
     * @param projectId
     * @param name variable name
     * @return variable uri
     */
    public static String createFilterVariable(RestApiClient restApiClient, String projectId,
                                              String name, String attributeUri) {
        return createFilterVariable(restApiClient, projectId, name, attributeUri, "TRUE");
    }

    /**
     * Create filter variable, default value is filtered by given expression
     *
     * @param restApiClient
     * @param projectId
     * @param name
     * @param attributeUri attribute uri
     * @param expression expression for default values
     * @return variable uri
     */
    public static String createFilterVariable(RestApiClient restApiClient, String projectId,
                                              String name, String attributeUri, String expression) {
        return createVariable(restApiClient, projectId, name, Prompt.PromptType.FILTER, attributeUri, expression);
    }

    /**
     * Create numeric variable
     *
     * @param restApiClient
     * @param projectId
     * @param name variable name
     * @param defaultValue default numeric value
     * @return variable uri
     */
    public static String createNumericVariable(RestApiClient restApiClient, String projectId,
                                               String name, String defaultValue) {
        return createVariable(restApiClient, projectId, name, Prompt.PromptType.SCALAR, null, defaultValue);
    }

    /**
     * Get variable uri. This method actually gets prompt uri, but it's still meaningful
     * because creating prompt or variable obj individually is not allowed via this REST.
     *
     * @param restApiClient
     * @param projectId
     * @param title
     * @return variable uri
     */
    public static String getVariableUri(RestApiClient restApiClient, String projectId, String title) {
        try {
            JSONArray variables = getJsonObject(restApiClient, format(GET_VARIABLE_LINK, projectId))
                    .getJSONObject("query")
                    .getJSONArray("entries");

            for (int i = 0; i < variables.length(); i++) {
                if (title.equals(variables.getJSONObject(i).getString("title"))) {
                    return variables.getJSONObject(i).getString("link");
                }
            }
            throw new ObjNotFoundException(Variable.class);
        } catch (IOException | JSONException e) {
            throw new RuntimeException("there is an error while searching", e);
        }
    }

    private static String createVariable(RestApiClient restApiClient, String projectId,
                                         String name, Prompt.PromptType type, String attUri, String expression) {
        try {
            JSONObject obj;
            if (type == Prompt.PromptType.SCALAR)
                obj = Prompt.getScalarObj(name);
            else
                obj = Prompt.getFilterObj(name, attUri);

            // create prompt obj
            String variableUri = getJsonObject(restApiClient,
                    restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), obj.toString()))
                    .getJSONObject("prompt")
                    .getJSONObject("meta")
                    .getString("uri");

            //create variable obj
            getJsonObject(restApiClient, restApiClient.newPostMethod(format(VARIABLE_LINK, projectId),
                    new JSONObject() {{
                        put("variable", new JSONObject() {{
                            put("expression", expression);
                            put("level", "project");
                            put("prompt", variableUri);
                            put("related", format("/gdc/projects/%s", projectId));
                            put("type", type.getType());
                        }});
                    }}.toString()));

            return variableUri;
        } catch (JSONException | IOException e) {
            throw new RuntimeException("There is an error while creating variable", e);
        }
    }
    /**
     * this is now using for ObjNotFoundException only
     */
    private class Variable extends AbstractObj implements Queryable, Updatable {
        protected Variable(@JsonProperty("meta") Meta meta) {
            super(meta);
        }
    }
}
