package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.CategoryBucket;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.PersonalInfoDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.scheduleEmail.ScheduleEmailRestRequest;
import com.gooddata.sdk.model.md.Fact;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.sdk.model.md.Restriction.title;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ScheduledEmailsForKPIDashboardsExtendedTest extends AbstractGoodSalesEmailSchedulesTest {

    private static final String INSIGHT_HAS_RESTRICTED_FACT = "Restricted fact";
    private static final String INSIGHT_HAS_XSS = "Metric XSS";
    private String identification;
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private CommonRestRequest commonRestRequest;
    private FactRestRequest factRestRequest;
    private String today;

    @BeforeClass
    public void setUp() {
        identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        attachmentsDirectory =
            new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"schedules"})
    public void signInImapUser() throws JSONException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
    }

    @AfterClass
    public void validateProjectAfterClass() throws JSONException {
        validateAfterClass = false;
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();
        getMetricCreator().createWonMetric();
        getMetricCreator().createBestCaseMetric();
        getMetricCreator().createWinRateMetric();
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_SCHEDULE, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void prepareDashboardWithSpecialCases() {
        createSimpleInsight(REPORT_NO_DATA, METRIC_WON);
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(REPORT_NO_DATA).waitForReportComputing().getMetricsBucket()
            .getMetricConfiguration(METRIC_WON).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CLOSED, "01/01/2019", "01/01/2019");
        analysisPage.saveInsight();

        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(FACT_AMOUNT, FieldType.FACT)
            .addAttribute(ATTR_ACCOUNT).addAttribute(ATTR_OPP_SNAPSHOT).saveInsight(REPORT_TOO_LARGE)
            .waitForReportComputing();

        createSimpleInsight(REPORT_INCOMPUTABLE, METRIC_AMOUNT);
        analysisPage = initAnalysePage();
        analysisPage.openInsight(REPORT_INCOMPUTABLE).addDate().waitForReportComputing().getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CLOSED, "01/01/5000", "01/01/5000");
        analysisPage.saveInsight();

        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        factRestRequest.setFactRestricted(factUri);
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT, FieldType.FACT)
            .waitForReportComputing().saveInsight(INSIGHT_HAS_RESTRICTED_FACT).waitForReportComputing();

        createMetric("metricXSSFormat", format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))),
            MetricFormatterDialog.Formatter.XSS.toString());
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric("metricXSSFormat").saveInsight(INSIGHT_HAS_XSS);
    }

    @Test(dependsOnMethods = "prepareDashboardWithSpecialCases")
    public void scheduleEmailWithWidgetAndInsight() throws IOException, MessagingException{
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().addKpi(new KpiConfiguration.Builder()
            .metric(METRIC_AVG_AMOUNT)
            .dataSet(DATE_DATASET_CREATED)
            .build()).addInsight(REPORT_NO_DATA).addInsightNext(REPORT_TOO_LARGE)
            .addInsightNext(REPORT_INCOMPUTABLE).addInsightNext(INSIGHT_HAS_RESTRICTED_FACT)
            .addInsightNext(INSIGHT_HAS_XSS);

        String nameDashboard = "Dashboard special cases" + identification;

        indigoDashboardsPage.changeDashboardTitle(nameDashboard).saveEditModeWithWidgets();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();

        String expectedResultXSS = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_XSS)
            .getChartReport().getDataLabels().get(0);
        String expectedResultAVG = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AVG_AMOUNT)
            .getValue();
        String expectedResultSumOfAmount = indigoDashboardsPage.getWidgetByHeadline(Insight.class, INSIGHT_HAS_RESTRICTED_FACT)
            .getPivotTableReport().getValueMeasuresPresent().get(0);

        indigoDashboardsPage.scheduleEmailing().submit();

        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String > contents = asList(getFirstPdfContentFrom(
            waitForScheduleMessages(nameDashboard, 1).get(0)).split("\n"));

        today = DateRange.getCurrentDate();
        log.info("Content Special Cases" + contents);

        assertThat(contents, hasItems("No data for your filter selection", expectedResultXSS, today,
            "Sum of " + METRIC_AMOUNT, expectedResultSumOfAmount, "Incomputable report",
            "SORRY, WE CAN'T DISPLAY THIS INSIGHT", "Contact your administrator.", "Too large report", "No Data Report",
            expectedResultAVG));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void deleteScheduleEmail() throws IOException, MessagingException{
        try {
            String nameDashboard = "Dashboard Delete";
            String userDeleted = createAndAddUserToProject(UserRoles.ADMIN);
            logout();
            signInAtGreyPages(userDeleted, testParams.getPassword());

            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
            addKPIToDashboard(indigoDashboardsPage, nameDashboard);
            String expectedResult = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AVG_AMOUNT).getValue();

            indigoDashboardsPage.scheduleEmailing().submit();
            indigoDashboardsPage.scheduleEmailing().submit();
            EmailSchedulePage emailSchedulePage = initEmailSchedulesPage();
            assertEquals(emailSchedulePage.getKPIPrivateScheduleTitles().size(), 2);
            assertTrue(emailSchedulePage.getKPIPrivateScheduleTitles().get(0).getAttribute("title")
                .contains(nameDashboard));

            initIndigoDashboardsPage().switchToEditMode().deleteDashboardOnMenuItem(true);

            initEmailSchedulesPage();
            takeScreenshot(browser, "Delete Schedule Email", getClass());
            assertEquals(emailSchedulePage.getKPIPrivateScheduleTitles().size(), 0);

            initIndigoDashboardsPage();
            addKPIToDashboard(indigoDashboardsPage, nameDashboard);
            indigoDashboardsPage.scheduleEmailing().submit();

            initEmailSchedulesPage().deleteKPISchedule(nameDashboard);
            assertEquals(emailSchedulePage.getKPIPrivateScheduleTitles().size(), 0);

            String nameDashboardSchedule = "Dashboard Delete" + identification;
            initIndigoDashboardsPage();
            addKPIToDashboard(indigoDashboardsPage, nameDashboardSchedule);
            indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
            indigoDashboardsPage.scheduleEmailing().addRecipientToField(singletonList(imapUser)).submit();

            PersonalInfoDialog personalInfoDialog = initAccountPage().openPersonalInfoDialog();
            if (personalInfoDialog.getEmail().equals(userDeleted) &&
                !personalInfoDialog.getEmail().equals(testParams.getDomainUser())) {

                log.info("Deleted User Editor " + userDeleted);
                initAccountPage().deleteAccount();
            }

            updateRecurrencyString(commonRestRequest.getLastScheduleUri());
            List<String > contents = asList(getFirstPdfContentFrom(
                waitForScheduleMessages(nameDashboardSchedule, 1).get(0)).split("\n"));

            today = DateRange.getCurrentDate();
            assertThat(contents, hasItems(METRIC_AVG_AMOUNT, today, "Page 1/1", expectedResult));
        } finally {
            signInAtGreyPages(imapUser, imapPassword);
        }
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void scheduleEmailWithBulletChart() throws IOException, MessagingException{
        String nameInsight = "Bullet Chart";
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(nameInsight, ReportType.BULLET_CHART)
                .setMeasureBucket(asList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(METRIC_AMOUNT_BOP)),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_AMOUNT), MeasureBucket.Type.SECONDARY_MEASURES),
                    MeasureBucket.createMeasureBucket(getMetricByTitle(METRIC_BEST_CASE), MeasureBucket.Type.TERTIARY_MEASURES)))
                .setCategoryBucket(asList(
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_DEPARTMENT),
                        CategoryBucket.Type.VIEW),
                    CategoryBucket.createCategoryBucket(getAttributeByTitle(ATTR_FORECAST_CATEGORY),
                        CategoryBucket.Type.VIEW))));

        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        indigoDashboardsPage.addDashboard().addInsight(nameInsight);

        String nameDashboard = "Dashboard Bullet Chart" + identification;

        indigoDashboardsPage.changeDashboardTitle(nameDashboard).saveEditModeWithWidgets();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();

        indigoDashboardsPage.scheduleEmailing().submit();

        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String > contents = asList(getFirstPdfContentFrom(
            waitForScheduleMessages(nameDashboard, 1).get(0)).split("\n"));

        log.info("Content Dashboard Bullet Chart: " + contents);
        String legendsOfContent = METRIC_AMOUNT_BOP + " " + METRIC_AMOUNT + " " + METRIC_BEST_CASE;
        assertThat(contents, hasItems(legendsOfContent, "Direct Sales", "Include", "Exclude", nameInsight));
    }

    protected ScheduleEmailRestRequest initScheduleEmailRestRequest() {
        return new ScheduleEmailRestRequest(new RestClient(
            new RestClient.RestProfile(testParams.getHost(), imapUser, imapPassword, true)), testParams.getProjectId());
    }

    private void createSimpleInsight(String title, String metric) {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(title, ReportType.TABLE)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }

    private void addKPIToDashboard(IndigoDashboardsPage indigoDashboardsPage, String nameDashboard) {
        indigoDashboardsPage.addDashboard().addKpi(new KpiConfiguration.Builder()
            .metric(METRIC_AVG_AMOUNT)
            .dataSet(DATE_DATASET_CREATED)
            .build()).changeDashboardTitle(nameDashboard).saveEditModeWithWidgets();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
    }
}
