package com.gooddata.qa.graphene.fragments.greypages.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
	
	public String createStorage(String title, String description, String authorizationToken, String copyOf) throws JSONException {
		WebDriver browser = GrapheneContext.getProxy();
		fillCreateStorageForm(title, description, authorizationToken, copyOf);
		waitForElementNotVisible(this.title);
		waitForElementPresent(BY_GP_PRE_JSON);
		System.out.println("Related execution URL is " + browser.getCurrentUrl());
		Assert.assertTrue(browser.getCurrentUrl().contains("executions"), "Storage creation didn't redirect to /executions/* page");
		JSONObject json = loadJSON();
		String storageUrl = json.getJSONObject("asyncTask").getJSONObject("links").getString("storage");
		System.out.println("Created storage on URL " + storageUrl);
		return storageUrl;
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
		waitForElementVisible(this.title);
		this.title.clear();
		this.description.clear();
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
		waitForElementVisible(submit);
		Graphene.guardHttp(submit).click();
		Graphene.waitGui().until().element(BY_BUTTON_CREATE).is().visible();
		GrapheneContext.getProxy().getCurrentUrl().endsWith("/gdc/storages");
	}

}