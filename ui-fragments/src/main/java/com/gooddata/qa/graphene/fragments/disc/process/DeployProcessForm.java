package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.browser.BrowserUtils;

public class DeployProcessForm extends AbstractFragment {

    public static final By LOCATOR = By.className("ait-process-deploy-fragment");

    @FindBy(className = "deploy-process-dialog-area")
    private WebElement dialogArea;

    @FindBy(css = "input[value='ZIP']")
    private WebElement zipFileOption;

    @FindBy(css = ".git-radio input")
    private WebElement gitOption;

    @FindBy(css = ".select-zip .fileInput")
    private WebElement packageInput;

    @FindBy(xpath = ".//*[text()='FULL PATH TO INFO.JSON']/following::input[1]")
    private WebElement gitPathInput;

    @FindBy(xpath = ".//*[text()='PROCESS NAME']/following::input")
    private WebElement processNameInput;

    @FindBy(className = "button-positive")
    private WebElement deployButton;

    @FindBy(className = "ait-component-selection-dropdown-button")
    private ProcessTypeDropdown processTypeDropdown;

    @FindBy(xpath = ".//*[text()='PATH TO CONFIGURATION.JSON']/following::input[1]")
    private WebElement s3ConfigurationPathInput;

    @FindBy(xpath = ".//*[text()='ACCESS KEY FOR S3']/following::input[1]")
    private WebElement s3AccessKeyInput;

    @FindBy(className = "expand-btn")
    private WebElement expandAdditionalParamsButton;

    @FindBy(xpath = ".//*[text()='SECRET KEY FOR S3']/following::input[1]")
    private WebElement s3SecretKeyInput;

    @FindBy(xpath = ".//*[text()='Region']/following::input")
    private WebElement s3RegionInput;

    @FindBy(css = ".form-title .input-checkbox")
    private WebElement s3ServerSideEncryptionInput;

    @FindBy(className = "process-type-link-to-help")
    private WebElement learnMore;

    @FindBy(className = "s-btn-create_data_source")
    private WebElement btnCreateDataSource;

    @FindBy(className = "s-btn-edit_data_source")
    private WebElement btnEditDatasource;

    @FindBy(className = "link")
    private WebElement switchToDatasourceLink;

    @FindBy(className = "generic-add-data-source-button")
    private WebElement addDatasourceButton;


    public static final DeployProcessForm getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DeployProcessForm.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public static final DeployProcessForm getInstance(By locator, SearchContext searchContext) {
        return Graphene.createPageFragment(DeployProcessForm.class, waitForElementVisible(locator, searchContext));
    }

    public void selectZipAndDeploy(String processName, ProcessType processType, File packageFile) {
        selectZipFileOption(processType)
                .inputPackageFile(packageFile)
                .enterProcessName(processName)
                .submit();
    }

    public void enterEtlProcessNameAndDeploy(String processName) {
        enterProcessName(processName)
                .submit();
    }

    public void enterS3RegionAndEnableEncryptAndDeploy(String processName, String s3Region) {
        enterProcessName(processName)
                .enterS3Region(s3Region)
                .selectServerSideEncryption()
                .submit();
    }

    public void removeS3RegionAndDisableEncryptAndDeploy(String processName) {
        enterProcessName(processName)
                .enterS3Region("")
                .selectServerSideEncryption()
                .submit();
    }

    public void deployProcessWithZipFile(String processName, ProcessType processType, File packageFile) {
        selectProcessType(processType);
        selectZipAndDeploy(processName, processType, packageFile);
    }

    public void deployProcessWithGitStorePath(String processName, String gitStorePath) {
        selectProcessType(ProcessType.RUBY_SCRIPTS)
                .selectGitOption()
                .enterGitPath(gitStorePath)
                .enterProcessName(processName)
                .submit();
    }

    public void deployEtlProcess(String processName,
                                 ProcessType processType,
                                 String s3ConfigurationPath,
                                 String s3AccessKey,
                                 String s3SecretKey,
                                 String s3Region,
                                 boolean serverSideEncryption) {
        selectProcessType(processType)
                .enterProcessName(processName)
                .enterS3ConfigurationPath(s3ConfigurationPath)
                .enterS3AccessKey(s3AccessKey)
                .enterS3SecretKey(s3SecretKey);
        if (StringUtils.isNotEmpty(s3Region)) {
            enterS3Region(s3Region);
        }
        if (serverSideEncryption) {
            selectServerSideEncryption();
        }
        submit();
    }

