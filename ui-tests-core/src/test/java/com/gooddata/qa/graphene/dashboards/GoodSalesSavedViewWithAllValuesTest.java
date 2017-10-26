package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRIORITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;

public class GoodSalesSavedViewWithAllValuesTest extends AbstractDashboardWidgetTest {
    private final static String TEST_DASHBOARD = "Dashboard-Having-Report-And-Attribute-Filters";
    private final static String REPORT = "Report-Having-Two-Attributes";
    private final static String COMPLETED = "Completed";
    private final static String DEFERRED = "Deferred";
    private final static String IN_PROGRESS = "In Progress";
    private final static String LOW = "LOW";
    private final static String NORMAL = "NORMAL";
    private final static String HIGH = "HIGH";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "GoodSales-Attribute-Filter-On-View-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        createAmountMetric();
        createNumberOfActivitiesMetric();
        List<Attribute> attributes = Stream.of("attr.activity.status", "attr.activity.priority")
                .map(e -> getMdService().getObj(getProject(), Attribute.class, Restriction.identifier(e)))
                .collect(Collectors.toList());

        createReport(REPORT, attributes, singletonList(getMetricByTitle(METRIC_NUMBER_OF_ACTIVITIES)));

        initDashboardsPage().addNewDashboard(TEST_DASHBOARD).editDashboard()
                .addReportToDashboard(REPORT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_STATUS)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRIORITY);

        getFilter(ATTR_PRIORITY).editAttributeFilterValues(LOW);
        getFilter(ATTR_STATUS).editAttributeFilterValues(COMPLETED, DEFERRED);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_STATUS).getRoot());
        DashboardWidgetDirection.MIDDLE.moveElementToRightPlace(getFilter(ATTR_PRIORITY).getRoot());

        dashboardsPage.turnSavedViewOption(true).saveDashboard().publishDashboard(true);
        takeScreenshot(browser, "test-dashboard", getClass());
    }

    @Test(dependsOnGroups = "createProject")
    public void testSavedViewAfterChangingFilterToAllValues() throws JSONException {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            initDashboardsPage().selectDashboard(TEST_DASHBOARD);
            getFilter(ATTR_PRIORITY).openPanel().getAttributeFilterPanel().selectAllItems().submitPanel();
            getFilter(ATTR_STATUS).changeAttributeFilterValues(COMPLETED, DEFERRED, IN_PROGRESS);

            SavedViewWidget.DashboardSaveActiveViewDialog dialog = dashboardsPage.getSavedViewWidget()
                    .openSavedViewMenu().openSaveActiveViewDialog();
            assertEquals(dialog.getFilters().stream().map(WebElement::getText).collect(Collectors.toList()),
                    asList(ATTR_STATUS, ATTR_PRIORITY), ATTR_PRIORITY + " and " + ATTR_STATUS + " are not displayed");

            dialog.saveCurrentView("View after changing filter to all values", true);
            assertEquals(getReport(REPORT).getAttributeElements(), asList(COMPLETED, LOW, NORMAL, DEFERRED,
                    HIGH, IN_PROGRESS, NORMAL), "List of attributes is not correct");
            assertEquals(getReport(REPORT).getMetricElements(), asList(30510.0f, 31020.0f, 31109.0f, 31116.0f));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testSavedViewHavingNoChangeOnAllValuesFilter() throws JSONException {
        String dashboardHavingAllValuesFilter = "Dashboard having all values attribute filter";
        // clone existing dashboard
        initDashboardsPage().selectDashboard(TEST_DASHBOARD).saveAsDashboard(dashboardHavingAllValuesFilter,
                SaveAsDialog.PermissionType.USE_EXISTING_PERMISSIONS);

        dashboardsPage.editDashboard();
        getFilter(ATTR_PRIORITY).openEditPanel().getAttributeFilterPanel().selectAllItems().submitPanel();
        dashboardsPage.saveDashboard();

        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            initDashboardsPage().selectDashboard(dashboardHavingAllValuesFilter);
            getFilter(ATTR_STATUS).changeAttributeFilterValues(COMPLETED, DEFERRED, IN_PROGRESS);

            SavedViewWidget.DashboardSaveActiveViewDialog dialog = dashboardsPage.getSavedViewWidget()
                    .openSavedViewMenu().openSaveActiveViewDialog();
            assertEquals(dialog.getFilters().stream().map(WebElement::getText).collect(Collectors.toList()),
                    singletonList(ATTR_STATUS), ATTR_STATUS + "is not displayed");

            dialog.saveCurrentView("View having no change on all values filter", true);
            assertEquals(getReport(REPORT).getAttributeElements(), asList(COMPLETED, LOW, NORMAL, DEFERRED,
                    HIGH, IN_PROGRESS, NORMAL), "List of attributes is not correct");
            assertEquals(getReport(REPORT).getMetricElements(), asList(30510.0f, 31020.0f, 31109.0f, 31116.0f));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private void createReport(String reportName, List<Attribute> attributes, List<Metric> metrics) {
        ReportDefinition definition = GridReportDefinitionContent.create(reportName, singletonList(METRIC_GROUP),
                attributes.stream().map(AttributeInGrid::new).collect(Collectors.toList()), metrics.stream().map
                        (MetricElement::new).collect(Collectors.toList()));

        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }
}