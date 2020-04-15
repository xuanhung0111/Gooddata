package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Variables;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.common.FilterContextHerokuAppPage;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.graphene.utils.GoodSalesUtils;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.RIGHT;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.DOWN;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.BOTTOM;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.LEFT;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.UP;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.MIDDLE;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.PENULTIMATE_BOTTOM;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CLOSE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_TIMELINE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_CREATED;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesFilterEmbeddedDashboardOuterAppTest extends AbstractEmbeddedModeTest {

    private final static int CURRENT_YEAR = LocalDate.now().getYear();

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String NEGOTIATION = "Negotiation";
    private static final String CLOSED_WON = "Closed Won";
    private static final String CLOSED_LOST = "Closed Lost";
    private static final String ALL = "All";
    private static final String THIS = "this";
    private static final String GDC_SELECT_ALL = "GDC_SELECT_ALL";
    private static final String FIRST_TAB = "First Tab";
    private static final String SECOND_TAB = "Second Tab";
    private static final String FIRST_REPORT = "First Report";
    private static final String SECOND_REPORT = "Second Report";
    private static final String FIRST_DASHBOARD = "First Dashboard";
    private static final String VARIABLE_STAGENAME = "V Stage Name";
    private static final String DATE_DIMENSION_CREATED = "Date Dimension (Created)";
    private static final String DATE_DIMENSION_SNAPSHOT = "Date Dimension (Snapshot)";
    private static final String DATE_DIMENSION_TIMELINE = "Date Dimension (Timeline)";
    private static final String DATE_DIMENSION_CLOSED = "Date Dimension (Closed)";
    private static final String DATE_DIMENSION_ACTIVITY = "Date Dimension (Activity)";

    private String variableUri;
    private String stageNameId;
    private String salesRepId;
    private String yearCreatedId;
    private String yearSnapshotId;
    private String yearCloseId;
    private String yearActivityId;
    private String yearTimelineId;
    private String quarterCreatedId;
    private String dashboardTitle;

    private DashboardRestRequest dashboardRestRequest;
    private FilterItemContent allValuesFilterStageName;
    private FilterItemContent allValuesFilterYearAttributeCreated;
    private FilterItemContent dateFilterCreatedContent;
    private FilterItemContent dateFilterCloseContent;
    private FilterItemContent dateFilterSnapshotContent;
    private FilterItemContent dateFilterActivityContent;
    private FilterItemContent dateFilterTimelineContent;

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.CONTROL_EXECUTION_CONTEXT_ENABLED, true);
        getMetricCreator().createAmountMetric();

        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());

        Variables variables = getVariableCreator();

        variableUri = variables.createFilterVariable(VARIABLE_STAGENAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
            asList(DISCOVERY, SHORT_LIST, CONVICTION, CLOSED_WON));

        stageNameId = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getIdentifier();
        salesRepId = getAttributeByTitle(ATTR_SALES_REP).getDefaultDisplayForm().getIdentifier();
        yearCreatedId = getAttributeByTitle(ATTR_YEAR_CREATED).getDefaultDisplayForm().getIdentifier();
        yearSnapshotId = getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getDefaultDisplayForm().getIdentifier();

        allValuesFilterStageName = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        allValuesFilterYearAttributeCreated = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_CREATED));

        createReport(GridReportDefinitionContent.create(FIRST_REPORT,
            singletonList(METRIC_GROUP),
            asList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME)),
                new AttributeInGrid(getAttributeByTitle(ATTR_SALES_REP)),
                new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_CREATED))),
            singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        createReport(GridReportDefinitionContent.create(SECOND_REPORT,
            singletonList(METRIC_GROUP),
            singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
            singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
            asList(new Filter(format("[%s]", variableUri)))));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithMultipleMode() throws IOException {
        prepareDashboard();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        setFilterContextAndWaitForLoading(singletonList(ATTR_STAGE_NAME), stageNameId, singletonList(GDC_SELECT_ALL),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), ALL);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), ALL);
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString("Interest, Adam Bradley, 2010, 2011"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItems(DISCOVERY, SHORT_LIST, CONVICTION, CLOSED_WON));

        setFilterContextAndWaitForLoading(EMPTY_LIST, stageNameId, asList(INTEREST, DISCOVERY, SHORT_LIST,
            RISK_ASSESSMENT, CONVICTION, NEGOTIATION, CLOSED_WON, CLOSED_LOST),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), ALL);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), ALL);
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(INTEREST + ", Adam Bradley, 2010, 2011"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItems(DISCOVERY, SHORT_LIST, CONVICTION, CLOSED_WON));

        setFilterContextAndWaitForLoading(EMPTY_LIST, stageNameId, asList(INTEREST, SHORT_LIST),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), INTEREST + ", " + SHORT_LIST);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), SHORT_LIST);
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(INTEREST + ", Adam Bradley, 2010, 2011"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItems(SHORT_LIST));

        setFilterContextAndWaitForLoading(EMPTY_LIST, salesRepId, singletonList("Adam Bradley"),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_SALES_REP), "Adam Bradley");
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(SHORT_LIST + ", Adam Bradley, " + 2011));
        assertThat(getAttributeValues(SECOND_REPORT), hasItem(SHORT_LIST));

        setFilterContextAndWaitForLoading(EMPTY_LIST, yearCreatedId,
            asList("2007", "2008", "2009", "2010", "2011", "2012", "2013"), asList(FIRST_REPORT, SECOND_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithMultipleMode", this.getClass());

        assertEquals(getCurrentValueFilter(ATTR_YEAR_CREATED), "2007, 2008, 2009, 2010, 2011, 2012, 2013");
        assertEquals(getCurrentValueFilter(DATE_DIMENSION_CREATED), "2008 - 2012");
        assertThat(getAttributeValues(FIRST_REPORT).toString(),
            containsString(INTEREST + ", Adam Bradley, 2010, 2011, 2012"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItems(SHORT_LIST));
    }

    @Test(dependsOnMethods = "filterFromOuterApp_ToGDCEmbeddedDashboard_WithMultipleMode")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithSingleMode() {
        initDashboardsPage().selectDashboard(FIRST_DASHBOARD).editDashboard();
        getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue().editAttributeFilterValues(SHORT_LIST);
        getFilter(VARIABLE_STAGENAME).changeSelectionToOneValue().editAttributeFilterValues(SHORT_LIST);
        getFilter(ATTR_YEAR_CREATED).changeSelectionToOneValue().editAttributeFilterValues("2010");
        dashboardsPage.saveDashboard();
        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        setFilterContextAndWaitForLoading(singletonList(ATTR_STAGE_NAME), stageNameId, singletonList(GDC_SELECT_ALL),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), SHORT_LIST);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), SHORT_LIST);
        assertThat(getAttributeValues(FIRST_REPORT).toString(),
            containsString(SHORT_LIST + ", Alexsandr Fyodr, 2010"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItems(SHORT_LIST));

        setFilterContextAndWaitForLoading(EMPTY_LIST, stageNameId, singletonList(INTEREST),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), INTEREST);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), DISCOVERY);
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(INTEREST + ", Adam Bradley, 2010"));
        assertEquals(getAttributeValues(SECOND_REPORT), EMPTY_LIST);

        setFilterContextAndWaitForLoading(EMPTY_LIST, yearCreatedId, singletonList(GDC_SELECT_ALL),
            singletonList(FIRST_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_YEAR_CREATED), "2010");
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(INTEREST + ", Adam Bradley, 2010"));

        setFilterContextAndWaitForLoading(EMPTY_LIST, yearCreatedId, asList("2008", "2009", "2010", "2011", "2012", "2013"),
            singletonList(FIRST_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithSingleMode", this.getClass());

        assertEquals(getCurrentValueFilter(ATTR_YEAR_CREATED), "2008");
        assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(INTEREST + ", Thomas Gones, 2008"));
    }

    @Test(dependsOnMethods = "filterFromOuterApp_ToGDCEmbeddedDashboard_WithSingleMode")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithTheSameModeLinkingTab() {
        initDashboardsPage().selectDashboard(FIRST_DASHBOARD).editDashboard();
        getFilter(ATTR_STAGE_NAME).changeSelectionToMultipleValues()
            .editAttributeFilterValues(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT,
                CONVICTION, NEGOTIATION, CLOSED_WON, CLOSED_LOST);
        dashboardsPage.renameTab(0, FIRST_TAB).duplicateDashboardTab(0).renameTab(1, SECOND_TAB)
            .saveDashboard();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        try {
            setFilterContextAndWaitForLoading(singletonList(ATTR_STAGE_NAME), stageNameId, singletonList(CLOSED_WON),
                asList(FIRST_REPORT, SECOND_REPORT));

            assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), CLOSED_WON);
            assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), CLOSED_WON);
            assertThat(getAttributeValues(FIRST_REPORT).toString(), containsString(CLOSED_WON + ", Adam Bradley, 2010"));
            assertThat(getAttributeValues(SECOND_REPORT), hasItem(CLOSED_WON));
        } finally {
            herokuAppPage.setFilterContext(stageNameId, singletonList(GDC_SELECT_ALL));
        }
        setFilterContextAndWaitForLoading(EMPTY_LIST, yearCreatedId,
            asList("2007", "2008", "2009", "2010", "2011", "2012", "2013"), asList(FIRST_REPORT, SECOND_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithTheSameModeLinkingTab", this.getClass());

        assertEquals(getCurrentValueFilter(ATTR_YEAR_CREATED), "2007");
    }

    @Test(dependsOnMethods = "filterFromOuterApp_ToGDCEmbeddedDashboard_WithTheSameModeLinkingTab")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithTheDifferentModeLinkingTab() {
        initDashboardsPage().selectDashboard(FIRST_DASHBOARD).openTab(0).editDashboard();
        getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue().editAttributeFilterValues(INTEREST);
        getFilter(VARIABLE_STAGENAME).changeSelectionToMultipleValues()
            .editAttributeFilterValues(SHORT_LIST, CONVICTION);

        dashboardsPage.saveDashboard().openTab(1).editDashboard();
        getFilter(ATTR_STAGE_NAME).changeSelectionToMultipleValues()
            .editAttributeFilterValues(INTEREST, SHORT_LIST, CLOSED_WON);
        getFilter(VARIABLE_STAGENAME).changeSelectionToOneValue().editAttributeFilterValues(CONVICTION);
        dashboardsPage.saveDashboard();
        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        openTab(0);
        setFilterContextAndWaitForLoading(singletonList(ATTR_STAGE_NAME), stageNameId, asList(SHORT_LIST, CLOSED_LOST),
            asList(FIRST_REPORT, SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), SHORT_LIST);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), SHORT_LIST);
        assertThat(getAttributeValues(FIRST_REPORT).toString(),
            containsString(SHORT_LIST + ", Alexsandr Fyodr, 2010"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItem(SHORT_LIST));

        openTab(1);
        waitForReportLoaded(asList(FIRST_REPORT, SECOND_REPORT));

        setFilterContextAndWaitForLoading(EMPTY_LIST, stageNameId, asList(SHORT_LIST, CLOSED_LOST),
            asList(FIRST_REPORT, SECOND_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithTheDifferentModeLinkingTab", this.getClass());

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), SHORT_LIST + ", " + CLOSED_LOST);
        assertEquals(getCurrentValueFilter(VARIABLE_STAGENAME), SHORT_LIST);
        assertThat(getAttributeValues(FIRST_REPORT).toString(),
            containsString(SHORT_LIST + ", Alexsandr Fyodr, 2010"));
        assertThat(getAttributeValues(SECOND_REPORT), hasItem(SHORT_LIST));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithGroupFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        allValuesFilterStageName = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        allValuesFilterYearAttributeCreated = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_CREATED));

        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT, asList(
                Pair.of(allValuesFilterStageName, TabItem.ItemPosition.LEFT),
                Pair.of(allValuesFilterYearAttributeCreated, TabItem.ItemPosition.RIGHT))))
            .addFilter(allValuesFilterStageName)
            .addFilter(allValuesFilterYearAttributeCreated).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);
        initDashboardsPage().selectDashboard(dashboardTitle).editDashboard()
            .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_YEAR_CREATED);

        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();
        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        setFilterContextAndWaitForLoading(singletonList(ATTR_YEAR_CREATED), asList(Pair.of(yearCreatedId, "2010"),
            Pair.of(stageNameId, SHORT_LIST), Pair.of(stageNameId, CLOSED_WON)), singletonList(SECOND_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithGroupFilter", this.getClass());

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), SHORT_LIST + ", " + CLOSED_WON);
        assertEquals(getCurrentValueFilter(ATTR_YEAR_CREATED), "2010");
        assertEquals(getAttributeValues(SECOND_REPORT), asList(SHORT_LIST, CLOSED_WON));
        assertEquals(getRawMetricValues(SECOND_REPORT), asList("$1,255,979.12", "$12,395,325.24"));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToOuterApp_WithMultipleMode() throws IOException {
        dashboardTitle = generateDashboardName();
        allValuesFilterStageName = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        allValuesFilterYearAttributeCreated = createSingleValueFilter(
            getAttributeByTitle(ATTR_YEAR_CREATED), "2010");

        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT, asList(
                Pair.of(allValuesFilterStageName, TabItem.ItemPosition.LEFT),
                Pair.of(allValuesFilterYearAttributeCreated, TabItem.ItemPosition.RIGHT))))
            .addFilter(allValuesFilterStageName)
            .addFilter(allValuesFilterYearAttributeCreated).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);
        initDashboardsPage().selectDashboard(dashboardTitle);
        embeddedUri = dashboardsPage.openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(INTEREST, CLOSED_WON);
        BrowserUtils.switchToMainWindow(browser);

        assertEquals(herokuAppPage.getLatestIncomingMessage(),
            asList(stageNameId + "|" + INTEREST, stageNameId + "|" + CLOSED_WON));

        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        getFilter(ATTR_YEAR_CREATED).changeAttributeFilterValues("2007");
        BrowserUtils.switchToMainWindow(browser);

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToOuterApp_WithMultipleMode", this.getClass());

        assertEquals(herokuAppPage.getLatestIncomingMessage(), asList(yearCreatedId + "|" + "2007"));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToOuterApp_WithGroupFilter() throws IOException {
        dashboardTitle = generateDashboardName();
        allValuesFilterYearAttributeCreated = createSingleValueFilter(
            getAttributeByTitle(ATTR_YEAR_CREATED), "2010");
        allValuesFilterStageName = createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME), DISCOVERY);

        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT, asList(
                Pair.of(allValuesFilterStageName, TabItem.ItemPosition.LEFT),
                Pair.of(allValuesFilterYearAttributeCreated, TabItem.ItemPosition.RIGHT))))
            .addFilter(allValuesFilterStageName)
            .addFilter(allValuesFilterYearAttributeCreated).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);
        initDashboardsPage().selectDashboard(dashboardTitle).editDashboard()
            .groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_YEAR_CREATED);
        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(SHORT_LIST);
        getFilter(ATTR_YEAR_CREATED).changeAttributeFilterValues("2011");
        dashboardsPage.applyValuesForGroupFilter();
        BrowserUtils.switchToMainWindow(browser);

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToOuterApp_WithGroupFilter", this.getClass());

        assertEquals(herokuAppPage.getLatestIncomingMessage(),
            asList(stageNameId + "|" + SHORT_LIST, yearCreatedId + "|" + "2011"));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithDateDimension() {
        dashboardTitle = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboardTitle).addReportToDashboard(SECOND_REPORT)
            .addTimeFilterToDashboard(GoodSalesUtils.DATE_DIMENSION_SNAPSHOT,
                TimeFilterPanel.DateGranularity.YEAR, THIS, RIGHT);
        yearSnapshotId = getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getDefaultDisplayForm().getIdentifier();
        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();
        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        waitForReportLoaded(singletonList(SECOND_REPORT));

        setDateFilterContextAndWaitForLoading(singletonList(GoodSalesUtils.DATE_DIMENSION_SNAPSHOT),
            singletonList(Pair.of(yearSnapshotId, Pair.of("2010-05-25", "2012-12-25"))),
            singletonList(SECOND_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithDateDimension", this.getClass());

        assertEquals(getCurrentValueFilter(GoodSalesUtils.DATE_DIMENSION_SNAPSHOT), "May 25 2010 - Dec 25 2012");
        assertEquals(getAttributeValues(SECOND_REPORT), asList(DISCOVERY, SHORT_LIST, CONVICTION, CLOSED_WON));
        assertEquals(getRawMetricValues(SECOND_REPORT), asList("$4,249,027.88", "$5,612,062.60", "$3,067,466.12", "$38,310,753.45"));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithFormat_YYYY_MM_DD() throws IOException {
        dashboardTitle = generateDashboardName();
        dateFilterCreatedContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_CREATED),
            2010 - CURRENT_YEAR, 2012 - CURRENT_YEAR);

        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT,
                asList(Pair.of(dateFilterCreatedContent, TabItem.ItemPosition.RIGHT))))
            .addFilter(dateFilterCreatedContent).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);
        embeddedUri = initDashboardsPage().selectDashboard(dashboardTitle).openEmbedDashboardDialog().getPreviewURI();
        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);
        waitForReportLoaded(singletonList(SECOND_REPORT));
        setDateFilterContextAndWaitForLoading(singletonList(DATE_DIMENSION_CREATED),
            singletonList(Pair.of(yearCreatedId, Pair.of("2017-05-25", "2010-05-25"))), EMPTY_LIST);

        assertEquals(getCurrentValueFilter(DATE_DIMENSION_CREATED), "May 25 2017");

        herokuAppPage.setDateFilterContext(singletonList(Pair.of(yearCreatedId, Pair.of("2017/05/25", "2017/12/25"))));
        herokuAppPage.setDateFilterContext(asList(Pair.of(yearCreatedId, Pair.of("2017/05/25", "2017/12/25"))));

        assertTrue(herokuAppPage.isNoData(), "Don’t support, system should returns error “No data”");

        herokuAppPage.setDateFilterContext(singletonList(Pair.of(yearCreatedId, Pair.of("2017-05-25", "2010-05-25"))));
        quarterCreatedId = getAttributeByTitle(ATTR_QUARTER_CREATED).getDefaultDisplayForm().getIdentifier();
        herokuAppPage.setDateFilterContext(singletonList(Pair.of(quarterCreatedId, Pair.of("2010-05-25", "2010-05-25"))));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithFormat_YYYY_MM_DD", this.getClass());
        assertEquals(getCurrentValueFilter(DATE_DIMENSION_CREATED), "May 25 2017");
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_WithMultipleDate() throws IOException {
        dashboardTitle = generateDashboardName();
        dateFilterCreatedContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_CREATED),
            2000 - CURRENT_YEAR, 2000 - CURRENT_YEAR);
        dateFilterCloseContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_CLOSE),
            2000 - CURRENT_YEAR, 2000 - CURRENT_YEAR);
        dateFilterSnapshotContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
            2000 - CURRENT_YEAR, 2000 - CURRENT_YEAR);
        dateFilterActivityContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_ACTIVITY),
            2000 - CURRENT_YEAR, 2000 - CURRENT_YEAR);
        dateFilterTimelineContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_TIMELINE),
            2000 - CURRENT_YEAR, 2000 - CURRENT_YEAR);

        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT,
                asList(Pair.of(dateFilterCreatedContent, TabItem.ItemPosition.RIGHT),
                    Pair.of(dateFilterCloseContent, TabItem.ItemPosition.LEFT),
                    Pair.of(dateFilterSnapshotContent, TabItem.ItemPosition.TOP_RIGHT),
                    Pair.of(dateFilterActivityContent, TabItem.ItemPosition.TOP),
                    Pair.of(dateFilterTimelineContent, TabItem.ItemPosition.MIDDLE)
                )))
            .addFilter(dateFilterCreatedContent).addFilter(dateFilterCloseContent)
            .addFilter(dateFilterSnapshotContent).addFilter(dateFilterActivityContent)
            .addFilter(dateFilterTimelineContent).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);

        yearCreatedId = getAttributeByTitle(ATTR_YEAR_CREATED).getDefaultDisplayForm().getIdentifier();
        yearCloseId = getAttributeByTitle(ATTR_YEAR_CLOSE).getDefaultDisplayForm().getIdentifier();
        yearSnapshotId = getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getDefaultDisplayForm().getIdentifier();
        yearActivityId = getAttributeByTitle(ATTR_YEAR_ACTIVITY).getDefaultDisplayForm().getIdentifier();
        yearTimelineId = getAttributeByTitle(ATTR_YEAR_TIMELINE).getDefaultDisplayForm().getIdentifier();

        embeddedUri = initDashboardsPage().selectDashboard(dashboardTitle).openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        waitForFilterLoaded(asList(DATE_DIMENSION_CREATED, DATE_DIMENSION_CLOSED, DATE_DIMENSION_SNAPSHOT,
            DATE_DIMENSION_ACTIVITY, DATE_DIMENSION_TIMELINE));

        setDateFilterContextAndWaitForLoading(asList(DATE_DIMENSION_CREATED, DATE_DIMENSION_CLOSED, DATE_DIMENSION_SNAPSHOT,
            DATE_DIMENSION_ACTIVITY, DATE_DIMENSION_TIMELINE),
            asList(Pair.of(yearCreatedId, Pair.of("2010-05-25", "2012-12-25")),
                Pair.of(yearCloseId, Pair.of("2010-05-25", "2012-12-25")),
                Pair.of(yearSnapshotId, Pair.of("2010-05-25", "2012-12-25")),
                Pair.of(yearActivityId, Pair.of("2010-05-25", "2012-12-25")),
                Pair.of(yearTimelineId, Pair.of("2010-05-25", "2012-12-25"))), singletonList(SECOND_REPORT));

        takeScreenshot(browser, "filterFromOuterApp_ToGDCEmbeddedDashboard_WithMultipleDate", this.getClass());

        assertEquals(getCurrentValueFilter(DATE_DIMENSION_CREATED), "May 25 2010 - Dec 25 2012");
        assertEquals(getCurrentValueFilter(DATE_DIMENSION_CLOSED), "May 25 2010 - Dec 25 2012");
        assertEquals(getCurrentValueFilter(DATE_DIMENSION_SNAPSHOT), "May 25 2010 - Dec 25 2012");
        assertEquals(getCurrentValueFilter(DATE_DIMENSION_ACTIVITY), "May 25 2010 - Dec 25 2012");
        assertEquals(getCurrentValueFilter(DATE_DIMENSION_TIMELINE), "May 25 2010 - Dec 25 2012");
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithSingleTab() {
        dashboardTitle = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboardTitle);

        addAndResizeTwoTableReport();

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_SALES_REP);
        LEFT.moveElementToRightPlace(getFilter(ATTR_SALES_REP).getRoot());
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_YEAR_SNAPSHOT);
        getFilter(ATTR_YEAR_SNAPSHOT).changeSelectionToMultipleValues().editAttributeFilterValues("2010", "2011");
        MIDDLE.moveElementToRightPlace(getFilter(ATTR_YEAR_SNAPSHOT).getRoot());

        EmbeddedWidget embeddedWidget = addAndResizeWebContentToDashboard();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(INTEREST, DISCOVERY, SHORT_LIST);

        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(stageNameId + "|" + INTEREST,
            stageNameId + "|" + DISCOVERY, stageNameId + "|" + SHORT_LIST));

        getFilter(ATTR_SALES_REP).changeAttributeFilterValues("Cory Owens");

        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(salesRepId + "|" + "Cory Owens"));

        getFilter(ATTR_YEAR_SNAPSHOT).changeAttributeFilterValues(
            "2007", "2008", "2009", "2010", "2011", "2012", "2013");

        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(
            yearSnapshotId + "|" + "2007", yearSnapshotId + "|" + "2008", yearSnapshotId + "|" + "2009",
            yearSnapshotId + "|" + "2010", yearSnapshotId + "|" + "2011", yearSnapshotId + "|" + "2012",
            yearSnapshotId + "|" + "2013"));

        TableReport firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        TableReport secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithSingleTab", this.getClass());

        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(INTEREST + ", Cory Owens, 2009, 2010, 2011, 2012"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(), hasItems(DISCOVERY, SHORT_LIST));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithSameMode() {
        dashboardTitle = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboardTitle);

        addAndResizeTwoTableReport();

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        DOWN.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        EmbeddedWidget embeddedWidget = addAndResizeWebContentToDashboard();
        dashboardsPage.duplicateDashboardTab(0).openTab(0);
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY, SHORT_LIST, CLOSED_WON);
        TableReport firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        TableReport secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(DISCOVERY + ", Adam Bradley, 2011, 2012"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(),
            hasItems(DISCOVERY, SHORT_LIST, CLOSED_WON));
        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(
            stageNameId + "|" + DISCOVERY, stageNameId + "|" + SHORT_LIST, stageNameId + "|" + CLOSED_WON));

        dashboardsPage.openTab(1);
        firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithSameMode", this.getClass());
        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(DISCOVERY + ", Adam Bradley, 2011, 2012"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(),
            hasItems(DISCOVERY, SHORT_LIST, CLOSED_WON));
        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(
            stageNameId + "|" + DISCOVERY, stageNameId + "|" + SHORT_LIST, stageNameId + "|" + CLOSED_WON));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithMultipleMode() {
        dashboardTitle = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboardTitle);
        addAndResizeTwoTableReport();

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        DOWN.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue().editAttributeFilterValues(INTEREST);

        addAndResizeWebContentToDashboard();

        dashboardsPage.addNewTab(SECOND_TAB);
        addAndResizeTwoTableReport();
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        DOWN.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).changeSelectionToMultipleValues()
            .editAttributeFilterValues(INTEREST, SHORT_LIST, CLOSED_WON);

        EmbeddedWidget embeddedWidget = addAndResizeWebContentToDashboard();
        dashboardsPage.saveDashboard().openTab(0);
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(RISK_ASSESSMENT);
        TableReport firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        TableReport secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(RISK_ASSESSMENT + ", Adam Bradley, 2011, 2012"));
        assertTrue(secondTableReport.hasNoData(), "Report should have no data");
        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(stageNameId + "|" + RISK_ASSESSMENT));

        dashboardsPage.openTab(1);
        firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithMultipleMode", this.getClass());
        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(INTEREST + ", Adam Bradley, 2010, 2011, 2012"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(), hasItems(SHORT_LIST, CLOSED_WON));
        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(
            stageNameId + "|" + INTEREST, stageNameId + "|" + SHORT_LIST, stageNameId + "|" + CLOSED_WON));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithGroup() throws IOException {
        dashboardTitle = generateDashboardName();
        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab(initTab(FIRST_TAB, FIRST_REPORT, asList(
                Pair.of(allValuesFilterStageName, TabItem.ItemPosition.LEFT),
                Pair.of(allValuesFilterYearAttributeCreated, TabItem.ItemPosition.RIGHT))))
            .addFilter(allValuesFilterStageName)
            .addFilter(allValuesFilterYearAttributeCreated).getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);

        initDashboardsPage().selectDashboard(dashboardTitle).editDashboard();
        UP.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        MIDDLE.moveElementToRightPlace(getFilter(ATTR_YEAR_CREATED).getRoot());
        RIGHT.moveElementToRightPlace(getFilter(ATTR_YEAR_CREATED).getRoot());

        TableReport tableReport = dashboardsPage.getReport(FIRST_REPORT, TableReport.class);
        tableReport.getRoot().click();
        tableReport.resizeFromTopLeftButton(-300, 0);

        EmbeddedWidget embeddedWidget = addAndResizeWebContentToDashboard();
        dashboardsPage.groupFiltersOnDashboard(ATTR_STAGE_NAME, ATTR_YEAR_CREATED).saveDashboard();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(SHORT_LIST);
        getFilter(ATTR_YEAR_CREATED).changeAttributeFilterValues("2012");
        dashboardsPage.applyValuesForGroupFilter();

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithGroup", this.getClass());

        assertThat(tableReport.waitForLoaded().getAttributeValues().toString(),
            containsString(SHORT_LIST + ", Adam Bradley, 2012"));
        assertThat(embeddedWidget.getLatestIncomingMessage(), hasItems(
            stageNameId + "|" + SHORT_LIST, yearCreatedId + "|" + "2012"));
    }

    private void setFilterContextAndWaitForLoading(List<String> filters, String identifier,
                                                   List<String> values, List<String> reports) {
        waitForFilterLoaded(filters);
        FilterContextHerokuAppPage.getInstance(browser).setFilterContext(identifier, values);
        waitForReportLoaded(reports);
    }

    private void setFilterContextAndWaitForLoading(List<String> filters,
                                                   List<Pair<String, String>> values, List<String> reports) {
        waitForFilterLoaded(filters);
        FilterContextHerokuAppPage.getInstance(browser).setFilterContext(values);
        waitForReportLoaded(reports);
    }

    private void setDateFilterContextAndWaitForLoading(List<String> filters,
                                                       List<Pair<String, Pair<String, String>>> values,
                                                       List<String> reports) {
        waitForFilterLoaded(filters);
        FilterContextHerokuAppPage.getInstance(browser).setDateFilterContext(values);
        waitForReportLoaded(reports);
    }

    private EmbeddedWidget addAndResizeWebContentToDashboard() {
        dashboardsPage.addWebContentToDashboard(HEROKU_APP_LINK);
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        LEFT.moveElementToRightPlace(waitForElementVisible(embeddedWidget.getRoot()));
        PENULTIMATE_BOTTOM.moveElementToRightPlace(waitForElementVisible(embeddedWidget.getRoot()));
        embeddedWidget.resizeFromBottomRightButton(500, 300);
        return embeddedWidget;
    }

    private void addAndResizeTwoTableReport() {
        dashboardsPage.addReportToDashboard(FIRST_REPORT)
            .getReport(FIRST_REPORT, TableReport.class).resizeFromTopLeftButton(-300, 0);
        dashboardsPage.addReportToDashboard(SECOND_REPORT);
        RIGHT.moveElementToRightPlace(getReport(SECOND_REPORT).getRoot());
    }

    private String getCurrentValueFilter(String attribute) {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        String results = getFilter(attribute).getCurrentValue();
        BrowserUtils.switchToMainWindow(browser);
        return results;
    }

    private List<String> getAttributeValues(String reportName) {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        List<String> results = getReport(reportName).getAttributeValues();
        BrowserUtils.switchToMainWindow(browser);
        return results;
    }

    private List<String> getRawMetricValues(String reportName) {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        List<String> results = getReport(reportName).getRawMetricValues();
        BrowserUtils.switchToMainWindow(browser);
        return results;
    }

    private void waitForReportLoaded(List<String> reports) {
        if (!isEmpty(reports)) {
            browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
            reports.forEach(e ->
                EmbeddedDashboard.getInstance(browser).getReport(e, TableReport.class).waitForLoaded());
            BrowserUtils.switchToMainWindow(browser);
        }
    }

    private void waitForFilterLoaded(List<String> filters) {
        if (!isEmpty(filters)) {
            browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
            filters.forEach(e -> Graphene.waitGui().until(browser ->
                !EmbeddedDashboard.getInstance(browser).getFilterWidgetByName(e).getCurrentValue().isEmpty()));
            BrowserUtils.switchToMainWindow(browser);
        }
    }

    private void openTab(int tabIndex) {
        browser.switchTo().frame(waitForElementVisible(BY_IFRAME, browser));
        dashboardsPage.getTabs().openTab(tabIndex);
        BrowserUtils.switchToMainWindow(browser);
    }

    private Tab initTab(String name, String report, List<Pair<FilterItemContent, TabItem.ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream()
            .map(this::buildFilterItem)
            .collect(Collectors.toList());
        ReportItem reportItem = createReportItem(getReportByTitle(report).getUri(),
            filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));
        return new Tab().setTitle(name).addItems(Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream)
            .collect(Collectors.toList()));
    }

    private FilterItem buildFilterItem(Pair<FilterItemContent, TabItem.ItemPosition> filterItem) {
        FilterItem filterItemContent = new FilterItem().setContentId(filterItem.getLeft().getId());
        filterItemContent.setPosition(filterItem.getRight());
        return filterItemContent;
    }

    private void prepareDashboard() throws IOException {
        dateFilterCreatedContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_CREATED),
            2008 - CURRENT_YEAR, 2012 - CURRENT_YEAR);

        JSONObject dashboardContent = new Dashboard()
            .setName(FIRST_DASHBOARD)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT,
                asList(Pair.of(dateFilterCreatedContent, TabItem.ItemPosition.RIGHT))))
            .addFilter(dateFilterCreatedContent).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);

        initDashboardsPage().selectDashboard(FIRST_DASHBOARD).editDashboard();
        RIGHT.moveElementToRightPlace(getReport(SECOND_REPORT).getRoot());

        dashboardsPage.addReportToDashboard(FIRST_REPORT);
        dashboardsPage.getReport(FIRST_REPORT, TableReport.class).resizeFromTopLeftButton(-300, 0);

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        BOTTOM.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(SHORT_LIST, CONVICTION);

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_SALES_REP);
        RIGHT.moveElementToRightPlace(getFilter(ATTR_SALES_REP).getRoot());
        BOTTOM.moveElementToRightPlace(getFilter(ATTR_SALES_REP).getRoot());

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_YEAR_CREATED);
        LEFT.moveElementToRightPlace(getFilter(ATTR_YEAR_CREATED).getRoot());
        BOTTOM.moveElementToRightPlace(getFilter(ATTR_YEAR_CREATED).getRoot());
        getFilter(ATTR_YEAR_CREATED).editAttributeFilterValues("2010", "2011");

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, VARIABLE_STAGENAME);
        LEFT.moveElementToRightPlace(getFilter(VARIABLE_STAGENAME).getRoot());
        getFilter(VARIABLE_STAGENAME).editAttributeFilterValues(DISCOVERY, SHORT_LIST);

        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();
    }
}
