package com.gooddata.qa.graphene.filters;

import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class FiscalDateFilterNameTest extends AbstractDashboardWidgetTest {
    private static final String GOODDATA_DATE_CREATED_DATASET_ID = "created.dataset.dt";
    private static final String BROADCAST_SALARY_DATASET_ID = "salary.broadcast_dataset.dt";

    private final static int year = LocalDate.now().getYear() - 2011;
    private static final String BROADCAST_FISCAL_SALARY_FILTER_TEXT = "DATE (SALARY)";
    private static final String GOODDATA_FISCAL_CREATED_FILTER_NAME = "Date (Created)";
    private static final String GOODDATA_FISCAL_CREATED_FILTER_NEW_NAME = "Date (Created) New Name";
    private static final String GOODDATA_FISCAL_CREATED_FILTER_NEW_TEXT = "DATE (CREATED) NEW NAME";
    private static final String BROADCAST_FISCAL_SALARY_FILTER_NAME = "Date (Salary)";
    private static final String BROADCAST_FISCAL_SALARY_FILTER_NEW_NAME = "Date (Salary) New Name";
    private static final String BROADCAST_FISCAL_SALARY_FILTER_NEW_TEXT = "DATE (SALARY) NEW NAME";

    private static final String PAYMENT_REPORT = "Sum Of Payments Report";
    private static final String PAYMENT_METRIC = "Sum Of Payments Metric";

    private static final List<String> EXPECTED_FILTERS_IN_REPORT = asList(GOODDATA_FISCAL_CREATED_FILTER_NAME,
            BROADCAST_FISCAL_SALARY_FILTER_NAME);

    private static final List<String> FISCAL_ENABLED_DATE_FILTER_VALUES = Arrays.asList(
            "Date (Activity)",
            "Date (Closed)",
            "Date (Created)",
            "Date (Salary)",
            "Date (Snapshot)",
            "Date (Timeline)"
    );

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "fiscal date filter name test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        setupMaql(LdmModel.loadFromFile("/fiscal-date/maql.txt"));

        URL fiscalDateResouce = getClass().getResource("/fiscal-date/upload.zip");
        String webdavURL = uploadFileToWebDav(fiscalDateResouce, null);
        getFileFromWebDav(webdavURL, fiscalDateResouce);
        new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .postEtlPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()));
        new ProjectRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                .setFeatureFlagInProject(ProjectFeatureFlags.FISCAL_CALENDAR_ENABLED, true);

        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.EDITOR);
        createPaymentReport();
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void renameDateDataset() throws IOException, JSONException {
        try {
            changeDatasetTitleByRest(GOODDATA_DATE_CREATED_DATASET_ID, GOODDATA_FISCAL_CREATED_FILTER_NEW_NAME);
            changeDatasetTitleByRest(BROADCAST_SALARY_DATASET_ID, BROADCAST_FISCAL_SALARY_FILTER_NEW_NAME);

            initDashboardsPage().addNewDashboard(generateDashboardName());
            dashboardsPage.addTimeFilterToDashboard(
                    GOODDATA_FISCAL_CREATED_FILTER_NEW_NAME,
                    DateGranularity.YEAR,
                    String.format("%s ago", year)).addTimeFilterToDashboard(
                    BROADCAST_FISCAL_SALARY_FILTER_NEW_NAME,
                    DateGranularity.YEAR,
                    String.format("%s ago", year));

            assertTrue(dashboardsPage.getContent().hasFilterWidget(GOODDATA_FISCAL_CREATED_FILTER_NEW_TEXT),
                    "Dashboard must contain filter: " + GOODDATA_FISCAL_CREATED_FILTER_NEW_NAME);
            assertTrue(dashboardsPage.getContent().hasFilterWidget(BROADCAST_FISCAL_SALARY_FILTER_NEW_TEXT),
                    "Dashboard must contain filter: " + BROADCAST_FISCAL_SALARY_FILTER_NEW_NAME);
        } finally {
            changeDatasetTitleByRest(GOODDATA_DATE_CREATED_DATASET_ID, GOODDATA_FISCAL_CREATED_FILTER_NAME);
            changeDatasetTitleByRest(BROADCAST_SALARY_DATASET_ID, BROADCAST_FISCAL_SALARY_FILTER_NAME);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testDateFilterDropdownListNames() {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        List<String> filterItems = dashboardsPage.editDashboard().getAllDateFilterValues();
        assertEquals(filterItems, FISCAL_ENABLED_DATE_FILTER_VALUES, "Filter values does not match");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterListNamesOnReportFilters() {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        dashboardsPage.addReportToDashboard(PAYMENT_REPORT);

        dashboardsPage
                .addTimeFilterToDashboard(
                        GOODDATA_FISCAL_CREATED_FILTER_NAME,
                        DateGranularity.YEAR,
                        String.format("%s ago", year))
                .addTimeFilterToDashboard(
                        BROADCAST_FISCAL_SALARY_FILTER_NAME,
                        DateGranularity.YEAR,
                        String.format("%s ago", year));

        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
        List<String> filters = tableReport.getAllFilterNames();
        assertEquals(filters, EXPECTED_FILTERS_IN_REPORT,
                "actual filter list in report " + tableReport.getReportTiTle() + " does not match expected");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterNameOnReportTooltip() throws JSONException {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        dashboardsPage.addReportToDashboard(PAYMENT_REPORT);

        dashboardsPage
                .addTimeFilterToDashboard(
                        GOODDATA_FISCAL_CREATED_FILTER_NAME,
                        DateGranularity.YEAR,
                        String.format("%s ago", year))
                .addTimeFilterToDashboard(
                        BROADCAST_FISCAL_SALARY_FILTER_NAME,
                        DateGranularity.YEAR,
                        String.format("%s ago", year)).saveDashboard();

        TableReport tableReport = dashboardsPage.getContent().getLatestReport(TableReport.class).waitForLoaded();
        List<String> filterNames = tableReport.openReportInfoViewPanel().getAllFilterNames();
        assertEquals(filterNames, EXPECTED_FILTERS_IN_REPORT,
                "actual filter list in report " + tableReport.getReportTiTle() + " does not match expected");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilterTitle() {
        initDashboardsPage().addNewDashboard(generateDashboardName());
        dashboardsPage
                .addTimeFilterToDashboard(
                        BROADCAST_FISCAL_SALARY_FILTER_NAME,
                        DateGranularity.YEAR,
                        String.format("%s ago", year));

        FilterWidget widget = dashboardsPage.getContent().getFilterWidgetByName(BROADCAST_FISCAL_SALARY_FILTER_NAME);
        widget.changeTitle(BROADCAST_FISCAL_SALARY_FILTER_TEXT + " new title");
        dashboardsPage.saveDashboard();
        assertEquals(widget.getTitle(), BROADCAST_FISCAL_SALARY_FILTER_TEXT + " NEW TITLE");
    }

    private void createPaymentReport() {
        createMetric(PAYMENT_METRIC,
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title("Payment"))),
                "#,##0.00");

        createReport(new UiReportDefinition()
                        .withName(PAYMENT_REPORT)
                        .withWhats(PAYMENT_METRIC)
                        .withHows("Year (Salary)"),
                PAYMENT_REPORT);
    }

    private void changeDatasetTitleByRest(final String datasetIdentifier, final String newTitle)
            throws IOException, JSONException {
        String datasetUri = getMdService().getObjUri(getProject(), Dataset.class, identifier(datasetIdentifier));
        final CommonRestRequest restRequest = new CommonRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        JSONObject json = restRequest.getJsonObject(datasetUri);

        json.getJSONObject("dataSet").getJSONObject("meta").put("title", newTitle);
        restRequest.executeRequest(RestRequest.initPostRequest(datasetUri + "?mode=edit", json.toString()),
                HttpStatus.OK);
    }
}
