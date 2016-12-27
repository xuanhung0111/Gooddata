package com.gooddata.qa.graphene.fragments.disc.process;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.ZIP_FILES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsFile;

import java.io.File;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DeployProcessForm extends AbstractFragment {

    private static final By LOCATOR = By.className("ait-process-deploy-fragment");

    @FindBy(css = "input[value='ZIP']")
    private WebElement zipFileOption;

    @FindBy(css = ".select-zip .fileInput")
    private WebElement packageInput;

    @FindBy(tagName = "select")
    private Select processType;

    @FindBy(xpath = ".//*[text()='PROCESS NAME']/following::input")
    private WebElement processNameInput;

    @FindBy(className = "ait-deploy-process-confirm-btn")
    private WebElement deployButton;

    public static final DeployProcessForm getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DeployProcessForm.class, waitForElementVisible(LOCATOR, searchContext));
    }

    public void deployProcessWithZipFile(String processName, ProcessType processType, PackageFile packageFile) {
        selectZipFileOption()
                .inputPackage(packageFile)
                .selectProcessType(processType)
                .enterProcessName(processName)
                .submit();
        waitForFragmentNotVisible(this);
    }

    private DeployProcessForm selectZipFileOption() {
        waitForElementVisible(zipFileOption).click();
        return this;
    }

    private DeployProcessForm inputPackage(PackageFile packageFile) {
        waitForElementPresent(packageInput).sendKeys(packageFile.loadFile().getAbsolutePath());
        return this;
    }

    private DeployProcessForm selectProcessType(ProcessType type) {
        waitForElementVisible(processType).selectByValue(type.getValue());
        return this;
    }

    private DeployProcessForm enterProcessName(String name) {
        waitForElementVisible(processNameInput).clear();
        processNameInput.sendKeys(name);
        return this;
    }

    private void submit() {
        waitForElementVisible(deployButton).click();
    }

    public enum PackageFile {

        BASIC("Basic.zip");

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
        CLOUD_CONNECT("GRAPH"),
        RUBY_SCRIPTS("RUBY");

        private String value;

        private ProcessType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
