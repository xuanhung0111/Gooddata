package com.gooddata.qa.utils.http.variable;

import org.json.JSONException;
import org.json.JSONObject;

// default modifier is required, this is not expected to be used anywhere else
class Prompt {

    private Prompt() {
        // do nothing
    }

    static JSONObject getScalarObj(String name) throws JSONException {
        return getPromptObject(PromptType.SCALAR, name);
    }

    static JSONObject getFilterObj(String name, String attUri) throws JSONException {
        if (attUri == null) {
            throw new IllegalArgumentException("filter prompt requires attribute uri");
        }

        JSONObject obj = getPromptObject(PromptType.FILTER, name);
        obj.getJSONObject("prompt").getJSONObject("content").put("attribute", attUri);

        return obj;
    }

    private static JSONObject getPromptObject(PromptType promptType, String name) throws JSONException {
        return new JSONObject() {{
            put("prompt", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("type", promptType.getType());
                }});
                put("meta", new JSONObject() {{
                    put("title", name);
                    put("category", "prompt");
                }});
            }});
        }};
    }

    enum PromptType {
        SCALAR("scalar"),
        FILTER("filter");

        String type;

        PromptType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
