package com.gooddata.qa.graphene.filters;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.Fact;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.AvailableValuesConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.SelectionConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.http.variable.VariableRestRequest;
import com.gooddata.qa.utils.io.ResourceUtils;
import com.gooddata.qa.utils.java.Builder;
import com.google.common.collect.Ordering;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MetricAvailableFilterTest extends AbstractDashboardWidgetTest {
    private static final String MORE_INFO = "More info";
    private static final String AVAILABLE_VALUES = "Available Values";
    private static final String STATE = "State";
    private static final String VARIABLE = "Variable";
    private static final String AMOUNT = "Amount";
    private static final String AMOUNT_AVG = "Average of Amount";
    private static final String AMOUNT_MIN = "Min of Amount";
    private static final String AMOUNT_SUM = "Sum of Amount";
    private static final String METRIC_AVAILABLE = "Metric Available";
    private static final String AMOUNT_SUM_BY_STATE = "Amount Sum By State";
    private static final String DEFAULT_METRIC_FORMAT = "#,##0";
    private static final List<String> STATE_INPUTS = asList("New York", "North Carolina", "Texas", "Wisconsin");
    private static final List<String> STATE_MUF = asList("Arizona", "New York", "North Carolina");
    private static final String PRIVATE_METRIC = "Private metric";
    private static final String PRIVATE_EDITOR_METRIC = "Private editor metric";

    private String expressionAvailableMetric;
    private String dashboardName;
    private String filterName;
    private String filterType;
    private String filterUri;
    private DashboardRestRequest dashboardRequest;
    private String mufUri;
    private String assignedMufUserId;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        // use empty project
        projectTitle = "Metric Available Test";
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @BeforeClass(alwaysRun = true)
    public void addUsersOnDesktopExecution(ITestContext context) {
        filterType = context.getCurrentXmlTest().getParameter("filter");
    }

    @Override
    protected void customizeProject() throws Throwable {
        // upload payroll.csv
        uploadCSV(ResourceUtils.getFilePathFromResource("/payroll-csv/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll-file", getClass());

        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        // Create metrics
        String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));

        createMetric(AMOUNT_AVG, format("SELECT AVG([%s])", amountUri), DEFAULT_METRIC_FORMAT);
        createMetric(AMOUNT_MIN, format("SELECT MIN([%s])", amountUri), DEFAULT_METRIC_FORMAT);

        Metric amountSumMetric = createMetric(AMOUNT_SUM, format("SELECT SUM([%s])", amountUri), DEFAULT_METRIC_FORMAT);
        Attribute state = getMdService().getObj(getProject(), Attribute.class, title(STATE));
        List<String> attrEleOfStateUris = getAttributeElementUris(STATE, STATE_INPUTS);

        expressionAvailableMetric = format("SELECT [%s] WHERE [%s] IN ([%s], [%s], [%s], [%s])",
                amountSumMetric.getUri(), state.getUri(), attrEleOfStateUris.get(0), attrEleOfStateUris.get(1),
                attrEleOfStateUris.get(2), attrEleOfStateUris.get(3));

        createMetric(METRIC_AVAILABLE, expressionAvailableMetric, DEFAULT_METRIC_FORMAT);
        createPrivateMetric(getGoodDataClient(testParams.getEditorUser(), testParams.getPassword()),
                PRIVATE_EDITOR_METRIC);
        createPrivateMetric(getGoodDataClient(testParams.getUser(), testParams.getPassword()),
                PRIVATE_METRIC);

        String variableUri = new VariableRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createFilterVariable(VARIABLE, state.getDefaultDisplayForm().getUri());

        createReport(GridReportDefinitionContent.create(AMOUNT_SUM_BY_STATE,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(getAttributeByTitle(STATE))),
                singletonList(new MetricElement(amountSumMetric))));

        filterName = filterType == "prompt" ? VARIABLE : STATE;
        filterUri = filterType == "prompt" ? variableUri : state.getDefaultDisplayForm().getUri();

        // To setup Muf
        List<String> attrEleOfMufUris = getAttributeElementUris(STATE, STATE_MUF);
        final String expression = format("[%s] IN ([%s], [%s], [%s])",
                getAttributeByTitle(STATE).getUri(), attrEleOfMufUris.get(0), attrEleOfMufUris.get(1), attrEleOfMufUris.get(2));
        mufUri = dashboardRequest.createMufObjectByUri("muf", expression);
        assignedMufUserId = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId())
                .getUserProfileUri(testParams.getUserDomain(), testParams.getEditorUser());

        // There is first report to avoid case doesn't have any dashboard
        createDashboardHasReportAndFilter();

        // turn on the "useAvailableEnabled" feature flag
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.USE_AVAILABLE_ENABLED, true);
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void hasAvailableValuesTab() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        initDashboardsPage().selectDashboard(dashboard);
        List<String> tabNames = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName).getTabNames();

        assertTrue(tabNames.contains(AVAILABLE_VALUES), "Available value should be displayed");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifyAvailableValuesTabContents() throws IOException, JSONException {
        String availableValuesDescriptionsInput = "Select one or more metrics to limit filter values. " +
                "The filter dropdown will show only the attribute values for which at least one of the selected metrics " +
                "returns a relevant (non-null) value. More info";
        String dashboard = createDashboardHasFilter();
        initDashboardsPage().selectDashboard(dashboard);
        AvailableValuesConfigPanel availableValuesConfigPanel = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class);

        assertEquals(availableValuesConfigPanel.getAvailableValuesDescriptions(), availableValuesDescriptionsInput,
                "Available values descriptions is not correct!");
        assertEquals(availableValuesConfigPanel.getMoreInfoText(), MORE_INFO,
                "\"More info\" link didn't displayed!");
        assertTrue(availableValuesConfigPanel.isAddMetricButtonVisible(),
                "\"Add Metric\" button didn't displayed!");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifyMetricPickerDropDown() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        initDashboardsPage().selectDashboard(dashboard);
        SelectItemPopupPanel metricPickerDropDown = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                .openMetricPickerDropDown();

        assertEquals(metricPickerDropDown.getItems(),
                Arrays.asList(AMOUNT_AVG, METRIC_AVAILABLE, AMOUNT_MIN, PRIVATE_METRIC, AMOUNT_SUM),
                "All metrics should be displayed and sorted by alphabet!");
        assertTrue(metricPickerDropDown.isSearchInputVisible(), "The search input should be displayed!");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifyRestrictingAttributeValesByMetrics() throws IOException, JSONException {
        String dashboard = createDashboardHasReportAndFilter();
        initDashboardsPage().selectDashboard(dashboard);
        TableReport tableReport = dashboardsPage.getReport(AMOUNT_SUM_BY_STATE, TableReport.class);
        List<String> attributeElementsBeforeFilter = tableReport.getAttributeValues();

        WidgetConfigPanel widgetConfigPanel = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName);

        widgetConfigPanel
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                .selectMetric(METRIC_AVAILABLE);
        widgetConfigPanel.saveConfiguration();
        dashboardsPage.saveDashboard();
        Screenshots.takeScreenshot(browser, "verify-restricting-attribute-value-by-metrics", getClass());
        assertEquals(getFilter(filterName).getAllAttributeValues(), STATE_INPUTS,
                "List of state value should be limit by metric!");
        assertEquals(tableReport.getAttributeValues(), attributeElementsBeforeFilter,
                "Report should be rendered with all values of state");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifySelectedMetricIsHiddenFromMetricPicker() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        initDashboardsPage().selectDashboard(dashboard);
        List<String> availableMetricNames = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                .selectMetric(METRIC_AVAILABLE)
                .openMetricPickerDropDown()
                .getItems();
        assertFalse(availableMetricNames.contains(METRIC_AVAILABLE),
                "metric \"Metric Available\" shouldn't be shown in the metric picker dropdown");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifyAddedMetrics() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        initDashboardsPage().selectDashboard(dashboard);
        List<String> listAddedMetrics = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                .selectMetrics(asList(METRIC_AVAILABLE, AMOUNT_AVG, AMOUNT_MIN))
                .getSelectedMetrics();
        assertFalse(Ordering.natural().isOrdered(listAddedMetrics), "List metrics should be listed in order of added time");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifyHiddenMetrics() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        SelectItemPopupPanel selectItemPopupPanel = initDashboardsPage().selectDashboard(dashboard)
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                .openMetricPickerDropDown();
        assertFalse(selectItemPopupPanel.getItems().contains(PRIVATE_EDITOR_METRIC));
        assertTrue(selectItemPopupPanel.getHiddenItems().contains(PRIVATE_METRIC));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void verifyHiddenMetricsFromOtherUser() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        WidgetConfigPanel widgetConfigPanel = initDashboardsPage().selectDashboard(dashboard)
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName);
        widgetConfigPanel.getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class).selectMetric(PRIVATE_METRIC);
        widgetConfigPanel.saveConfiguration();
        dashboardsPage.saveDashboard();
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try{
            widgetConfigPanel = initDashboardsPage().selectDashboard(dashboard).editDashboard()
                    .getDashboardEditFilter().openWidgetConfigPanel(filterName);
            Screenshots.takeScreenshot(browser, "verify-hidden-metrics-from-ohter-user", getClass());
            assertEquals(widgetConfigPanel.getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                    .getSelectedMetrics(), singletonList(PRIVATE_METRIC));
            assertEquals(widgetConfigPanel.getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                    .getTooltipFromIHiddenMetric(PRIVATE_METRIC), "Hidden metric.");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void editMaqlMetric() throws IOException, JSONException {
        editMetricExpression(getMetricByTitle(METRIC_AVAILABLE), createUseAvailableMetricWithNullValue());
        try {
            String dashboard = createDashboardHasReportAndUseAvailableFilter(METRIC_AVAILABLE);
            initDashboardsPage().selectDashboard(dashboard);
            TableReport tableReport = dashboardsPage.getReport(AMOUNT_SUM_BY_STATE, TableReport.class).waitForLoaded();
            assertTrue(getFilter(filterName).getAllAttributeValues().isEmpty(),
                    "List of state value should be followed to updated metric");
            assertEquals(tableReport.getAttributeValues(), asList("Arizona", "Arkansas", "California", "Connecticut", "Florida",
                    "Iowa", "Massachusetts", "New York", "North Carolina", "Oklahoma", "Rhode Island", "South Carolina", "Texas"),
                    "Report should be rendered with all values of state");
        } finally {
            editMetricExpression(getMetricByTitle(METRIC_AVAILABLE), expressionAvailableMetric);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void addAllMetrics() throws IOException, JSONException {
        String dashboard = createDashboardHasFilter();
        initDashboardsPage().selectDashboard(dashboard);
        AvailableValuesConfigPanel availableValuesConfigPanel = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                .selectMetrics(asList(AMOUNT_AVG, AMOUNT_MIN, AMOUNT_SUM, METRIC_AVAILABLE, PRIVATE_METRIC));
        assertFalse(availableValuesConfigPanel.isAddMetricButtonEnabled(), "Add metric button should be disabled");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void deletedUsedMetricAvailableFilter() throws IOException, JSONException {
        String metric = "Metric";
        createMetric(metric, expressionAvailableMetric, DEFAULT_METRIC_FORMAT);
        String dashboard = createDashboardHasReportAndUseAvailableFilter(metric);
        initMetricPage().openMetricDetailPage(metric).deleteObject();
        initDashboardsPage().selectDashboard(dashboard);
        AvailableValuesConfigPanel availableValuesConfigPanel = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class);

        assertTrue(availableValuesConfigPanel.getSelectedMetrics().contains(metric),
                "deleted metric should be displayed");
        assertEquals(availableValuesConfigPanel.getTooltipFromSelectedMetric(metric),
                "This metric has been deleted.");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void connectFilterInDashboardTest() throws IOException, JSONException {
        String dashboard = createDashboardHasReportAndUseAvailableFilter(METRIC_AVAILABLE);
        initDashboardsPage()
                .selectDashboard(dashboard)
                .addNewTab("Test Tab")
                .addAttributeFilterToDashboard(
                        filterType == "prompt" ? DashAttributeFilterTypes.PROMPT : DashAttributeFilterTypes.ATTRIBUTE, filterName);
        AvailableValuesConfigPanel availableValuesConfigPanel = dashboardsPage
                .getDashboardEditBar()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName)
                .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class);
        assertTrue(availableValuesConfigPanel.getSelectedMetrics().contains(METRIC_AVAILABLE));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void changeToOneValueFilterTest() throws IOException, JSONException {
        String dashboard = createDashboardHasReportAndUseAvailableFilter(METRIC_AVAILABLE);
        initDashboardsPage().selectDashboard(dashboard);

        WidgetConfigPanel widgetConfigPanel = dashboardsPage
                .editDashboard()
                .getDashboardEditFilter()
                .openWidgetConfigPanel(filterName);
        widgetConfigPanel
                .getTab(Tab.SELECTION, SelectionConfigPanel.class)
                .changeSelectionToOneValue();
        widgetConfigPanel.saveConfiguration();

        dashboardsPage.saveDashboard();

        TableReport tableReport = dashboardsPage.getReport(AMOUNT_SUM_BY_STATE, TableReport.class).waitForLoaded();
        List<String> attributeValues = tableReport.getAttributeValues();

        assertEquals(getFilter(filterName).getCurrentValue(), "New York");
        assertEquals(attributeValues, singletonList("New York"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"attribute", "prompt"})
    public void useAvailableWithMUF() throws IOException {
        String dashboard = createDashboardHasReportAndUseAvailableFilter(METRIC_AVAILABLE);
        dashboardRequest.addMufToUser(assignedMufUserId, mufUri);
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            initDashboardsPage().selectDashboard(dashboard);
            TableReport tableReport = dashboardsPage.getReport(AMOUNT_SUM_BY_STATE, TableReport.class).waitForLoaded();
            Screenshots.takeScreenshot(browser, "use-available-with-muf", getClass());
            assertEquals(tableReport.getAttributeValues(), STATE_MUF);
            assertEquals(getFilter(filterName).getAllAttributeValues(), asList("New York", "North Carolina"));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private String createPrivateMetric(final GoodData goodData, final String name) {
        final MetadataService mdService = goodData.getMetadataService();
        final Metric privateMetric = createMetric(goodData, name, "SELECT 1", "#,##0");

        privateMetric.setUnlisted(true);
        mdService.updateObj(privateMetric);
        return name;
    }

    private void editMetricExpression(Metric metric, String expression) throws IOException, JSONException {
        JSONObject json = getJsonObject(getRestApiClient(), metric.getUri());
        json.getJSONObject("metric").getJSONObject("content").put("expression", expression);
        executeRequest(getRestApiClient(),
                getRestApiClient().newPostMethod(metric.getUri() + "?mode=edit", json.toString()),
                HttpStatus.OK);
    }

    private String createDashboardHasReportAndUseAvailableFilter(String metric) throws JSONException, IOException {
        dashboardName = generateDashboardName();
        FilterItemContent filter = createMultipleValuesFilterBy(filterUri);
        dashboardRequest.createDashboard(createDashboardContentWithFilterAndReport(filter).getMdObject());
        dashboardRequest.addUseAvailableMetricToDashboardFilters(dashboardName, metric);
        return dashboardName;
    }

    private String createDashboardHasReportAndFilter() throws JSONException, IOException {
        dashboardName = generateDashboardName();
        FilterItemContent filter = createMultipleValuesFilterBy(filterUri);
        Dashboard dashboard = createDashboardContentWithFilterAndReport(filter);
        dashboardRequest.createDashboard(dashboard.getMdObject());
        return dashboardName;
    }

    private String createDashboardHasFilter() throws JSONException, IOException {
        dashboardName = generateDashboardName();
        FilterItemContent filter = createMultipleValuesFilterBy(filterUri);
        Dashboard dashboard = createDashboardContentWithFilter(filter);
        dashboardRequest.createDashboard(dashboard.getMdObject());
        return dashboardName;
    }

    private Dashboard createDashboardContentWithFilterAndReport(FilterItemContent filter) {
        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(dashboardName);
            dash.addTab(Builder.of(com.gooddata.qa.mdObjects.dashboard.tab.Tab::new)
                    .with(tab -> {
                        FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                            item.setContentId(filter.getId());
                            item.setPosition(TabItem.ItemPosition.RIGHT);
                        }).build();

                        tab.addItem(Builder.of(ReportItem::new).with(reportItem -> {
                            reportItem.setObjUri(getReportByTitle(AMOUNT_SUM_BY_STATE).getUri());
                            reportItem.setPosition(TabItem.ItemPosition.LEFT);
                            reportItem.setAppliedFilterIds(singletonList(filterItem.getId()));
                        }).build());
                        tab.addItem(filterItem);
                    })
                    .build());
            dash.addFilter(filter);
        }).build();
    }

    private Dashboard createDashboardContentWithFilter(FilterItemContent filter) {
        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(dashboardName);
            dash.addTab(Builder.of(com.gooddata.qa.mdObjects.dashboard.tab.Tab::new)
                    .with(tab -> {
                        FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                            item.setContentId(filter.getId());
                            item.setPosition(TabItem.ItemPosition.RIGHT);
                        }).build();

                        tab.addItem(filterItem);
                    })
                    .build());
            dash.addFilter(filter);
        }).build();
    }

    private String createUseAvailableMetricWithNullValue() {
        Attribute state = getAttributeByTitle(STATE);
        List<String> attrEleUris = getMdService().getAttributeElements(state).stream()
                .map(AttributeElement::getUri)
                .collect(Collectors.toList());
        return format("SELECT [%s] WHERE [%s] NOT IN ([%s], [%s], [%s], [%s], [%s], [%s], [%s]," +
                " [%s], [%s], [%s], [%s], [%s], [%s], [%s], [%s])", getMetricByTitle(AMOUNT_SUM).getUri(), state.getUri(),
                attrEleUris.get(0), attrEleUris.get(1), attrEleUris.get(2), attrEleUris.get(3), attrEleUris.get(4),
                attrEleUris.get(5), attrEleUris.get(6), attrEleUris.get(7), attrEleUris.get(8), attrEleUris.get(9),
                attrEleUris.get(10), attrEleUris.get(11), attrEleUris.get(12), attrEleUris.get(13), attrEleUris.get(14));
    }
}
