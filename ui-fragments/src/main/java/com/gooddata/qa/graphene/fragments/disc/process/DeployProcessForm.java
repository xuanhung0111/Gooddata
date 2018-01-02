package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;

import java.io.File;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DeployProcessForm extends AbstractFragment {

    private static final By LOCATOR = By.className("ait-process-deploy-fragment");

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

    @FindBy(css = "button:first-child")
    private WebElement deployButton;

    @FindBy(className = "ait-component-selection-dropdown-button")
    private ProcessTypeDropdown processTypeDropdown;

    @FindBy(xpath = ".//*[text()='PATH TO CONFIGURATION.JSON']/following::input[1]")
    private WebElement s3ConfigurationPathInput;

    @FindBy(xpath = ".//*[text()='ACCESS KEY FOR S3']/following::input[1]")
    private WebElement s3AccessKeyInput;

    @FindBy(xpath = ".//*[text()='SECRET KEY FOR S3']/following::input[1]")
    private WebElement s3SecretKeyInput;

    @FindBy(xpath = ".//*[text()='Region']/following::input")
    private WebElement s3RegionInput;

    @FindBy(css = ".form-title .input-checkbox")
    private WebElement s3ServerSideEncryptionInput;

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
        waitForFragmentNotVisible(this);
    }

    public void enterEtlProcessNameAndDeploy(String processName) {
        enterProcessName(processName)
                .submit();
        waitForFragmentNotVisible(this);
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
        waitForFragmentNotVisible(this);
    }

    public DeployProcessForm deployEtlProcess(String processName,
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
                .enterS3SecretKey(s3SecretKey)
                .enterS3Region(s3Region);
        if (serverSideEncryption) {
            selectServerSideEncryption();
        }
        submit();
        waitForFragmentNotVisible(this);
        return this;
    }

    public DeployProcessForm deploySqlExecutorProcess(String processName) {
        selectProcessType(ProcessType.SQL_EXECUTOR)
                .enterProcessName(processName)
                .submit();
        waitForFragmentNotVisible(this);
        return this;
    }

    public DeployProcessForm selectProcessType(ProcessType processType) {
        getProcessTypeDropdown()
                .expand()
                .selectProcessType(processType.getTitle());
        return this;
    }

    public DeployProcessForm selectGitOption() {
        waitForElementVisible(gitOption).click();
        return this;
    }

    public DeployProcessForm inputPackageFile(File packageFile) {
        waitForElementPresent(packageInput).sendKeys(packageFile.getAbsolutePath());
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

    public DeployProcessForm enterS3Region(String s3Region) {
        waitForElementVisible(s3RegionInput).clear();
        s3RegionInput.sendKeys(s3Region);
        return this;
    }

    public DeployProcessForm selectServerSideEncryption() {
        waitForElementVisible(s3ServerSideEncryptionInput).click();
        return this;
    }

    public String getS3ConfigurationPath() {
        return waitForElementVisible(s3ConfigurationPathInput).getAttribute("value");
    }

    public String getS3AccessKey() {
        return waitForElementVisible(s3AccessKeyInput).getAttribute("value");
    }

    public String getS3Region() {
        return waitForElementVisible(s3RegionInput).getAttribute("value");
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

    public void submit() {
        waitForElementVisible(deployButton).click();
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
        ADS_INTEGRATOR("gdc-etl-ads-integrator", "ADS Integrator"),
        SQL_EXECUTOR("gdc-etl-sql-executor", "SQL Executor");

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