    public void deploySqlExecutorProcess(String processName) {
        selectProcessType(ProcessType.SQL_EXECUTOR)
                .enterProcessName(processName)
                .submit();
    }

    public DeployProcessForm selectProcessType(ProcessType processType) {
        getProcessTypeDropdown()
                .expand()
                .selectProcessType(processType.getTitle());
        return this;
    }

    public DeployProcessForm scrollToSelectProcessType(ProcessType processType, int range) {
        getProcessTypeDropdown()
                .expand()
                .selectLatestProcessTypeVersion(processType.getTitle(), range);
        return this;
    }

    public DeployProcessForm selectGitOption() {
        waitForElementVisible(gitOption).click();
        return this;
    }

    public DeployProcessForm inputPackageFile(File packageFile) {
        waitForElementPresent(packageInput);
        // TODO: https://github.com/mozilla/geckodriver/issues/1173
        // Workaround for sendKeys() with input element with opacity=0
        BrowserUtils.runScript(browser, "arguments[0].style.opacity=1;", packageInput);
        // now we have to lose focus from the input field
        waitForElementVisible(By.cssSelector(".form-title"), getRoot()).click();
        waitForElementVisible(packageInput).sendKeys(packageFile.getAbsolutePath());
        return this;
    }

    public DeployProcessForm enterGitPath(String gitPath) {
        waitForElementVisible(gitPathInput).clear();
        gitPathInput.sendKeys(gitPath);
        return this;
    }

    public DeployProcessForm enterProcessName(String name) {
        waitForElementVisible(processNameInput).clear();
        processNameInput.sendKeys(name);
        return this;
    }

    public DeployProcessForm enterS3ConfigurationPath(String s3ConfigurationPath) {
        waitForElementVisible(s3ConfigurationPathInput).clear();
        s3ConfigurationPathInput.sendKeys(s3ConfigurationPath);
        return this;
    }

    public DeployProcessForm enterS3AccessKey(String s3AccessKey) {
        waitForElementVisible(s3AccessKeyInput).clear();
        s3AccessKeyInput.sendKeys(s3AccessKey);
        return this;
    }

    public DeployProcessForm enterS3SecretKey(String s3SecretKey) {
        waitForElementVisible(s3SecretKeyInput).clear();
        s3SecretKeyInput.sendKeys(s3SecretKey);
        return this;
    }

    public DeployProcessForm expandAdditionalParamsArea() {
        if (!isAdditionalParamsExpand()) {
            expandAdditionalParamsButton.click();
        }
        return this;
    }

    public DeployProcessForm enterS3Region(String s3Region) {
        expandAdditionalParamsArea();
        waitForElementVisible(s3RegionInput).clear();
        s3RegionInput.sendKeys(s3Region);
        return this;
    }

    public DeployProcessForm selectServerSideEncryption() {
        expandAdditionalParamsArea();
        waitForElementVisible(s3ServerSideEncryptionInput).click();
        return this;
    }

    public DeploySDDProcessDialog selectADDProcess() {
        selectProcessType(ProcessType.AUTOMATED_DATA_DISTRIBUTION);
        return DeploySDDProcessDialog.getInstance(browser);
    }

    public DataSourceDialog addNewDatasource() {
        waitForElementVisible(btnCreateDataSource).click();
        return DataSourceDialog.getInstance(browser);
    }

    public DataSourceDialog editDatasource() {
        waitForElementVisible(btnEditDatasource).click();
        return DataSourceDialog.getInstance(browser);
    }

    public String getS3ConfigurationPath() {
        return waitForElementVisible(s3ConfigurationPathInput).getAttribute("value");
    }

    public String getS3AccessKey() {
        return waitForElementVisible(s3AccessKeyInput).getAttribute("value");
    }

    public String getS3Region() {
        expandAdditionalParamsArea();
        return waitForElementVisible(s3RegionInput).getAttribute("value");
    }

