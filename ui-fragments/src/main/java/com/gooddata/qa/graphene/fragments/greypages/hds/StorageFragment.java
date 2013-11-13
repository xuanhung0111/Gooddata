package com.gooddata.qa.graphene.fragments.greypages.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class StorageFragment extends AbstractGreyPagesFragment {
	
	@FindBy
	private WebElement title;
	
	@FindBy
	private WebElement description;
	
	@FindBy
	private WebElement authorizationToken;
	
	@FindBy
	private WebElement copyOf;
	
	@FindBy(xpath="div[@class='submit']/input")
	private WebElement submit;
	
	private static final By BY_BUTTON_CREATE = By.xpath("//div[@class='submit']/input[@value='Create']");
	
	public boolean verifyValidCreateStorageForm() {
		waitForElementVisible(title);
		waitForElementVisible(description);
		waitForElementVisible(copyOf);
		waitForElementVisible(authorizationToken);
		Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
		return true;
	}
	
	public void fillCreateStorageForm(String title, String description, String authorizationToken, String copyOf) {
		waitForElementVisible(this.title);
		if (title != null && title.length() > 0) this.title.sendKeys(title);
		if (description != null && description.length() > 0) this.description.sendKeys(description);
		if (authorizationToken != null && authorizationToken.length() > 0) this.authorizationToken.sendKeys(authorizationToken);
		if (copyOf != null && copyOf.length() > 0) this.copyOf.sendKeys(copyOf);
		Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
		Graphene.guardHttp(submit).click();
	}
	
	public String createStorage(String title, String description, String authorizationToken, String copyOf) throws JSONException, InterruptedException {
		fillCreateStorageForm(title, description, authorizationToken, copyOf);
		waitForElementNotVisible(this.title);
		waitForElementPresent(BY_GP_PRE_JSON);
		return waitForStorageCreated(10);
	}
	
	public boolean verifyValidEditStorageForm(String title, String description) {
		waitForElementVisible(this.title);
		waitForElementVisible(this.description);
		waitForElementNotVisible(copyOf);
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
		waitForElementNotVisible(copyOf);
		waitForElementNotVisible(authorizationToken);
		Assert.assertEquals(submit.getAttribute("value"), "Delete", "Submit button is not 'Delete'");
		return true;
	}
	
	public void deleteStorage() {
		Graphene.guardHttp(waitForElementVisible(submit)).click();
		waitForElementVisible(BY_BUTTON_CREATE);
		Assert.assertTrue(browser.getCurrentUrl().endsWith("/storages"), "Browser wasn't redirected to storages page");
	}
	
	private String waitForStorageCreated(int checkIterations) throws JSONException, InterruptedException {
		String executionUrl = browser.getCurrentUrl();
		System.out.println("Related execution URL is " + executionUrl);
		Assert.assertTrue(executionUrl.contains("executions"), "Storage creation didn't redirect to /executions/* page");
		int i = 0;
		boolean asyncTaskPoll = isAsyncTaskPoll();
		while (asyncTaskPoll && i < checkIterations) {
			System.out.println("Current storage execution is polling");
			Thread.sleep(5000);
			browser.get(executionUrl);
			asyncTaskPoll = isAsyncTaskPoll();
			i++;
		}
		JSONObject json = loadJSON();
		String storageUrl = json.getJSONObject("asyncTask").getJSONObject("links").getString("storage");
		System.out.println("Created storage on URL " + storageUrl);
		return storageUrl;
	}
	
	private boolean isAsyncTaskPoll() throws JSONException {
		JSONObject json = loadJSON();
		return json.getJSONObject("asyncTask").getJSONObject("links").has("poll");
	}

}