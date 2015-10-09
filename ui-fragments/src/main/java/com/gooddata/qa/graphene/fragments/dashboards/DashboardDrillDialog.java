package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class DashboardDrillDialog extends AbstractFragment {

    @FindBy(css = ".reportInfoPanelHandle")
    private WebElement reportInfoButton;

    @FindBy(css = ".breadcrumbsTools .label")
    private List<WebElement> breadcrumbs;

    @FindBy(css = ".charts .c-chartType")
    private List<WebElement> chartIcons;

    @FindBy(css = "button")
    private WebElement closeButton;

    private static final By REPORT_LOCATOR = By.cssSelector(".report");
    private static final String LOADING = "Loading...";
    public static final By LOCATOR = By.cssSelector(".c-drillDialog");

    public <T extends AbstractReport> T getReport(Class<T> clazz) {
        return Graphene.createPageFragment(clazz, waitForElementVisible(REPORT_LOCATOR, browser));
    }

    public void closeDialog() {
        waitForElementVisible(closeButton).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void clickOnBreadcrumbs(String label) {
        waitForBreadcrumbsLoaded();
        for (WebElement e : breadcrumbs) {
            if (!label.equals(e.getText().trim())) continue;
            e.click();
            break;
        }
    }

    public String getBreadcrumbsString() {
        waitForBreadcrumbsLoaded();
        return StringUtils.join(Collections2.transform(breadcrumbs,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.getText().trim();
            }
        }),">>");
    }

    public String getBreadcrumbTitle(String label) {
        waitForBreadcrumbsLoaded();
        for (WebElement e : breadcrumbs) {
            if (label.equals(waitForElementVisible(e).getText().trim()))
                return e.getAttribute("title").trim();
        }
        throw new IllegalArgumentException(String.format("Breadcrumb '%s' is not visible!", label));
    }

    public List<String> getChartTitles() {
        waitForElementVisible(this.getRoot());
        final By spanSelector = By.cssSelector("span");

        return Lists.newArrayList(Collections2.transform(chartIcons,
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement input) {
                return input.findElement(spanSelector).getAttribute("title").trim();
            }
        }));
    }

    public String getSelectedChartTitle() {
        waitForElementVisible(this.getRoot());

        for (WebElement e : chartIcons) {
            if (e.findElement(BY_PARENT).getAttribute("class")
                    .contains("yui3-c-charttype-selected"))
                return e.findElement(By.cssSelector("span")).getAttribute("title").trim();
        }
        return "";
    }

    public void changeChartType(String title) {
        waitForElementVisible(this.getRoot());
        waitForCollectionIsNotEmpty(chartIcons);
        for (WebElement e : chartIcons) {
            if (!title.equals(waitForElementVisible(e)
                    .findElement(By.cssSelector("span")).getAttribute("title").trim()))
                continue;
            e.click();
            return;
        }
    }

    public boolean isReportInfoButtonVisible() {
        return waitForElementPresent(reportInfoButton).isEnabled();
    }

    public ReportInfoViewPanel openReportInfoViewPanel() {
        waitForElementVisible(this.getRoot());
        waitForElementPresent(reportInfoButton);
        if ("none".equals(reportInfoButton.getCssValue("display"))) {
            // hover
        }
        waitForElementVisible(reportInfoButton).click();
        return Graphene.createPageFragment(ReportInfoViewPanel.class,
                waitForElementVisible(this.getRoot().findElement(By.cssSelector(".reportInfoView"))));
    }

    private void waitForBreadcrumbsLoaded() {
        waitForElementVisible(this.getRoot());
        waitForCollectionIsNotEmpty(breadcrumbs);
        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !LOADING.equals(breadcrumbs.get(breadcrumbs.size() - 1).getText());
            }
        });
    }
}
