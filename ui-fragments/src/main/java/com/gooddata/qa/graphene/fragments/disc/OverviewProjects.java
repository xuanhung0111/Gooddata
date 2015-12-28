package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class OverviewProjects extends AbstractFragment {

    @FindBy(css = ".ait-overview-projects-empty-state")
    private WebElement overviewEmptyState;

    @FindBy(css = ".ait-overview-project-list-item")
    private List<WebElement> overviewProjects;

    @FindBy(css = ".project-detail-item")
    private List<WebElement> overviewProcesses;

    @FindBy(css = ".ait-overview-projects-runnow-btn")
    private WebElement runNowButton;

    @FindBy(css = ".ait-overview-projects-disable-btn")
    private WebElement disableButton;

    @FindBy(css = ".ait-overview-projects-stop-btn")
    private WebElement stopButton;

    @FindBy(css = ".buttons-bar .checkbox-button input")
    private WebElement checkAllProjects;

    @FindBy(css = ".bulk-stop-dialog-main")
    private WebElement stopDialog;

    @FindBy(css = ".bulk-disable-dialog-main")
    private WebElement disableDialog;

    @FindBy(css = ".dialog-main")
    private WebElement runNowDialog;

    @FindBy(css = ".button-positive")
    private WebElement positiveButton;

    @FindBy(css = ".button-negative")
    private WebElement negativeButton;

    @FindBy(css = ".ait-project-detail-fragment")
    private WebElement projectDetail;

    private String XPATH_OVERVIEW_ERROR_SCHEDULE_TITLE =
            "//div[@class='overview-project-error-cell-schedule-name']/a[@href='${scheduleUrl}']";
    private String XPATH_OVERVIEW_SCHEDULE_TITLE =
            "//div[@class='overview-project-cell-schedule-name']/a[@href='${scheduleUrl}']";

    private By BY_OVERVIEW_PROJECT_CHECKBOX = By.cssSelector(".overview-project-cell-checkbox input");
    private By BY_OVERVIEW_PROJECT_EXPAND_BUTTON = By
            .cssSelector(".overview-project-cell-expand-control .action-icon");
    private By BY_OVERVIEW_PROJECT_TITLE_LINK = By.cssSelector(".ait-overview-project-list-item-title");
    private By BY_OVERVIEW_PROJECT_ERROR_MESSAGE = By.cssSelector(".overview-project-error-cell-message");
    private By BY_OVERVIEW_PROJECT_LOG = By.cssSelector(".ait-execution-history-item-log");
    private By BY_OVERVIEW_PROJECT_RUNTIME = By.cssSelector(".overview-project-cell-runtime");
    private By BY_OVERVIEW_PROJECT_DATE = By.cssSelector(".overview-project-cell-date");
    private By BY_OVERVIEW_PROJECT_OK_INFO = By.cssSelector(".overview-project-ok-cell-info");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_CHECKBOX = By.cssSelector(".overview-schedule-item .ember-checkbox");
    private By BY_OVERVIEW_PROJECT_ERROR_SCHEDULE_LINK = By
            .cssSelector(".overview-project-error-cell-schedule-name a");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_LINK = By.cssSelector(".overview-project-cell-schedule-name a");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_NAME = By.cssSelector(".title");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_GRAPH_NAME = By.cssSelector(".label");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_ITEM = By
            .cssSelector(".overview-schedule-list .overview-schedule-item");
    private By BY_OVERVIEW_PROCESS_TITLE = By.cssSelector(".process-title");

    public void waitForOverviewProcessesLoaded() {
        waitForCollectionIsNotEmpty(overviewProcesses);
    }

    public WebElement getOverviewProjectExpandButton(WebElement overviewProjectDetail) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_EXPAND_BUTTON, overviewProjectDetail);
    }

    public WebElement getOverviewProjectName(WebElement overviewProject) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_TITLE_LINK, overviewProject);
    }

    public void accessProjectDetailPage(ProjectInfo project) {
        waitForElementVisible(BY_OVERVIEW_PROJECT_TITLE_LINK, getOverviewProjectWithAdminRole(project)).click();
    }

    public WebElement getOverviewProjectWithAdminRole(final ProjectInfo projectInfo) {
        waitForCollectionIsNotEmpty(overviewProjects);
        return overviewProjects.stream()
            .filter(project -> {
                WebElement projectTitle = waitForElementVisible(project)
                        .findElement(BY_OVERVIEW_PROJECT_TITLE_LINK);
                return project.findElement(BY_OVERVIEW_PROJECT_CHECKBOX).isEnabled()
                        && projectTitle.getText().equals(projectInfo.getProjectName())
                        && projectTitle.getAttribute("href").contains(projectInfo.getProjectId());
        })
        .findFirst().orElse(null);
    }

    public void checkAllProjects() {
        waitForElementVisible(checkAllProjects).click();
        Graphene.waitGui().until().element(disableButton).is().enabled();
    }

    public void checkOnSelectedProjects(OverviewProjectDetails selectedProject) {
        WebElement overviewProject = getOverviewProjectWithAdminRole(selectedProject.getProjectInfo());
        assertNotNull(overviewProject);
        overviewProject.findElement(BY_OVERVIEW_PROJECT_CHECKBOX).click();
    }

    public void checkOnOverviewSchedules(OverviewProjectDetails selectedOverviewProject) {
        WebElement overviewProjectDetail =
                getOverviewProjectWithAdminRole(selectedOverviewProject.getProjectInfo());
        assertNotNull(overviewProjectDetail);
        if (!overviewProjectDetail.getAttribute("class").contains("expanded-border"))
            overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        for (final OverviewProcess selectedProcess : selectedOverviewProject.getOverviewProcesses()) {
            WebElement overviewProcess = overviewProcesses.stream().
                    filter(process -> selectedProcess.getProcessName().equalsIgnoreCase(
                            process.findElement(BY_OVERVIEW_PROCESS_TITLE).getText())).
                    findFirst().
                    get();
            checkOnSelectedSchedules(overviewProcess, selectedProcess.getOverviewSchedules());
        }
    }

    public void bulkAction(OverviewProjectStates state) {
        switch (state) {
            case FAILED:
            case SUCCESSFUL:
                runNowProject();
                break;
            case RUNNING:
            case SCHEDULED:
                stopProject();
                break;
            default:
                break;
        }
    }

    public void disableAction() {
        waitForElementVisible(disableButton).click();
        waitForElementVisible(disableDialog);
        waitForElementVisible(negativeButton).click();
        waitForElementNotPresent(disableDialog);
    }

    public int getOverviewProjectNumber() {
        return waitForCollectionIsNotEmpty(overviewProjects).size();
    }

    public String getOverviewScheduleGraphName(WebElement overviewSchedule) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_SCHEDULE_GRAPH_NAME, overviewSchedule).getText();
    }

    public WebElement getOverviewProcessName(WebElement overviewProcess) {
        return waitForElementVisible(BY_OVERVIEW_PROCESS_TITLE, overviewProcess);
    }

    public WebElement getOverviewProcess(OverviewProcess expectedProcess) {
        return overviewProcesses.stream()
                .filter(process -> expectedProcess.getProcessName().equalsIgnoreCase(
                        getOverviewProcessName(process).getText()))
                .findFirst()
                .get();
    }

    public int getProcessNumber() {
        return overviewProcesses.size();
    }

    public List<WebElement> getOverviewSchedules(WebElement overviewProcess) {
        return overviewProcess.findElements(BY_OVERVIEW_PROJECT_SCHEDULE_ITEM);
    }

    public String getOverviewScheduleLink(OverviewProjectStates state, WebElement overviewSchedule) {
        By scheduleLinkLocator =
                state == OverviewProjectStates.FAILED ? BY_OVERVIEW_PROJECT_ERROR_SCHEDULE_LINK
                        : BY_OVERVIEW_PROJECT_SCHEDULE_LINK;
        return waitForElementVisible(scheduleLinkLocator, overviewSchedule).getAttribute("href");
    }

    public String getOverviewScheduleName(WebElement overviewSchedule) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_SCHEDULE_NAME, overviewSchedule).getText();
    }

    public WebElement getOverviewProjectLog(WebElement overviewProjectDetail) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_LOG, overviewProjectDetail);
    }

    public WebElement getOverviewProjectRuntime(WebElement overviewProjectDetail) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_RUNTIME, overviewProjectDetail);
    }

    public WebElement getOverviewProjectDate(WebElement overviewProjectDetail) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_DATE, overviewProjectDetail);
    }

    public WebElement getOverviewProjectErrorMessage(WebElement overviewProjectDetail) {
        return overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE);
    }

    public WebElement getOverviewProjectOKInfo(WebElement overviewProjectDetail) {
        return waitForElementVisible(BY_OVERVIEW_PROJECT_OK_INFO, overviewProjectDetail);
    }

    public WebElement getOverviewProjectWithoutAdminRole(final String projectName) {
        waitForCollectionIsNotEmpty(overviewProjects);
        return overviewProjects.stream()
                .filter(project -> 
                !waitForElementVisible(project).findElement(BY_OVERVIEW_PROJECT_CHECKBOX).isEnabled() &&
                    projectName.equals(project.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK).getText()))
                .findFirst()
                .get();
    }

    public String getOverviewEmptyStateMessage() {
        return waitForElementVisible(overviewEmptyState).getText();
    }

    public WebElement getOverviewScheduleName(OverviewProjectStates overviewState, ProjectInfo projectInfo,
            String scheduleUrl) {
        WebElement overviewProject = getOverviewProjectWithAdminRole(projectInfo);
        overviewProject.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        String xpathScheduleTitle =
                overviewState == OverviewProjectStates.FAILED ? XPATH_OVERVIEW_ERROR_SCHEDULE_TITLE
                        : XPATH_OVERVIEW_SCHEDULE_TITLE;
        return overviewProject.findElement(By.xpath(xpathScheduleTitle.replace("${scheduleUrl}", scheduleUrl)));
    }

    private void checkOnSelectedSchedules(final WebElement overviewProcess, List<OverviewSchedule> selectedSchedules) {
        for (final OverviewSchedule selectedSchedule : selectedSchedules) {
            overviewProcess.findElements(BY_OVERVIEW_PROJECT_SCHEDULE_ITEM).stream()
                .filter(schedule -> selectedSchedule.getScheduleName()
                        .equals(schedule.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME).getText()))
                .map(e -> e.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_CHECKBOX))
                .findFirst()
                .get()
                .click();
        }
    }

    private void stopProject() {
        waitForElementVisible(stopButton);
        assertTrue(stopButton.isEnabled());
        stopButton.click();
        waitForElementVisible(stopDialog);
        waitForElementVisible(negativeButton).click();
        waitForElementNotPresent(stopDialog);
    }

    private void runNowProject() {
        waitForElementVisible(runNowButton);
        assertTrue(runNowButton.isEnabled());
        runNowButton.click();
        waitForElementVisible(runNowDialog);
        waitForElementVisible(positiveButton).click();
        waitForElementNotPresent(runNowDialog);
    }
}
