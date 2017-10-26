package com.gooddata.qa.mdObjects.dashboard.tab;

import com.gooddata.qa.mdObjects.MdObject;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class TabItem implements MdObject {

    private int positionX;
    private int positionY;
    private int sizeX;
    private int sizeY;

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public void setPosition(ItemPosition position) {
        mapItemPosition(position);
    }

    @Override
    public JSONObject getMdObject() {
        JSONObject obj = null;

        try {
            obj = initPayload();

            // override item's size if it's not set
            if (sizeX == 0 && sizeY == 0)
                getSizeByItemType();

            obj.getJSONObject((String) obj.keys().next())
                    .put("positionX", positionX)
                    .put("positionY", positionY)
                    .put("sizeX", sizeX)
                    .put("sizeY", sizeY);

        } catch (JSONException e) {
            handleJSONException(e);
        }

        return obj;
    }

    protected abstract JSONObject initPayload() throws JSONException;

    protected abstract void getSizeByItemType();

    protected void mapItemSize(ItemSize itemSize) {
        sizeX = itemSize.getSizeX();
        sizeY = itemSize.getSizeY();
    }

    private void mapItemPosition(ItemPosition itemPosition) {
        positionX = itemPosition.getPosX();
        positionY = itemPosition.getPosY();
    }

    public enum ItemPosition {
        TOP(370, 0),
        TOP_LEFT(0, 0),
        TOP_RIGHT(750, 0),
        LEFT(0, 150),
        RIGHT(750, 151);

        private int posX;
        private int posY;

        ItemPosition(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public int getPosX() {
            return posX;
        }

        public int getPosY() {
            return posY;
        }
    }
}
