package com.gooddata.qa.graphene.aqe;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DASH_PIPELINE_ANALYSIS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STATUS;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;

public class ValidElementsResourceTest extends GoodSalesAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-valid-elements-resource";
    }

    /*
     * This test is to cover the following bug: "AQE-1028 - Get 500 Internal Error when creating
     * filtered variable with some attribute elements
     */
    @Test(dependsOnGroups = {"createProject"})
    public void checkForFilteredVariable() {
        initVariablePage().createVariable(new AttributeVariable("Test variable" + System.currentTimeMillis())
            .withAttribute(ATTR_MONTH_YEAR_CREATED)
            .withAttributeValues("Jan 2010", "Feb 2010", "Mar 2010"));
    }

    /*
     * This test is to cover the following bug:
     * "AQE-1029 - Cascading filter: get 400 bad request when opening list value of child filter"
     */
    @Test(dependsOnGroups = {"createProject"})
    public void checkForCascadingFilter() {
        List<String> filteredValuesProduct =
                asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid");
        try {
            initDashboardsPage();
            dashboardsPage.addNewDashboard("Dashboard Test");
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();

            dashboardEditBar
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_DEPARTMENT)
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT)
                    .saveDashboard();

            FilterWidget departmentFilter = getFilterWidget(ATTR_DEPARTMENT);
            FilterWidget productFilter = getFilterWidget(ATTR_PRODUCT);

            assertTrue(CollectionUtils.isEqualCollection(asList("Direct Sales", "Inside Sales"),
                    departmentFilter.getAllAttributeValues()), "List of filter elements is not properly.");
            assertTrue(CollectionUtils.isEqualCollection(
                    asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "TouchAll", "WonderKid"),
                    productFilter.getAllAttributeValues()), "List of filter elements is not properly.");

            dashboardsPage.editDashboard();
            sleepTightInSeconds(5);
            dashboardEditBar.setParentsFilter(ATTR_PRODUCT, ATTR_DEPARTMENT);// parentFilteName is Department
            dashboardEditBar.saveDashboard();

            departmentFilter.changeAttributeFilterValue("Direct Sales");
            assertTrue(CollectionUtils.isEqualCollection(filteredValuesProduct,
                    productFilter.getAllAttributeValues()), "Cascading filters are not applied correcly");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    /*
     * This test is to cover the following bug:
     * "AQE-1030 - Get 400 bad request when filtering by Date"
     */
    @Test(dependsOnGroups = {"createProject"})
    public void checkTimeFilter() {
        String top5OpenByMoneyReport = "Top 5 Open (by $)";
        String top5WonByMoneyReport = "Top 5 Won (by $)";
        String top5LostByMoneyReport = "Top 5 Lost (by $)";
        List<String> reportAttributeValues = asList("Paramore-Redd Online Marketing > Explorer",
                        "Medical Transaction Billing > Explorer", "AccountNow > Explorer",
                        "Mortgagebot > Educationly", "Dennis Corporation > WonderKid");
        List<Float> reportMetricValues = asList(13.1f, 820.7f, 693.5f, 484.9f,
                        483.1f, 36.7f, 2.3f, 1.9f, 1.4f, 1.3f);
        System.out.println("Verifying element resource of time filter ...");
        openDashboardTab(1);

        getFilterWidget("Close Quarter").changeTimeFilterByEnterFromAndToDate("01/01/2008", "12/30/2014");
        Screenshots.takeScreenshot(browser, "AQE-Check time filter", this.getClass());
        sleepTightInSeconds(2);
        checkRedBar(browser);

        TableReport dashboardTableReport = getDashboardTableReport(top5OpenByMoneyReport);
        assertTrue(CollectionUtils.isEqualCollection(dashboardTableReport.getAttributeElements(),
                            reportAttributeValues),
                            "Attribute values in report 'Top 5 Open (by $)' are not properly");
        assertTrue(CollectionUtils.isEqualCollection(dashboardTableReport.getMetricElements(),
                            reportMetricValues),
                            "Metric values in report 'Top 5 Open (by $)' are not properly");

        assertTrue(getDashboardTableReport(top5WonByMoneyReport).isNoData(), 
                          "The message in report is not properly.");
        assertTrue(getDashboardTableReport(top5LostByMoneyReport).isNoData());
    }

    /*
     * This test is to cover the following bug:
     * "AQE-1031 - Get 500 internal server error when going to dashboard content variable status"
     */
    @Test(dependsOnGroups = {"createProject"})
    public void checkVariableFilterDashboard() throws ParseException, IOException, JSONException {
        String top5OpenByMoney = "Top 5 Open (by $)";
        String newAdminUser = createAndAddUserToProject(UserRoles.ADMIN);

        initDashboardsPage();
        dashboardsPage.selectDashboard(DASH_PIPELINE_ANALYSIS);
        dashboardsPage.getTabs().openTab(1);

        dashboardsPage
                .editDashboard()
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, "Status")
                .saveDashboard();

        logout();
        signInAtGreyPages(newAdminUser, testParams.getPassword());

        openDashboardTab(1);
        assertTrue(CollectionUtils.isEqualCollection(getFilterWidget(ATTR_STATUS).getAllAttributeValues(),
                asList("Open")), "Variable filter is applied incorrecly");
        getFilterWidget("Close Quarter").changeTimeFilterByEnterFromAndToDate("10/01/2014", "12/31/2014");
        sleepTightInSeconds(5);

        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "AQE-Check variable filter at dashboard", this.getClass());
        assertTrue(CollectionUtils.isEqualCollection(getDashboardTableReport(top5OpenByMoney)
                .getAttributeElements(), asList("Brighton Cromwell > WonderKid")),
                "Attribute values in report 'Top 5 Open (by $)' are not properly");
        assertTrue(CollectionUtils.isEqualCollection(getDashboardTableReport(top5OpenByMoney)
                .getMetricElements(), asList(8.6f, 100.0f)),
                "Metric values in report 'Top 5 Open (by $)' are not properly");

       logoutAndLoginAs(true, UserRoles.ADMIN);
    }

    // This test is to cover the bug "AQE-1033 - Get 400 bad request when adding filter into report"
    @Test(dependsOnGroups = {"createProject"})
    public void checkListElementsInReportFilter() {
        initReportsPage();
        createReport(new UiReportDefinition().withType(ReportTypes.TABLE)
                                           .withName("CheckListElementsReport")
                                           .withWhats("Amount")
                                           .withHows(new HowItem(ATTR_PRODUCT, Position.LEFT))
                                           .withHows(new HowItem(ATTR_DEPARTMENT, Position.TOP)),
                                           "CheckListElementsInReport");

        reportPage.addFilter(FilterItem.Factory.createAttributeFilter(ATTR_PRODUCT,
                "CompuSci", "Educationly", "Explorer", "WonderKid"));
        checkRedBar(browser);

        reportPage.addFilter(FilterItem.Factory.createAttributeFilter(ATTR_DEPARTMENT, "Direct Sales"));
        Screenshots.takeScreenshot(browser, "AQE-Check list elements in report filter", this.getClass());
        checkRedBar(browser);

        TableReport report = reportPage.getTableReport();
        assertTrue(CollectionUtils.isEqualCollection(report.getAttributeElements(),
                asList("Direct Sales", "CompuSci", "Educationly", "Explorer", "WonderKid")),
                "Attribute values in report are not properly.");
        assertTrue(CollectionUtils.isEqualCollection(report.getMetricElements(),
                asList(15582695.69f, 16188138.24f, 30029658.14f, 6978618.41f)),
                "Metric values in report are not properly.");
    }

    /*
     * This test is to cover the bug
     * "AQE-1034 - Get 500 Internal Server Error when loading list element of attribute in analysis page"
     */
    @Test(dependsOnGroups = {"createProject"})
    public void checkListElementsInChartReportFilter() {
        initReportsPage();
        createReport(new UiReportDefinition().withType(ReportTypes.FUNNEL)
                                           .withName("CheckListElementsInChartReport")
                                           .withWhats(METRIC_AMOUNT)
                                           .withHows(ATTR_DEPARTMENT), "CheckListElementsInChartReport");

        reportPage.addFilter(FilterItem.Factory.createAttributeFilter(ATTR_PRODUCT, "CompuSci",
                "Educationly", "Explorer", "WonderKid"));
        Screenshots.takeScreenshot(browser, "AQE-Check list elements in chart report filter",
                this.getClass());
        reportPage.saveReport();
        checkRedBar(browser);
    }

    private FilterWidget getFilterWidget(String filterName) {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        for (FilterWidget filter : dashboardsPage.getFilters()) {
            if (!filter.getRoot().getAttribute("class").contains("s-" + simplifyText(filterName)))
                continue;
            return filter;
        }
        return null;
    }

    private void openDashboardTab(int tabindex) {
        initDashboardsPage();
        browser.navigate().refresh();
        dashboardsPage.selectDashboard(DASH_PIPELINE_ANALYSIS);
        dashboardsPage.getTabs().openTab(tabindex);
    }

    private TableReport getDashboardTableReport(String reportName) {
        return dashboardsPage.getContent().getReport(reportName, TableReport.class);
    }
}
