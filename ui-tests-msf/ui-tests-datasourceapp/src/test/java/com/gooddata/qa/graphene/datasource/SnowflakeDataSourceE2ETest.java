package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage;
import org.openqa.selenium.By;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.*;

public class SnowflakeDataSourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private final String INITIAL_TEXT = "Create your first data source\n" +
            "Data source stores information about connection into a data warehouse";
    private final String SNOWFLAKE = "Snowflake";
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_INVALID = "Auto_datasource_invalid" + generateHashString();
    private final String DATASOURCE_WAREHOUSE = "ATT_WAREHOUSE";
    private final String DATASOURCE_DATABASE = "ATT_DATASOURCE_TEST";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "PUBLIC";
    private final String INVALID_VALUE = "invalid value" + generateHashString();

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        DATASOURCE_URL = testParams.getSnowflakeJdbcUrl();
        DATASOURCE_USERNAME = testParams.getSnowflakeUserName();
        DATASOURCE_PASSWORD = testParams.getSnowflakePassword();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();

    }

    //In the first time , domain doesn’t have any Datasources, initial screen is showed
    //Customer want to create Datasource , he can do that on 2 ways:
    // +  clicks on button <Create data source> at the bottom in the left sidebar
    // +  clicks on button <Data-source-type>
    @Test(dependsOnGroups = {"createProject"})
    public void initialStageTest() {
        if(testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment !!");
        }
    }

    //In case, has ADS Datasource on Domain , verify display list datasource and show Detail of first datasource
    @Test(dependsOnMethods = {"initialStageTest"})
    public void verifyFirstUIDatasourceTest() {
        if (dsMenu.isListDatasourceEmpty()) {
            OverviewPage overviewPage = initDiscOverviewPage();
            dataSourceManagementPage = overviewPage.openDatasourcePage();
            assertTrue(browser.getCurrentUrl().contains("?navigation=disc"));
            InitialContent initialContent = contentWrapper.getInitialContent();
            assertThat(initialContent.getInitialContentText(), containsString(INITIAL_TEXT));
            assertEquals(initialContent.getNumberOfCloudResourceButton(), 4);
            assertEquals(initialContent.getTextOnCloudResourceButton(0), SNOWFLAKE);
            initialContent.openSnowflakeEdit();
            DataSourceMenu dsMenu = dataSourceManagementPage.getMenuBar();
            dsMenu.selectSnowflakeResource();
        } else {
            OverviewPage overviewPage = initDiscOverviewPage();
            dataSourceManagementPage = overviewPage.openDatasourcePage();
            assertTrue(browser.getCurrentUrl().contains("?navigation=disc"));
            String firstDSText = dsMenu.getListDataSources().get(0);
            ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
            DatasourceHeading heading = container.getDatasourceHeading();
            assertEquals(firstDSText, heading.getName());
        }
    }

    //Customer can validate before create the data source, input the information of the data source then clicks on button <Test connection>
    @Test(dependsOnMethods = "initialStageTest")
    public void checkRequiredDataSourceInformation() {
        initDatasourceManagementPage();
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.clickSavebutton();
        // check vailidate required field
        assertEquals(configuration.getNumberOfRequiredMessage(), 6);
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{DATASOURCE_NAME, INVALID_VALUE, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Cannot reach the url"},
                {DATASOURCE_NAME, DATASOURCE_URL, INVALID_VALUE, DATASOURCE_USERNAME, DATASOURCE_DATABASE,
                        DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Warehouse not found"},
                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_WAREHOUSE, INVALID_VALUE, DATASOURCE_DATABASE,
                        DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Incorrect credentials"},
                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME,
                        INVALID_VALUE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Database not found"},
                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME,
                        DATASOURCE_DATABASE, DATASOURCE_PREFIX, INVALID_VALUE, "Connection failed! Schema not found"},
                {DATASOURCE_NAME, DATASOURCE_URL, INVALID_VALUE, DATASOURCE_USERNAME,
                        DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Warehouse not found"}};
    }

    @Test(dependsOnMethods = "checkRequiredDataSourceInformation")
    public void checkInvalidPassword() {
        initDatasourceManagementPage();
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection failed! Incorrect credentials");
    }

    @Test(dependsOnMethods = "checkInvalidPassword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String name, String url, String warehouse, String username
            , String database, String prefix, String schema, String validateMessage) {
        initDatasourceManagementPage();
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(name);
        configuration.addSnowflakeInfo(url, warehouse, username, DATASOURCE_PASSWORD, database, prefix, schema);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), validateMessage);
    }

    //Customer input correct data and click Save button  for saving datasource
    @Test(dependsOnMethods = "checkInvalidDataSourceInformation")
    public void checkCreateNewDatasource() {
        initDatasourceManagementPage();
        dsMenu.selectSnowflakeResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail snowflakeDetail = container.getConnectionDetail();
        checkSnowflakeDetail(container.getDatasourceHeading().getName(), snowflakeDetail.getTextUrl(), snowflakeDetail.getTextUsername(),
                snowflakeDetail.getTextDatabase(), snowflakeDetail.getTextWarehouse(), snowflakeDetail.getTextPrefix(),
                snowflakeDetail.getTextSchema());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkCreateNewDatasource")
    public void checkEditDatasource() {
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        ConnectionDetail snowflakeDetail = container.getConnectionDetail();
        checkSnowflakeDetail(container.getDatasourceHeading().getName(), snowflakeDetail.getTextUrl(), snowflakeDetail.getTextUsername(),
                snowflakeDetail.getTextDatabase(), snowflakeDetail.getTextWarehouse(), snowflakeDetail.getTextPrefix(),
                snowflakeDetail.getTextSchema());
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(DATASOURCE_NAME_CHANGED);
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        configuration.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        checkSnowflakeDetailUpdate(container.getDatasourceHeading().getName(), snowflakeDetail.getTextUrl(), snowflakeDetail.getTextUsername(),
                snowflakeDetail.getTextDatabase(), snowflakeDetail.getTextWarehouse(), snowflakeDetail.getTextPrefix(),
                snowflakeDetail.getTextSchema());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME_CHANGED), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkEditDatasource")
    public void createViewTable() {
        dsMenu.selectDataSource(DATASOURCE_NAME_CHANGED);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        GenerateOutputStageDialog generateDialog = heading.getGenerateDialog();
        String sql = getResourceAsString("/sql.txt");
        assertEquals(generateDialog.getMessage(), sql);
        generateDialog.clickCopy();
        DatasourceMessageBar messageBar = DatasourceMessageBar.getInstance(browser);
        assertEquals(messageBar.waitForSuccessMessageBar().getText(), "SQL copied to clipboard");
        waitForElementNotVisible(messageBar.getRoot());
        generateDialog.clickClose();
        // check generate outputStage in case invalid datasource
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(DATASOURCE_INVALID);
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        configuration.addSnowflakeInfo(INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE,
                INVALID_VALUE, INVALID_VALUE, INVALID_VALUE);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        DatasourceMessageBar ErrormessageBar = heading.getErrorMessageDialog();
        assertEquals(ErrormessageBar.waitForErrorMessageBar().getText(), "Background task failed: Failed to obtain JDBC Connection: " +
                "Connection factory returned null from createConnection");
    }

    @Test(dependsOnMethods = "createViewTable")
    public void deleteDatasourceTest() {
        dsMenu.selectDataSource(DATASOURCE_INVALID);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDelete();
        contentWrapper.waitLoadingManagePage();
        dsMenu.waitForDatasourceNotVisible(DATASOURCE_NAME_CHANGED);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            deleteDatasource(DATASOURCE_NAME);
        }
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME_CHANGED)) {
            deleteDatasource(DATASOURCE_NAME_CHANGED);
        }
        if (dsMenu.isDataSourceExist(DATASOURCE_INVALID)) {
            deleteDatasource(DATASOURCE_INVALID);
        }
    }

    private void deleteDatasource(String datasourceName) {
        dsMenu.selectDataSource(datasourceName);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDelete();
        contentWrapper.waitLoadingManagePage();
        dsMenu.waitForDatasourceNotVisible(datasourceName);
    }

    private void checkSnowflakeDetail(String name, String url, String username,
                                      String database, String warehouse, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME));
        assertEquals(url, DATASOURCE_URL);
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(warehouse, DATASOURCE_WAREHOUSE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }

    private void checkSnowflakeDetailUpdate(String name, String url, String username,
                                            String database, String warehouse, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME_CHANGED));
        assertEquals(url, DATASOURCE_URL);
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(warehouse, DATASOURCE_WAREHOUSE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }
}
