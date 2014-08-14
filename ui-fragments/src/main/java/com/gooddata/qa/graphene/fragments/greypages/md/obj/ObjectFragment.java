package com.gooddata.qa.graphene.fragments.greypages.md.obj;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;

public class ObjectFragment extends AbstractGreyPagesFragment {

    public JSONObject getObject() throws JSONException {
        return loadJSON();
    }
}