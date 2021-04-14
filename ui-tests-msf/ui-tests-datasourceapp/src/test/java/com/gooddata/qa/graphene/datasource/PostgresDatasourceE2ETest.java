package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class PostgresDatasourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private final String INITIAL_TEXT = "Create your first data source\n" +
            "Data source stores information about connection into a data warehouse";
    private final String POSTGRE_SQL = "PostgreSQL";
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_INVALID = "Auto_datasource_invalid" + generateHashString();
    private final String DATASOURCE_DATABASE = "qa";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "qa_test";
    private final String INVALID_VALUE = "invalid value" + generateHashString();
    private final List<String> LIST_SSL_ITEM = asList("Prefer", "Require", "Verify-full");

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        DATASOURCE_URL = testParams.getPostgreJdbcUrl().replace("jdbc:postgresql://", "")
                .replace(":5432/", "");
        DATASOURCE_USERNAME = testParams.getPostgreUserName();
        DATASOURCE_PASSWORD = testParams.getPostgrePassword();
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
            initDiscOverviewPage();
            dataSourceManagementPage = OverviewPage.getInstance(browser).openDatasourcePage();
            assertTrue(browser.getCurrentUrl().contains("?navigation=disc"));
            InitialContent initialContent = contentWrapper.getInitialContent();
            assertThat(initialContent.getInitialContentText(), containsString(INITIAL_TEXT));
            assertEquals(initialContent.getNumberOfCloudResourceButton(), 5);
            assertEquals(initialContent.getTextOnCloudResourceButton(3), POSTGRE_SQL);
            initialContent.openPostgresEdit();
            DataSourceMenu dsMenu = dataSourceManagementPage.getMenuBar();
            dsMenu.selectPostgreResource();
        } else {
            initDiscOverviewPage();
            dataSourceManagementPage = OverviewPage.getInstance(browser).openDatasourcePage();
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
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        container.clickSavebutton();
        // check vailidate required field
        assertEquals(container.getNumberOfRequiredMessage(), 4);
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{DATASOURCE_NAME, DATASOURCE_URL, INVALID_VALUE, DATASOURCE_DATABASE,
                        DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Incorrect credentials"},
                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_USERNAME,
                        INVALID_VALUE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection failed! Database not found"}};
//                {DATASOURCE_NAME, DATASOURCE_URL, DATASOURCE_USERNAME,
//                        DATASOURCE_DATABASE, DATASOURCE_PREFIX, INVALID_VALUE, "Connection failed! Schema not found"}};
    }

    @Test(dependsOnMethods = "checkRequiredDataSourceInformation")
    public void checkInvalidPassword() {
        initDatasourceManagementPage();
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addPostgreBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        container.clickValidateButton();
        assertEquals(container.getValidateMessage(), "Connection failed! Incorrect credentials");
    }

    @Test(dependsOnMethods = "checkInvalidPassword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String name, String url, String username, String database, String prefix, String schema, String validateMessage) {
        initDatasourceManagementPage();
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(name);
        container.addPostgreBasicInfo(url, username, DATASOURCE_PASSWORD, database, prefix, schema);
        container.clickValidateButton();
        assertEquals(container.getValidateMessage(), validateMessage);
    }

    //Customer input correct data and click Save button  for saving datasource
    @Test(dependsOnMethods = "checkInvalidDataSourceInformation")
    public void checkCreateNewDatasource() {
        initDatasourceManagementPage();
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        assertThat(container.listPostgreSSLItem(), containsInAnyOrder(LIST_SSL_ITEM.toArray()));
        container.clickCancelButton();
        contentWrapper.waitLoadingManagePage();
        dsMenu.selectPostgreResource();
        contentWrapper.waitLoadingManagePage();
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addPostgreBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        container.clickValidateButton();
        assertEquals(container.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail postgresDetail = container.getConnectionDetail();
        checkPostgresDetail(container.getDatasourceHeading().getName(), postgresDetail.getTextUrl(), postgresDetail.getTextUsername(),
                postgresDetail.getTextDatabase(), postgresDetail.getTextPrefix(), postgresDetail.getTextSchema());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkCreateNewDatasource")
    public void checkEditDatasource() {
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        ConnectionDetail postgresDetail = container.getConnectionDetail();
        checkPostgresDetail(container.getDatasourceHeading().getName(), postgresDetail.getTextUrl(), postgresDetail.getTextUsername(),
                postgresDetail.getTextDatabase(), postgresDetail.getTextPrefix(), postgresDetail.getTextSchema());
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(DATASOURCE_NAME_CHANGED);
        container.addPostgreBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD, DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA);
        container.clickValidateButton();
        assertEquals(container.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        checkPostgresDetailUpdate(container.getDatasourceHeading().getName(), postgresDetail.getTextUrl(), postgresDetail.getTextUsername(),
                postgresDetail.getTextDatabase(), postgresDetail.getTextPrefix(), postgresDetail.getTextSchema());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME_CHANGED), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkEditDatasource")
    public void createViewTable() {
        dsMenu.selectDataSource(DATASOURCE_NAME_CHANGED);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        GenerateOutputStageDialog generateDialog = heading.getGenerateDialog();
        String sql = getResourceAsString("/sql_postgre.txt");
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
        container.addPostgreBasicInfo(INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        DatasourceMessageBar ErrormessageBar = heading.getErrorMessageDialog();
        assertTrue(ErrormessageBar.waitForErrorMessageBar().getText().contains("Background task failed: Failed to obtain JDBC Connection"));
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

    private void checkPostgresDetail(String name, String url, String username,
                                      String database, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME));
        assertEquals(url, "jdbc:postgresql://qa-pg.dev.intgdc.com:5432");
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }

    private void checkPostgresDetailUpdate(String name, String url, String username,
                                            String database, String prefix, String schema) {
        assertTrue(name.contains(DATASOURCE_NAME_CHANGED));
        assertEquals(url, "jdbc:postgresql://qa-pg.dev.intgdc.com:5432");
        assertEquals(username, DATASOURCE_USERNAME);
        assertEquals(database, DATASOURCE_DATABASE);
        assertEquals(prefix, DATASOURCE_PREFIX);
        assertEquals(schema, DATASOURCE_SCHEMA);
    }

}
