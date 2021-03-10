package com.gooddata.qa.graphene;

import com.gooddata.sdk.common.GoodDataRestException;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.sdk.service.project.ProjectService;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.csvuploader.DatasetMessageBar;
import com.gooddata.qa.graphene.fragments.freegrowth.DataloadPage;
import com.gooddata.qa.graphene.fragments.freegrowth.FreeGrowthDataset;
import com.gooddata.qa.graphene.fragments.freegrowth.FreeLandingPage;
import com.gooddata.qa.graphene.fragments.freegrowth.ManageTabs;
import com.gooddata.qa.graphene.fragments.freegrowth.WorkspaceHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.utils.WaitUtils;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.web.client.RestClientException;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForSchedulesPageLoaded;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.util.stream.Collectors.toList;

public abstract class AbstractFreemiumGrowthTest extends AbstractUITest {

    // validations are enabled by default on any child class
    protected boolean validateAfterClass = true;

    // this flag will enable feature: create new user for each test instead of using provided user account
    protected boolean useDynamicUser;

    protected RestClient restClient;
    protected final List<Pair<String, String>> createdProjects = new ArrayList<>();
    protected int maxProjects = 0;
    protected String editionName = "";

    protected static final String[] PAYROLL_COLUMN_TYPES = {"Attribute", "Attribute", "Attribute", "Attribute",
            "Attribute", "Attribute", "Attribute", "Date (Year-Month-Day)", "Measure"};
    protected static final CsvFile PAYROLL = CsvFile.loadFile(
            getFilePathFromResource("/payroll.csv"))
            .setColumnTypes(PAYROLL_COLUMN_TYPES);

    @BeforeClass(alwaysRun = true)
    public void enableDynamicUser() {
        useDynamicUser = Boolean.parseBoolean(testParams.loadProperty("useDynamicUser"));
    }

    public void initProperties() {
        log.info("init properties");
    }

    @Test(groups = {"prepareTest"})
    public void createAndUseDynamicUser() throws ParseException, JSONException, IOException {
        initProperties();
        // update domain user
        testParams.setDomainUser(testParams.getUser());

        if (!useDynamicUser) {
            log.warning("This test does not need to use dynamic user!");
            return;
        }

        // update user into dynamic user
        String dynamicUser = createDynamicUserFrom(testParams.getUser().replace("@", "+dynamic@"));
        testParams.setUser(dynamicUser);
    }

