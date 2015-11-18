package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.utils.CssUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashFilterTypes;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class GoodSalesDashboardAllKindsFiltersTest extends GoodSalesAbstractTest {

    private static final String TEST_DASHBOAD_FILTERS = "test-dashboard-filters";
    private static final String TESTING_REPORT = "Testing report";
    private static final String REPORT_1 = "Report 1";
    private static final String REPORT_2 = "Report 2";

    private static final String STAGE_NAME = "Stage Name";
    private static final String AMOUNT = "Amount";
    private static final String YEAR_SNAPSHOT = "Year (Snapshot)";

    private static final String STAGE_NAME_FILTER = "stage_name";

    private String nVariableUri = "";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-dashboard-all-kinds-filters";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createTestingReport() {
        initReportsPage();
        createReport(new UiReportDefinition().withName(TESTING_REPORT).withWhats(AMOUNT).withHows(STAGE_NAME)
                .withHows(new HowItem(YEAR_SNAPSHOT, Position.TOP)), TESTING_REPORT);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void testSingleOptionAttributeFilter() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);
            addAttributeFilterToDashboard(STAGE_NAME, DashFilterTypes.ATTRIBUTE);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            FilterWidget filter = getFilterWidget(STAGE_NAME_FILTER);
            filter.changeSelectionToOneValue();
            dashboardEditBar.saveDashboard();

            assertEquals(filter.getCurrentValue(), "Interest");

            assertEquals(
                    getRowElementsFrom(dashboardsPage.getContent().getLatestReport(TableReport.class)).size(), 1);

            filter.openPanel();
            assertTrue(Graphene.createPageFragment(AttributeFilterPanel.class,
                    waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).verifyPanelInOneValueMode());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void verifyFilterConnectedWithReport() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);
            addAttributeFilterToDashboard(STAGE_NAME, DashFilterTypes.ATTRIBUTE);

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(isEqualCollection(report.openReportInfoViewPanel().getAllFilterNames(),
                    singleton(STAGE_NAME)));

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            assertTrue(isEqualCollection(report.getAllFilterNames(), singleton(STAGE_NAME)));
            dashboardEditBar.saveDashboard();
            assertTrue(getRowElementsFrom(report).size() > 1);

            getFilterWidget(STAGE_NAME_FILTER).changeAttributeFilterValue("Short List");

            // reload table report unless will get ArrayIndexOutOfBoundException: Index 1: Size 1
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.waitForReportLoading();
            assertTrue(getRowElementsFrom(report).size() == 1);

            dashboardsPage.editDashboard();
            report.removeFilters(STAGE_NAME);
            dashboardEditBar.saveDashboard();

            getFilterWidget(STAGE_NAME_FILTER).changeAttributeFilterValue("Short List");

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

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            dashboardEditBar.addTimeFilterToDashboard(3, "3 ago");
            dashboardEditBar.saveDashboard();

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(isEqualCollection(report.openReportInfoViewPanel().getAllFilterNames(),
                    singleton("Date dimension (Snapshot)")));
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2012']")).isDisplayed());
            assertTrue(report.getRoot().findElements(By.cssSelector("div[title='2011']")).isEmpty());

            FilterWidget filter = getFilterWidget("filter-time");
            filter.changeTimeFilterValueByClickInTimeLine("2011");

            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.waitForReportLoading();
            assertTrue(report.getRoot().findElements(By.cssSelector("div[title='2012']")).isEmpty());
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2011']")).isDisplayed());

            dashboardsPage.editDashboard();
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.removeFilters("Date dimension (Snapshot)");
            dashboardEditBar.saveDashboard();

            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.waitForReportLoading();
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2010']")).isDisplayed());
            getFilterWidget("filter-time").changeTimeFilterValueByClickInTimeLine("2011");
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2010']")).isDisplayed());
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void filterInheritAttributeName() {
        try {
            initDashboardsPage();
            dashboardsPage.addNewDashboard(TEST_DASHBOAD_FILTERS);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "Account");
            dashboardEditBar.saveDashboard();
            checkRedBar(browser);
            assertEquals(getFilterWidget("account").getTitle(), "ACCOUNT");

            initAttributePage();
            attributePage.initAttribute("Account");
            attributeDetailPage.renameAttribute("Account Edit");

            initDashboardsPage();
            assertEquals(getFilterWidget("account").getTitle(), "ACCOUNT EDIT");

            dashboardsPage.editDashboard();
            getFilterWidget("account").changeTitle("Filter Account");
            dashboardEditBar.saveDashboard();
            assertEquals(getFilterWidget("filter_account").getTitle(), "FILTER ACCOUNT");

            dashboardsPage.editDashboard();
            getFilterWidget("filter_account").changeTitle("");
            dashboardEditBar.saveDashboard();
            assertEquals(getFilterWidget("account_edit").getTitle(), "ACCOUNT EDIT");
        } finally {
            dashboardsPage.deleteDashboard();
            initAttributePage();
            attributePage.initAttribute("Account Edit");
            attributeDetailPage.renameAttribute("Account");
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void createVariables() {
        initVariablePage();
        variablePage.createVariable(new AttributeVariable("FStageName").withAttribute(STAGE_NAME));
        variablePage.createVariable(new AttributeVariable("FQuarter/Year")
                .withAttribute("Quarter/Year (Snapshot)").withAttributeElements("Q1/2012", "Q2/2012", "Q3/2012",
                        "Q4/2012"));
        nVariableUri = variablePage.createVariable(new NumericVariable("NVariable").withDefaultNumber(123456));
    }

    @Test(dependsOnMethods = {"createVariables"})
    public void createReportsWithVariableFilter() {
        initReportsPage();
        UiReportDefinition rd =
                new UiReportDefinition().withName(REPORT_1).withWhats(AMOUNT).withHows(STAGE_NAME)
                        .withHows(new HowItem(YEAR_SNAPSHOT, HowItem.Position.TOP));
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
            addAttributeFilterToDashboard("FQuarter/Year", DashFilterTypes.PROMPT, DashboardWidgetDirection.UP);
            addAttributeFilterToDashboard("FStageName", DashFilterTypes.PROMPT, DashboardWidgetDirection.DOWN);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(isEqualCollection(report.getAllFilterNames(), asList("FQuarter/Year", "FStageName")));
            assertTrue(report.areAllFiltersDisabled());
            dashboardEditBar.saveDashboard();
            assertTrue(isEqualCollection(report.openReportInfoViewPanel().getAllFilterNames(),
                    asList("FQuarter/Year", "FStageName")));

            getFilterWidget("fstagename").changeAttributeFilterValue("Short List");
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
            addAttributeFilterToDashboard("FQuarter/Year", DashFilterTypes.PROMPT, DashboardWidgetDirection.UP);
            addAttributeFilterToDashboard("FStageName", DashFilterTypes.PROMPT, DashboardWidgetDirection.DOWN);

            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardsPage.editDashboard();
            FilterWidget filter = getFilterWidget("fstagename");
            filter.changeSelectionToOneValue();
            filter.openPanel();
            assertTrue(Graphene.createPageFragment(AttributeFilterPanel.class,
                    waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).verifyPanelInOneValueMode());
            dashboardEditBar.saveDashboard();

            assertEquals(filter.getCurrentValue(), "Interest");
            assertTrue(getRowElementsFrom(getReport(REPORT_1, TableReport.class)).size() == 1);
            assertTrue(getRowElementsFrom(getReport(REPORT_2, TableReport.class)).size() > 1);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createVariables"})
    public void testReportWithNumericalVariableInMetricSentence() {
        initVariablePage();
        variablePage.openVariableFromList("NVariable");
        variableDetailPage.setDefaultValue(2011);

        GoodData goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        String metric = "GREATER-NVariable";
        String expression = "SELECT [/gdc/md/${pid}/obj/1279] WHERE [/gdc/md/${pid}/obj/513] > [" +
                nVariableUri + "]";
        goodDataClient.getMetadataService().createObj(project, new Metric(metric,
                expression.replace("${pid}", testParams.getProjectId()), "#,##0"));

        initReportsPage();
        createReport(new UiReportDefinition().withName("Report 4").withWhats(AMOUNT, metric)
                .withHows(YEAR_SNAPSHOT), "Report 4");

        initDashboardsPage();
        try {
            addReportToDashboard("Report 4");
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            List<String> years = report.getAttributeElements();
            List<String> nVarValues = report.getRawMetricElements().subList(3, 3 + years.size());
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

    @Test(dependsOnMethods = {"createProject"})
    public void testDashboardFilterOverrideReportFilter() {
        initReportsPage();
        createReport(
                new UiReportDefinition()
                        .withName("Report 3")
                        .withWhats(AMOUNT)
                        .withHows(new HowItem(YEAR_SNAPSHOT, HowItem.Position.TOP),
                                new HowItem(STAGE_NAME, "Short List")), "report 3");
        checkRedBar(browser);

        try {
            addReportToDashboard("Report 3", DashboardWidgetDirection.LEFT);
            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(getRowElementsFrom(report).size() == 1);
            addAttributeFilterToDashboard(STAGE_NAME, DashFilterTypes.ATTRIBUTE);
            assertTrue(getRowElementsFrom(report).size() > 1);
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private void addReportToDashboard(String name) {
        addReportToDashboard(name, DashboardWidgetDirection.NONE);
    }

    private void addReportToDashboard(String name, DashboardWidgetDirection direction) {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TEST_DASHBOAD_FILTERS);
        addReportToCurrentDashboard(name, direction);
    }

    private void addReportToCurrentDashboard(String name, DashboardWidgetDirection direction) {
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        dashboardEditBar.addReportToDashboard(name);

        WebElement latestReport = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        direction.moveElementToRightPlace(latestReport);
        dashboardEditBar.saveDashboard();
        checkRedBar(browser);
    }

    private void addAttributeFilterToDashboard(String attribute, DashFilterTypes type,
            DashboardWidgetDirection direction) {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.editDashboard();
        dashboardEditBar.addListFilterToDashboard(type, attribute);
        dashboardEditBar.saveDashboard();
        checkRedBar(browser);

        if (direction == DashboardWidgetDirection.NONE)
            return;

        dashboardsPage.editDashboard();
        WebElement filter = getFilterWidget(CssUtils.simplifyText(attribute)).getRoot();
        // get focus
        filter.click();
        direction.moveElementToRightPlace(filter);
        dashboardEditBar.saveDashboard();
        checkRedBar(browser);
    }

    private void addAttributeFilterToDashboard(String attribute, DashFilterTypes type) {
        addAttributeFilterToDashboard(attribute, type, DashboardWidgetDirection.NONE);
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
