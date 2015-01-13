package com.gooddata.qa.graphene.common;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

import static org.testng.Assert.fail;

public class CheckUtils {

    private static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");
    private static final By BY_RED_BAR_WARNING = By.cssSelector("div.c-status.box-warning");
    private static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    private CheckUtils() {
    }

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

    public static void waitForEmailSchedulePageLoaded(WebDriver browser) {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"), browser);
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

    public static Select waitForElementVisible(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().visible();
        return select;
    }

    public static WebElement waitForFragmentVisible(AbstractFragment fragment) {
        Graphene.waitGui().until().element(fragment.getRoot()).is().visible();
        return fragment.getRoot();
    }

    public static void waitForElementNotVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().visible();
    }

    public static void waitForElementNotVisible(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().visible();
    }

    public static void waitForElementNotVisible(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().not().visible();
    }

    public static void waitForFragmentNotVisible(AbstractFragment fragment) {
        Graphene.waitGui().until().element(fragment.getRoot()).is().not().visible();
    }

    public static WebElement waitForElementPresent(By byElement, WebDriver browser) {
        Graphene.waitGui().until().element(byElement).is().present();
        return browser.findElement(byElement);
    }

    public static WebElement waitForElementPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().present();
        return element;
    }

    public static Select waitForElementPresent(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().present();
        return select;
    }

    public static void waitForElementNotPresent(By byElement) {
        Graphene.waitGui().until().element(byElement).is().not().present();
    }

    public static void waitForElementNotPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().present();
    }

    public static void waitForElementNotPresent(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().not().present();
    }

    public static void waitForCollectionIsEmpty(final Collection<?> items) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return items.isEmpty();
            }
        });
    }

    public static void waitForCollectionIsNotEmpty(final Collection<?> items) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !items.isEmpty();
            }
        });
    }
}
