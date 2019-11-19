package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.TABLE_BODY_FONT_SIZE;
import static com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags.TABLE_HEADER_FONT_SIZE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOP_5_OPEN_BY_CASH;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_TOP_5_OF_BEST_CASE;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.http.InvalidStatusCodeException;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.StyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.Sort;

public class GoodSalesReportWidgetOnDashboardTest extends GoodSalesAbstractTest {

    private static final String REPORT = "Test report";
    private static final String VARIABLE_NAME = "variable";
    private static final String TABLE_REPORT = "Table Report";
    private String sourceProjectId;
    private String targetProjectId;
    private ProjectRestRequest projectRestRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-dashboard-report-widget";
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createAnotherProject() {
        sourceProjectId = testParams.getProjectId();
        targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE" + generateHashString());
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfActivitiesMetric();
        getReportCreator().createTop5OpenByCashReport();
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
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

    @Test(dependsOnGroups = {"createProject"})
    public void editReportFromDashboard() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(REPORT_TOP_5_OPEN_BY_CASH);

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard(REPORT_TOP_5_OPEN_BY_CASH);
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
            dashboardsPage.selectDashboard(REPORT_TOP_5_OPEN_BY_CASH);
            takeScreenshot(browser, "editReportFromDashboard - headline report in dashboard", getClass());
            OneNumberReport headlineReport = dashboardsPage.getContent().getLatestReport(OneNumberReport.class);
            assertThat(headlineReport.getDescription(), equalTo(METRIC_TOP_5_OF_BEST_CASE));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
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
            assertEquals(report.getRawMetricValues(), asList("101,054", "53,217"));

            report.sortBy("# of Activities", CellType.METRIC_HEADER, Sort.ASC);
            takeScreenshot(browser, "sortTableReport - after sorting", getClass());
            assertEquals(report.getRawMetricValues(), asList("53,217", "101,054"));

            refreshDashboardsPage();
            assertEquals(report.getRawMetricValues(), asList("101,054", "53,217"));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteWidgetsByHotKey() {
        String dashboardName = "Delete widgets by hot keys";

        initDashboardsPage();
        dashboardsPage.addNewDashboard(dashboardName);

        try {
            dashboardsPage.editDashboard();

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            int dashboardWidgetsCount = dashboardEditBar.getDashboardWidgetsCount();

            dashboardEditBar
                    .addReportToDashboard(REPORT_TOP_5_OPEN_BY_CASH)
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Account");
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
            assertTrue(report.isReportTitleVisible(), "Report title should be visible");

            dashboardsPage.editDashboard();
            WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(report.getRoot(), browser);
            configPanel.getTab(Tab.STYLE, StyleConfigPanel.class).setTitleHidden();
            sleepTightInSeconds(2);
            configPanel.saveConfiguration();
            dashboardEditBar.saveDashboard();
            takeScreenshot(browser, "configReportTitleVisibility - report title is not visible", getClass());

            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertFalse(report.isReportTitleVisible(), "Report title shouldn't be visible");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createNumericVariableTest() throws ParseException, JSONException, IOException {
        UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        initVariablePage()
                .createVariable(new NumericVariable(VARIABLE_NAME)
                .withDefaultNumber(1234)
                .withUserSpecificNumber(userManagementRestRequest
                        .getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser()), 5678));
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

        logoutAndLoginAs(false, UserRoles.EDITOR);

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

    @Test(dependsOnGroups = {"createProject"})
    public void prepareTableReport() {
        createReport(new UiReportDefinition().withName(TABLE_REPORT).withWhats(METRIC_AMOUNT).withHows(ATTR_ACCOUNT)
            .withType(ReportTypes.TABLE), TABLE_REPORT);
    }

    @Test(dependsOnMethods = {"prepareTableReport"})
    public void disableAlternateColorOfRows() {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, true);

            initDashboardsPage().addReportToDashboard(TABLE_REPORT);

            TableReport report = dashboardsPage.getContent().getReport(TABLE_REPORT, TableReport.class);
            assertEquals(getRowElementsFrom(report).get(0).getCssValue("background-color"),
                getRowElementsFrom(report).get(1).getCssValue("background-color"));
        } finally {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, false);
        }
    }

