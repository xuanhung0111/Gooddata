package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.DISCOverviewProjectStates;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DISCOverviewProjects extends AbstractFragment {

    @FindBy(css = ".ait-overview-projects-empty-state")
    private WebElement overviewEmptyState;

    @FindBy(css = ".ait-overview-project-list-item")
    protected List<WebElement> overviewProjects;

    @FindBy(css = ".project-detail-item")
    private List<WebElement> projectDetailItems;

    @FindBy(css = ".overview-schedule-list .overview-schedule-item")
    private List<WebElement> overviewScheduleItems;

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
    
    private String XPATH_OVERVIEW_ERROR_SCHEDULE_TITLE = "//div[@class='overview-project-error-cell-schedule-name']/a[@href='${scheduleUrl}']";
    private String XPATH_OVERVIEW_SCHEDULE_TITLE = "//div[@class='overview-project-cell-schedule-name']/a[@href='${scheduleUrl}']";

    private By BY_OVERVIEW_PROJECT_CHECKBOX = By
            .cssSelector(".overview-project-cell-checkbox input");
    private By BY_OVERVIEW_PROJECT_EXPAND_BUTTON = By
            .cssSelector(".overview-project-cell-expand-control .action-icon");
    private By BY_OVERVIEW_PROJECT_TITLE_LINK = By
            .cssSelector(".ait-overview-project-list-item-title");
    private By BY_OVERVIEW_PROJECT_ERROR_MESSAGE = By
            .cssSelector(".overview-project-error-cell-message");
    private By BY_OVERVIEW_PROJECT_LOG = By.cssSelector(".ait-execution-history-item-log");
    private By BY_OVERVIEW_PROJECT_RUNTIME = By.cssSelector(".overview-project-cell-runtime");
    private By BY_OVERVIEW_PROJECT_DATE = By.cssSelector(".overview-project-cell-date");
    private By BY_OVERVIEW_PROJECT_OK_INFO = By.cssSelector(".overview-project-ok-cell-info");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_CHECKBOX = By
            .cssSelector(".overview-schedule-item .ember-checkbox");
    private By BY_OVERVIEW_PROJECT_ERROR_SCHEDULE_LINK = By
            .cssSelector(".overview-project-error-cell-schedule-name a");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_LINK = By
            .cssSelector(".overview-project-cell-schedule-name a");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_NAME = By.cssSelector(".title");
    private By BY_OVERVIEW_PROJECT_SCHEDULE_ITEM = By
            .cssSelector(".overview-schedule-list .overview-schedule-item");
    private By BY_OVERVIEW_PROCESS_TITLE = By.cssSelector(".process-title");

    private String getOverviewEmptyStateMessage() {
        return waitForElementVisible(overviewEmptyState).getText();
    }

    private void assertProjectInfoWithoutAdminRole(String projectState, String projectName,
            String projectId, Map<String, String> processes,
            Map<List<String>, List<String>> expectedSchedules) throws InterruptedException {
        WebElement overviewProjectDetail = getOverviewProjectDetail(projectName, projectId, false);
        assertTrue(overviewProjectDetail != null);
        try {
            waitForElementVisible(overviewProjectDetail
                    .findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON));
        } catch (NoSuchElementException ex) {
            System.out.println("Project expand icon is not displayed!");
        }
        if (expectedSchedules.size() == 1) {
            if (!projectState.equals(DISCOverviewProjectStates.SCHEDULED.getOption())
                    && !projectState.equals(DISCOverviewProjectStates.STOPPED.getOption())) {
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_LOG).isEnabled());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME)
                        .getText().isEmpty());
                System.out.println("Project schedule runtime: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText()
                        .isEmpty());
                System.out.println("Project schedule date: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText());
            }
            if (projectState.equals(DISCOverviewProjectStates.FAILED.getOption())
                    || projectState.equals(DISCOverviewProjectStates.STOPPED.getOption())) {
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().isEmpty());
                System.out.println("Overview project error message: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                .getText());
            }
            if (projectState.equals(DISCOverviewProjectStates.SUCCESSFUL.getOption())) {
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO)
                        .getText().isEmpty());
                System.out.println("Overview project ok info: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText());
            }
        } else if (expectedSchedules.size() > 1) {
            if (!projectState.equals(DISCOverviewProjectStates.SCHEDULED.getOption())) {
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                        .isEmpty());
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText()
                        .isEmpty());
            }
            if (projectState.equals(DISCOverviewProjectStates.FAILED.getOption())) {
                String errorMessage = String.format("%d schedules", expectedSchedules.size());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().isEmpty());
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().equals(errorMessage));
                System.out.println("Overview project error message: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                .getText());
            }
            if (projectState.equals(DISCOverviewProjectStates.SUCCESSFUL.getOption())) {
                String okInfo = String.format("%d schedules", expectedSchedules.size());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO)
                        .getText().isEmpty());
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText()
                        .equals(okInfo));
                System.out.println("Overview project ok info: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText());
            }
        }
    }

    private void assertOverviewSchedules(String state, String projectId, String processName,
            List<WebElement> overviewSchedules, Map<List<String>, List<String>> expectedSchedules) {
        for (WebElement overviewSchedule : overviewSchedules) {
            By scheduleLink = BY_OVERVIEW_PROJECT_SCHEDULE_LINK;
            assertTrue(expectedSchedules.containsKey(Arrays.asList(processName, overviewSchedule
                    .findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME).getText())));
            List<String> expectedScheduleDetails =
                    expectedSchedules.get(Arrays.asList(processName,
                            overviewSchedule.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME)
                                    .getText()));
            if (!state.equals(DISCOverviewProjectStates.SCHEDULED.getOption())
                    && !state.equals(DISCOverviewProjectStates.STOPPED.getOption())) {
                assertTrue(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_LOG).isEnabled());
                assertFalse(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                        .isEmpty());
                System.out.println("Project schedule runtime: "
                        + overviewSchedule.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText());
                assertFalse(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_DATE).getText()
                        .isEmpty());
                System.out.println("Project schedule date: "
                        + overviewSchedule.findElement(BY_OVERVIEW_PROJECT_DATE).getText());
            }
            if (state.equals(DISCOverviewProjectStates.FAILED.getOption())
                    || state.equals(DISCOverviewProjectStates.STOPPED.getOption())) {
                scheduleLink = BY_OVERVIEW_PROJECT_ERROR_SCHEDULE_LINK;
                assertFalse(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().isEmpty());
                System.out
                        .println("Overview schedule error message: "
                                + overviewSchedule.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                        .getText());
                assertEquals(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText(), expectedScheduleDetails.get(4));
                if (expectedScheduleDetails.get(1) != null)
                    assertEquals(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_RUNTIME)
                            .getText(), expectedScheduleDetails.get(1));
            }
            if (state.equals(DISCOverviewProjectStates.SUCCESSFUL.getOption())) {
                assertTrue(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText()
                        .isEmpty());
                System.out.println("Overview schedule ok info: "
                        + overviewSchedule.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText());
                assertTrue(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                        .equals(expectedScheduleDetails.get(1)));
            }
            assertEquals(overviewSchedule.findElement(scheduleLink).getAttribute("href"),
                    expectedScheduleDetails.get(0));
        }
    }

    private void runNowProject() {
        waitForElementVisible(runNowButton);
        assertTrue(runNowButton.isEnabled());
        runNowButton.click();
        waitForElementVisible(runNowDialog);
        waitForElementVisible(positiveButton).click();
        waitForElementNotPresent(runNowDialog);
    }

    private void disableProject() {
        waitForElementVisible(disableButton);
        assertTrue(disableButton.isEnabled());
        disableButton.click();
        waitForElementVisible(disableDialog);
        waitForElementVisible(negativeButton).click();
        waitForElementNotPresent(disableDialog);
    }

    private void stopProject() {
        waitForElementVisible(stopButton);
        assertTrue(stopButton.isEnabled());
        stopButton.click();
        waitForElementVisible(stopDialog);
        waitForElementVisible(negativeButton).click();
        waitForElementNotPresent(stopDialog);
    }

    public WebElement getOverviewProjectDetail(String projectName, String projectId, boolean isAdmin)
            throws InterruptedException {
        for (int i = 0; i < 5 && overviewProjects.isEmpty(); i++) {
            System.out.println("Wait for Overview projects list loaded!");
            Thread.sleep(3000);
        }
        for (WebElement overviewProject : overviewProjects) {
            String overviewProjectName =
                    overviewProject.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK).getText();
            if (!overviewProject.getAttribute("class").contains("non-admin")) {
                String overviewProjectId =
                        overviewProject
                                .findElement(BY_OVERVIEW_PROJECT_TITLE_LINK)
                                .getAttribute("href")
                                .substring(
                                        overviewProject.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK)
                                                .getAttribute("href").lastIndexOf("/") + 1);
                if (overviewProjectName.equals(projectName) && overviewProjectId.equals(projectId))
                    return overviewProject;
            } else {
                if (overviewProjectName.equals(projectName))
                    return overviewProject;
            }
        }
        return null;
    }

    public void assertOverviewEmptyState(DISCOverviewProjectStates state) {
        assertEquals(getOverviewEmptyStateMessage(), state.getOverviewEmptyState());
    }

    public void assertOverviewProject(String projectState, String projectName, String projectId,
            Map<String, String> processes, Map<List<String>, List<String>> expectedSchedules)
            throws InterruptedException {
        WebElement overviewProjectDetail = getOverviewProjectDetail(projectName, projectId, true);
        assertNotNull(overviewProjectDetail);
        assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).isEnabled());
        if (overviewProjectDetail.getAttribute("class").contains("expanded-border"))
            overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        if (expectedSchedules.size() == 1) {
            if (!projectState.equals(DISCOverviewProjectStates.SCHEDULED.getOption())
                    && !projectState.equals(DISCOverviewProjectStates.STOPPED.getOption())) {
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_LOG).isEnabled());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME)
                        .getText().isEmpty());
                System.out.println("Project schedule runtime: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText()
                        .isEmpty());
                System.out.println("Project schedule date: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText());
            }
            if (projectState.equals(DISCOverviewProjectStates.FAILED.getOption())
                    || projectState.equals(DISCOverviewProjectStates.STOPPED.getOption())) {
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().isEmpty());
                System.out.println("Overview project error message: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                .getText());
            }
            if (projectState.equals(DISCOverviewProjectStates.SUCCESSFUL.getOption())) {
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO)
                        .getText().isEmpty());
                System.out.println("Overview project ok info: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText());
            }
        } else if (expectedSchedules.size() > 1) {
            if (!projectState.equals(DISCOverviewProjectStates.SCHEDULED.getOption())) {
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                        .isEmpty());
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText()
                        .isEmpty());
            }
            if (projectState.equals(DISCOverviewProjectStates.FAILED.getOption())) {
                String errorMessage = String.format("%d schedules", expectedSchedules.size());
                System.out.println("errorGraph" + errorMessage);
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().isEmpty());
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText().equals(errorMessage));
                System.out.println("Overview project error message: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                .getText());
            }
            if (projectState.equals(DISCOverviewProjectStates.SUCCESSFUL.getOption())) {
                String okInfo = String.format("%d schedules", expectedSchedules.size());
                assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO)
                        .getText().isEmpty());
                assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText()
                        .equals(okInfo));
                System.out.println("Overview project ok info: "
                        + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText());
            }
        }
        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        for (int i = 0; i < 5 && projectDetailItems.size() == 0; i++) {
            Thread.sleep(3000);
        }
        assertEquals(processes.size(), projectDetailItems.size());
        int index = 0;
        for (Entry<String, String> process : processes.entrySet()) {
            assertTrue(process.getKey().equalsIgnoreCase(
                    projectDetailItems.get(index).findElement(BY_OVERVIEW_PROCESS_TITLE).getText()));
            String processDetailUrl =
                    process.getValue().substring(0, process.getValue().lastIndexOf("/"));
            System.out.println("processDetailUrl: " + processDetailUrl);
            assertEquals(processDetailUrl,
                    projectDetailItems.get(index).findElement(BY_OVERVIEW_PROCESS_TITLE)
                            .getAttribute("href"));
            assertOverviewSchedules(projectState, projectId, process.getKey(), projectDetailItems
                    .get(index).findElements(BY_OVERVIEW_PROJECT_SCHEDULE_ITEM), expectedSchedules);
            index++;
        }
    }

    public void checkAllProjects() throws InterruptedException {
        waitForElementVisible(checkAllProjects).click();
        for (int i = 0; i < 5 && !disableButton.isEnabled(); i++) {
            Thread.sleep(1000);
        }
    }

    public void checkOnSelectedProjects(Map<String, String> projectsMap)
            throws InterruptedException {
        WebElement overviewProjectItem;
        for (Entry<String, String> project : projectsMap.entrySet()) {
            overviewProjectItem =
                    getOverviewProjectDetail(project.getKey(), project.getValue(), true);
            assertNotNull(overviewProjectItem);
            if (!overviewProjectItem.getAttribute("class").contains("checked-item"))
                overviewProjectItem.findElement(BY_OVERVIEW_PROJECT_CHECKBOX).click();
        }
    }

    public void checkOnSelectedSchedules(String projectName, String projectId,
            Map<String, List<String>> selectedSchedules) throws InterruptedException {
        WebElement overviewProjectDetail = getOverviewProjectDetail(projectName, projectId, true);
        if (!overviewProjectDetail.getAttribute("class").contains("expanded-border"))
            overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        for (int i = 0; i < 5 && projectDetailItems.size() == 0; i++) {
            Thread.sleep(3000);
        }
        for (int i = 0; i < projectDetailItems.size(); i++) {
            if (selectedSchedules.containsKey(projectDetailItems.get(i)
                    .findElement(BY_OVERVIEW_PROCESS_TITLE).getText())) {
                System.out.println("Selected process: "
                        + projectDetailItems.get(i).findElement(BY_OVERVIEW_PROCESS_TITLE)
                                .getText());
                List<WebElement> overviewSchedules =
                        projectDetailItems.get(i).findElements(BY_OVERVIEW_PROJECT_SCHEDULE_ITEM);
                List<String> selectedSchedulesOfProcess =
                        selectedSchedules.get(projectDetailItems.get(i)
                                .findElement(BY_OVERVIEW_PROCESS_TITLE).getText());
                for (WebElement overviewSchedule : overviewSchedules) {
                    System.out.println("Selected schedule: "
                            + overviewSchedule.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME)
                                    .getText());
                    if (selectedSchedulesOfProcess.contains(overviewSchedule.findElement(
                            BY_OVERVIEW_PROJECT_SCHEDULE_NAME).getText())) {
                        overviewSchedule.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_CHECKBOX).click();
                    }
                }
            }
        }
    }

    public void bulkAction(DISCOverviewProjectStates state, boolean disable) {
        if (disable)
            disableProject();
        else {
            switch (state) {
                case FAILED:
                    runNowProject();
                    break;
                case RUNNING:
                    stopProject();
                    break;
                case SCHEDULED:
                    stopProject();
                    break;
                case SUCCESSFUL:
                    runNowProject();
                    break;
            }
        }

    }

    public void checkProjectNotAdmin(String projectState, String projectName, String projectId,
            Map<String, String> processes, Map<List<String>, List<String>> expectedSchedules)
            throws InterruptedException {
        WebElement overviewProject = getOverviewProjectDetail(projectName, null, false);
        assertNotNull(overviewProject);
        try {
            overviewProject.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK).click();
            waitForElementVisible(projectDetail);
        } catch (NoSuchElementException ex) {
            System.out.println("Non-admin user cannot access project detail page!");
        }
        waitForElementVisible(getRoot());
        assertProjectInfoWithoutAdminRole(projectState, projectName, projectId, processes,
                expectedSchedules);
    }

    public int getOverviewProjectNumber() throws InterruptedException {
        for (int i = 0; i < 10 && overviewProjects.size() == 0; i++) {
            Thread.sleep(1000);
        }
        return overviewProjects.size();
    }
    
    public WebElement getOverviewScheduleName(DISCOverviewProjectStates overviewState, String projectName, String projectId, boolean isAdmin, String scheduleUrl) throws InterruptedException {
        WebElement overviewProject = getOverviewProjectDetail(projectName, projectId, isAdmin);
        overviewProject.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        String xpathScheduleTitle = overviewState.equals(DISCOverviewProjectStates.FAILED) ? XPATH_OVERVIEW_ERROR_SCHEDULE_TITLE : XPATH_OVERVIEW_SCHEDULE_TITLE;
        return overviewProject.findElement(By.xpath(xpathScheduleTitle.replace("${scheduleUrl}", scheduleUrl)));
    }
    
    public void assertOverviewScheduleName(DISCOverviewProjectStates overviewState, String projectName, String projectId, boolean isAdmin, String scheduleUrl, String scheduleName) throws InterruptedException {
        String overviewScheduleLink = scheduleUrl.substring(scheduleUrl.indexOf("#"));
        String overviewScheduleName = getOverviewScheduleName(overviewState, projectName, projectId, isAdmin, overviewScheduleLink).findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME).getText();
        assertEquals(overviewScheduleName, scheduleName);
    }
}
