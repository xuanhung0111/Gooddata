package com.gooddata.qa.graphene.fragments.greypages.md;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ValidateFragment extends AbstractGreyPagesFragment {
	
	@FindBy
	private WebElement invalid_objects;
	
	@FindBy
	private WebElement ldm;
	
	@FindBy
	private WebElement metric_filter;
	
	@FindBy
	private WebElement pdm;
	
	@FindBy
	private WebElement submit;
	
	public String validate() {
		//TODO check/uncheck required validation
		waitForElementVisible(submit);
		Graphene.guardHttp(submit).click();
		waitForElementNotVisible(submit);
		waitForElementVisible(BY_GP_LINK);
		Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
		waitForElementNotPresent(BY_GP_PRE_JSON);
		//TODO read result
		return "";
	}
}
