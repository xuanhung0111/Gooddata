package com.gooddata.qa.graphene.fragments.disc.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.google.common.base.Predicate;

public class __ProjectDetailPage extends AbstractFragment {

    @FindBy(className = "ait-project-title")
    private WebElement title;

    @FindBy(className = "ait-project-new-schedule-btn")
    private WebElement newScheduleButton;

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
        return findProcess(Restriction.NAME, processName).get();
    }

    public ProcessDetail getProcessById(String processId) {
        return findProcess(Restriction.ID, processId).get();
    }

    public boolean hasProcess(String processName) {
        return findProcess(Restriction.NAME, processName).isPresent();
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

    public CreateScheduleForm openCreateScheduleForm() {
        waitForElementVisible(newScheduleButton).click();
        return CreateScheduleForm.getInstance(browser);
    }

    private Optional<ProcessDetail> findProcess(Restriction restriction, String value) {
        if (restriction == Restriction.NAME) {
            return processes.stream().filter(p -> value.equals(p.getTitle())).findFirst();
        }
        return processes.stream().filter(p -> isElementPresent(By.id(value), p.getRoot())).findFirst();
    }

    private enum Restriction {
        ID, NAME
    }
}
