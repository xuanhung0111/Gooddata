package com.gooddata.qa.graphene.reports;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.rolap.RolapRestUtils.postEtlPullIntegration;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;

public class SimpleCompAttributesTest extends AbstractProjectTest {

    private static final int statusPollingCheckIterations = 60; // (60*5s)
    Map<String, Integer> attributeMapping = new HashMap<String, Integer>();
    Map<String, Pair<String, Integer>> attributesElementsMapping = new HashMap<String, Pair<String, Integer>>();

    @DataProvider(name = "defaultRelations")
    public static Object[][] compAttrsData() {
        return new Object[][]{
                {"CompAttr1", "attr.comp1", "to {attr.invoice}", " as {attr.comp1?40}"},
                {"CompAttr2", "attr.comp2", "to {attr.comp1}", " as {attr.comp2?32}"},
                {"CompAttr3", "attr.comp3", "to {attr.comp2}", " as {attr.comp3?42}"},
                {"CompAttr4", "attr.comp4", "to {attr.invoice_item}", " as {attr.comp4?25}"},
                {"CompAttr5", "attr.comp5", "to {attr.invoice_item}", " as {attr.comp5?35}"},
                {"CompAttr6", "attr.comp6", "to {attr.invoice_item}", " as {attr.comp6?45}"},
                {"CompAttr7", "attr.comp7", "to {attr.invoice_item}", " as {attr.comp7?28}"},
                {"CompAttr8", "attr.comp8", "to {attr.person}", " as {attr.comp8?47}"},
                {"CompAttr9", "attr.comp9", "to {dt_invoice.month}", " as {attr.comp9?30}"}
        };
    }

    @Override
    public void initProperties() {
        projectTitle = "SimpleProject-test-compAttrs";
    }

    @Override
    protected void customizeProject() throws Throwable {
        // create model
        URL maqlResource = getClass().getResource("/etl/maql-simple.txt");
        postMAQL(IOUtils.toString(maqlResource), statusPollingCheckIterations);

        URL csvResource = getClass().getResource("/etl/invoice.csv");
        String webdavURL = uploadFileToWebDav(csvResource, null);

        URL uploadInfoResource = getClass().getResource("/etl/upload_info.json");
        uploadFileToWebDav(uploadInfoResource, webdavURL);

        postEtlPullIntegration(getRestApiClient(), testParams.getProjectId(), parseIntegrationEntry(webdavURL));

        // create comp-attrs model
        maqlResource = getClass().getResource("/comp-attributes/extended/ca-model2-maql.txt");
        postMAQL(IOUtils.toString(maqlResource), statusPollingCheckIterations);

        csvResource = getClass().getResource("/comp-attributes/extended/dtst.comp.csv");
        webdavURL = uploadFileToWebDav(csvResource, null);

        uploadInfoResource = getClass().getResource("/comp-attributes/extended/upload_info.json");
        uploadFileToWebDav(uploadInfoResource, webdavURL);

        postEtlPullIntegration(getRestApiClient(), testParams.getProjectId(), parseIntegrationEntry(webdavURL));
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "defaultRelations")
    public void createRel(String attributeTitle, String attrIdentifier, String relation, String relationAs) throws JSONException {
        String createMAQL = "alter attribute {" + attrIdentifier + "} add relations " + relation + relationAs + ";";
        postMAQL(createMAQL, statusPollingCheckIterations);
        int attributeID = getAttributeID(attributeTitle);
        attributeMapping.put(attrIdentifier, attributeID);

        JSONObject attribute = getObjectByID(attributeID);
        JSONArray attributeRelations = attribute.getJSONObject("attribute").getJSONObject("content").getJSONArray("rel");
        String attrRel = (String) attributeRelations.get(0);
        assertEquals(attrRel, relation + relationAs);
    }

    @Test(dependsOnMethods = {"createRel"}, dataProvider = "defaultRelations")
    public void alterAndCompute(String attributeTitle, String attrIdentifier, String relation, String relationAs) throws JSONException {
        // get attribute
        int attributeID = attributeMapping.get(attrIdentifier);

        // get attribute elements
        JSONArray attrDisplayForms = getObjectByID(attributeID).getJSONObject("attribute").getJSONObject("content").getJSONArray("displayForms");
        String attrElementsURI = ((JSONObject) attrDisplayForms.get(0)).getJSONObject("links").getString("elements");
        int attrElementsID = Integer.parseInt(attrElementsURI.replaceAll(".*/obj/", "").replaceAll("/elements", ""));
        ArrayList<Pair<String, Integer>> objectElementsByID = getObjectElementsByID(attrElementsID);

        // choose random one
        Pair<String, Integer> attributeElement = objectElementsByID.get(new Random().nextInt(objectElementsByID.size()));

        // store id/value of element mapped to attributeIdentifier
        attributesElementsMapping.put(attrIdentifier, attributeElement);

        // drop old relation
        String dropMAQL = "alter attribute {" + attrIdentifier + "} drop relations " + relation + ";";
        postMAQL(dropMAQL, statusPollingCheckIterations);
        assertTrue(getObjectByID(attributeID).getJSONObject("attribute").getJSONObject("content").isNull("rel"));

        // re-create new one
        String createMAQL = "alter attribute {" + attrIdentifier + "} add relations " + relation + relationAs.replaceAll("\\?.*}", "?" + attributeElement.getValue()) + "};";
        System.out.println("createMAQL = " + createMAQL);
        postMAQL(createMAQL, statusPollingCheckIterations);
    }

    private String parseIntegrationEntry(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }
}