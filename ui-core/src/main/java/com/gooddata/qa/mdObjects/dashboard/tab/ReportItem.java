package com.gooddata.qa.mdObjects.dashboard.tab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ReportItem extends TabItem {
    private String objUri;
    private List<String> appliedFilterIds = new ArrayList<>();
    private ItemSize itemSize;

    public ReportItem setItemSize(ItemSize itemSize) {
        this.itemSize = itemSize;
        return this;
    }

    public ReportItem setObjUri(String objUri) {
        this.objUri = objUri;
        return this;
    }

    public ReportItem setAppliedFilterIds(List<String> appliedFilterIds) {
        this.appliedFilterIds = appliedFilterIds;
        return this;
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
        mapItemSize(itemSize != null ? ItemSize.REPORT_ITEM_CUSTOMIZE : ItemSize.REPORT_ITEM);
    }
}
