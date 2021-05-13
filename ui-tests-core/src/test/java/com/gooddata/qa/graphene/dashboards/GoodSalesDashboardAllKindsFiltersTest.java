package com.gooddata.qa.graphene.dashboards;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardContent;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.CssUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Collection;

import static com.gooddata.qa.graphene.fragments.dashboards.DashboardContent.REPORT_TITLE_LOCATOR;
import static com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage.REPORT_LOADED_CLASS_NAME;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Collections.singletonList;
import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
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
        getMetricCreator().createAmountMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createTestingReport() {
        initReportsPage();
        createReport(new UiReportDefinition().withName(TESTING_REPORT).withWhats(METRIC_AMOUNT).withHows(ATTR_STAGE_NAME)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP)), TESTING_REPORT);
        checkRedBar(browser);
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void verifyFilterConnectedWithReport() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);
            addAttributeFilterToDashboard(ATTR_STAGE_NAME, DashAttributeFilterTypes.ATTRIBUTE);

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertEquals(report.openReportInfoViewPanel().getAllFilterNames(), singleton(ATTR_STAGE_NAME));

            DashboardEditBar dashboardEditBar = dashboardsPage.editDashboard();
            assertEquals(report.getAllFilterNames(), singleton(ATTR_STAGE_NAME));
            dashboardEditBar.saveDashboard();

            dashboardsPage.waitForFilterLoaded(singletonList(ATTR_STAGE_NAME));
            dashboardsPage.waitForReportLoaded(singletonList(TESTING_REPORT));
            assertTrue(getRowElementsFrom(report).size() > 1, "Report should have records");

            getFilterWidget(STAGE_NAME_FILTER).changeAttributeFilterValues("Short List");

            // reload table report unless will get ArrayIndexOutOfBoundException: Index 1: Size 1
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertEquals(getRowElementsFrom(report).size(), 1);

            dashboardsPage.editDashboard();
            report.removeFilters(ATTR_STAGE_NAME);
            dashboardEditBar.saveDashboard();
            dashboardsPage.waitForFilterLoaded(singletonList(ATTR_STAGE_NAME));
            dashboardsPage.waitForReportLoaded(singletonList(TESTING_REPORT));

            getFilterWidget(STAGE_NAME_FILTER).changeAttributeFilterValues("Short List");

            // reload table report
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertTrue(getRowElementsFrom(report).size() > 1, "Report should have records");
            assertTrue(report.openReportInfoViewPanel().getAllFilterNames().isEmpty(), "Report should have no filter");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnMethods = {"createTestingReport"})
    public void testSingleOptionAttributeFilter() {
        try {
            addReportToDashboard(TESTING_REPORT, DashboardWidgetDirection.LEFT);
            addAttributeFilterToDashboard(ATTR_STAGE_NAME, DashAttributeFilterTypes.ATTRIBUTE);

            initDashboardsPage().editDashboard();
            FilterWidget filter = getFilterWidget(STAGE_NAME_FILTER);
            filter.changeSelectionToOneValue();

            dashboardsPage.waitForFilterLoaded(singletonList(ATTR_STAGE_NAME));
            dashboardsPage.waitForReportLoaded(singletonList(TESTING_REPORT));

            dashboardsPage.saveDashboard();

            dashboardsPage.waitForFilterLoaded(singletonList(ATTR_STAGE_NAME));
            dashboardsPage.waitForReportLoaded(singletonList(TESTING_REPORT));

            assertEquals(filter.getCurrentValue(), "Interest");

            assertEquals(
                    getRowElementsFrom(dashboardsPage.getContent().getLatestReport(TableReport.class)).size(), 1);

            filter.openPanel();
            assertTrue(Graphene.createPageFragment(AttributeFilterPanel.class,
                    waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser)).isOnSingleMode(),
                    "Attribute filter panel should be on single mode");
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
            dashboardsPage.waitForFilterLoaded(singletonList(DATE_DIMENSION_SNAPSHOT));
            dashboardsPage.waitForReportLoaded(singletonList(TESTING_REPORT));

            TableReport report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            assertEquals(report.openReportInfoViewPanel().getAllFilterNames(), singleton(DATE_DIMENSION_SNAPSHOT));
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2012']")).isDisplayed(),
                    "Should have record of 2012");
            assertTrue(report.getRoot().findElements(By.cssSelector("div[title='2011']")).isEmpty(),
                    "Shouldn't have record of 2011");
            report.closeReportInfoViewPanel();

            FilterWidget filter = getFilterWidget("filter-time");
            filter.changeTimeFilterValueByClickInTimeLine("2011");

            report = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertTrue(report.getRoot().findElements(By.cssSelector("div[title='2012']")).isEmpty(),
                    "Shouldn't have record of 2012");
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2011']")).isDisplayed(),
                    "Should have record of 2011");

            dashboardsPage.editDashboard();
            report = dashboardsPage.getContent().getLatestReport(TableReport.class);
            report.removeFilters(DATE_DIMENSION_SNAPSHOT);
            dashboardsPage.saveDashboard();
            dashboardsPage.waitForFilterLoaded(singletonList(DATE_DIMENSION_SNAPSHOT));
            dashboardsPage.waitForReportLoaded(singletonList(TESTING_REPORT));

            report = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2010']")).isDisplayed(),
                    "Should have record of 2010");
            getFilterWidget("filter-time").changeTimeFilterValueByClickInTimeLine("2011");
            assertTrue(report.getRoot().findElement(By.cssSelector("div[title='2010']")).isDisplayed(),
                    "Should have record of 2010");
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
            dashboardsPage.waitForFilterLoaded(singletonList(ATTR_ACCOUNT));
            checkRedBar(browser);
            assertEquals(getFilterWidget("account").getTitle(), "ACCOUNT");

            initAttributePage().initAttribute("Account")
                .changeName("Account Edit");

            initDashboardsPage().selectDashboard(TEST_DASHBOAD_FILTERS);
            assertEquals(getFilterWidget("account_edit").getTitle(), "ACCOUNT EDIT");

            dashboardsPage.editDashboard();
            getFilterWidget("account_edit").changeTitle("Filter Account");

            //Work around for saving dashboard with changing title filter filter not successfully
            waitForElementVisible(By.id("footerCopyright"), browser).click();
            dashboardsPage.saveDashboard();
            dashboardsPage.waitForFilterLoaded(singletonList("FILTER ACCOUNT"));
            assertEquals(getFilterWidget("filter_account").getTitle(), "FILTER ACCOUNT");

            dashboardsPage.editDashboard();
            getFilterWidget("filter_account").changeTitle("");

            //Work around for saving dashboard with changing title filter filter not successfully
            waitForElementVisible(By.id("footerCopyright"), browser).click();

            assertEquals(getFilterWidget("filter_account").getTitle(), "ACCOUNT EDIT");

            dashboardsPage.saveDashboard();
            dashboardsPage.waitForFilterLoaded(singletonList("ACCOUNT EDIT"));
            assertEquals(getFilterWidget("account_edit").getTitle(), "ACCOUNT EDIT");
        } finally {
            dashboardsPage.deleteDashboard();
            initAttributePage().initAttribute("Account Edit")
                .changeName("Account");
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
            assertEquals(getRowElementsFrom(report).size(), 1);
            addAttributeFilterToDashboard(ATTR_STAGE_NAME, DashAttributeFilterTypes.ATTRIBUTE);
            assertTrue(getRowElementsFrom(report).size() > 1, "Report should have records");
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
                dashboardContent.getReport(report.findElement(REPORT_TITLE_LOCATOR).getText(), TableReport.class).waitForLoaded();
            });
        }

        dashboardsPage.waitForFilterLoaded(singletonList(attribute));

        dashboardsPage.saveDashboard();
        checkRedBar(browser);
        dashboardsPage.waitForFilterLoaded(singletonList(attribute));
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
