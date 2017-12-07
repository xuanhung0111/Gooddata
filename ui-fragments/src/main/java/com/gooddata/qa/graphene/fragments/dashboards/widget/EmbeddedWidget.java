package com.gooddata.qa.graphene.fragments.dashboards.widget;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportContainer;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class EmbeddedWidget extends AbstractFragment {

    @FindBy(tagName = "iframe")
    private WebElement iframe;

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

    private EmbeddedWidget resize(By button, int xOffset, int yOffset) {
        getActions()
                .clickAndHold(waitForElementVisible(button, browser))
                .moveByOffset(xOffset, yOffset)
                .release()
                .perform();

        return this;
    }
}
