package com.gooddata.qa.graphene.redshift;

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

public class ValidateDatasourceRedshiftTest extends AbstractADDProcessTest {
    private DataSourceUtils dataSourceUtils;
    private DataSourceDialog dialog;
    private DeploySDDProcessDialog deploySDDProcessDialog;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private String REDSHIFT_IAM_SECRETKEY;
    private String REDSHIFT_IAM_ACCESSKEY;
    private String REDSHIFT_IAM_DBUSER;
    private String REDSHIFT_IAM_LONG_URL;
    private String REDSHIFT_IAM_SHORT_URL;
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_IAM = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_IAM_2 = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_DATABASE = "dev";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATASOURCE_SCHEMA = "automation_daily_test";
    private final String INVALID_VALUE = "invalid value" + generateHashString();

    @Test(dependsOnGroups = "createProject")
    public void checkRequiredDataSourceInformation() {
        DATASOURCE_URL = testParams.getRedshiftJdbcUrl();
        DATASOURCE_USERNAME = testParams.getRedshiftUserName();
        DATASOURCE_PASSWORD = testParams.getRedshiftPassword();
        REDSHIFT_IAM_DBUSER = testParams.getRedshiftIAMDbUser();
        REDSHIFT_IAM_ACCESSKEY = testParams.getRedshiftIAMAccessKey();
        REDSHIFT_IAM_SECRETKEY = testParams.getRedshiftIAMSecretKey();
        REDSHIFT_IAM_LONG_URL = testParams.getRedshiftIAMLongUrl();
        REDSHIFT_IAM_SHORT_URL = testParams.getRedshiftIAMShortUrl();
        dataSourceUtils = new DataSourceUtils(testParams.getUser());
        openRedshiftDetail();
        dialog.clickValidateButton();
        assertEquals(dialog.getNumberOfRequiredMessage(), 5);
        dialog.addBasicRedshiftInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD, DATASOURCE_DATABASE, DATASOURCE_SCHEMA);
        dialog.clickConfirmButton();
        assertEquals(dialog.getNumberOfRequiredMessage(), 1);
    }

    @Test(dependsOnMethods =  "checkRequiredDataSourceInformation")
    public void checkInvalidPassword() {
        openRedshiftDetail();
        dialog.addBasicRedshiftInfo(DATASOURCE_URL, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_DATABASE, DATASOURCE_SCHEMA);
        dialog.clickValidateButton();
        assertEquals(dialog.getErrorMessage(), "Incorrect credentials");
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{INVALID_VALUE, DATASOURCE_USERNAME, DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA, "Cannot reach the url"},
                {DATASOURCE_URL, INVALID_VALUE, DATASOURCE_DATABASE, DATASOURCE_SCHEMA,
                        "Incorrect credentials"},
                {DATASOURCE_URL, DATASOURCE_USERNAME, INVALID_VALUE, DATASOURCE_SCHEMA,
                        "Database not found"},
                {DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_DATABASE, INVALID_VALUE,
                        "Schema not found"}};
    }

    @Test(dependsOnMethods =  "checkInvalidPassword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String url, String username
            , String database, String schema, String validateMessage) {
        openRedshiftDetail();
        dialog.addBasicRedshiftInfo(url, username, DATASOURCE_PASSWORD, database, schema);
        dialog.clickValidateButton();
        assertEquals(dialog.getErrorMessage(), validateMessage);
    }

    @Test(dependsOnMethods =  "checkInvalidDataSourceInformation")
    public void checkCreateValidDatasource() {
        openRedshiftDetail();
        dialog.addBasicRedshiftInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA);
        dialog.addDatasourceName(DATASOURCE_NAME);
        dialog.addOutputStagePrefix(DATASOURCE_PREFIX);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), "Connection validation succeeded.");
        dialog.clickConfirmButton();
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME);
    }

    @DataProvider
    public Object[][] IAMInformation() {
        return new Object[][]{{DATASOURCE_NAME_IAM, REDSHIFT_IAM_LONG_URL, REDSHIFT_IAM_DBUSER, REDSHIFT_IAM_ACCESSKEY,
                DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection validation succeeded."},
                {DATASOURCE_NAME_IAM_2, REDSHIFT_IAM_SHORT_URL, REDSHIFT_IAM_DBUSER, REDSHIFT_IAM_ACCESSKEY,
                        DATASOURCE_DATABASE, DATASOURCE_PREFIX, DATASOURCE_SCHEMA, "Connection validation succeeded."}};
    }

    @Test(dependsOnMethods =  "checkCreateValidDatasource", dataProvider = "IAMInformation")
    public void checkCreateValidIAMDatasource(String name, String url, String dbuser, String accesskey, String database
            , String prefix, String schema, String validateMessage) {
        openRedshiftDetail();
        dialog.addIAMRedshiftInfo(url, dbuser, accesskey, REDSHIFT_IAM_SECRETKEY, database, schema);
        dialog.addDatasourceName(name);
        dialog.addOutputStagePrefix(prefix);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), validateMessage);
        dialog.clickConfirmButton();
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), name);
    }

    @Test(dependsOnMethods =  "checkCreateValidIAMDatasource")
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
        assertEquals(dialog.getTextPrefix(), DATASOURCE_PREFIX);
        assertEquals(dialog.getTextSchema(), DATASOURCE_SCHEMA);

        // check edit datasource and verify after edit
        dialog.addBasicRedshiftInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,DATASOURCE_DATABASE,
                DATASOURCE_SCHEMA);
        dialog.addDatasourceName(DATASOURCE_NAME_CHANGED);
        dialog.addOutputStagePrefix(DATASOURCE_PREFIX);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), "Connection validation succeeded.");
        dialog.clickConfirmButton();
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME_CHANGED);
        dialog = deployForm.editDatasource();
        assertEquals(dialog.getTextDataSourceName(),DATASOURCE_NAME_CHANGED);
        assertEquals(dialog.getTextUrl(), DATASOURCE_URL);
        assertEquals(dialog.getTextUserName(), DATASOURCE_USERNAME);
        assertEquals(dialog.getTextDatabase(), DATASOURCE_DATABASE);
        assertEquals(dialog.getTextPrefix(), DATASOURCE_PREFIX);
        assertEquals(dialog.getTextSchema(), DATASOURCE_SCHEMA);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws IOException {
        String dataSourceID = dataSourceUtils.getDataSourceRestRequest().getDatasourceByName(DATASOURCE_NAME);
        String dataSourceChangeID = dataSourceUtils.getDataSourceRestRequest().getDatasourceByName(DATASOURCE_NAME_CHANGED);
        String dataSourceIAM = dataSourceUtils.getDataSourceRestRequest().getDatasourceByName(DATASOURCE_NAME_IAM);
        String dataSourceIAM2 = dataSourceUtils.getDataSourceRestRequest().getDatasourceByName(DATASOURCE_NAME_IAM_2);
        if (dataSourceID != null) {
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceID);
        }
        if (dataSourceChangeID != null) {
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceChangeID);
        }
        if (dataSourceIAM != null) {
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceIAM);
        }
        if (dataSourceIAM2 != null) {
            dataSourceUtils.getDataSourceRestRequest().deleteDataSource(dataSourceIAM2);
        }
    }

    private void openRedshiftDetail() {
        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        deploySDDProcessDialog = deployForm.selectADDProcess();
        dialog = deployForm.addNewDatasource();
        dialog.selectDatasourceProvider(DataSourceDialog.DatasourceProvider.REDSHIFT);
    }
}
