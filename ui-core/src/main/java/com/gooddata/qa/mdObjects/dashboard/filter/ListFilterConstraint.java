package com.gooddata.qa.mdObjects.dashboard.filter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListFilterConstraint implements FilterConstraint {

    private String type;
    private List<String> elementUrisToFiltered = new ArrayList<>();

    private ListFilterConstraint(String type, List<String> elementUrisToFiltered) {
        this.type = type;
        this.elementUrisToFiltered = elementUrisToFiltered;
    }

    public ListFilterConstraint(List<String> elementUrisToFiltered) {
        this("list", elementUrisToFiltered);
    }

    @Override
    public JSONObject getMdObject() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("type", type).put("elements", elementUrisToFiltered);
        } catch (JSONException e) {
            handleJSONException(e);
        }

        return obj;
    }
}
