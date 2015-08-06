package com.gooddata.qa.graphene.datawarehouse;

import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceFragment;
import com.gooddata.qa.graphene.fragments.greypages.datawarehouse.InstanceUsersFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.*;

@Test(groups = {"datawarehouse"}, description = "Basic verification of datawarehouse restapi in GD platform")
public class BasicDatawarehouseRestTest extends AbstractDatawarehouseTest {

    private String storageUrl;
    private String userCreatedByUrl;
    private String userCreatedById;

    private String testUserId;
    private String testUserLogin;
    private String authorizationToken;

    private static final String STORAGE_TITLE = "Storage";
    public static final String UPDATED_STORAGE_TITLE = STORAGE_TITLE + " updated";
    private static final String STORAGE_DESCRIPTION = "Description";
    public static final String UPDATED_STORAGE_DESCRIPTION = STORAGE_DESCRIPTION + " updated";

    private static final String NEW_USER_ROLE = "dataAdmin";
    private static final String NEW_USER_UPDATED_ROLE = "admin";

    @BeforeClass
    public void initProperties() {
          startPageContext = new StartPageContext() {
            
            @Override
            public void waitForStartPageLoaded() {
                waitForFragmentVisible(storageForm);
            }
            
            @Override
            public String getStartPage() {
                return PAGE_INSTANCES;
            }
        };
        testUserId = testParams.loadProperty("dss.storage.test.user.id");
        testUserLogin = testParams.loadProperty("dss.storage.test.user.login");
        authorizationToken = testParams.loadProperty("dss.authorizationToken");
    }

    @Test(groups = {"datawarehouseInit"})
    public void resourceStoragesNotAvailableForAnonymous() throws JSONException {
        waitForElementPresent(gpLoginFragment.getRoot());
        assertTrue(browser.getCurrentUrl().contains("gdc/account/login"),
                "Redirect to /gdc/account/login wasn't done for anonymous user");
    }

    @Test(groups = {"datawarehouseInit"}, dependsOnMethods = {"resourceStoragesNotAvailableForAnonymous"})
    public void resourceStoragesAvailable() throws JSONException {
        signInAtGreyPages(testParams.getUser(), testParams.getPassword());

        verifyStorageCreateFormPresentWithTrailingSlash();
        JSONObject json = loadJSON();
        assertTrue(json.getJSONObject("instances").has("items"), "Instances with items array is not available");
        takeScreenshot(browser, "datawarehouse-base-resource", this.getClass());
    }

    @Test(dependsOnGroups = {"datawarehouseInit"})
    public void gpInstanceFormsAvailable() {
        waitForElementPresent(storageForm.getRoot());
    }

    @Test(dependsOnGroups = {"datawarehouseInit"})
    public void datawarehouseResourceLinkNotAvailableAtBasicResource() {
        openUrl(PAGE_GDC);
        assertEquals(browser.getTitle(), "GoodData API root");
        assertTrue(browser.findElements(By.partialLinkText("instances")).size() == 0,
                "Instances link is present at basic /gdc resource");
    }

    @Test(dependsOnGroups = {"datawarehouseInit"})
    public void verifyDatawarehouseRoot() throws JSONException {
        openUrl(PAGE_ROOT);

        final JSONObject json = loadJSON();
        final JSONObject object = json.getJSONObject("datawarehouse");
        final JSONObject linksObject = object.getJSONObject("links");
        assertEquals("/gdc/datawarehouse", linksObject.getString("self"));
        assertEquals("/gdc", linksObject.getString("parent"));
        assertEquals("/gdc/datawarehouse/instances", linksObject.getString("instances"));

        assertTrue(browser.findElements(By.linkText("/gdc/datawarehouse")).size() == 1, "Root does not contain self link");
        assertTrue(browser.findElements(By.linkText("/gdc")).size() == 1, "Root does not contain parent link");
        List<WebElement> instancesLink = browser.findElements(By.linkText("/gdc/datawarehouse/instances"));
        assertTrue(instancesLink.size() == 1, "Root does not contain instances link");
        instancesLink.get(0).click();

        assertEquals(getBasicRootUrl() + "/gdc/datawarehouse/instances", browser.getCurrentUrl());
    }

