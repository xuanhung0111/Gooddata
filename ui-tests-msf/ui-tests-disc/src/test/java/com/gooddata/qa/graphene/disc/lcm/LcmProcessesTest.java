package com.gooddata.qa.graphene.disc.lcm;

import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.LcmDirectoryConfiguration;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.io.ResourceUtils;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.lcm.LcmRestUtils.deleteSegmentDefault;
import static org.testng.Assert.*;

public class LcmProcessesTest extends AbstractDataloadProcessTest {

    public DataloadScheduleDetail scheduleDetail;
    public CreateScheduleForm scheduleForm;
    private final String LCM_RELEASE_PROCESS = "LCM_RELEASE_" + generateHashString();
    private final String LCM_ROLLOUT_PROCESS = "LCM_ROLLOUT_" + generateHashString();
    private final String LCM_PROVISIONING_PROCESS = "LCM_PROVISIONING_" + generateHashString();
    private final String MASTER_NAME_PROJECT = "MASTERPROJECT" + generateHashString();
    private static String END_CODE_PARAM;
    private static String END_CODE_HIDDEN_PARAM;
    private static String DEVELOPMENT_PID;
    private static String DEVELOPMENT_TITLE;
    private static String ADS_WAREHOUSE;
    private static final String INSIGHT_LCM = "Insight-LCM";
    private static final String CLIENT_PROJECT = "LcmProjectClient";
    private static final String SEGMENT_ID = "SegmentProject";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "_LCM_TEST_UI";
    }

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException {
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));
        Parameters parameters = defaultParameters.get().addParameter(Parameter.SQL_QUERY,
                SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_TABLE.getName()));

        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE, parameters);
    }

    @Test(dependsOnMethods = {"initData"}, groups = {"precondition"})
    public void runAddLoadingData() throws JSONException {
        initDiscProjectDetailPage();
        ADS_WAREHOUSE = ads.getConnectionUrl();
        DEVELOPMENT_PID = testParams.getProjectId();
        DEVELOPMENT_TITLE = projectTitle;
        createAddLoadScheduleProcess(ProcessType.AUTOMATED_DATA_DISTRIBUTION.getTitle());

        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        scheduleDetail.close();
        initAnalysePage().addMetric(FACT_TOTAL_PRICE, FieldType.FACT).addAttribute(ATTR_NAME).waitForReportComputing()
                .saveInsight(INSIGHT_LCM);
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

        assertTrue(projectDetailPage.goToAnalyze().getPageHeader().expandInsightSelection().isExist(INSIGHT_LCM),
                "There is no insight in Master Project");
    }

    @Test(dependsOnMethods = {"runLcmProvisioningProcess"}, groups = {"precondition"})
    public void runLcmRolloutProcess() {
        initDiscProjectsPage();
        projectsPage.searchProject(DEVELOPMENT_PID).clickOnProjectTitleLink(DEVELOPMENT_TITLE)
                .goToAnalyze().getPageHeader().expandInsightSelection().deleteInsight(INSIGHT_LCM);
        initDiscProjectDetailPage();
        projectDetailPage.getDataProcessName(LCM_RELEASE_PROCESS).openSchedule("lcm-brick[M3]-release");
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
        deployProcess.quickSelectSpecialProcessType(processType, 5000).enterProcessName(processName).submit();
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
                .addParameter(LcmDirectoryConfiguration.GDC_PASSWORD.getParamName(), testParams.getPassword())
                .addParameter(LcmDirectoryConfiguration.GDC_USERNAME.getParamName(), testParams.getUser())
                .addParameter(LcmDirectoryConfiguration.ORGANIZATION.getParamName(),
                        testParams.getHost().split(".intgdc.")[0])
                .addParameter(LcmDirectoryConfiguration.ADS_PASSWORD.getParamName(), testParams.getPassword())
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
        scheduleForm.schedule();
    }

    private void createProcess(String endcodeParam) {
        scheduleForm.addParameter(LcmDirectoryConfiguration.GD_ENDCODE_PARAM.getParamName(), endcodeParam);
        scheduleForm.schedule();
    }

    @AfterClass(alwaysRun = true)
    private void deleteClientAndProject() {
        deleteSegmentDefault(new RestClient(getProfile(ADMIN)), testParams.getHost().split(".intgdc.")[0],
                SEGMENT_ID);
    }
}