    public String getRedirectedPageFromLearnMore() {
        return waitForElementVisible(learnMore.findElement(By.className("action-important-link")))
                .getAttribute("href");
    }

    public boolean isAdditionalParamsExpand() {
        return waitForElementVisible(expandAdditionalParamsButton).getAttribute("class").contains("icon-navigateup");
    }

    public boolean isEnableServerSideEncryption() {
        expandAdditionalParamsArea();
        return waitForElementVisible(s3ServerSideEncryptionInput).isSelected();
    }

    public boolean isPackageInputError() {
        return waitForElementVisible(By.cssSelector(".select-zip .input-text"), getRoot())
                .getAttribute("class").contains("has-error");
    }

    public boolean isProcessNameInputError() {
        return waitForElementVisible(processNameInput).getAttribute("class").contains("has-error");
    }

    public boolean isGitPathInputError() {
        return waitForElementVisible(gitPathInput).getAttribute("class").contains("has-error");
    }

    public boolean isS3ConfigurationPathError() {
        return waitForElementVisible(s3ConfigurationPathInput).getAttribute("class").contains("has-error");
    }

    public boolean isS3AccessKeyError() {
        return waitForElementVisible(s3AccessKeyInput).getAttribute("class").contains("has-error");
    }

    public boolean isS3SecretKeyError() {
        return waitForElementVisible(s3SecretKeyInput).getAttribute("class").contains("has-error");
    }

    public boolean isSubmitButtonDisabled() {
        return isElementDisabled(deployButton);
    }

    public void clickSubmitButton() {
        waitForElementEnabled(deployButton).click();
    }

    public void submit() {
        clickSubmitButton();
        waitForFragmentNotVisible(this);
    }

    public DeployProcessForm clickSwitchToDataSourceLink() {
        waitForElementVisible(switchToDatasourceLink).click();
        return this;
    }

    public DeploySDDProcessDialog clickAddDatasource() {
        waitForElementVisible(addDatasourceButton).click();
        return DeploySDDProcessDialog.getInstance(browser);
    }

    private ProcessTypeDropdown getProcessTypeDropdown() {
        return waitForFragmentVisible(processTypeDropdown);
    }

    private DeployProcessForm selectZipFileOption(ProcessType processType) {
        if (processType == ProcessType.RUBY_SCRIPTS) {
            waitForElementVisible(zipFileOption).click();
        }
        return this;
    }

    public enum PackageFile {

        BASIC("Basic.zip"),
        ONE_GRAPH("One_Graph.zip"),
        RUBY("ruby.zip"),
        CTL_EVENT("CTL_event.zip"),
        ADS_TABLE("adsTable.zip");

        private String name;

        private PackageFile(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public File loadFile() {
            return getResourceAsFile("/" + ZIP_FILES + "/" + getName());
        }
    }

    public enum ProcessType {
        CLOUD_CONNECT("GRAPH", "CloudConnect Graph"),
        RUBY_SCRIPTS("RUBY", "Generic Ruby"),
        CSV_DOWNLOADER("gdc-etl-csv-downloader", "CSV Downloader"),
        SQL_DOWNLOADER("gdc-etl-sql-downloader", "SQL Downloader"),
        GOOGLE_ANALYTICS_DOWNLOADER("gdc-etl-ga-downloader", "Google Analytics Downloader"),
        SALESFORCE_DOWNLOADER("gdc-etl-salesforce-downloader", "Salesforce Downloader"),
        ADS_INTEGRATOR("gdc-etl-ads-integrator", "ADS Integrator"),
        SQL_EXECUTOR("gdc-etl-sql-executor", "SQL Executor"),
        AUTOMATED_DATA_DISTRIBUTION("gdc-data-distribution", "Automated Data Distribution"),
        INVALID_PROCESS_TYPE("invalid-process-type", "Invalid Process Type"),
        LCM_RELEASE("lcm-release", "Release"),
        LCM_ROLLOUT("lcm-rollout", "Rollout"),
        LCM_RPOVISIONING("lcm-rollout", "Workspace Provisioning");

        private String value;
        private String title;

        private ProcessType(String value, String title) {
            this.value = value;
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public String getTitle() {
            return title;
        }
    }
}
