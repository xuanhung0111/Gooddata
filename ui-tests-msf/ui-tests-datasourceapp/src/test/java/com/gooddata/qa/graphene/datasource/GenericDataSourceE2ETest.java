package com.gooddata.qa.graphene.datasource;

import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceManagementPage;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.ContentWrapper;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.DataSourceMenu;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.ContentDatasourceContainer;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.ConnectionConfiguration;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.DatasourceHeading;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.MoreContentDialog;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.EditDatasourceAliasDialog;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeploySDDProcessDialog;

import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;


public class GenericDataSourceE2ETest extends AbstractDatasourceManagementTest {
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private ContentDatasourceContainer container;
    private ConnectionConfiguration configuration;
    private DatasourceHeading heading;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private String URL_DATASOURCE;
    private final String DATASOURCE_TYPE = "Generic data source";
    private final String DATASOURCE_NAME = "Generic_datasource_" + generateHashString();
    private final String DATASOURCE_ALIAS = "Generic_alias_" + generateHashString();
    private final String CITY_KEY = "city_key";
    private final String CITY_VALUE = "city_value";
    private final String COUNTRY_KEY = "country_key";
    private final String COUNTRY_VALUE = "country_value";
    private final String HOMETOWN_SECURE_KEY = "hometown_secure_key";
    private final String HOMETOWN_VALUE = "house_secure_value";
    private final String VILLA_SECURE_KEY = "villa_secure_key";
    private final String VILLA_VALUE = "villa_secure_value";
    private final String COPY_REFERENCE = "${%s.%s}";
    private final String NEW_DATASOURCE_ALIAS = "New_alias_" + generateHashString();
    private final String GENERIC_ALIAS = "Generic_alias_" + generateHashString();
    private final String SPECIAL_ALIAS = "!@#!@#att";
    private final String ERROR_MSG = "\'%s\' invalid value";
    private final String REQUIRED_ERROR_MSG = "This field is required";
    private final String DUPLICATE_ERROR_MSG = "The alias is already existed";
    private final String WARNING_MSG = "Warning. The data source is used by the following processes";
    private final String PROCESS_NAME = "GenericProcess_" + generateHashString();


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

    @Test(dependsOnGroups = {"createProject"})
    public void initialStageTest() {
        if (testParams.isPIEnvironment() || testParams.isProductionEnvironment()
                || testParams.isPerformanceEnvironment()) {
            throw new SkipException("Initial Page is not tested on PI or Production environment");
        } else {
            initDatasourceManagementPage();
            dsMenu.selectGenericDataSource();
            contentWrapper.waitLoadingManagePage();
            container = contentWrapper.getContentDatasourceContainer();
            configuration = container.getConnectionConfiguration();
        }
    }

    @Test(dependsOnMethods = "initialStageTest")
    public void checkDefaultTemplateCreateDatasource() {
        assertEquals(configuration.getDatasourceType(), DATASOURCE_TYPE);
        assertTrue(configuration.isAddingComponentDisplayed());

        configuration.clickAddParameterButton();
        assertTrue(configuration.isNewLineParameterDisplayed());
        assertEquals(configuration.getPlaceHolderParameterInput(), asList("parameter name", "parameter value"));
        configuration.clickOnTrash();

        configuration.clickAddSecureParameterButton();
        assertTrue(configuration.isNewLineSecureParameterDisplayed());
        assertEquals(configuration.getPlaceHolderSecureParameterInput(), asList("secure parameter name", "secure parameter value"));
        configuration.clickOnTrash();
    }

