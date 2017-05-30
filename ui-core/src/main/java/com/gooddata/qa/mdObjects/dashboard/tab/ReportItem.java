package com.gooddata.qa.mdObjects.dashboard.tab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReportItem extends TabItem {
    private String objUri;
    private List<String> appliedFilterIds = new ArrayList<>();

    public void setObjUri(String objUri) {
        this.objUri = objUri;
    }

    public void setAppliedFilterIds(List<String> appliedFilterIds) {
        this.appliedFilterIds = appliedFilterIds;
    }

    @Override
    protected JSONObject initPayload() throws JSONException {
        return new JSONObject().put("reportItem", new JSONObject() {{
            put("obj", objUri);
            put("filters", appliedFilterIds);
            put("visualization", new JSONObject() {{
                put("grid", new JSONObject().put("columnWidths", new JSONArray()));
                put("oneNumber", new JSONObject().put("labels", new JSONObject()));
            }});
            put("style", new JSONObject() {{
                put("displayTitle", 1);
                put("background", new JSONObject().put("opacity", 0));
            }});
        }});
    }

    @Override
    protected void getSizeByItemType() {
        mapItemSize(ItemSize.REPORT_ITEM);
    }
}