package com.gooddata.qa.graphene.fragments.disc;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DISCNavigation extends AbstractFragment {

	@FindBy(xpath = "//a[text()='Projects']")
	protected WebElement projectsButton;
	
	public WebElement getProjectsButton() {
		return projectsButton;
	}
}
