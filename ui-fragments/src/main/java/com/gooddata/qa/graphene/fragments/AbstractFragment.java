package com.gooddata.qa.graphene.fragments;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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

    public void waitForAnalysisPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForEmailSchedulePageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"));
    }

    public WebElement waitForElementVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().visible();
        return element;
    }

    public Select waitForElementVisible(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().visible();
        return select;
    }

    public WebElement waitForElementVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().visible();
        return browser.findElement(byElement);
    }

    public void waitForElementNotVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().visible();
    }

    public void waitForElementNotVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().visible();
    }

    public WebElement waitForElementPresent(By byElement) {
        Graphene.waitGui().until().element(byElement).is().present();
        return browser.findElement(byElement);
    }

    public WebElement waitForElementPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().present();
        return element;
    }

    public void waitForElementNotPresent(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().present();
    }

    public void waitForElementNotPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().present();
    }
}
