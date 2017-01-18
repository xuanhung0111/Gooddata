package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.getVariableUri;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUserProfileUri;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
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

public class GoodSalesDefaultFilterWithSavedViewTest extends AbstractDashboardWidgetTest {

    private static final String DASHBOARD_WITH_SAVED_VIEW = "Dashboard-with-saved-view";

    private static final String DF_VARIABLE = "DF-Variable";
    private static final String REPORT_WITH_ADDITIONAL_ATTRIBUTE = "Report-with-additional-attribute";
    private static final String REPORT_WITH_PROMPT_FILTER = "Report-with-prompt-filter";

    private static final String DEFAULT_VIEW = "Default View (reset filters)";
    private static final String SAVED_VIEW_WITH_STAGE_NAME_FILTER = "SavedView-StageName";
    private static final String SAVED_VIEW_WITH_ALL_FILTERS = "SavedView-All-Filters";

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String DIRECT_SALES = "Direct Sales";
    private static final String INSIDE_SALES = "Inside Sales";
    private static final String ALL = "All";

    private boolean multipleChoice;

    @BeforeClass(alwaysRun = true)
    public void setUp(ITestContext context) {
        multipleChoice = parseBoolean(context.getCurrentXmlTest().getParameter("multipleChoice"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        initVariablePage().createVariable(new AttributeVariable(DF_VARIABLE)
                .withAttribute(ATTR_STAGE_NAME)
                .withAttributeValues(asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT)));

        Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));

