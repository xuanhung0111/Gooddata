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
import com.gooddata.qa.graphene.fragments.disc.process.DataloadProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.google.common.base.Predicate;

public class ProjectDetailPage extends AbstractFragment {

    public static final String URI = "admin/disc/#/projects/%s";
    private static final String DATALOAD_PROCESS_NAME = "Automated Data Distribution";

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
    private Collection<WebElement> processes;

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
        if (processName.equals(DATALOAD_PROCESS_NAME)) {
            throw new RuntimeException("The type of DATALOAD process should be called in getDataloadProcess method");
        }

        return findProcess(Restriction.NAME, processName)
                .map(p -> Graphene.createPageFragment(ProcessDetail.class, p)).get();
    }

    public DataloadProcessDetail getDataloadProcess() {
        return findProcess(Restriction.NAME, DATALOAD_PROCESS_NAME)
                .map(p -> Graphene.createPageFragment(DataloadProcessDetail.class, p)).get();
    }

    public ProcessDetail getProcessById(String processId) {
        WebElement process = findProcess(Restriction.ID, processId).get();

        if (getProcessTitle(process).equals(DATALOAD_PROCESS_NAME)) {
            throw new RuntimeException("The type of DATALOAD process should be called in getDataloadProcess method");
        }

        return Graphene.createPageFragment(ProcessDetail.class, process);
    }

    public boolean hasProcess(String processName) {
        return findProcess(Restriction.NAME, processName).isPresent();
    }

    public DeployProcessForm clickDeployButton() {
        waitForElementVisible(deployProcessButton).click();
        return DeployProcessForm.getInstance(browser);
    }

    public ProjectDetailPage deployProcessWithZipFile(String processName, ProcessType processType, File packageFile) {
        clickDeployButton().deployProcessWithZipFile(processName, processType, packageFile);
        return this;
    }

    public ProjectDetailPage deployProcessWithGitStorePath(String processName, String gitStorePath) {
        clickDeployButton().deployProcessWithGitStorePath(processName, gitStorePath);
        return this;
    }

    public void downloadProcess(String processName) {
        getProcess(processName).downloadProcess();
    }

    public ProjectDetailPage deleteProcess(String processName) {
        int processNumber = processes.size();
        getProcess(processName).deleteProcess();

        Predicate<WebDriver> processDeleted = browser -> processes.size() == processNumber - 1;
        Graphene.waitGui().until(processDeleted);
        return this;
    }

    public Collection<String> getProcessNames() {
        return processes.stream().map(this::getProcessTitle).collect(toList());
    }

    public CreateScheduleForm openCreateScheduleForm() {
        waitForElementVisible(newScheduleButton).click();
        return CreateScheduleForm.getInstance(browser);
    }

    private Optional<WebElement> findProcess(Restriction restriction, String value) {
        if (restriction == Restriction.NAME) {
            return processes.stream().filter(p -> value.equals(getProcessTitle(p))).findFirst();
        }
        return processes.stream().filter(p -> isElementPresent(By.id(value), p)).findFirst();
    }

    private String getProcessTitle(WebElement process) {
        return process.findElement(By.className("ait-process-title")).getText();
    }

    private enum Restriction {
        ID, NAME
    }
}
