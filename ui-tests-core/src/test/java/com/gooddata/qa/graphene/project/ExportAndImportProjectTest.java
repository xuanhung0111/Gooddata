package com.gooddata.qa.graphene.project;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;
import com.gooddata.qa.utils.io.ResourceUtils;

public class ExportAndImportProjectTest extends AbstractProjectTest {

    private final static String SIMPLE_REPORT = "Simple-Report";
    private final static String REPORT_WITH_EXISTING_OBJS = "Report-with-existing-objects";
    private final static String REPORT_WITH_NEW_OBJS = "Report-with-new-objects";
    private final static String SUM_METRIC = "Sum-Metric";
    private final static String AVG_METRIC = "AVG-Metric";
    private final static String SIMPLE_FILTERED_VARIABLE = "Simple-filtered-variable";
    private final static String SIMPLE_NUMERIC_VARIABLE = "Simple-numeric-variable";

    private final static String PARTIAL_HIGH_SCHOOL = "Partial High School";
    private final static String BACHELORS_DEGREE = "Bachelors Degree";
    private final static String GRADUATE_DEGREE = "Graduate Degree";
    private final static String PARTIAL_COLLEGE = "Partial College";
    private final static String HIGH_SCHOOL_DEGREE = "High School Degree";
    private final static String STORE_MANAGER = "Store Manager";
    private final static String VP_FINANCE = "VP Finance";
    private final static String EDUTCATION = "Education";
    private final static String POSITION = "Position";
    private final static String AMOUNT = "Amount";
    private final static int SIMPLE_NUMERIC_VARIABLE_VALUE = 200000;

    private final static String TARGET_PROJECT_TITLE = "Target-Project";

    @Test(dependsOnMethods = {"createProject"})
    public void setUpProject() {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.PAYROLL_CSV + "/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll-file", getClass());

        initReportCreation().createReport(new UiReportDefinition().withName(SIMPLE_REPORT).withHows(EDUTCATION));

        initVariablePage().createVariable(new AttributeVariable(SIMPLE_FILTERED_VARIABLE)
                .withAttribute(EDUTCATION)
                .withAttributeElements(asList(PARTIAL_COLLEGE, PARTIAL_HIGH_SCHOOL)));

        initVariablePage().createVariable(new NumericVariable(SIMPLE_NUMERIC_VARIABLE)
                .withDefaultNumber(SIMPLE_NUMERIC_VARIABLE_VALUE));

        initMetricPage().createAggregationMetric(MetricTypes.SUM, new CustomMetricUI()
                .withName(SUM_METRIC)
                .withFacts(AMOUNT));

        final DashboardEditBar editBar = initDashboardsPage().editDashboard().addReportToDashboard(SIMPLE_REPORT);
        waitForDashboardPageLoaded(browser);
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(waitForElementVisible(
                dashboardsPage.getContent().getReport(SIMPLE_REPORT, TableReport.class).getRoot()));

