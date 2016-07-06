package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.ReportInfoViewPanel;

public abstract class AbstractReport extends AbstractFragment {

    @FindBy(css = ".reportInfoPanelHandle")
    protected WebElement reportInfoButton;

    public ReportInfoViewPanel openReportInfoViewPanel() {
        waitForElementVisible(this.getRoot());
        waitForElementPresent(reportInfoButton);
        new Actions(browser).moveToElement(this.getRoot()).perform();
        waitForElementVisible(reportInfoButton).click();
        return Graphene.createPageFragment(ReportInfoViewPanel.class,
                waitForElementVisible(By.cssSelector(".reportInfoView"), browser));
    }

    public void closeReportInfoViewPanel() {
        WebElement infoPanel = waitForElementVisible(By.cssSelector(".reportInfoView"), browser);
        waitForElementVisible(reportInfoButton).click();
        waitForElementNotVisible(infoPanel);
    }
}
