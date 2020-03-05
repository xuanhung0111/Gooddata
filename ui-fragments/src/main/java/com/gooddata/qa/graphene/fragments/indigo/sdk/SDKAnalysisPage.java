package com.gooddata.qa.graphene.fragments.indigo.sdk;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.Headline;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;

import java.util.concurrent.TimeUnit;

public class SDKAnalysisPage extends AnalysisPage {

    public static final SDKAnalysisPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(SDKAnalysisPage.class, waitForElementVisible(id("root"), context));
    }

    public Headline getHeadline() {
        return Graphene.createPageFragment(Headline.class,
                waitForElementVisible(className("headline"), getRoot()));
    }

    public PivotTableReport getPivotTableReport() {
        return Graphene.createPageFragment(PivotTableReport.class,
                waitForElementVisible(className("s-pivot-table"), getRoot()));
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class,
                waitForElementVisible(className("viz-line-family-chart-wrap"), getRoot()));
    }

    public String getAppIntro() {
        return waitForElementPresent(className("App-intro"), browser).getText();
    }

    public String getWarning() {
        return waitForElementPresent(className("s-error"), browser).getText();
    }

    public void waitForVisualizationLoading() {
        By loadingWheel = By.className("s-loading");
        try {
            Graphene.waitGui().withTimeout(1, TimeUnit.SECONDS).until(browser ->
                    isElementVisible(loadingWheel, getRoot()));
        } catch (TimeoutException e) {
            //do nothing
        }
        waitForElementNotPresent(loadingWheel, getRoot());
    }
}
