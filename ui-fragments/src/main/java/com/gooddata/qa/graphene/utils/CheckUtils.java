package com.gooddata.qa.graphene.utils;

import static java.lang.String.format;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public final class CheckUtils {

    private static final String STATUS_BAR_SELECTOR = "div#status > div.box-%s div.leftContainer";
    private static final By BY_GREEN_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "success"));
    private static final By BY_REPORT_ERROR = By.cssSelector("div.error-container");

    public static final By BY_RED_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "error"));
    public static final By BY_RED_BAR_WARNING = By.cssSelector(format(STATUS_BAR_SELECTOR, "warning"));
    public static final By BY_BLUE_BAR = By.cssSelector(format(STATUS_BAR_SELECTOR, "info"));
    public static final By BY_DISMISS_BUTTON = By.cssSelector("div#status .s-btn-dismiss");

    private CheckUtils() {
    }

    public static void checkRedBar(SearchContext searchContext) {
        if (searchContext.findElements(BY_RED_BAR).size() != 0) {
            fail("RED BAR APPEARED - " + searchContext.findElement(BY_RED_BAR).getText());
        }
        if (searchContext.findElements(BY_RED_BAR_WARNING).size() != 0) {
            fail("RED BAR APPEARED - " + searchContext.findElement(BY_RED_BAR_WARNING).getText());
        }
        //this kind of error appeared for the first time in geo chart
        if (searchContext.findElements(BY_REPORT_ERROR).size() != 0 && searchContext.findElement(BY_REPORT_ERROR).isDisplayed()) {
            fail("Report error APPEARED - " + searchContext.findElement(BY_REPORT_ERROR).getText());
        }
    }

    public static void checkGreenBar(SearchContext searchContext) {
        waitForElementVisible(BY_GREEN_BAR, searchContext);
    }

    public static void checkBlueBar(SearchContext searchContext) {
        waitForElementVisible(BY_BLUE_BAR, searchContext);
    }

    public static void checkGreenBar(SearchContext searchContext, String desiredMessage) {
        String greenBarMessage = waitForElementVisible(BY_GREEN_BAR, searchContext).getText();

        if (desiredMessage.length() != 0 && !greenBarMessage.equals(desiredMessage)) {
            fail("WRONG GREEN BAR MESSAGE - is: " + greenBarMessage + " expected: " + desiredMessage);
        }
    }

    public static void waitForDashboardPageLoaded(final SearchContext searchContext) {
        if (isElementPresent(By.cssSelector(".embedded"), searchContext))
            waitForElementVisible(By.cssSelector(".s-dashboardLoaded"), searchContext);
        else
            waitForElementVisible(
                    By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"),
                    searchContext);
        if (searchContext.findElements(BY_RED_BAR).size() != 0) {
            if ("Dashboard no longer exists".equals(searchContext.findElement(BY_RED_BAR).getText())) {
                waitForElementVisible(BY_DISMISS_BUTTON, searchContext).click();
            }
            Graphene.waitGui().withTimeout(5, TimeUnit.SECONDS).until(new Predicate<WebDriver>() {
                @Override
                public boolean apply(WebDriver input) {
                    return searchContext.findElements(BY_RED_BAR).isEmpty();
                }
            });
        }
        checkRedBar(searchContext);
    }

    public static void waitForReportsPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-domainPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForDataPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-dataPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForProjectPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-projectPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForProjectsPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='projectsCentral' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForEmailSchedulePageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForAnalysisPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForSchedulesPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForObjectPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForAccountPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By
                .xpath("//div[@id='p-accountPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForUserProfilePageLoaded(SearchContext searchContext) {
        waitForElementVisible(By
                .xpath("//div[@id='p-profilePage' and contains(@class,'s-displayed')]"), searchContext);
    }
    
    public static WebElement waitForElementVisible(By byElement, SearchContext searchContext) {
        Graphene.waitGui().until().element(byElement).is().visible();
        return searchContext.findElement(byElement);
    }

    public static WebElement waitForElementVisible(By byElement, SearchContext searchContext, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(byElement).is().visible();
        return searchContext.findElement(byElement);
    }

    public static WebElement waitForElementVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().visible();
        return element;
    }

    public static WebElement waitForElementVisible(WebElement element, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(element).is().visible();
        return element;
    }

    public static Select waitForElementVisible(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().visible();
        return select;
    }

    public static <T extends AbstractFragment> T waitForFragmentVisible(T fragment) {
        Graphene.waitGui().until().element(fragment.getRoot()).is().visible();
        return fragment;
    }

    public static void waitForElementNotVisible(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().visible();
    }

    public static void waitForElementNotVisible(WebElement element, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(element).is().not().visible();
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

    public static WebElement waitForElementPresent(By byElement, SearchContext searchContext) {
        Graphene.waitGui().until().element(byElement).is().present();
        return searchContext.findElement(byElement);
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

    public static void waitForElementNotPresent(By byElement, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(byElement).is().not().present();
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

    public static <T> Collection<T> waitForCollectionIsNotEmpty(final Collection<T> items) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !items.isEmpty();
            }
        });

        return items;
    }

    /**
     * Check if element is currently present in DOM
     * @see http://stackoverflow.com/questions/7991522/selenium-webdriver-test-if-element-is-present
     * @param locatorKey By element for location
     * @param context context to search
     * @return
     */
    public static boolean isElementPresent(By locatorKey, SearchContext context) {
        return context.findElements(locatorKey).size() > 0;
    }

    /**
     * Get texts of all elements matching given By selector under specified context
     * @param selector selector to match items
     * @param context search context
     * @return array of strings with element texts
     */
    public static Collection<String> getElementTexts(By selector, SearchContext context) {
        List<String> texts = new ArrayList<String>();
        for (WebElement ele : context.findElements(selector)) {
            texts.add(ele.getText());
        }

        return texts;
    }

}
