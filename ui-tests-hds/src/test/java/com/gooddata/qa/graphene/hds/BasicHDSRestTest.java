package com.gooddata.qa.graphene.hds;

import com.gooddata.qa.graphene.fragments.greypages.hds.StorageFragment;
import com.gooddata.qa.graphene.fragments.greypages.hds.StorageUsersFragment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.jboss.arquillian.graphene.Graphene.createPageFragment;
import static org.testng.Assert.*;

@Test(groups = { "hds" }, description = "Basic verification of hds restapi in GD platform")
public class BasicHDSRestTest extends AbstractHDSTest {
	
	private String storageUrl;
	private String userCreatedByUrl;
	
	private String testUserId;
	
	private static final String STORAGE_TITLE = "HDS storage";
	private static final String STORAGE_DESCRIPTION = "HDS description";
	private static final String STORAGE_COPY_OF = "/gdc/storages/${storageId}";

    private static final String NEW_USER_ROLE = "dataAdmin";
    private static final String NEW_USER_UPDATED_ROLE = "admin";
    
    @FindBy(tagName="form")
	private StorageFragment storageForm;

    @FindBy(tagName = "form")
    private StorageUsersFragment storageUsersForm;
	
	@BeforeClass
	public void initStartPage() {
		startPage = PAGE_GDC_STORAGES;
		testUserId = loadProperty("hds.storage.test.user.id");
	}
	
	@Test(groups = {"hdsInit"})
	public void resourceStoragesNotAvailableForAnonymous() throws JSONException {
		waitForElementPresent(BY_GP_PRE_JSON);
		assertTrue(browser.getCurrentUrl().contains("gdc/account/token"), "Redirect to /gdc/account/token wasn't done for anonymous user");
	}

	@Test(groups = {"hdsInit"}, dependsOnMethods = { "resourceStoragesNotAvailableForAnonymous" })
	public void resourceStoragesAvailable() throws JSONException {
		validSignInWithDemoUser(true);
		
		loadPlatformPageBeforeTestMethod();
		JSONObject json = loadJSON();
		assertTrue(json.getJSONObject("storages").has("items"), "storages with items array is not available");
		takeScreenshot(browser, "hds-base-resource", this.getClass());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void gpFormsAvailable() {
		waitForElementPresent(storageForm.getRoot());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void hdsResourceLinkNotAvailableAtBasicResource() {
		openUrl(PAGE_GDC);
		assertEquals(browser.getTitle(), "GoodData API root");
		assertTrue(browser.findElements(By.partialLinkText("storages")).size() == 0, "Storages link is present at basic /gdc resource");
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void verifyDefaultResource() throws JSONException {
		verifyStoragesResourceJSON();
	}
	
	/** ===================== Section with valid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void verifyStorageCreateFormPresentWithTrailingSlash() throws JSONException {
		openUrl(PAGE_GDC_STORAGES + "/");
		waitForElementVisible(storageForm.getRoot());
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorage() throws JSONException, InterruptedException {
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
		storageUrl = storageForm.createStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, authorizationToken, null);
	}
	
	@Test(dependsOnMethods = {"createStorage"})
	public void verifyDefaultResourceAfterStorageCreated() throws JSONException {
		verifyStoragesResourceJSON();
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void verifyStorageUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + storageUrl + "/");
		waitForElementVisible(storageForm.getRoot());
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void verifyStorage() throws JSONException {
		openStorageUrl();
		takeScreenshot(browser, "hds-simple-storage", this.getClass());
		JSONObject json = loadJSON();
		assertTrue(json.has("storage"), "Storage element isn't present");
		JSONObject storage = json.getJSONObject("storage");
		assertTrue(storage.getString("title").equals(STORAGE_TITLE), "Storage title doesn't match");
		assertTrue(storage.getString("description").equals(STORAGE_DESCRIPTION), "Storage description doesn't match");
		assertTrue(storage.getString("authorizationToken").equals(authorizationToken), "Storage authorizationToken doesn't match");
		assertTrue(storage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
		assertTrue(storage.getJSONObject("links").getString("self").equals(storageUrl), "Storage self link doesn't match");
		assertTrue(storage.getJSONObject("links").getString("users").equals(storageUrl + "/users"), "Storage users link doesn't match");
		assertTrue(storage.getString("status").equals("ENABLED"), "Storage isn't enabled");
		userCreatedByUrl = storage.getString("createdBy");
		String updatedByUrl = storage.getString("updatedBy");
		assertEquals(updatedByUrl, userCreatedByUrl, "Storage createdBy and updatedBy attributes do not match");
		assertTrue(storage.has("created"), "Created time not present");
		assertTrue(storage.has("updated"), "Updated time not present");
		browser.get(getBasicRootUrl() + getStorageUsersUrl());
		JSONObject jsonUsers = loadJSON();
		assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("parent").equals(storageUrl), "Storage users parent link doesn't match");
		assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("self").equals(storageUrl + "/users"), "Storage users self link doesn't match");
		assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").length() == 1, "Number of users doesn't match");
		assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").getJSONObject(0).getJSONObject("user").getString("profile").equals(userCreatedByUrl), "Creator in users doesn't match with creator in storage");
		browser.get(getBasicRootUrl() + userCreatedByUrl);
		JSONObject jsonUser = loadJSON();
		assertTrue(jsonUser.getJSONObject("accountSetting").getString("login").equals(user), "Login of user in profile doesn't match");
	}
	
	@Test(dependsOnMethods = { "verifyStorage" })
	public void updateStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storageForm.updateStorage(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated");
		assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated"), "Edit form doesn't contain expected values");
		takeScreenshot(browser, "hds-updated-storage", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateStorage", "removeUserFromStorage"}, alwaysRun = true)
	public void deleteStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		StorageFragment storage = createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
		storage.deleteStorage();
	}
	
	/** ===================== Section with invalid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutTitle() throws JSONException {
		createInvalidStorage(null, STORAGE_DESCRIPTION, authorizationToken, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutDescription() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, null, authorizationToken, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutAuthToken() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, null, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithInvalidCopyOfURI() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, authorizationToken, STORAGE_COPY_OF, "Malformed request");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyTitle() throws JSONException {
		invalidUpdateOfStorage(null, STORAGE_DESCRIPTION, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyDescription() throws JSONException {
		invalidUpdateOfStorage(STORAGE_TITLE, null, "Validation failed");
	}

    /** ===================== Section with valid storage users cases ============ */
    
	@Test(dependsOnMethods = {"verifyStorage"})
    public void addUserToStorage() {
        openStorageUsersUrl();
        assertTrue(testUserId != null, "Missing test user ID - provide property 'hds.storage.test.user.id'");
        storageUsersForm.verifyValidAddUserForm();
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_ROLE, getTestUserProfileUri());
        takeScreenshot(browser, "hds-add-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }
	
	@Test(dependsOnMethods = { "verifyStorage" })
	public void verifyStorageUsersAddFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + getStorageUsersUrl() + "/");
		waitForElementVisible(storageUsersForm.getRoot());
	}
	
	@Test(dependsOnMethods = { "addUserToStorage" })
	public void verifyStorageUsersUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getAddedUserUrlWithHost() + "/");
		waitForElementVisible(storageUsersForm.getRoot());
	}

