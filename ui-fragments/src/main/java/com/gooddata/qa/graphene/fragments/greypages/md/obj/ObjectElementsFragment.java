package com.gooddata.qa.graphene.fragments.greypages.md.obj;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ObjectElementsFragment extends AbstractGreyPagesFragment {

    public ArrayList<Pair<String, Integer>> getObjectElements() throws JSONException {
        ArrayList<Pair<String, Integer>> pairs = new ArrayList<Pair<String, Integer>>();

        waitForElementVisible(BY_GP_PRE_JSON, browser);
        JSONArray attributeElements = loadJSON().getJSONObject("attributeElements").getJSONArray("elements");

        for (int i = 0; i < attributeElements.length(); i++) {
            String elementTitle = ((org.json.JSONObject) attributeElements.get(i)).getString("title");
            String elementURI = ((org.json.JSONObject) attributeElements.get(i)).getString("uri");
            int elementID = Integer.parseInt(elementURI.substring(elementURI.lastIndexOf("=") + 1, elementURI.length()));

            pairs.add(Pair.of(elementTitle, elementID));
        }
        return pairs;
    }
}
