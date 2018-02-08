package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.greypages.account.AccountLoginFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceUsersFragment;
import com.gooddata.qa.graphene.fragments.greypages.gdc.GdcFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2.Manage2Fragment;
import com.gooddata.qa.graphene.fragments.greypages.md.ldm.singleloadinterface.SingleLoadInterfaceFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp.ExportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp.PartialExportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp.ImportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp.PartialImportFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectElementsFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectFragment;
import com.gooddata.qa.graphene.fragments.greypages.md.query.attributes.QueryAttributesFragment;
import com.gooddata.qa.graphene.fragments.greypages.projects.ProjectFragment;
import com.gooddata.qa.models.GraphModel;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.model.ModelRestUtils;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.webdav.WebDavClient;
import com.google.common.base.Predicate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static java.util.Objects.isNull;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AbstractGreyPageTest extends AbstractTest {

    protected static final By BY_GP_FORM = By.tagName("form");
    protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
    protected static final By BY_GP_PRE_JSON = By.tagName("pre");
    protected static final By BY_GP_LINK = By.tagName("a");
    protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");

    protected static final String PAGE_GDC = "gdc";
    protected static final String PAGE_GDC_MD = PAGE_GDC + "/md";
    protected static final String PAGE_GDC_PROJECTS = PAGE_GDC + "/projects";
    protected static final String PAGE_ACCOUNT_LOGIN = PAGE_GDC + "/account/login";
    protected static final String HOST_NAME = "%{HostName}";
    protected static final String PROJECT_ID = "%{ProjectID}";
    protected static final String PAGE_TOKEN = PAGE_GDC + "/account/token";
    /**
     * ----- Grey pages fragments -----
     */

    @FindBy(tagName = "form")
    protected AccountLoginFragment gpLoginFragment;

    @FindBy(tagName = "form")
    protected ProjectFragment gpProject;

    @FindBy(className = "param")
    protected GdcFragment gdcFragment;

    @FindBy(tagName = "form")
    protected Manage2Fragment manage2Fragment;

    @FindBy(tagName = "form")
    protected ExportFragment exportFragment;

    @FindBy(tagName = "form")
    protected PartialExportFragment partialExportFragment;

    @FindBy(tagName = "form")
    protected ImportFragment importFragment;

    @FindBy(tagName = "form")
    protected PartialImportFragment partialImportFragment;

    @FindBy(tagName = "pre")
    protected QueryAttributesFragment queryAttributesFragment;

    @FindBy(tagName = "pre")
    protected ObjectFragment objectFragment;

    @FindBy(tagName = "pre")
    protected ObjectElementsFragment objectElementsFragment;

    @FindBy(tagName = "form")
    protected SingleLoadInterfaceFragment singleLoadInterfaceFragment;

    @FindBy(tagName = "form")
    protected InstanceFragment storageForm;

    @FindBy(tagName = "form")
    protected InstanceUsersFragment storageUsersForm;

    public JSONObject loadJSON() throws JSONException {
        waitForElementPresent(BY_GP_PRE_JSON, browser);
        return new JSONObject(browser.findElement(BY_GP_PRE_JSON).getText());
    }

    public void signInAtGreyPages(String username, String password) throws JSONException {
        openUrl(PAGE_ACCOUNT_LOGIN);
        waitForElementPresent(gpLoginFragment.getRoot());
        gpLoginFragment.login(username, password);
        Screenshots.takeScreenshot(browser, "login-gp", this.getClass());
    }

    public void postMAQL(String maql, int statusPollingCheckIterations) throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/ldm/manage2");
        waitForElementPresent(manage2Fragment.getRoot());
        waitForElementPresent(manage2Fragment.getRoot());
        assertTrue(manage2Fragment.postMAQL(maql, statusPollingCheckIterations), "MAQL was not successfully processed");
    }

    public String uploadFileToWebDav(URL resourcePath, String webContainer) throws URISyntaxException {
        refreshToken();
        WebDavClient webDav = WebDavClient.getInstance(testParams.getUser(), testParams.getPassword());
        File resourceFile = new File(resourcePath.toURI());
        if (webContainer == null) {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructure(gdcFragment.getUserUploadsURL()),
                    "Create WebDav storage structure");
        } else {
            openUrl(PAGE_GDC);
            waitForElementPresent(gdcFragment.getRoot());
            assertTrue(webDav.createStructureIfNotExists(gdcFragment.getUserUploadsURL()),
                    "Create WebDav storage structure if not exists");
            webDav.setWebDavStructure(webContainer);
        }

        webDav.uploadFile(resourceFile);
        return webDav.getWebDavStructure();
    }

    public InputStream getFileFromWebDav(String webContainer, URL resourcePath)
            throws URISyntaxException, IOException {
        File resourceFile = new File(resourcePath.toURI());
        return WebDavClient.getInstance(testParams.getUser(), testParams.getPassword())
                .getFile(webContainer + "/" + resourceFile.getName());
    }

    public String exportProject(boolean exportUsers, boolean exportData, boolean crossDataCenter,
            int statusPollingCheckIterations) throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/export");
        waitForElementPresent(exportFragment.getRoot());
        return exportFragment.invokeExport(exportUsers, exportData, crossDataCenter, statusPollingCheckIterations);
    }

    public String exportPartialProject(String exportObjectUri, int statusPollingCheckIterations)
            throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/partialmdexport");
        waitForElementPresent(partialExportFragment.getRoot());
        return partialExportFragment.invokeExport(exportObjectUri, statusPollingCheckIterations);
    }

    public void importProject(String exportToken, int statusPollingCheckIterations)
            throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/import");
        waitForElementPresent(importFragment.getRoot());
        assertTrue(importFragment.invokeImport(exportToken, statusPollingCheckIterations),
                "Project import failed");
    }

    public void importPartialProject(String exportToken, int statusPollingCheckIterations)
            throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/maintenance/partialmdimport");
        waitForElementPresent(partialImportFragment.getRoot());
        assertTrue(partialImportFragment.invokeImport(exportToken, statusPollingCheckIterations),
                "Partial project import failed");
    }

    public JSONObject fetchSLIManifest(String dataset) throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/ldm/singleloadinterface");
        waitForElementPresent(singleLoadInterfaceFragment.getRoot());
        return singleLoadInterfaceFragment.postDataset(dataset);
    }

    public int getAttributeID(String attributeTitle) throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/query/attributes");
        waitForElementPresent(queryAttributesFragment.getRoot());
        return queryAttributesFragment.getAttributeIDByTitle(attributeTitle);
    }

    public JSONObject getObjectByID(int objectID) throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/obj/" + objectID);
        waitForElementPresent(objectFragment.getRoot());
        return objectFragment.getObject();
    }

    public ArrayList<Pair<String, Integer>> getObjectElementsByID(int objectID) throws JSONException {
        refreshToken();
        openUrl(PAGE_GDC_MD + "/" + testParams.getProjectId() + "/obj/" + objectID + "/elements");
        waitForElementPresent(objectElementsFragment.getRoot());
        return objectElementsFragment.getObjectElements();
    }

    /**
     * A hook for inviting users from other roles to project.
     * @throws ParseException
     * @throws JSONException
     * @throws IOException
     */
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        log.warning("This test does not need to invite users into project!");
    }

    protected void addUserToProject(String email, UserRoles userRole) throws ParseException, IOException, JSONException {
        UserManagementRestUtils.addUserToProject(
                testParams.getDomainUser() != null ? getDomainUserRestApiClient() : getRestApiClient(),
                        testParams.getProjectId(), email, userRole);
    }

    /**
     * Create and add dynamic user to project with specific role.
     * This action is mandatory full support for EDITOR and VIEWER. Other roles (DASHBOARD_ONLY, ADMIN) just appear
     * in rarely case, so they will not be cached in TestParameter and assume the password is same as the main user.
     * All dynamic user will be automatically deleted after test.
     * @param role
     * @return dynamic user email
     * @throws ParseException
     * @throws JSONException
     * @throws IOException
     */
    protected String createAndAddUserToProject(UserRoles role) throws ParseException, JSONException, IOException {
        RestApiClient restApiClient;
        String domainUser;

        if (isNull(testParams.getDomainUser())) {
            restApiClient = getRestApiClient();
            domainUser = testParams.getUser();
        } else {
            restApiClient = getDomainUserRestApiClient();
            domainUser = testParams.getDomainUser();
        }

        String dynamicUser = createDynamicUserFrom(domainUser.replace("@", "+" + role.getName().toLowerCase() + "@"));
        UserManagementRestUtils.addUserToProject(restApiClient, testParams.getProjectId(), dynamicUser, role);

        switch (role) {
            case EDITOR:
                testParams.setEditorUser(dynamicUser);
                return dynamicUser;
            case VIEWER:
                testParams.setViewerUser(dynamicUser);
                return dynamicUser;
            case DASHBOARD_ONLY:
                testParams.setDashboardOnlyUser(dynamicUser);
                return dynamicUser;
            default:
                return dynamicUser;
        }
    }

    /**
     * Create a dynamic user and automatically delete it after test.
     * Note: UserManagementRestUtils.createUser() is not automatically delete it.
     * @param email
     * @return dynamic user email
     * @throws ParseException
     * @throws JSONException
     * @throws IOException
     */
    protected String createDynamicUserFrom(String email) throws ParseException, JSONException, IOException {
        RestApiClient restApiClient = testParams.getDomainUser() == null ? getRestApiClient() : getDomainUserRestApiClient();
        String dynamicUser = generateEmail(email);

        UserManagementRestUtils.createUser(restApiClient, testParams.getUserDomain(), dynamicUser, testParams.getPassword());
        log.info(String.format("User %s is created successfully", dynamicUser));
        extraUsers.add(dynamicUser);

        return dynamicUser;
    }

    private void refreshToken() {
        openUrl(PAGE_TOKEN);
        Predicate<WebDriver> tokenIsRefreshed = browser -> waitForElementPresent(By.tagName("p"), browser)
                .getText()
                .equals("Your authentication context has been refreshed and stays valid next 10 minutes.");
        Graphene.waitGui()
            .pollingEvery(1, TimeUnit.SECONDS)
            .withTimeout(5, TimeUnit.SECONDS)
            .until(tokenIsRefreshed);
    }
}
