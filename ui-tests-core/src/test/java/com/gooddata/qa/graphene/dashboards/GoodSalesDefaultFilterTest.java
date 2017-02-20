package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.getVariableUri;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.ITestContext;
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
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;

public class GoodSalesDefaultFilterTest extends AbstractDashboardWidgetTest {

    private static final String DF_VARIABLE = "DF-Variable";
    private static final String REPORT = "Report";
    private static final String REPORT_WITH_PROMPT_FILTER = "Report-with-prompt-filter";

    private static final String DEFAULT_VIEW = "Default View (reset filters)";
    private static final String UNSAVED_VIEW = "* Unsaved View";

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String DIRECT_SALES = "Direct Sales";
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
        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), DF_VARIABLE);

        createReportViaRest(GridReportDefinitionContent.create(REPORT,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(stageNameAttribute.getDefaultDisplayForm().getUri(), stageNameAttribute.getTitle())),
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

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "filterProvider", groups = {"df-single", "df-multiple"})
    public void checkDefaultFilterApplied(DashAttributeFilterTypes type, String name, String report) {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(report)
                .addAttributeFilterToDashboard(type, name);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(report).getRoot());
        if (!multipleChoice) getFilter(name).changeSelectionToOneValue();

        getFilter(name).editAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-is-applied-with-" + type + "-" + DISCOVERY, getClass());
        assertEquals(getFilter(name).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(report).getAttributeElements(), singletonList(DISCOVERY));

        refreshDashboardsPage();
        takeScreenshot(browser, "Default-filter-is-kept-after-refresh-page", getClass());
        assertEquals(getFilter(name).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(report).getAttributeElements(), singletonList(DISCOVERY));

        dashboardsPage.editDashboard();
        getFilter(name).editAttributeFilterValues(SHORT_LIST);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Default-filter-is-applied-with-" + type + "-" + SHORT_LIST, getClass());
        assertEquals(getFilter(name).getCurrentValue(), SHORT_LIST);
        assertEquals(getReport(report).getAttributeElements(), singletonList(SHORT_LIST));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void setInitialDashViewForViewer() throws JSONException {
        final String dashBoard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashBoard)
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(DF_VARIABLE).changeSelectionToOneValue();
        }

        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(DISCOVERY);
        getFilter(DF_VARIABLE).editAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard().publishDashboard(true);

        takeScreenshot(browser, "Attribute-and-prompt-default-filter-combination-applied", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(DISCOVERY));

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashBoard);

            takeScreenshot(browser, "Default-filter-for-viewer-is-applied-same-as-admin", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), DISCOVERY);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(DISCOVERY));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void setInitialDashViewForAttribute() throws JSONException {
        final String dashBoard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashBoard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        }

        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(DISCOVERY);
        getFilter(ATTR_DEPARTMENT).editAttributeFilterValues(DIRECT_SALES);
        dashboardsPage.saveDashboard();

        takeScreenshot(browser, "Attribute-default-filter-combination-applied", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void notApplyValueDashViewForAttributeInPreviewMode() throws JSONException {
        final String dashBoard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashBoard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        }

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        getFilter(ATTR_DEPARTMENT).changeAttributeFilterValues(DIRECT_SALES);
        dashboardsPage.saveDashboard();

        if (multipleChoice) {
            takeScreenshot(browser, "Attribute-default-filter-combination-applied", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), ALL);
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), ALL);
        } else {
            takeScreenshot(browser, "Attribute-default-filter-combination-applied", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
            assertEquals(getFilter(ATTR_DEPARTMENT).getCurrentValue(), DIRECT_SALES);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void turnOnDashboardSavedViewAfterSettingDefaultFilter() throws JSONException {
        final String dashBoard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashBoard)
                .addReportToDashboard(REPORT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT).getRoot());
        if (!multipleChoice) getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();

        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard().publishDashboard(true);

        takeScreenshot(browser, "Default-view-displayed-after-turn-on-dashboard-saved-view", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(DISCOVERY));
        assertThat(DEFAULT_VIEW, containsString(dashboardsPage.getSavedViewWidget().getCurrentSavedView()));

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.VIEWER);
            initDashboardsPage().selectDashboard(dashBoard);

            takeScreenshot(browser, "Default-view-applied-for-viewer-role", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
            assertEquals(getReport(REPORT).getAttributeElements(), singletonList(DISCOVERY));
            assertThat(DEFAULT_VIEW, containsString(dashboardsPage.getSavedViewWidget().getCurrentSavedView()));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void checkDashboardRenderWithDefaultFilter() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT).getRoot());
        if (!multipleChoice) getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();

        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard();

        SavedViewWidget savedViewWidget = dashboardsPage.getSavedViewWidget();
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(DISCOVERY));
        assertThat(DEFAULT_VIEW, containsString(savedViewWidget.getCurrentSavedView()));

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(SHORT_LIST);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(SHORT_LIST));
        assertEquals(savedViewWidget.getCurrentSavedView(), UNSAVED_VIEW);

        savedViewWidget.openSavedViewMenu().selectSavedView(DEFAULT_VIEW);
        takeScreenshot(browser, "Dashboard-render-with-default-filter", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), DISCOVERY);
        assertEquals(getReport(REPORT).getAttributeElements(), singletonList(DISCOVERY));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"df-single", "df-multiple"})
    public void setInitialValueForFilterGroup() {
        initDashboardsPage()
                .addNewDashboard(generateDashboardName())
                .addReportToDashboard(REPORT_WITH_PROMPT_FILTER)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, DF_VARIABLE)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME, DF_VARIABLE);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_WITH_PROMPT_FILTER).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(getFilter(DF_VARIABLE).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(DF_VARIABLE).changeSelectionToOneValue();
        }

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        getFilter(DF_VARIABLE).changeAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveDashboard();
        getReport(REPORT_WITH_PROMPT_FILTER).waitForReportLoading();

        if (multipleChoice) {
            takeScreenshot(browser, "DF-not-applied-for-filter-group", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), ALL);
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(),
                    asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT));
        } else {
            takeScreenshot(browser, "DF-not-applied-for-filter-group", getClass());
            assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
            assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), INTEREST);
            assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(INTEREST));
        }

        dashboardsPage.editDashboard();
        getFilter(ATTR_STAGE_NAME).editAttributeFilterValues(SHORT_LIST);
        getFilter(DF_VARIABLE).editAttributeFilterValues(SHORT_LIST);
        dashboardsPage.saveDashboard();
        getReport(REPORT_WITH_PROMPT_FILTER).waitForReportLoading();

        takeScreenshot(browser, "DF-applied-for-filter-group", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(getFilter(DF_VARIABLE).getCurrentValue(), SHORT_LIST);
        assertEquals(getReport(REPORT_WITH_PROMPT_FILTER).getAttributeElements(), singletonList(SHORT_LIST));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"df-single", "df-multiple"})
    public void changeAnotherFilterValueBeforeApplyGroupFilter() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                .groupFiltersOnDashboard(ATTR_STAGE_NAME);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getFilter(ATTR_STAGE_NAME).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
            getFilter(ATTR_DEPARTMENT).changeSelectionToOneValue();
        }
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(INTEREST);
        getFilter(ATTR_DEPARTMENT).changeAttributeFilterValues(DIRECT_SALES);
        dashboardsPage.applyValuesForGroupFilter();

        takeScreenshot(browser,
                "Filter-value-belongs-to-group-applied-correctly-after-change-another-filter-value", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"df-single", "df-multiple"})
    public void checkFilterResetToDefaultWhenSaveAsDashboard() {
        final String dashboard = generateDashboardName();

        initDashboardsPage()
                .addNewDashboard(dashboard)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME)
                .getFilterWidgetByName(ATTR_STAGE_NAME)
                .editAttributeFilterValues(INTEREST);

        if (!multipleChoice) {
            getFilter(ATTR_STAGE_NAME).changeSelectionToOneValue();
        }
        dashboardsPage.saveDashboard();

        getFilter(ATTR_STAGE_NAME).changeAttributeFilterValues(DISCOVERY);
        dashboardsPage.saveAsDashboard("new-" + dashboard, PermissionType.USE_EXISTING_PERMISSIONS);

        takeScreenshot(browser, "Filter-reset-to-default-after-save-as-dashboard", getClass());
        assertEquals(getFilter(ATTR_STAGE_NAME).getCurrentValue(), INTEREST);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private DashboardsPage refreshDashboardsPage() {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        return dashboardsPage;
    }
}
