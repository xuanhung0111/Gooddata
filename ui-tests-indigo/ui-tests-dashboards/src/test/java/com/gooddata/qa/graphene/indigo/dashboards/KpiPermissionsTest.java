package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_EOP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import java.io.IOException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

public class KpiPermissionsTest extends AbstractDashboardTest {
    private IndigoRestRequest indigoRestRequest;
    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)),
                testParams.getProjectId());
    }

    @Override
    protected void addUsersWithOtherRolesToProject()
            throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void loginAsEditor() throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);
    }

    @Test(dependsOnMethods = {"loginAsEditor"})
    public void creatingNewKpiDashboard() throws JSONException {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addKpi(
                new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED)
                        .build());

        assertThat(indigoDashboardsPage.isOnEditMode(), is(true));
        assertThat(indigoDashboardsPage.getDashboardTitle(), equalTo("Untitled"));
    }

    @Test(dependsOnMethods = {"loginAsEditor"})
    public void savingNewKpiDashboard() throws IOException, JSONException {
        String dashboardTitle = "Untitled";

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addKpi(
                new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED)
                        .build()).saveEditModeWithWidgets();

        try {
            assertThat(indigoDashboardsPage.isOnEditMode(), is(false));
            assertThat(indigoDashboardsPage.getDashboardTitle(), equalTo(dashboardTitle));
            assertThat(indigoDashboardsPage.getDashboardTitles(), contains(dashboardTitle));
            assertThat(browser.getCurrentUrl().endsWith(indigoRestRequest
                    .getAnalyticalDashboardIdentifier(dashboardTitle)), is(true));
        } finally {
            deleteDashboard(dashboardTitle);
        }
    }

    @Test(dependsOnMethods = {"loginAsEditor"})
    public void savingKpiDashboardAfterChanges() throws IOException, JSONException {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addKpi(
                new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED)
                        .build()).saveEditModeWithWidgets();

        String newTitle = "New magic title";

        try {
            indigoDashboardsPage.switchToEditMode().changeDashboardTitle(newTitle);

            indigoDashboardsPage.selectFirstWidget(Kpi.class);
            indigoDashboardsPage.getConfigurationPanel().selectMetricByName(METRIC_SNAPSHOT_EOP);
            indigoDashboardsPage.waitForWidgetsLoading().saveEditModeWithWidgets();

            assertThat(indigoDashboardsPage.isOnEditMode(), is(false));
            assertThat(indigoDashboardsPage.getDashboardTitles(), contains(newTitle));
            assertThat(indigoDashboardsPage.getDashboardTitle(), equalTo(newTitle));
            assertThat(indigoDashboardsPage.getFirstWidget(Kpi.class).getHeadline(),
                    equalTo(METRIC_SNAPSHOT_EOP));
            assertThat(browser.getCurrentUrl().endsWith(indigoRestRequest
                    .getAnalyticalDashboardIdentifier(newTitle)), is(true));
        } finally {
            indigoRestRequest.deleteDashboardsUsingCascade();
        }
    }

    private void deleteDashboard(String dashboardTitle) throws IOException, JSONException {
        indigoRestRequest.deleteAnalyticalDashboard(indigoRestRequest
                .getAnalyticalDashboardUri(dashboardTitle));
    }

}
