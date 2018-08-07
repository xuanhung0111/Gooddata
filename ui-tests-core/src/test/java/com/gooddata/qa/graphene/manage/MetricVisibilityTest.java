package com.gooddata.qa.graphene.manage;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricDetailsPage;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_LOST;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_STAGE_NAME;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class MetricVisibilityTest extends GoodSalesAbstractTest {

    private CommonRestRequest commonRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfOpenOppsMetric();
        getMetricCreator().createWonMetric();
        commonRestRequest = new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    @DataProvider(name = "userRole")
    public Object[][] getUserRole() throws IOException {
        final JSONObject userProfile = new UserManagementRestRequest(
                new RestClient(getProfile(DOMAIN)), testParams.getProjectId())
                .getUserProfileByEmail(testParams.getUserDomain(), testParams.getUser());
        String fullName = userProfile.getString("firstName") + " " + userProfile.getString("lastName");
        return new Object[][] {
                {UserRoles.ADMIN, format("Only the owner (%s) and people with the link can navigate to this metric.",
                        fullName)},
                {UserRoles.EDITOR, format("Only the owner (%s) and people with the link can navigate to this metric.\n" +
                        "Admins and Editors can edit.", fullName)}
        };
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "userRole")
    public void testMetricVisibilityOnDetailMetricPage(UserRoles userRoles, String infoVisibilitySection) {
        getMetricCreator().createAmountMetric();
        logoutAndLoginAs(true, userRoles);
        try {
            MetricDetailsPage metricDetailsPage = initMetricPage().openMetricDetailPage(METRIC_AMOUNT);
            PermissionSettingDialog permissionSettingDialog = metricDetailsPage.openPermissionSettingDialog().setVisibility(true);
            assertEquals(permissionSettingDialog.getRowInfoVisibility(),
                    "Everyone can find this metric and use all metrics it contains");
            assertEquals(permissionSettingDialog.getToolTipFromVisibilityQuestionIcon(),
                    "If you decide to hide this metric again, metrics it uses will remain public unless hidden individually.");
            permissionSettingDialog.cancel();

            assertEquals(metricDetailsPage.openPermissionSettingDialog().setVisibility(false).getRowInfoVisibility(),
                    infoVisibilitySection);
            permissionSettingDialog.cancel();

            assertTrue(metricDetailsPage.setVisibility(false).isPrivateMetric(), "Eye icon should display");
            assertEquals(metricDetailsPage.getTooltipFromEyeIcon(), "Only people who have a link can see this metric.");

            metricDetailsPage.clickEyeIcon().setVisibility(true).save();
            Graphene.waitGui().until(browser -> !metricDetailsPage.isPrivateMetric());
            assertFalse(metricDetailsPage.isPrivateMetric(), "Eye icon shouldn't display");
            assertTrue(initMetricPage().isMetricVisible(METRIC_AMOUNT), "Metric should display");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void viewPrivateMetricTest() {
        getMetricCreator().createAmountMetric();
        try {
            MetricPage metricPage = initMetricPage();
            MetricDetailsPage metricDetailsPage = metricPage.openMetricDetailPage(METRIC_AMOUNT);
            metricDetailsPage.setVisibility(false);
            logoutAndLoginAs(true, UserRoles.EDITOR);
            initMetricPage();
            assertFalse(metricPage.isMetricVisible(METRIC_AMOUNT), "Private metric shouldn't display");
            assertTrue(metricPage.isMetricVisible(METRIC_NUMBER_OF_OPEN_OPPS), "Public metric should display");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testMetricVisibilityOnDataPage() {
        getMetricCreator().createAmountMetric();
        try {
            MetricPage metricPage = initMetricPage();
            PermissionSettingDialog permissionSettingDialog = metricPage.openPermissionSettingDialogFor(METRIC_AMOUNT);
            assertTrue(permissionSettingDialog.isEditPermissionSectionVisible(), "Edit permission section should display");
            permissionSettingDialog.cancel();

            metricPage.setVisibility(false, METRIC_AMOUNT);
            assertTrue(metricPage.isPrivateMetric(METRIC_AMOUNT), "Eye icon should display beside user name");

            metricPage.setVisibility(true, METRIC_AMOUNT);
            assertFalse(metricPage.isPrivateMetric(METRIC_AMOUNT), "Eye icon shouldn't display beside user name");
        } finally {
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void testMetricsVisibilityOnDataPage() {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createLostMetric();
        try {
            MetricPage metricPage = initMetricPage();
            PermissionSettingDialog permissionSettingDialog = metricPage.openPermissionSettingDialogFor(METRIC_AMOUNT, METRIC_LOST);
            assertTrue(permissionSettingDialog.isEditPermissionSectionVisible(),
                    "Edit permission section should display");
            assertEquals(permissionSettingDialog.getHeaderTitle(), "Change permissions for 2 selected metrics");
            assertEquals(permissionSettingDialog.getAffectedElementInfo(), "2 metrics");
            permissionSettingDialog.cancel();

            metricPage.setVisibility(false, METRIC_AMOUNT, METRIC_LOST);
            assertTrue(metricPage.isPrivateMetric(METRIC_AMOUNT), "Eye icon should display beside user name");
            assertTrue(metricPage.isPrivateMetric(METRIC_LOST), "Eye icon should display beside user name");

            metricPage.setVisibility(true, METRIC_AMOUNT, METRIC_LOST);
            assertFalse(metricPage.isPrivateMetric(METRIC_AMOUNT), "Eye icon shouldn't display beside user name");
            assertFalse(metricPage.isPrivateMetric(METRIC_LOST), "Eye icon shouldn't display beside user name");

            logoutAndLoginAs(true, UserRoles.EDITOR);
            assertFalse(initMetricPage().openPermissionSettingDialogFor(METRIC_AMOUNT).isEditPermissionSectionVisible(),
                    "Edit permission section shouldn't display");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_LOST).getUri());
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void hidePrivateMetricFromAdvancedMetric() {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createLostMetric();
        try {
        initMetricPage().setVisibility(false, METRIC_AMOUNT, METRIC_LOST);
        assertEquals(initReportCreation().openWhatPanel().clickAddAdvanceMetric().clickShareMetricLink()
                .getPrivateElementValues(), asList(METRIC_AMOUNT, METRIC_LOST));
        logoutAndLoginAs(true, UserRoles.EDITOR);
        assertFalse(initReportCreation().openWhatPanel().clickAddAdvanceMetric().clickShareMetricLink()
                .getElementValues().containsAll(asList(METRIC_AMOUNT, METRIC_LOST)));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_LOST).getUri());
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void affectReportToPrivateMetric() {
        getMetricCreator().createAmountMetric();
        initMetricPage().setVisibility(false, METRIC_AMOUNT);
        initReportCreation()
                .createReport(new UiReportDefinition().withName(REPORT_AMOUNT_BY_STAGE_NAME).withWhats(METRIC_AMOUNT));
        try {
            initReportsPage().setVisibility(true, REPORT_AMOUNT_BY_STAGE_NAME);
            assertFalse(initMetricPage().isPrivateMetric(METRIC_AMOUNT));
        } finally {
            commonRestRequest.deleteObjectsUsingCascade(getReportByTitle(REPORT_AMOUNT_BY_STAGE_NAME).getUri());
            commonRestRequest.deleteObjectsUsingCascade(getMetricByTitle(METRIC_AMOUNT).getUri());
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void affectDashboardToPrivateMetricAndReport() {
        getReportCreator().createAmountByStageNameReport();
        try {
            initReportsPage().setVisibility(false, REPORT_AMOUNT_BY_STAGE_NAME);
            initDashboardsPage().addReportToDashboard(REPORT_AMOUNT_BY_STAGE_NAME).saveDashboard();
            assertFalse(initReportsPage().isPrivateReport(REPORT_AMOUNT_BY_STAGE_NAME));
            assertFalse(initMetricPage().isPrivateMetric(METRIC_AMOUNT));
        } finally {
            commonRestRequest.deleteObjectsUsingCascade(getReportByTitle(REPORT_AMOUNT_BY_STAGE_NAME).getUri());
        }
    }
}
