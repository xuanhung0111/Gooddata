package com.gooddata.qa.graphene.snowflake;

import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.fragments.disc.process.DataSourceDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.utils.cloudresources.DataSourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class ValidateDatasourceSnowflakeTest extends AbstractADDProcessTest {
    private DataSourceUtils dataSourceUtils;
    private DataSourceDialog dialog;
    private DeploySDDProcessDialog deploySDDProcessDialog;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_WAREHOUSE = "ATT_WAREHOUSE";
    private final String DATASOURCE_DATABASE = "ATT_DATASOURCE_TEST";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "PUBLIC";
    private final String INVALID_VALUE = "invalid value" + generateHashString();

    @Test(dependsOnGroups = "createProject")
    public void checkRequiredDataSourceInformation() {
        DATASOURCE_URL = testParams.getSnowflakeJdbcUrl();
        DATASOURCE_USERNAME = testParams.getSnowflakeUserName();
        DATASOURCE_PASSWORD = testParams.getSnowflakePassword();
        dataSourceUtils = new DataSourceUtils(testParams.getUser());
        openSnowflakeDetail();
        dialog.clickValidateButton();
        assertEquals(dialog.getNumberOfRequiredMessage(), 6);
        dialog.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD, DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA, DATASOURCE_WAREHOUSE);
        dialog.clickConfirmButton();
        assertEquals(dialog.getNumberOfRequiredMessage(), 1);
    }

    @Test(dependsOnMethods =  "checkRequiredDataSourceInformation")
    public void checkInvalidPassword() {
        openSnowflakeDetail();
        dialog.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA, DATASOURCE_WAREHOUSE);
        dialog.clickValidateButton();
        assertEquals(dialog.getErrorMessage(), "Incorrect credentials");
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{INVALID_VALUE, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA, "Cannot reach the url"},
                {DATASOURCE_URL, INVALID_VALUE, DATASOURCE_USERNAME, DATASOURCE_DATABASE, DATASOURCE_SCHEMA,
                        "Warehouse not found"},
                {DATASOURCE_URL, DATASOURCE_WAREHOUSE, INVALID_VALUE, DATASOURCE_DATABASE, DATASOURCE_SCHEMA,
                        "Incorrect credentials"},
                {DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_SCHEMA,
                        "Database not found"},
                {DATASOURCE_URL, DATASOURCE_WAREHOUSE, DATASOURCE_USERNAME, DATASOURCE_DATABASE, INVALID_VALUE,
                        "Schema not found"},
                {DATASOURCE_URL, INVALID_VALUE, DATASOURCE_USERNAME, DATASOURCE_DATABASE, DATASOURCE_SCHEMA,
                        "Warehouse not found"}};
    }

    @Test(dependsOnMethods =  "checkInvalidPassword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String url, String warehouse, String username
            , String database, String schema, String validateMessage) {
        openSnowflakeDetail();
        dialog.addSnowflakeInfo(url, username, DATASOURCE_PASSWORD, database, schema, warehouse);
        dialog.clickValidateButton();
        assertEquals(dialog.getErrorMessage(), validateMessage);
    }

    @Test(dependsOnMethods =  "checkInvalidDataSourceInformation")
    public void checkCreateValidDatasource() {
        openSnowflakeDetail();
        dialog.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA, DATASOURCE_WAREHOUSE);
        dialog.addDatasourceName(DATASOURCE_NAME);
        dialog.addOutputStagePrefix(DATASOURCE_PREFIX);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), "Connection validation succeeded.");
        dialog.clickConfirmButton();
        dialog = DataSourceDialog.getInstance(browser);
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME);
    }

    @Test(dependsOnMethods =  "checkCreateValidDatasource")
    public void checkEditDatasource() {
        //check predefined datasource information
        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        deploySDDProcessDialog = deployForm.selectADDProcess();
        deploySDDProcessDialog.selectDataSource(DATASOURCE_NAME);
        dialog = deployForm.editDatasource();
        assertEquals(dialog.getTextDataSourceName(),DATASOURCE_NAME);
        assertEquals(dialog.getTextUrl(), DATASOURCE_URL);
        assertEquals(dialog.getTextUserName(), DATASOURCE_USERNAME);
        assertEquals(dialog.getTextDatabase(), DATASOURCE_DATABASE);
        assertEquals(dialog.getTextWarehouse(), DATASOURCE_WAREHOUSE);
        assertEquals(dialog.getTextPrefix(), DATASOURCE_PREFIX);
        assertEquals(dialog.getTextSchema(), DATASOURCE_SCHEMA);

        // check edit datasource and verify after edit
        dialog.addSnowflakeInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA, DATASOURCE_WAREHOUSE);
        dialog.addDatasourceName(DATASOURCE_NAME_CHANGED);
        dialog.addOutputStagePrefix(DATASOURCE_PREFIX);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), "Connection validation succeeded.");
        dialog.clickConfirmButton();
        dialog = DataSourceDialog.getInstance(browser);
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME_CHANGED);
        dialog = deployForm.editDatasource();
        assertEquals(dialog.getTextDataSourceName(),DATASOURCE_NAME_CHANGED);
        assertEquals(dialog.getTextUrl(), DATASOURCE_URL);
        assertEquals(dialog.getTextUserName(), DATASOURCE_USERNAME);
        assertEquals(dialog.getTextDatabase(), DATASOURCE_DATABASE);
        assertEquals(dialog.getTextWarehouse(), DATASOURCE_WAREHOUSE);
        assertEquals(dialog.getTextPrefix(), DATASOURCE_PREFIX);
        assertEquals(dialog.getTextSchema(), DATASOURCE_SCHEMA);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws IOException {
        String dataSourceID = dataSourceUtils.getDataSourceRestRequest().getDatasourceByName(DATASOURCE_NAME);
        String dataSourceChangeID = dataSourceUtils.getDataSourceRestRequest().getDatasourceByName(DATASOURCE_NAME_CHANGED);
        if (dataSourceID != null) {
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceID);
        }
        if (dataSourceChangeID != null) {
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceChangeID);
        }
    }

    private void openSnowflakeDetail() {
        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        deploySDDProcessDialog = deployForm.selectADDProcess();
        dialog = deployForm.addNewDatasource();
        dialog.selectDatasourceProvider(DataSourceDialog.DatasourceProvider.SNOWFLAKE);
    }
}
