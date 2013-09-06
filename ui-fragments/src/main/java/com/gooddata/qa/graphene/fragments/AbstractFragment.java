package com.gooddata.qa.graphene.fragments;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class AbstractFragment {
	
	@Root
	protected WebElement root;
	
	@Drone
	protected WebDriver browser;
	
	public WebElement getRoot() {
		return root;
	}
	
	protected static final By BY_LINK = By.tagName("a");
	
	public void waitForDashboardPageLoaded() {
		waitForElementVisible(By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"));
	}
	
	public void waitForElementVisible(WebElement element) {
		Graphene.waitGui().until().element(element).is().visible();
	}
	
	public void waitForElementVisible(By byElement) {
		Graphene.waitGui().until().element(byElement).is().visible();
	}
	
	public void waitForElementNotVisible(WebElement element) {
		Graphene.waitGui().until().element(element).is().not().visible();
	}
	
	public void waitForElementNotVisible(By byElement) {
		Graphene.waitGui().until().element(byElement).is().not().visible();
	}
	
	public void waitForElementPresent(By byElement) {
		Graphene.waitGui().until().element(byElement).is().present();
	}
	
	public void waitForElementPresent(WebElement element) {
		Graphene.waitGui().until().element(element).is().present();
	}
	
	public void waitForElementNotPresent(By byElement) {
		Graphene.waitGui().until().element(byElement).is().not().present();
	}
}
