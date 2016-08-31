package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class Insight extends Widget {
    public static final String MAIN_SELECTOR = ".dash-item.type-visualization";

    private static final By BY_CHART_REPORT = By.className("highcharts-container");

    public static Insight getInstance(final WebElement root) {
        return Graphene.createPageFragment(Insight.class, waitForElementVisible(root));
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class,
                waitForElementVisible(BY_CHART_REPORT, browser));
    }

    public static boolean isInsight(final Widget widget) {
        return widget.getRoot().getAttribute("class").contains("type-visualization");
    }
}
