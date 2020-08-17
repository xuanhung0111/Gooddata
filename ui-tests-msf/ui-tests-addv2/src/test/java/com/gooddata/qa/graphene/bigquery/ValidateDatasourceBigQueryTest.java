package com.gooddata.qa.graphene.bigquery;

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

public class ValidateDatasourceBigQueryTest extends AbstractADDProcessTest {
    private DataSourceUtils dataSourceUtils;
    private DataSourceDialog dialog;
    private DeploySDDProcessDialog deploySDDProcessDialog;
    private String DATASOURCE_CLIENT_EMAIL;
    private String DATASOURCE_PRIVATE_KEY;
    private String privateKeyString;
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_CHANGED = "Auto_datasource_changed" + generateHashString();
    private final String DATASOURCE_PROJECT = "gdc-us-dev";
    private final String DATASOURCE_DATASET = "att_team";
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String INVALID_VALUE = "invalidvalue" + generateHashString();

    @Test(dependsOnGroups = "createProject")
    public void checkRequiredDataSourceInformation() {
        DATASOURCE_CLIENT_EMAIL = testParams.getBigqueryClientEmail();
        DATASOURCE_PRIVATE_KEY = testParams.getBigqueryPrivateKey();
        privateKeyString = DATASOURCE_PRIVATE_KEY.replace("\n", "\\n");
        dataSourceUtils = new DataSourceUtils(testParams.getUser());
        openBigQueryDetail();
        dialog.clickValidateButton();
        assertEquals(dialog.getNumberOfRequiredMessage(), 4);
        dialog.addBigquerryInfo(DATASOURCE_CLIENT_EMAIL, DATASOURCE_PRIVATE_KEY, DATASOURCE_PROJECT, DATASOURCE_DATASET);
        dialog.clickConfirmButton();
        assertEquals(dialog.getNumberOfRequiredMessage(), 1);
    }

    @Test(dependsOnMethods =  "checkRequiredDataSourceInformation")
    public void checkInvalidPassword() {
        openBigQueryDetail();
        dialog.addBigquerryInfo(DATASOURCE_CLIENT_EMAIL, INVALID_VALUE, DATASOURCE_PROJECT, DATASOURCE_DATASET);
        dialog.clickValidateButton();
        assertEquals(dialog.getErrorMessage(), "Incorrect credentials");
    }

    @DataProvider
    public Object[][] invalidInformation() {
        return new Object[][]{{INVALID_VALUE, DATASOURCE_PROJECT, DATASOURCE_DATASET, "Connection validation failed"},
                {DATASOURCE_CLIENT_EMAIL, INVALID_VALUE, DATASOURCE_DATASET, "Project not found"},
                {DATASOURCE_CLIENT_EMAIL, DATASOURCE_PROJECT, INVALID_VALUE, "Dataset not found"}
        };
    }

    @Test(dependsOnMethods =  "checkInvalidPassword", dataProvider = "invalidInformation")
    public void checkInvalidDataSourceInformation(String email, String project
            , String dataset, String validateMessage) {
        openBigQueryDetail();
        dialog.addBigquerryInfo(email, privateKeyString, project, dataset);
        dialog.clickValidateButton();
        assertEquals(dialog.getErrorMessage(), validateMessage);
    }

    @Test(dependsOnMethods =  "checkInvalidDataSourceInformation")
    public void checkCreateValidDatasource() {
        openBigQueryDetail();
        dialog.addBigquerryInfo(DATASOURCE_CLIENT_EMAIL, privateKeyString, DATASOURCE_PROJECT, DATASOURCE_DATASET);
        dialog.addDatasourceName(DATASOURCE_NAME);
        dialog.addOutputStagePrefix(DATASOURCE_PREFIX);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), "Connection validation succeeded.");
        dialog.clickConfirmButton();
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
        assertEquals(dialog.getTextClientEmail(), DATASOURCE_CLIENT_EMAIL);
        assertEquals(dialog.getTextProject(), DATASOURCE_PROJECT);
        assertEquals(dialog.getTextDataset(), DATASOURCE_DATASET);
        assertEquals(dialog.getTextPrefix(), DATASOURCE_PREFIX);

        // check edit datasource and verify after edit
        dialog.addBigquerryInfo(DATASOURCE_CLIENT_EMAIL, privateKeyString, DATASOURCE_PROJECT, DATASOURCE_DATASET);
        dialog.addDatasourceName(DATASOURCE_NAME_CHANGED);
        dialog.addOutputStagePrefix(DATASOURCE_PREFIX);
        dialog.clickValidateButton();
        assertEquals(dialog.getSuccessMessage(), "Connection validation succeeded.");
        dialog.clickConfirmButton();
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME_CHANGED);
        dialog = deployForm.editDatasource();
        assertEquals(dialog.getTextDataSourceName(),DATASOURCE_NAME_CHANGED);
        assertEquals(dialog.getTextClientEmail(), DATASOURCE_CLIENT_EMAIL);
        assertEquals(dialog.getTextProject(), DATASOURCE_PROJECT);
        assertEquals(dialog.getTextDataset(), DATASOURCE_DATASET);
        assertEquals(dialog.getTextPrefix(), DATASOURCE_PREFIX);
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

    private void openBigQueryDetail() {
        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        deploySDDProcessDialog = deployForm.selectADDProcess();
        dialog = deployForm.addNewDatasource();
        dialog.selectDatasourceProvider(DataSourceDialog.DatasourceProvider.BIGQUERRY);
    }
}
