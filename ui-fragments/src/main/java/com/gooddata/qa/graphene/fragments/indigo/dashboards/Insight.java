package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;

public class Insight extends Widget {
    public static final String MAIN_SELECTOR = ".dash-item.type-visualization";

    private static final By BY_CHART_REPORT = By.className("highcharts-container");
    private static final By BY_TABLE_REPORT = By.className("gd-base-visualization");

    public static Insight getInstance(final WebElement root) {
        return Graphene.createPageFragment(Insight.class, waitForElementVisible(root));
    }

    public ChartReport getChartReport() {
        return Graphene.createPageFragment(ChartReport.class,
                waitForElementVisible(BY_CHART_REPORT, getRoot()));
    }

    public TableReport getTableReport() {
        return Graphene.createPageFragment(TableReport.class,
                waitForElementVisible(BY_TABLE_REPORT, getRoot()));
    }

    public static boolean isInsight(final Widget widget) {
        return widget.getRoot().getAttribute("class").contains("type-visualization");
    }

    public boolean isEmptyValue() {
        return isElementPresent(By.className("info-label-icon-empty"), getRoot());
    }
}
