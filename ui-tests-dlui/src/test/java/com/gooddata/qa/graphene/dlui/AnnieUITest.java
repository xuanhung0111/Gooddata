package com.gooddata.qa.graphene.dlui;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;

public class AnnieUITest extends AbstractProjectTest {

    @FindBy(css = CSS_ADD_NEW_FIELDS_LOCATOR)
    private WebElement addNewFieldsButton;

    @FindBy(css = "iframe[src*='dlui-annie']")
    private WebElement dluiFrame;

    @FindBy(css = ".annie-dialog-main")
    private WebElement annieDialog;

    @FindBy(css = ".btn-dismiss")
    private WebElement dismissButton;

    private static final String ENABLE_DATA_EXPLORER_URI_TEMPLATE =
            "/gdc/projects/%s/projectFeatureFlags";
    private static final String ENABLE_DATA_EXPLORER = "enableDataExplorer";
    private static final String CSS_ADD_NEW_FIELDS_LOCATOR = ".s-btn-add_new_fields";

    @Test(dependsOnMethods = {"createProject"}, groups = {"annie-ui"})
    public void enableAnnieUIFeature() throws JSONException{
        try {
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|");
            assertTrue(browser.findElements(By.cssSelector(CSS_ADD_NEW_FIELDS_LOCATOR)).size() == 0);
    
            enableDataExplorerFeature(true);

            openUrl(startPage);
            openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|");
            waitForElementVisible(addNewFieldsButton).click();
            browser.switchTo().frame(waitForElementVisible(dluiFrame));
            waitForElementVisible(annieDialog);
            waitForElementVisible(dismissButton).click();
            waitForElementNotVisible(dluiFrame);
            browser.switchTo().defaultContent();
        } finally {
            enableDataExplorerFeature(false);
        }
    }

    @Test(dependsOnGroups = {"annie-ui"}, groups = {"tests"})
    public void prepareForEndingTest() {
        successfulTest = true;
    }

    private void enableDataExplorerFeature(boolean on) throws JSONException {
        String enableDataExplorerUri =
                String.format(ENABLE_DATA_EXPLORER_URI_TEMPLATE, testParams.getProjectId());
        JSONObject featureFlag =new JSONObject()
            .put("key", ENABLE_DATA_EXPLORER)
            .put("value", on)
            .put("links", new JSONObject().put("self", enableDataExplorerUri + "/"
                    + ENABLE_DATA_EXPLORER));
        System.out.println(new JSONObject().put("featureFlag", featureFlag).toString());
        getRestApiClient();
        HttpRequestBase postRequest =
                restApiClient.newPostMethod(enableDataExplorerUri,
                        new JSONObject().put("featureFlag", featureFlag).toString());
        HttpResponse postResponse = restApiClient.execute(postRequest);
        assertEquals(postResponse.getStatusLine().getStatusCode(), 201, "Invalid status code");
    }
}