    @Test(dependsOnGroups = {"datawarehouseInit"})
    public void verifyDefaultResource() throws JSONException {
        verifyStoragesResourceJSON();
    }

    /**
     * ===================== Section with valid storage cases =================
     */

    @Test(dependsOnMethods = {"gpInstanceFormsAvailable"})
    public void verifyStorageCreateFormPresentWithTrailingSlash() throws JSONException {
        openUrl(PAGE_INSTANCES + "/");
        waitForElementVisible(storageForm.getRoot());
    }

    @Test(dependsOnMethods = {"gpInstanceFormsAvailable"})
    public void createInstance() throws JSONException, InterruptedException {
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
        storageUrl = storageForm.createStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, authorizationToken);
    }

    @Test(dependsOnMethods = {"createInstance"})
    public void verifyDefaultResourceAfterStorageCreated() throws JSONException {
        verifyStoragesResourceJSON();
    }

    @Test(dependsOnMethods = {"createInstance"})
    public void verifyInstanceUpdateFormPresentWithTrailingSlash() throws JSONException {
        browser.get(getBasicRootUrl() + storageUrl + "/");
        waitForElementVisible(storageForm.getRoot());
    }

    @Test(dependsOnMethods = {"createInstance"})
    public void verifyInstanceEnabled() throws JSONException {
        verifyStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, "ENABLED");
        verifyStorageUsers();
        verifyStorageSchemas();
        verifyStorageSchema("default", "Default schema for new ADS instance");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void updateInstance() throws JSONException {
        openStorageUrl();
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION),
                "Edit form doesn't contain current values");
        storageForm.updateStorage(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION);
        assertTrue(storageForm.verifyValidEditStorageForm(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION),
                "Edit form doesn't contain expected values");
        takeScreenshot(browser, "datawarehouse-updated-storage", this.getClass());
    }

    @Test(dependsOnMethods = {"updateInstance", "removeUserFromInstanceByLogin"}, alwaysRun = true)
    public void deleteInstance() throws JSONException {
        openStorageUrl();
        waitForElementVisible(BY_GP_FORM_SECOND, browser);
        InstanceFragment storage = createPageFragment(InstanceFragment.class, browser.findElement(BY_GP_FORM_SECOND));
        assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
        storage.deleteStorageSuccess();
    }

    /**
     * ===================== Section with invalid storage cases =================
     */

    @Test(dependsOnMethods = {"gpInstanceFormsAvailable"})
    public void createInstanceWithoutTitle() throws JSONException {
        createInvalidStorage(null, STORAGE_DESCRIPTION, authorizationToken, "title must not be empty");
    }

    @Test(dependsOnMethods = {"gpInstanceFormsAvailable"})
    public void createInstanceWithoutAuthToken() throws JSONException {
        createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, null, "token must not be empty");
    }

    @Test(dependsOnMethods = {"gpInstanceFormsAvailable"})
    public void createStorageWithNonexistentAuthToken() throws JSONException {
        createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, "nonexistentAuthToken",
                "Project group with name 'nonexistentAuthToken' does not exists.");
    }

    @Test(dependsOnMethods = {"gpInstanceFormsAvailable"})
    public void createStorageWithInvalidAuthToken() throws JSONException {
        // use non-datawarehouse-enabled authorization token to create a new datawarehouse
        createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, testParams.getAuthorizationToken(),
                "Project group with name '" + testParams.getAuthorizationToken() +
                        "' does not have valid connection information");
    }

    @Test(dependsOnMethods = {"createInstance"})
    public void updateStorageWithEmptyTitle() throws JSONException {
        invalidUpdateOfStorage(null, STORAGE_DESCRIPTION, "title must not be empty");
    }

    /**
     * ===================== Section with valid storage users cases ============
     */

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserToInstance() throws JSONException, InterruptedException {
        openStorageUsersUrl();
        assertTrue(testUserId != null, "Missing test user ID - provide property 'dss.storage.test.user.id'");
        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_ROLE, getTestUserProfileUri(), null, true);
        takeScreenshot(browser, "datawarehouse-add-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void verifyStorageUsersAddFormPresentWithTrailingSlash() throws JSONException {
        browser.get(getBasicRootUrl() + getStorageUsersUrl() + "/");
        waitForElementVisible(storageUsersForm.getRoot());
    }

    @Test(dependsOnMethods = {"addUserToInstance"})
    public void verifyStorageUsersUpdateFormPresentWithTrailingSlash() throws JSONException {
        browser.get(getAddedUserUrlWithHost() + "/");
        waitForElementVisible(storageUsersForm.getRoot());
    }

    @Test(dependsOnMethods = "addUserToInstance")
    public void verifyInstanceAddedUser() throws JSONException {
        verifyUser(NEW_USER_ROLE, "datawarehouse-added-user");
    }

    @Test(dependsOnMethods = "verifyInstanceAddedUser")
    public void updateInstanceUser() throws JSONException {
        browser.get(getAddedUserUrlWithHost());
        storageUsersForm.verifyValidUpdateUserForm(NEW_USER_ROLE, getTestUserProfileUri());
        storageUsersForm.fillUpdateUserForm(NEW_USER_UPDATED_ROLE, getTestUserProfileUri());
        takeScreenshot(browser, "datawarehouse-update-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }

    @Test(dependsOnMethods = "updateInstanceUser")
    public void verifyUpdatedUser() throws JSONException {
        verifyUser(NEW_USER_UPDATED_ROLE, "datawarehouse-updated-user");
    }

    @Test(dependsOnMethods = {"verifyUpdatedUser"})
    public void removeUserFromInstance() throws Exception {
        browser.get(getAddedUserUrlWithHost());
        final InstanceUsersFragment deleteFragment =
                createPageFragment(InstanceUsersFragment.class, browser.findElement(BY_GP_FORM_SECOND));
        deleteFragment.verifyValidDeleteUserForm();
        deleteFragment.deleteUser();
        final JSONObject jsonObject = loadJSON();
        assertEquals(browser.getCurrentUrl(), getBasicRootUrl() + storageUrl + "/users");
        assertEquals(1, jsonObject.getJSONObject("users").getJSONArray("items").length());
    }

    @Test(dependsOnMethods = {"removeUserFromInstance"})
    public void addUserToInstanceByLogin() throws JSONException, InterruptedException {
        openStorageUsersUrl();
        assertTrue(testUserId != null, "Missing test user ID - provide property 'dss.storage.test.user.id'");
        assertTrue(testUserLogin != null, "Missing test user login - provide property 'dss.storage.test.user.login'");
        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_ROLE, null, testUserLogin, true);
        takeScreenshot(browser, "datawarehouse-add-user-filled-form-login", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }

    @Test(dependsOnMethods = "addUserToInstanceByLogin")
    public void verifyInstanceAddedUserByLogin() throws JSONException {
        verifyInstanceAddedUser();
    }

    @Test(dependsOnMethods = {"verifyInstanceAddedUserByLogin"})
    public void removeUserFromInstanceByLogin() throws Exception {
        removeUserFromInstance();
    }

    /**
     * ===================== Section with invalid storage users cases ============
     */

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserWithEmptyProfileAndLogin() throws JSONException, InterruptedException {
        invalidUserAssignment(null, null, NEW_USER_ROLE, "One (and only one) of 'profile' or 'login' must be " +
                "provided.");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserWithBothProfileAndLogin() throws JSONException, InterruptedException {
        invalidUserAssignment(getTestUserProfileUri(), testUserLogin, NEW_USER_ROLE,
                "One (and only one) of 'profile' or 'login' must be provided.");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addNonExistingUser() throws JSONException, InterruptedException {
        invalidUserAssignment("/gdc/account/profile/nonexisting", null, NEW_USER_ROLE,
                "User 'nonexisting' has not been found");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserWithInvalidURI() throws JSONException, InterruptedException {
        invalidUserAssignment("/invalid/uri", null, NEW_USER_ROLE, "Validation failed");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserWithInvalidLogin() throws JSONException, InterruptedException {
        invalidUserAssignment(null, "asdfasdfa", NEW_USER_ROLE,
                "must be a string matching the regular expression \".+@.+\\..+\"]]");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserWithInvalidURI2() throws JSONException, InterruptedException {
        invalidUserAssignment("/gdc/account/profile", null, NEW_USER_ROLE, "Validation failed");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addUserWithInvalidURI3() throws JSONException, InterruptedException {
        invalidUserAssignment("/gdc/account/profile/", null, NEW_USER_ROLE, "Validation failed");
    }

    @Test(dependsOnMethods = {"verifyInstanceEnabled"})
    public void addExistingUserToStorage() throws JSONException, InterruptedException {
        invalidUserAssignment(userCreatedByUrl, null, NEW_USER_ROLE, "User '" + userCreatedById + "' already exists");
    }

    /**
     * ===================== Section with invalid storage state cases ============
     */

    @Test(dependsOnMethods = {"deleteInstance"})
    public void verifyDeletedInstance() throws JSONException, InterruptedException {
        openStorageUrl();
        verifyStorage(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION, "DELETED");
    }

    @Test(dependsOnMethods = {"verifyDeletedInstance"})
    public void deleteDeletedInstance() throws JSONException, InterruptedException {
        openStorageUrl();
        waitForElementVisible(BY_GP_FORM_SECOND, browser);
        InstanceFragment storage = createPageFragment(InstanceFragment.class, browser.findElement(BY_GP_FORM_SECOND));
        assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
        storage.deleteStorage();
        verifyDeletedInstanceNotReady(storageUrl);
    }

    @Test(dependsOnMethods = {"deleteDeletedInstance"})
    public void getUsersInDeletedStorage() throws JSONException {
        browser.get(getBasicRootUrl() + getStorageUsersUrl());
        verifyDeletedInstanceNotReady(getStorageUsersUrl());
    }

    @Test(dependsOnMethods = {"deleteDeletedInstance"})
    public void getJdbcInfoInDeletedStorage() throws JSONException {
        browser.get(getBasicRootUrl() + getStorageJdbcUrl());
        verifyDeletedInstanceNotReady(getStorageJdbcUrl());
    }

    @Test(dependsOnMethods = {"deleteDeletedInstance"})
    public void updateDeletedStorage() throws JSONException {
        openStorageUrl();
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidEditStorageForm(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION),
                "Edit form doesn't contain current values");
        storageForm.updateStorage(UPDATED_STORAGE_TITLE, UPDATED_STORAGE_DESCRIPTION);
        verifyDeletedInstanceNotReady(storageUrl);
    }

    // TODO add next invalid cases when permissions are implemented

    /**
     * ===================== HELP methods =================
     */

    private void openStorageUrl() {
        browser.get(getBasicRootUrl() + storageUrl);
        waitForElementVisible(storageForm.getRoot());
        waitForElementPresent(BY_GP_PRE_JSON, browser);
    }

    private void openStorageUsersUrl() {
        browser.get(getBasicRootUrl() + getStorageUsersUrl());
        waitForElementVisible(storageUsersForm.getRoot());
        waitForElementPresent(BY_GP_PRE_JSON, browser);
    }

    private String getStorageUsersUrl() {
        return storageUrl + "/users";
    }

    private void openStorageSchemasUrl() {
        openStorageSchemaUrl(null);
    }

    private void openStorageSchemaUrl(final String schemaName) {
        final String suffix = (schemaName == null) ? "" : "/" + schemaName;
        browser.get(getBasicRootUrl() + getStorageSchemasUrl() + suffix);
        waitForElementPresent(BY_GP_PRE_JSON, browser);
    }

    private String getStorageSchemasUrl() {
        return storageUrl + "/schemas";
    }

    private String getStorageJdbcUrl() {
        return storageUrl + "/jdbc";
    }

    private String getTestUserProfileUri() {
        return "/gdc/account/profile/" + testUserId;
    }

    private String getAddedUserUrl() {
        return getStorageUsersUrl() + "/" + testUserId;
    }

    private String getAddedUserUrlWithHost() {
        return getBasicRootUrl() + getAddedUserUrl();
    }

    private void createInvalidStorage(String title, String description, String authorizationToken,
                                      String expectedErrorMessageSubstring) throws JSONException {
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
        storageForm.fillCreateStorageForm(title, description, authorizationToken);
        verifyErrorMessage(expectedErrorMessageSubstring, PAGE_INSTANCES);
    }

    private void verifyUser(final String role, final String screenshotName) throws JSONException {
        browser.get(getAddedUserUrlWithHost());

        final JSONObject json = loadJSON();
        takeScreenshot(browser, screenshotName, this.getClass());
        final JSONObject userObject = json.getJSONObject("user");
        assertEquals(role, userObject.getString("role"));
        assertEquals(getTestUserProfileUri(), userObject.getString("profile"));
        assertEquals(getAddedUserUrl(), userObject.getJSONObject("links").getString("self"));
        assertEquals(storageUrl + "/users", userObject.getJSONObject("links").getString("parent"));
    }

    private void invalidUserAssignment(String userProfile, String login, String role, String expectedErrorMessage)
            throws JSONException, InterruptedException {
        openStorageUsersUrl();
        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(role, userProfile, login, false);
        verifyErrorMessage(expectedErrorMessage, storageUrl + "/users");
    }

    private void invalidUpdateOfStorage(String title, String description, String expectedErrorMessage)
            throws JSONException {
        openStorageUrl();
        waitForElementVisible(storageForm.getRoot());
        assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION),
                "Edit form doesn't contain current values");
        storageForm.updateStorage(title, description);
        verifyErrorMessage(expectedErrorMessage, storageUrl);
    }

    private void verifyErrorMessage(String messageSubstring, String expectedPage) throws JSONException {
        assertTrue(browser.getCurrentUrl().endsWith(expectedPage), "Browser was redirected at another page");
        JSONObject json = loadJSON();
        String errorMessage = json.getJSONObject("error").getString("message");
        assertTrue(errorMessage.contains(messageSubstring), "Another error message present: " + errorMessage +
                ", expected message substring: " + messageSubstring);
    }

    private void verifyStoragesResourceJSON() throws JSONException {
        JSONObject json = loadJSON();
        assertTrue(json.getJSONObject("instances").has("items"), "Instances with items array is not available");
        JSONArray storagesItems = json.getJSONObject("instances").getJSONArray("items");
        if (storagesItems.length() > 0) {
            JSONObject firstStorage = storagesItems.getJSONObject(0).getJSONObject("instance");
            assertTrue(firstStorage.has("title"), "Instance title isn't present");
            assertTrue(firstStorage.has("description"), "Instance description isn't present");
            assertTrue(firstStorage.has("authorizationToken"), "Instance authorizationToken isn't present");
            assertTrue(firstStorage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_INSTANCES),
                    "Instance parent link doesn't match");
            assertTrue(firstStorage.getJSONObject("links").has("self"), "Instance self link isn't present");
            assertTrue(firstStorage.getJSONObject("links").has("users"), "Instance users link isn't present");
            assertTrue(firstStorage.getJSONObject("links").has("jdbc"), "Instance jdbc link isn't present");
            assertTrue(firstStorage.getJSONObject("links").has("schemas"), "Instance schemas link isn't present");
            assertTrue(firstStorage.has("status"), "Instance status isn't present");
        }
        assertTrue(json.getJSONObject("instances").getJSONObject("links").getString("parent").endsWith("datawarehouse"),
                "Parent link doesn't match");
        assertTrue(json.getJSONObject("instances").getJSONObject("links").getString("self").substring(1).equals(
                PAGE_INSTANCES), "Instances self link doesn't match");
    }

    private void verifyStorage(final String title, final String description, final String state) throws JSONException {
        openStorageUrl();
        takeScreenshot(browser, "datawarehouse-simple-storage", this.getClass());
        JSONObject json = loadJSON();
        assertTrue(json.has("instance"), "Instance element isn't present");
        JSONObject storage = json.getJSONObject("instance");
        assertTrue(storage.getString("title").equals(title), "Instance title doesn't match");
        assertTrue(storage.getString("description").equals(description), "Instance description doesn't match");
        assertTrue(storage.getString("environment").equals("TESTING"), "Instance environment is TESTING");
        assertTrue(storage.getString("authorizationToken").equals(authorizationToken),
                "Instance authorizationToken doesn't match");
        assertTrue(storage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_INSTANCES),
                "Instance parent link doesn't match");
        assertTrue(storage.getJSONObject("links").getString("self").equals(storageUrl),
                "Instance self link doesn't match");
        assertTrue(storage.getJSONObject("links").getString("users").equals(storageUrl + "/users"),
                "Instance users link doesn't match");
        assertTrue(storage.getJSONObject("links").getString("jdbc").equals(getStorageJdbcUrl()),
                "Instance jdbc link doesn't match");
        assertTrue(storage.getJSONObject("links").getString("schemas").equals(getStorageSchemasUrl()),
                "Instance schemas link doesn't match");
        final String currentState = storage.getString("status");
        assertTrue(currentState.equals(state), format("Instance is in invalid state - %s.", currentState));
        userCreatedByUrl = storage.getString("createdBy");
        userCreatedById = userCreatedByUrl.substring(userCreatedByUrl.lastIndexOf("/") + 1);

        String updatedByUrl = storage.getString("updatedBy");
        assertEquals(updatedByUrl, userCreatedByUrl, "Instance createdBy and updatedBy attributes do not match");
        assertTrue(storage.has("created"), "Created time not present");
        assertTrue(storage.has("updated"), "Updated time not present");
        browser.get(getBasicRootUrl() + getStorageUsersUrl());
    }

    private void verifyStorageUsers() throws JSONException {
        JSONObject jsonUsers = loadJSON();
        assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("parent").equals(storageUrl),
                "Instance users parent link doesn't match");
        assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("self").equals(
                storageUrl + "/users"), "Instance users self link doesn't match");
        assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").length() == 1,
                "Number of users doesn't match");
        assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").getJSONObject(0).getJSONObject("user")
                .getString("profile").equals(userCreatedByUrl),
                "Creator in users doesn't match with creator in storage");
        browser.get(getBasicRootUrl() + userCreatedByUrl);
        JSONObject jsonUser = loadJSON();
        assertTrue(jsonUser.getJSONObject("accountSetting").getString("login").equals(testParams.getUser()),
                "Login of user in profile doesn't match");
    }

    private void verifyStorageSchemas() throws JSONException {
        openStorageSchemasUrl();
        final JSONObject jsonSchemas = loadJSON();
        assertEquals(jsonSchemas.getJSONObject("schemas").getJSONObject("links").getString("self"), getStorageSchemasUrl(),
                "Instance schemas self link doesn't match");
        assertEquals(jsonSchemas.getJSONObject("schemas").getJSONObject("links").getString("parent"), storageUrl,
                "Instance schemas parent link doesn't match");
        assertTrue(jsonSchemas.getJSONObject("schemas").getJSONArray("items").length() == 1,
                "Number of schemas doesn't match, default schema is missing");
    }

    private void verifyStorageSchema(final String schemaName, final String schemaDescription) throws JSONException{
        openStorageSchemaUrl(schemaName);
        final JSONObject jsonSchema = loadJSON();
        assertEquals(jsonSchema.getJSONObject("schema").getString("name"), schemaName,
                "Schema name doesn't match");
        assertEquals(jsonSchema.getJSONObject("schema").getString("description"), schemaDescription,
                "Schema description doesn't match");
        assertEquals(jsonSchema.getJSONObject("schema").getJSONObject("links").getString("self"), getStorageSchemasUrl() + "/" + schemaName,
                "Schema self link doesn't match");
        assertEquals(jsonSchema.getJSONObject("schema").getJSONObject("links").getString("parent"), getStorageSchemasUrl(),
                "Schema parent link doesn't match");
        assertEquals(jsonSchema.getJSONObject("schema").getJSONObject("links").getString("instance"), storageUrl,
                "Schema dss link doesn't match");
    }

    private void verifyDeletedInstanceNotReady(final String url) throws JSONException {
        verifyErrorMessage("is in DELETED state", url);
    }
}
