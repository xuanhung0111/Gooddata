package com.gooddata.qa.graphene.dashboards;

import com.gooddata.GoodData;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.manage.VariableDetailPage;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.CssUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

public class GoodSalesDashboardAllKindsFiltersTest extends GoodSalesAbstractTest {

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
        createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createTestingReport() {
        initReportsPage();
        createReport(new UiReportDefinition().withName(TESTING_REPORT).withWhats(METRIC_AMOUNT).withHows(ATTR_STAGE_NAME)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP)), TESTING_REPORT);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void testSingleOptionAttributeFilter() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);
            addAttributeFilterToDashboard(ATTR_STAGE_NAME, DashAttributeFilterTypes.ATTRIBUTE);

            dashboardsPage.editDashboard();
            FilterWidget filter = getFilterWidget(STAGE_NAME_FILTER);
            filter.changeSelectionToOneValue();
            dashboardsPage.saveDashboard();

            assertEquals(filter.getCurrentValue(), "Interest");

            assertEquals(
                    getRowElementsFrom(dashboardsPage.getContent().getLatestReport(TableReport.class)).size(), 1);

            filter.openPanel();
            assertTrue(Graphene.createPageFragment(AttributeFilterPanel.class,
                    waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).isOnSingleMode());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void verifyFilterConnectedWithReport() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);
            addAttributeFilterToDashboard(ATTR_STAGE_NAME, DashAttributeFilterTypes.ATTRIBUTE);

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(isEqualCollection(report.openReportInfoViewPanel().getAllFilterNames(),
                    singleton(ATTR_STAGE_NAME)));

            DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
            assertTrue(isEqualCollection(report.getAllFilterNames(), singleton(ATTR_STAGE_NAME)));
            dashboardEditBar.saveDashboard();
            assertTrue(getRowElementsFrom(report).size() > 1);

            getFilterWidget(STAGE_NAME_FILTER).changeAttributeFilterValues("Short List");

            // reload table report unless will get ArrayIndexOutOfBoundException: Index 1: Size 1
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.waitForLoaded();
            assertTrue(getRowElementsFrom(report).size() == 1);

            dashboardsPage.editDashboard();
            report.removeFilters(ATTR_STAGE_NAME);
            dashboardEditBar.saveDashboard();

            getFilterWidget(STAGE_NAME_FILTER).changeAttributeFilterValues("Short List");

            // reload table report
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(getRowElementsFrom(report).size() > 1);
            assertTrue(report.openReportInfoViewPanel().getAllFilterNames().isEmpty());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void timeFilter() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);

            dashboardsPage
                    .addTimeFilterToDashboard(DATE_DIMENSION_SNAPSHOT, DateGranularity.YEAR,
                            String.format("%s ago", Calendar.getInstance().get(Calendar.YEAR) - YEAR_OF_DATA))
                    .saveDashboard();

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(isEqualCollection(report.openReportInfoViewPanel().getAllFilterNames(),
                    singleton(DATE_DIMENSION_SNAPSHOT)));
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2012']")).isDisplayed());
            assertTrue(report.getRoot().findElements(By.cssSelector("div[title='2011']")).isEmpty());
            report.closeReportInfoViewPanel();

            FilterWidget filter = getFilterWidget("filter-time");
            filter.changeTimeFilterValueByClickInTimeLine("2011");

            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.waitForLoaded();
            assertTrue(report.getRoot().findElements(By.cssSelector("div[title='2012']")).isEmpty());
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2011']")).isDisplayed());

            dashboardsPage.editDashboard();
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.removeFilters(DATE_DIMENSION_SNAPSHOT);
            dashboardsPage.saveDashboard();

            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.waitForLoaded();
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2010']")).isDisplayed());
            getFilterWidget("filter-time").changeTimeFilterValueByClickInTimeLine("2011");
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2010']")).isDisplayed());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterInheritAttributeName() {
        try {
            initDashboardsPage().addNewDashboard(TEST_DASHBOAD_FILTERS);

            dashboardsPage
                    .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, "Account")
                    .saveDashboard();
            checkRedBar(browser);
            assertEquals(getFilterWidget("account").getTitle(), "ACCOUNT");

            initAttributePage().initAttribute("Account")
                .changeName("Account Edit");

            initDashboardsPage().selectDashboard(TEST_DASHBOAD_FILTERS);
            assertEquals(getFilterWidget("account_edit").getTitle(), "ACCOUNT EDIT");

            dashboardsPage.editDashboard();
            getFilterWidget("account_edit").changeTitle("Filter Account");
            dashboardsPage.saveDashboard();
            assertEquals(getFilterWidget("filter_account").getTitle(), "FILTER ACCOUNT");

            dashboardsPage.editDashboard();
            getFilterWidget("filter_account").changeTitle("");
            dashboardsPage.saveDashboard();
            assertEquals(getFilterWidget("account_edit").getTitle(), "ACCOUNT EDIT");
        } finally {
            dashboardsPage.deleteDashboard();
            initAttributePage().initAttribute("Account Edit")
                .changeName("Account");
        }
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

    @Test(dependsOnMethods = {"createReportsWithVariableFilter"})
    public void testVariableFilters() {
        try {
            addReportToDashboard(REPORT_1, DashboardWidgetDirection.LEFT);
            addReportToCurrentDashboard(REPORT_2, DashboardWidgetDirection.RIGHT);
            addAttributeFilterToDashboard("FQuarter/Year", DashAttributeFilterTypes.PROMPT, DashboardWidgetDirection.UP);
            addAttributeFilterToDashboard("FStageName", DashAttributeFilterTypes.PROMPT, DashboardWidgetDirection.DOWN);

            dashboardsPage.editDashboard();
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(isEqualCollection(report.getAllFilterNames(), asList("FQuarter/Year", "FStageName")));
            dashboardsPage.saveDashboard();
            assertTrue(isEqualCollection(report.openReportInfoViewPanel().getAllFilterNames(),
                    asList("FQuarter/Year", "FStageName")));

            getFilterWidget("fstagename").changeAttributeFilterValues("Short List");
            assertTrue(getRowElementsFrom(getReport(REPORT_1, TableReport.class)).size() == 1);
            assertTrue(getRowElementsFrom(getReport(REPORT_2, TableReport.class)).size() > 1);
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

            dashboardsPage.editDashboard();
            FilterWidget filter = getFilterWidget("fstagename");
            filter.changeSelectionToOneValue();
            filter.openPanel();
            assertTrue(Graphene.createPageFragment(AttributeFilterPanel.class,
                    waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).isOnSingleMode());
            dashboardsPage.saveDashboard();

            assertEquals(filter.getCurrentValue(), "Interest");
            assertTrue(getRowElementsFrom(getReport(REPORT_1, TableReport.class)).size() == 1);
            assertTrue(getRowElementsFrom(getReport(REPORT_2, TableReport.class)).size() > 1);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createVariables"})
    public void testReportWithNumericalVariableInMetricSentence() {
        initVariablePage()
                .openVariableFromList("NVariable")
                .setDefaultNumericValue(2011)
                .saveChange();

        GoodData goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        String metric = "GREATER-NVariable";
        String expression = "SELECT [" + getMetricByTitle(METRIC_AMOUNT).getUri() + "]" +
                " WHERE [" + getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri() + "] > [" + nVariableUri + "]";
        goodDataClient.getMetadataService().createObj(project, new Metric(metric,
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

    @Test(dependsOnGroups = {"createProject"})
    public void testDashboardFilterOverrideReportFilter() {
        initReportsPage();
        createReport(
                new UiReportDefinition()
                        .withName("Report 3")
                        .withWhats(METRIC_AMOUNT)
                        .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, HowItem.Position.TOP),
                                new HowItem(ATTR_STAGE_NAME, "Short List")), "report 3");
        checkRedBar(browser);

        try {
            addReportToDashboard("Report 3", DashboardWidgetDirection.LEFT);
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(getRowElementsFrom(report).size() == 1);
            addAttributeFilterToDashboard(ATTR_STAGE_NAME, DashAttributeFilterTypes.ATTRIBUTE);
            assertTrue(getRowElementsFrom(report).size() > 1);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDashboardFilterOverridesReportTimeFilter() {
        createReport(
                new UiReportDefinition()
                        .withName("Report 5")
                        .withWhats(METRIC_AMOUNT)
                        .withHows(new HowItem(ATTR_STAGE_NAME, Position.LEFT),
                                new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP))
                        .withFilters(FilterItem.Factory.createAttributeFilter(ATTR_YEAR_SNAPSHOT, "2011")),
                "Report 5"
        );
        checkRedBar(browser);

        try {
            addReportToDashboard("Report 5");
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertThat(report.getAttributeValues(), hasItem("2011"));

            dashboardsPage.addTimeFilterToDashboard(DATE_DIMENSION_SNAPSHOT, DateGranularity.YEAR,
                    String.format("%s ago", Calendar.getInstance().get(Calendar.YEAR) - 2012))
                    .saveDashboard();
            assertThat(report.getAttributeValues(), not(hasItem("2011")));
            assertThat(report.getAttributeValues(), hasItem("2012"));
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

        WebElement latestReport = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        if (direction != null) {
            direction.moveElementToRightPlace(latestReport);
        }
        dashboardEditBar.saveDashboard();
        checkRedBar(browser);
    }

    private void addAttributeFilterToDashboard(String attribute, DashAttributeFilterTypes type,
            DashboardWidgetDirection direction) {
        initDashboardsPage()
                .selectDashboard(TEST_DASHBOAD_FILTERS)
                .addAttributeFilterToDashboard(type, attribute);

        if (direction == null) {
            dashboardsPage.saveDashboard();
            checkRedBar(browser);
            return;
        }

        WebElement filter = getFilterWidget(CssUtils.simplifyText(attribute)).getRoot();
        // get focus
        filter.click();
        direction.moveElementToRightPlace(filter);
        dashboardsPage.saveDashboard();
        checkRedBar(browser);
    }

    private void addAttributeFilterToDashboard(String attribute, DashAttributeFilterTypes type) {
        addAttributeFilterToDashboard(attribute, type, null);
    }

    private Collection<WebElement> getRowElementsFrom(TableReport report) {
        sleepTightInSeconds(1);
        return report.getRoot().findElements(By.cssSelector(".gridTile div.element.cell.rows span"));
    }

    private FilterWidget getFilterWidget(String condition) {
        return dashboardsPage.getContent().getFilterWidget(condition);
    }

    private <T extends AbstractReport> T getReport(String name, Class<T> clazz) {
        sleepTightInSeconds(1);
        return dashboardsPage.getContent().getReport(name, clazz);
    }
}