    @Test(groups = {"prepareTest"}, dependsOnMethods = "createAndUseDynamicUser")
    public void login() {
        restClient = new RestClient(getProfile(ADMIN));
        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    @Test(groups = {"createProject"}, dependsOnGroups = {"prepareTest"})
    public void createProject() {
        log.info("Creating project");
    }

    @Test(groups = {"landingPage"}, dependsOnGroups = {"createProject"})
    public void verifyLandingPage() {
        FreeLandingPage landingPage = FreeLandingPage.getInstance(browser);
        Assert.assertEquals(landingPage.getTitleContainer().getTitle(), "GoodData");
        Assert.assertEquals(landingPage.getTitleContainer().getEdition(), editionName);
    }

    @Test(groups = {"landingPage"}, dependsOnMethods = {"verifyLandingPage"})
    public void gotoFirstProject() {
        String projectId = createdProjects.get(0).getLeft();
        FreeLandingPage landingPage = FreeLandingPage.getInstance(browser);
        landingPage.getWorkspaceContainer().gotoProject(projectId);
    }

    @Test(dependsOnGroups = {"landingPage"})
    public void csvUpload() {
        DataloadPage dataloadPage = DataloadPage.getInstance(browser);
        dataloadPage.verifyDataloadPage();
        dataloadPage.uploadFile(PAYROLL).triggerIntegration();
        FreeGrowthDataset.waitForDatasetLoaded(browser);

        DatasetMessageBar.getInstance(browser).waitForSuccessMessageBar();
        FreeGrowthDataset freeGrowthDataset = FreeGrowthDataset.getInstance(browser);
        freeGrowthDataset.verifyDatasetPage(createdProjects.get(0).getLeft());
        freeGrowthDataset.openAnalyzePage();
    }

    @Test(dependsOnMethods = {"csvUpload"})
    public void testAnalysePage() {
        AnalysisPage analysisPage = AnalysisPage.getInstance(browser);
        analysisPage.addAttribute(ATTR_DEPARTMENT).changeReportType(ReportType.TABLE).waitForReportComputing();
        analysisPage.saveInsight(PAYROLL.getName());

        Assert.assertFalse(analysisPage.getPageHeader().isExportButtonPresent(), "export to report should not be displayed");
        WorkspaceHeader.getInstance(browser).getManageMenuItem().get().click();
    }

    @Test(dependsOnMethods = {"testAnalysePage"})
    public void testDataManagePage() {
        WaitUtils.waitForDataPageLoaded(browser);
        ManageTabs manageTabs = ManageTabs.getInstance(browser).verifyManageTabs();
        manageTabs.clickScheduleEmailTab();
    }

    @Test(dependsOnMethods = {"testDataManagePage"})
    public void testScheduleManagePage() {
        waitForSchedulesPageLoaded(browser);
        waitForElementNotVisible(BY_SCHEDULES_LOADING);
        EmailSchedulePage emailSchedulePage = EmailSchedulePage.getInstance(browser);
        Assert.assertFalse(emailSchedulePage.isOpenNewScheduleVisible(), "schedule email button should not be displayed");
    }

    @Test(dependsOnMethods = {"testScheduleManagePage"})
    public void testSignOutThenSignIn() {
        logoutThenLogIn();
        // assert that default page is KPI DB
        waitForOpeningIndigoDashboard();
        IndigoDashboardsPage.getInstance(browser);
        String projectName = WorkspaceHeader.getInstance(browser).getCurrentProjectName();
        Assert.assertEquals(projectName, createdProjects.get(0).getRight());
    }

    protected void logoutThenLogIn() {
        Graphene.waitGui()
                .until(ExpectedConditions.elementToBeClickable(BY_LOGGED_USER_BUTTON))
                .click();

        waitForElementVisible(BY_LOGOUT_LINK, browser).click();
        waitForElementNotPresent(BY_LOGGED_USER_BUTTON);

        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    protected String createNewEmptyProject(String projectTitle) {
        final Project project = new Project(projectTitle, testParams.getAuthorizationToken());
        project.setDriver(testParams.getProjectDriver());
        project.setEnvironment(testParams.getProjectEnvironment());

        String projectId = restClient.getProjectService().createProject(project)
                .get(testParams.getCreateProjectTimeout(), TimeUnit.MINUTES).getId();
        createdProjects.add(Pair.of(projectId, projectTitle));
        log.info("created empty project:" + projectId);
        return projectId;
    }

    private void deleteCreatedProject() {
        List<String> domainProjects = restClient.getProjectService().getProjects().stream()
                .map(Project::getId).collect(toList());
        createdProjects.stream()
                .filter(createdProjectId -> domainProjects.contains(createdProjectId))
                .forEach(createdProjectId -> {
                    try {
                        deleteProject(restClient, createdProjectId.getLeft());
                    } catch (GoodDataRestException | RestClientException e) {
                        return;
                    }
                    log.info(createdProjectId + " is removed project");
                });
    }

    protected void deleteProject(final RestClient restClient, final String projectId) {
        final ProjectService service = restClient.getProjectService();
        service.removeProject(service.getProjectById(projectId));
        log.info("deleted project:" + projectId);
    }

    /* Viet fix to fore it follows Aquillian cycle */
    @AfterClass(groups = {"arquillian"}, inheritGroups = true, alwaysRun = true)
    //@AfterClass(alwaysRun = true)
    public void deleteProjectTearDown(ITestContext context) {
        if (testParams.isReuseProject()) {
            log.info("Project is being re-used and won't be deleted.");
            return;
        }

        log.info("Delete mode is set to " + testParams.getDeleteMode().toString());
        deleteCreatedProject();
    }
}
