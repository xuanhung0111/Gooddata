package com.gooddata.qa.graphene;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.browser.BrowserUtils.getCurrentBrowserAgent;
import static com.gooddata.qa.browser.BrowserUtils.maximize;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;

import java.io.IOException;
import java.io.InputStream;

import com.gooddata.project.ProjectValidationResults;

import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectDriver;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertFalse;

import com.gooddata.qa.utils.http.rolap.RolapRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public abstract class AbstractProjectTest extends AbstractUITest {

    protected static final int DEFAULT_PROJECT_CHECK_LIMIT = 60; // 5 minutes

    protected String projectTitle = "simple-project";
    protected String projectTemplate = "";
    protected int projectCreateCheckIterations = DEFAULT_PROJECT_CHECK_LIMIT;

    // validations are enabled by default on any child class
    protected boolean validateAfterClass = true;

    // this flag will enable feature: create new user for each test instead of using provided user account
    protected boolean useDynamicUser;

    @BeforeClass(alwaysRun = true)
    public void enableDynamicUser() {
        useDynamicUser = Boolean.parseBoolean(testParams.loadProperty("useDynamicUser"));
    }

    @Test(groups = {"createProject"})
    public void init() throws JSONException {
        System.out.println("Current browser agent is: " + getCurrentBrowserAgent(browser).toUpperCase());

        // Use this utility to maximize browser in chrome - MAC
        // browser.manage().window().maximize(); do not work
        maximize(browser);

        // sign in with admin user
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"init"}, groups = {"createProject"})
    public void configureStartPage() {
        startPageContext = new StartPageContext() {

            @Override
            public void waitForStartPageLoaded() {
                waitForProjectsPageLoaded(browser);
            }

            @Override
            public String getStartPage() {
                return PAGE_PROJECTS;
            }
        };
    }

    @Test(dependsOnMethods = {"configureStartPage"}, groups = {"createProject"})
    public void createProject() throws JSONException {
        if (testParams.isReuseProject()) {
            if (testParams.getProjectId() != null && !testParams.getProjectId().isEmpty()) {
                System.out.println("Project will be re-used, id: " + testParams.getProjectId());
                return;
            } else {
                System.out.println("Project reuse is expected, but projectId is missing, new project will be created...");
            }
        }

        if (!canAccessGreyPage(browser)) {
            System.out.println("Use REST api to create project.");
            testParams.setProjectId(ProjectRestUtils.createProject(getGoodDataClient(), projectTitle,
                    projectTemplate, testParams.getAuthorizationToken(), ProjectDriver.POSTGRES,
                    testParams.getProjectEnvironment()));

        } else {
            openUrl(PAGE_GDC_PROJECTS);
            waitForElementVisible(gpProject.getRoot());

            projectTitle += "-" + testParams.getProjectDriver().name();
            if (projectTemplate.isEmpty()) {
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null,
                        testParams.getAuthorizationToken(), testParams.getProjectDriver(),
                        testParams.getProjectEnvironment(), projectCreateCheckIterations));
            } else {
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, projectTemplate,
                        testParams.getAuthorizationToken(), ProjectDriver.POSTGRES, testParams.getProjectEnvironment(),
                        projectCreateCheckIterations));

                if (testParams.getProjectDriver().equals(ProjectDriver.VERTICA)) {
                    String exportToken = exportProject(true, true, false, projectCreateCheckIterations * 5);
                    deleteProject(testParams.getProjectId());

                    openUrl(PAGE_GDC_PROJECTS);
                    waitForElementVisible(gpProject.getRoot());
                    testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null,
                            testParams.getAuthorizationToken2(), testParams.getProjectDriver(),
                            testParams.getProjectEnvironment(), projectCreateCheckIterations));
                    importProject(exportToken, projectCreateCheckIterations * 5);
                }
            }
            Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());
        }

        projectTitle = testParams.getProjectId().substring(0, 6) + "-" + projectTitle;

        ProjectRestUtils.updateProjectTitle(getRestApiClient(), getProject(), projectTitle);
        log.info("Project title: " + projectTitle);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"createProject"})
    public void inviteUsersIntoProject() throws ParseException, IOException, JSONException {
        addUsersWithOtherRolesToProject();
    }

    @Test(dependsOnMethods = {"inviteUsersIntoProject"}, groups = {"createProject"})
    public void createAndUseDynamicUser() throws ParseException, JSONException, IOException {
        if (!useDynamicUser) {
            log.warning("This test does not need to use dynamic user!");
            return;
        }

        if (!isCurrentUserDomainUser()) {
            log.warning("Main user in this test is not domain user. Cannot use dynamic user! Use the current to test!");
            return;
        }

        String dynamicUser = createDynamicUserFrom(testParams.getUser().replace("@", "+dynamic@"));
        addUserToProject(dynamicUser, UserRoles.ADMIN);

        // update domain user
        testParams.setDomainUser(testParams.getUser());
        testParams.setUser(dynamicUser);

        // update REST clients to the correct user because they can be generated and cached
        restApiClient = getRestApiClient(testParams.getUser(), testParams.getPassword());
        goodDataClient = getGoodDataClient(testParams.getUser(), testParams.getPassword());

        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
    }

    @AfterClass(alwaysRun = true)
    public void validateProjectTearDown() throws JSONException {
        //it is necessary to login admin to validate project on afterClass
        logout();
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);

        if (validateAfterClass) {
            System.out.println("Going to validate project after tests...");
            // TODO remove when ATP-1520, ATP-1519, ATP-1822 are fixed
            String testName = this.getClass().getSimpleName();
            if (!canAccessGreyPage(browser) || testName.contains("Coupa") || testName.contains("Pardot")) {
                System.out.println("Validations are skipped for Coupa and Pardot projects"
                        + " or running in IE, Android and Safari");
                return;
            }
            final ProjectValidationResults results = validateProject();
            assertFalse(results.isError(), "Validation contains no errors");
            assertFalse(results.isFatalError(), "Validation contains no fatal errors");
        } else {
            System.out.println("Validations were skipped at this test class...");
        }
    }

    @AfterClass(dependsOnMethods = {"validateProjectTearDown"}, alwaysRun = true)
    public void deleteProjectTearDown(ITestContext context) {
        if (testParams.isReuseProject()) {
            log.info("Project is being re-used and won't be deleted.");
            return;
        }

        log.info("Delete mode is set to " + testParams.getDeleteMode().toString());
        String projectId = testParams.getProjectId();
        if (projectId != null && projectId.length() > 0) {
            switch (testParams.getDeleteMode()) {
                case DELETE_ALWAYS:
                    log.info("Project will be deleted: " + projectId);
                    ProjectRestUtils.deleteProject(getGoodDataClient(), projectId);
                    break;
                case DELETE_IF_SUCCESSFUL:
                    if (context.getFailedTests().size() == 0) {
                        log.info("Test was successful, project will be deleted: " + projectId);
                        ProjectRestUtils.deleteProject(getGoodDataClient(), projectId);
                    } else {
                        log.info("Test wasn't successful, project won't be deleted...");
                    }
                    break;
                case DELETE_NEVER:
                    log.info("Delete mode set to NEVER, project won't be deleted...");
                    break;
            }
        } else {
            System.out.println("No project created -> no delete...");
        }
    }

    protected MetadataService getMdService() {
        return getGoodDataClient().getMetadataService();
    }

    protected Project getProject() {
        return getGoodDataClient().getProjectService().getProjectById(testParams.getProjectId());
    }

    protected Metric createMetric(String name, String expression, String format) {
        return createMetric(getGoodDataClient(), name, expression, format);
    }
    
    protected Metric createMetric(GoodData gooddata, String name, String expression, String format) {
        return gooddata.getMetadataService().createObj(getProject(), new Metric(name, expression, format));
    }

    protected Collection<String> getAttributeValues(Attribute attribute) {
        return getMdService().getAttributeElements(attribute)
                .stream().map(attr -> attr.getTitle()).map(String::trim).collect(toList());
    }

    protected Report createReportViaRest(ReportDefinition defination) {
        return createReportViaRest(getGoodDataClient(), defination);
    }

    protected Report createReportViaRest(GoodData gooddata, ReportDefinition definition) {
        MetadataService metadataService = gooddata.getMetadataService();
        definition = metadataService.createObj(getProject(), definition);
        return metadataService.createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    public void setupMaql(String maql) throws JSONException, IOException {
        getGoodDataClient()
                .getModelService()
                .updateProjectModel(getProject(), maql)
                .get();
    }

    public void setupDataViaRest(String datasetId, InputStream dataset) {
        getGoodDataClient()
                .getDatasetService()
                .loadDataset(getProject(), datasetId, dataset)
                .get();
    }

    public void setupData(String csvPath, String uploadInfoPath)
            throws JSONException, IOException, URISyntaxException {
        String webdavServerUrl = getWebDavServerUrl(getRestApiClient(), getRootUrl());

        String webdavUrl = webdavServerUrl + "/" + UUID.randomUUID().toString();

        URL csvResource = getClass().getResource(csvPath);
        URL uploadInfoResource = getClass().getResource(uploadInfoPath);

        uploadFileToWebDav(csvResource, webdavUrl);
        uploadFileToWebDav(uploadInfoResource, webdavUrl);

        String integrationEntry = webdavUrl.substring(webdavUrl.lastIndexOf("/") + 1, webdavUrl.length());
        RolapRestUtils.postEtlPullIntegration(getRestApiClient(), testParams.getProjectId(),
                integrationEntry);
    }

    private String getWebDavServerUrl(final RestApiClient restApiClient, final String serverRootUrl)
            throws IOException, JSONException {
        final JSONArray links = getJsonObject(restApiClient, "/gdc", HttpStatus.OK)
                .getJSONObject("about")
                .getJSONArray("links");

        JSONObject link;
        for (int i = 0, n = links.length(); i < n; i++) {
            link = links.getJSONObject(i);
            if (!"user-uploads".equals(link.getString("title"))) continue;
            return getWebDavServerUrl(link.getString("link"), serverRootUrl);
        }

        return "";
    }

    private String getWebDavServerUrl(String userUploadsLink, String serverRootUrl) {
        String webdavServerUrl = "";
        if (userUploadsLink.startsWith("https://")) {
            webdavServerUrl = userUploadsLink;
        } else if (userUploadsLink.startsWith("/")) {
            webdavServerUrl = serverRootUrl + userUploadsLink.substring(1);
        } else {
            webdavServerUrl = serverRootUrl + userUploadsLink;
        }
        return webdavServerUrl;
    }

    private boolean isCurrentUserDomainUser() {
        return UserManagementRestUtils.isDomainUser(getRestApiClient(), testParams.getUserDomain());
    }
}
