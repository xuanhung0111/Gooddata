package com.gooddata.qa.graphene.fragments.greypages.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ColumnFragment extends AbstractGreyPagesFragment {
	
	public enum Types {
		DATE, DATETIME, DECIMAL, INT, TEXT;
		
		public By getOptionElement() {
			return By.xpath("option[@value='" + this.getText() + "']");
		}
		
		public String getText() {
			return this.toString().toLowerCase();
		}
	}
	
	@FindBy
	private WebElement name;
	
	@FindBy
	private WebElement type;
	
	@FindBy
	private WebElement primary;
	
	@FindBy
	private WebElement foreignKeyTable;
	
	@FindBy
	private WebElement foreignKeyColumn;
	
	@FindBy(xpath="div[@class='submit']/input")
	private WebElement submit;
	
	private static final By BY_BUTTON_CREATE = By.xpath("//div[@class='submit']/input[@value='Create']");
	
	public boolean verifyValidCreateColumnForm() {
		waitForElementVisible(name);
		waitForElementVisible(type);
		waitForElementVisible(primary);
		waitForElementVisible(foreignKeyTable);
		waitForElementVisible(foreignKeyColumn);
		Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
		return true;
	}
	
	public void fillCreateColumnForm(String name, Types type, boolean primaryKey, String foreignKeyTable, String foreignKeyColumn) {
		waitForElementVisible(this.name);
		if (name != null && name.length() > 0) this.name.sendKeys(name);
		if (type != null) {
			this.type.findElement(type.getOptionElement()).click();
		}
		if (primaryKey) {
			if (!this.primary.isSelected()) this.primary.click();
		} else {
			if (this.primary.isSelected()) this.primary.click();
		}
		if (foreignKeyTable != null && foreignKeyTable.length() > 0) this.foreignKeyTable.sendKeys(foreignKeyTable);
		if (foreignKeyColumn != null && foreignKeyColumn.length() > 0) this.foreignKeyColumn.sendKeys(foreignKeyColumn);
		Assert.assertEquals(submit.getAttribute("value"), "Create", "Submit button is not 'Create'");
		Graphene.guardHttp(submit).click();
	}
	
	public String createColumn(String name, Types type, boolean primaryKey, String foreignKeyTable, String foreignKeyColumn) throws JSONException {
		fillCreateColumnForm(name, type, primaryKey, foreignKeyTable, foreignKeyColumn);
		waitForElementNotVisible(BY_BUTTON_CREATE);
		JSONObject json = loadJSON();
		String columnUrl = json.getJSONObject("column").getJSONObject("links").getString("self");
		System.out.println("Created column on URL " + columnUrl);
		return columnUrl;
	}
	
	public boolean verifyValidUpdateColumnForm(String name, Types type, boolean primary, String foreignKeyTable, String foreignKeyColumn) {
		waitForElementVisible(this.name);
		waitForElementVisible(this.type);
		waitForElementVisible(this.primary);
		waitForElementVisible(this.foreignKeyTable);
		waitForElementVisible(this.foreignKeyColumn);
		Assert.assertEquals(submit.getAttribute("value"), "Update", "Submit button is not 'Update'");
		Select typeSelect = new Select(GrapheneContext.getProxy().findElement(By.name("type")));
		return this.name.getAttribute("value").equals(name) && typeSelect.getFirstSelectedOption().getText().equals(type)
				&& this.primary.isSelected() == primary && foreignKeyTable != null ? this.foreignKeyTable.getAttribute("value").equals(foreignKeyTable) : true
				&& foreignKeyColumn != null ? this.foreignKeyColumn.getAttribute("value").equals(foreignKeyColumn) : true;
	}
	
	public void updateColumn(String newName, Types newType, boolean newPrimaryKey, String newForeignKeyTable, String newForeignKeyColumn) {
		waitForElementVisible(this.name);
		this.name.clear();
		if (newName != null && newName.length() > 0) {
			this.name.sendKeys(newName);
		}
		if (type != null) {
			this.type.findElement(newType.getOptionElement()).click();
		}
		if (newPrimaryKey) {
			if (!this.primary.isSelected()) this.primary.click();
		} else {
			if (this.primary.isSelected()) this.primary.click();
		}
		this.foreignKeyTable.clear();
		if (newForeignKeyTable != null && newForeignKeyTable.length() > 0) this.foreignKeyTable.sendKeys(newForeignKeyTable);
		this.foreignKeyColumn.clear();
		if (newForeignKeyColumn != null && newForeignKeyColumn.length() > 0) this.foreignKeyColumn.sendKeys(newForeignKeyColumn);
		Graphene.guardHttp(submit).click();
	}
	
	public boolean verifyValidDeleteColumnForm() {
		waitForElementNotVisible(name);
		waitForElementNotVisible(type);
		waitForElementNotVisible(primary);
		waitForElementNotVisible(foreignKeyTable);
		waitForElementNotVisible(foreignKeyColumn);
		Assert.assertEquals(submit.getAttribute("value"), "Delete", "Submit button is not 'Delete'");
		return true;
	}
	
	public void deleteColumn() {
		waitForElementVisible(submit);
		Graphene.guardHttp(submit).click();
		Graphene.waitGui().until().element(BY_BUTTON_CREATE).is().visible();
		GrapheneContext.getProxy().getCurrentUrl().endsWith("/gdc/columns");
	}

}