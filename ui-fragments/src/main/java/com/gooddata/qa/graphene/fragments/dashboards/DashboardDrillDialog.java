package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;

public class DashboardDrillDialog extends AbstractFragment {

    @FindBy(css = ".reportInfoPanelHandle")
    private WebElement reportInfoButton;

    @FindBy(css = ".breadcrumbsTools .label")
    private List<WebElement> breadcrumbs;

    @FindBy(css = ".charts .yui3-c-charttype")
    private List<WebElement> chartIcons;

    @FindBy(css = "button")
    private WebElement closeButton;

    private static final By REPORT_LOCATOR = By.cssSelector(".report");
    private static final String LOADING = "Loading...";
    public static final By LOCATOR = By.cssSelector(".c-drillDialog");
    private static final By CHART_TITLE_LOCATOR = By.cssSelector("span");

    public static final DashboardDrillDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(DashboardDrillDialog.class, waitForElementVisible(LOCATOR, context));
    }

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
        
        return chartIcons.stream()
                .map(e -> e.findElement(CHART_TITLE_LOCATOR).getAttribute("title").trim())
                .collect(Collectors.toList());
    }

    public String getSelectedChartTitle() {
        waitForElementVisible(this.getRoot());

        for (WebElement e : chartIcons) {
            if (e.getAttribute("class").contains("yui3-c-charttype-selected"))
                return e.findElement(CHART_TITLE_LOCATOR).getAttribute("title").trim();
        }
        return "";
    }

    public void changeChartType(String type) {
        waitForElementVisible(this.getRoot());
        waitForCollectionIsNotEmpty(chartIcons);
        By icon = cssSelector(".charts .yui3-c-charttype.type-${type}.s-enabled".replace("${type}", type));
        waitForElementVisible(icon, this.getRoot()).click();
        waitForElementNotPresent(cssSelector(".c-report.reloading"));
        waitForElementVisible(cssSelector(".c-report"), this.getRoot());
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
        Graphene.waitGui().until(input -> !LOADING.equals(breadcrumbs.get(breadcrumbs.size() - 1).getText()));
    }
}
