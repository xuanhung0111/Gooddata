package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class RedshiftDataSourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private String REDSHIFT_IAM_SECRETKEY;
    private String REDSHIFT_IAM_ACCESSKEY;
    private String REDSHIFT_IAM_DBUSER;
    private String REDSHIFT_IAM_LONG_URL;
    private String REDSHIFT_IAM_SHORT_URL;
    private final String INITIAL_TEXT = "Create your first data source\n" +
            "Data source stores information about connection into a data warehouse";
    private final String AMAZON_REDSHIFT = "Amazon Redshift";
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_INVALID = "Auto_datasource_invalid" + generateHashString();
    private final String DATASOURCE_DATABASE = "dev";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "automation_daily_test";
    private final String INVALID_VALUE = "invalid value" + generateHashString();

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        DATASOURCE_URL = testParams.getRedshiftJdbcUrl();
        DATASOURCE_USERNAME = testParams.getRedshiftUserName();
        DATASOURCE_PASSWORD = testParams.getRedshiftPassword();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        REDSHIFT_IAM_DBUSER = testParams.getRedshiftIAMDbUser();
        REDSHIFT_IAM_ACCESSKEY = testParams.getRedshiftIAMAccessKey();
        REDSHIFT_IAM_SECRETKEY = testParams.getRedshiftIAMSecretKey();
        REDSHIFT_IAM_LONG_URL = testParams.getRedshiftIAMLongUrl();
        REDSHIFT_IAM_SHORT_URL = testParams.getRedshiftIAMShortUrl();
    }

    //In the first time , domain doesn’t have any Datasources, initial screen is showed
    //Customer want to create Datasource , he can do that on 2 ways:
    // +  clicks on button <Create data source> at the bottom in the left sidebar
    // +  clicks on button <Data-source-type>
    @Test(dependsOnGroups = {"createProject"})
    public void initialStageTest() {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment !!");
        }
    }

    //In case, has ADS Datasource on Domain , verify display list datasource and show Detail of first datasource
    @Test(dependsOnMethods = {"initialStageTest"})
    public void verifyFirstUIDatasourceTest() {
        if (dsMenu.isListDatasourceEmpty()) {
            initDatasourceManagementPage();
            InitialContent initialContent = contentWrapper.getInitialContent();
            assertThat(initialContent.getInitialContentText(), containsString(INITIAL_TEXT));
            assertEquals(initialContent.getNumberOfCloudResourceButton(), 4);
            assertEquals(initialContent.getTextOnCloudResourceButton(2), AMAZON_REDSHIFT);
            initialContent.openRedshiftEdit();
            dataSourceManagementPage = initDatasourceManagementPage();
            DataSourceMenu dsMenu = dataSourceManagementPage.getMenuBar();
            dsMenu.selectRedshiftResource();
        } else {
            initDatasourceManagementPage();
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
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.clickSavebutton();
        // check vailidate required field
        assertEquals(configuration.getNumberOfRequiredMessage(), 5);
    }

    @DataProvider
    public Object[][] IAMInformation() {
        return new Object[][]{{DATASOURCE_NAME, REDSHIFT_IAM_LONG_URL, REDSHIFT_IAM_DBUSER, REDSHIFT_IAM_ACCESSKEY,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection succeeded"},
                {DATASOURCE_NAME, REDSHIFT_IAM_SHORT_URL, REDSHIFT_IAM_DBUSER, REDSHIFT_IAM_ACCESSKEY,
                        DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection succeeded"}};
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{DATASOURCE_NAME, INVALID_VALUE, DATASOURCE_USERNAME, DATASOURCE_DATABASE, DATASOURCE_PREFIX,
                DATASOURCE_SCHEMA, "Connection failed! Cannot reach the url"},
                {DATASOURCE_NAME, DATASOURCE_URL, INVALID_VALUE, DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA,
                        "Connection failed! Incorrect credentials"},
                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA,
                        "Connection failed! Database not found"},
                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_DATABASE, DATASOURCE_PREFIX, INVALID_VALUE,
                        "Connection failed! Schema not found"}};
    }

    @Test(dependsOnMethods = "checkRequiredDataSourceInformation", dataProvider = "IAMInformation")
    public void checkDataSourceInformationWithIAMAccount(String name, String url, String dbuser, String accesskey, String database
            , String prefix, String schema, String validateMessage) {
        initDatasourceManagementPage();
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(name);
        configuration.addRedshiftIAMInfo(url, dbuser, accesskey, REDSHIFT_IAM_SECRETKEY, database, prefix, schema);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), validateMessage);
    }

    @Test(dependsOnMethods = "checkDataSourceInformationWithIAMAccount")
    public void checkInvalidPasword() {
        initDatasourceManagementPage();
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addRedshiftBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, INVALID_VALUE,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection failed! Incorrect credentials");
    }

    @Test(dependsOnMethods = "checkInvalidPasword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String name, String url, String username, String database
            , String prefix, String schema, String validateMessage) {
        initDatasourceManagementPage();
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(name);
        configuration.addRedshiftBasicInfo(url, username, DATASOURCE_PASSWORD, database, prefix, schema);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), validateMessage);
    }

    //Customer input correct data and click Save button  for saving datasource
    @Test(dependsOnMethods = "checkRequiredDataSourceInformation")
    public void checkCreateNewDatasource() {
        initDatasourceManagementPage();
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addRedshiftBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail redshiftDetail = container.getConnectionDetail();
        checkRedshiftDetail(container.getDatasourceHeading().getName(), redshiftDetail.getTextUrl(), redshiftDetail.getTextUserName(),
                redshiftDetail.getTextDatabase(), redshiftDetail.getTextPrefix(),
                redshiftDetail.getTextSchema());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkCreateNewDatasource")
    public void checkEditDatasource() {
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        ConnectionDetail redshiftDetail = container.getConnectionDetail();
        checkRedshiftDetail(container.getDatasourceHeading().getName(), redshiftDetail.getTextUrl(), redshiftDetail.getTextUserName(),
                redshiftDetail.getTextDatabase(), redshiftDetail.getTextPrefix(),
                redshiftDetail.getTextSchema());
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(DATASOURCE_NAME_CHANGED);
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        configuration.addRedshiftBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        checkRedshiftDetailUpdate(container.getDatasourceHeading().getName(), redshiftDetail.getTextUrl(), redshiftDetail.getTextUserName(),
                redshiftDetail.getTextDatabase(), redshiftDetail.getTextPrefix(), redshiftDetail.getTextSchema());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME_CHANGED), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkEditDatasource")
    public void createViewTable() {
        dsMenu.selectDataSource(DATASOURCE_NAME_CHANGED);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        GenerateOutputStageDialog generateDialog = heading.getGenerateDialog();
        String sql = getResourceAsString("/sql_redshift.txt");
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
        configuration.addRedshiftBasicInfo(INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE,
                INVALID_VALUE, INVALID_VALUE);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        DatasourceMessageBar ErrormessageBar = heading.getErrorMessageDialog();
        assertEquals(ErrormessageBar.waitForErrorMessageBar().getText(), "Background task failed: Failed to obtain JDBC Connection:" +
                " Connection factory returned null from createConnection");
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

    private void checkRedshiftDetail(String name, String url, String username,
                                     String database, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME));
        assertEquals(url, DATASOURCE_URL);
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }

    private void checkRedshiftDetailUpdate(String name, String url, String username,
                                           String database, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME_CHANGED));
        assertEquals(url, DATASOURCE_URL);
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }
}
