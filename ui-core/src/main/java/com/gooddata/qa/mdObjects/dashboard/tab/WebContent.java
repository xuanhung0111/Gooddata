package com.gooddata.qa.mdObjects.dashboard.tab;

import org.json.JSONException;
import org.json.JSONObject;

public class WebContent extends TabItem {

    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;
    private String url;

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

    public String getUrl() {
        return url;
    }

    public WebContent setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    protected JSONObject initPayload() throws JSONException {
        JSONObject positionJson = new JSONObject();
        positionJson.put("positionX", positionX).put("positionY", positionY)
                .put("sizeX", sizeX).put("sizeY", sizeY).put("url", url);
        JSONObject obj = new JSONObject();

        try {
            obj.put("iframeItem", positionJson);
        } catch (JSONException e) {
            handleJSONException(e);
        }
        return obj;
    }

    public WebContent setWebContent(String url) {
        setUrl(url).setPosition(TabItem.ItemPosition.MIDDLE);
        return this;
    }

    public WebContent setWebContentForPaging(String url) {
        setUrl(url).setPosition(ItemPosition.RIGHT_OF_NEXT_PAGE);
        return this;
    }

    @Override
    protected void getSizeByItemType() {
        mapItemSize(ItemSize.WIDGET_ITEM);
    }
}
