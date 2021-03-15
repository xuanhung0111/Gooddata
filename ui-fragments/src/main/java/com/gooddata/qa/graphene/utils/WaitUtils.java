package com.gooddata.qa.graphene.utils;

import static com.gooddata.qa.graphene.utils.CheckUtils.BY_DISMISS_BUTTON;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static org.openqa.selenium.By.className;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public final class WaitUtils {

    private static final Logger log = Logger.getLogger(WaitUtils.class.getName());
    private static final int TIMEOUT_WAIT_OLD_CLIENT_LOADED = 5 * 60; // minutes

    private WaitUtils() {
    }

    public static void waitForDashboardPageLoaded(final SearchContext searchContext) {
        if (isElementPresent(By.cssSelector(".embedded"), searchContext)) {
            waitForElementVisible(By.cssSelector(".s-dashboardLoaded"), searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
        }
        else {
            waitForElementVisible(
                    By.xpath("//div[@id='p-projectDashboardPage' and contains(@class,'s-displayed')]"),
                    searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
        }
        if (searchContext.findElements(BY_RED_BAR).size() != 0) {
            if ("Dashboard no longer exists".equals(searchContext.findElement(BY_RED_BAR).getText())) {
                waitForElementVisible(BY_DISMISS_BUTTON, searchContext).click();
            }
            Graphene.waitGui().withTimeout(5, TimeUnit.SECONDS).until(input -> searchContext.findElements(BY_RED_BAR).isEmpty());
        }
        checkRedBar(searchContext);
    }

    public static void waitForReportsPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-domainPage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForDataPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-dataPage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForModelDataPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@class='joint-paper-scroller joint-theme-default']"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForProjectsAndUsersPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-projectPage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForProjectsPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='projectsCentral' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForEmailSchedulePageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForAnalysisPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-analysisPage' and contains(@class,'s-displayed')]"), searchContext);
        List<WebElement> visibleElements = searchContext.findElements(By.cssSelector(".reportEditorHeader button")).stream()
                .filter(elememt -> isElementVisible(elememt)).collect(Collectors.toList());
        //Case1(Creating report): There are 3 visible elements such as: What, How and Filter
        //Case2(Saving report): There are 4 visible elements such as: What, How ,Filter and Show Configuration
        //Case3(Restoring another version): There are 4 visible elements such as: What, How ,Filter and Show Configuration
        //In Case1 & 2: Wait until enable elements equal visible elements(3 or 4)
        //In Case3: Wait until enable element equals 1(Just show configuration be enabled)
        Graphene.waitGui().until(browser -> {
            long enabledElementsCount = visibleElements.stream().filter(element -> !element.getAttribute("class")
                    .contains("disabled")).count();
            return enabledElementsCount == visibleElements.size() || enabledElementsCount == 1;
        });
    }

    public static void waitForSchedulesPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-emailSchedulePage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForObjectPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-objectPage' and contains(@class,'s-displayed')]"), searchContext);
    }

    public static void waitForAccountPageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-accountPage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForUserProfilePageLoaded(SearchContext searchContext) {
        waitForElementVisible(By.xpath("//div[@id='p-profilePage' and contains(@class,'s-displayed')]"),
                searchContext, TIMEOUT_WAIT_OLD_CLIENT_LOADED);
    }

    public static void waitForMainPageLoading(SearchContext searchContext) {
        waitForElementNotPresent(By.className("main-loading"), searchContext);
    }

    public static WebElement waitForElementVisible(By byElement, SearchContext searchContext) {
        Graphene.waitGui().until().element(searchContext, byElement).is().visible();
        return searchContext.findElement(byElement);
    }

    public static WebElement waitForElementVisible(By byElement, SearchContext searchContext, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(searchContext, byElement).is().visible();
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

    public static void waitForElementNotVisible(By byElement, SearchContext searchContext) {
        Graphene.waitGui().until().element(searchContext, byElement).is().not().visible();
    }

    public static void waitForElementNotVisible(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().not().visible();
    }

    public static void waitForFragmentNotVisible(AbstractFragment fragment) {
        Graphene.waitGui().until().element(fragment.getRoot()).is().not().visible();
    }

    public static WebElement waitForElementEnabled(final WebElement element) {
        // check for regularly disabled input, checks 'disabled' attribute, this will return true for the most elements
        // since they don't contain 'disabled' attribute
        Graphene.waitGui().until().element(element).is().enabled();

        // check for other elements styled as button, input, etc. that are disabled programmatically and styled with css
        Function<WebDriver, Boolean> elementEnabled = browser -> !element.getAttribute("class").contains("disabled");
        Graphene.waitGui().until(elementEnabled);

        return element;
    }

    public static WebElement waitForElementPresent(By byElement, SearchContext searchContext) {
        Graphene.waitGui().until().element(searchContext, byElement).is().present();
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

    public static void waitForElementNotPresent(By byElement, SearchContext searchContext) {
        Graphene.waitGui().until().element(searchContext, byElement).is().not().present();
    }

    public static void waitForElementNotPresent(By byElement, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(byElement).is().not().present();
    }

    public static void waitForElementNotPresent(WebElement element, int timeout) {
        Graphene.waitGui().withTimeout(timeout, TimeUnit.SECONDS).until().element(element).is().not().present();
    }

    public static void waitForElementNotPresent(WebElement element) {
        Graphene.waitGui().until().element(element).is().not().present();
    }

    public static void waitForElementNotPresent(Select select) {
        Graphene.waitGui().until().element(select.getFirstSelectedOption()).is().not().present();
    }

    public static void waitForCollectionIsEmpty(final Collection<?> items) {
        Function<WebDriver, Boolean> collectionEmpty = browser -> items.isEmpty();
        Graphene.waitGui().until(collectionEmpty);
    }

    public static void waitForElementAttributeContainValue(WebElement element, String attribute, String value) {
        Graphene.waitGui().until().element(element).attribute(attribute).contains(value);
    }

    public static void waitForElementAttributeNotContainValue(WebElement element, String attribute, String value) {
        Graphene.waitGui().until().element(element).attribute(attribute).not().contains(value);
    }

    public static <T extends Collection<?>> T waitForCollectionIsNotEmpty(final T items) {
        Function<WebDriver, Boolean> collectionNotEmpty = browser -> !items.isEmpty();
        Graphene.waitGui().until(collectionNotEmpty);

        return items;
    }

    public static <T extends Collection<?>> T waitForCollectionIsNotEmpty(final T items, int size) {
        Function<WebDriver, Boolean> collectionNotEmpty = browser -> !items.isEmpty() && items.size() == size;
        Graphene.waitGui().until(collectionNotEmpty);

        return items;
    }

    public static void waitForStringInUrl(final String url) {
        Function<WebDriver, Boolean> containsString = driver -> driver.getCurrentUrl().contains(url);
        Graphene.waitGui().until(containsString);
    }

    public static void waitForStringMissingInUrl(final String url) {
        Function<WebDriver, Boolean> missingString = driver -> !driver.getCurrentUrl().contains(url);
        Graphene.waitGui().until(missingString);
    }

    public static void waitForExporting(final File destination) {
        System.out.println("waiting for export " + destination.getName());

        Function<WebDriver, Boolean> isExportCompleted = browser -> destination.exists() && destination.length() != 0;
        Graphene.waitGui().pollingEvery(1, TimeUnit.SECONDS)
                .withTimeout(5, TimeUnit.MINUTES)
                .until(isExportCompleted);
        System.out.print("File size: " + destination.length());
    }

    public static void waitForExportingIgnoreAlert(final File destination, WebDriver driver) {
        System.out.println("waiting for export " + destination.getName());

        Function<WebDriver, Boolean> isExportCompleted = browser -> destination.exists() && destination.length() != 0;
        try {
            Graphene.waitGui().pollingEvery(1, TimeUnit.SECONDS).withTimeout(5, TimeUnit.MINUTES).until(isExportCompleted);
            System.out.print("File size: " + destination.length());
        } catch(Exception e) {
            driver.navigate().refresh();
            driver.switchTo().alert().dismiss();
            driver.switchTo().defaultContent();
            Graphene.waitGui().pollingEvery(1, TimeUnit.SECONDS).withTimeout(5, TimeUnit.MINUTES).until(isExportCompleted);
            System.out.print("File size: " + destination.length());
        }
    }

    public static void waitForIndigoMessageDisappear(SearchContext context) {
        WebElement message = waitForElementVisible(By.className("gd-messages"), context);

        Graphene.waitGui().withTimeout(30, TimeUnit.SECONDS).until(browser -> {
            if (isElementPresent(By.className("success"), message)) {
                waitForElementNotVisible(message);
                return true;
            }
            if (isElementPresent(By.className("error"), message)) {
                throw new RuntimeException("Indigo error message found");
            }
            if (!isElementVisible(message)) {
                log.info("Success message flash so quickly and consider this is correct behavior");
                return true;
            }
            return false;
        });
    }

    public static void waitForLoadingIconHidden() {
        final By loadingIcon = className("gd-spinner");
        try {
            Function<WebDriver, Boolean> isLoadingIconVisible = browser -> isElementVisible(loadingIcon, browser);
            Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(isLoadingIconVisible);
        } catch (TimeoutException e) {
            //do nothing
        }
        waitForElementNotVisible(loadingIcon);
    }
}
