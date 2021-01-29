package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;

import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class S3DataSourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private ContentDatasourceContainer container;
    private S3Configuration configuration;
    private DatasourceHeading heading;
    private String URL_DATASOURCE;
    private final String DATASOURCE_TYPE = "Amazon S3";
    private final String DATASOURCE_NAME = "S3_datasource_" + generateHashString();
    private final String DATASOURCE_ALIAS = "S3_alias_" + generateHashString();
    private final String NEW_DATASOURCE_ALIAS = "New_alias_" + generateHashString();
    private final String COPY_REFERENCE = "${%s.%s}";
    private final String SPECIAL_ALIAS = "!@#!@#att";
    private final String ERROR_MSG = "\'%s\' invalid value";
    private final String REQUIRED_ERROR_MSG = "This field is required";
    private final String DUPLICATE_ERROR_MSG = "The alias is already existed";
    private final String BUCKET_PLACEHOLDER = "my-bucket/analytics";
    private final String BUCKET_VALUE = "msf-dev-grest";
    private final String ACCESS_KEY = "AKIA4JVILUMKI4WMQYJF";
    private final String SECRET_KEY = "1kKYFEBh4xJRLx8KCngHHdUjc+8Y5gjwNND2zv6h";
    private final String VALIDATE_MSG = "Connection succeeded";
    private final String REGION_VALUE = "Region (optional)";
    private final String BUCKET_PARAM = "Bucket";
    private final String ACCESSKEY_PARAM = "Access key";
    private final String SECRET_PARAM = "Secret key";
    private final String REGION_PARAM = "Region (optional)";
    private final String ENCRYPTION_PARAM = "Server side encryption";
    private final String PROCESS_NAME = "s3Process_" + generateHashString();
    private final String DATASOURCE_PATH = "msf-dev";
    private final String WARNING_MSG = "Warning. The data source is used by the following processes";

    @Override
    public void prepareProject() throws Throwable {
        super.prepareProject();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void initialStageTest() {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        }
        initDatasourceManagementPage();

        //In case, has ADS Datasource on Domain , ignore test check "Create new S3 Datasource"
        if (dsMenu.isListDatasourceEmpty()) {
            assertTrue(dataSourceManagementPage.isCreateS3DatasourceButtonDisplayed(),
                    "Should have create S3 datasource button");
        }
        dsMenu.selectS3DataSource();
        contentWrapper.waitLoadingManagePage();
        container = contentWrapper.getContentDatasourceContainer();
        configuration = container.getS3ConnectionConfiguration();
    }

    @Test(dependsOnMethods = "initialStageTest")
    public void checkDefaultTemplateCreateS3Datasource() {
        assertEquals(configuration.getDatasourceType(), DATASOURCE_TYPE);
        assertEquals(configuration.getPlaceHolderBucketField(), BUCKET_PLACEHOLDER);
        assertFalse(configuration.isEncryptionChecked(), "Should be disable by default");
    }

    @Test(dependsOnMethods = "checkDefaultTemplateCreateS3Datasource")
    public void verifyCreateValidNewDatasourceTest() {
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addAliasTitle(DATASOURCE_ALIAS);
        configuration.addBucket(BUCKET_VALUE);
        configuration.addAccessKey(ACCESS_KEY);
        configuration.addSecretKey(SECRET_KEY);

        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), VALIDATE_MSG);

        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();

        assertEquals(container.getDataSourceName(), DATASOURCE_NAME);
        assertEquals(container.getDataSourceAlias(), DATASOURCE_ALIAS);
        assertEquals(container.getDatasourceType(), DATASOURCE_TYPE);

        assertEquals(container.getBucketValue(), BUCKET_VALUE);
        assertEquals(container.getAccessKeyValue(), ACCESS_KEY);
        assertEquals(container.getSecretKeyValue(), "******************");
        assertEquals(container.getRegionValue(), "not set");
        assertEquals(container.getEncryptionValue(), "false");

        assertThat(container.getCopyReferenceParam(BUCKET_PARAM),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, simplifyText(BUCKET_PARAM))));
        assertThat(container.getCopyReferenceParam(SECRET_PARAM),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, simplifyText(SECRET_PARAM))));
        assertThat(container.getCopyReferenceParam(REGION_PARAM),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, "region")));
        assertThat(container.getCopyReferenceParam(ENCRYPTION_PARAM),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, "server_side_encryption")));
        assertThat(container.getCopyReferenceParam(ACCESSKEY_PARAM),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, simplifyText(ACCESSKEY_PARAM))));
    }

    @Test(dependsOnMethods = "verifyCreateValidNewDatasourceTest")
    public void editS3DatasourceTest() {
        heading = container.getDatasourceHeading();
        heading.clickMoreButton();
        MoreContentDialog dialogOption = MoreContentDialog.getInstance(browser);
        assertFalse(dialogOption.isGenerateOutputStageEnabled());
        assertFalse(dialogOption.isPublishIntoWorkspaceEnabled());

        dialogOption.clickEditButton();
        contentWrapper.waitLoadingManagePage();

        configuration.addBucket(BUCKET_VALUE + "_edit");
        configuration.addAccessKey(ACCESS_KEY + "_edit");
        configuration.addSecretKey(SECRET_KEY + "_edit");
        container.clickCancelButton();

        assertEquals(container.getBucketValue(), BUCKET_VALUE);
        assertEquals(container.getAccessKeyValue(), ACCESS_KEY);

        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        configuration.addBucket(BUCKET_VALUE + "_edit");
        configuration.addAccessKey(ACCESS_KEY + "_edit");
        configuration.addSecretKey(SECRET_KEY + "_edit");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();

        assertEquals(container.getBucketValue(), BUCKET_VALUE + "_edit");
        assertEquals(container.getAccessKeyValue(), ACCESS_KEY + "_edit");
    }

    @Test(dependsOnMethods = "editS3DatasourceTest")
    public void editS3AliasDatasource() {
        heading.clickEditAliasButton();
        EditDatasourceAliasDialog editAlias =  EditDatasourceAliasDialog.getInstance(browser);
        editAlias.inputAliasName(NEW_DATASOURCE_ALIAS);

        assertEquals(container.getDataSourceAlias(), NEW_DATASOURCE_ALIAS);
        heading.clickEditAliasButton();
        editAlias.inputAliasName(SPECIAL_ALIAS);
        assertTrue(editAlias.isErrorMessageDisplayed(), "Error message should be displayed");
        assertThat(editAlias.getErrorMessageContent(), containsString(String.format(ERROR_MSG, SPECIAL_ALIAS)));
        editAlias.clickCancelButton();
        URL_DATASOURCE = browser.getCurrentUrl();
    }

    @Test(dependsOnMethods = "editS3AliasDatasource")
    public void checkDuplicatedDatasource() {
        dsMenu.selectS3DataSource();
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addAliasTitle(NEW_DATASOURCE_ALIAS);
        configuration.addBucket(BUCKET_VALUE);
        configuration.addAccessKey(ACCESS_KEY);
        configuration.addSecretKey(SECRET_KEY);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        assertTrue(container.isAliasErrorMessageDisplay(), "Alias error message should be displayed");
        assertEquals(container.getAliasErrorMessage(), DUPLICATE_ERROR_MSG);
        container.clickCancelButton();
    }

    @Test(dependsOnMethods = "checkDuplicatedDatasource")
    public void createNewS3DatasourceWithInvalidCase() {
        dsMenu.selectS3DataSource();
        container.clickSavebutton();
        assertTrue(container.isAliasErrorMessageDisplay(), "Alias error message should be displayed");
        assertEquals(container.getAliasErrorMessage(), REQUIRED_ERROR_MSG);

        assertEquals(configuration.getErrorMessageOnS3DatasourceParamLine(BUCKET_PARAM), REQUIRED_ERROR_MSG);
        assertEquals(configuration.getErrorMessageOnS3DatasourceParamLine(ACCESSKEY_PARAM), REQUIRED_ERROR_MSG);
        assertEquals(configuration.getErrorMessageOnS3DatasourceParamLine(SECRET_PARAM), REQUIRED_ERROR_MSG);
        container.clickCancelButton();

        dsMenu.selectDataSource(DATASOURCE_NAME);
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        configuration.addBucket(BUCKET_VALUE);
        configuration.addAccessKey(ACCESS_KEY);
        configuration.addSecretKey(SECRET_KEY + "_invalid");

        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(),  "Connection failed! Invalid secret key");

        configuration.addSecretKey(SECRET_KEY);
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), VALIDATE_MSG);
    }

    @Test(dependsOnMethods = "createNewS3DatasourceWithInvalidCase")
    public void deployProcess() {
        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        DeploySDDProcessDialog deploySDDProcessDialog = deployForm.selectADDProcess()
                .selectDataSourceType(DATASOURCE_NAME);
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME);

        deployForm.enterProcessName(PROCESS_NAME).submit();
        assertTrue(projectDetailPage.hasProcess(PROCESS_NAME), "Process is not deployed");

        projectDetailPage.getProcess(PROCESS_NAME).clickRedeployButton();
        assertTrue(deploySDDProcessDialog.isS3DatasourceDisabled(), "Must be disabled when re-deploy process");
        deploySDDProcessDialog.inputDatasourcePath(DATASOURCE_PATH);
        deployForm.submit();
        assertTrue(projectDetailPage.hasProcess(PROCESS_NAME), "Process is not re-deployed");
        projectDetailPage.getProcess(PROCESS_NAME).clickRedeployButton();
        assertEquals(deploySDDProcessDialog.getDatasourcePath(), DATASOURCE_PATH);
    }

    @Test(dependsOnMethods = "deployProcess")
    public void editDatasourceInProcess() {
        log.info("======== Reopen datasource page =======");
        openUrl(URL_DATASOURCE);
        heading.clickEditAliasButton();
        EditDatasourceAliasDialog editAlias =  EditDatasourceAliasDialog.getInstance(browser);
        assertThat(editAlias.getWarningMessageContent(), containsString(WARNING_MSG));
    }

    @AfterClass(alwaysRun = true)
    public void deleteProcessAndDatasource() {
        initDiscProjectDetailPage().deleteProcess(PROCESS_NAME);
        openUrl(URL_DATASOURCE);
        heading.clickMoreButton();
        MoreContentDialog dialogOption = MoreContentDialog.getInstance(browser);
        dialogOption.clickDeleteButton().clickDelete();
    }
}
