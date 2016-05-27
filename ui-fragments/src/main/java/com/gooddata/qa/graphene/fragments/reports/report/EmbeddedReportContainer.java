package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class EmbeddedReportContainer extends AbstractReport {

    public static final By LOCATOR = By.className("yui3-c-reportdashboardwidget");

    @FindBy(id = "reportInfoContainer")
    private WebElement reportInfo;

    public String getInfo() {
        return waitForElementVisible(reportInfo).getText();
    }

    public TableReport getTableReport() {
        return Graphene.createPageFragment(TableReport.class,
                waitForElementVisible(By.className("c-report-container"), getRoot()));
    }

    public OneNumberReport getHeadlineReport() {
        return Graphene.createPageFragment(OneNumberReport.class,
                waitForElementVisible(By.className("c-oneNumberReport"), getRoot()));
    }
}
