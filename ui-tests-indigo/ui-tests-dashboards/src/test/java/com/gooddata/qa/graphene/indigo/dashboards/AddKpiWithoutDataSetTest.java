package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;

public class AddKpiWithoutDataSetTest extends AbstractDashboardTest {

    private static final String METRIC_CONNECT_WITH_DATA_SET = "Connected";
    private static final String METRIC_NOT_CONNECT_WITH_DATA_SET = "NotConnected";
    private static final String DATA_SET = "templ:minimalistic";

    @BeforeClass
    public void initProperties() {
        projectTitle = "Add-kpi-without-data-set-test";
    }

    @Test(dependsOnGroups = {"dashboardsInit"}, groups = {"nodate-precondition"})
    public void setupForNoDate()
            throws JSONException, IOException, URISyntaxException {
        setupMaql("/no-date/no-date-maql.txt");
        setupData("/no-date/no-date.csv", "/no-date/upload_info.json");

        String age = getMdService().getObjUri(getProject(), Fact.class, identifier("fact.fact"));
        getMdService().createObj(getProject(), new Metric(METRIC_NOT_CONNECT_WITH_DATA_SET,
                "SELECT SUM([" + age + "])", "#,##0"));
    }

    @Test(dependsOnGroups = {"nodate-precondition"}, groups = {"nodate-test"})
    public void testAddKpiNotConnectedWithDataSet() {
        assertFalse(initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .waitForDashboardLoad()
            .dragAddKpiPlaceholder()
            .getConfigurationPanel()
            .selectMetricByName(METRIC_NOT_CONNECT_WITH_DATA_SET)
            .isDataSetEnabled());

        IndigoDashboardsPage.getInstance(browser).saveEditModeWithWidgets();

        takeScreenshot(browser, "add-kpi-not-connect-with-data-set", getClass());
    }

    @Test(dependsOnGroups = {"nodate-test"}, groups = {"date-precondition"})
    public void setupWithDate()
            throws JSONException, IOException, URISyntaxException {
        setupMaql("/add-date/add-date-maql.txt");
        setupData("/add-date/add-date.csv", "/add-date/upload_info.json");

        getMdService().createObj(getProject(), new Metric(METRIC_CONNECT_WITH_DATA_SET,
                "SELECT 1", "#,##0"));
    }

    @Test(dependsOnGroups = {"date-precondition"}, groups = {"date-test"})
    public void testUpdateKpiConnectedWithDataSet() {

        initIndigoDashboardsPage()
            .waitForDashboardLoad()
            .switchToEditMode()
            .getLastWidget(Kpi.class)
            .clickOnContent();

        IndigoDashboardsPage.getInstance(browser).getConfigurationPanel()
            .selectMetricByName(METRIC_CONNECT_WITH_DATA_SET)
            .selectDataSetByName(DATA_SET);

        IndigoDashboardsPage.getInstance(browser).saveEditModeWithWidgets();
        takeScreenshot(browser, "update-kpi-connect-with-data-set", getClass());
    }
}
