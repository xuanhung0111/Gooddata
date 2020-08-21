package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import com.gooddata.qa.graphene.utils.GoodSalesUtils;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ExportKPIDashboardsToPDF extends AbstractDashboardTest {

    private final String KD_HAS_ONLY_KPI_WIDGETS = "Only KPI";
    private final String KD_HAS_INVALID_DATA = "Invalid Data";
    private final String KD_HAS_TOO_LARGE = "KD has long nameKD has long nameKD has long nameKD has long name";
    private final String KD_HAS_PROTECTED_ATTRIBUTE_RESTRICTED_FACT = "Protected Restricted";
    private final String KD_HAS_NO_DATA = "No Data KD";
    private final String KD_HAS_XSS_SPECIAL_UNICODE = "Special Unicode";
    private final String KD_HAS_HEADER = "KPI header";
    private final String KD_HAS_FILTER = "KD Filter";
    private final String KD_ALL_ROLES = "KPI all roles";


    private String today;
    private KpiConfiguration kpiAmount;
    private FactRestRequest factRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();
        getMetricCreator().createWonMetric();

        today = DateRange.getCurrentDate();

        kpiAmount = new KpiConfiguration.Builder().metric(METRIC_AMOUNT).dataSet(DATE_DATASET_CREATED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString()).build();
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
        projectRestRequest.setFeatureFlagInProject(ProjectFeatureFlags.ENABLE_TABLE_COLUMNS_GROW_TO_FIT, false);
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.EDITOR_AND_INVITATIONS);
        createAndAddUserToProject(UserRoles.EDITOR_AND_USER_ADMIN);
        createAndAddUserToProject(UserRoles.EXPLORER);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnMethods = "cannotExportKPIsDashboard")
    public void prepareInsights() {
        String insightNoData = "Insight No Data";
        String date = "01/01/2019";
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightNoData, ReportType.TABLE)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_WON)))));

        AnalysisPage analysisPage = initAnalysePage().openInsight(insightNoData).waitForReportComputing();
        analysisPage.getMetricsBucket().getMetricConfiguration(METRIC_WON).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CLOSED, date, date);
        analysisPage.saveInsight();
        saveDashboard(insightNoData, KD_HAS_NO_DATA);

        String insightTooLarge = "Insight" + generateHashString();
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightTooLarge, ReportType.TABLE)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT))))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_ACCOUNT),
                        CategoryBucket.Type.ATTRIBUTE),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_OPP_SNAPSHOT),
                        CategoryBucket.Type.ATTRIBUTE))));

        initIndigoDashboardsPage().addDashboard().addInsight(insightTooLarge).changeDashboardTitle(KD_HAS_TOO_LARGE)
            .saveEditModeWithWidgets();

        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(KD_HAS_INVALID_DATA)
            .addKpi(kpiAmount);

        indigoDashboardsPage.openExtendedDateFilterPanel().selectStaticPeriod("01/01/5000", "01/01/5000").apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        indigoDashboardsPage.addDashboard().changeDashboardTitle(KD_HAS_ONLY_KPI_WIDGETS)
            .openExtendedDateFilterPanel()
            .selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.addKpi(kpiAmount).saveEditModeWithWidgets();

        String insightXSS = "Insight XSS";

        final String specialCharacters = "@#$%^&*()âêûťžŠô";
        final String longName = "Metric has long name which is used to test shorten be applied Metric has " +
            "long name which is used to test shorten be applied Metric has long name which is used to test shorten be applied";
        String unicodeName = "Tiếng Việt ພາສາລາວ résumé";
        final String xssFormatMetricName = "<button>" + METRIC_PERCENT_OF_GOAL + "</button>";
        final String xssFormatMetricMaQL = "SELECT 1";
        final Metric xssFormatMetric = getMdService().createObj(getProject(),
            new Metric(xssFormatMetricName, xssFormatMetricMaQL, "<button>#,##0.00</button>"));

        createMetric(specialCharacters,"SELECT 1", "#,##0");
        createMetric(longName,"SELECT 1", "#,##0");
        createMetric(unicodeName,"SELECT 1", "#,##0");

        indigoRestRequest.createInsight(
            new InsightMDConfiguration(insightXSS, ReportType.TABLE)
                .setMeasureBucket(
                    asList(MeasureBucket.createSimpleMeasureBucket(xssFormatMetric),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(specialCharacters)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(longName)),
                        MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(unicodeName)))));

        saveDashboard(insightXSS, KD_HAS_XSS_SPECIAL_UNICODE);

        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(KD_HAS_HEADER).addKpi(kpiAmount)
            .getWidgetByHeadline(Kpi.class, METRIC_AMOUNT);
        indigoDashboardsPage.getRows().get(0).getRowHeader().changeDashboardRowTitle("Title", true)
            .changeDashboardRowDescription("Description", true);
        indigoDashboardsPage.saveEditModeWithWidgets();

        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(KD_HAS_FILTER).addKpi(kpiAmount)
            .addAttributeFilter(ATTR_DEPARTMENT, "Inside Sales")
            .openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
        indigoDashboardsPage.saveEditModeWithWidgets();

        initIndigoDashboardsPage().addDashboard().addKpi(kpiAmount).changeDashboardTitle(KD_ALL_ROLES)
            .saveEditModeWithWidgets().waitForWidgetsLoading();
    }

    @Test(dependsOnGroups = "createProject")
    public void cannotExportKPIsDashboard() {
        assertFalse(initIndigoDashboardsPage().isHeaderOptionsButtonVisible(),
            "Export KD option should be not present");
        assertFalse(initIndigoDashboardsPage().addDashboard().isHeaderOptionsButtonVisible(),
            "Export KD option should be not present");
    }

    @Test(dependsOnMethods = "prepareInsights")
    public void cannotExportWithViewerDisabledExportRole() throws IOException{
        try {
            createAndAddUserToProject(UserRoles.VIEWER_DISABLED_EXPORT);
            logoutAndLoginAs(false, UserRoles.VIEWER_DISABLED_EXPORT);
            assertFalse(initIndigoDashboardsPage().openHeaderOptionsButton().isPDFExportItemVisible());
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnMethods = {"prepareInsights"}, dataProvider = "getWidgetNameAndExpectedDataProvider")
    public void exportKPIsDashboardContainInvalidData(String nameWidget, List<String> expectedResults) {
        String insightUri = indigoRestRequest.getAnalyticalDashboardUri(nameWidget);

        try {
            initIndigoDashboardsPage().selectKpiDashboard(nameWidget).waitForWidgetsLoading()
                .exportDashboardToPDF();

            List<String> contents = asList(getContentFrom(nameWidget).split("\n"));
            takeScreenshot(browser, nameWidget, getClass());
            log.info(nameWidget + contents.toString());

            assertEquals(contents, expectedResults);
        } finally {
            indigoRestRequest.deleteAnalyticalDashboard(insightUri);
        }
    }

    @DataProvider(name = "getWidgetNameAndExpectedDataProvider")
    public Object[][] getWidgetNameAndExpectedDataProvider() {
        return new Object[][]{
            { KD_HAS_ONLY_KPI_WIDGETS,
                asList("$116,625,456.54", METRIC_AMOUNT, KD_HAS_ONLY_KPI_WIDGETS + " " + today, "Page 1/1") },
            { KD_HAS_INVALID_DATA,
                asList("Error", METRIC_AMOUNT, KD_HAS_INVALID_DATA + " " + today, "Page 1/1") },
            { KD_HAS_NO_DATA,
                asList("–", "No data for your filter selection", "Insight No Data", KD_HAS_NO_DATA + " " + today,
                    "Page 1/1") },
            { KD_HAS_XSS_SPECIAL_UNICODE,
                asList("<button>% of Goal</button> @#$%^&*()âêûťžŠô Metric has long name which is" +
                                " used to test shorten be applied Metric has long name w… Tiếng Việt ພາສາລາວ résumé",
                    "<button>1.00</button> 1 1 1", "Insight XSS", KD_HAS_XSS_SPECIAL_UNICODE + " " + today, "Page 1/1") },
            { KD_HAS_HEADER,
                asList("Title", "Description", "–", METRIC_AMOUNT, KD_HAS_HEADER + " " + today, "Page 1/1") },
            { KD_HAS_FILTER,
                asList("$36,219,131.58", METRIC_AMOUNT, KD_HAS_FILTER + " " + today, "Page 1/1") }
        };
    }

    @Test(dependsOnMethods = {"prepareInsights"}, dataProvider = "getRolesUser")
    public void exportKPIsDashboardContainProtectedRestrictedData(UserRoles roles) {
        String insightProtectedRestricted = "Protected Restricted Insight";

        String factUri = factRestRequest.getFactByTitle(GoodSalesUtils.METRIC_AMOUNT).getUri();
        String attributeUri = factRestRequest.getAttributeByTitle(ATTR_STAGE_NAME).getUri();
        try {
            factRestRequest.setFactProtected(attributeUri);
            factRestRequest.setFactRestricted(factUri);
            initAnalysePage().changeReportType(ReportType.TABLE).addMetric(GoodSalesUtils.METRIC_AMOUNT, FieldType.FACT)
                .addAttribute(ATTR_STAGE_NAME).saveInsight(insightProtectedRestricted);

            saveDashboard(insightProtectedRestricted, KD_HAS_PROTECTED_ATTRIBUTE_RESTRICTED_FACT);

            logoutAndLoginAs(true, roles);

            initIndigoDashboardsPage().selectKpiDashboard(KD_HAS_PROTECTED_ATTRIBUTE_RESTRICTED_FACT)
                .openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
            indigoDashboardsPage.exportDashboardToPDF();

            List<String> contents = asList(getContentFrom(KD_HAS_PROTECTED_ATTRIBUTE_RESTRICTED_FACT).split("\n"));
            takeScreenshot(browser, roles.toString(), getClass());
            log.info(KD_HAS_PROTECTED_ATTRIBUTE_RESTRICTED_FACT + contents.toString());

            assertEquals(contents, asList(ATTR_STAGE_NAME, "", METRIC_SUM_OF_AMOUNT, "Interest 770,636,605.83",
                "Discovery 263,228,715.00", "Short List 288,774,598.20", "Risk Assessment 168,395,155.55",
                "Conviction 139,230,134.43", "Negotiation 88,231,566.30", "Closed Won 1,868,538,549.36",
                "Closed Lost 2,030,878,384.05", "Protected Restricted Insight", "Protected Restricted " + today,
                "Page 1/1"));
        } finally {
            factRestRequest.unsetFactProtected(attributeUri);
            factRestRequest.unsetFactRestricted(factUri);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "getRolesUser")
    public Object[][] getRolesUser() {
        return new Object[][]{
            {UserRoles.ADMIN},
            {UserRoles.EDITOR},
            {UserRoles.VIEWER}
        };
    }

    @Test(dependsOnMethods = {"prepareInsights"}, dataProvider = "getAllRolesUser")
    public void exportKPIsDashboardWithAllRoles(UserRoles roles) {
        logoutAndLoginAs(true, roles);
        try {
            initIndigoDashboardsPage().selectKpiDashboard(KD_ALL_ROLES).exportDashboardToPDF();

            List<String> contents = asList(getContentFrom("KPI all roles").split("\n"));
            takeScreenshot(browser, roles.toString(), getClass());
            log.info("KPI all roles" + contents.toString());

            assertThat(contents, hasItems(METRIC_AMOUNT, "KPI all roles " + today));
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "getAllRolesUser")
    public Object[][] getAllRolesUser() {
        return new Object[][]{
            {UserRoles.ADMIN},
            {UserRoles.EDITOR},
            {UserRoles.EDITOR_AND_INVITATIONS},
            {UserRoles.EDITOR_AND_USER_ADMIN},
            {UserRoles.EXPLORER},
            {UserRoles.VIEWER},
        };
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void exportEmbeddedKPIsDashboard() throws IOException{
        try {
            createAndAddUserToProject(UserRoles.DASHBOARD_ONLY);
            logoutAndLoginAs(false, UserRoles.DASHBOARD_ONLY);

            initEmbeddedIndigoDashboardPageByType(EmbeddedType.URL).exportDashboardToPDF();

            List<String> contents = asList(getContentFrom(KD_HAS_INVALID_DATA).split("\n"));
            takeScreenshot(browser, KD_HAS_INVALID_DATA, getClass());
            log.info(KD_HAS_INVALID_DATA + contents.toString());

            assertThat(contents, hasItems(METRIC_AMOUNT));
        } finally {
            logoutAndLoginAs(false, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportKPIsDashboardIsHiddenWhenTurnOffFF() {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_EXPORT_PDF, false);
            assertFalse(initIndigoDashboardsPage().openHeaderOptionsButton().isPDFExportItemVisible(),
                "Should be not there is Export PDF button into “Header Option” button");
        } finally {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_EXPORT_PDF, true);
        }
    }

    @Test(dependsOnGroups = "createProject", dataProvider = "getSpecialNameKPIDashboard")
    public void exportKPIsDashboardContainSpecialNames(String specialName, String expectedName) {
        initIndigoDashboardsPage().addDashboard().changeDashboardTitle(specialName)
            .addKpi(kpiAmount).saveEditModeWithWidgets()
            .exportDashboardToPDF();

        List<String> contents = asList(getContentFrom(expectedName).split("\n"));
        assertThat(contents, hasItems(METRIC_AMOUNT));
    }

    @DataProvider(name = "getSpecialNameKPIDashboard")
    public Object[][] getSpecialNameKPIDashboard() {
        return new Object[][]{
            {"<button>' Public DB3# .\\ ,\"Widgets!;</button>", "_button_' Public DB3# __ __Widgets!;__button_"},
            {"@#$%^&*()", "@#$%^&_()"},
            {"has long name more 50 characterhas long name more 50 characterhas long name more 50 character",
                "has long name more 50 characterhas long name more "}
        };
    }

    @Test(dependsOnMethods = {"prepareInsights"})
    public void exportAndImportProject() {
        String exportToken = exportProject(true, true, true, DEFAULT_PROJECT_CHECK_LIMIT);
        String workingProjectId = testParams.getProjectId();
        String targetProjectId = createNewEmptyProject("TARGET_PROJECT_TITLE");

        testParams.setProjectId(targetProjectId);
        try {
            importProject(exportToken, DEFAULT_PROJECT_CHECK_LIMIT);
            initIndigoDashboardsPageWithWidgets().selectKpiDashboard(KD_HAS_INVALID_DATA).waitForWidgetsLoading()
                .exportDashboardToPDF();
            List<String> contents = asList(getContentFrom(KD_HAS_INVALID_DATA).split("\n"));
            assertThat(contents, hasItems("Error", METRIC_AMOUNT, KD_HAS_INVALID_DATA + " " + today, "Page 1/1"));
        } finally {
            testParams.setProjectId(workingProjectId);
        }
    }

    private void saveDashboard(String data, String nameDashboard) {
        initIndigoDashboardsPage().addDashboard().addInsight(data).changeDashboardTitle(nameDashboard)
            .saveEditModeWithWidgets();
    }
}
