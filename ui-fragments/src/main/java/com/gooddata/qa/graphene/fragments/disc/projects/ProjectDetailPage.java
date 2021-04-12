package com.gooddata.qa.graphene.fragments.disc.projects;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.process.DataloadProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;

public class ProjectDetailPage extends AbstractFragment {

    @FindBy (className = "logical-data-model")
    private WebElement logicalDataModel;

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
    private WebElement goToAnalyzeLink;

    @FindBy(css = ".empty-state .title")
    private WebElement emptyStateTitle;

    @FindBy(css = ".guide-deploy")
    private WebElement guideDeployMessage;

    @FindBy(className = "process-detail")
    private Collection<WebElement> processes;

    public String getTitle() {
        return waitForElementVisible(title).getText();
    }

    public String getProjectIdMetadata() {
        return waitForElementVisible(projectIdMetadata).getText();
    }

    public LogicalDataModelPage goToModelerPage() {
        waitForElementVisible(logicalDataModel).click();
        return LogicalDataModelPage.getInstance(browser);
    }

    public AnalysisPage goToAnalyze() {
        waitForElementVisible(goToAnalyzeLink).click();
        return Graphene.createPageFragment(AnalysisPage.class, waitForElementVisible(className("adi-editor"), browser));
    }

    public String getEmptyStateTitle() {
        return waitForElementVisible(emptyStateTitle).getText();
    }

    public String getGuideDeployMessage() {
        return waitForElementVisible(guideDeployMessage).getText();
    }

    public ProcessDetail getProcess(String processName) {
        if (processName.equals(DATALOAD_PROCESS_NAME)) {
            throw new RuntimeException("The type of DATALOAD process should be called in getDataloadProcess method");
        }

        return findProcess(Restriction.NAME, processName)
                .map(p -> Graphene.createPageFragment(ProcessDetail.class, p)).get();
    }

    public DataloadProcessDetail getDataDistributonProcess(String processName) {
        if (processName.equals(DATALOAD_PROCESS_NAME)) {
            throw new RuntimeException("The type of DATALOAD process should be called in getDataloadProcess method");
        }

        return findProcess(Restriction.NAME, processName)
                .map(p -> Graphene.createPageFragment(DataloadProcessDetail.class, p)).get();
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

    public boolean isDeployProcessFormVisible() {
        return isElementVisible(DeployProcessForm.LOCATOR, browser);
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

    public void deployEtlProcess(String processName,
                                 ProcessType processType,
                                 String s3ConfigurationPath,
                                 String s3AccessKey,
                                 String s3SecretKey,
                                 String s3Region,
                                 boolean serverSideEncryption) {
        sleepTightInSeconds(3); // This case doesn't have any other option for waiting time.
        clickDeployButton().deployEtlProcess(processName, processType, s3ConfigurationPath, s3AccessKey, s3SecretKey,
                s3Region, serverSideEncryption);
    }

    public void deploySqlExecutorProcess(String processName) {
        clickDeployButton().deploySqlExecutorProcess(processName);
    }

    public void downloadProcess(String processName) {
        getProcess(processName).downloadProcess();
    }

    public ProjectDetailPage deleteProcess(String processName) {
        int processNumber = processes.size();
        getProcess(processName).deleteProcess();

        Function<WebDriver, Boolean> processDeleted = browser -> processes.size() == processNumber - 1;
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

    public DataloadProcessDetail getDataProcessName(String processName) {
        return findProcess(Restriction.NAME, processName)
                .map(p -> Graphene.createPageFragment(DataloadProcessDetail.class, p)).get();
    }

    private enum Restriction {
        ID, NAME
    }
}