    @Test(dependsOnMethods = "addUserToStorage")
    public void verifyAddedUser() throws JSONException {
        verifyUser(NEW_USER_ROLE, "hds-added-user");
    }

    @Test(dependsOnMethods = "verifyAddedUser")
    public void updateUser() throws JSONException {
        browser.get(getAddedUserUrlWithHost());
        storageUsersForm.verifyValidUpdateUserForm(NEW_USER_ROLE, getTestUserProfileUri());
        storageUsersForm.fillAddUserToStorageForm(NEW_USER_UPDATED_ROLE, getTestUserProfileUri());
        takeScreenshot(browser, "hds-update-user-filled-form", this.getClass());
        assertEquals(browser.getCurrentUrl(), getAddedUserUrlWithHost());
    }
    
    @Test(dependsOnMethods = "updateUser")
    public void verifyUpdatedUser() throws JSONException {
        verifyUser(NEW_USER_UPDATED_ROLE, "hds-updated-user");
    }

    @Test(dependsOnMethods = {"verifyUpdatedUser"})
    public void removeUserFromStorage() throws Exception {
        browser.get(getAddedUserUrlWithHost());
        final StorageUsersFragment deleteFragment =
                createPageFragment(StorageUsersFragment.class, browser.findElement(BY_GP_FORM_SECOND));
        deleteFragment.verifyValidDeleteUserForm();
        deleteFragment.deleteUser();
        final JSONObject jsonObject = loadJSON();
        assertEquals(browser.getCurrentUrl(), getBasicRootUrl() + storageUrl + "/users");
        assertEquals(1, jsonObject.getJSONObject("users").getJSONArray("items").length());
    }
    
    /** ===================== Section with invalid storage users cases ============ */
    
    @Test(dependsOnMethods = { "verifyStorage" })
	public void addUserWithEmptyProfile() throws JSONException {
    	invalidUserAssignment(null, NEW_USER_ROLE, "URI can not be empty");
	}
    
