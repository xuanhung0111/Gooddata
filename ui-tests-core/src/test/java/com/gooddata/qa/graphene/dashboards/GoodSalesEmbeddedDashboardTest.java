package com.gooddata.qa.graphene.dashboards;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.StyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.login.LoginFragment;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;
import static java.nio.file.Files.deleteIfExists;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesEmbeddedDashboardTest extends GoodSalesAbstractTest {

    private static final int OTHER_WIDGETS_TAB_INDEX = 3;
    private static final int HEADLINE_REPORT_TAB_INDEX = 2;
    private static final int CHART_REPORT_TAB_INDEX = 1;
    private static final int TABULAR_REPORT_TAB_INDEX = 0;
    private static final int NUMBER_OF_TABS = 5;

    private static final String ADDITIONAL_PROJECT_TITLE = "GoodSales-project-to-share-dashboard";
    private static final String EMBEDDED_DASHBOARD_NAME = "Embedded Dashboard";

    private static final long EXPECTED_EXPORT_DASHBOARD_SIZE = 62000L;
    private static final long EXPECTED_EXPORT_CHART_SIZE = 28000L;

    private UiReportDefinition tabularReportDef;
    private List<String> attributeValues;
    private List<Float> metricValues;
    private UiReportDefinition chartReportDef;
    private UiReportDefinition headlineReportDef;
    private UiReportDefinition drillingReportDef;
    private String headlineReportDescription;
    private String headlineReportValue;
    private String sharedDashboardUrl;

    private String embedUri;
    private String htmlEmbedCode;
    private String additionalProjectId = "";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-embedded-dashboard-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createNumberOfActivitiesMetric();

        tabularReportDef = new UiReportDefinition().withName("tabular_report")
                .withWhats(new WhatItem(METRIC_AMOUNT, ATTR_STATUS))
                .withHows(ATTR_YEAR_CREATED);
        chartReportDef = new UiReportDefinition().withName("chart_report")
                .withWhats(METRIC_AMOUNT)
                .withHows(ATTR_YEAR_CREATED)
                .withType(ReportTypes.BAR);
        headlineReportDef = new UiReportDefinition().withName("headline_report")
                .withWhats(METRIC_AMOUNT)
                .withType(ReportTypes.HEADLINE);
        drillingReportDef = new UiReportDefinition().withName("drilling_report")
                .withWhats(new WhatItem(METRIC_NUMBER_OF_ACTIVITIES))
                .withHows(ATTR_ACTIVITY_TYPE);
    }

    @Test(dependsOnGroups = "createProject")
    public void prepareReportsForEmbeddedDashboard() {
        createReport(tabularReportDef, tabularReportDef.getName());
        TableReport tabularReport = reportPage.getTableReport();
        attributeValues = tabularReport.getAttributeValues();
        metricValues = tabularReport.getMetricValues();
        createReport(chartReportDef, chartReportDef.getName());
        createReport(headlineReportDef, headlineReportDef.getName());
        OneNumberReport headlineReport = reportPage.getHeadlineReport();
        headlineReportDescription = headlineReport.getDescription();
        headlineReportValue = headlineReport.getValue();
        createReport(drillingReportDef, drillingReportDef.getName());
    }

    @Test(dependsOnMethods = "prepareReportsForEmbeddedDashboard")
    public void createDashboardToShare() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(EMBEDDED_DASHBOARD_NAME)
                .addReportToDashboard(tabularReportDef.getName())
                .addNewTab("chart_report")
                .addReportToDashboard(chartReportDef.getName())
                .addNewTab("headline_report")
                .addReportToDashboard(headlineReportDef.getName())
                .addNewTab("other_widgets")
                .addLineToDashboard()
                .addWebContentToDashboard("https://s3.amazonaws.com/gdc-testing-public/images/publicImage.png")
                .addWidgetToDashboard(WidgetTypes.KEY_METRIC, METRIC_AMOUNT)
                .addWidgetToDashboard(WidgetTypes.GEO_CHART, METRIC_AMOUNT)
                .addNewTab("drilling_report")
                .addReportToDashboard("drilling_report")
                .saveDashboard();

        dashboardsPage.editDashboard();
        TableReport report = dashboardsPage.openTab(4).getReport(drillingReportDef.getName(), TableReport.class);
        report.addDrilling(Pair.of(Collections.singletonList(ATTR_ACTIVITY_TYPE), tabularReportDef.getName()), 
                "Reports");
        dashboardsPage.saveDashboard();

        sharedDashboardUrl = browser.getCurrentUrl();

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();

        embedUri = embedDashboardDialog.getPreviewURI();
        htmlEmbedCode = embedDashboardDialog.getEmbedCode();
    }

    @Test(dependsOnMethods = "createDashboardToShare")
    public void createAdditionalProject() throws JSONException {
        additionalProjectId = createProjectUsingFixture(ADDITIONAL_PROJECT_TITLE, ResourceTemplate.GOODSALES);
    }

    @DataProvider(name = "embeddedDashboard")
    public Object[][] getEmbeddedDashboardProvider() {
        return new Object[][] {
            {true, false},
            {false, false},
            {false, true}
        };
    }

    @Test(dependsOnMethods = "createAdditionalProject", dataProvider = "embeddedDashboard")
    public void embedDashboard(boolean withIframe, boolean useDashboardUriWithIdentifier) throws IOException {
        EmbeddedDashboard embeddedDashboard;

        if (useDashboardUriWithIdentifier) {
            embeddedDashboard = initEmbeddedDashboardWithUri(getEmbeddedUriWithIdentifier());
        } else if (withIframe) {
            embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, 
                    EMBEDDED_DASHBOARD_NAME);
        } else {
            embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);
        }

        assertEquals(embeddedDashboard.getTabs().getNumberOfTabs(), NUMBER_OF_TABS,
                "Incorrect tab number on embedded dashboard!");

        TableReport tableReport = embeddedDashboard.openTab(TABULAR_REPORT_TAB_INDEX)
                .getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeValues(), is(attributeValues));
        assertThat(tableReport.getMetricValues(), is(metricValues));

        embeddedDashboard
                .openTab(CHART_REPORT_TAB_INDEX)
                .getReport(chartReportDef.getName(), ChartReport.class)
                .openReportInfoViewPanel()
                .downloadReportAsFormat(ExportFormat.PDF);

        try {
            verifyReportExport(ExportFormat.PDF, chartReportDef.getName(), EXPECTED_EXPORT_CHART_SIZE);

        } finally {
            deleteIfExists(Paths.get(getExportFilePath(chartReportDef.getName(), ExportFormat.PDF)));
        }

        OneNumberReport oneNumberReport =  embeddedDashboard.openTab(HEADLINE_REPORT_TAB_INDEX)
                .getReport(headlineReportDef.getName(), OneNumberReport.class);

        assertEquals(oneNumberReport.getDescription(), headlineReportDescription);
        assertEquals(oneNumberReport.getValue(), headlineReportValue);

        if (testParams.isClientDemoEnvironment()) {
            log.info("Client-demo does not support dashboard export");
            return; 
        }

        String exportedDashboardName = embeddedDashboard.printDashboardTab(OTHER_WIDGETS_TAB_INDEX);

        try {
            verifyDashboardExport(exportedDashboardName, "other_widgets", EXPECTED_EXPORT_DASHBOARD_SIZE);

        } finally {
            deleteIfExists(Paths.get(getExportFilePath(exportedDashboardName, ExportFormat.PDF)));
        }

        checkRedBar(browser);
    }

    @Test(dependsOnMethods = "createAdditionalProject", dataProvider = "embeddedDashboard")
    public void configReportTitleVisibility(boolean withIframe, boolean useDashboardUriWithIdentifier) {
        String dashboardName = "Config Report Title On Embedded Dashboard";
        EmbeddedDashboard embeddedDashboard;

        if (useDashboardUriWithIdentifier) {
            embeddedDashboard = initEmbeddedDashboardWithUri(getEmbeddedUriWithIdentifier());
        } else if (withIframe) {
            embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, dashboardName);
        } else {
            embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);
        }

        TableReport tableReport = embeddedDashboard
                .openTab(TABULAR_REPORT_TAB_INDEX)
                .getReport(tabularReportDef.getName(), TableReport.class);

        takeScreenshot(browser,
                "embedded-dashboard-with-iframe-mode-: " + valueOf(withIframe) + "-report-title-is-visible", getClass());
        assertTrue(tableReport.isReportTitleVisible(), "Report title is not visible");

        embeddedDashboard.editDashboard();

        setReportTitleVisibility(tableReport, false);

        embeddedDashboard.saveDashboard();

        try {
            tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

            takeScreenshot(browser,
                    "embedded-dashboard-with-iframe-mode-: " + valueOf(withIframe) + "-report-title-is-invisible",
                    getClass());
            assertFalse(tableReport.isReportTitleVisible(), "Report title is visible");

        } finally {
            embeddedDashboard.editDashboard();

            setReportTitleVisibility(tableReport, true);

            embeddedDashboard.saveDashboard();
        }
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void drillingOnEmbeddedDashboard() {
        // check drill to attribute
        String attributeValueToDrill = "2009";
        List<String> drilledDownReportAttributeValues =
                Lists.newArrayList("Q1/2009", "Q2/2009", "Q3/2009", "Q4/2009");
        List<Float> drilledDownReportMetricValues =
                Lists.newArrayList(1279125.6F, 1881130.8F, 2381755.0F, 3114457.0F);

        String metricValueToDrill = "$2,773,426.95";
        List<String> drilledInReportAttributeValues = Lists.newArrayList("Lost", "Open", "Won");
        List<Float> drilledInReportMetricValues =
                Lists.newArrayList(1980676.11F, 326592.22F, 466158.62F);

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Drill On Embedded Dashboard");

        embeddedDashboard
                .getReport(tabularReportDef.getName(), TableReport.class)
                .drillOn(attributeValueToDrill, CellType.ATTRIBUTE_VALUE);

        DashboardDrillDialog drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

        TableReport drilledTableReport = drillDialog.getReport(TableReport.class);

        assertThat(drilledTableReport.getAttributeValues(), is(drilledDownReportAttributeValues));
        assertThat(drilledTableReport.getMetricValues(), is(drilledDownReportMetricValues));

        drillDialog.closeDialog();

        embeddedDashboard
                .getReport(tabularReportDef.getName(), TableReport.class)
                .drillOn(metricValueToDrill, CellType.METRIC_VALUE);

        drillDialog = Graphene
                .createPageFragment(DashboardDrillDialog.class, waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

        drilledTableReport = drillDialog.getReport(TableReport.class);

        assertThat(drilledTableReport.getAttributeValues(), is(drilledInReportAttributeValues));
        assertThat(drilledTableReport.getMetricValues(), is(drilledInReportMetricValues));

        drillDialog.closeDialog();

        // check drill to report
        embeddedDashboard.openTab(4)
                .getReport(drillingReportDef.getName(), TableReport.class)
                .drillOn("Email", CellType.ATTRIBUTE_VALUE);
        drillDialog = Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));

        drilledTableReport = drillDialog.getReport(TableReport.class);
        assertThat(drilledTableReport.getAttributeValues(), is(Lists.newArrayList("2008", "2009", "2010", "2011",
                "2012")));
        assertThat(drilledTableReport.getMetricValues(), is(Lists.newArrayList(2773426.95F, 8656468.20F,
                29140409.09F, 60270072.20F, 15785080.10F)));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void filterEmbeddedDashboardWithUrlParameter() {
        browser.get(sharedDashboardUrl);

        EmbedDashboardDialog embedDialog = dashboardsPage.openEmbedDashboardDialog();
        String[] filteredAttributeValues = {"2009", "2011"};
        List<Float> filteredMetricValues = Lists.newArrayList(8656468.20F, 60270072.20F);
        embedDialog.selectFilterAttribute(ATTR_YEAR_CREATED, filteredAttributeValues);
        String htmlEmbedCode = embedDialog.getEmbedCode();
        String embedUri = embedDialog.getPreviewURI();

        EmbeddedDashboard embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode,
                additionalProjectId, "Embedded Dashboard With Url Parameter");

        TableReport tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeValues(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricValues(), is(filteredMetricValues));

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeValues(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricValues(), is(filteredMetricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void embedOneTabDashboard() {
        initDashboardsPage()
                .addNewDashboard("One Tab Dashboard")
                .addReportToDashboard(tabularReportDef.getName())
                .saveDashboard();

        EmbedDashboardDialog dashboardEmbedDialog = dashboardsPage.openEmbedDashboardDialog();
        String embedUri = dashboardEmbedDialog.getPreviewURI();
        String htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed One Tab Dashboard");

        assertFalse(embeddedDashboard.isTabBarVisible(), "Tab bar still presents on embedded dashboard with iframe!");

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        assertFalse(embeddedDashboard.isTabBarVisible(), "Tab bar still presents on embedded dashboard without iframe!");

        embeddedDashboard = embedDashboardToOtherProjectDashboard(
                htmlEmbedCode.replace("dashboard.html#", "dashboard.html#nochrome=true&"), additionalProjectId,
                "Embed One Tab Dashboard without Print & Edit button");

        assertFalse(embeddedDashboard.isPrintButtonVisible(), "Print button is still visible!");
        assertFalse(embeddedDashboard.isEditButtonVisible(), "Edit button is still visible!");
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void embeddedDashboardWithFilter() {
        initDashboardsPage()
                .addNewDashboard("Dashboard with filter")
                .addReportToDashboard(tabularReportDef.getName())
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_YEAR_CREATED)
                .saveDashboard();

        EmbedDashboardDialog dashboardEmbedDialog = dashboardsPage.openEmbedDashboardDialog();
        String embedUri = dashboardEmbedDialog.getPreviewURI();
        String htmlEmbedCode = dashboardEmbedDialog.getEmbedCode();

        String[] filteredAttributeValues = {"2009", "2011"};
        List<Float> filteredMetricValues = Lists.newArrayList(8656468.20F, 60270072.20F);

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Embed One Tab Dashboard");

        embeddedDashboard.getFirstFilter().changeAttributeFilterValues(filteredAttributeValues);

        TableReport tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeValues(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricValues(), is(filteredMetricValues));

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        embeddedDashboard.getFirstFilter().changeAttributeFilterValues(filteredAttributeValues);
        tableReport = embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class);

        assertThat(tableReport.getAttributeValues(), is(newArrayList(filteredAttributeValues)));
        assertThat(tableReport.getMetricValues(), is(filteredMetricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void viewEmbeddedDashboardWithNotAuthorizedAccount() throws ParseException, IOException, JSONException {
        String otherUser = createDynamicUserFrom(testParams.getUser());

        try {
            signInAtGreyPages(otherUser, testParams.getPassword());
            browser.get(embedUri);
            String notAuthorizedMessage = LoginFragment.getInstance(browser).getNotAuthorizedMessage();
            log.info("Not authorized message: " + notAuthorizedMessage);
            assertTrue(notAuthorizedMessage.contains("ACCESS DENIED"));
            assertTrue(notAuthorizedMessage
                    .contains("You are not authorized to access this area. Please contact your administrator."));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void openReportOnEmbeddedDashboard() {
        EmbeddedDashboard embeddedDashboard = embedDashboardToOtherProjectDashboard(htmlEmbedCode,
                additionalProjectId, "Open report on embedded dashboard");

        embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class)
                .openReportInfoViewPanel()
                .clickViewReportButton();
        waitForAnalysisPageLoaded(browser);

        TableReport tableReport = Graphene.createPageFragment(ReportPage.class,
                waitForElementVisible(ReportPage.LOCATOR, browser))
                .getTableReport();

        assertThat(tableReport.getAttributeValues(), is(attributeValues));
        assertThat(tableReport.getMetricValues(), is(metricValues));

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        embeddedDashboard.getReport(tabularReportDef.getName(), TableReport.class)
                .openReportInfoViewPanel()
                .clickViewReportButton();
        waitForAnalysisPageLoaded(browser);

        tableReport = Graphene.createPageFragment(ReportPage.class, waitForElementVisible(ReportPage.LOCATOR, browser))
                .getTableReport();

        assertThat(tableReport.getAttributeValues(), is(attributeValues));
        assertThat(tableReport.getMetricValues(), is(metricValues));
    }

    @Test(dependsOnMethods = "createAdditionalProject")
    public void createScheduleOfEmbeddedDashboard() throws JSONException {
        new ProjectRestRequest(new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS, true);

        String defaultScheduleSubject = EMBEDDED_DASHBOARD_NAME + " Dashboard";

        EmbeddedDashboard embeddedDashboard =
                embedDashboardToOtherProjectDashboard(htmlEmbedCode, additionalProjectId, "Schedule on Embedded Dashboard");

        DashboardScheduleDialog scheduleDialog = embeddedDashboard.openTab(CHART_REPORT_TAB_INDEX)
                .showDashboardScheduleDialog();

        assertEquals(scheduleDialog.getSelectedFrequency(), "Daily", "Incorrect default frequency!");
        assertEquals(scheduleDialog.getSelectedTab(), "Chart_report", "Incorrect default tab!");

        String scheduleSubject = scheduleDialog.showCustomForm().getCustomEmailSubject();
        assertEquals(scheduleSubject, defaultScheduleSubject + " - chart_report", "Incorrect default subject!");

        String customScheduleSubject = defaultScheduleSubject + System.currentTimeMillis();

        scheduleDialog.setCustomEmailSubject(customScheduleSubject)
                .setCustomRecipients(Lists.newArrayList(testParams.getViewerUser()))
                .schedule();

        assertTrue(initEmailSchedulesPage().isPrivateSchedulePresent(customScheduleSubject));
        WebElement schedule1 = EmailSchedulePage.getInstance(browser).getPrivateSchedule(customScheduleSubject);
        assertEquals(EmailSchedulePage.getInstance(browser).getBccEmailsOfPrivateSchedule(schedule1), testParams.getViewerUser());

        embeddedDashboard = initEmbeddedDashboardWithUri(embedUri);

        scheduleDialog = embeddedDashboard.openTab(HEADLINE_REPORT_TAB_INDEX)
                .showDashboardScheduleDialog();

        assertEquals(scheduleDialog.getSelectedFrequency(), "Daily", "Incorrect default frequency!");
        assertEquals(scheduleDialog.getSelectedTab(), "Headline_report", "Incorrect default tab!");

        scheduleSubject = scheduleDialog.showCustomForm().getCustomEmailSubject();
        assertEquals(scheduleSubject, defaultScheduleSubject + " - headline_report", "Incorrect default subject!");

        customScheduleSubject = defaultScheduleSubject + System.currentTimeMillis();

        scheduleDialog.setCustomEmailSubject(customScheduleSubject)
                .setCustomRecipients(Lists.newArrayList(testParams.getViewerUser()))
                .schedule();

        assertTrue(initEmailSchedulesPage().isPrivateSchedulePresent(customScheduleSubject));
        WebElement schedule2 = EmailSchedulePage.getInstance(browser).getPrivateSchedule(customScheduleSubject);
        assertEquals(EmailSchedulePage.getInstance(browser).getBccEmailsOfPrivateSchedule(schedule2), testParams.getViewerUser());
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws JSONException {
        if (!"".equals(additionalProjectId)) {
            deleteProject(additionalProjectId);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private EmbeddedDashboard embedDashboardToOtherProjectDashboard(String embedCode, String projectToShare,
            String dashboardName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectToShare + DASHBOARD_PAGE_SUFFIX);
        waitForDashboardPageLoaded(browser);

        EmbeddedWidget embeddedWidget = dashboardsPage
                .addNewDashboard(dashboardName)
                .addWebContentToDashboard(embedCode)
                .getLastEmbeddedWidget()
                .resizeFromTopLeftButton(-300, 0)
                .resizeFromBottomRightButton(200, 600);

        dashboardsPage.saveDashboard();
        return embeddedWidget.getEmbeddedDashboard();
    }

    private EmbeddedDashboard initEmbeddedDashboardWithUri(String uri) {
        browser.get(uri);
        return Graphene.createPageFragment(EmbeddedDashboard.class, waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
    }

    private void setReportTitleVisibility(TableReport report, boolean visible) {
        WidgetConfigPanel configPanel = WidgetConfigPanel.openConfigurationPanelFor(report.getRoot(), browser);
        StyleConfigPanel stylePanel = configPanel.getTab(Tab.STYLE, StyleConfigPanel.class);

        if (visible) {
            stylePanel.setTitleVisible();

        } else {
            stylePanel.setTitleHidden();
        }

        configPanel.saveConfiguration();
    }

    private String getExportFilePath(String name, ExportFormat format) {
        return testParams.getDownloadFolder() + testParams.getFolderSeparator() + name + "." + format.getName();
    }

    private String getEmbeddedUriWithIdentifier() {
        String embeddedUriWithIdentifier = "";
        String [] strSplit = embedUri.split("#");
        String [] embeddedParams = strSplit[1].split("&");

        embeddedUriWithIdentifier += strSplit[0] + "#";
        for (int i = 0; i < embeddedParams.length; i++) {
            if (i > 0) {
                embeddedUriWithIdentifier += "&";
            }

            if (embeddedParams[i].startsWith("dashboard=")) {
                String dashboardUri = embeddedParams[i].split("=")[1];
                String dashboardIdentifier = getObjIdentifiers(Arrays.asList(dashboardUri)).get(0);
                embeddedParams[i] = embeddedParams[i].replace(
                        embeddedParams[i].split("/obj/")[1], "identifier:" + dashboardIdentifier);
            }
            embeddedUriWithIdentifier += embeddedParams[i];
        }
        return embeddedUriWithIdentifier;
    }
}