        editBar.saveDashboard();
    }

    @Test(dependsOnMethods = {"setUpProject"})
    public void testExportImportProject() throws JSONException {
        final String exportToken = exportProject(true, true, false, DEFAULT_PROJECT_CHECK_LIMIT);
        ProjectRestUtils.deleteProject(getGoodDataClient(), testParams.getProjectId());

        final String targetProjectId = ProjectRestUtils.createBlankProject(getGoodDataClient(), TARGET_PROJECT_TITLE,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());

        testParams.setProjectId(targetProjectId);
        importProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);

        initDashboardsPage();

        //use List.equals due to checking attribute order on dashboard
        assertTrue(dashboardsPage.getContent()
                .getReport(SIMPLE_REPORT, TableReport.class)
                .getAttributeElements()
                .equals(asList(BACHELORS_DEGREE, GRADUATE_DEGREE, HIGH_SCHOOL_DEGREE, PARTIAL_COLLEGE,
                        PARTIAL_HIGH_SCHOOL)), "There is difference between actual and expected attributes");
        takeScreenshot(browser, "imported-dashboard", getClass());

        assertTrue(initVariablePage().isVariableVisible(SIMPLE_NUMERIC_VARIABLE) && variablePage
                .isVariableVisible(SIMPLE_FILTERED_VARIABLE), "Imported variables are not exist");

        assertTrue(initMetricPage().isMetricVisible(SUM_METRIC), "Imported metric is not exist");
    }

    @Test(dependsOnMethods = {"testExportImportProject"})
    public void createReportOnImportedProject() {
        initReportsPage().getReportsList().openReport(SIMPLE_REPORT);

        //use List.equals due to checking attribute order on report
        assertTrue(reportPage.getTableReport()
                .getAttributeElements()
                .equals(asList(BACHELORS_DEGREE, GRADUATE_DEGREE, HIGH_SCHOOL_DEGREE, PARTIAL_COLLEGE, PARTIAL_HIGH_SCHOOL)),
                "There is difference between actual and expected attributes");

        initReportCreation().createReport(new UiReportDefinition()
                .withName(REPORT_WITH_EXISTING_OBJS)
                .withWhats(SUM_METRIC)
                .withHows(EDUTCATION)
                .withFilters(FilterItem.Factory.createPromptFilter(SIMPLE_FILTERED_VARIABLE)));

        assertTrue(isEqualCollection(reportPage.getTableReport()
                .getAttributeElements(),
                asList(PARTIAL_COLLEGE, PARTIAL_HIGH_SCHOOL)),
                "There is difference between actual and expected attributes");
        takeScreenshot(browser, "Simple-report-with-existing-objects", getClass());

        initMetricPage().createAggregationMetric(MetricTypes.SUM, new CustomMetricUI()
                .withName(AVG_METRIC)
                .withFacts(AMOUNT));

        final String attributeVariableName = "Position variable";
        initVariablePage().createVariable(new AttributeVariable(attributeVariableName)
                .withAttribute(POSITION)
                .withAttributeElements(asList(STORE_MANAGER, VP_FINANCE)));

        initReportCreation().createReport(new UiReportDefinition()
                .withName(REPORT_WITH_NEW_OBJS)
                .withWhats(AVG_METRIC)
                .withHows(POSITION)
                .withFilters(FilterItem.Factory.createPromptFilter(attributeVariableName)));

        assertTrue(isEqualCollection(reportPage.getTableReport()
                .getAttributeElements(),
                asList(STORE_MANAGER, VP_FINANCE)),
                "There is difference between actual and expected attributes");
        takeScreenshot(browser, "Simple-report-with-new-objects", getClass());
    }

    @Test(dependsOnMethods = {"testExportImportProject"})
    public void editDashboardOnImportedProject() {
        final DashboardEditBar editBar = initDashboardsPage().editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, EDUTCATION);

        final FilterWidget filterWidget = dashboardsPage.getContent().getFilterWidget(simplifyText(EDUTCATION));

        final WebElement educationFilterElement = filterWidget.getRoot();
        educationFilterElement.click();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(educationFilterElement);

        editBar.addTextToDashboard(TextObject.HEADLINE, "Simple-Text-Obj", "google.com").saveDashboard();

        filterWidget.changeAttributeFilterValue(PARTIAL_HIGH_SCHOOL);

        assertTrue(isEqualCollection(dashboardsPage.getContent()
                .getReport(SIMPLE_REPORT, TableReport.class)
                .getAttributeElements(),
                singletonList(PARTIAL_HIGH_SCHOOL)),
                "There is difference between actual and expected attributes");
        takeScreenshot(browser, "report-after-using-filter", getClass());
    }

    @Test(dependsOnMethods = {"createReportOnImportedProject"})
    public void createDashboardOnImportedProject() {
        final DashboardEditBar editBar = initDashboardsPage().editDashboard();
        dashboardsPage.addNewTab("Tab contains new reports");
        editBar.addReportToDashboard(REPORT_WITH_EXISTING_OBJS).addReportToDashboard(REPORT_WITH_NEW_OBJS);

        final TableReport existingObjsTableReport = dashboardsPage.getContent()
                .getReport(REPORT_WITH_EXISTING_OBJS, TableReport.class);

        existingObjsTableReport.getRoot().click();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(existingObjsTableReport.getRoot());

        final TableReport newObjsTableReport = dashboardsPage.getContent()
                .getReport(REPORT_WITH_NEW_OBJS, TableReport.class);

        newObjsTableReport.getRoot().click();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(newObjsTableReport.getRoot());
        editBar.saveDashboard();

        // use List.equals due to checking attribute order
        assertTrue(existingObjsTableReport.getAttributeElements().equals(asList(PARTIAL_COLLEGE, PARTIAL_HIGH_SCHOOL)),
                "There is difference between actual and expected attributes");
        assertTrue(newObjsTableReport.getAttributeElements().equals(asList(STORE_MANAGER, VP_FINANCE)),
                "There is difference between actual and expected attributes");
        takeScreenshot(browser, "added-reports-to-dashboard", getClass());

        dashboardsPage
                .editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, EDUTCATION)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, POSITION)
                .saveDashboard();
        takeScreenshot(browser, "added-filters-to-dashboard", getClass());

        dashboardsPage.getContent().getFilterWidget(simplifyText(EDUTCATION))
                .changeAttributeFilterValue(PARTIAL_COLLEGE);
        assertTrue(isEqualCollection(existingObjsTableReport.getAttributeElements(), singletonList(PARTIAL_COLLEGE)),
                "There is difference between actual and expected attributes");

        dashboardsPage.getContent().getFilterWidget(simplifyText(POSITION)).changeAttributeFilterValue(STORE_MANAGER);
        assertTrue(isEqualCollection(newObjsTableReport.getAttributeElements(), singletonList(STORE_MANAGER)),
                "There is difference between actual and expected attributes");
    }
}
