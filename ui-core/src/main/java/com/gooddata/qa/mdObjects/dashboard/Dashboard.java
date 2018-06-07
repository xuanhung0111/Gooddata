package com.gooddata.qa.mdObjects.dashboard;

import com.gooddata.qa.mdObjects.MdObject;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Dashboard implements MdObject {

    private String name = "empty";
    private List<JSONObject> tabs = new ArrayList<>();
    private List<JSONObject> filters = new ArrayList<>();

    public Dashboard setName(String name) {
        this.name = name;
        return this;
    }

    public Dashboard addTab(Tab tab) {
        tabs.add(tab.getMdObject());
        return this;
    }

    public Dashboard addFilter(FilterItemContent filter) {
        filters.add(filter.getMdObject());
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public JSONObject getMdObject() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("projectDashboard", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("tabs", new JSONArray(tabs));
                    put("filters", new JSONArray(filters));
                }});
                put("meta", new JSONObject() {{
                    put("title", name);
                }});
            }});
        } catch (JSONException e) {
            handleJSONException(e);
        }

        return obj;
    }
}
