package com.gooddata.qa.graphene.fragments.greypages.md.obj;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

public class ObjectElementsFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement submit;

    public ArrayList<Pair<String, Integer>> getObjectElements() throws JSONException, InterruptedException {
        ArrayList<Pair<String, Integer>> pairs = new ArrayList<Pair<String, Integer>>();

        Graphene.guardHttp(waitForElementPresent(submit)).click();

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