package com.gooddata.qa.graphene.fragments.reports.report;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Fragment represents chart report in
 *  - Dashboard page
 *  - Report page
 *  - Drill dialog
 */
public class ChartReport extends AbstractDashboardReport {

    @FindBy(css = ".yui3-c-chart")
    protected WebElement chartEl;

    public boolean isChart() {
        return chartEl != null;
    }
}
