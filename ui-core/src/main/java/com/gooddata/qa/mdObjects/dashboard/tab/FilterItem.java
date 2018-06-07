package com.gooddata.qa.mdObjects.dashboard.tab;

import net.minidev.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterItem extends TabItem {

    private String contentId = "empty";
    private String id = generateIdentifier();

    public FilterItem setContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    public String getId() {
        return id;
    }

    @Override
    protected JSONObject initPayload() throws JSONException {
        return new JSONObject().put("filterItem", new JSONObject()
                .put("id", id)
                .put("contentId", contentId)
                .put("parentFilters", new JSONArray()));
    }

    @Override
    protected void getSizeByItemType() {
        mapItemSize(ItemSize.FILTER_ITEM);
    }
}
