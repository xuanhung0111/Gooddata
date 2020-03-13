package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DrillModalDialog extends AbstractFragment {

    @FindBy(className = "s-drill-close-button")
    private WebElement closeButton;

    @FindBy(className = "s-drill-title")
    private WebElement titleInsight;

    private static final By ROOT = className("s-drill-modal-dialog");
    private static final By BY_CHART_REPORT = className("highcharts-container");

    public static DrillModalDialog getInstance(final SearchContext searchContext) {
        return Graphene.createPageFragment(DrillModalDialog.class,
            waitForElementVisible(ROOT, searchContext));
    }

    public String getTitleInsight() {
        return waitForElementVisible(titleInsight).getText();
    }

    public void close() {
        waitForElementVisible(closeButton).click();
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class, waitForElementVisible(BY_CHART_REPORT, getRoot()));
    }
}
