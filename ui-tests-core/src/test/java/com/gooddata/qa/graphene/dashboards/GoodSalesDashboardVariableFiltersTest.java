package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardContent;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.project.Project;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.gooddata.qa.browser.BrowserUtils.refreshCurrentPage;
import static com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage.REPORT_LOADED_CLASS_NAME;
import static com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage.waitForReportLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForReportsPageLoaded;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

public class GoodSalesDashboardVariableFiltersTest extends GoodSalesAbstractTest {

    private static final String TEST_DASHBOAD_FILTERS = "test-dashboard-filters";
    private static final String TESTING_REPORT = "Testing report";
    private static final String REPORT_1 = "Report 1";
    private static final String REPORT_2 = "Report 2";

    private static final String STAGE_NAME_FILTER = "stage_name";

    private String nVariableUri = "";

    private static final int YEAR_OF_DATA = 2012;

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-test-dashboard-all-kinds-filters";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createVariables() {
        initVariablePage().createVariable(new AttributeVariable("FStageName").withAttribute(ATTR_STAGE_NAME));

        VariableDetailPage.getInstance(browser)
            .goToVariablesPage()
            .createVariable(new AttributeVariable("FQuarter/Year")
                .withAttribute("Quarter/Year (Snapshot)")
                .withAttributeValues("Q1/2012", "Q2/2012", "Q3/2012", "Q4/2012"));

