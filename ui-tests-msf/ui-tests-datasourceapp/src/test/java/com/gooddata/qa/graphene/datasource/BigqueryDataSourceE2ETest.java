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

public class BigqueryDataSourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_CLIENT_EMAIL;
    private String DATASOURCE_PRIVATE_KEY;
    private String privateKeyString;
    private final String INITIAL_TEXT = "Create your first data source\n" +
            "Data source stores information about connection into a data warehouse";
    private final String BIG_QUERY = "Google BigQuery";
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_INVALID = "Auto_datasource_invalid" + generateHashString();
    private final String DATASOURCE_PROJECT = "gdc-us-dev";
    private final String DATASOURCE_DATASET = "att_team";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String INVALID_VALUE = "invalidvalue" + generateHashString();

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        DATASOURCE_CLIENT_EMAIL = testParams.getBigqueryClientEmail();
        DATASOURCE_PRIVATE_KEY = testParams.getBigqueryPrivateKey();
        privateKeyString = DATASOURCE_PRIVATE_KEY.replace("\n", "\\n");
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
            assertEquals(initialContent.getNumberOfCloudResourceButton(), 5);
            assertEquals(initialContent.getTextOnCloudResourceButton(1), BIG_QUERY);
            initialContent.openBigQueryEdit();
            dataSourceManagementPage = initDatasourceManagementPage();
            DataSourceMenu dsMenu = dataSourceManagementPage.getMenuBar();
            dsMenu.selectBigQueryResource();
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
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.clickSavebutton();
        // check vailidate required field
        assertEquals(configuration.getNumberOfRequiredMessage(), 4);
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{DATASOURCE_NAME, INVALID_VALUE, DATASOURCE_PROJECT,
                DATASOURCE_DATASET, DATASOURCE_PREFIX, "Connection failed! Connection validation failed"},
                {DATASOURCE_NAME, DATASOURCE_CLIENT_EMAIL, INVALID_VALUE,
                        DATASOURCE_DATASET, DATASOURCE_PREFIX, "Connection failed! Project not found"},
                {DATASOURCE_NAME, DATASOURCE_CLIENT_EMAIL, DATASOURCE_PROJECT,
                        INVALID_VALUE, DATASOURCE_PREFIX, "Connection failed! Dataset not found"}
        };
    }

    @Test(dependsOnMethods = "checkRequiredDataSourceInformation")
    public void checkInvalidPasword() {
        initDatasourceManagementPage();
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addBigqueryInfo(DATASOURCE_CLIENT_EMAIL, INVALID_VALUE, DATASOURCE_PROJECT, DATASOURCE_DATASET, DATASOURCE_PREFIX);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection failed! Incorrect credentials");
    }

    @Test(dependsOnMethods = "checkInvalidPasword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String name, String clientEmail, String project,
                                                  String dataset, String prefix, String validateMessage) {
        initDatasourceManagementPage();
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(name);
        configuration.addBigqueryInfo(clientEmail, privateKeyString, project, dataset, prefix);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), validateMessage);
    }

    //Customer input correct data and click Save button  for saving datasource
    @Test(dependsOnMethods = "checkInvalidDataSourceInformation")
    public void checkCreateNewDatasource() {
        initDatasourceManagementPage();
        dsMenu.selectBigQueryResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addBigqueryInfo(DATASOURCE_CLIENT_EMAIL, privateKeyString, DATASOURCE_PROJECT,
                DATASOURCE_DATASET, DATASOURCE_PREFIX);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail bigqueryDetail = container.getConnectionDetail();
        checkBigqueryDetail(container.getDatasourceHeading().getName(), bigqueryDetail.getTextClientEmail(),
                bigqueryDetail.getTextProject(), bigqueryDetail.getTextDataset(), bigqueryDetail.getTextPrefix());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkCreateNewDatasource")
    public void checkEditDatasource() {
        dsMenu.selectDataSource(DATASOURCE_NAME);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        ConnectionDetail bigqueryDetail = container.getConnectionDetail();
        checkBigqueryDetail(container.getDatasourceHeading().getName(), bigqueryDetail.getTextClientEmail(), bigqueryDetail.getTextProject(),
                bigqueryDetail.getTextDataset(), bigqueryDetail.getTextPrefix());
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        container.addConnectionTitle(DATASOURCE_NAME_CHANGED);
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        configuration.addBigqueryInfo(DATASOURCE_CLIENT_EMAIL, privateKeyString, DATASOURCE_PROJECT,
                DATASOURCE_DATASET, DATASOURCE_PREFIX);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        checkBigQueryDetailUpdate(container.getDatasourceHeading().getName(), bigqueryDetail.getTextClientEmail(), bigqueryDetail.getTextProject(),
                bigqueryDetail.getTextDataset(), bigqueryDetail.getTextPrefix());
        assertTrue(dsMenu.isDataSourceExist(DATASOURCE_NAME_CHANGED), "list data sources doesn't have created Datasource");
    }

    @Test(dependsOnMethods = "checkEditDatasource")
    public void createViewTable() {
        dsMenu.selectDataSource(DATASOURCE_NAME_CHANGED);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        GenerateOutputStageDialog generateDialog = heading.getGenerateDialog();
        String sql = getResourceAsString("/sql_bigquery.txt");
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
        configuration.addBigqueryInfo(INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, INVALID_VALUE);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        DatasourceMessageBar ErrormessageBar = heading.getErrorMessageDialog();
        assertEquals(ErrormessageBar.waitForErrorMessageBar().getText(), "Background task failed: Invalid BigQuery private key: Invalid PKCS#8 data.");
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

    public void deleteDatasource(String datasourceName) {
        dsMenu.selectDataSource(datasourceName);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDelete();
        initDatasourceManagementPage();
        contentWrapper.waitLoadingManagePage();
        dsMenu.waitForDatasourceNotVisible(datasourceName);
    }

    private void checkBigqueryDetail(String name, String clientEmail, String project, String dataset, String prefix) {
        assertTrue(name.contains(DATASOURCE_NAME), "Datasource name is not correct");
        assertEquals(clientEmail, DATASOURCE_CLIENT_EMAIL);
        assertEquals(project, DATASOURCE_PROJECT);
        assertEquals(dataset, DATASOURCE_DATASET);
        assertEquals(prefix, DATASOURCE_PREFIX);
    }

    private void checkBigQueryDetailUpdate(String name, String clientemail, String project, String dataset, String prefix) {
        assertTrue(name.contains(DATASOURCE_NAME_CHANGED));
        assertEquals(clientemail, DATASOURCE_CLIENT_EMAIL);
        assertEquals(project, DATASOURCE_PROJECT);
        assertEquals(dataset, DATASOURCE_DATASET);
        assertEquals(prefix, DATASOURCE_PREFIX);
    }
}
