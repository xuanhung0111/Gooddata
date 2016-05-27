package com.gooddata.qa.graphene.fragments.dashboards.widget;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportContainer;

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
