package com.gooddata.qa.graphene.fragments.dashboards.widget;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportContainer;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class EmbeddedWidget extends AbstractFragment {

    @FindBy(tagName = "iframe")
    private WebElement iframe;

    public static final By BY_LATEST_INCOMING_MESSAGE = By.cssSelector(".incoming-message:first-of-type div");
    private static final By BY_TOP_LEFT_RESIZE_BUTTON = By.className("yui3-selectionbox-resize-tl");
    private static final By BY_BOTTOM_RIGHT_RESIZE_BUTTON = By.className("yui3-selectionbox-resize-br");

    public EmbeddedDashboard getEmbeddedDashboard() {
        browser.switchTo().frame(iframe);
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        return Graphene.createPageFragment(EmbeddedDashboard.class,
                waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
    }

    public String getContentBodyAsText() {
        try {
            browser.switchTo().frame(iframe);
            return waitForElementVisible(browser.findElement(By.tagName("body"))).getText();
        } finally {
            browser.switchTo().defaultContent();
        }
    }

    public List<String> getLatestIncomingMessage() {
        try {
            browser.switchTo().frame(iframe);
            List<String> messages = ElementUtils.getElementTexts(waitForCollectionIsNotEmpty(
                browser.findElements(BY_LATEST_INCOMING_MESSAGE)));

            //wait for the latest message text is updated
            messages = waitToChange(messages.get(0));
            log.info("Incoming Message :" + messages.toString() + "\n");
            return messages;
        } finally {
            browser.switchTo().defaultContent();
        }
    }

    public String getImageUri() {
        try {
            browser.switchTo().frame(iframe);
            return waitForElementVisible(browser.findElement(By.tagName("img"))).getAttribute("src");
        } finally {
            browser.switchTo().defaultContent();
        }
    }

    public EmbeddedWidget waitForImageLoading() {
        try {
            browser.switchTo().frame(iframe);
            Graphene.waitGui().withTimeout(2, TimeUnit.SECONDS).until(browser -> {
                WebElement image = waitForElementVisible(browser.findElement(By.className("yui3-c-iframewidget")));
                String lastSrc = image.getAttribute("src");
                return !lastSrc.equals(image.getAttribute("src"));
            });
        } catch (TimeoutException e){
            //Do nothing
        } finally {
            browser.switchTo().defaultContent();
        }
        return this;
    }

    public EmbeddedReportContainer getEmbeddedReportContainer() {
        browser.switchTo().frame(iframe);
        return Graphene.createPageFragment(EmbeddedReportContainer.class,
                waitForElementVisible(EmbeddedReportContainer.LOCATOR, browser));
    }

    public EmbeddedWidget resizeFromTopLeftButton(int xOffset, int yOffset) {
        return resize(BY_TOP_LEFT_RESIZE_BUTTON, xOffset, yOffset);
    }

    public EmbeddedWidget resizeFromBottomRightButton(int xOffset, int yOffset) {
        return resize(BY_BOTTOM_RIGHT_RESIZE_BUTTON, xOffset, yOffset);
    }

    protected List<String> waitToChange(String currentMessage) {
        List<String> messages = ElementUtils.getElementTexts(waitForCollectionIsNotEmpty(
            browser.findElements(BY_LATEST_INCOMING_MESSAGE)));
        try {
            Function<WebDriver, Boolean> elementChanged = browser -> !messages.get(0).equals(currentMessage);
            Graphene.waitGui().withTimeout(2, TimeUnit.SECONDS).until(elementChanged);
        } catch (TimeoutException e) {
            //do nothing
        }
        return messages;
    }

    private EmbeddedWidget resize(By button, int xOffset, int yOffset) {
        getActions()
                .clickAndHold(waitForElementVisible(button, browser))
                .moveByOffset(xOffset, yOffset)
                .release()
                .perform();

        return this;
    }
}
