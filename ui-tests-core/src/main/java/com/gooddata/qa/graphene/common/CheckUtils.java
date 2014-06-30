package com.gooddata.qa.graphene.common;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.testng.Assert.fail;

public class CheckUtils {

    private static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");
    private static final By BY_RED_BAR_WARNING = By.cssSelector("div.c-status.box-warning");
    private static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    public static void checkRedBar(WebDriver browser) {
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

    public static void waitForDashboardPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"), browser);
        checkRedBar(browser);
    }

    public static void waitForReportsPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-domainPage' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForDataPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-dataPage' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForProjectPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-projectPage' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForPulsePageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-pulsePage' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForProjectsPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='projectsCentral' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForAnalysisPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForSchedulesPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"), browser);
    }

    public static void waitForObjectPageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]"), browser);
    }

    public static WebElement waitForElementVisible(By byElement, WebDriver browser) {
        Graphene.waitGui().until().element(byElement).is().visible();
        return browser.findElement(byElement);
    }

    public static WebElement waitForElementVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().visible();
        return element;
    }

    public static void waitForElementNotVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().visible();
    }

    public static WebElement waitForElementPresent(By byElement, WebDriver browser) {
        Graphene.waitGui().until().element(byElement).is().present();
        return browser.findElement(byElement);
    }

    public static WebElement waitForElementPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().present();
        return element;
    }

    public static void waitForElementNotPresent(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().present();
    }

    public static void waitForElementNotPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().present();
    }
}
