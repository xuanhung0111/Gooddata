package com.gooddata.qa.utils.http.variable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gooddata.md.AbstractObj;
import com.gooddata.md.Meta;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.md.Queryable;
import com.gooddata.md.Updatable;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static java.lang.String.format;

public class VariableRestRequest extends CommonRestRequest {

    private final String VARIABLE_LINK = "/gdc/md/%s/variables/item";
    private final String GET_VARIABLE_LINK = "/gdc/md/%s/query/prompts";
    private final String CREATE_AND_GET_OBJ_LINK = "/gdc/md/%s/obj?createAndGet=true";

    public VariableRestRequest(RestClient client, String projectId) {
        super(client, projectId);
    }

    /**
     * Create filter variable, default value is all values
     *
     * @param name variable name
     * @return variable uri
     */
    public String createFilterVariable(String name, String attributeUri) {
        return createFilterVariable(name, attributeUri, "TRUE");
    }

    /**
     * Create filter variable, default value is filtered by given expression
     *
     * @param name
     * @param attributeUri attribute uri
     * @param expression   expression for default values
     * @return variable uri
     */
    public String createFilterVariable(String name, String attributeUri, String expression) {

        return createVariable(name, Prompt.PromptType.FILTER, attributeUri, expression);
    }

    /**
     * Create numeric variable
     *
     * @param name variable name
     * @param defaultValue default numeric value
     * @return variable uri
     */
    public String createNumericVariable(String name, String defaultValue) {
        return createVariable(name, Prompt.PromptType.SCALAR, null, defaultValue);
    }

    /**
     * Get variable uri. This method actually gets prompt uri, but it's still meaningful
     * because creating prompt or variable obj individually is not allowed via this REST.
     *
     * @param title
     * @return variable uri
     */
    public String getVariableUri(String title) {
        try {
            JSONArray variables = getJsonObject(format(GET_VARIABLE_LINK, projectId))
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

    private String createVariable(String name, Prompt.PromptType type, String attUri, String expression) {
        try {
            JSONObject obj;
            if (type == Prompt.PromptType.SCALAR)
                obj = Prompt.getScalarObj(name);
            else
                obj = Prompt.getFilterObj(name, attUri);

            // create prompt obj
            String variableUri = getJsonObject(
                    RestRequest.initPostRequest(
                            format(CREATE_AND_GET_OBJ_LINK, projectId),
                            obj.toString()))
                    .getJSONObject("prompt")
                    .getJSONObject("meta")
                    .getString("uri");

            //create variable obj
            executeRequest(RestRequest.initPostRequest(format(VARIABLE_LINK, projectId),
                    new JSONObject() {{
                        put("variable", new JSONObject() {{
                            put("expression", expression);
                            put("level", "project");
                            put("prompt", variableUri);
                            put("related", format("/gdc/projects/%s", projectId));
                            put("type", type.getType());
                        }});
                    }}.toString()), HttpStatus.OK);

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
