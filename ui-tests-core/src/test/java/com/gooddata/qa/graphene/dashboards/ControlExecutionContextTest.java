package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.entity.attribute.ComputedAttributeDefinition;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedDashboardTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.TOP_RIGHT;
import static com.gooddata.qa.mdObjects.dashboard.tab.TabItem.ItemPosition.LEFT;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ControlExecutionContextTest extends AbstractEmbeddedDashboardTest {

    private static final String INTEREST = "Interest";
    private static final String DISCOVERY = "Discovery";
    private static final String SHORT_LIST = "Short List";
    private static final String RISK_ASSESSMENT = "Risk Assessment";
    private static final String CONVICTION = "Conviction";
    private static final String NEGOTIATION = "Negotiation";
    private static final String CLOSED_WON = "Closed Won";
    private static final String CLOSED_LOST = "Closed Lost";
    private static final String REPORT_AMOUNT_BY_STAGE_NAME_AND_YEAR_SNAP_SHOT = "Sum of amount by stage name and year snap shot";
    private static final String REPORT_AMOUNT_BY_VARIABLE = "Sum of amount by variable";
    private static final String REPORT_AMOUNT_BY_COMPUTED_ATTRIBUTE = "Sum of amount by computed attribute";
    private static final String COMPUTED_ATTRIBUTE = "Computed attribute";
    private static final String BEST = "Best";
    private static final String GREAT = "Great";
    private static final String GOOD = "Good";

    private final static int currentYear = LocalDate.now().getYear();
    private DashboardRestRequest dashboardRequest;
    private UserManagementRestRequest userManagementRestRequest;
    private String variableUri;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.CONTROL_EXECUTION_CONTEXT_ENABLED, true);
        getMetricCreator().createAmountMetric();
        variableUri = getVariableCreator().createFilterVariable("Variable", getAttributeByTitle(ATTR_STAGE_NAME).getUri(),
                asList(INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT, CONVICTION, NEGOTIATION, CLOSED_WON, CLOSED_LOST));

        initAttributePage().moveToCreateAttributePage().createComputedAttribute(
                new ComputedAttributeDefinition().withAttribute(ATTR_STAGE_NAME).withMetric(METRIC_AMOUNT)
                        .withName(COMPUTED_ATTRIBUTE)
                        .withBucket(new ComputedAttributeDefinition.AttributeBucket(0, GOOD, "2000000"))
                        .withBucket(new ComputedAttributeDefinition.AttributeBucket(1, GREAT, "3000000"))
                        .withBucket(new ComputedAttributeDefinition.AttributeBucket(2, BEST)));

        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_COMPUTED_ATTRIBUTE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(COMPUTED_ATTRIBUTE))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_STAGE_NAME_AND_YEAR_SNAP_SHOT,
                Arrays.asList(new AttributeInGrid(getAttributeByTitle(ATTR_YEAR_SNAPSHOT)), METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_STAGE_NAME,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT)))));

        createReport(GridReportDefinitionContent.create(REPORT_AMOUNT_BY_VARIABLE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(ATTR_STAGE_NAME))),
                singletonList(new MetricElement(getMetricByTitle(METRIC_AMOUNT))),
                singletonList(new Filter(format("[%s]", variableUri)))));
        //To avoid case has only one dashboard
        createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
    }

    /**
     * To provide data to define filter with all values or some default values
     * and filter's position on dashboard. Purpose helps test cover both 2 cases as default and
     * change setting filter.
     */
    @DataProvider(name = "defaultFilters")
    public Object[][] getDefaultFilters() throws IOException {
        FilterItemContent allValuesFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent defaultValuesFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME), INTEREST, SHORT_LIST);
        return new Object[][] {
                {Pair.of(allValuesFilter, RIGHT)},
                {Pair.of(defaultValuesFilter, RIGHT)},
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "defaultFilters")
    public void setUriParameterMultipleFilters(Pair<FilterItemContent, ItemPosition> filterContent) throws IOException {
        String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
        initDashboardsPage().selectDashboard(dashboardName);

        takeScreenshot(browser, "set_uri_parameter_filters_has_id_" + filterContent.getLeft().getId(), getClass());
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, SHORT_LIST, CONVICTION);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
        TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class);
        assertEquals(filterWidget.getCurrentValue(), format("%s, %s", SHORT_LIST, CONVICTION));
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), asList(SHORT_LIST, CONVICTION));

        filterWidget.openPanel().getAttributeFilterPanel().selectAllItems().submitPanel();
        assertEquals(filterWidget.getCurrentValue(), "All");
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), asList(
                INTEREST, DISCOVERY, SHORT_LIST, RISK_ASSESSMENT, CONVICTION, NEGOTIATION, CLOSED_WON, CLOSED_LOST));

        filterWidget.openPanel().getAttributeFilterPanel().changeValues(INTEREST, CLOSED_WON);
        assertEquals(filterWidget.getCurrentValue(), format("%s, %s", INTEREST, CLOSED_WON));
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), asList(INTEREST, CLOSED_WON));
    }

    @Test(dependsOnGroups = "createProject")
    public void setUriParameterSingleFilter() throws IOException {
        String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
        initDashboardsPage().selectDashboard(dashboardName);

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, SHORT_LIST);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
        TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class);
        assertEquals(filterWidget.getCurrentValue(), SHORT_LIST);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), singletonList(SHORT_LIST));

        filterWidget.openPanel().getAttributeFilterPanel().changeValues(CLOSED_WON);
        assertEquals(filterWidget.getCurrentValue(), CLOSED_WON);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), singletonList(CLOSED_WON));
    }

    /**
     * To provide data to define be multiple values or single value filter
     * and filter's position on dashboard. Purpose helps test cover both 2 configuration filters.
     */
    @DataProvider(name = "configurationFilter")
    public Object[][] getConfigurationFilter() throws IOException {
        FilterItemContent multipleValuesFilter = createMultipleValuesFilter(getAttributeByTitle(COMPUTED_ATTRIBUTE));
        FilterItemContent singleValueFilter = createSingleValueFilter(getAttributeByTitle(COMPUTED_ATTRIBUTE));
        return new Object[][] {
                {Pair.of(multipleValuesFilter, RIGHT), new String[]{BEST, GREAT}},
                {Pair.of(singleValueFilter, RIGHT), new String[]{BEST}},
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "configurationFilter")
    public void filterEmbeddedDashboardByComputedAttribute(Pair<FilterItemContent, ItemPosition> filterContent,
                                                           String[] parameterFilters) throws IOException {
        String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_COMPUTED_ATTRIBUTE, filterContent);
        initDashboardsPage().selectDashboard(dashboardName);

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(COMPUTED_ATTRIBUTE, parameterFilters);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
        TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_COMPUTED_ATTRIBUTE, TableReport.class).waitForLoaded();
        for (String parameterFilter : parameterFilters) {
            assertTrue(filterWidget.getCurrentValue().contains(parameterFilter), "Filter render incorrectly");
        }
        assertEquals(tableReport.getAttributeValues(), asList(parameterFilters));
    }

    /**
     * To provide data to define type filter and expected values
     * Purpose helps test cover all types filter such as: attribute and date filter(quarter/year, month/year or year).
     */
    @DataProvider(name = "getFilterAttribute")
    public Object[][] getFilterAttribute() throws IOException {
        return new Object[][] {
                {Pair.of(ATTR_YEAR_SNAPSHOT, "2010"), asList("$1,185,127.28", "$2,080,448.83", "$1,347,427.16", "$1,222,172.30",
                        "$494,341.51", "$647,612.26", "$8,886,381.82", "$11,058,850.84")},
                {Pair.of(ATTR_QUARTER_YEAR_SNAPSHOT, "Q1/2011"), asList("$1,719,072.21", "$1,456,305.27", "$1,772,094.56",
                        "$1,128,294.69", "$869,266.57", "$607,980.33", "$13,197,254.24", "$15,286,440.08")},
                {Pair.of(ATTR_MONTH_YEAR_SNAPSHOT, "May 2011"), asList("$1,505,006.79", "$2,435,878.81", "$2,161,176.73",
                        "$1,349,148.32", "$821,714.42", "$463,879.80", "$16,956,237.65", "$18,311,820.48")},
                {Pair.of(ATTR_YEAR_CREATED, "2010"), asList("$438,126.48", "$1,883,095.10", "$799,879.77", "$500,490.83",
                        "$385,583.11", "$382,066.39", "$7,238,061.34", "$7,265,451.81", "$372,107.89", "$788,859.09",
                        "$987,167.78", "$728,147.43", "$174,277.59", "$114,395.92", "$12,094,870.87", "$12,961,613.47")}
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "getFilterAttribute")
    public void filterEmbeddedDashboardByDate(Pair<String, String> filterAttribute, List<String> expectedResults) throws IOException {
        String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME_AND_YEAR_SNAP_SHOT,
                Pair.of(createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                        2010 - currentYear, 2012 - currentYear), RIGHT));
        initDashboardsPage().selectDashboard(dashboardName);

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(filterAttribute.getLeft(), filterAttribute.getRight());

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
        TableReport tableReport = embeddedDashboard
                .getReport(REPORT_AMOUNT_BY_STAGE_NAME_AND_YEAR_SNAP_SHOT, TableReport.class).waitForLoaded();
        takeScreenshot(browser, "filter_embedded_dashboard_by_" + filterAttribute.getLeft(), getClass());
        assertEquals(filterWidget.getCurrentValue(), "2010 - 2012");
        assertEquals(tableReport.getRawMetricValues(), expectedResults);
    }

    /**
     * To provide data to define be multiple values or single value filter
     * Purpose helps test cover both 2 configuration filters and verify expected value after editing on embedded mode.
     */
    @DataProvider(name = "typeFilter")
    public Object[][] getTypeFilter() throws IOException {
        FilterItemContent multipleValuesFilter = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent singleValueFilter = createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        return new Object[][] {
                {Pair.of(multipleValuesFilter, RIGHT), "All", asList(INTEREST,
                        DISCOVERY, SHORT_LIST, RISK_ASSESSMENT, CONVICTION, NEGOTIATION, CLOSED_WON, CLOSED_LOST)},
                {Pair.of(singleValueFilter, RIGHT), INTEREST, singletonList(INTEREST)},
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "typeFilter")
    public void resetFilterAfterEditing(Pair<FilterItemContent, ItemPosition> filterContent, String expectedFilter,
                                        List<String> expectedReports) throws IOException {
        String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME, filterContent);
        initDashboardsPage().selectDashboard(dashboardName);

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, DISCOVERY);

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
        TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class);
        assertEquals(filterWidget.getCurrentValue(), DISCOVERY);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), singletonList(DISCOVERY));
        embeddedDashboard.editDashboard().cancelDashboard();
        assertEquals(filterWidget.getCurrentValue(), expectedFilter);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), expectedReports);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addMufForUser() throws ParseException, JSONException, IOException {
        List<String> listFilters = asList(DISCOVERY, SHORT_LIST);
        Attribute stageNameAttribute = getAttributeByTitle(ATTR_STAGE_NAME);
        List<AttributeElement> stageNameValues = getMdService().getAttributeElements(stageNameAttribute).stream()
                .filter(attributeElement -> listFilters.contains(attributeElement.getTitle()))
                .collect(Collectors.toList());

        final List<String> filteredElementUris = stageNameValues.stream()
                .map(AttributeElement::getUri).collect(Collectors.toList());

        final Map<String, Collection<String>> conditions = new HashMap<>();
        conditions.put(stageNameAttribute.getUri(), filteredElementUris);

        dashboardRequest.addMufToUser(userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(),
                testParams.getEditorUser()), dashboardRequest.createSimpleMufObjByUri("MUF", conditions));
    }

    /**
     * To provide data to define parameter values
     * Purpose helps test cover 3 cases: inner, outer and overlap between attribute elements and muf
     */
    @DataProvider(name = "parameterValues")
    public Object[][] getParameterValues() throws IOException {
        return new Object[][] {
                {new String[]{INTEREST, DISCOVERY}, DISCOVERY, singletonList(DISCOVERY)},
                {new String[]{DISCOVERY, SHORT_LIST}, "All", asList(DISCOVERY, SHORT_LIST)},
                {new String[]{CONVICTION}, "All", emptyList()}
        };
    }

    @Test(dependsOnMethods = "addMufForUser", dataProvider = "parameterValues")
    public void applyMufOnEmbeddedDashboard(String[] parameterValues, String filterValue, List<String> reportValue)
            throws IOException {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                    Pair.of(createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
            initDashboardsPage().selectDashboard(dashboardName);

            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, parameterValues);

            embeddedUri = embedDashboardDialog.getPreviewURI();
            EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
            FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
            TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class);
            assertEquals(filterWidget.getCurrentValue(), filterValue);
            assertEquals(tableReport.waitForLoaded().getAttributeValues(), reportValue);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = "addMufForUser", dataProvider = "parameterValues")
    public void applyMufOnEmbeddedDashboardForVariableValue(String[] parameterValues, String filterValue,
                                                            List<String> reportValue) throws IOException {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            String dashboardName = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_VARIABLE,
                    Pair.of(createMultipleValuesFilterBy(variableUri), RIGHT));
            initDashboardsPage().selectDashboard(dashboardName);

            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, parameterValues);

            embeddedUri = embedDashboardDialog.getPreviewURI();
            EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
            FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
            TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_VARIABLE, TableReport.class);
            assertEquals(filterWidget.getCurrentValue(), filterValue);
            assertEquals(tableReport.waitForLoaded().getAttributeValues(), reportValue);
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    /**
     * To provide data to define parameter values and dashboard has report depend on attribute or variable
     * Purpose helps test cover 3 cases: inner and outer between attribute elements and muf
     */
    @DataProvider(name = "parameterValue")
    public Object[][] getParameterValue() throws IOException {
        String dashboardHasAttribute = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_STAGE_NAME,
                Pair.of(createSingleValueFilter(getAttributeByTitle(ATTR_STAGE_NAME)), RIGHT));
        String dashboardHasVariable = createDashboardWithReportAndFilter(REPORT_AMOUNT_BY_VARIABLE,
                Pair.of(createSingleValuesFilterBy(variableUri), RIGHT));
        return new Object[][] {
                {Pair.of(dashboardHasAttribute, REPORT_AMOUNT_BY_STAGE_NAME), CONVICTION, Pair.of(emptyList(), DISCOVERY)},
                {Pair.of(dashboardHasAttribute, REPORT_AMOUNT_BY_STAGE_NAME), SHORT_LIST,
                        Pair.of(singletonList(SHORT_LIST), SHORT_LIST)},
                {Pair.of(dashboardHasVariable, REPORT_AMOUNT_BY_VARIABLE), CONVICTION, Pair.of(emptyList() ,DISCOVERY)},
                {Pair.of(dashboardHasVariable, REPORT_AMOUNT_BY_VARIABLE), SHORT_LIST,
                        Pair.of(singletonList(SHORT_LIST), SHORT_LIST)}
        };
    }

    @Test(dependsOnMethods = "addMufForUser", dataProvider = "parameterValue")
    public void applyMufOnEmbeddedDashboardForSingleFilter(Pair<String, String> dashboard, String parameterFilter,
                                                           Pair<List<String>, String> expectedValue) throws IOException {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initDashboardsPage().selectDashboard(dashboard.getLeft());

            takeScreenshot(browser, "applyMufOnEmbeddedDashboard", getClass());
            EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
            embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, parameterFilter);

            embeddedUri = embedDashboardDialog.getPreviewURI();
            EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
            FilterWidget filterWidget = embeddedDashboard.getFirstFilter();
            TableReport tableReport = embeddedDashboard.getReport(dashboard.getRight(), TableReport.class);
            assertEquals(filterWidget.getCurrentValue(), expectedValue.getRight());
            assertEquals(tableReport.waitForLoaded().getAttributeValues(), expectedValue.getLeft());
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void preserveFilterBetweenTabsOnEmbeddedMode() throws IOException {
        String dashboardName = createDashboardHasTabs();
        initDashboardsPage().selectDashboard(dashboardName);

        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_STAGE_NAME, SHORT_LIST)
                .selectFilterAttribute(COMPUTED_ATTRIBUTE, BEST)
                .selectFilterAttribute(ATTR_YEAR_SNAPSHOT, "2010", "2011");

        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();
        TableReport tableReport = embeddedDashboard.getReport(REPORT_AMOUNT_BY_STAGE_NAME, TableReport.class);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), singletonList(SHORT_LIST));
        assertEquals(embeddedDashboard.getFilterWidgetByName(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(embeddedDashboard.getFilterWidgetByName(ATTR_YEAR_SNAPSHOT).getCurrentValue(), "2010, 2011");

        embeddedDashboard.openTab(1);
        assertThat(tableReport.waitForLoaded().getAttributeValues(), hasItem(SHORT_LIST));
        assertEquals(embeddedDashboard.getFilterWidgetByName("DATE DIMENSION (SNAPSHOT)").getCurrentValue(), "2010 - 2012");

        embeddedDashboard.openTab(2);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), singletonList(BEST));
        assertEquals(embeddedDashboard.getFilterWidgetByName(COMPUTED_ATTRIBUTE).getCurrentValue(), BEST);

        embeddedDashboard.openTab(3);
        assertEquals(tableReport.waitForLoaded().getAttributeValues(), singletonList(SHORT_LIST));
        assertEquals(embeddedDashboard.getFilterWidgetByName(ATTR_STAGE_NAME).getCurrentValue(), SHORT_LIST);
        assertEquals(embeddedDashboard.getFilterWidgetByName(COMPUTED_ATTRIBUTE).getCurrentValue(), BEST);
        assertEquals(embeddedDashboard.getFilterWidgetByName(ATTR_YEAR_SNAPSHOT).getCurrentValue(), "2010, 2011");
        assertEquals(embeddedDashboard.getFilterWidgetByName("DATE DIMENSION (SNAPSHOT)").getCurrentValue(), "2010 - 2012");
    }

    private String createDashboardWithReportAndFilter(String report, Pair<FilterItemContent, ItemPosition> filters)
            throws IOException {
        String dashboardName = "dashboard " + generateHashString();
        JSONObject dashboardContent = new Dashboard()
                .setName(dashboardName)
                .addTab(initTab("First Tab", report, singletonList(filters)))
                .addFilter(filters.getLeft())
                .getMdObject();

        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId()).createDashboard(dashboardContent);
        return dashboardName;
    }

    private String createDashboardHasTabs() throws IOException {
        FilterItemContent stageNameFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_STAGE_NAME));
        FilterItemContent yearSnapShotFilterContent = createMultipleValuesFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT));
        FilterItemContent dateFilterContent = createDateFilter(getAttributeByTitle(ATTR_YEAR_SNAPSHOT),
                2010 - currentYear, 2012 - currentYear);
        FilterItemContent computedAttributeFilterContent = createMultipleValuesFilter(getAttributeByTitle(COMPUTED_ATTRIBUTE));
        
        String dashboardName = "dashboard " + generateHashString();
        JSONObject dashboardContent = new Dashboard()
                .setName(dashboardName)
                .addTab(initTab("First Tab", REPORT_AMOUNT_BY_STAGE_NAME, asList(
                        Pair.of(stageNameFilterContent, TOP_RIGHT),
                        Pair.of(yearSnapShotFilterContent, RIGHT))))
                .addTab(initTab("Second Tab", REPORT_AMOUNT_BY_STAGE_NAME_AND_YEAR_SNAP_SHOT, singletonList(
                        Pair.of(dateFilterContent, RIGHT))))
                .addTab(initTab("Third Tab", REPORT_AMOUNT_BY_COMPUTED_ATTRIBUTE, singletonList(
                        Pair.of(computedAttributeFilterContent, RIGHT))))
                .addTab(initTab("Fourth Tab", REPORT_AMOUNT_BY_STAGE_NAME, asList(
                        Pair.of(stageNameFilterContent, TOP_RIGHT),
                        Pair.of(dateFilterContent, TOP),
                        Pair.of(computedAttributeFilterContent, LEFT),
                        Pair.of(yearSnapShotFilterContent, RIGHT))))
                .addFilter(stageNameFilterContent)
                .addFilter(yearSnapShotFilterContent)
                .addFilter(dateFilterContent)
                .addFilter(computedAttributeFilterContent)
                .getMdObject();

        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId()).createDashboard(dashboardContent);
        return dashboardName;
    }

    private Tab initTab(String name, String report, List<Pair<FilterItemContent, ItemPosition>> appliedFilters) {
        List<FilterItem> filterItems = appliedFilters.stream()
                .map(this::buildFilterItem)
                .collect(Collectors.toList());
        ReportItem reportItem = createReportItem(getReportByTitle(report).getUri(),
                filterItems.stream().map(FilterItem::getId).collect(Collectors.toList()));
        return new Tab().setTitle(name).addItems(Stream.of(singletonList(reportItem), filterItems).flatMap(List::stream)
                .collect(Collectors.toList()));
    }

    private FilterItem buildFilterItem(Pair<FilterItemContent, ItemPosition> filterItem) {
        FilterItem filterItemContent = new FilterItem().setContentId(filterItem.getLeft().getId());
        filterItemContent.setPosition(filterItem.getRight());
        return filterItemContent;
    }
}
