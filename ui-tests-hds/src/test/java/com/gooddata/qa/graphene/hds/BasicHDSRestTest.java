package com.gooddata.qa.graphene.hds;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.fragments.greypages.hds.ColumnFragment;
import com.gooddata.qa.graphene.fragments.greypages.hds.StorageFragment;
import com.gooddata.qa.graphene.fragments.greypages.hds.TableFragment;
import com.gooddata.qa.graphene.fragments.greypages.hds.ColumnFragment.Types;
import com.gooddata.qa.utils.graphene.Screenshots;

@Test(groups = { "hds" }, description = "Basic verification of hds restapi in GD platform")
public class BasicHDSRestTest extends AbstractHDSTest {
	
	private String storageUrl;
	private String tableUrl;
	private String columnUrl;
	
	private static final String STORAGE_TITLE = "HDS storage";
	private static final String STORAGE_DESCRIPTION = "HDS description";
	private static final String STORAGE_AUTH_TOKEN = "abcd";
	private static final String STORAGE_COPY_OF = "/gdc/storages/${storageId}";
	
	private static final String TABLE_NAME = "My table";
	
	private static final String COLUMN_NAME = "My column";
	
	private static final By BY_LINK_TABLES = By.partialLinkText("tables");
	private static final By BY_LINK_COLUMNS = By.partialLinkText("columns");
	
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
		Assert.assertTrue(storage.getString("description").equals(STORAGE_DESCRIPTION), "Storage description doesn't match");
		Assert.assertTrue(storage.getString("authorizationToken").equals(STORAGE_AUTH_TOKEN), "Storage authorizationToken doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("parent").substring(1).equals(PAGE_GDC_STORAGES), "Storage parent link doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("self").equals(storageUrl), "Storage self link doesn't match");
		Assert.assertTrue(storage.getJSONObject("links").getString("tables").equals(storageUrl + "/tables"), "Storage tables link doesn't match");
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
		Assert.assertTrue(storage.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storage.updateStorage(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated");
		storage.verifyValidEditStorageForm(STORAGE_TITLE + " updated", STORAGE_DESCRIPTION + " updated");
		Screenshots.takeScreenshot(browser, "hds-updated-storage", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateStorage" }, dependsOnGroups = { "tables-tests", "columns-tests" }, alwaysRun = true)
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
	
	/** ===================== Section with table cases ================= */
	
	@Test(dependsOnMethods = {"updateStorage"}, groups = { "tables-tests" })
	public void verifyTablesResource() throws JSONException {
		verifyTablesResourceJSON();
	}
	
	@Test(dependsOnMethods = { "updateStorage" }, groups = { "tables-tests" })
	public void verifyTableCreateFormPresentWithTrailingSlash() throws JSONException {
		openTablesUrl();
		browser.get(browser.getCurrentUrl() + "/");
		waitForElementVisible(BY_GP_FORM);
	}
	
	@Test(dependsOnMethods = { "verifyTablesResource" }, groups = { "tables-tests" })
	public void createTable() throws JSONException {
		openTablesUrl();
		
		TableFragment table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(table.verifyValidCreateTableForm(), "Create form is invalid");
		tableUrl = table.createTable(TABLE_NAME);
	}
	
	@Test(dependsOnMethods = {"createTable"}, groups = { "tables-tests" })
	public void verifyTablesResourceAfterTableCreated() throws JSONException {
		verifyTablesResourceJSON();
	}
	
	@Test(dependsOnMethods = { "createTable" }, groups = { "tables-tests" })
	public void verifyTable() throws JSONException {
		openTableUrl();
		Screenshots.takeScreenshot(browser, "hds-simple-table", this.getClass());
		JSONObject json = loadJSON();
		Assert.assertTrue(json.has("table"), "Table element isn't present");
		JSONObject table = json.getJSONObject("table");
		Assert.assertTrue(table.getString("name").equals(TABLE_NAME), "Table title doesn't match");
		Assert.assertTrue(table.getJSONObject("links").getString("parent").endsWith("tables"), "Table parent link doesn't match");
		Assert.assertTrue(table.getJSONObject("links").getString("self").equals(tableUrl), "Table self link doesn't match");
		Assert.assertTrue(table.getJSONObject("links").getString("columns").equals(tableUrl + "/columns"), "Table columns link doesn't match");
		String createdByUrl = table.getString("createdBy");
		String updatedByUrl = table.getString("updatedBy");
		Assert.assertEquals(updatedByUrl, createdByUrl, "Table createdBy and updatedBy attributes do not match");
		String createdDate = table.getString("created");
		String updatedDate = table.getString("updated");
		Assert.assertEquals(updatedDate, createdDate, "Table created and updated dates do not match");
	}
	
	@Test(dependsOnMethods = { "createTable" }, groups = { "tables-tests" })
	public void verifyTableUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + tableUrl + "/");
		waitForElementVisible(BY_GP_FORM);
	}
	
	@Test(dependsOnMethods = { "verifyTable" }, groups = { "tables-tests" })
	public void updateTable() throws JSONException {
		openTableUrl();
		waitForElementVisible(BY_GP_FORM);
		TableFragment table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(table.verifyValidEditTableForm(TABLE_NAME), "Edit form doesn't contain current value");
		table.updateTable(TABLE_NAME + " updated");
		table.verifyValidEditTableForm(TABLE_NAME + " updated");
		Screenshots.takeScreenshot(browser, "hds-updated-table", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateTable" }, groups = { "tables-tests" }, dependsOnGroups = { "columns-tests" }, alwaysRun = true)
	public void deleteTable() throws JSONException {
		openTableUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		TableFragment table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(table.verifyValidDeleteTableForm(), "Delete form is invalid");
		table.deleteTable();
	}
	
	/** ===================== Section with invalid table cases ================= */
	
	@Test(dependsOnMethods = { "verifyTablesResource" }, groups = { "tables-tests" })
	public void createTableWithoutName() throws JSONException {
		createInvalidTable(null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "createTable" }, groups = { "tables-tests" })
	public void createTableWithExistingName() throws JSONException {
		createInvalidTable(TABLE_NAME, "Table \"" + TABLE_NAME + "\" already exists");
	}
	
	@Test(dependsOnMethods = { "createTable" }, groups = { "tables-tests" })
	public void updateTableWithEmptyTitle() throws JSONException {
		invalidUpdateOfTable(null, "Validation failed");
	}
	
	/** ===================== Section with valid column cases ================= */
	
	@Test(dependsOnMethods = {"verifyTable"}, groups = { "columns-tests" })
	public void verifyColumnsResource() throws JSONException {
		verifyColumnsResourceJSON();
	}
	
	@Test(dependsOnMethods = { "verifyTable" }, groups = { "columns-tests" })
	public void verifyColumnCreateFormPresentWithTrailingSlash() throws JSONException {
		openColumnsUrl();
		browser.get(browser.getCurrentUrl() + "/");
		waitForElementVisible(BY_GP_FORM);
	}
	
	@Test(dependsOnMethods = { "verifyColumnsResource" }, groups = { "columns-tests" })
	public void createColumn() throws JSONException {
		openColumnsUrl();
		
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(column.verifyValidCreateColumnForm(), "Create form is invalid");
		columnUrl = column.createColumn(COLUMN_NAME, ColumnFragment.Types.TEXT, true, null, null);
	}
	
	@Test(dependsOnMethods = {"createColumn"}, groups = { "columns-tests" })
	public void verifyColumnsResourceAfterColumnCreated() throws JSONException {
		verifyColumnsResourceJSON();
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void verifyColumn() throws JSONException {
		openColumnUrl();
		Screenshots.takeScreenshot(browser, "hds-simple-column", this.getClass());
		JSONObject json = loadJSON();
		Assert.assertTrue(json.has("column"), "Column element isn't present");
		JSONObject column = json.getJSONObject("column");
		Assert.assertTrue(column.getString("name").equals(COLUMN_NAME), "Column title doesn't match");
		Assert.assertTrue(column.getString("type").equals(ColumnFragment.Types.TEXT.getText()), "Column type doesn't match");
		Assert.assertTrue(column.getBoolean("primary") == Boolean.TRUE, "Column primary doesn't match");
		Assert.assertTrue(column.getJSONObject("links").getString("parent").endsWith("columns"), "Column parent link doesn't match");
		Assert.assertTrue(column.getJSONObject("links").getString("self").equals(columnUrl), "Column self link doesn't match");
		String createdByUrl = column.getString("createdBy");
		String updatedByUrl = column.getString("updatedBy");
		Assert.assertEquals(updatedByUrl, createdByUrl, "Column createdBy and updatedBy attributes do not match");
		String createdDate = column.getString("created");
		String updatedDate = column.getString("updated");
		Assert.assertEquals(updatedDate, createdDate, "Column created and updated dates do not match");
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void createColumnWithForeignKeyInSameTable() throws JSONException {
		openColumnsUrl();
		
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(column.verifyValidCreateColumnForm(), "Create form is invalid");
		String secondColumnUrl = column.createColumn(COLUMN_NAME + "-second", ColumnFragment.Types.TEXT, false, TABLE_NAME + " updated", COLUMN_NAME);
		
		browser.get(getBasicRootUrl() + secondColumnUrl);
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
		Screenshots.takeScreenshot(browser, "hds-simple-column-foreign-key-same-table", this.getClass());
		JSONObject json = loadJSON();
		Assert.assertTrue(json.has("column"), "Column element isn't present");
		Assert.assertTrue(json.getJSONObject("column").getString("name").equals(COLUMN_NAME + "-second"), "Column title doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getString("type").equals(ColumnFragment.Types.TEXT.getText()), "Column type doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getBoolean("primary") == Boolean.FALSE, "Column primary doesn't match");
		Assert.assertTrue(json.getJSONObject("column").has("foreignKey"), "Column fk element isn't present");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("foreignKey").getString("table").equals(TABLE_NAME + " updated"), "Column fk table doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("foreignKey").getString("column").equals(COLUMN_NAME), "Column fk column doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("links").getString("parent").endsWith("columns"), "Column parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("links").getString("self").equals(secondColumnUrl), "Column self link doesn't match");
	
		column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(column.verifyValidDeleteColumnForm(), "Delete form is invalid");
		column.deleteColumn();
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void createColumnWithForeignKeyInAnotherTable() throws JSONException {
		openTablesUrl();
		
		TableFragment table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(table.verifyValidCreateTableForm(), "Create form is invalid");
		String secondTableUrl = table.createTable(TABLE_NAME + "-second");
		
		browser.get(getBasicRootUrl() + secondTableUrl);
		waitForElementPresent(BY_LINK_COLUMNS);
		browser.findElement(BY_LINK_COLUMNS).click();
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
		
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(column.verifyValidCreateColumnForm(), "Create form is invalid");
		String secondColumnUrl = column.createColumn(COLUMN_NAME + "-second", ColumnFragment.Types.TEXT, false, TABLE_NAME + " updated", COLUMN_NAME);
		
		browser.get(getBasicRootUrl() + secondColumnUrl);
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
		Screenshots.takeScreenshot(browser, "hds-simple-column-foreign-key-another-table", this.getClass());
		JSONObject json = loadJSON();
		Assert.assertTrue(json.has("column"), "Column element isn't present");
		Assert.assertTrue(json.getJSONObject("column").getString("name").equals(COLUMN_NAME + "-second"), "Column title doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getString("type").equals(ColumnFragment.Types.TEXT.getText()), "Column type doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getBoolean("primary") == Boolean.FALSE, "Column primary doesn't match");
		Assert.assertTrue(json.getJSONObject("column").has("foreignKey"), "Column fk element isn't present");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("foreignKey").getString("table").equals(TABLE_NAME + " updated"), "Column fk table doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("foreignKey").getString("column").equals(COLUMN_NAME), "Column fk column doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("links").getString("parent").endsWith("columns"), "Column parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("column").getJSONObject("links").getString("self").equals(secondColumnUrl), "Column self link doesn't match");
		
		column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(column.verifyValidDeleteColumnForm(), "Delete form is invalid");
		column.deleteColumn();
		
		browser.get(getBasicRootUrl() + secondTableUrl);
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
		
		table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(table.verifyValidDeleteTableForm(), "Delete form is invalid");
		table.deleteTable();
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void verifyColumnUpdateFormPresentWithTrailingSlash() throws JSONException {
		browser.get(getBasicRootUrl() + columnUrl + "/");
		waitForElementVisible(BY_GP_FORM);
	}
	
	@Test(dependsOnMethods = { "verifyColumn" }, groups = { "columns-tests" })
	public void updateColumn() throws JSONException {
		openColumnUrl();
		waitForElementVisible(BY_GP_FORM);
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(column.verifyValidUpdateColumnForm(COLUMN_NAME, ColumnFragment.Types.TEXT, true, null, null), "Edit form doesn't contain current value");
		column.updateColumn(COLUMN_NAME + " updated", ColumnFragment.Types.INT, false, null, null);
		column.verifyValidUpdateColumnForm(COLUMN_NAME + " updated", ColumnFragment.Types.INT, false, null, null);
		Screenshots.takeScreenshot(browser, "hds-updated-column", this.getClass());
	}
	
	@Test(dependsOnMethods = { "updateColumn" }, groups = { "columns-tests" })
	public void deleteColumn() throws JSONException {
		openColumnUrl();
		waitForElementVisible(BY_GP_FORM_SECOND);
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM_SECOND));
		Assert.assertTrue(column.verifyValidDeleteColumnForm(), "Delete form is invalid");
		column.deleteColumn();
	}
	
	/** ===================== Section with invalid column cases ================= */
	
	@Test(dependsOnMethods = { "verifyColumnsResource" }, groups = { "columns-tests" })
	public void createColumnWithoutName() throws JSONException {
		createInvalidColumn(null, ColumnFragment.Types.TEXT, false, null, null, "Validation failed");
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void createColumnWithExistingName() throws JSONException {
		createInvalidColumn(COLUMN_NAME, ColumnFragment.Types.TEXT, false, null, null, "Column '" + COLUMN_NAME + "' already exists in table '" + TABLE_NAME + " updated' in storage with id");
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void createColumnWithNonExistingForeignKeyTable() throws JSONException {
		createInvalidColumn(COLUMN_NAME + "-fkt", ColumnFragment.Types.TEXT, false, "nonex", "nonex", "Table 'nonex' not found in storage with id");
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void createColumnWithNonExistingForeignKeyColumn() throws JSONException {
		createInvalidColumn(COLUMN_NAME + "-fkc", ColumnFragment.Types.TEXT, false, TABLE_NAME + " updated", "nonex", "Column 'nonex' not found in table '" + TABLE_NAME + " updated' in storage with id");
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void createSecondPrimaryColumn() throws JSONException {
		createInvalidColumn(COLUMN_NAME + "-another-primary", ColumnFragment.Types.TEXT, true, null, null, "Only one primary key per table is allowed");
	}
	
	@Test(dependsOnMethods = { "createColumn" }, groups = { "columns-tests" })
	public void updateColumnWithEmptyName() throws JSONException {
		invalidUpdateOfColumn(null, ColumnFragment.Types.TEXT, true, null, null, "Validation failed");
	}
	
	/** ===================== HELP methods ================= */
	
	private void openStorageUrl() {
		browser.get(getBasicRootUrl() + storageUrl);
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
	}
	
	private void openTablesUrl() {
		openStorageUrl();
		waitForElementPresent(BY_LINK_TABLES);
		browser.findElement(BY_LINK_TABLES).click();
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
	}
	
	private void openTableUrl() {
		browser.get(getBasicRootUrl() + tableUrl);
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
	}
	
	private void openColumnsUrl() {
		openTableUrl();
		waitForElementPresent(BY_LINK_COLUMNS);
		browser.findElement(BY_LINK_COLUMNS).click();
		waitForElementVisible(BY_GP_FORM);
		waitForElementPresent(BY_GP_PRE_JSON);
	}
	
	private void openColumnUrl() {
		browser.get(getBasicRootUrl() + columnUrl);
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
	
	private void createInvalidTable(String name, String expectedErrorMessage) throws JSONException {
		openTablesUrl();
		waitForElementVisible(BY_GP_FORM);
		TableFragment table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(table.verifyValidCreateTableForm(), "Create form is invalid");
		table.fillCreateTableForm(name);
		verifyErrorMessage(expectedErrorMessage, "/tables");
	}
	
	private void createInvalidColumn(String name, Types type, boolean primaryKey, String foreignKeyTable, String foreignKeyColumn, String expectedErrorMessage) throws JSONException {
		openColumnsUrl();
		waitForElementVisible(BY_GP_FORM);
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(column.verifyValidCreateColumnForm(), "Create form is invalid");
		column.fillCreateColumnForm(name, type, primaryKey, foreignKeyTable, foreignKeyColumn);
		verifyErrorMessage(expectedErrorMessage, "/columns");
	}
	
	private void invalidUpdateOfStorage(String title, String description, String expectedErrorMessage) throws JSONException {
		openStorageUrl();
		waitForElementVisible(BY_GP_FORM);
		StorageFragment storage = Graphene.createPageFragment(StorageFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(storage.verifyValidEditStorageForm(STORAGE_TITLE, STORAGE_DESCRIPTION), "Edit form doesn't contain current values");
		storage.updateStorage(title, description);
		verifyErrorMessage(expectedErrorMessage, storageUrl);
	}
	
	private void invalidUpdateOfTable(String name, String expectedErrorMessage) throws JSONException {
		openTableUrl();
		waitForElementVisible(BY_GP_FORM);
		TableFragment table = Graphene.createPageFragment(TableFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(table.verifyValidEditTableForm(TABLE_NAME), "Edit form doesn't contain current value");
		table.updateTable(name);
		verifyErrorMessage(expectedErrorMessage, tableUrl);
	}
	
	private void invalidUpdateOfColumn(String name, Types type, boolean primaryKey, String foreignKeyTable, String foreignKeyColumn, String expectedErrorMessage) throws JSONException {
		openColumnUrl();
		waitForElementVisible(BY_GP_FORM);
		ColumnFragment column = Graphene.createPageFragment(ColumnFragment.class, browser.findElement(BY_GP_FORM));
		Assert.assertTrue(column.verifyValidUpdateColumnForm(COLUMN_NAME, ColumnFragment.Types.TEXT, true, null, null), "Edit form doesn't contain current value");
		column.updateColumn(name, type, primaryKey, foreignKeyTable, foreignKeyColumn);
		verifyErrorMessage(expectedErrorMessage, columnUrl);
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
			Assert.assertTrue(firstStorage.getJSONObject("links").has("tables"), "Storage tables link isn't present");
			Assert.assertTrue(firstStorage.getJSONObject("links").has("users"), "Storage users link isn't present");
			Assert.assertTrue(firstStorage.has("status"), "Storage status isn't present");
		}
		Assert.assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("parent").endsWith("gdc"), "Parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("storages").getJSONObject("links").getString("self").substring(1).equals(PAGE_GDC_STORAGES), "Storages self link doesn't match");
	}
	
	private void verifyTablesResourceJSON() throws JSONException {
		openTablesUrl();
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("tables").has("items"), "tables with items array is not available");
		JSONArray tablesItems = json.getJSONObject("tables").getJSONArray("items");
		if (tablesItems.length() > 0) {
			JSONObject firstTable = tablesItems.getJSONObject(0).getJSONObject("table");
			Assert.assertTrue(firstTable.has("name"), "Table name isn't present");
			Assert.assertTrue(firstTable.getJSONObject("links").getString("parent").endsWith("tables"), "Table parent link doesn't match");
			Assert.assertTrue(firstTable.getJSONObject("links").has("self"), "Table self link isn't present");
			Assert.assertTrue(firstTable.getJSONObject("links").has("columns"), "Table columns link isn't present");
		}
		Assert.assertTrue(json.getJSONObject("tables").getJSONObject("links").getString("parent").endsWith(storageUrl), "Parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("tables").getJSONObject("links").getString("self").endsWith("tables"), "Tables self link doesn't match");
	}
	
	private void verifyColumnsResourceJSON() throws JSONException {
		openColumnsUrl();
		JSONObject json = loadJSON();
		Assert.assertTrue(json.getJSONObject("columns").has("items"), "columns with items array is not available");
		JSONArray columnsItems = json.getJSONObject("columns").getJSONArray("items");
		if (columnsItems.length() > 0) {
			JSONObject firstColumn = columnsItems.getJSONObject(0).getJSONObject("column");
			Assert.assertTrue(firstColumn.has("name"), "Column name isn't present");
			Assert.assertTrue(firstColumn.has("type"), "Column type isn't present");
			Assert.assertTrue(firstColumn.has("primary"), "Column primary isn't present");
			Assert.assertTrue(firstColumn.getJSONObject("links").getString("parent").endsWith("columns"), "Column parent link doesn't match");
			Assert.assertTrue(firstColumn.getJSONObject("links").has("self"), "Column self link isn't present");
		}
		Assert.assertTrue(json.getJSONObject("columns").getJSONObject("links").getString("parent").endsWith(tableUrl), "Parent link doesn't match");
		Assert.assertTrue(json.getJSONObject("columns").getJSONObject("links").getString("self").endsWith("columns"), "Columns self link doesn't match");
	}
}