        nVariableUri = VariableDetailPage.getInstance(browser)
            .goToVariablesPage()
            .createVariable(new NumericVariable("NVariable").withDefaultNumber(123456));
    }

    @Test(dependsOnMethods = {"createVariables"})
    public void createReportsWithVariableFilter() {
        initReportsPage();
        UiReportDefinition rd =
            new UiReportDefinition().withName(REPORT_1).withWhats(METRIC_AMOUNT).withHows(ATTR_STAGE_NAME)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, HowItem.Position.TOP));
        createReport(rd, REPORT_1);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("FStageName", "2010", "2011", "2012",
            "Interest", "Discovery", "Short List", "Risk Assessment", "Conviction", "Negotiation",
            "Closed Won", "Closed Lost"));
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("FQuarter/Year", "2012", "Interest",
            "Discovery", "Short List", "Risk Assessment", "Conviction", "Negotiation", "Closed Won",
            "Closed Lost"));
        reportPage.saveReport();

        initReportsPage();
        rd.withName(REPORT_2);
        createReport(rd, REPORT_2);
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("FQuarter/Year", "2012", "Interest",
            "Discovery", "Short List", "Risk Assessment", "Conviction", "Negotiation", "Closed Won",
            "Closed Lost"));
        reportPage.saveReport();
    }

    @Test(dependsOnMethods = {"createVariables"})
    public void testReportWithNumericalVariableInMetricSentence() {
        initVariablePage()
            .openVariableFromList("NVariable")
            .setDefaultNumericValue(2011)
            .saveChange();

        RestClient restClient = new RestClient(getProfile(Profile.ADMIN));
        Project project = restClient.getProjectService().getProjectById(testParams.getProjectId());
        String metric = "GREATER-NVariable";
        String expression = "SELECT [" + getMetricByTitle(METRIC_AMOUNT).getUri() + "]" +
            " WHERE [" + getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri() + "] > [" + nVariableUri + "]";
        restClient.getMetadataService().createObj(project, new Metric(metric,
            expression.replace("${pid}", testParams.getProjectId()), "#,##0"));

        initReportsPage();
        createReport(new UiReportDefinition().withName("Report 4").withWhats(METRIC_AMOUNT, metric)
            .withHows(ATTR_YEAR_SNAPSHOT), "Report 4");

        try {
            addReportToDashboard("Report 4");
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            List<String> years = report.getAttributeValues();
            List<String> nVarValues = report.getRawMetricValues().subList(3, 3 + years.size());
            Iterator<String> yearsIterator = years.iterator();
            Iterator<String> nVarIterator = nVarValues.iterator();

            while (yearsIterator.hasNext() && nVarIterator.hasNext()) {
                if (Integer.parseInt(yearsIterator.next()) > 2011) {
                    assertNotEquals(nVarIterator.next(), "");
                } else {
                    assertEquals(nVarIterator.next(), "");
                }
            }
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }


    @Test(dependsOnMethods = {"createReportsWithVariableFilter"})
    public void testVariableFilters() {
        try {
            addReportToDashboard(REPORT_1, DashboardWidgetDirection.LEFT);
            addReportToCurrentDashboard(REPORT_2, DashboardWidgetDirection.RIGHT);
            addAttributeFilterToDashboard("FQuarter/Year", DashAttributeFilterTypes.PROMPT, DashboardWidgetDirection.UP);
            addAttributeFilterToDashboard("FStageName", DashAttributeFilterTypes.PROMPT, DashboardWidgetDirection.DOWN);

            dashboardsPage.editDashboard();
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertEquals(report.getAllFilterNames(), asList("FQuarter/Year", "FStageName"));
            dashboardsPage.saveDashboard();
            dashboardsPage.waitForFilterLoaded(asList("FQuarter/Year", "FStageName"));
            dashboardsPage.waitForReportLoaded(asList(REPORT_1, REPORT_2));
            assertEquals(report.openReportInfoViewPanel().getAllFilterNames(), asList("FQuarter/Year", "FStageName"));

            getFilterWidget("fstagename").changeAttributeFilterValues("Short List");
            assertEquals(getRowElementsFrom(getReport(REPORT_1, TableReport.class)).size(), 1);
            assertTrue(getRowElementsFrom(getReport(REPORT_2, TableReport.class)).size() > 1,
                "Report should have records");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createReportsWithVariableFilter"})
    public void testSingleOptionVariableFilter() {
        try {
            addReportToDashboard(REPORT_1, DashboardWidgetDirection.LEFT);
            addReportToCurrentDashboard(REPORT_2, DashboardWidgetDirection.RIGHT);
            addAttributeFilterToDashboard("FQuarter/Year", DashAttributeFilterTypes.PROMPT, DashboardWidgetDirection.UP);
            addAttributeFilterToDashboard("FStageName", DashAttributeFilterTypes.PROMPT, DashboardWidgetDirection.DOWN);

            initDashboardsPage().editDashboard();
            FilterWidget filter = getFilterWidget("fstagename");
            filter.changeSelectionToOneValue();
            waitForReportLoaded(browser);

            filter.openPanel();
            assertTrue(Graphene.createPageFragment(AttributeFilterPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).isOnSingleMode(),
                "Attribute filter panel should be on single mode");
            dashboardsPage.saveDashboard();
            waitForDashboardPageLoaded(browser);

            refreshCurrentPage(browser);
            waitForDashboardPageLoaded(browser);

            assertEquals(filter.getCurrentValue(), "Interest");
            assertEquals(getRowElementsFrom(getReport(REPORT_1, TableReport.class)).size(), 1);
            assertTrue(getRowElementsFrom(getReport(REPORT_2, TableReport.class)).size() > 1,
                "Report should have records");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void addReportToDashboard(String name) {
        addReportToDashboard(name, null);
    }

    private void addReportToDashboard(String name, DashboardWidgetDirection direction) {
        initDashboardsPage().addNewDashboard(TEST_DASHBOAD_FILTERS);
        addReportToCurrentDashboard(name, direction);
    }

    private void addReportToCurrentDashboard(String name, DashboardWidgetDirection direction) {
        DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
        dashboardEditBar.addReportToDashboard(name);

        WebElement latestReport = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded().getRoot();
        dashboardsPage.waitForReportLoaded(singletonList(name));

        if (direction != null) {
            direction.moveElementToRightPlace(latestReport);
        }
        dashboardEditBar.saveDashboard();
        checkRedBar(browser);
        dashboardsPage.waitForReportLoaded(singletonList(name));
    }

    private void addAttributeFilterToDashboard(String attribute, DashAttributeFilterTypes type,
            DashboardWidgetDirection direction) {
        initDashboardsPage()
                .selectDashboard(TEST_DASHBOAD_FILTERS)
                .addAttributeFilterToDashboard(type, attribute);

        if (direction == null) {
            dashboardsPage.saveDashboard();
            waitForReportsPageLoaded(browser);
            waitForDashboardPageLoaded(browser);
            checkRedBar(browser);
            return;
        }

        WebElement filter = getFilterWidget(CssUtils.simplifyText(attribute)).getRoot();
        // get focus
        filter.click();
        direction.moveElementToRightPlace(filter);
        DashboardContent dashboardContent = dashboardsPage.getContent();
        if(dashboardContent.getNumberOfReports() > 0) {
            dashboardContent.getReports().stream().forEach(report -> {
                Graphene.waitGui().until().element(report).attribute("class").contains(REPORT_LOADED_CLASS_NAME);
            });
        }

        dashboardsPage.saveDashboard();
        waitForDashboardPageLoaded(browser);
        checkRedBar(browser);
    }

    private void addAttributeFilterToDashboard(String attribute, DashAttributeFilterTypes type) {
        addAttributeFilterToDashboard(attribute, type, null);
    }

    private Collection<WebElement> getRowElementsFrom(TableReport report) {
        sleepTightInSeconds(1);
        return report.waitForLoaded().getRoot().findElements(By.cssSelector(".gridTile div.element.cell.rows span"));
    }

    private FilterWidget getFilterWidget(String condition) {
        return dashboardsPage.getContent().getFilterWidget(condition);
    }

    private <T extends AbstractReport> T getReport(String name, Class<T> clazz) {
        sleepTightInSeconds(1);
        return dashboardsPage.getContent().getReport(name, clazz);
    }
}