    @Test(dependsOnMethods = "checkDefaultTemplateCreateDatasource")
    public void verifyCreateValidNewDatasourceTest() {
        log.info("========Datasource name is : " + DATASOURCE_NAME);
        log.info("========Datasource alias is : " + DATASOURCE_ALIAS);
        container.addConnectionTitle(DATASOURCE_NAME);
        container.addAliasTitle(DATASOURCE_ALIAS);
        configuration.clickAddParameterButton().inputAddParameter(CITY_KEY, CITY_VALUE);
        configuration.clickAddSecureParameterButton().inputAddSecureParameter(HOMETOWN_SECURE_KEY, HOMETOWN_VALUE);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        assertEquals(container.getDataSourceName(), DATASOURCE_NAME);
        assertEquals(container.getDataSourceAlias(), DATASOURCE_ALIAS);
        assertEquals(container.getDatasourceType(), DATASOURCE_TYPE);

        assertEquals(container.getCreatedKeyParamValue(CITY_KEY), CITY_KEY);
        assertEquals(container.getCreatedValueParamValue(CITY_KEY), CITY_VALUE);
        assertEquals(container.getCreatedKeyParamValue(HOMETOWN_SECURE_KEY), HOMETOWN_SECURE_KEY);
        assertEquals(container.getCreatedValueParamValue(HOMETOWN_SECURE_KEY), "******************");

        assertThat(container.getCopyReferenceParam(HOMETOWN_SECURE_KEY),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, HOMETOWN_SECURE_KEY)));
        sleepTightInSeconds(1);
        assertThat(container.getCopyReferenceParam(CITY_KEY),
                containsString(String.format(COPY_REFERENCE, DATASOURCE_ALIAS, CITY_KEY)));
    }

    @Test(dependsOnMethods = "verifyCreateValidNewDatasourceTest")
    public void editGenericDatasourceTest() {
        browser.navigate().refresh();
        heading = container.getDatasourceHeading();
        heading.clickMoreButton();
        MoreContentDialog dialogOption = MoreContentDialog.getInstance(browser);
        assertFalse(dialogOption.isGenerateOutputStageEnabled());
        assertFalse(dialogOption.isPublishIntoWorkspaceEnabled());

        dialogOption.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        assertThat(configuration.countTrashIcon(), equalTo(2));

        configuration.inputAddParameter(COUNTRY_KEY, COUNTRY_VALUE);
        configuration.inputAddSecureParameter(VILLA_SECURE_KEY, VILLA_VALUE);

        assertEquals(configuration.getCurrentParameterValue(), asList(COUNTRY_KEY, COUNTRY_VALUE));
        assertEquals(configuration.getCurrentSecureParameterInput(), asList(VILLA_SECURE_KEY, VILLA_VALUE));

        configuration.deleteParameterValue(COUNTRY_KEY);
        configuration.deleteSecureParameter(VILLA_SECURE_KEY);
        assertTrue(configuration.isEmptyParameter(), "Should delete all parameters");

        container.clickCancelButton();
        dsMenu.selectDataSource(DATASOURCE_NAME);
        heading.clickEditButton();
        contentWrapper.waitLoadingManagePage();
        configuration.inputAddParameter(COUNTRY_KEY, COUNTRY_VALUE);
        configuration.inputAddSecureParameter(VILLA_SECURE_KEY, VILLA_VALUE);
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        sleepTightInSeconds(1);

        assertEquals(container.getCreatedKeyParamValue(COUNTRY_KEY), COUNTRY_KEY);
        assertEquals(container.getCreatedValueParamValue(COUNTRY_KEY), COUNTRY_VALUE);
        assertEquals(container.getCreatedKeyParamValue(VILLA_SECURE_KEY), VILLA_SECURE_KEY);
        assertEquals(container.getCreatedValueParamValue(VILLA_SECURE_KEY), "******************");
    }

    @Test(dependsOnMethods = "editGenericDatasourceTest")
    public void editAliasDatasource() {
        log.info("========New datasource alias is: " + NEW_DATASOURCE_ALIAS);
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

    @Test(dependsOnMethods = "editAliasDatasource")
    public void createNewDatasourceWithInvalidCase() {
        log.info("========Generic alias is: " + GENERIC_ALIAS);
        dsMenu.selectGenericDataSource();
        container.clickSavebutton();

        assertTrue(container.isAliasErrorMessageDisplay(), "Alias error message should be displayed");
        assertEquals(container.getAliasErrorMessage(), REQUIRED_ERROR_MSG);

        container.addAliasTitle(NEW_DATASOURCE_ALIAS);
        container.clickSavebutton();
        sleepTightInSeconds(1);

        assertTrue(container.isAliasErrorMessageDisplay(), "Alias error message should be displayed");
        assertEquals(container.getAliasErrorMessage(), DUPLICATE_ERROR_MSG);

        container.addAliasTitle(GENERIC_ALIAS);
        configuration.clickAddParameterButton();
        configuration.inputOnlyAddParameter(CITY_KEY);
        configuration.inputOnlyAddParameter(CITY_KEY);
        configuration.clickAddSecureParameterButton();
        configuration.inputOnlyAddSecureParameter(COUNTRY_KEY);
        container.clickSavebutton();

        assertEquals(configuration.getErrorMessageOnParamLine(0), REQUIRED_ERROR_MSG);
        assertEquals(configuration.getErrorMessageOnParamLine(1), REQUIRED_ERROR_MSG);
        container.clickCancelButton();
    }

    @Test(dependsOnMethods = "createNewDatasourceWithInvalidCase")
    public void deployProcess() {
        ProjectDetailPage projectDetailPage = initDiscProjectDetailPage();
        sleepTightInSeconds(2);
        DeployProcessForm deployForm = projectDetailPage.clickDeployButton();
        DeploySDDProcessDialog deploySDDProcessDialog = deployForm
            .scrollToSelectProcessType(DeployProcessForm.ProcessType.LCM_RELEASE, 10000)
            .enterProcessName(PROCESS_NAME)
            .clickSwitchToDataSourceLink()
            .clickAddDatasource();
        assertEquals(deploySDDProcessDialog.getSelectedDataSourceName(), DATASOURCE_NAME);

        deployForm.enterProcessName(PROCESS_NAME).submit();
        assertTrue(projectDetailPage.hasProcess(PROCESS_NAME), "Process is not deployed");
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
