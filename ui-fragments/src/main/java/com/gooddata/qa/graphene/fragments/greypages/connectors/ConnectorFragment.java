package com.gooddata.qa.graphene.fragments.greypages.connectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ConnectorFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement projectTemplateUri;

    @FindBy
    private WebElement active;

    @FindBy(css = "div.submit > input")
    private WebElement submitIntegrationButton;

    @FindBy(xpath = "//div[@class='submit']/input[@value='delete this integration']")
    private WebElement deleteIntegrationButton;

    public void createIntegration(String template) throws JSONException {
        waitForElementVisible(projectTemplateUri).sendKeys(template);
        Graphene.guardHttp(submitIntegrationButton).click();
        assertTrue(browser.getCurrentUrl().endsWith("integration"), "Integration was created");
        JSONObject json = loadJSON();
        assertTrue(json.getJSONObject("integration").getBoolean("active"), "Integration isn't created");
        System.out.println("Integration created...");
    }

    public void disableIntegration() throws JSONException {
        waitForElementVisible(active);
        if (active.isSelected()) active.click();
        Graphene.waitGui().until().element(active).is().not().selected();
        Graphene.guardHttp(submitIntegrationButton).click();
        JSONObject json = loadJSON();
        assertFalse(json.getJSONObject("integration").getBoolean("active"), "Integration wasn't disabled");
        System.out.println("Integration disabled...");
    }

    public void deleteIntegration() throws JSONException {
        waitForElementVisible(deleteIntegrationButton);
        Graphene.guardHttp(deleteIntegrationButton).click();

        assertFalse(browser.getCurrentUrl().contains("integration"), "Integration wasn't deleted");
        JSONObject json = loadJSON();
        assertTrue(json.getJSONObject("connector").has("connectorId"), "There isn't a connector ID");

        System.out.println("Integration deleted...");
    }
}
