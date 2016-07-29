package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.addMufToUser;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createSimpleMufObjByUri;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardContent;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.google.common.base.Function;

public class GoodSalesFilterDropdownAttributeValueTest extends GoodSalesAbstractTest {

    private static final String SHORT_LIST_ID = "1251";
    private static final String RISK_ASSESSMENT_ID = "966645";
    private static final String CONVICTION_ID = "966646";
    private static final String INTEREST_ID = "966643";
    private static final String DISCOVERY_ID = "966644";
    private static final String NEGOTIATION_ID = "966647";
    private static final String CLOSED_WON_ID = "966648";
    private static final String CLOSED_LOST_ID = "966649";

    private static final String METRIC_AVAILABLE = "MetricAvailable";
    private static final String REPORT_1 = "Report1";
    private static final String REPORT_2 = "Report2";
    private static final String REPORT_3 = "Report3";

    private static final String F_STAGE_NAME = "FStageName";

    private static final String USE_AVAILABLE_DASHBOARD_1 = "UseAvailable1";
    private static final String USE_AVAILABLE_DASHBOARD_2 = "UseAvailable2";
    private static final String USE_AVAILABLE_DASHBOARD = "UseAvailable";

    private Metric metricAvailable;
    private Attribute stageName;
    private String amountMetricUri;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-filter-dropdown-attribute-value";
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public void initialization() {
        amountMetricUri = getMdService().getObjUri(getProject(), Metric.class, identifier("ah1EuQxwaCqs"));
        stageName = getMdService().getObj(getProject(), Attribute.class, identifier("attr.stage.name"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public void createVariable() {
        initVariablePage();

        variablePage.createVariable(new AttributeVariable(F_STAGE_NAME).withAttribute(ATTR_STAGE_NAME)
                .withAttributeElements("Discovery", "Risk Assessment"));
    }

    @Test(dependsOnMethods = {"initialization", "createVariable"}, groups = {"init"})
    public void prepareMetricAndReports() throws IOException, JSONException {
        // *** create metric available ***
        metricAvailable = getMdService().createObj(getProject(), new Metric(METRIC_AVAILABLE,
                buildFirstMetricExpression(amountMetricUri, stageName.getUri()), "#,##0.00"));

        // *** create report 1 ***
        String yearSnapshotUri = getMdService().getObj(getProject(), Attribute.class, identifier("snapshot.year"))
                .getDefaultDisplayForm().getUri();
        ReportDefinition definition = GridReportDefinitionContent.create(REPORT_1, singletonList("metricGroup"),
                asList(new AttributeInGrid(stageName.getDefaultDisplayForm().getUri()),
                        new AttributeInGrid(yearSnapshotUri)), singletonList(new GridElement(amountMetricUri,
                                METRIC_AMOUNT)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        // *** create report 2 ***
        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName(REPORT_2).withWhats(METRIC_AMOUNT)
                .withHows(ATTR_STAGE_NAME).withHows(ATTR_YEAR_SNAPSHOT);
        createReport(rd, REPORT_2);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter(F_STAGE_NAME, "Discovery", "2010", "2011",
                "2012", "Risk Assessment", "2010", "2011", "2012"));
        reportPage.saveReport();
    }

    @Test(dependsOnMethods = {"prepareMetricAndReports"}, groups = {"init"})
    public void createUseAvailableDashboardWithOneReport() throws IOException, JSONException {
        initDashboardsPage();

        dashboardsPage.addNewDashboard(USE_AVAILABLE_DASHBOARD_1);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(REPORT_1);
        WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(report);

        dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
        WebElement filter = dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getRoot();
        filter.click();
        DashboardWidgetDirection.UP.moveElementToRightPlace(filter);
        dashboardEditBar.saveDashboard();

        configureUseAvailableToDashboard(getCurrentDashboardUri());
    }

    @Test(dependsOnMethods = {"createUseAvailableDashboardWithOneReport"}, groups = {"init"})
    public void createUseAvailableDashboardWithTwoReports() throws IOException, JSONException {
        initDashboardsPage();

        dashboardsPage.selectDashboard(USE_AVAILABLE_DASHBOARD_1);
        dashboardsPage.saveAsDashboard(USE_AVAILABLE_DASHBOARD_2, PermissionType.USE_EXISTING_PERMISSIONS);

        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addReportToDashboard(REPORT_2);
        WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(report);

        dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, F_STAGE_NAME);
        WebElement filter = dashboardsPage.getContent().getFilterWidget(simplifyText(F_STAGE_NAME)).getRoot();
        filter.click();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(filter);
        dashboardEditBar.saveDashboard();

        configureUseAvailableToDashboard(getCurrentDashboardUri());
    }

    @Test(dependsOnGroups = {"init"})
    public void addUseAvailableForAttributeFilter() throws IOException, JSONException {
        makeCopyFromDashboard(USE_AVAILABLE_DASHBOARD_1);

        try {
            List<String> attributeValues = asList("Short List", "Risk Assessment", "Conviction");

            FilterWidget filter = dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            AttributeFilterPanel filterPanel = filter.openPanel().getPanel(AttributeFilterPanel.class);
            assertTrue(isEqualCollection(filterPanel.getAllAtributeValues(), attributeValues));

            filter.changeAttributeFilterValue("Conviction");
            sleepTightInSeconds(2);
            assertTrue(isEqualCollection(singleton("Conviction"), getAttributeValuesInFirstRow(REPORT_1)),
                    "Report1 doesnt apply StageName filter correctly!");

            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Account");
            dashboardEditBar.saveDashboard();

            assertTrue(isUseAvailableStillRemainInDashboard(getCurrentDashboardUri()));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void addUseAvailableForPromptFilter() throws IOException, JSONException {
        makeCopyFromDashboard(USE_AVAILABLE_DASHBOARD_2);

        try {
            FilterWidget filter = dashboardsPage.getContent().getFilterWidget(simplifyText(F_STAGE_NAME));
            AttributeFilterPanel filterPanel = filter.openPanel().getPanel(AttributeFilterPanel.class);
            assertTrue(isEqualCollection(filterPanel.getAllAtributeValues(), singleton("Risk Assessment")));
            assertTrue(isEqualCollection(asList("Discovery", "Risk Assessment"),
                    getAttributeValuesInFirstRow(REPORT_2)));

            dashboardsPage
                    .editDashboard()
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Account")
                    .saveDashboard();

            assertTrue(isUseAvailableStillRemainInDashboard(getCurrentDashboardUri()),
                    "UseAvailable is removed from dashboard!");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void connectFilterWithUseAvailable() {
        // *** create report 3 ***
        ReportDefinition definition = GridReportDefinitionContent.create(REPORT_3, singletonList("metricGroup"),
                asList(new AttributeInGrid(stageName.getDefaultDisplayForm().getUri())),
                singletonList(new GridElement(amountMetricUri, "Amount")));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        makeCopyFromDashboard(USE_AVAILABLE_DASHBOARD_1);
        try {
            dashboardsPage.addNewTab("new_tab");

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.addReportToDashboard(REPORT_3);
            WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
            DashboardWidgetDirection.LEFT.moveElementToRightPlace(report);

            dashboardEditBar.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STAGE_NAME);
            WebElement filter = dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME)).getRoot();
            filter.click();
            DashboardWidgetDirection.UP.moveElementToRightPlace(filter);
            dashboardEditBar.saveDashboard();

            FilterWidget filterWidget = dashboardsPage.getContent().getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            AttributeFilterPanel filterPanel = filterWidget.openPanel().getPanel(AttributeFilterPanel.class);
            assertTrue(isEqualCollection(filterPanel.getAllAtributeValues(),
                    asList("Short List", "Risk Assessment", "Conviction")),
                    "Attribute values of StageName filter is not correct!");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void useAvailableWithSingleOptionFilter() {
        makeCopyFromDashboard(USE_AVAILABLE_DASHBOARD_2);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            DashboardContent dashboardContent = dashboardsPage.getContent();

            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            stageNameFilter.changeSelectionToOneValue();

            FilterWidget fStageNameFilter = dashboardContent.getFilterWidget(simplifyText(F_STAGE_NAME));
            fStageNameFilter.changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();
            sleepTightInSeconds(2);

            assertEquals(stageNameFilter.getCurrentValue(), "Short List",
                    "Current value of StageName filter is not correct!");
            assertEquals(fStageNameFilter.getCurrentValue(), "Risk Assessment",
                    "Current value of FStageName filter is not correct!");

            assertTrue(isEqualCollection(getAttributeValuesInFirstRow(REPORT_1), singleton("Short List")),
                    "Report1 doesnt apply StageName filter correctly!");
            assertTrue(dashboardContent.getReport(REPORT_2, TableReport.class).isNoData(),
                    "Report2 still has data");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void filterOutUseAvailableEmptyFilter() throws IOException, JSONException {
        makeCopyFromDashboard(USE_AVAILABLE_DASHBOARD_2);
        String stageNameUri = stageName.getUri();
        editMetricExpression(buildSecondMetricExpression(amountMetricUri, stageNameUri));

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            DashboardContent dashboardContent = dashboardsPage.getContent();

            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            stageNameFilter.changeSelectionToOneValue();

            FilterWidget fStageNameFilter = dashboardContent.getFilterWidget(simplifyText(F_STAGE_NAME));
            fStageNameFilter.changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            assertEquals(stageNameFilter.getCurrentValue(), "All",
                    "Current value of StageName filter is not correct!");
            assertEquals(fStageNameFilter.getCurrentValue(), "All",
                    "Current value of FStageName filter is not correct!");

            assertTrue(stageNameFilter.openPanel().getPanel(AttributeFilterPanel.class).getAllAtributeValues()
                    .isEmpty(), "StageName filter still has value!");
            stageNameFilter.getRoot().click();
            assertTrue(fStageNameFilter.openPanel().getPanel(AttributeFilterPanel.class).getAllAtributeValues()
                    .isEmpty(), "FStageName filter still has value!");
            fStageNameFilter.getRoot().click();
        } finally {
            editMetricExpression(buildFirstMetricExpression(amountMetricUri, stageNameUri));
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"}, priority = 1)
    public void combineMufAndUseAvailable() throws IOException, JSONException {
        String stageNameUri = stageName.getUri();

        Map<String, Collection<String>> conditions = new HashMap<String, Collection<String>>();
        conditions.put(stageNameUri, buildStageElementUris());

        String mufUri = createSimpleMufObjByUri(getRestApiClient(),
                getProject().getId(), "Stage Name user filter", conditions);
        addMufToUser(getRestApiClient(), getProject().getId(), testParams.getUser(), mufUri);

        makeCopyFromDashboard(USE_AVAILABLE_DASHBOARD_2);

        try {
            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            assertTrue(isEqualCollection(asList("Risk Assessment", "Conviction"), stageNameFilter.openPanel()
                    .getPanel(AttributeFilterPanel.class).getAllAtributeValues()),
                    "Attribute values of StageName filter are not correct!");
            FilterWidget fStageNameFilter = dashboardContent.getFilterWidget(simplifyText(F_STAGE_NAME));
            assertTrue(isEqualCollection(singleton("Risk Assessment"),
                    fStageNameFilter.openPanel().getPanel(AttributeFilterPanel.class).getAllAtributeValues()),
                    "Attribute values of FStageName filter are not correct!");

            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            stageNameFilter.changeSelectionToOneValue();
            fStageNameFilter.changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            assertEquals(stageNameFilter.getCurrentValue(), "Risk Assessment",
                    "Current value of StageName filter is not correct!");
            assertEquals(fStageNameFilter.getCurrentValue(), "Risk Assessment",
                    "Current value of StageName filter is not correct!");

            editMetricExpression(buildThridMetricExpression(amountMetricUri, stageNameUri));
        } finally {
            dashboardsPage.deleteDashboard();
            editMetricExpression(buildFirstMetricExpression(amountMetricUri, stageNameUri));
            addMufToUser(getRestApiClient(), getProject().getId(), testParams.getUser(), "");
        }
    }

    private void makeCopyFromDashboard(String dashboardName) {
        initDashboardsPage();

        dashboardsPage.selectDashboard(dashboardName);
        dashboardsPage.saveAsDashboard(USE_AVAILABLE_DASHBOARD, PermissionType.USE_EXISTING_PERMISSIONS);
        dashboardsPage.selectDashboard(USE_AVAILABLE_DASHBOARD);
    }

    private String getCurrentDashboardUri() {
        for (String part : browser.getCurrentUrl().split("\\|")) {
            if (part.startsWith("/gdc/md")) {
                return part;
            }
        }
        throw new IllegalStateException("Cannot find current dashboard uri");
    }

    private Collection<String> getAttributeValuesInFirstRow(String reportName) {
        List<List<String>> attributesByRow =
                dashboardsPage.getContent().getReport(reportName, TableReport.class).getAttributeElementsByRow();
        return newHashSet(transform(attributesByRow, new Function<List<String>, String>() {
            @Override
            public String apply(List<String> input) {
                return input.get(0);
            }
        }));
    }

    private void configureUseAvailableToDashboard(String dashboardUri) throws IOException, JSONException {
        JSONObject json = getJsonObject(getRestApiClient(), dashboardUri);
        json = addUseAvailableContentToJson(json);
        executeRequest(getRestApiClient(),
                getRestApiClient().newPostMethod(dashboardUri + "?mode=edit", json.toString()),
                HttpStatus.OK);
    }

    private String buildFirstMetricExpression(String amountUri, String stageNameUri) {
        String expressionTemplate = "SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s])";
        return format(expressionTemplate, amountUri, stageNameUri,
                buildAttributeElementUri(stageNameUri, SHORT_LIST_ID),
                buildAttributeElementUri(stageNameUri, RISK_ASSESSMENT_ID),
                buildAttributeElementUri(stageNameUri, CONVICTION_ID));
    }

    private String buildSecondMetricExpression(String amountUri, String stageNameUri) {
        String expressionTemplate = "SELECT SUM([%s]) WHERE [%s] NOT IN ([%s],[%s],[%s],[%s],[%s],[%s],[%s],[%s])";
        return format(expressionTemplate, amountUri, stageNameUri,
                buildAttributeElementUri(stageNameUri, SHORT_LIST_ID),
                buildAttributeElementUri(stageNameUri, RISK_ASSESSMENT_ID),
                buildAttributeElementUri(stageNameUri, CONVICTION_ID),
                buildAttributeElementUri(stageNameUri, INTEREST_ID),
                buildAttributeElementUri(stageNameUri, DISCOVERY_ID),
                buildAttributeElementUri(stageNameUri, NEGOTIATION_ID),
                buildAttributeElementUri(stageNameUri, CLOSED_WON_ID),
                buildAttributeElementUri(stageNameUri, CLOSED_LOST_ID));
    }

    private String buildThridMetricExpression(String amountUri, String stageNameUri) {
        String expressionTemplate = "SELECT SUM([%s]) WHERE [%s] IN ([%s],[%s],[%s],[%s],[%s])";
        return format(expressionTemplate, amountUri, stageNameUri,
                buildAttributeElementUri(stageNameUri, INTEREST_ID),
                buildAttributeElementUri(stageNameUri, DISCOVERY_ID),
                buildAttributeElementUri(stageNameUri, NEGOTIATION_ID),
                buildAttributeElementUri(stageNameUri, CLOSED_WON_ID),
                buildAttributeElementUri(stageNameUri, CLOSED_LOST_ID));
    }

    private void editMetricExpression(String expression) throws IOException, JSONException {
        JSONObject json = getJsonObject(getRestApiClient(), metricAvailable.getUri());
        json.getJSONObject("metric").getJSONObject("content").put("expression", expression);
        executeRequest(getRestApiClient(),
                getRestApiClient().newPostMethod(metricAvailable.getUri() + "?mode=edit", json.toString()),
                HttpStatus.OK);
    }

    private JSONObject addUseAvailableContentToJson(JSONObject json) throws JSONException {
        JSONArray filters = json.getJSONObject("projectDashboard").getJSONObject("content")
                .getJSONArray("filters");
        JSONObject currentObj;

        for (int i = 0, n = filters.length(); i < n; i++) {
            currentObj = filters.getJSONObject(i).getJSONObject("filterItemContent");
            if (currentObj.has("useAvailable")) {
                continue;
            }
            currentObj.put("useAvailable", new JSONObject() {{
                put("metrics", new JSONArray().put(metricAvailable.getUri()));
            }});
        }

        return json;
    }

    private boolean isUseAvailableStillRemainInDashboard(String dashboardUri) throws IOException, JSONException {
        JSONObject json = getJsonObject(getRestApiClient(), dashboardUri);
        JSONArray filters = json.getJSONObject("projectDashboard").getJSONObject("content")
                .getJSONArray("filters");
        JSONObject currentObj;
        boolean found = false;
        boolean ok = true;

        for (int i = 0, n = filters.length(); i < n; i++) {
            currentObj = filters.getJSONObject(i).getJSONObject("filterItemContent");
            if (!currentObj.has("useAvailable")) {
                continue;
            }

            found = true;
            ok &= metricAvailable.getUri().equals(currentObj.getJSONObject("useAvailable")
                    .getJSONArray("metrics").getString(0));
        }
        return found && ok;
    }

    private String buildAttributeElementUri(String attributeUri, String elementId) {
        return attributeUri + "/elements?id=" + elementId;
    }

    private Collection<String> buildStageElementUris() {
        final String stageUri = stageName.getUri();

        return Arrays.asList(buildAttributeElementUri(stageUri, RISK_ASSESSMENT_ID),
                buildAttributeElementUri(stageUri, CONVICTION_ID),
                buildAttributeElementUri(stageUri, NEGOTIATION_ID));
    }
}
