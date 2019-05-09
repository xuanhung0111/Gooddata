package com.gooddata.qa.mdObjects.dashboard.tab;

import org.json.JSONException;
import org.json.JSONObject;

public class Widget extends TabItem {

    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;
    private String showMap;

    @Override
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    @Override
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    @Override
    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    @Override
    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public Widget setShowMap(String showMap) {
        this.showMap = showMap;
        return this;
    }

    @Override
    protected JSONObject initPayload() throws JSONException {
        JSONObject positionJson = new JSONObject();
        positionJson.put("positionX", positionX).put("positionY", positionY)
                .put("sizeX", sizeX).put("sizeY", sizeY)
                .put("showMap", showMap);
        JSONObject obj = new JSONObject();

        try {
            obj.put("geoChartItem", positionJson);
        } catch (JSONException e) {
            handleJSONException(e);
        }
        return obj;
    }

    public Widget setWidgetTopMiddle() {
        setShowMap("1").setPosition(ItemPosition.TOP_MIDDLE);
        return this;
    }

    @Override
    protected void getSizeByItemType() {
        mapItemSize(ItemSize.WIDGET_ITEM);
    }
}
