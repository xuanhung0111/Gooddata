package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import org.openqa.selenium.WebElement;

import java.util.UUID;

public abstract class AbstractDashboardWidgetTest extends GoodSalesAbstractTest {

    protected FilterWidget getFilter(String name) {
        FilterWidget filter = dashboardsPage.getFilterWidgetByName(name);
        focusWidget(filter.getRoot());

        return filter;
    }

    protected TableReport getReport(String name) {
        TableReport report = dashboardsPage.getReport(name, TableReport.class);
        focusWidget(report.getRoot());

        return report;
    }

    protected String generateDashboardName() {
        return "Dashboard-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private void focusWidget(WebElement dashboardWidget) {
        if (!isWidgetFocus(dashboardWidget)) {
            dashboardWidget.click();
        }
    }

    // There are two cases that consider the widget is focus:
    // 1. Widget is in view mode: we can do action directly on it
    // 2. Widget is in edit mode and css class contains Attribute "selected"
    private boolean isWidgetFocus(WebElement dashboardWidget) {
        return !dashboardWidget.getAttribute("class").contains("editMode")
                || dashboardWidget.getAttribute("class").contains("selected");
    }

}
