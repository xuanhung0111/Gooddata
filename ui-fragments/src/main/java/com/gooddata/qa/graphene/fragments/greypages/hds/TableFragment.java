package com.gooddata.qa.graphene.fragments.greypages.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class TableFragment extends AbstractGreyPagesFragment {
	
	@FindBy
	private WebElement name;
	
	@FindBy(xpath="div[@class='submit']/input")
	private WebElement submit;
	
	private static final By BY_BUTTON_CREATE = By.xpath("//div[@class='submit']/input[@value='Create']");
	
	public boolean verifyValidCreateTableForm() {
		waitForElementVisible(name);
		Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
		return true;
	}
	
	public void fillCreateTableForm(String name) {
		waitForElementVisible(this.name);
		if (name != null && name.length() > 0) this.name.sendKeys(name);
		Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
		Graphene.guardHttp(submit).click();
	}
	
	public String createTable(String name) throws JSONException {
		fillCreateTableForm(name);
		waitForElementNotVisible(BY_BUTTON_CREATE);
		JSONObject json = loadJSON();
		String tableUrl = json.getJSONObject("table").getJSONObject("links").getString("self");
		System.out.println("Created table on URL " + tableUrl);
		return tableUrl;
	}
	
	public boolean verifyValidEditTableForm(String name) {
		waitForElementVisible(this.name);
		Assert.assertEquals(submit.getAttribute("value"), "Update", "Submit button is not 'Update'");
		return this.name.getAttribute("value").equals(name);
	}
	
	public void updateTable(String newName) {
		waitForElementVisible(this.name);
		this.name.clear();
		if (newName != null && newName.length() > 0) {
			this.name.sendKeys(newName);
		}
		Graphene.guardHttp(submit).click();
	}
	
	public boolean verifyValidDeleteTableForm() {
		waitForElementNotVisible(name);
		Assert.assertEquals(submit.getAttribute("value"), "Delete", "Submit button is not 'Delete'");
		return true;
	}
	
	public void deleteTable() {
		waitForElementVisible(submit);
		Graphene.guardHttp(submit).click();
		Graphene.waitGui().until().element(BY_BUTTON_CREATE).is().visible();
		Assert.assertTrue(browser.getCurrentUrl().endsWith("/tables"), "Browser wasn't redirected to tables page");
	}

}