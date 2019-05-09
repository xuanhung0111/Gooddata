package com.gooddata.qa.mdObjects.dashboard.tab;

import org.json.JSONException;
import org.json.JSONObject;

public class Text extends TabItem {

    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;
    private String text;
    private String textSize;

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

    public String getText() {
        return text;
    }

    public Text setText(String text) {
        this.text = text;
        return this;
    }

    public Text setTextSize(String textSize) {
        this.textSize = textSize;
        return this;
    }

    @Override
    protected JSONObject initPayload() throws JSONException {
        JSONObject textItemJson = new JSONObject();
        textItemJson.put("positionX", positionX).put("positionY", positionY).put("sizeX", sizeX)
                .put("sizeY", sizeY).put("text", text).put("textSize", textSize);
        JSONObject obj = new JSONObject();
        try {
            obj.put("textItem", textItemJson);
        } catch (JSONException e) {
            handleJSONException(e);
        }
        return obj;
    }

    @Override
    protected void getSizeByItemType() {
        mapItemSize(ItemSize.TEXT_ITEM);
    }


    public Text setHeadline(String text) {
        setText(text).setTextSize("middle").setPosition(ItemPosition.TOP_MIDDLE);
        return this;
    }
}