    @Test(dependsOnMethods = { "verifyStorage" })
	public void addNonExistingUser() throws JSONException {
    	invalidUserAssignment("/gdc/account/profile/nonexisting", NEW_USER_ROLE, "User 'nonexisting' has not been found");
	}
    
    @Test(dependsOnMethods = { "verifyStorage" })
	public void addUserWithInvalidURI() throws JSONException {
    	invalidUserAssignment("/invalid/uri", NEW_USER_ROLE, "Validation failed");
	}
    
    @Test(dependsOnMethods = { "verifyStorage" })
	public void addUserWithInvalidURI2() throws JSONException {
    	invalidUserAssignment("/gdc/account/profile", NEW_USER_ROLE, "Validation failed");
	}
    
    @Test(dependsOnMethods = { "verifyStorage" })
	public void addUserWithInvalidURI3() throws JSONException {
    	invalidUserAssignment("/gdc/account/profile/", NEW_USER_ROLE, "Validation failed");
	}
	
    @Test(dependsOnMethods = { "verifyStorage" })
	public void addExistingUserToStorage() throws JSONException {
    	invalidUserAssignment(userCreatedByUrl, NEW_USER_ROLE, "User '" + userCreatedByUrl + "' already exists in storage '");
	}
    
    // TODO add next invalid cases when permissions are implemented

    /** ===================== HELP methods ================= */
	
	private void openStorageUrl() {
		browser.get(getBasicRootUrl() + storageUrl);
		waitForElementVisible(storageForm.getRoot());
		waitForElementPresent(BY_GP_PRE_JSON);
	}

    private void openStorageUsersUrl() {
        browser.get(getBasicRootUrl() + getStorageUsersUrl());
        waitForElementVisible(storageUsersForm.getRoot());
        waitForElementPresent(BY_GP_PRE_JSON);
    }
    
    private String getStorageUsersUrl() {
    	return storageUrl + "/users";
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

    private void createInvalidStorage(String title, String description, String authorizationToken, String copyOf, String expectedErrorMessage) throws JSONException {
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidCreateStorageForm(), "Create form is invalid");
		storageForm.fillCreateStorageForm(title, description, authorizationToken, copyOf);
		verifyErrorMessage(expectedErrorMessage, PAGE_GDC_STORAGES);
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
    
    private void invalidUserAssignment(String userProfile, String role, String expectedErrorMessage) throws JSONException {
    	openStorageUsersUrl();
    	storageUsersForm.verifyValidAddUserForm();
		storageUsersForm.fillAddUserToStorageForm(role, userProfile);
		verifyErrorMessage(expectedErrorMessage, storageUrl + "/users");
	}
    
    private void invalidUpdateOfStorage(String title, String description, String expectedErrorMessage) throws JSONException {
		openStorageUrl();
		waitForElementVisible(storageForm.getRoot());
		assertTrue(storageForm.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storageForm.updateStorage(title, description);
		verifyErrorMessage(expectedErrorMessage, storageUrl);
	}
	
	private void verifyErrorMessage(String messageSubstring, String expectedPage) throws JSONException {
		assertTrue(browser.getCurrentUrl().endsWith(expectedPage), "Browser was redirected at another page");
		JSONObject json = loadJSON();
		String errorMessage = json.getJSONObject("error").getString("message");
		assertTrue(errorMessage.contains(messageSubstring), "Another error message present: " + errorMessage + ", expected message substring: " + messageSubstring);
	}
	
	private void verifyStoragesResourceJSON() throws JSONException {
		JSONObject json = loadJSON();
		assertTrue(json.getJSONObject("storages").has("items"), "storages with items array is not available");
		JSONArray storagesItems = json.getJSONObject("storages").getJSONArray("items");
		if (storagesItems.length() > 0) {
			JSONObject firstStorage = storagesItems.getJSONObject(0).getJSONObject("storage");
			assertTrue(firstStorage.has("title"), "Storage title isn't present");
			assertTrue(firstStorage.has("description"), "Storage description isn't present");
			assertTrue(firstStorage.has("authorizationToken"), "Storage authorizationToken isn't present");
			assertTrue(firstStorage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
			assertTrue(firstStorage.getJSONObject("links").has("self"), "Storage self link isn't present");
			assertTrue(firstStorage.getJSONObject("links").has("users"), "Storage users link isn't present");
			assertTrue(firstStorage.has("status"), "Storage status isn't present");
		}
		assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("parent").endsWith("gdc"), "Parent link doesn't match");
		assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("self").substring(1).equals(PAGE_GDC_STORAGES), "Storages self link doesn't match");
	}
}
