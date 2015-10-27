package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardAddWidgetPanel;

public class GoodSalesPersonalObjectsInDashboardWidgetTest extends GoodSalesAbstractTest {

    private String personalMetric;
    private String personalReport;

    @BeforeClass(alwaysRun = true)
    public void before() {
        projectTitle = "GoodSales-Personal-Objects-In-Dashboard-Widget";
        addUsersWithOtherRoles = true;
        personalReport = "[Personal] Report";
        personalMetric = "[Personal] Share %";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"pre-condition"})
    public void createPersonalMetric() {
        initMetricPage();
        personalMetric = "[Personal] Share %";
        waitForFragmentVisible(metricPage).createShareMetric(personalMetric, "Amount", "Date dimension (Snapshot)",
                "Year (Snapshot)");
    }

    @Test(dependsOnMethods = {"createPersonalMetric"}, groups = {"pre-condition"})
    public void createPersonalReport() {
        initReportsPage();
        UiReportDefinition reportDefinition = new UiReportDefinition()
            .withName(personalReport)
            .withWhats(personalMetric);
        createReport(reportDefinition, personalReport);
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"pre-condition"})
    public void testPersonalObjects() throws JSONException {
        String dashboardName = "Personal dashboard";
        logout();
        signIn(false, UserRoles.EDITOR);

        try {
            initDashboardsPage();
            dashboardsPage.addNewDashboard(dashboardName);
            dashboardsPage.editDashboard();

            checkCannotFindPersonalReport();
            Stream.of(WidgetTypes.KEY_METRIC, WidgetTypes.KEY_METRIC_WITH_TREND, WidgetTypes.GEO_CHART)
                .forEach(this::checkCannotFindPersonalMetric);

            dashboardsPage.getDashboardEditBar().cancelDashboard();

        } finally {
            dashboardsPage.deleteDashboard();

            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    private void checkCannotFindPersonalReport() {
        waitForElementVisible(className("s-btn-report"), browser).click();
        waitForElementVisible(cssSelector(".searchfield input"), browser).sendKeys(personalReport);
        assertTrue(isElementPresent(className("gd-list-view-noResults"), browser));
    }

    private void checkCannotFindPersonalMetric(WidgetTypes type) {
        waitForElementVisible(className("s-btn-widget"), browser).click();

        Graphene.createPageFragment(DashboardAddWidgetPanel.class,
                waitForElementVisible(className("yui3-c-adddashboardwidgetpickerpanel-content"), browser))
                .initWidget(type);

        waitForElementVisible(className("s-btn-select_metric___"), browser).click();
        waitForElementVisible(cssSelector(".gdc-picker:not(.gdc-hidden) .s-search-field input"), browser)
            .sendKeys(personalMetric);
        assertEquals(waitForElementVisible(className("emptyMessage"), browser).getText(),
                "No metrics match your search criteria");
    }
}