        Attribute stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        Attribute departmentAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_DEPARTMENT));

        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), DF_VARIABLE);

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

    @Test(dependsOnGroups = {"precondition"}, groups = {"initSavedView"})
    public void initDashboardWithSavedView() {
        initDashboardsPage()
                .addNewDashboard(DASHBOARD_WITH_SAVED_VIEW)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(DF_VARIABLE).changeSelectionToOneValue();
        }
        dashboardsPage.saveDashboard();

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER, DF_VARIABLE);

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(SHORT_LIST);
        getFilter(DF_VARIABLE).changeAttributeFilterValues(SHORT_LIST);
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_ALL_FILTERS);
    }

    @Test(dependsOnGroups = {"initSavedView"}, groups = {"df-single", "df-multiple"})
    public void checkDefaultFilterAffectOnDefaultViewOnly() {
        initDashboardsPage().selectDashboard(DASHBOARD_WITH_SAVED_VIEW);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(RISK_ASSESSMENT);
        getFilter(DF_VARIABLE).editAttributeFilterValues(RISK_ASSESSMENT);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-is-set-on-default-saved-view", getClass());
        assertThat(DEFAULT_VIEW, containsString(savedViewWidget.getCurrentSavedView()));
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), RISK_ASSESSMENT);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), RISK_ASSESSMENT);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(RISK_ASSESSMENT));

        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        takeScreenshot(browser, "Default-filter-does-not-affect-to-saved-view-with-stage-name-filter", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), RISK_ASSESSMENT);
        assertTrue(getReport(REPORT_WITH_PROMPT_FILTER).isNoData(), "Report is not render correctly");

        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_ALL_FILTERS);
        takeScreenshot(browser, "Default-filter-does-not-affect-to-saved-view-with-all-filters", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(SHORT_LIST));
    }

    @Test(dependsOnGroups = {"initSavedView"}, groups = {"df-single", "df-multiple"})
    public void setDefaultFilterFromSavedViewWithOneFilter() {
        initDashboardsPage().selectDashboard(DASHBOARD_WITH_SAVED_VIEW);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(RISK_ASSESSMENT);
        getFilter(DF_VARIABLE).editAttributeFilterValues(RISK_ASSESSMENT);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-not-affect-on-stage-name-filter-included-in-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), RISK_ASSESSMENT);

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), RISK_ASSESSMENT);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), RISK_ASSESSMENT);
    }

    @Test(dependsOnGroups = {"initSavedView"}, groups = {"df-single", "df-multiple"})
    public void setDefaultFilterFromSavedViewWithAllFilters() {
        initDashboardsPage().selectDashboard(DASHBOARD_WITH_SAVED_VIEW);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().selectSavedView(SAVED_VIEW_WITH_ALL_FILTERS);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(INTEREST);
        getFilter(DF_VARIABLE).editAttributeFilterValues(INTEREST);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-not-affect-on-all-filters-included-in-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_ALL_FILTERS);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), INTEREST);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void setInitialValueForFilterGroupOnSavedView() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(getFilter(DF_VARIABLE).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(DF_VARIABLE).changeSelectionToOneValue();
        }
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        dashboardsPage.applyValuesForGroupFilter();

        SavedViewWidget savedViewWidget = dashboardsPage
                .getSavedViewWidget()
                .openSavedViewMenu()
                .saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER, DF_VARIABLE);

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(SHORT_LIST);
        getFilter(DF_VARIABLE).editAttributeFilterValues(SHORT_LIST);
        dashboardsPage.applyValuesForGroupFilter().saveDashboard();
        getReport(REPORT_WITH_PROMPT_FILTER).waitForReportLoading();

        takeScreenshot(browser, "DF-applied-for-filter-group-on-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
        assertTrue(getReport(REPORT_WITH_PROMPT_FILTER).isNoData(), "Report is not rendered correctly");

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(SHORT_LIST));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void addAdditionalFilterFromSavedView() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT_WITH_ADDITIONAL_ATTRIBUTE)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        if (!multipleChoice) getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(SHORT_LIST);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);
        if (!multipleChoice) getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Add-additional-filter-but-do-not-set-default-value-from-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        if (multipleChoice) {
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(),
                    asList(SHORT_LIST, DIRECT_SALES, INSIDE_SALES));
        } else {
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
            assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(),
                    asList(SHORT_LIST, DIRECT_SALES));
        }

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        if (multipleChoice) {
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(),
                    asList(DISCOVERY, DIRECT_SALES, INSIDE_SALES));
        } else {
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
            assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(),
                    asList(DISCOVERY, DIRECT_SALES));
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void setInitialValueForAdditionalFilterFromSavedView() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT_WITH_ADDITIONAL_ATTRIBUTE)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(SHORT_LIST);

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        savedViewWidget.openSavedViewMenu().saveCurrentView(SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);

        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);
        getFilter(ATTR_DEPARTMENT).editAttributeFilterValues(INSIDE_SALES);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Add-and-set-default-value-for-additional-filter-from-saved-view", getClass());
        assertEquals(savedViewWidget.getCurrentSavedView(), SAVED_VIEW_WITH_STAGE_NAME_FILTER);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), INSIDE_SALES);
        assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(), asList(SHORT_LIST, INSIDE_SALES));

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), INSIDE_SALES);
        assertEquals(getReport(REPORT_WITH_ADDITIONAL_ATTRIBUTE).getAttributeElements(), asList(DISCOVERY, INSIDE_SALES));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-multiple"}, description = "Verify default view of "
            + "viewer user updated depend on permission of variable filter for this user in multiple choice mode")
    public void checkViewerDefaultViewUpdatedInMultipleChoiceMode() throws JSONException, ParseException, IOException {
        final String dashboard = generateDashboardName();

        selectViewerSpecificValuesFrom(DF_VARIABLE, INTEREST, DISCOVERY, SHORT_LIST);
        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        getFilter(DF_VARIABLE).editAttributeFilterValues(INTEREST, DISCOVERY, RISK_ASSESSMENT);
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
            getFilter(DF_VARIABLE).editAttributeFilterValues(RISK_ASSESSMENT);
            dashboardsPage.saveDashboard();

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashboard);

            takeScreenshot(browser, "Viewer-default-filter-shows-all-when-the-value-set-by-admin-out-of-range", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, DISCOVERY, SHORT_LIST));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            restoreViewerSpecificValuesFrom(DF_VARIABLE);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single"}, description = "Verify default view of "
            + "viewer user updated depend on permission of variable filter for this user in single choice mode")
    public void checkViewerDefaultViewUpdatedInSingleChoiceMode() throws ParseException, JSONException, IOException {
        final String dashboard = generateDashboardName();

        selectViewerSpecificValuesFrom(DF_VARIABLE, INTEREST, DISCOVERY, SHORT_LIST);
        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        getFilter(DF_VARIABLE)
                .changeSelectionToOneValue()
                .editAttributeFilterValues(RISK_ASSESSMENT);

        dashboardsPage.saveDashboard().publishDashboard(true);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(RISK_ASSESSMENT));

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashboard);

            takeScreenshot(browser, "Viewer-default-filter-shows-first-value-when-out-of-range-in-single-choice-mode", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), INTEREST);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(INTEREST));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            restoreViewerSpecificValuesFrom(DF_VARIABLE);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-multiple"}, description = "Verify saved view of viewer "
            + "user updated well after admin change permission of variable filter for this user in multiple choice mode")
    public void checkViewerSavedViewUpdatedInMultipleChoiceMode() throws JSONException, ParseException, IOException {
        final String dashboard = generateDashboardName();
        final String savedView1 = "SavedView1";
        final String savedView2 = "SavedView2";

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        dashboardsPage.saveDashboard().publishDashboard(true);

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            SavedViewWidget savedViewWidget = initDashboardsPage().selectDashboard(dashboard).getSavedViewWidget();

            getFilter(DF_VARIABLE).changeAttributeFilterValues(INTEREST);
            savedViewWidget.openSavedViewMenu().saveCurrentView(savedView1);
            getFilter(DF_VARIABLE).changeAttributeFilterValues(INTEREST, DISCOVERY);
            savedViewWidget.openSavedViewMenu().saveCurrentView(savedView2);

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            selectViewerSpecificValuesFrom(DF_VARIABLE, DISCOVERY, SHORT_LIST);

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            savedViewWidget = initDashboardsPage()
                    .selectDashboard(dashboard)
                    .getSavedViewWidget()
                    .openSavedViewMenu()
                    .selectSavedView(savedView1);

            takeScreenshot(browser, "Viewer-saved-view-shows-all-when-value-out-of-range-variable-filter", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(DISCOVERY, SHORT_LIST));
    
            savedViewWidget.openSavedViewMenu().selectSavedView(savedView2);
            takeScreenshot(browser, "Viewer-saved-view-updated-upon-change-of-variable-filter", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(DISCOVERY));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            restoreViewerSpecificValuesFrom(DF_VARIABLE);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single"}, description = "Verify saved view of viewer "
            + "user updated well after admin change permission of variable filter for this user in single choice mode")
    public void checkViewerSavedViewUpdatedInSingleChoiceMode() throws JSONException, ParseException, IOException {
        final String dashboard = generateDashboardName();
        final String savedView = "SavedView";

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        getFilter(DF_VARIABLE).changeSelectionToOneValue();
        dashboardsPage.saveDashboard().publishDashboard(true);

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            SavedViewWidget savedViewWidget = initDashboardsPage().selectDashboard(dashboard).getSavedViewWidget();

            getFilter(DF_VARIABLE).changeAttributeFilterValues(DISCOVERY);
            savedViewWidget.openSavedViewMenu().saveCurrentView(savedView);

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            selectViewerSpecificValuesFrom(DF_VARIABLE, INTEREST, SHORT_LIST);

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            savedViewWidget = initDashboardsPage()
                    .selectDashboard(dashboard)
                    .getSavedViewWidget()
                    .openSavedViewMenu()
                    .selectSavedView(savedView);

            takeScreenshot(browser, "Viewer-saved-view-shows-first-value-when-out-of-range-in-single-choice-mode", getClass());
            // Due to WA-6145, filter shows all instead of first value in available list
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, SHORT_LIST));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            restoreViewerSpecificValuesFrom(DF_VARIABLE);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-multiple"}, description = "Verify viewer default view work "
            + "properly in case the value is out of range of variable filter when switch from saved view to")
    public void checkViewerDefaultViewInSpecialCase() throws JSONException, ParseException, IOException {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        getFilter(DF_VARIABLE).editAttributeFilterValues(RISK_ASSESSMENT);
        dashboardsPage.saveDashboard().publishDashboard(true);

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            SavedViewWidget savedViewWidget = initDashboardsPage()
                    .selectDashboard(dashboard)
                    .getSavedViewWidget();

            getFilter(DF_VARIABLE).changeAttributeFilterValues(INTEREST);
            savedViewWidget.openSavedViewMenu().saveCurrentView("SavedView1");
            getFilter(DF_VARIABLE).changeAttributeFilterValues(INTEREST, DISCOVERY);
            savedViewWidget.openSavedViewMenu().saveCurrentView("SavedView2");

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            selectViewerSpecificValuesFrom(DF_VARIABLE, INTEREST, SHORT_LIST);

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage()
                    .selectDashboard(dashboard)
                    .getSavedViewWidget()
                    .openSavedViewMenu()
                    .selectSavedView(DEFAULT_VIEW);

            getReport(REPORT_WITH_PROMPT_FILTER).waitForReportLoading();
            takeScreenshot(browser, "Viewer-default-view-shows-all-when-out-of-permission", getClass());
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), asList(INTEREST, SHORT_LIST));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            restoreViewerSpecificValuesFrom(DF_VARIABLE);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private void selectViewerSpecificValuesFrom(String variable, String... values) 
            throws ParseException, JSONException, IOException {
        initVariablePage()
                .openVariableFromList(variable)
                .selectUserSpecificAttributeValues(getViewerProfileUri(), asList(values))
                .saveChange();
    }

    private void restoreViewerSpecificValuesFrom(String variable) throws ParseException, JSONException, IOException {
        initVariablePage()
                .openVariableFromList(variable)
                .restoreUserSpecificValuesToDefault(getViewerProfileUri())
                .saveChange();
    }

    private String getViewerProfileUri() throws ParseException, JSONException, IOException {
        RestApiClient restApiClient = testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient();
        return getUserProfileUri(restApiClient, testParams.getUserDomain(), testParams.getViewerUser());
    }
}
