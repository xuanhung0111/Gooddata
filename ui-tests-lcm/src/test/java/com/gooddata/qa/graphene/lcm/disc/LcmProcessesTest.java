package com.gooddata.qa.graphene.lcm.disc;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.LcmDirectoryConfiguration;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisInsightSelectionPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.GeoPushpinChartPicker;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.json.JSONException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.deleteSegmentDefault;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

public class LcmProcessesTest extends AbstractDataloadProcessTest {

    public DataloadScheduleDetail scheduleDetail;
    public CreateScheduleForm scheduleForm;
    private final String LCM_RELEASE_PROCESS = "LCM_RELEASE_" + generateHashString();
    private final String LCM_ROLLOUT_PROCESS = "LCM_ROLLOUT_" + generateHashString();
    private final String LCM_PROVISIONING_PROCESS = "LCM_PROVISIONING_" + generateHashString();
    private final String MASTER_NAME_PROJECT = "ATT_Master_Of_Segment_" + generateHashString();
    private final String CLIENT_PROJECT = "LcmClientProject_" + generateHashString();
    private final String SEGMENT_ID = "ATTSegmentProject_" + generateHashString();
    private static String END_CODE_PARAM;
    private static String END_CODE_HIDDEN_PARAM;
    private static String DEVELOPMENT_PID;
    private static String DEVELOPMENT_TITLE;
    private static String ADS_WAREHOUSE;
    private static final String INSIGHT_LCM = "Insight-LCM";
    private static final String RELEASE_SCHEDULE_NAME = "Release_Schedule";
    private final String CLIENT_NAME = "LcmClient" + generateHashString();
    private GeoPushpinChartPicker geoChart;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_LCM_TEST_UI";
        useDynamicUser = false;
    }

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException {
        if (BrowserUtils.isFirefox()) {
            throw new SkipException("Skip test case on Firefox Browser due to disabled weblg ");
        }

        String adsTableText = SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_TABLE.getName())
                .replace("${SEGMENT_ID}", SEGMENT_ID )
                .replace("${CLIENT_NAME}", CLIENT_NAME )
                .replace("${CLIENT_PROJECT}",  CLIENT_PROJECT)
                .replace("${TOKEN_ID}", testParams.getAuthorizationToken());

        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));
        Parameters parameters = defaultParameters.get().addParameter(Parameter.SQL_QUERY, adsTableText);
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE, parameters);
    }

    @Test(dependsOnMethods = {"initData"}, groups = {"precondition"})
    public void configAttributeToGeoPushpin() {
        initAttributePage().initAttribute("city").selectGeoLableType("Geo pushpin");
    }

    @Test(dependsOnMethods = {"configAttributeToGeoPushpin"}, groups = {"precondition"})
    public void runAddLoadingData() throws JSONException {
        initDiscProjectDetailPage();
        ADS_WAREHOUSE = ads.getConnectionUrl();
        DEVELOPMENT_PID = testParams.getProjectId();
        DEVELOPMENT_TITLE = projectTitle;
        createAddLoadScheduleProcess(ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initAnalysePage().changeReportType(ReportType.GEO_CHART)
                .addAttributeToLocationPushpin(ATTR_GEO_PUSHPIN, FieldType.GEO)
                .addAttributeToMeasureSize(ATTR_AMOUNT, FieldType.FACT)
                .addAttributeToMeasureColor(ATTR_AMOUNT, FieldType.FACT)
                .addStack(ATTR_COUNTRY).waitForReportComputing().saveInsight(INSIGHT_LCM);
    }

    @Test(dependsOnMethods = {"runAddLoadingData"}, groups = {"precondition"})
    public void runLcmReleaseProcess() throws Throwable {
        createProject();
        END_CODE_HIDDEN_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() +
                TxtFile.GD_ENDCODE_PARAM_RELEASE.getName());
        initDiscProjectDetailPage();
        sleepTightInSeconds(2); // wait by Loading Drop DownList value selected
        projectDetailPage.clickDeployButton();
        createDeployProcess(ProcessType.LCM_RELEASE, LCM_RELEASE_PROCESS);
        initDiscProjectDetailPage();
        createAndRunReleaseProcess(LCM_RELEASE_PROCESS, END_CODE_HIDDEN_PARAM);

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initDiscProjectsPage();
        projectsPage.searchProject(MASTER_NAME_PROJECT + " #1");

        assertTrue(projectsPage.hasProject(MASTER_NAME_PROJECT + " #1"), "There is no Master Project");

        projectsPage.clickOnProjectTitleLink(MASTER_NAME_PROJECT + " #1");
        createAddLoadScheduleProcess(ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();

        assertTrue(projectDetailPage.goToAnalyze().getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                "There is no insight in Master Project");
    }

    @Test(dependsOnMethods = {"runLcmReleaseProcess"}, groups = {"precondition"})
    public void runLcmProvisioningProcess() {
        initDiscProjectDetailPage();
        END_CODE_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() +
                TxtFile.GD_ENDCODE_PARAM_PROVISIONING.getName());
        sleepTightInSeconds(2); // wait by Loading Drop DownList value selected
        projectDetailPage.clickDeployButton();
        createDeployProcess(ProcessType.LCM_RPOVISIONING, LCM_PROVISIONING_PROCESS);
        initDiscProjectDetailPage();
        createAndRunProcess(LCM_PROVISIONING_PROCESS, END_CODE_PARAM);

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initDiscProjectsPage();
        projectsPage.searchProject(CLIENT_PROJECT);

        assertTrue(projectsPage.hasProject(CLIENT_PROJECT), "There is no Client Project");

        projectsPage.clickOnProjectTitleLink(CLIENT_PROJECT);
        createAddLoadScheduleProcess(ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        AnalysisPage analysisPage= projectDetailPage.goToAnalyze();
        assertTrue(analysisPage.getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                "There is no insight in Master Project");

        analysisPage.getPageHeader().expandInsightSelection().openInsight(INSIGHT_LCM);
        analysisPage.waitForReportComputing();
        sleepTightInSeconds(2); // waiting for chart rendering, have no option on graphene test to wait
        geoChart = GeoPushpinChartPicker.getInstance(browser);
        geoChart.hoverOnGeoPushpin(-445, -120);
        assertTrue(geoChart.isGeoPopupTooltipDisplayed(), "Tooltip on Geo pushpin should be displayed");
        assertEquals(geoChart.getGeoPopupTooltip(), asList("city", "Sum of population", "Sum of population", "state"));
        assertTrue(geoChart.isPushpinCategoryLegendVisible(), "Geo pushpin category is not visibled");
    }

    @Test(dependsOnMethods = {"runLcmProvisioningProcess"}, groups = {"precondition"})
    public void runLcmRolloutProcess() {
        initDiscProjectsPage();
        projectsPage.searchProject(DEVELOPMENT_PID).clickOnProjectTitleLink(DEVELOPMENT_TITLE)
                .goToAnalyze().getPageHeader().expandInsightSelection().deleteInsight(INSIGHT_LCM);
        initDiscProjectDetailPage();
        projectDetailPage.getDataProcessName(LCM_RELEASE_PROCESS).openSchedule(RELEASE_SCHEDULE_NAME);
        scheduleDetail.executeSchedule().waitForExecutionFinish().close();
        initDiscProjectsPage();
        projectsPage.searchProject(MASTER_NAME_PROJECT + " #2");

        assertTrue(projectsPage.hasProject(MASTER_NAME_PROJECT + " #2"), "There is no New Master Project");

        END_CODE_PARAM = ResourceUtils.getResourceAsString(SQL_FILES.getPath() +
                TxtFile.GD_ENDCODE_PARAM_ROLLOUT.getName());
        initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        projectDetailPage.clickDeployButton();
        createDeployProcess(ProcessType.LCM_ROLLOUT, LCM_ROLLOUT_PROCESS);
        initDiscProjectDetailPage();
        createAndRunProcess(LCM_ROLLOUT_PROCESS, END_CODE_PARAM);

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initDiscProjectsPage().searchProject(CLIENT_PROJECT).clickOnProjectTitleLink(CLIENT_PROJECT);

        assertFalse(projectDetailPage.goToAnalyze().getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                "Insight still existed");
    }

    private void createDeployProcess(ProcessType processType, String processName) {
        DeployProcessForm deployProcess = DeployProcessForm.getInstance(browser);
        deployProcess.scrollToSelectProcessType(processType, 10000).enterProcessName(processName).submit();
    }

    private void createAndRunProcess(String processTypeName, String endcodeParam) {
        addParamScheduleProcess(processTypeName);
        createProcess(endcodeParam);
        runProcess();
    }

    private void createAndRunReleaseProcess(String processTypeName, String endcodeParam) {
        addParamScheduleProcess(processTypeName);
        createReleaseProcess(endcodeParam);
        runProcess();
    }

    private void createAddLoadScheduleProcess(String processTypeName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName).selectAllDatasetsOption().schedule();
        runProcess();
    }

    private void runProcess() {
        scheduleDetail = DataloadScheduleDetail.getInstance(browser);
        scheduleDetail.executeSchedule().waitForExecutionFinish();
    }

    private void addParamScheduleProcess(String processTypeName) {
        projectDetailPage.openCreateScheduleForm().selectProcess(processTypeName);
        scheduleForm = CreateScheduleForm.getInstance(browser);
        scheduleForm.addParameter(LcmDirectoryConfiguration.CLIENT_GDC_HOSTNAME.getParamName(), testParams.getHost())
                .addSecureParameter(LcmDirectoryConfiguration.GDC_PASSWORD.getParamName(), testParams.getPassword())
                .addParameter(LcmDirectoryConfiguration.GDC_USERNAME.getParamName(), testParams.getUser())
                .addParameter(LcmDirectoryConfiguration.ORGANIZATION.getParamName(), testParams.getUserDomain())
                .addSecureParameter(LcmDirectoryConfiguration.ADS_PASSWORD.getParamName(), testParams.getPassword())
                .addParameter(LcmDirectoryConfiguration.CLIENT_GDC_PROTOCOL.getParamName(),
                        LcmDirectoryConfiguration.CLIENT_GDC_PROTOCOL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.GDC_LOG_LEVEL.getParamName(),
                        LcmDirectoryConfiguration.GDC_LOG_LEVEL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.EXCLUDE_FACT_RULE.getParamName(),
                        LcmDirectoryConfiguration.EXCLUDE_FACT_RULE.getParamValue())
                .addParameter(LcmDirectoryConfiguration.TRANSFER_ALL.getParamName(),
                        LcmDirectoryConfiguration.TRANSFER_ALL.getParamValue())
                .addParameter(LcmDirectoryConfiguration.SEGMENT_ID.getParamName(), SEGMENT_ID)
                .addParameter(LcmDirectoryConfiguration.JDBC_URL.getParamName(), ADS_WAREHOUSE)
                .addParameter(LcmDirectoryConfiguration.DEVELOPMENT_PID.getParamName(), DEVELOPMENT_PID)
                .addParameter(LcmDirectoryConfiguration.MASTER_PROJECT_NAME.getParamName(), MASTER_NAME_PROJECT);
    }

    private void createReleaseProcess(String endcodeParam) {
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_HIDEN_PARAMS.getParamName(), endcodeParam);
        scheduleForm.addParameter(LcmDirectoryConfiguration.TOKEN_ID.getParamName(), testParams.getAuthorizationToken());
        scheduleForm.enterScheduleName(RELEASE_SCHEDULE_NAME);
        scheduleForm.schedule();
    }

    private void createProcess(String endcodeParam) {
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_PARAM.getParamName(), endcodeParam);
        scheduleForm.schedule();
    }

    @AfterClass(alwaysRun = true)
    private void deleteClientAndProject() {
        if (scheduleForm != null) {
            deleteSegmentDefault(new RestClient(getProfile(ADMIN)), testParams.getUserDomain(), SEGMENT_ID);
        }
    }
}
