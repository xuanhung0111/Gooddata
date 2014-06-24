package com.gooddata.qa.graphene.common;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.testng.Assert.fail;

public class CheckUtils extends CommonUtils {

    public CheckUtils(WebDriver browser) {
        super(browser, null, null);
    }

    private static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");
    private static final By BY_RED_BAR_WARNING = By.cssSelector("div.c-status.box-warning");
    private static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    public void checkRedBar() {
        if (browser.findElements(BY_RED_BAR).size() != 0) {
            fail("RED BAR APPEARED - " + browser.findElement(BY_RED_BAR).getText());
        }
        if (browser.findElements(BY_RED_BAR_WARNING).size() != 0) {
            fail("RED BAR APPEARED - " + browser.findElement(BY_RED_BAR_WARNING).getText());
        }
        //this kind of error appeared for the first time in geo chart
        if (browser.findElements(BY_REPORT_ERROR).size() != 0 && browser.findElement(BY_REPORT_ERROR).isDisplayed()) {
            fail("Report error APPEARED - " + browser.findElement(BY_REPORT_ERROR).getText());
        }
    }

    public void waitForDashboardPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"));
        checkRedBar();
    }

    public void waitForReportsPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-domainPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForDataPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-dataPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForProjectPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-projectPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForPulsePageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-pulsePage' and contains(@class,'s-displayed')]"));
    }

    public void waitForProjectsPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='projectsCentral' and contains(@class,'s-displayed')]"));
    }

    public void waitForAnalysisPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"));
    }

    public void waitForSchedulesPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"));
    }

    public void waitForObjectPageLoaded() {
        waitForElementVisible(By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]"));
    }

    public WebElement waitForElementVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().visible();
        return browser.findElement(byElement);
    }

    public WebElement waitForElementVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().visible();
        return element;
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
