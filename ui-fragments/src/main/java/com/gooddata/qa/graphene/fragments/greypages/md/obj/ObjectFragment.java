package com.gooddata.qa.graphene.fragments.greypages.md.obj;

import org.json.JSONException;
import org.json.JSONObject;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ObjectFragment extends AbstractGreyPagesFragment {

    public JSONObject getObject() throws JSONException {
        return loadJSON();
    }
}