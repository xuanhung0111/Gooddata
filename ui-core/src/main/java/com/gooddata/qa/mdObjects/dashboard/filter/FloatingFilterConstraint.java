package com.gooddata.qa.mdObjects.dashboard.filter;

import org.json.JSONException;
import org.json.JSONObject;

public class FloatingFilterConstraint implements FilterConstraint {

    private String type;
    private int from;
    private int to;

    private FloatingFilterConstraint(String type, int from, int to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public FloatingFilterConstraint(int from, int to) {
        this("floating", from, to);
    }

    @Override
    public JSONObject getMdObject() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", type).put("from", from).put("to", to);
        } catch (JSONException e) {
            handleJSONException(e);
        }

        return obj;
    }
}
