package com.gooddata.qa.graphene.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.greypages.hds.StorageFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "hds" }, description = "Basic verification of hds restapi in GD platform")
public class BasicHDSRestTest extends AbstractHDSTest {
	
	private String storageUrl;
	
	private static final String STORAGE_TITLE = "HDS storage";
	private static final String STORAGE_DESCRIPTION = "HDS description";
	private static final String STORAGE_AUTH_TOKEN = "pgroup1";
	private static final String STORAGE_COPY_OF = "/gdc/storages/${storageId}";
	
	@BeforeClass
	public void initStartPage() {
		startPage = PAGE_GDC_STORAGES;
	}
	
	@Test(groups = {"hdsInit"})
	public void resourceStoragesNotAvailableForAnonymous() throws JSONException {
		waitForElementPresent(BY_GP_PRE_JSON);
		Assert.assertTrue(browser.getCurrentUrl().contains("gdc/account/token"), "Redirect to /gdc/account/token wasn't done for anonymous user");
	}

	@Test(groups = {"hdsInit"}, dependsOnMethods = { "resourceStoragesNotAvailableForAnonymous" })
	public void resourceStoragesAvailable() throws JSONException {
		validSignInWithDemoUser(true);
		
		loadPlatformPageBeforeTestMethod();
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("storages").has("items"), "storages with items array is not available");
		Screenshots.takeScreenshot(browser, "hds-base-resource", this.getClass());
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void gpFormsAvailable() {
		waitForElementPresent(BY_GP_FORM);
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void hdsResourceLinkNotAvailableAtBasicResource() {
		browser.get(getRootUrl() + PAGE_GDC);
		Assert.assertEquals(browser.getTitle(), "GoodData API root");
		Assert.assertTrue(browser.findElements(By.partialLinkText("storages")).size() == 0, "Storages link is present at basic /gdc resource");
	}
	
	@Test(dependsOnGroups = {"hdsInit"})
	public void verifyDefaultResource() throws JSONException {
		verifyStoragesResourceJSON();
	}
	
	/** ===================== Section with valid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void verifyStorageCreateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getRootUrl() + PAGE_GDC_STORAGES + "/");
		waitForElementVisible(BY_GP_FORM);
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorage() throws JSONException {
		waitForElementVisible(BY_GP_FORM);
		StorageFragment storage = Graphene.createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(storage.verifyValidCreateStorageForm(), "Create form is invalid");
		storageUrl = storage.createStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, STORAGE_AUTH_TOKEN, null);
	}
	
	@Test(dependsOnMethods = {"createStorage"})
	public void verifyDefaultResourceAfterStorageCreated() throws JSONException {
		verifyStoragesResourceJSON();
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void verifyStorageUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + storageUrl + "/");
		waitForElementVisible(BY_GP_FORM);
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void verifyStorage() throws JSONException {
		openStorageUrl();
		Screenshots.takeScreenshot(browser, "hds-simple-storage", this.getClass());
		JSONObject json = loadJSON();
		Assert.assertTrue(json.has("storage"), "Storage element isn't present");
		JSONObject storage = json.getJSONObject("storage");
		Assert.assertTrue(storage.getString("title").equals(STORAGE_TITLE), "Storage title doesn't match");
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storage.getString("description").equals("Some description."), "Storage description doesn't match");
		Assert.assertTrue(storage.getString("authorizationToken").equals(STORAGE_AUTH_TOKEN), "Storage authorizationToken doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("self").equals(storageUrl), "Storage self link doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("users").equals(storageUrl + "/users"), "Storage users link doesn't match");
		Assert.assertTrue(storage.getString("status").equals("ENABLED"), "Storage isn't enabled");
		String createdByUrl = storage.getString("createdBy");
		String updatedByUrl = storage.getString("updatedBy");
		Assert.assertEquals(updatedByUrl, createdByUrl, "Storage createdBy and updatedBy attributes do not match");
		String createdDate = storage.getString("created");
		String updatedDate = storage.getString("updated");
		Assert.assertEquals(updatedDate, createdDate, "Storage created and updated dates do not match");
		browser.get(getBasicRootUrl() + storageUrl + "/users");
		JSONObject jsonUsers = loadJSON();
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("parent").equals(storageUrl), "Storage users parent link doesn't match");
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONObject("links").getString("self").equals(storageUrl + "/users"), "Storage users self link doesn't match");
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").length() == 1, "Number of users doesn't match");
		Assert.assertTrue(jsonUsers.getJSONObject("users").getJSONArray("items").getJSONObject(0).getJSONObject("user").getString("profile").equals(createdByUrl), "Creator in users doesn't match with creator in storage");
		browser.get(getBasicRootUrl() + createdByUrl);
		JSONObject jsonUser = loadJSON();
		Assert.assertTrue(jsonUser.getJSONObject("accountSetting").getString("login").equals(user), "Login of user in profile doesn't match");
	}
	
	@Test(dependsOnMethods = { "verifyStorage" })
	public void updateStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM);
		StorageFragment storage = Graphene.createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM));
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storage.verifyValidEditStorageForm(STORAGE_TITLE, "Some description."), "Edit form doesn't contain current values");
		storage.updateStorage(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated");
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storage.verifyValidEditStorageForm(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated"), "Edit form doesn't contain expected values");
		Screenshots.takeScreenshot(browser, "hds-updated-storage", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateStorage" }, alwaysRun = true)
	public void deleteStorage() throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		StorageFragment storage = Graphene.createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(storage.verifyValidDeleteStorageForm(), "Delete form is invalid");
		storage.deleteStorage();
	}
	
	/** ===================== Section with invalid storage cases ================= */
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutTitle() throws JSONException {
		createInvalidStorage(null, STORAGE_DESCRIPTION, STORAGE_AUTH_TOKEN, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutDescription() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, null, STORAGE_AUTH_TOKEN, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithoutAuthToken() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, null, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "gpFormsAvailable" })
	public void createStorageWithInvalidCopyOfURI() throws JSONException {
		createInvalidStorage(STORAGE_TITLE, STORAGE_DESCRIPTION, STORAGE_AUTH_TOKEN, STORAGE_COPY_OF, "Malformed request");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyTitle() throws JSONException {
		invalidUpdateOfStorage(null, STORAGE_DESCRIPTION, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "createStorage" })
	public void updateStorageWithEmptyDescription() throws JSONException {
		invalidUpdateOfStorage(STORAGE_TITLE, null, "Validation failed");
	}
	
	/** ===================== HELP methods ================= */
	
	private void openStorageUrl() {
		browser.get(getBasicRootUrl() + storageUrl);
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
	}
	
	private void createInvalidStorage(String title, String description, String authorizationToken, String copyOf, String expectedErrorMessage) throws JSONException {
		waitForElementVisible(BY_GP_FORM);
		StorageFragment storage = Graphene.createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(storage.verifyValidCreateStorageForm(), "Create form is invalid");
		storage.fillCreateStorageForm(title, description, authorizationToken, copyOf);
		verifyErrorMessage(expectedErrorMessage, PAGE_GDC_STORAGES);
	}
	
	private void invalidUpdateOfStorage(String title, String description, String expectedErrorMessage) throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM);
		StorageFragment storage = Graphene.createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM));
		//TODO - wait for HDS C3 milestone 2 for fix (change description)
		//Assert.assertTrue(storage.verifyValidEditStorageForm(STORAGE_TITLE, "Some description."), "Edit form doesn't contain current values");
		storage.updateStorage(title, description);
		verifyErrorMessage(expectedErrorMessage, storageUrl);
	}
	
	private void verifyErrorMessage(String messageSubstring, String expectedPage) throws JSONException {
		Assert.assertTrue(browser.getCurrentUrl().endsWith(expectedPage), "Browser was redirected at another page");
		JSONObject json = loadJSON();
		String errorMessage = json.getJSONObject("error").getString("message");
		Assert.assertTrue(errorMessage.contains(messageSubstring), "Another error message present: " + errorMessage + ", expected message substring: " + messageSubstring);
	}
	
	private void verifyStoragesResourceJSON() throws JSONException {
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("storages").has("items"), "storages with items array is not available");
		JSONArray storagesItems = json.getJSONObject("storages").getJSONArray("items");
		if (storagesItems.length() > 0) {
			JSONObject firstStorage = storagesItems.getJSONObject(0).getJSONObject("storage");
			Assert.assertTrue(firstStorage.has("title"), "Storage title isn't present");
			Assert.assertTrue(firstStorage.has("description"), "Storage description isn't present");
			Assert.assertTrue(firstStorage.has("authorizationToken"), "Storage authorizationToken isn't present");
			Assert.assertTrue(firstStorage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
			Assert.assertTrue(firstStorage.getJSONObject("links").has("self"), "Storage self link isn't present");
			Assert.assertTrue(firstStorage.getJSONObject("links").has("users"), "Storage users link isn't present");
			Assert.assertTrue(firstStorage.has("status"), "Storage status isn't present");
		}
		Assert.assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("parent").endsWith("gdc"), "Parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("self").substring(1).equals(PAGE_GDC_STORAGES), "Storages self link doesn't match");
	}
}
