package com.gooddata.qa.mdObjects.dashboard.tab;

import com.gooddata.qa.mdObjects.MdObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Tab implements MdObject {
    private String title = "First Tab";
    private List<JSONObject> items = new ArrayList<>();

    public Tab addItem(TabItem item) {
        items.add(item.getMdObject());
        return this;
    }

    public Tab addItems(List<TabItem> tabItems) {
        items.addAll(tabItems.stream().map(TabItem::getMdObject).collect(toList()));
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public JSONObject getMdObject() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("title", title).put("items", new JSONArray(items));
        } catch (JSONException e) {
            handleJSONException(e);
        }
        return obj;
    }
}