    @Test(dependsOnMethods = {"prepareTableReport"})
    public void setNegativeFontSizeForTableHeaderAndBody() throws IOException {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, true);
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "0");
            assertEquals(projectRestRequest.getValueOfProjectFeatureFlag(TABLE_HEADER_FONT_SIZE.getFlagName()), "0");

            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "0");
            assertEquals(projectRestRequest.getValueOfProjectFeatureFlag(TABLE_BODY_FONT_SIZE.getFlagName()), "0");
            try {
                projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "-5");
            } catch (InvalidStatusCodeException e) {
                assertEquals(e.getStatusCode(), 400);
            }
            try {
                projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "-5");
            } catch (InvalidStatusCodeException e) {
                assertEquals(e.getStatusCode(), 400);
            }
            try {
                projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "string");
            } catch (InvalidStatusCodeException e) {
                assertEquals(e.getStatusCode(), 400);
            }

        } finally {
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, false);
        }
    }

    @Test(dependsOnMethods = {"prepareTableReport"})
    public void setMore20FontSizeForTableHeaderAndBody() throws IOException {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, true);
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "50");
            initDashboardsPage().addReportToDashboard(TABLE_REPORT);
            TableReport report = dashboardsPage.getContent().getReport(TABLE_REPORT, TableReport.class);
            assertThat(getRowElementsFrom(report).get(0).getAttribute("style"),
                containsString("font-size: 50px"));

            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "50");
            initDashboardsPage().addReportToDashboard(TABLE_REPORT);

            report = dashboardsPage.getContent().getReport(TABLE_REPORT, TableReport.class);

            assertThat(report.getCellElements(CellType.METRIC_VALUE).get(0).getAttribute("style"),
                containsString("font-size: 50px"));
            assertThat(report.getCellElements(CellType.ATTRIBUTE_VALUE).get(0).getAttribute("style"),
                containsString("font-size: 50px"));

        } finally {
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, false);
        }
    }

    @Test(dependsOnMethods = {"prepareTableReport"})
    public void setFontSizeCombineApplyingMetricFormatting() throws IOException{
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, true);
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "18");
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "18");

            String metricName = "Metric Background Color Format";
            String tableReportName = "Table Background Color";
            createMetric(metricName, format(
                "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))),
                MetricFormatterDialog.Formatter.BACKGROUND_COLOR_FORMAT.toString());
            createReport(new UiReportDefinition().withName(tableReportName).withWhats(metricName)
                .withHows(ATTR_DEPARTMENT).withType(ReportTypes.TABLE), TABLE_REPORT);

            initDashboardsPage().addReportToDashboard(tableReportName);
            TableReport report = dashboardsPage.getContent().getReport(tableReportName, TableReport.class);
            assertThat(report.getCellElements(CellType.METRIC_VALUE).get(0).getAttribute("style"),
                containsString("font-size: 18px; background: rgb(175, 248, 239)"));

            assertThat(report.getCellElements(CellType.ATTRIBUTE_VALUE).get(0).getAttribute("style"),
                containsString("font-size: 18px"));
        } finally {
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, false);
        }
    }

    @Test(dependsOnMethods = {"prepareTableReport"})
    public void keepFontSizeAfterDoingActions() throws IOException{
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, true);
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "18");
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "18");

            initDashboardsPage().addReportToDashboard(TABLE_REPORT);
            TableReport report = dashboardsPage.getContent().getReport(TABLE_REPORT, TableReport.class);
            report.addDrilling(Pair.of(Arrays.asList(METRIC_AMOUNT), ATTR_STAGE_NAME));
            report.drillOnFirstValue(CellType.METRIC_VALUE);

            DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

            assertThat(drillDialog.getReport(TableReport.class).getCellElements(CellType.METRIC_VALUE)
                    .get(0).getAttribute("style"), containsString("font-size: 18px"));

            drillDialog.closeDialog();
            report.waitForLoaded();

            report.sortBy(METRIC_AMOUNT, CellType.METRIC_HEADER, Sort.DESC).waitForLoaded();
            assertThat(report.getCellElements(CellType.METRIC_VALUE)
                .get(0).getAttribute("style"), containsString("font-size: 18px"));
        } finally {
            projectRestRequest.updateProjectConfiguration(TABLE_HEADER_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, false);
        }
    }

    @Test(dependsOnMethods = {"prepareTableReport"})
    public void testExportAndImportProjectWithInsight() throws IOException{
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, true);
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "50");
            final int statusPollingCheckIterations = 60; // (60*5s)
            String exportToken = exportProject(
                true, true, true, statusPollingCheckIterations);
            testParams.setProjectId(targetProjectId);
            try {
                importProject(exportToken, statusPollingCheckIterations);
                initDashboardsPage().addReportToDashboard(TABLE_REPORT);

                TableReport report = dashboardsPage.getContent().getReport(TABLE_REPORT, TableReport.class);
                assertThat(getRowElementsFrom(report).get(0).getAttribute("style"),
                    containsString("font-size: 50px"));
            } finally {
                testParams.setProjectId(sourceProjectId);
            }
        }finally {
            projectRestRequest.updateProjectConfiguration(TABLE_BODY_FONT_SIZE.getFlagName(), "12");
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.DISABLE_ZEBRA_EFFECT, false);
        }
    }

    private List<WebElement> getRowElementsFrom(TableReport report) {
        sleepTightInSeconds(1);
        return report.getRoot().findElements(By.cssSelector(".gridTile div.element.cell.rows"));
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
