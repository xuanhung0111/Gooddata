package com.gooddata.qa.graphene.fragments.greypages.md.obj;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectExecutionContext extends ObjectFragment {
    public String getType() throws JSONException, InterruptedException {
        JSONObject content = getObject()
                .getJSONObject("executionContext")
                .getJSONObject("content");

        return content.getString("type");
    }
}
