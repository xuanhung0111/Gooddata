package com.gooddata.qa.graphene.dashboards;

import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.Filter;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.fixture.utils.GoodSales.Variables;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.common.FilterContextHerokuAppPage;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DrillingConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.LEFT;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.RIGHT;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.MIDDLE;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.DOWN;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.UP;
import static com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection.PENULTIMATE_BOTTOM;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_SALES_REP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesFilterEmbeddedDashboardLogMessageTest extends AbstractEmbeddedModeTest {

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String CLOSED_WON = "Closed Won";
    private static final String CLOSED_LOST = "Closed Lost";
    private static final String ALL = "All";
    private static final String GDC_SELECT_ALL = "GDC_SELECT_ALL";
    private static final String FIRST_TAB = "First Tab";
    private static final String SECOND_TAB = "Second Tab";
    private static final String FIRST_REPORT = "First Report";
    private static final String SECOND_REPORT = "Second Report";
    private static final String PARENT_CHILD_REPORT = "Parent Child Report";
    private static final String PARENT_CHILD_DASHBOARD = "Parent Child Dashboard";
    private static final String BOTH_OUTER_AND_INNER_DASHBOARD = "Outer Inner Dashboard";
    private static final String DRILLING_REPORT = "Drilling Report";
    private static final String VARIABLE_STAGENAME = "V Stage Name";
    private static final String DASHBOARD_DRILLING_GROUP = "Dashboards";
    private static final String SOURCE_TAB = "Source Tab";
    private static final String TARGET_TAB = "Target Tab";

    private String variableUri;
    private String stageNameId;
    private String productId;
    private String accountId;
    private String salesRepId;
    private String yearCreatedId;
    private String yearSnapshotId;
    private String dashboardTitle;

    private DashboardRestRequest dashboardRestRequest;
    private FilterItemContent allValuesFilterStageName;
    private FilterItemContent allValuesFilterYearAttributeCreated;
    private FilterItemContent allValuesFilterProduct;
    private FilterItemContent allValuesFilterAccount;

    @Override
    protected void customizeProject() throws Throwable {
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.CONTROL_EXECUTION_CONTEXT_ENABLED, true);
        getMetricCreator().createAmountMetric();
        dashboardRestRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());

        Variables variables = getVariableCreator();

        variableUri = variables.createFilterVariable(VARIABLE_STAGENAME, getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
            asList(DISCOVERY, SHORT_LIST, CONVICTION, CLOSED_WON));

        salesRepId = getAttributeByTitle(ATTR_SALES_REP).getDefaultDisplayForm().getIdentifier();
        yearCreatedId = getAttributeByTitle(ATTR_YEAR_CREATED).getDefaultDisplayForm().getIdentifier();
        yearSnapshotId = getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getDefaultDisplayForm().getIdentifier();
        stageNameId = getAttributeByTitle(ATTR_STAGE_NAME).getDefaultDisplayForm().getIdentifier();
        productId = getAttributeByTitle(ATTR_PRODUCT).getDefaultDisplayForm().getIdentifier();
        accountId = getAttributeByTitle(ATTR_ACCOUNT).getDefaultDisplayForm().getIdentifier();

        allValuesFilterProduct = createMultipleValuesFilter(getAttributeByTitle(ATTR_PRODUCT));
        allValuesFilterStageName = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        allValuesFilterYearAttributeCreated = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_CREATED));
        allValuesFilterAccount = createMultipleValuesFilter(getAttributeByTitle(ATTR_ACCOUNT));
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

        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(),
            hasItems(stageNameId + "|" + INTEREST, stageNameId + "|" + DISCOVERY, stageNameId + "|" + SHORT_LIST));

        getFilter(ATTR_SALES_REP).changeAttributeFilterValues("Cory Owens");

        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(),
            hasItems(salesRepId + "|" + "Cory Owens"));

        getFilter(ATTR_YEAR_SNAPSHOT).changeAttributeFilterValues(
            "2007", "2008", "2009", "2010", "2011", "2012", "2013");

        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(), hasItems(
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
        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(),
            hasItems(stageNameId + "|" + DISCOVERY, stageNameId + "|" + SHORT_LIST, stageNameId + "|" + CLOSED_WON));

        dashboardsPage.openTab(1);
        firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithSameMode", this.getClass());
        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(DISCOVERY + ", Adam Bradley, 2011, 2012"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(),
            hasItems(DISCOVERY, SHORT_LIST, CLOSED_WON));
        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(),
            hasItems(stageNameId + "|" + DISCOVERY, stageNameId + "|" + SHORT_LIST, stageNameId + "|" + CLOSED_WON));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithMultipleMode() {
        dashboardTitle = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboardTitle);
        addAndResizeTwoTableReport();

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        DOWN.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue().editAttributeFilterValues(INTEREST);

        EmbeddedWidget embeddedWidget = addAndResizeWebContentToDashboard();

        dashboardsPage.addNewTab(SECOND_TAB);
        addAndResizeTwoTableReport();
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        DOWN.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).changeSelectionToMultipleValues()
            .editAttributeFilterValues(INTEREST, SHORT_LIST, CLOSED_WON);

        addAndResizeWebContentToDashboard();
        dashboardsPage.saveDashboard().openTab(0);
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(RISK_ASSESSMENT);
        TableReport firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        TableReport secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(RISK_ASSESSMENT + ", Adam Bradley, 2011, 2012"));
        assertTrue(secondTableReport.hasNoData(), "Report should have no data");
        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(),
            hasItems(stageNameId + "|" + RISK_ASSESSMENT));

        dashboardsPage.openTab(1);
        firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        takeScreenshot(browser, "filterFromGDCEmbeddedDashboard_ToInnerFilterWidget_WithMultipleMode", this.getClass());
        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(INTEREST + ", Adam Bradley, 2010, 2011, 2012"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(), hasItems(SHORT_LIST, CLOSED_WON));
        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(), hasItems(
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
        assertThat(embeddedWidget.getFilterContextHerokuAppPage().getLatestIncomingMessage(), hasItems(
            stageNameId + "|" + SHORT_LIST, yearCreatedId + "|" + "2012"));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromInnerFilterWidget_ToOuterGDCEmbeddedDashboard() {
        dashboardTitle = generateDashboardName();
        initDashboardsPage().addNewDashboard(dashboardTitle);

        addAndResizeTwoTableReport();

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue().editAttributeFilterValues(INTEREST);
        RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, VARIABLE_STAGENAME);
        getFilter(VARIABLE_STAGENAME).changeSelectionToOneValue().editAttributeFilterValues(SHORT_LIST);
        LEFT.moveElementToRightPlace(getFilter(VARIABLE_STAGENAME).getRoot());
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_YEAR_CREATED);
        getFilter(ATTR_YEAR_CREATED).changeSelectionToOneValue().editAttributeFilterValues("2010");
        MIDDLE.moveElementToRightPlace(getFilter(ATTR_YEAR_CREATED).getRoot());

        addAndResizeWebContentToDashboard();

        dashboardsPage.duplicateDashboardTab(0).openTab(1).editDashboard();

        getFilter(ATTR_STAGE_NAME).changeSelectionToMultipleValues().editAttributeFilterValues(INTEREST, SHORT_LIST, CLOSED_WON);
        getFilter(VARIABLE_STAGENAME).changeSelectionToMultipleValues().editAttributeFilterValues(SHORT_LIST, CLOSED_WON);
        getFilter(ATTR_YEAR_CREATED).changeSelectionToMultipleValues().editAttributeFilterValues("2010", "2011");
        dashboardsPage.saveDashboard().openTab(0);

        FilterContextHerokuAppPage filterContextHerokuAppPage = dashboardsPage.getLastEmbeddedWidget()
            .getFilterContextHerokuAppPage();
        filterContextHerokuAppPage.setFilterContext(stageNameId, asList(RISK_ASSESSMENT, CLOSED_WON));
        BrowserUtils.switchToMainWindow(browser);

        TableReport firstTableReport = getReport(FIRST_REPORT).waitForLoaded();
        TableReport secondTableReport = getReport(SECOND_REPORT).waitForLoaded();

        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), RISK_ASSESSMENT);
        assertEquals(getFilter(VARIABLE_STAGENAME).getCurrentValue(), DISCOVERY);
        assertThat(firstTableReport.getAttributeValues().toString(),
            containsString(RISK_ASSESSMENT + ", Cory Owens, 2010"));
        assertTrue(secondTableReport.hasNoData(), "Report should have no data");

        dashboardsPage.openTab(1);

        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), RISK_ASSESSMENT + ", " + CLOSED_WON);
        assertEquals(getFilter(VARIABLE_STAGENAME).getCurrentValue(), CLOSED_WON);
        assertThat(firstTableReport.waitForLoaded().getAttributeValues().toString(),
            containsString(RISK_ASSESSMENT + ", Adam Bradley, 2011"));
        assertThat(secondTableReport.waitForLoaded().getAttributeValues(), hasItems(CLOSED_WON));
    }

    @Test(dependsOnGroups = "createProject")
    public void filterFromOuterApp_ToGDCEmbeddedDashboard_AndInnerFilterWidget() throws IOException {
        JSONObject dashboardContent = new Dashboard()
            .setName(BOTH_OUTER_AND_INNER_DASHBOARD)
            .addTab(initTab(FIRST_TAB, SECOND_REPORT, asList(
                Pair.of(allValuesFilterStageName, TabItem.ItemPosition.RIGHT))))
            .addFilter(allValuesFilterStageName).getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);

        initDashboardsPage().selectDashboard(BOTH_OUTER_AND_INNER_DASHBOARD).editDashboard();
        addAndResizeWebContentToDashboard();

        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        setFilterContextAndWaitForLoading(singletonList(ATTR_STAGE_NAME), stageNameId,
            singletonList(CLOSED_WON), singletonList(SECOND_REPORT));

        assertEquals(getCurrentValueFilter(ATTR_STAGE_NAME), CLOSED_WON);
        assertThat(getAttributeValues(SECOND_REPORT), hasItem(CLOSED_WON));
        assertEquals(getLatestIncomingMessageInnerWidget(), singletonList(stageNameId + "|" + GDC_SELECT_ALL));
    }

    @Test(dependsOnMethods = {"filterFromOuterApp_ToGDCEmbeddedDashboard_AndInnerFilterWidget"})
    public void filterFromGDCEmbeddedDashboard_ToOuterApp_AndInnerFilterWidget() {
        initDashboardsPage().selectDashboard(BOTH_OUTER_AND_INNER_DASHBOARD).editDashboard();
        addAndResizeWebContentToDashboard();

        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        BrowserUtils.switchToMainWindow(browser);

        assertThat(getAttributeValues(SECOND_REPORT), hasItem(DISCOVERY));
        assertEquals(getLatestIncomingMessageInnerWidget(), singletonList(stageNameId + "|" + DISCOVERY));

        assertEquals(FilterContextHerokuAppPage.getInstance(browser)
            .getLatestIncomingMessage(), singletonList(stageNameId + "|" + DISCOVERY));
    }

    @Test(dependsOnMethods = {"filterFromGDCEmbeddedDashboard_ToOuterApp_AndInnerFilterWidget"})
    public void filterFromInnerFilterWidgetToGDCEmbeddedDashboard_AndOuterApp() {
        initDashboardsPage().selectDashboard(BOTH_OUTER_AND_INNER_DASHBOARD).editDashboard();
        addAndResizeWebContentToDashboard();
        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        setFilterContextAndWaitForLoading(EMPTY_LIST, stageNameId, singletonList(SHORT_LIST), singletonList(SECOND_REPORT));
        BrowserUtils.switchToMainWindow(browser);

        assertThat(getAttributeValues(SECOND_REPORT), hasItem(SHORT_LIST));
        assertEquals(FilterContextHerokuAppPage.getInstance(browser)
            .getLatestIncomingMessage(), singletonList(stageNameId + "|" + GDC_SELECT_ALL));
    }

    @Test(dependsOnGroups = "createProject")
    public void changeValueOfFilterInDrillingToGDCEmbeddedDashboard() throws IOException {
        dashboardTitle = generateDashboardName();
        createReport(GridReportDefinitionContent.create(DRILLING_REPORT,
            singletonList(METRIC_GROUP),
            singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_PRODUCT))),
            singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        JSONObject dashboardContent = new Dashboard()
            .setName(dashboardTitle)
            .addTab((initTab(TARGET_TAB, DRILLING_REPORT, asList(
                Pair.of(allValuesFilterProduct, TabItem.ItemPosition.RIGHT)))))
            .addTab((initTab(SOURCE_TAB, DRILLING_REPORT, asList(
                Pair.of(allValuesFilterProduct, TabItem.ItemPosition.RIGHT)))))
            .addFilter(allValuesFilterProduct).getMdObject();

        dashboardRestRequest.createDashboard(dashboardContent);
        initDashboardsPage().selectDashboard(dashboardTitle).editDashboard();

        TableReport tableReport = dashboardsPage.getReport(DRILLING_REPORT, TableReport.class);
        addDrillingHavingInnerDrill(tableReport, SOURCE_TAB);
        dashboardsPage.saveDashboard().editDashboard();
        dashboardsPage.openTab(1);
        addAndResizeWebContentToDashboard();

        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        tableReport.drillOn("Grammar Plus", TableReport.CellType.ATTRIBUTE_VALUE).waitForLoaded();
        BrowserUtils.switchToMainWindow(browser);

        assertEquals(getLatestIncomingMessageInnerWidget(), singletonList(productId + "|" + "Grammar Plus"));
        assertEquals(herokuAppPage.getLatestIncomingMessage(), singletonList(productId + "|" + "Grammar Plus"));
    }

    @Test(dependsOnGroups = "createProject")
    public void sendValueParentAndChild_OfFilter_FromInner_ToOuterDashboard() throws IOException {
        createReport(GridReportDefinitionContent.create(PARENT_CHILD_REPORT,
            singletonList(METRIC_GROUP),
            asList(new AttributeInGrid(getAttributeByTitle(ATTR_ACCOUNT)),
                new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
            singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        JSONObject dashboardContent = new Dashboard()
            .setName(PARENT_CHILD_DASHBOARD)
            .addTab((initTab(TARGET_TAB, PARENT_CHILD_REPORT, asList(
                Pair.of(allValuesFilterAccount, TabItem.ItemPosition.TOP_RIGHT),
                Pair.of(allValuesFilterStageName, TabItem.ItemPosition.RIGHT)))))
            .addFilter(allValuesFilterAccount).addFilter(allValuesFilterStageName).getMdObject();
        dashboardRestRequest.createDashboard(dashboardContent);
        initDashboardsPage().selectDashboard(PARENT_CHILD_DASHBOARD).editDashboard();

        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);

        addAndResizeWebContentToDashboard();
        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        getFilter(ATTR_ACCOUNT).changeAttributeFilterValues("101 Financial");
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(CLOSED_LOST);
        BrowserUtils.switchToMainWindow(browser);

        assertThat(herokuAppPage.getAllIncomingMessages(), hasItems(stageNameId + "|" + CLOSED_LOST,
            accountId + "|" + "101 Financial", accountId + "|" + GDC_SELECT_ALL, stageNameId + "|" + GDC_SELECT_ALL));
        assertEquals(getAttributeValues(PARENT_CHILD_REPORT), asList("101 Financial", CLOSED_LOST));
        assertEquals(getAllIncomingMessageInnerWidget(),
            asList(stageNameId + "|" + CLOSED_LOST, accountId + "|" + "101 Financial", accountId + "|" + GDC_SELECT_ALL,
                stageNameId + "|" + GDC_SELECT_ALL));
    }

    @Test(dependsOnMethods = {"sendValueParentAndChild_OfFilter_FromInner_ToOuterDashboard"})
    public void notSendValueParentAndChild_OfFilter_FromInner_ToOuterDashboard() {
        initDashboardsPage().selectDashboard(PARENT_CHILD_DASHBOARD);

        addAndResizeWebContentToDashboard();
        embeddedUri = dashboardsPage.saveDashboard().openEmbedDashboardDialog().getPreviewURI();

        FilterContextHerokuAppPage herokuAppPage = initFilterContextHerokuAppPage();
        herokuAppPage.inputEmbeddedDashboardUrl(embeddedUri);

        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        getFilter(ATTR_ACCOUNT).changeAttributeFilterValues("101 Financial");
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(CLOSED_LOST);
        getFilter(ATTR_STAGE_NAME).openPanel().getAttributeFilterPanel().showAllAttributes().changeValues(SHORT_LIST);
        BrowserUtils.switchToMainWindow(browser);

        assertEquals(getCurrentValueFilter(ATTR_ACCOUNT), ALL);
        assertThat(getAttributeValues(PARENT_CHILD_REPORT).toString(), containsString("1-800 We Answer, " + SHORT_LIST));
        assertEquals(FilterContextHerokuAppPage.getInstance(browser).getLatestIncomingMessage(),
            asList(stageNameId + "|" + SHORT_LIST));

        assertEquals(getAllIncomingMessageInnerWidget(),
            asList(stageNameId + "|" + SHORT_LIST, stageNameId + "|" + CLOSED_LOST, accountId + "|" + "101 Financial",
                accountId + "|" + GDC_SELECT_ALL, stageNameId + "|" + GDC_SELECT_ALL));
    }

    private void addDrillingHavingInnerDrill(TableReport tableReport, String tabDrillTo) {
        WidgetConfigPanel widgetConfigPanel = WidgetConfigPanel
            .openConfigurationPanelFor(tableReport.getRoot(), browser);
        widgetConfigPanel.getTab(WidgetConfigPanel.Tab.DRILLING, DrillingConfigPanel.class)
            .addDrilling(Pair.of(singletonList(ATTR_PRODUCT), tabDrillTo), DASHBOARD_DRILLING_GROUP);
        widgetConfigPanel.saveConfiguration();
    }

    private List<String> getLatestIncomingMessageInnerWidget() {
        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        List<String> message = dashboardsPage.getLastEmbeddedWidget().getFilterContextHerokuAppPage()
            .getLatestIncomingMessage();
        BrowserUtils.switchToMainWindow(browser);
        return message;
    }

    private List<String> getAllIncomingMessageInnerWidget() {
        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        List<String> message = dashboardsPage.getLastEmbeddedWidget().getFilterContextHerokuAppPage()
            .getAllIncomingMessages();
        BrowserUtils.switchToMainWindow(browser);
        return message;
    }

    private void setFilterContextAndWaitForLoading(List<String> filters, String identifier,
                                                   List<String> values, List<String> reports) {
        waitForFilterLoaded(filters);
        FilterContextHerokuAppPage.getInstance(browser).setFilterContext(identifier, values);
        waitForReportLoaded(reports);
    }

    private EmbeddedWidget addAndResizeWebContentToDashboard() {
        dashboardsPage.addWebContentToDashboard(HEROKU_APP_LINK);
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        LEFT.moveElementToRightPlace(waitForElementVisible(embeddedWidget.getRoot()));
        PENULTIMATE_BOTTOM.moveElementToRightPlace(waitForElementVisible(embeddedWidget.getRoot()));
        embeddedWidget.resizeFromBottomRightButton(400, 300);
        return embeddedWidget;
    }

    private void addAndResizeTwoTableReport() {
        dashboardsPage.addReportToDashboard(FIRST_REPORT)
            .getReport(FIRST_REPORT, TableReport.class).resizeFromTopLeftButton(-300, 0);
        dashboardsPage.addReportToDashboard(SECOND_REPORT);
        RIGHT.moveElementToRightPlace(getReport(SECOND_REPORT).getRoot());
    }

    private String getCurrentValueFilter(String attribute) {
        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        String results = getFilter(attribute).getCurrentValue();
        BrowserUtils.switchToMainWindow(browser);
        return results;
    }

    private List<String> getAttributeValues(String reportName) {
        browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
        List<String> results = getReport(reportName).waitForLoaded().getAttributeValues();
        BrowserUtils.switchToMainWindow(browser);
        return results;
    }

    private void waitForReportLoaded(List<String> reports) {
        if (!isEmpty(reports)) {
            browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
            reports.forEach(e ->
                EmbeddedDashboard.getInstance(browser).getReport(e, TableReport.class).waitForLoaded());
            BrowserUtils.switchToMainWindow(browser);
        }
    }

    private void waitForFilterLoaded(List<String> filters) {
        if (!isEmpty(filters)) {
            browser.switchTo().frame(waitForElementPresent(BY_IFRAME, browser));
            filters.forEach(e -> Graphene.waitGui().until(browser ->
                !EmbeddedDashboard.getInstance(browser).getFilterWidgetByName(e).getCurrentValue().isEmpty()));
            BrowserUtils.switchToMainWindow(browser);
        }
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
}
