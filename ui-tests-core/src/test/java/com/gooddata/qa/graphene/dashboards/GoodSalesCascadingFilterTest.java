package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_MONTH_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPP_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_QUARTER_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_REGION;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardContent;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.SaveAsDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.GroupConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.AttributeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.Sleeper;

public class GoodSalesCascadingFilterTest extends GoodSalesAbstractTest {

    private static final String REPORT_1 = "Report1";
    private static final String REPORT_2 = "Report2";
    private static final String REPORT_3 = "Report3";

    private static final String ATTRIBUTE_TEST_DASHBOARD = "AttributeTestDashboard";
    private static final String DATE_TEST_DASHBOARD = "DateTestDashboard";
    private static final String TMP_DASHBOARD = "TmpDashboard";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-test-cascading-filter";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"init"})
    public void createReports() {
        GoodData goodDataClient = getGoodDataClient();
        Project project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        MetadataService mdService = goodDataClient.getMetadataService();

        String amountMetricUri = mdService.getObjUri(project, Metric.class, identifier("ah1EuQxwaCqs"));
        Attribute account = mdService.getObj(project, Attribute.class, identifier("attr.account.id"));
        Attribute stageName = mdService.getObj(project, Attribute.class, identifier("attr.stage.name"));
        Attribute product = mdService.getObj(project, Attribute.class, identifier("attr.product.id"));

        // *** create report 1 ***
        ReportDefinition definition =
                GridReportDefinitionContent.create(
                        REPORT_1,
                        singletonList("metricGroup"),
                        asList(new AttributeInGrid(account.getDefaultDisplayForm().getUri()), new AttributeInGrid(
                                stageName.getDefaultDisplayForm().getUri())), singletonList(new GridElement(
                                amountMetricUri, METRIC_AMOUNT)));
        definition = mdService.createObj(project, definition);
        mdService.createObj(project, new Report(definition.getTitle(), definition));

        // *** create report 2 ***
        definition =
                GridReportDefinitionContent.create(
                        REPORT_2,
                        singletonList("metricGroup"),
                        asList(new AttributeInGrid(stageName.getDefaultDisplayForm().getUri()),
                                new AttributeInGrid(product.getDefaultDisplayForm().getUri())),
                        singletonList(new GridElement(amountMetricUri, METRIC_AMOUNT)));
        definition = mdService.createObj(project, definition);
        mdService.createObj(project, new Report(definition.getTitle(), definition));

        // *** create report 3 ***
        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName(REPORT_3).withWhats(METRIC_AMOUNT).withHows(ATTR_STAGE_NAME)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP),
                        new HowItem(ATTR_QUARTER_YEAR_SNAPSHOT, Position.TOP),
                        new HowItem(ATTR_MONTH_YEAR_SNAPSHOT, Position.TOP));
        createReport(rd, REPORT_3);
    }

    @Test(dependsOnMethods = {"createReports"}, groups = {"init"})
    public void prepareAttributeFiltersDashboard() {
        initDashboardsPage();

        dashboardsPage.addNewDashboard(ATTRIBUTE_TEST_DASHBOARD);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        addReportToDashboardAndMoveToRightPlace(REPORT_1, DashboardWidgetDirection.LEFT);

        addReportToDashboardAndMoveToRightPlace(REPORT_2, DashboardWidgetDirection.RIGHT);

        addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_ACCOUNT, DashboardWidgetDirection.UP);

        addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_STAGE_NAME, DashboardWidgetDirection.MIDDLE);

        addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_PRODUCT, DashboardWidgetDirection.DOWN);

        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnMethods = {"createReports"}, groups = {"init"})
    public void prepareDateFiltersDashboard() {
        initDashboardsPage();

        dashboardsPage.addNewDashboard(DATE_TEST_DASHBOARD);
        dashboardsPage.editDashboard();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();

        addReportToDashboardAndMoveToRightPlace(REPORT_3, DashboardWidgetDirection.LEFT);

        addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_YEAR_SNAPSHOT, DashboardWidgetDirection.UP);

        addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_QUARTER_YEAR_SNAPSHOT, 
                DashboardWidgetDirection.MIDDLE);

        addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_MONTH_YEAR_SNAPSHOT, DashboardWidgetDirection.DOWN);

        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnGroups = {"init"})
    public void testAdvanceCombineCascadingAndGroupFilter() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard(TMP_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();

            addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_PRODUCT, DashboardWidgetDirection.LEFT);
            addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_ACCOUNT, DashboardWidgetDirection.RIGHT);
            addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_REGION, DashboardWidgetDirection.UP);
            addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_DEPARTMENT, DashboardWidgetDirection.MIDDLE);

            dashboardEditBar.setParentsFilterUsingDataset(ATTR_ACCOUNT, ATTR_OPP_SNAPSHOT, ATTR_PRODUCT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_REGION, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilter(ATTR_DEPARTMENT, ATTR_REGION);

            WidgetConfigPanel configPanel = dashboardEditBar.openGroupConfigPanel();
            configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class)
                .selectFilters(ATTR_ACCOUNT, ATTR_PRODUCT);
            configPanel.saveConfiguration();

            configPanel = dashboardEditBar.openGroupConfigPanel();
            configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class)
                .selectFilters(ATTR_REGION, ATTR_DEPARTMENT);
            configPanel.saveConfiguration();
            dashboardEditBar.saveDashboard();

            WebElement groupAButton = waitForElementVisible(
                    By.cssSelector(".yui3-c-dashboardwidget.odd .s-btn-apply"), browser);
            WebElement groupBButton = waitForElementVisible(
                    By.cssSelector(".yui3-c-dashboardwidget.even .s-btn-apply"), browser);
            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget regionFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_REGION));
            FilterWidget departmentFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_DEPARTMENT));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));

            regionFilter.changeAttributeFilterValue("East Coast");
            regionFilter.getRoot().click();
            departmentFilter.changeAttributeFilterValue("Direct Sales");
            departmentFilter.getRoot().click();

            assertTrue(groupAButton.getAttribute("class").contains("disabled"));
            assertFalse(groupBButton.getAttribute("class").contains("disabled"));
            groupBButton.click();
            assertEquals(regionFilter.getCurrentValue(), "East Coast");
            assertEquals(departmentFilter.getCurrentValue(), "Direct Sales");
            assertEquals(productFilter.getCurrentValue(), "All");
            assertEquals(accountFilter.getCurrentValue(), "All");
            assertTrue(groupAButton.getAttribute("class").contains("disabled"));
            assertTrue(groupBButton.getAttribute("class").contains("disabled"));

            productFilter.changeAttributeFilterValue("Educationly");
            productFilter.getRoot().click();
            assertEquals(regionFilter.getCurrentValue(), "All");
            assertEquals(departmentFilter.getCurrentValue(), "All");
            assertEquals(productFilter.getCurrentValue(), "Educationly");
            assertEquals(accountFilter.getCurrentValue(), "All");
            assertFalse(groupAButton.getAttribute("class").contains("disabled"));
            assertFalse(groupBButton.getAttribute("class").contains("disabled"));

            regionFilter.changeAttributeFilterValue("West Coast");
            regionFilter.getRoot().click();
            groupAButton.click();
            groupBButton.click();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testBasicCombineCascadingAndGroupFilter() {
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_STAGE_NAME);

            WidgetConfigPanel configPanel = dashboardEditBar.openGroupConfigPanel();
            configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class)
                .selectFilters(ATTR_ACCOUNT, ATTR_STAGE_NAME, ATTR_PRODUCT);
            configPanel.saveConfiguration();
            dashboardEditBar.saveDashboard();

            WebElement groupButton = waitForElementVisible(By.cssSelector(".s-btn-apply"), browser);
            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            accountFilter.changeAttributeFilterValue("123 Exteriors", "14 West");
            accountFilter.getRoot().click();
            assertFalse(groupButton.getAttribute("class").contains("disabled"));

            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            stageNameFilter.getRoot().click();
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid")));
            productFilter.getRoot().click();

            stageNameFilter.changeAttributeFilterValue("Closed Won");
            stageNameFilter.getRoot().click();
            productFilter.changeAttributeFilterValue("Explorer");
            productFilter.getRoot().click();
            groupButton.click();
            Sleeper.sleepTightInSeconds(2);
            assertEquals(dashboardContent.getReport("Report1", TableReport.class)
                    .getAttributeElementsByRow().size(), 1);
            assertEquals(dashboardContent.getReport("Report2", TableReport.class)
                    .getAttributeElementsByRow().size(), 1);

            accountFilter.changeAttributeFilterValue("101 Financial");
            accountFilter.getRoot().click();
            assertEquals(productFilter.getCurrentValue(), "All");
            assertEquals(stageNameFilter.getCurrentValue(), "All");
            assertEquals(dashboardContent.getReport("Report1", TableReport.class)
                    .getAttributeElementsByRow().size(), 1);
            assertEquals(dashboardContent.getReport("Report2", TableReport.class)
                    .getAttributeElementsByRow().size(), 1);

            groupButton.click();
            Sleeper.sleepTightInSeconds(2);
            assertEquals(dashboardContent.getReport("Report1", TableReport.class)
                    .getAttributeElementsByRow().size(), 2);
            assertEquals(dashboardContent.getReport("Report2", TableReport.class)
                    .getAttributeElementsByRow().size(), 2);

        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void cascadingFilterForAttributeFilter() {
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_STAGE_NAME);
            dashboardEditBar.saveDashboard();

            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            accountFilter.changeAttributeFilterValue("123 Exteriors", "14 West");

            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid")));

            stageNameFilter.changeAttributeFilterValue("Closed Won");
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid")));

            productFilter.openPanel();
            productFilter.getPanel(AttributeFilterPanel.class).showAllAttributes().changeValues("TouchAll");
            assertEquals(accountFilter.getCurrentValue(), "All");
            assertEquals(stageNameFilter.getCurrentValue(), "All");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void cascadingFilterForAttributeFilterWithSingleOption() {
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));

            accountFilter.changeSelectionToOneValue();
            stageNameFilter.changeSelectionToOneValue();
            productFilter.changeSelectionToOneValue();

            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_STAGE_NAME);
            dashboardEditBar.saveDashboard();
            sleepTightInSeconds(2);

            assertEquals(accountFilter.getCurrentValue(), "1000Bulbs.com");
            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), singleton("Closed Lost")));
            stageNameFilter.getRoot().click();
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid")));
            productFilter.getRoot().click();
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void cascadingFilterForDateFilter() {
        makeCopyFromDashboard(DATE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilter(ATTR_QUARTER_YEAR_SNAPSHOT, ATTR_YEAR_SNAPSHOT);
            dashboardEditBar.setParentsFilter(ATTR_MONTH_YEAR_SNAPSHOT, ATTR_QUARTER_YEAR_SNAPSHOT);
            dashboardEditBar.saveDashboard();

            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget yearFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_YEAR_SNAPSHOT));
            FilterWidget quarterFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_QUARTER_YEAR_SNAPSHOT));
            FilterWidget monthFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_MONTH_YEAR_SNAPSHOT));
            yearFilter.changeAttributeFilterValue("1900");

            assertTrue(isEqualCollection(quarterFilter.getAllAttributeValues(), asList("Q1/1900", "Q2/1900",
                    "Q3/1900", "Q4/1900")));
            assertTrue(isEqualCollection(monthFilter.getAllAttributeValues(), asList("Jan 1900", "Feb 1900",
                    "Mar 1900", "Apr 1900", "May 1900", "Jun 1900", "Jul 1900", "Aug 1900", "Sep 1900",
                    "Oct 1900", "Nov 1900", "Dec 1900")));

            quarterFilter.changeAttributeFilterValue("Q1/1900");
            assertTrue(isEqualCollection(monthFilter.getAllAttributeValues(), asList("Jan 1900", "Feb 1900",
                    "Mar 1900")));

            monthFilter.openPanel();
            monthFilter.getPanel(AttributeFilterPanel.class).showAllAttributes().changeValues("Apr 1900");
            assertEquals(yearFilter.getCurrentValue(), "All");
            assertEquals(quarterFilter.getCurrentValue(), "All");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeFilterIsParentManyFilters() {
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.saveDashboard();

            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            accountFilter.changeAttributeFilterValue("123 Exteriors", "14 West");

            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Explorer")));

            stageNameFilter.changeAttributeFilterValue("Closed Won");
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Explorer")));

            assertEquals(accountFilter.getCurrentValue(), "123 Exteriors, 14 West");
            assertEquals(stageNameFilter.getCurrentValue(), "Closed Won");
            assertEquals(productFilter.getCurrentValue(), "All");
            productFilter.changeAttributeFilterValue("CompuSci");
            assertEquals(accountFilter.getCurrentValue(), "123 Exteriors, 14 West");
            assertEquals(stageNameFilter.getCurrentValue(), "Closed Won");
            assertEquals(productFilter.getCurrentValue(), "CompuSci");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testDateFilterIsParentManyFilters() {
        makeCopyFromDashboard(DATE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilter(ATTR_QUARTER_YEAR_SNAPSHOT, ATTR_YEAR_SNAPSHOT);
            dashboardEditBar.setParentsFilter(ATTR_MONTH_YEAR_SNAPSHOT, ATTR_YEAR_SNAPSHOT);
            dashboardEditBar.saveDashboard();

            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget yearFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_YEAR_SNAPSHOT));
            FilterWidget quarterFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_QUARTER_YEAR_SNAPSHOT));
            FilterWidget monthFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_MONTH_YEAR_SNAPSHOT));
            yearFilter.changeAttributeFilterValue("1900");

            assertTrue(isEqualCollection(quarterFilter.getAllAttributeValues(), asList("Q1/1900", "Q2/1900",
                    "Q3/1900", "Q4/1900")));
            assertTrue(isEqualCollection(monthFilter.getAllAttributeValues(), asList("Jan 1900", "Feb 1900",
                    "Mar 1900", "Apr 1900", "May 1900", "Jun 1900", "Jul 1900", "Aug 1900", "Sep 1900",
                    "Oct 1900", "Nov 1900", "Dec 1900")));

            quarterFilter.changeAttributeFilterValue("Q1/1900");
            assertTrue(isEqualCollection(monthFilter.getAllAttributeValues(), asList("Jan 1900", "Feb 1900",
                    "Mar 1900", "Apr 1900", "May 1900", "Jun 1900", "Jul 1900", "Aug 1900", "Sep 1900",
                    "Oct 1900", "Nov 1900", "Dec 1900")));

            assertEquals(yearFilter.getCurrentValue(), "1900");
            assertEquals(quarterFilter.getCurrentValue(), "Q1/1900");
            assertEquals(monthFilter.getCurrentValue(), "All");
            monthFilter.changeAttributeFilterValue("Jan 1900");
            assertEquals(yearFilter.getCurrentValue(), "1900");
            assertEquals(quarterFilter.getCurrentValue(), "Q1/1900");
            assertEquals(monthFilter.getCurrentValue(), "Jan 1900");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testAttributeFilterDoesntBelongCascading() {
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.saveDashboard();

            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            accountFilter.changeAttributeFilterValue("123 Exteriors", "14 West");

            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "TouchAll")));

            stageNameFilter.changeAttributeFilterValue("Closed Won");
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "TouchAll")));
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilterIsChildOfManyFilters() {
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT, ATTR_STAGE_NAME);
            dashboardEditBar.saveDashboard();

            DashboardContent dashboardContent = dashboardsPage.getContent();
            FilterWidget accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            FilterWidget stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            FilterWidget productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            accountFilter.changeAttributeFilterValue("123 Exteriors", "14 West");

            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Explorer")));

            stageNameFilter.changeAttributeFilterValue("Closed Won");
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Explorer")));

            productFilter.openPanel();
            productFilter.getPanel(AttributeFilterPanel.class).showAllAttributes().changeValues("TouchAll");
            assertEquals(accountFilter.getCurrentValue(), "All");
            assertEquals(stageNameFilter.getCurrentValue(), "All");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilterLinkingBetweenTabs() {
        FilterWidget accountFilter;
        FilterWidget stageNameFilter;
        FilterWidget productFilter;
        makeCopyFromDashboard(ATTRIBUTE_TEST_DASHBOARD);

        try {
            DashboardContent dashboardContent = dashboardsPage.getContent();
            dashboardsPage.editDashboard();
            DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_STAGE_NAME);

            dashboardsPage.addNewTab("Tab2");
            accountFilter = addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_ACCOUNT, 
                    DashboardWidgetDirection.UP);

            stageNameFilter = addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_STAGE_NAME, 
                    DashboardWidgetDirection.MIDDLE);

            productFilter = addListAttributeFilterToDashboardAndMoveToRightPlace(ATTR_PRODUCT, 
                    DashboardWidgetDirection.DOWN);

            dashboardEditBar.setParentsFilterUsingDataset(ATTR_STAGE_NAME, ATTR_OPP_SNAPSHOT, ATTR_ACCOUNT);
            dashboardEditBar.setParentsFilterUsingDataset(ATTR_PRODUCT, ATTR_OPP_SNAPSHOT, ATTR_STAGE_NAME);
            dashboardEditBar.saveDashboard();

            dashboardsPage.getTabs().openTab(0);
            stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost", "Interest", "Discovery", "Short List", "Risk Assessment", "Conviction",
                    "Negotiation")));
            productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid", "TouchAll")));

            accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            accountFilter.changeAttributeFilterValue("123 Exteriors", "14 West");

            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid")));

            dashboardsPage.getTabs().openTab(1);
            stageNameFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_STAGE_NAME));
            assertTrue(isEqualCollection(stageNameFilter.getAllAttributeValues(), asList("Closed Won",
                    "Closed Lost")));
            productFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_PRODUCT));
            assertTrue(isEqualCollection(productFilter.getAllAttributeValues(), asList("CompuSci", "Educationly",
                    "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid")));
            accountFilter = dashboardContent.getFilterWidget(simplifyText(ATTR_ACCOUNT));
            assertEquals(accountFilter.getCurrentValue(), "123 Exteriors, 14 West");
        } finally {
            dashboardsPage.deleteDashboard();
        }
    }

    private WebElement addReportToDashboardAndMoveToRightPlace(String reportName, 
            DashboardWidgetDirection dashboardWidgetDirection) {
        dashboardsPage.getDashboardEditBar().addReportToDashboard(reportName);
        WebElement report = dashboardsPage.getContent().getLatestReport(TableReport.class).getRoot();
        dashboardWidgetDirection.moveElementToRightPlace(report);
        return report;
    }

    private FilterWidget addListAttributeFilterToDashboardAndMoveToRightPlace(String attributeName,
            DashboardWidgetDirection dashboardWidgetDirection) {
        dashboardsPage.addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, attributeName);
        FilterWidget filterWidget = dashboardsPage.getContent().getFilterWidget(simplifyText(attributeName));
        WebElement filter = filterWidget.getRoot();
        filter.click();
        dashboardWidgetDirection.moveElementToRightPlace(filter);
        return filterWidget;
    }

    private void makeCopyFromDashboard(String dashboard) {
        initDashboardsPage();

        dashboardsPage.selectDashboard(dashboard);
        dashboardsPage.saveAsDashboard(TMP_DASHBOARD, PermissionType.USE_EXISTING_PERMISSIONS);
        dashboardsPage.selectDashboard(TMP_DASHBOARD);
    }
}
