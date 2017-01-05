package com.gooddata.qa.graphene.fragments.disc.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.google.common.base.Predicate;

public class __ProjectDetailPage extends AbstractFragment {

    @FindBy(className = "ait-project-title")
    private WebElement title;

    @FindBy(className = "s-btn-deploy_process")
    private WebElement deployProcessButton;

    @FindBy(css = "[class*='project-metadata'] .metadata-value")
    private WebElement projectIdMetadata;

    @FindBy(className = "action-link-with-icon")
    private WebElement goToDashboardsLink;

    @FindBy(css = ".empty-state .title")
    private WebElement emptyStateTitle;

    @FindBy(css = ".empty-state .message")
    private WebElement emptyStateMessage;

    @FindBy(className = "process-detail")
    private Collection<ProcessDetail> processes;

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public String getProjectIdMetadata() {
        return waitForElementVisible(projectIdMetadata).getText();
    }

    public void goToDashboards() {
        waitForElementVisible(goToDashboardsLink).click();
    }

    public String getEmptyStateTitle() {
        return waitForElementVisible(emptyStateTitle).getText();
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(emptyStateMessage).getText();
    }

    public ProcessDetail getProcess(String processName) {
        return findProcess(processName).get();
    }

    public boolean hasProcess(String processName) {
        return findProcess(processName).isPresent();
    }

    public DeployProcessForm clickDeployButton() {
        waitForElementVisible(deployProcessButton).click();
        return DeployProcessForm.getInstance(browser);
    }

    public __ProjectDetailPage deployProcessWithZipFile(String processName, ProcessType processType, File packageFile) {
        clickDeployButton().deployProcessWithZipFile(processName, processType, packageFile);
        return this;
    }

    public __ProjectDetailPage deployProcessWithGitStorePath(String processName, String gitStorePath) {
        clickDeployButton().deployProcessWithGitStorePath(processName, gitStorePath);
        return this;
    }

    public void downloadProcess(String processName) {
        getProcess(processName).downloadProcess();
    }

    public __ProjectDetailPage deleteProcess(String processName) {
        int processNumber = processes.size();
        getProcess(processName).deleteProcess();

        Predicate<WebDriver> processDeleted = browser -> processes.size() == processNumber - 1;
        Graphene.waitGui().until(processDeleted);
        return this;
    }

    public Collection<String> getProcessNames() {
        return processes.stream().map(p -> p.getTitle()).collect(toList());
    }

    private Optional<ProcessDetail> findProcess(String processName) {
        return processes.stream().filter(p -> processName.equals(p.getTitle())).findFirst();
    }
}
