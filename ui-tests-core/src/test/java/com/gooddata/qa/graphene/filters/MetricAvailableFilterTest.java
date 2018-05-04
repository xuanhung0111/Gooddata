package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.AvailableValuesConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.RestUtils.deleteObjectsUsingCascade;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MetricAvailableFilterTest extends AbstractDashboardWidgetTest {

    private static final String MORE_INFO = "More info";
    private static final String AVAILABLE_VALUES = "Available Values";
    private static final String PARENT_FILTERS = "Parent Filters";
    private static final String ARRANGE = "Arrange";
    private static final String STATE = "State";
    private static final String AMOUNT = "Amount";
    private static final String AMOUNT_AVG = "Average of Amount";
    private static final String AMOUNT_MIN = "Min of Amount";
    private static final String AMOUNT_SUM = "Sum of Amount";
    private static final String METRIC_AVAILABLE = "Metric Available";
    private static final String AMOUNT_SUM_BY_STATE = "Amount Sum By State";
    private static final String DEFAULT_METRIC_FORMAT = "#,##0";
    private static final List<String> STATE_INPUTS = Arrays.asList("New York", "North Carolina", "Texas", "Wisconsin");
    private static final int NEGATIVE_INDEX = -1;

    private String dashboardUri;
    private String availableValuesDescriptionsInput = "Select one or more metrics to limit filter values. " +
            "The filter dropdown will show only the attribute values for which at least one of the selected metrics " +
            "returns a relevant (non-null) value. More info";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        // use empty project
        projectTitle = "Metric Available Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        // upload payroll.csv
        uploadCSV(ResourceUtils.getFilePathFromResource("/payroll-csv/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll-file", getClass());

        // Create metrics
        String amountUri = getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));

        createMetric(AMOUNT_AVG, format("SELECT AVG([%s])", amountUri), DEFAULT_METRIC_FORMAT);
        createMetric(AMOUNT_MIN, format("SELECT MIN([%s])", amountUri), DEFAULT_METRIC_FORMAT);

        Metric amountSumMetric = createMetric(AMOUNT_SUM, format("SELECT SUM([%s])", amountUri), DEFAULT_METRIC_FORMAT);
        Attribute state = getMdService().getObj(getProject(), Attribute.class, title(STATE));
        List<String> attrEleUris = getMdService().getAttributeElements(state).stream()
                .filter(attrEle -> STATE_INPUTS.contains(attrEle.getTitle()))
                .map(AttributeElement::getUri)
                .collect(Collectors.toList());

        createMetric(METRIC_AVAILABLE, format("SELECT [%s] WHERE [%s] IN ([%s], [%s], [%s], [%s])",
                amountSumMetric.getUri(), state.getUri(), attrEleUris.get(0), attrEleUris.get(1), attrEleUris.get(2),
                attrEleUris.get(3)), DEFAULT_METRIC_FORMAT);

        // turn on the "useAvailableEnabled" feature flag
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.USE_AVAILABLE_ENABLED, true);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void hasAvailableValuesTab() throws IOException, JSONException {
        try {
            addAndEditNewDashboard();
            List<String> tabNames = addStateAttributeAndOpenConfigPanel().getTabNames();

            int availableTabIndex = tabNames.indexOf(AVAILABLE_VALUES),
                parentFilterTabIndex = tabNames.indexOf(PARENT_FILTERS),
                arrangeTabIndex = tabNames.indexOf(ARRANGE);

            assertTrue(availableTabIndex != NEGATIVE_INDEX, "\"Available Values\" tab didn't displayed!");
            assertTrue(parentFilterTabIndex != NEGATIVE_INDEX, "\"Parent filters\" tab didn't displayed!");
            assertTrue(arrangeTabIndex != NEGATIVE_INDEX, "\"Arrange\" tab didn't displayed!");
            assertTrue(parentFilterTabIndex < availableTabIndex,
                    "\"Available Values\" tab should be on right side of \"Parent Filter\" tab!");
            assertTrue(availableTabIndex < arrangeTabIndex,
                    "\"Available values\" tab should be on left side of \"Arrange\" tab!");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyAvailableValuesTabContents() throws IOException, JSONException {
        try {
            addAndEditNewDashboard();
            AvailableValuesConfigPanel availableValuesConfigPanel = addStateAttributeAndOpenConfigPanel()
                    .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class);

            assertEquals(availableValuesConfigPanel.getAvailableValuesDescriptions(), availableValuesDescriptionsInput,
                         "Available values descriptions is not correct!");
            assertEquals(availableValuesConfigPanel.getMoreInfoText(), MORE_INFO,
                         "\"More info\" link didn't displayed!");
            assertTrue(availableValuesConfigPanel.isAddMetricButtonVisible(),
                       "\"Add Metric\" button didn't displayed!");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyMetricPickerDropDown() throws IOException, JSONException {
        try {
            addAndEditNewDashboard();
            SelectItemPopupPanel metricPickerDropDown = addStateAttributeAndOpenConfigPanel()
                    .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                    .openMetricPickerDropDown();

            assertEquals(metricPickerDropDown.getItems(),
                         Arrays.asList(AMOUNT_AVG, METRIC_AVAILABLE, AMOUNT_MIN, AMOUNT_SUM),
                         "All metrics should be displayed and sorted by alphabet!");
            assertTrue(metricPickerDropDown.isSearchInputVisible(), "The search input should be displayed!");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifyRestrictingAtributeValesByMetrics() throws IOException, JSONException {
        // Create report
        createReport(new UiReportDefinition()
                .withName(AMOUNT_SUM_BY_STATE)
                .withWhats(AMOUNT_SUM)
                .withHows(STATE),
                "Amount Sum By State Report"
        );

        try {
            addAndEditNewDashboard().addReportToDashboard(AMOUNT_SUM_BY_STATE);

            TableReport report = getReport(AMOUNT_SUM_BY_STATE);
            DashboardWidgetDirection.DOWN.moveElementToRightPlace(report.getRoot());
            List<String> attributeElementsBeforeFilter = report.getAttributeValues();

            WidgetConfigPanel widgetConfigPanel = addStateAttributeAndOpenConfigPanel();

            widgetConfigPanel
                    .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                    .selectMetric(METRIC_AVAILABLE);
            widgetConfigPanel.saveConfiguration();
            dashboardsPage.getDashboardEditBar().saveDashboard();
            assertEquals(getFilter(STATE).getAllAttributeValues(), STATE_INPUTS,
                         "List of state value should be limit by metric!");
            assertEquals(report.getAttributeValues(), attributeElementsBeforeFilter,
                         "Report should be rendered with all values of state");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void verifySelectedMetricIsHiddenFromMetricPicker() throws IOException, JSONException {
        try {
            addAndEditNewDashboard();
            List<String> availableMetricNames = addStateAttributeAndOpenConfigPanel()
                    .getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class)
                    .selectMetric(METRIC_AVAILABLE)
                    .openMetricPickerDropDown()
                    .getItems();
            assertFalse(availableMetricNames.contains(METRIC_AVAILABLE),
                        "metric \"Metric Available\" shouldn't be shown in the metric picker dropdown");
        } finally {
            deleteObjectsUsingCascade(getRestApiClient(), testParams.getProjectId(), dashboardUri);
        }
    }

    private WidgetConfigPanel addStateAttributeAndOpenConfigPanel() {
        // Require that dashboard edit bar is visible before
        return dashboardsPage.getDashboardEditBar()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, STATE)
                .getDashboardEditFilter()
                .openWidgetConfigPanel(STATE);
    }

    private DashboardEditBar addAndEditNewDashboard() {
        initDashboardsPage()
                .addNewDashboard("Use available test")
                .editDashboard();
        dashboardUri = browser.getCurrentUrl().split("\\|")[2];
        return dashboardsPage.getDashboardEditBar();
    }
}
