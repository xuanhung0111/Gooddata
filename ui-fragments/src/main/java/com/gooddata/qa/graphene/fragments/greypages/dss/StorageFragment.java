package com.gooddata.qa.graphene.fragments.greypages.dss;

import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.AbstractDatawarehouseFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class StorageFragment extends AbstractDatawarehouseFragment {

    @FindBy
    private WebElement title;

    @FindBy
    private WebElement description;

    @FindBy
    private WebElement authorizationToken;

    @FindBy(id="environment_testing")
    private WebElement testingEnv;

    @FindBy(xpath = "div[@class='submit']/input")
    private WebElement submit;

    private static final By BY_BUTTON_CREATE = By.xpath("//div[@class='submit']/input[@value='Create']");

    public boolean verifyValidCreateStorageForm() {
        waitForElementVisible(title);
        waitForElementVisible(description);
        waitForElementVisible(authorizationToken);
        waitForElementVisible(testingEnv);
        Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
        return true;
    }

    public void fillCreateStorageForm(String title, String description, String authorizationToken) {
        waitForElementVisible(this.title);
        if (title != null && title.length() > 0) this.title.sendKeys(title);
        if (description != null && description.length() > 0) this.description.sendKeys(description);
        if (authorizationToken != null && authorizationToken.length() > 0)
            this.authorizationToken.sendKeys(authorizationToken);
        this.testingEnv.click();
        Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
        Graphene.guardHttp(submit).click();
    }

    public String createStorage(String title, String description, String authorizationToken) throws JSONException, InterruptedException {
        fillCreateStorageForm(title, description, authorizationToken);
        waitForElementNotVisible(this.title);
        waitForElementPresent(BY_GP_PRE_JSON, browser);
        return waitForStorageCreated(10);
    }

    public boolean verifyValidEditStorageForm(String title, String description) {
        waitForElementVisible(this.title);
        waitForElementVisible(this.description);
        waitForElementNotVisible(authorizationToken);
        Assert.assertEquals(submit.getAttribute("value"), "Update", "Submit button is not 'Update'");
        return this.title.getAttribute("value").equals(title) && this.description.getAttribute("value").equals(description);
    }

    public void updateStorage(String newTitle, String newDescription) {
        waitForElementVisible(this.title).clear();
        waitForElementVisible(description).clear();
        if (newTitle != null && newTitle.length() > 0) {
            this.title.sendKeys(newTitle);
        }
        if (newDescription != null && newDescription.length() > 0) {
            this.description.sendKeys(newDescription);
        }
        Graphene.guardHttp(submit).click();
    }

    public boolean verifyValidDeleteStorageForm() {
        waitForElementNotVisible(title);
        waitForElementNotVisible(description);
        waitForElementNotVisible(authorizationToken);
        Assert.assertEquals(submit.getAttribute("value"), "Delete", "Submit button is not 'Delete'");
        return true;
    }

    public void deleteStorageSuccess() {
        deleteStorage();
        waitForElementVisible(BY_BUTTON_CREATE, browser);
        Assert.assertTrue(browser.getCurrentUrl().endsWith("/dss/instances"), "Browser wasn't redirected to storages page");
    }

    public void deleteStorage() {
        Graphene.guardHttp(waitForElementVisible(submit)).click();
    }

    private String waitForStorageCreated(int checkIterations) throws JSONException, InterruptedException {
        return waitTaskSucceed(checkIterations, "dssInstance");
    }

}
