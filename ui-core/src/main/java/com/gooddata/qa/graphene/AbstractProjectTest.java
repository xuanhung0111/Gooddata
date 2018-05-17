package com.gooddata.qa.graphene;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.browser.BrowserUtils.getCurrentBrowserAgent;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;

import java.io.IOException;
import java.io.InputStream;

import com.gooddata.GoodData;
import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.ObjNotFoundException;
import com.gooddata.project.ProjectService;
import com.gooddata.project.ProjectValidationResults;
import com.gooddata.qa.fixture.FixtureException;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterType;
import com.gooddata.qa.mdObjects.dashboard.filter.FloatingFilterConstraint;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.ReportItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.rolap.RolapRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.json.JSONException;
import org.openqa.selenium.Dimension;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.project.Project;
import com.gooddata.qa.fixture.Fixture;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.RestApiClient;

import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertFalse;

import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public abstract class AbstractProjectTest extends AbstractUITest {

    public static final String DEFAULT_METRIC_FORMAT = "#,##0";
    public static final String DEFAULT_CURRENCY_METRIC_FORMAT = "$#,##0.00";

    protected static final int DEFAULT_PROJECT_CHECK_LIMIT = 60; // 5 minutes

    protected String projectTitle = "simple-project";
    protected ResourceTemplate appliedFixture;
    protected int projectCreateCheckIterations = DEFAULT_PROJECT_CHECK_LIMIT;

    // validations are enabled by default on any child class
    protected boolean validateAfterClass = true;

    // this flag will enable feature: create new user for each test instead of using provided user account
    protected boolean useDynamicUser;

    private RestClient restClient;

    @BeforeClass(alwaysRun = true)
    public void enableDynamicUser() {
        useDynamicUser = Boolean.parseBoolean(testParams.loadProperty("useDynamicUser"));
    }

    @Test(groups = {"createProject"})
    @Parameters({"windowSize"})
    public void init(@Optional("maximize") String windowSize) throws JSONException {
        System.out.println("Current browser agent is: " + getCurrentBrowserAgent(browser).toUpperCase());

        // override default value of properties
        initProperties();

        // adjust window size to run on mobile mode
        if (!windowSize.equals("maximize")) adjustWindowSize(windowSize);

        // sign in with admin user
        signIn(canAccessGreyPage(browser), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"init"}, groups = {"createProject"})
    public void createProject() throws Throwable {
        if (testParams.isReuseProject()) {
            if (testParams.getProjectId() != null && !testParams.getProjectId().isEmpty()) {
                log.info("Project will be re-used, id: " + testParams.getProjectId());
                return;
            } else {
                log.info("Project reuse is expected, but projectId is missing, new project will be created...");
            }
        }

        // strategy using to create a new project should be defined by this hook
        createNewProject();

        projectTitle = testParams.getProjectId().substring(0, 6) + "-" + projectTitle;
        ProjectRestRequest projectRestRequest = new ProjectRestRequest(
                new RestClient(getProfile(ADMIN)), testParams.getProjectId());
        projectRestRequest.updateProjectTitle(projectTitle);
        log.info("Project title: " + projectTitle);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"createProject"})
    public void inviteUsersIntoProject() throws ParseException, IOException, JSONException {
        addUsersWithOtherRolesToProject();
    }

    @Test(dependsOnMethods = {"inviteUsersIntoProject"}, groups = {"createProject"})
    public void createAndUseDynamicUser() throws ParseException, JSONException, IOException {
        if (!isCurrentUserDomainUser()) {
            log.warning("Main user in this test is not domain user. Cannot use dynamic user! Use the current to test!");
            return;
        }

        // update domain user
        testParams.setDomainUser(testParams.getUser());

        if (!useDynamicUser) {
            log.warning("This test does not need to use dynamic user!");
            return;
        }

        // update user into dynamic user
        String dynamicUser = createDynamicUserFrom(testParams.getUser().replace("@", "+dynamic@"));
        addUserToProject(dynamicUser, UserRoles.ADMIN);

        testParams.setUser(dynamicUser);

        // update REST clients to the correct user because they can be generated and cached
        restApiClient = getRestApiClient(testParams.getUser(), testParams.getPassword());
        goodDataClient = getGoodDataClient(testParams.getUser(), testParams.getPassword());

        logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
    }

    @Test(dependsOnMethods = {"createAndUseDynamicUser"}, groups = {"createProject"})
    public void prepareProject() throws Throwable {
        customizeProject();
    }

    @Test(dependsOnMethods = {"prepareProject"}, groups = {"createProject"})
    public void assignStartPage() {
        // start page could be any page, e.g.: analyse page, ...
        // to make it possible, this method should be executed last in createProject group
        configureStartPage();
    }

    @AfterClass(alwaysRun = true)
    public void validateProjectTearDown() throws JSONException {
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
                    deleteProject(getProfile(ADMIN), projectId);
                    break;
                case DELETE_IF_SUCCESSFUL:
                    if (context.getFailedTests().size() == 0) {
                        log.info("Test was successful, project will be deleted: " + projectId);
                        deleteProject(getProfile(ADMIN), projectId);
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

    /**
     * a hook to createProject group.
     * Property values should be overridden by using this method
     */
    protected void initProperties() {
        // should be implemented later in abstract test or test classes
    }

    /**
     * a hook to createProject group.
     * this helps user define what is the strategy to create a new project
     */
    protected void createNewProject() throws Throwable{
        if (Objects.isNull(appliedFixture)) {
            log.info("Using REST api to create an empty project.");
            testParams.setProjectId(createNewEmptyProject(projectTitle));
        } else {
            log.info("Using fixture named " + appliedFixture.getPath() + " to create project");
            testParams.setProjectId(createProjectUsingFixture(projectTitle, appliedFixture));
        }
    }

    /**
     * a hook to createProject group.
     * Any extra setting which is required for a specific test class should be added here
     */
    protected void customizeProject() throws Throwable {
        // should be implemented later in abstract test or test classes
    }

    /**
     * a hook to createProject group.
     * start page context which will be loaded after finishing a test should be assigned by using this
     * project.html is used by default
     */
    protected void configureStartPage() {
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

    protected MetadataService getMdService() {
        return getGoodDataClient().getMetadataService();
    }

    protected Project getProject() {
        return getGoodDataClient().getProjectService().getProjectById(testParams.getProjectId());
    }

    /**
     * Create project with specific fixture
     *
     * @param title project title
     * @param appliedFixture fixture which is applied to an empty project
     * @return project id
     */
    protected String createProjectUsingFixture(String title, ResourceTemplate appliedFixture) {
        return createProjectUsingFixture(title, appliedFixture, testParams.getUser());
    }

    /**
     *
     * @param title project title
     * @param appliedFixture fixture which is applied to an empty project
     * @param user Using to create project
     * @return project id
     */
    protected String createProjectUsingFixture(String title, ResourceTemplate appliedFixture, String user) {
        if (Objects.isNull(appliedFixture)) {
            throw new FixtureException("Fixture can't be null");
        }

        return new Fixture(appliedFixture)
                .setGoodDataClient(getGoodDataClient(user, testParams.getPassword()))
                .setRestApiClient(getRestApiClient(user, testParams.getPassword()))
                .deploy(title, testParams.getAuthorizationToken(),
                        testParams.getProjectDriver(), testParams.getProjectEnvironment());
    }

    protected RestClient getAdminRestClient() {
        if (Objects.isNull(restClient))
            restClient = new RestClient(getProfile(ADMIN));

        return restClient;
    }

    protected Collection<String> getAttributeValues(Attribute attribute) {
        return getMdService().getAttributeElements(attribute)
                .stream().map(AttributeElement::getTitle).map(String::trim).collect(toList());
    }

    protected Report createReportViaRest(ReportDefinition defination) {
        return createReportViaRest(getGoodDataClient(), defination);
    }

    protected Report createReportViaRest(GoodData gooddata, ReportDefinition definition) {
        MetadataService metadataService = gooddata.getMetadataService();
        definition = metadataService.createObj(getProject(), definition);
        return metadataService.createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    public void setupMaql(String maql) {
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

    public void setupData(String csvPath, String uploadInfoPath) {
        try {
            String webdavServerUrl = getWebDavServerUrl(getRestApiClient(), getRootUrl());

            String webdavUrl = webdavServerUrl + "/" + UUID.randomUUID().toString();

            URL csvResource = getClass().getResource(csvPath);
            URL uploadInfoResource = getClass().getResource(uploadInfoPath);

            uploadFileToWebDav(csvResource, webdavUrl);
            uploadFileToWebDav(uploadInfoResource, webdavUrl);

            String integrationEntry = webdavUrl.substring(webdavUrl.lastIndexOf("/") + 1, webdavUrl.length());
            new RolapRestRequest(new RestClient(getProfile(ADMIN)), testParams.getProjectId())
                    .postEtlPullIntegration(integrationEntry);
        } catch (JSONException | IOException | URISyntaxException e) {
            throw new RuntimeException("There is error while setupData");
        }
    }

    /**
     * Project title is updated internally in createProject group. Use this method to get Invitation subject
     * @return invitation subject
     */

    protected String getInvitationSubject() {
        return projectTitle + " Invitation";
    }

    //------------------------- SUPPORT GET OBJECTS - BEGIN -------------------------
    protected Attribute getAttributeByTitle(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title));
    }

    protected Attribute getAttributeByUri(String uri) {
        return getMdService().getObjByUri(uri, Attribute.class);
    }

    protected Attribute getAttributeByIdentifier(String id) {
        return getMdService().getObj(getProject(), Attribute.class, identifier(id));
    }

    protected String getAttributeElementUri(String attributeName, String elementName) {
        return getMdService().getAttributeElements(getAttributeByTitle(attributeName)).stream()
                .filter(e -> e.getTitle().equals(elementName)).findFirst().get().getUri();
    }

    protected List<String> getAttributeElementUris(String attributeName, List<String> elementNames) {
        return getMdService().getAttributeElements(getAttributeByTitle(attributeName)).stream()
                .filter(attrEle -> elementNames.contains(attrEle.getTitle()))
                .map(AttributeElement::getUri)
                .collect(Collectors.toList());
    }

    protected Metric getMetricByTitle(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title));
    }

    protected Metric getMetricByIdentifier(String id) {
        return getMdService().getObj(getProject(), Metric.class, identifier(id));
    }

    protected Fact getFactByTitle(String title) {
        return getMdService().getObj(getProject(), Fact.class, title(title));
    }

    protected Fact getFactByIdentifier(String id) {
        return getMdService().getObj(getProject(), Fact.class, identifier(id));
    }

    protected Dataset getDatasetByIdentifier(String id) {
        return getMdService().getObj(getProject(), Dataset.class, identifier(id));
    }

    protected Dataset getDatasetByTitle(String title) {
        return getMdService().getObj(getProject(), Dataset.class, title(title));
    }

    protected List<String> getObjIdentifiers(List<String> uris) {
        try {
            JSONArray array = RestUtils.getJsonObject(getRestApiClient(),
                    getRestApiClient().newPostMethod(
                            String.format("/gdc/md/%s/identifiers", testParams.getProjectId()),
                            new JSONObject().put("uriToIdentifier", uris).toString()))
                    .getJSONArray("identifiers");

            List<String> foundIds = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                 foundIds.add(array.getJSONObject(i).getString("identifier"));
            }

            return foundIds;
        } catch (IOException | JSONException e) {
            throw new RuntimeException("there is an error while searching obj", e);
        }
    }

    protected Report getReportByTitle(String title) {
        return getMdService().getObj(getProject(), Report.class, title(title));
    }
    //------------------------- SUPPORT GET OBJECTS - END -------------------------

    //------------------------- DASHBOARD MD OBJECTS - BEGIN -------------------------
    protected FilterItemContent createSingleValueFilter(Attribute attribute) {
        return Builder.of(FilterItemContent::new).with(item -> {
            item.setObjUri(attribute.getDefaultDisplayForm().getUri());
            item.setType(FilterType.LIST);
        }).build();
    }

    protected FilterItemContent createSingleValuesFilterBy(String uri) {
        return Builder.of(FilterItemContent::new).with(item -> {
            item.setObjUri(uri);
            item.setType(FilterType.LIST);
        }).build();
    }

    protected FilterItemContent createMultipleValuesFilterBy(String uri) {
        return Builder.of(FilterItemContent::new).with(item -> {
            item.setObjUri(uri);
            item.setMultiple(true);
            item.setType(FilterType.LIST);
        }).build();
    }

    protected FilterItemContent createMultipleValuesFilter(Attribute attribute) {
        return Builder.of(FilterItemContent::new).with(item -> {
            item.setObjUri(attribute.getDefaultDisplayForm().getUri());
            item.setMultiple(true);
            item.setType(FilterType.LIST);
        }).build();
    }

    protected FilterItemContent createDateFilter(Attribute date, int from, int to) {
        return Builder.of(FilterItemContent::new).with(item -> {
            item.setObjUri(date.getDefaultDisplayForm().getUri());
            item.setMultiple(true);
            item.setType(FilterType.TIME);
            item.setFilterConstraint(new FloatingFilterConstraint(from, to));
        }).build();
    }

    protected ReportItem createReportItem(String reportUri) {
        return createReportItem(reportUri, null);
    }

    protected ReportItem createReportItem(String reportUri, List<String> filterIds) {
        return Builder.of(ReportItem::new).with(item -> {
            item.setObjUri(reportUri);
            if (!Objects.isNull(filterIds)) {
                item.setAppliedFilterIds(filterIds);
            }
        }).build();
    }

    protected FilterItem createFilterItem(FilterItemContent item) {
        return Builder.of(FilterItem::new).with(filterItem -> filterItem.setContentId(item.getId())).build();
    }

    protected FilterItem createFilterItem(FilterItemContent item, TabItem.ItemPosition itemPosition) {
        return Builder.of(FilterItem::new).with(filterItem -> {
            filterItem.setContentId(item.getId());
            filterItem.setPosition(itemPosition);
        }).build();
    }
    //------------------------- DASHBOARD MD OBJECTS - END ------------------------

    //------------------------- REPORT, METRIC MD OBJECTS - BEGIN ------------------------

    protected String createReport(ReportDefinition reportDefinition) {
        ReportDefinition definition = getMdService().createObj(getProject(), reportDefinition);
        return getMdService().createObj(getProject(), new Report(definition.getTitle(), definition)).getUri();
    }

    protected Metric createMetric(String name, String expression) {
        return createMetric(name, expression, DEFAULT_METRIC_FORMAT);
    }

    protected Metric createMetric(String name, String expression, String format) {
        return createMetric(getGoodDataClient(), name, expression, format);
    }

    protected Metric createMetric(GoodData gooddata, String name, String expression, String format) {
        return gooddata.getMetadataService().createObj(getProject(), new Metric(name, expression, format));
    }

    protected Metric createMetricIfNotExist(GoodData gooddata, String name, String expression, String format) {
        try {
            return getMetricByTitle(name);
        } catch (ObjNotFoundException e) {
            return gooddata.getMetadataService().createObj(getProject(), new Metric(name, expression, format));
        }
    }

    protected String createNewEmptyProject(final RestProfile profile, final String projectTitle) {
        RestClient restClient = new RestClient(profile);
        return createNewEmptyProject(restClient, projectTitle);
    }

    protected String createNewEmptyProject(final RestClient restClient, final String projectTitle) {
        final Project project = new Project(projectTitle, testParams.getAuthorizationToken());
        project.setDriver(testParams.getProjectDriver());
        project.setEnvironment(testParams.getProjectEnvironment());

        return restClient.getProjectService().createProject(project).get().getId();
    }

    protected String createNewEmptyProject(final String projectTitle) {
        return createNewEmptyProject(getProfile(ADMIN), projectTitle);
    }

    protected void deleteProject(final RestProfile profile, final String projectId) {
        RestClient restClient = new RestClient(profile);
        final ProjectService service = restClient.getProjectService();
        service.removeProject(service.getProjectById(projectId));
    }

    protected void deleteProject(final String projectId) {
        deleteProject(getProfile(ADMIN), projectId);
    }

    //------------------------- REPORT, METRIC MD OBJECTS - END ------------------------

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

    private void adjustWindowSize(String windowSize) {
        // this is now using for ui-tests-dashboards only
        // if having wide use, we should consider dimension properties
        // drone definition: http://arquillian.org/arquillian-extension-drone/#webdriver-configuration (see property named dimensions)
        // e.g.: https://github.com/arquillian/arquillian-extension-drone/blob/master/drone-webdriver/src/test/resources/arquillian.xml#L31
        String[] dimensions = windowSize.split(",");
        if (dimensions.length == 2) {
            try {
                setWindowSize(Integer.valueOf(dimensions[0]), Integer.valueOf(dimensions[1]));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("ERROR: Invalid window size given: " + windowSize);
            }
        } else {
            throw new IllegalStateException("ERROR: Invalid window size given: " + windowSize);
        }
    }

    private void setWindowSize(final int width, final int height) {
        log.info("resizing window to " + width + "x" + height);
        browser.manage().window().setSize(new Dimension(width, height));
    }
}
