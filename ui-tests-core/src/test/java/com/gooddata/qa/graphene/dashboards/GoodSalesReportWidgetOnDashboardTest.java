package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.StyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.Sort;

public class GoodSalesReportWidgetOnDashboardTest extends GoodSalesAbstractTest {

    private static final String REPORT = "Test report";

    private static final String TOP_5_OPEN_REPORT = "Top 5 Open (by $)";

    private static final String VARIABLE_NAME = "variable";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-dashboard-report-widget";
    }

    @BeforeClass
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnMethods = {"createProject"})
    public void workWithHeadlineReport() {
        String reportName = "Headline report";
        String headlineValue = "$116,625,456.54";

        createReport(new UiReportDefinition().withName(reportName).withWhats(METRIC_AMOUNT)
                .withType(ReportTypes.HEADLINE), reportName);

        initDashboardsPage();
        dashboardsPage.addNewDashboard(reportName);

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard(reportName);
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "headline report - description is visible", getClass());

            OneNumberReport headlineReport = verifyHeadlineReport(headlineValue, METRIC_AMOUNT);

            dashboardsPage.editDashboard();
            headlineReport.focus()
                .changeDescription("");
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "headline report - description is invisible", getClass());

            verifyHeadlineReport(headlineValue, "");
            refreshDashboardsPage();
            verifyHeadlineReport(headlineValue, "");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void editReportFromDashboard() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TOP_5_OPEN_REPORT);

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard(TOP_5_OPEN_REPORT);
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "editReportFromDashboard - table report in dashboard", getClass());

            String currentWindowHandle = browser.getWindowHandle();
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.openReportInfoViewPanel().clickViewReportButton();

            // switch to newest window handle
            for (String s : browser.getWindowHandles()) {
                if (!s.equals(currentWindowHandle)) {
                    browser.switchTo().window(s);
                    break;
                }
            }
            waitForAnalysisPageLoaded(browser);

            waitForFragmentVisible(reportPage).selectReportVisualisation(ReportTypes.HEADLINE);
            waitForAnalysisPageLoaded(browser);
            reportPage.clickSaveReport()
                .confirmSaveReport()
                .waitForReportSaved();
            takeScreenshot(browser, "editReportFromDashboard - headline report", getClass());

            browser.close();
            browser.switchTo().window(currentWindowHandle);

            refreshDashboardsPage();
            dashboardsPage.selectDashboard(TOP_5_OPEN_REPORT);
            takeScreenshot(browser, "editReportFromDashboard - headline report in dashboard", getClass());
            OneNumberReport headlineReport = dashboardsPage.getContent().getLatestReport(OneNumberReport.class);
            assertThat(headlineReport.getDescription(), equalTo("Top 5"));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createTestReport() {
        createReport(new UiReportDefinition().withName(REPORT).withWhats("# of Activities")
                .withHows("Department"), REPORT);
    }

    @Test(dependsOnMethods = {"createTestReport"})
    public void sortTableReport() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("Sort table report");

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard(REPORT);
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "sortTableReport - before sorting", getClass());

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertEquals(report.getRawMetricElements(), asList("101,054", "53,217"));

            report.sortByHeader("# of Activities", Sort.ASC);
            takeScreenshot(browser, "sortTableReport - after sorting", getClass());
            assertEquals(report.getRawMetricElements(), asList("53,217", "101,054"));

            refreshDashboardsPage();
            assertEquals(report.getRawMetricElements(), asList("101,054", "53,217"));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void deleteWidgetsByHotKey() {
        String dashboardName = "Delete widgets by hot keys";

        initDashboardsPage();
        dashboardsPage.addNewDashboard(dashboardName);

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            int dashboardWidgetsCount = dashboardEditBar.getDashboardWidgetsCount();

            dashboardEditBar.addReportToDashboard(TOP_5_OPEN_REPORT);
            dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Account");
            assertEquals(dashboardEditBar.getDashboardWidgetsCount(), dashboardWidgetsCount + 2);
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "deleteWidgetsByHotKey - before deleting", getClass());

            // select widgets
            Actions actions = new Actions(browser);
            dashboardsPage.editDashboard();
            dashboardsPage.getContent().getFilters().get(0).getRoot().click();
            WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
            actions.keyDown(Keys.SHIFT).click(report).keyUp(Keys.SHIFT).perform();

            actions.sendKeys(Keys.DELETE).perform();
            takeScreenshot(browser, "deleteWidgetsByHotKey - after deleting", getClass());
            assertEquals(dashboardEditBar.getDashboardWidgetsCount(), dashboardWidgetsCount);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestReport"})
    public void configReportTitleVisibility() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("config report title");

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard(REPORT);
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "configReportTitleVisibility - report title is visible", getClass());

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(report.isReportTitleVisible());

            dashboardsPage.editDashboard();
            WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(report.getRoot(), browser);
            configPanel.getTab(Tab.STYLE, StyleConfigPanel.class).setTitleHidden();
            sleepTightInSeconds(2);
            configPanel.saveConfiguration();
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "configReportTitleVisibility - report title is not visible", getClass());

            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertFalse(report.isReportTitleVisible());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createNumericVariableTest() {
        initVariablePage();
        variablePage.createVariable(new NumericVariable(VARIABLE_NAME)
                .withDefaultNumber(1234)
                .withUserNumber(UserRoles.EDITOR, 5678));
    }

    @Test(dependsOnMethods = {"createNumericVariableTest"})
    public void viewVariableValuesForCurrentUser() throws JSONException {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("view variable values");

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addVariableStatusToDashboard(VARIABLE_NAME);
            takeScreenshot(browser, "viewVariableValuesForCurrentUser - view from admin", getClass());

            assertEquals(dashboardsPage.getContent().getVariableStatus(VARIABLE_NAME).getContent(), "1234");
        } finally {
            dashboardsPage.deleteDashboard();
        }

        logout();
        signIn(false, UserRoles.EDITOR);

        initDashboardsPage();
        dashboardsPage.addNewDashboard("view variable values");

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addVariableStatusToDashboard(VARIABLE_NAME);
            takeScreenshot(browser, "viewVariableValuesForCurrentUser - view from editor", getClass());

            assertEquals(dashboardsPage.getContent().getVariableStatus(VARIABLE_NAME).getContent(), "5678");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void refreshDashboardsPage() {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }

    private OneNumberReport verifyHeadlineReport(String value, String description) {
        OneNumberReport headlineReport = dashboardsPage.getContent().getLatestReport(OneNumberReport.class);
        assertThat(headlineReport.getValue(), equalTo(value));
        assertThat(headlineReport.getDescription(), equalTo(description));
        return headlineReport;
    }
}
