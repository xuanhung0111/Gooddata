package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.addMufToUser;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createMufObjectByUri;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.getVariableUri;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUserProfileUri;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;

public class GoodSalesDefaultFilterMultipleChoiceTest extends AbstractDashboardWidgetTest {

    private static final String DASHBOARD_WITH_SAVED_VIEW = "Dashboard-with-saved-view";
    private static final String DASHBOARD_MUF = "Muf-Dashboard";

    private static final String DF_VARIABLE = "DF-Variable";
    private static final String MUF_DF_VARIABLE = "Muf-Df-Variable";
    private static final String REPORT = "Report";
    private static final String REPORT_WITH_ADDITIONAL_ATTRIBUTE = "Report-with-additional-attribute";
    private static final String REPORT_WITH_PROMPT_FILTER = "Report-with-prompt-filter";
    private static final String REPORT_MUF = "Report-muf";

    private static final String DEFAULT_VIEW = "Default View (reset filters)";
    private static final String UNSAVED_VIEW = "* Unsaved View";
    private static final String SAVED_VIEW_WITH_STAGE_NAME_FILTER = "SavedView-StageName";
    private static final String SAVED_VIEW_WITH_ALL_FILTERS = "SavedView-All-Filters";

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String DIRECT_SALES = "Direct Sales";
    private static final String INSIDE_SALES = "Inside Sales";
    private static final String COMPUSCI = "CompuSci";
    private static final String EDUCATIONLY = "Educationly";
    private static final String EXPLORER = "Explorer";
    private static final String PHOENIXSOFT = "PhoenixSoft";
    private static final String ALL = "All";

    private Metric amountMetric;

    @BeforeClass(alwaysRun = true)
    public void addUsers() {
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initData() throws JSONException, IOException {
        initVariablePage().createVariable(new AttributeVariable(DF_VARIABLE)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT)));

        amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));

        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        Attribute departmentAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_DEPARTMENT));

        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), DF_VARIABLE);

        createReportViaRest(GridReportDefinitionContent.create(REPORT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric))));

        createReportViaRest(GridReportDefinitionContent.create(REPORT_WITH_ADDITIONAL_ATTRIBUTE,
                singletonList(METRIC_GROUP),
                asList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle()),
                        new AttributeInGrid(departmentAttribute.getDefaultDisplayForm().getUri(), departmentAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric))));

        createReportViaRest(GridReportDefinitionContent.create(REPORT_WITH_PROMPT_FILTER,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric)),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));
    }

    @DataProvider(name = "filterProvider")
    public Object[][] getFilterProvider() {
        return new Object[][] {
            {DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME, REPORT},
            {DashAttributeFilterTypes.PROMPT, DF_VARIABLE, REPORT_WITH_PROMPT_FILTER}
        };
    }

    @Test(dependsOnMethods = {"initData"}, dataProvider = "filterProvider")
    public void checkDefaultFilterApplied(DashAttributeFilterTypes type, String name, String report) {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(report)
                .addAttributeFilterToDashboard(type, name);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(report).getRoot());

        getFilter(name).changeAttributeFilterValue(INTEREST);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-is-applied-with-" + type + "-" + INTEREST, getClass());
        assertEquals(getFilter(name).getCurrentValue(), INTEREST);
        assertEquals(getReport(report).getAttributeElements(), singletonList(INTEREST));

        refreshDashboardsPage();
        takeScreenshot(browser, "Default-filter-is-kept-after-refresh-page", getClass());
        assertEquals(getFilter(name).getCurrentValue(), INTEREST);
        assertEquals(getReport(report).getAttributeElements(), singletonList(INTEREST));

        dashboardsPage.editDashboard();
        getFilter(name).changeAttributeFilterValue(DISCOVERY);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-is-applied-with-" + type + "-" + DISCOVERY, getClass());
        assertEquals(getFilter(name).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(report).getAttributeElements(), singletonList(DISCOVERY));
    }

    @Test(dependsOnMethods = {"initData"})
    public void setInitialDashViewForViewer() throws JSONException {
        final String dashBoard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashBoard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(INTEREST);
        getFilter(DF_VARIABLE).changeAttributeFilterValue(INTEREST);
        dashboardsPage.saveDashboard().publishDashboard(true);

        takeScreenshot(browser, "Attribute-and-prompt-default-filter-combination-applied", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), INTEREST);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(INTEREST));

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashBoard);

            takeScreenshot(browser, "Default-filter-for-viewer-is-applied-same-as-admin", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), INTEREST);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(INTEREST));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void turnOnDashboardSavedViewAfterSettingDefaultFilter() throws JSONException {
        final String dashBoard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashBoard)
                .addReportToDashboard(REPORT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT).getRoot());
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(INTEREST);
        dashboardsPage.saveDashboard().publishDashboard(true);

        takeScreenshot(browser, "Default-view-displayed-after-turn-on-dashboard-saved-view", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(INTEREST));
        assertThat(DEFAULT_VIEW, containsString(dashboardsPage.getSavedViewWidget().getCurrentSavedView()));

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashBoard);

            takeScreenshot(browser, "Default-view-applied-for-viewer-role", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
            assertEquals(getReport(REPORT).getAttributeElements(), singletonList(INTEREST));
            assertThat(DEFAULT_VIEW, containsString(dashboardsPage.getSavedViewWidget().getCurrentSavedView()));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkDashboardRenderWithDefaultFilter() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT).getRoot());
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(INTEREST);
        dashboardsPage.saveDashboard();

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(INTEREST));
        assertThat(DEFAULT_VIEW, containsString(savedViewWidget.getCurrentSavedView()));

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(DISCOVERY);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(DISCOVERY));
        assertEquals(savedViewWidget.getCurrentSavedView(), UNSAVED_VIEW);

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        takeScreenshot(browser, "Dashboard-render-with-default-filter", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(INTEREST));
    }

    @Test(dependsOnMethods = {"initData"})
    public void initDashboardWithSavedView() {
        initDashboardsPage()
                .addNewDashboard(DASHBOARD_WITH_SAVED_VIEW)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        dashboardsPage.saveDashboard();

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(INTEREST);
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER, DF_VARIABLE);

        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(INTEREST));

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(DISCOVERY);
        getFilter(DF_VARIABLE).changeAttributeFilterValue(DISCOVERY);
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_ALL_FILTERS);

        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_ALL_FILTERS);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(DISCOVERY));
    }

    @Test(dependsOnMethods = {"initDashboardWithSavedView"})
    public void checkDefaultFilterAffectOnDefaultViewOnly() {
        initDashboardsPage().selectDashboard(DASHBOARD_WITH_SAVED_VIEW);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(SHORT_LIST);
        getFilter(DF_VARIABLE).changeAttributeFilterValue(SHORT_LIST);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-is-set-on-default-saved-view", getClass());
        assertThat(DEFAULT_VIEW, containsString(savedViewWidget.getCurrentSavedView()));
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(SHORT_LIST));

        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        takeScreenshot(browser, "Default-filter-does-not-affect-to-saved-view-with-stage-name-filter", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
        assertTrue(getReport(REPORT_WITH_PROMPT_FILTER).isNoData(), "Report is not render correctly");

        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_ALL_FILTERS);
        takeScreenshot(browser, "Default-filter-does-not-affect-to-saved-view-with-all-filters", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(DISCOVERY));
    }

    @Test(dependsOnMethods = {"initDashboardWithSavedView"})
    public void setDefaultFilterFromSavedViewWithOneFilter() {
        initDashboardsPage().selectDashboard(DASHBOARD_WITH_SAVED_VIEW);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(SHORT_LIST);
        getFilter(DF_VARIABLE).changeAttributeFilterValue(SHORT_LIST);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-not-affect-on-stage-name-filter-included-in-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
    }

    @Test(dependsOnMethods = {"initDashboardWithSavedView"})
    public void setDefaultFilterFromSavedViewWithAllFilters() {
        initDashboardsPage().selectDashboard(DASHBOARD_WITH_SAVED_VIEW);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_ALL_FILTERS);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(RISK_ASSESSMENT);
        getFilter(DF_VARIABLE).changeAttributeFilterValue(RISK_ASSESSMENT);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-not-affect-on-all-filters-included-in-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_ALL_FILTERS);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), RISK_ASSESSMENT);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), RISK_ASSESSMENT);
    }

    @Test(dependsOnMethods = {"initData"})
    public void addAdditionalFilterFromSavedView() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT_WITH_ADDITIONAL_ATTRIBUTE)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(INTEREST);
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(DISCOVERY);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);

        dashboardsPage
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .saveDashboard();

        takeScreenshot(browser, "Add-additional-filter-but-do-not-set-default-value-from-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), ALL);
        assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(),
                asList(DISCOVERY, DIRECT_SALES, INSIDE_SALES));

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), ALL);
        assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(),
                asList(INTEREST, DIRECT_SALES, INSIDE_SALES));
    }

    @Test(dependsOnMethods = {"initData"})
    public void setInitialValueForAdditionalFilterFromSavedView() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT_WITH_ADDITIONAL_ATTRIBUTE)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(INTEREST);
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValue(DISCOVERY);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);
        getFilter(ATTR_DEPARTMENT).changeAttributeFilterValue(DIRECT_SALES);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Add-and-set-default-value-for-additional-filter-from-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
        assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(), asList(DISCOVERY, DIRECT_SALES));

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
        assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(), asList(INTEREST, DIRECT_SALES));
    }

    @Test(dependsOnMethods = {"initData"})
    public void checkViewerDefaultViewUpdatedUponChangedVariableFilter() throws JSONException, ParseException, IOException {
        final String dashboard = generateDashboardName();

        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        initVariablePage()
                .openVariableFromList(DF_VARIABLE)
                .selectUserSpecificAttributeValues(
                        getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getViewerUser()),
                        asList(INTEREST, DISCOVERY, SHORT_LIST))
                .saveChange();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        getFilter(DF_VARIABLE).changeAttributeFilterValue(INTEREST, DISCOVERY, RISK_ASSESSMENT);
        dashboardsPage.saveDashboard().publishDashboard(true);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, DISCOVERY, RISK_ASSESSMENT));

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashboard);

            takeScreenshot(browser, "Viewer-default-filter-is-updated-with-only-values-allowed-to-access", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), String.join(", ", INTEREST, DISCOVERY));
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, DISCOVERY));

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            initDashboardsPage().selectDashboard(dashboard).editDashboard();
            getFilter(DF_VARIABLE).changeAttributeFilterValue(RISK_ASSESSMENT);
            dashboardsPage.saveDashboard();

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashboard);

            takeScreenshot(browser, "Viewer-default-filter-shows-all-when-the-value-set-by-admin-out-of-range", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, DISCOVERY, SHORT_LIST));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "mufProvider")
    public Object[][] getMufProvider() throws ParseException, JSONException, IOException {
        final Attribute productAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PRODUCT));
        final String productValues = getMdService()
                .getAttributeElements(productAttribute)
                .subList(0, 3)
                .stream()
                .map(e -> e.getUri())
                .map(e -> format("[%s]", e))
                .collect(joining(","));

        final String expression = format("[%s] IN (%s)", productAttribute.getUri(), productValues);
        final String mufUri = createMufObjectByUri(getRestApiClient(), testParams.getProjectId(), "muf", expression);

        return new Object[][] {
            {testParams.getEditorUser(), UserRoles.EDITOR, mufUri},
            {testParams.getViewerUser(), UserRoles.VIEWER, mufUri}
        };
    }

    @Test(dependsOnGroups = {"createProject"})
    public void prepareDashboardForMufUser() throws JSONException, IOException {
        initVariablePage().createVariable(new AttributeVariable(MUF_DF_VARIABLE).withAttribute(ATTR_PRODUCT));

        Attribute productAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PRODUCT));
        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), MUF_DF_VARIABLE);

        createReportViaRest(GridReportDefinitionContent.create(REPORT_MUF,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(productAttribute.getDefaultDisplayForm().getUri(), productAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric)),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));

        initDashboardsPage()
                .addNewDashboard(DASHBOARD_MUF)
                .addReportToDashboard(REPORT_MUF)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, MUF_DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_MUF).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_PRODUCT).getRoot());
        dashboardsPage.saveDashboard().publishDashboard(true);

        takeScreenshot(browser, "Muf-Dashboard", getClass());
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"prepareDashboardForMufUser"}, dataProvider = "mufProvider")
    public void checkMufUserInitialValuesWithDefaultView(String user, UserRoles role, String mufObjectUri)
            throws ParseException, IOException, JSONException {
        addMufToUser(getRestApiClient(), testParams.getProjectId(), user, mufObjectUri);

        try {
            initDashboardsPage().selectDashboard(DASHBOARD_MUF).editDashboard();
            getFilter(ATTR_PRODUCT).changeAttributeFilterValue(COMPUSCI, EDUCATIONLY, PHOENIXSOFT);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValue(COMPUSCI, PHOENIXSOFT);

            dashboardsPage.saveDashboard();
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), asList(COMPUSCI, PHOENIXSOFT));

            logoutAndLoginAs(canAccessGreyPage(browser), role);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF);

            takeScreenshot(browser, "DF-is-updated-with-muf-value-in-default-view-with-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), String.join(", ", COMPUSCI, EDUCATIONLY));
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), COMPUSCI);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(COMPUSCI));

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF).editDashboard();
            getFilter(ATTR_PRODUCT).changeAttributeFilterValue(PHOENIXSOFT);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValue(PHOENIXSOFT);

            dashboardsPage.saveDashboard();
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(PHOENIXSOFT));

            logoutAndLoginAs(canAccessGreyPage(browser), role);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF);

            takeScreenshot(browser, "DF-shows-all-when-value-set-by-admin-out-of-range-with-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), ALL);
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), asList(COMPUSCI, EDUCATIONLY, EXPLORER));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    private void refreshDashboardsPage() {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
    }
}
