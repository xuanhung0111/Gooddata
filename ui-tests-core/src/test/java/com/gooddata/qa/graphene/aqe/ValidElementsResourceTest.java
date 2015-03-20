package com.gooddata.qa.graphene.aqe;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.HowItem;
import com.gooddata.qa.graphene.entity.HowItem.Position;
import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.FilterWidget;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"GoodSalesValidElements"}, description = "Tests for GoodSales project relates to ValidElements resource")
public class ValidElementsResourceTest extends GoodSalesAbstractTest {

    private String departmentAttr;
    private String productAttr;
    private String MonthYearCreatedAttr;
    private List<String> allDepartmentValues;
    private List<String> allProductValues;

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-valid-elements-resource";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initialize() throws InterruptedException, JSONException {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
        MonthYearCreatedAttr = "Month/Year (Created)";
        departmentAttr = "Department";
        productAttr = "Product";
        allDepartmentValues = Arrays.asList("Direct Sales", "Inside Sales");
        allProductValues = Arrays.asList("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft",
                                         "TouchAll", "WonderKid");
    }

    /*
     * This test is to cover the following bug: "AQE-1028 - Get 500 Internal Error when creating
     * filtered variable with some attribute elements
     */
    @Test(dependsOnMethods = {"initialize"})
    public void checkForFilteredVariable() throws InterruptedException {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable("Test variable" + System.currentTimeMillis())
            .withAttribute(MonthYearCreatedAttr)
            .withAttributeElements("Jan 2010", "Feb 2010", "Mar 2010"));
    }
    
    /*
     * This test is to cover the following bug:
     * "AQE-1029 - Cascading filter: get 400 bad request when opening list value of child filter"
     */
    @Test(dependsOnMethods = {"initialize"})
    public void checkForCascadingFilter() throws InterruptedException {
        List<String> filteredValuesProduct =
                Arrays.asList("CompuSci", "Educationly", "Explorer",
                        "Grammar Plus", "PhoenixSoft", "WonderKid");
        try {
            initDashboardsPage();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            dashboardsPage.addNewTab("test");
            
            dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, departmentAttr);
            dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, productAttr);
            
            dashboardEditBar.saveDashboard();
            
            FilterWidget departmentFilter = getFilterWidget(departmentAttr);
            FilterWidget productFilter    = getFilterWidget(productAttr);
            
            assertEquals(departmentFilter.getAllAttributeValues(),
                                allDepartmentValues,
                                "List of filter elements is not properly.");
            assertEquals(productFilter.getAllAttributeValues(),
                                allProductValues,
                                "List of filter elements is not properly.");
            
            dashboardsPage.editDashboard();
            Thread.sleep(5000);
            dashboardEditBar.setParentsFilter(productAttr, departmentAttr);// parentFilteName is Department
            dashboardEditBar.saveDashboard();
            
            departmentFilter.changeAttributeFilterValue("Direct Sales");
            
            assertEquals(productFilter.getAllAttributeValues(),
                                filteredValuesProduct,
                                "Cascading filters are not applied correcly");
        } finally {
            dashboardsPage.deleteDashboardTab(dashboardsPage.getTabs().getSelectedTabIndex());
        }
    }
    
    /*
     * This test is to cover the following bug:
     * "AQE-1030 - Get 400 bad request when filtering by Date"
     */
    @Test(dependsOnMethods = {"initialize"})
    public void checkTimeFilter() throws InterruptedException {
        String top5OpenByMoneyReport = "Top 5 Open (by $)";
        String top5WonByMoneyReport = "Top 5 Won (by $)";
        String top5LostByMoneyReport = "Top 5 Lost (by $)";
        List<String> reportAttributeValues = Arrays.asList("Paramore-Redd Online Marketing > Explorer",
                        "Medical Transaction Billing > Explorer", "AccountNow > Explorer",
                        "Mortgagebot > Educationly", "Dennis Corporation > WonderKid");
        List<Float> reportMetricValues = Arrays.asList(13.1f, 820.7f, 693.5f, 484.9f,
                        483.1f, 36.7f, 2.3f, 1.9f, 1.4f, 1.3f);
        System.out.println("Verifying element resource of time filter ...");
        openDashboardTab(1);
        
        getFilterWidget("Close Quarter").changeTimeFilterByEnterFromAndToDate("01/01/2008", "12/30/2014");
        Screenshots.takeScreenshot(browser, "AQE-Check time filter", this.getClass());
        Thread.sleep(2000);
        checkRedBar(browser);
        
        TableReport dashboardTableReport = getDashboardTableReport(top5OpenByMoneyReport);
        assertEquals(dashboardTableReport.getAttributeElements(),
                            reportAttributeValues,
                            "Attribute values in report 'Top 5 Open (by $)' are not properly");
        assertEquals(dashboardTableReport.getMetricElements(),
                            reportMetricValues,
                            "Metric values in report 'Top 5 Open (by $)' are not properly");

        assertTrue(getDashboardTableReport(top5WonByMoneyReport).isNoData(), 
                          "The message in report is not properly.");
        assertTrue(getDashboardTableReport(top5LostByMoneyReport).isNoData());
    }

    /*
     * This test is to cover the following bug:
     * "AQE-1031 - Get 500 internal server error when going to dashboard content variable status"
     */
    @Test(dependsOnMethods = {"initialize"})
    public void checkVariableFilterDashboard() throws ParseException, IOException, JSONException,
            InterruptedException {
        String top5OpenByMoney = "Top 5 Open (by $)";
        RestUtils.addUserToProject(testParams.getHost(), testParams.getProjectId(),
                testParams.getUser(), testParams.getPassword(), testParams.getEditorProfileUri(),
                UserRoles.ADMIN);

        initDashboardsPage();
        dashboardsPage.selectDashboard("Pipeline Analysis");
        dashboardsPage.getTabs().openTab(1);
        
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT, "Status");
        dashboardEditBar.saveDashboard();
        
        signInAsDifferentUser(UserRoles.EDITOR);
        openDashboardTab(1);
        assertEquals(getFilterWidget("Status").getAllAttributeValues(), Arrays.asList("Open"),
                "Variable filter is applied incorrecly");
        getFilterWidget("Close Quarter").changeTimeFilterByEnterFromAndToDate("10/01/2014", "12/31/2014");
        Thread.sleep(5000);
        
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "AQE-Check variable filter at dashboard", this.getClass());
        assertEquals(getDashboardTableReport(top5OpenByMoney).getAttributeElements(),
                            Arrays.asList("Brighton Cromwell > WonderKid"),
                            "Attribute values in report 'Top 5 Open (by $)' are not properly");
        assertEquals(getDashboardTableReport(top5OpenByMoney).getMetricElements(),
                            Arrays.asList(8.6f, 100.0f),
                            "Metric values in report 'Top 5 Open (by $)' are not properly");
        
       signInAsDifferentUser(UserRoles.ADMIN);
    }

    // This test is to cover the bug "AQE-1033 - Get 400 bad request when adding filter into report"
    @Test(dependsOnMethods = {"initialize"})
    public void checkListElementsInReportFilter() throws InterruptedException{
        initReportsPage();
        createReport(new ReportDefinition().withType(ReportTypes.TABLE)
                                                      .withName("CheckListElementsReport")
                                                      .withWhats("Amount")
                                                      .withHows(new HowItem(productAttr, Position.LEFT))
                                                      .withHows(new HowItem(departmentAttr, Position.TOP)),
                                                      "CheckListElementsInReport");

        reportPage.addFilter(FilterItem.Factory.createListValuesFilter(productAttr,
                                                "CompuSci", "Educationly", "Explorer", "WonderKid"));
        checkRedBar(browser);

        reportPage.addFilter(FilterItem.Factory.createListValuesFilter(departmentAttr, "Direct Sales"));
        Screenshots.takeScreenshot(browser, "AQE-Check list elements in report filter", this.getClass());
        checkRedBar(browser);

        assertEquals(reportPage.getTableReport().getAttributeElements(),
                            Arrays.asList("Direct Sales", "CompuSci", "Educationly", "Explorer", "WonderKid"),
                            "Attribute values in report are not properly.");
        System.out.println("metric list: " + reportPage.getTableReport().getMetricElements());
        assertEquals(reportPage.getTableReport().getMetricElements(),
                            Arrays.asList(15582695.69f, 16188138.24f, 30029658.14f, 6978618.41f),
                            "Metric values in report are not properly.");
    }

    /*
     * This test is to cover the bug
     * "AQE-1034 - Get 500 Internal Server Error when loading list element of attribute in analysis page"
     */
    @Test(dependsOnMethods = {"initialize"})
    public void checkListElementsInChartReportFilter() throws InterruptedException {
        initReportsPage();
        createReport(new ReportDefinition().withType(ReportTypes.FUNNEL)
                                                      .withName("CheckListElementsInChartReport")
                                                      .withWhats("Amount")
                                                      .withHows("Department"), "CheckListElementsInChartReport");

        reportPage.addFilter(FilterItem.Factory.createListValuesFilter(productAttr, "CompuSci",
                "Educationly", "Explorer", "WonderKid"));
        Screenshots.takeScreenshot(browser, "AQE-Check list elements in chart report filter",
                this.getClass());
        reportPage.saveReport();
        checkRedBar(browser);
    }

    // This test is to cover the bug "VIZ-502 - Server side image rendering is broken"
    @Test(dependsOnMethods = {"initialize"})
    public void checkServerSideImageRedering() throws InterruptedException {
        openDashboardTab(4); // "Activities" tab
        browser.get(browser.getCurrentUrl() + "&renderImgForce=true");
        waitForDashboardPageLoaded(browser);
        browser.navigate().refresh();
        getFilterWidget("Activity Date").changeTimeFilterByEnterFromAndToDate("01/01/2008", "12/30/2014");

        List<String> reportsToCheck =
                Arrays.asList("Activities by Type", "Activity Level", "Activity by Sales Rep");
        for(Iterator<String> list = reportsToCheck.iterator(); list.hasNext(); ) {
            WebElement reportImg = dashboardsPage.getContent().getImageFromReport(list.next());
            assertTrue(RestUtils.isValidImage(testParams.getHost(), testParams.getUser(),
                                                     testParams.getPassword(), reportImg),
                                                     "Image reports are not loaded properly");
        }
    }

    private FilterWidget getFilterWidget(String filterName) {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        for (FilterWidget filter : dashboardsPage.getFilters()) {
            if (!filter.getRoot().getAttribute("class")
                                 .contains("s-" + CssUtils.simplifyText(filterName)))
                continue;
            return filter;
        }
        return null;
    }

    private void openDashboardTab(int tabindex) throws InterruptedException {
        initDashboardsPage();
        browser.navigate().refresh();
        dashboardsPage.selectDashboard("Pipeline Analysis");
        dashboardsPage.getTabs().openTab(tabindex);
    }

    private void signInAsDifferentUser(UserRoles role) throws JSONException {
        logout();
        signIn(false, role);
    }

    private TableReport getDashboardTableReport(String reportName) {
        return dashboardsPage.getContent().getTableReport(reportName);
    }
}
