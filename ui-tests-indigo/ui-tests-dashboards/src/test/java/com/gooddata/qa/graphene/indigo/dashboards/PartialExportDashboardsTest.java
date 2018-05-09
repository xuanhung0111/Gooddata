package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static java.util.Objects.nonNull;

import java.io.IOException;

import com.gooddata.fixture.ResourceManagement.*;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.utils.http.indigo.IndigoRestUtils;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertTrue;

public class PartialExportDashboardsTest extends GoodSalesAbstractTest {

    private String TEST_DASHBOARD = "Test Dashboard";
    private String SECOND_TAB = "Second Tab";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle += "Dashboard-Partial-Export-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();

        Dashboard testDashboard = Builder.of(Dashboard::new).with(dashboard -> {
            dashboard.setName(TEST_DASHBOARD);
            dashboard.addTab(Builder.of(Tab::new).build()); // empty tab named First Tab
            dashboard.addTab(Builder.of(Tab::new).with(tab -> {
                        tab.setTitle(SECOND_TAB);
                        tab.addItem(Builder.of(ReportItem::new)
                                .with(item -> {
                                    item.setObjUri(getReportCreator().createTop5OpenByCashReport());
                                    item.setPosition(TabItem.ItemPosition.LEFT);
                                }).build());
                    }
            ).build());
        }).build();

        new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId())
                .createDashboard(testDashboard.getMdObject());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void createKpiLinkToDashboardTab() {
        initIndigoDashboardsPage()
                .getSplashScreen()
                .startEditingWidgets()
                .waitForDashboardLoad()
                .addKpi(new KpiConfiguration.Builder()
                        .metric(METRIC_AMOUNT)
                        .dataSet(DATE_DATASET_CLOSED)
                        .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
                        .drillTo(SECOND_TAB)
                        .build())
                .saveEditModeWithWidgets();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void exportDashboardsToAnotherProject() throws JSONException, IOException {
        String oldPid = testParams.getProjectId();
        String token = exportPartialProject(IndigoRestUtils
                .getAnalyticalDashboards(getRestApiClient(), oldPid).get(0), DEFAULT_PROJECT_CHECK_LIMIT);

        String newPid = createProjectUsingFixture("Copy of " + projectTitle, ResourceTemplate.GOODSALES);
        testParams.setProjectId(newPid);
        try {
            importPartialProject(token, DEFAULT_PROJECT_CHECK_LIMIT);
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "Imported dashboard", getClass());

            IndigoDashboardsPage.getInstance(browser).getLastWidget(Kpi.class).clickKpiValue();
            waitForDashboardPageLoaded(browser);
            takeScreenshot(browser, "Dashboard tab: " + SECOND_TAB + " is selected", getClass());
            assertTrue(dashboardsPage.getTabs().isTabSelected(1));
        } finally {
            testParams.setProjectId(oldPid);
            if (nonNull(newPid)) {
                deleteProject(newPid);
            }
        }
    }
}
