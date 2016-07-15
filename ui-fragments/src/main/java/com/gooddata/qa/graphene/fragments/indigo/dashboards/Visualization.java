package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;

public class Visualization extends Widget {
    public static final String MAIN_SELECTOR = ".dash-item.type-visualization";
    public static final String WIDGET_LOADING_CLASS = "widget-loading";
    public static final String CONTENT_LOADING_CLASS = "content-loading";

    private static final By BY_CHART_REPORT = By.className("highcharts-container");

    public static final By IS_WIDGET_LOADING = By.cssSelector(MAIN_SELECTOR + " ." + WIDGET_LOADING_CLASS);
    public static final By IS_CONTENT_LOADING = By.cssSelector(MAIN_SELECTOR + " ." + CONTENT_LOADING_CLASS);

    public ChartReportReact getChartReport() {
        return Graphene.createPageFragment(ChartReportReact.class,
                waitForElementVisible(BY_CHART_REPORT, browser));
    }
}
