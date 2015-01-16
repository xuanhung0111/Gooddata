package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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

    public void assertOverviewProject(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProjectDetail =
                getOverviewProjectWithAdminRole(expectedOverviewProject.getProjectInfo());
        assertNotNull(overviewProjectDetail);
        assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).isEnabled());
        if (overviewProjectDetail.getAttribute("class").contains("expanded-border"))
            overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        int projectScheduleNumber = expectedOverviewProject.getProjectScheduleNumber();
        if (projectScheduleNumber == 1) {
            System.out.println("Overview Schedule Number: " + projectScheduleNumber);
            OverviewSchedule overviewSchedule =
                    expectedOverviewProject.getOverviewProcesses().get(0).getOverviewSchedules()
                            .get(0);
            assertProjectInfoWithOnlyOneSchedule(projectState, overviewProjectDetail,
                    overviewSchedule);
        } else if (projectScheduleNumber > 1) {
            System.out.println("Overview Schedule Number: " + projectScheduleNumber);
            assertProjectInfoWithMultipleSchedule(projectState, overviewProjectDetail,
                    projectScheduleNumber);
        }
        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        waitForCollectionIsNotEmpty(overviewProcesses);
        assertOverviewProcesses(projectState, expectedOverviewProject.getOverviewProcesses());
    }

    public void assertOverviewEmptyState(OverviewProjectStates state) {
        assertEquals(getOverviewEmptyStateMessage(), state.getOverviewEmptyState());
    }

    public void checkProjectNotAdmin(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProject =
                getOverviewProjectWithoutAdminRole(expectedOverviewProject.getProjectName());
        assertNotNull(overviewProject);
        try {
            overviewProject.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK).click();
            Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS).until().element(projectDetail)
                    .is().visible();
        } catch (NoSuchElementException ex) {
            System.out.println("Non-admin user cannot access project detail page!");
        }
        waitForElementVisible(getRoot());
        assertOverviewProjectWithoutAdminRole(projectState, expectedOverviewProject);
    }

    public WebElement getOverviewProjectWithAdminRole(final ProjectInfo projectInfo) {
        waitForCollectionIsNotEmpty(overviewProjects);
        WebElement overviewProject = Iterables.find(overviewProjects, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement project) {
                waitForElementVisible(project);
                WebElement projectTitle = project.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK);
                return project.findElement(BY_OVERVIEW_PROJECT_CHECKBOX).isEnabled()
                        && projectTitle.getText().equals(projectInfo.getProjectName())
                        && projectTitle.getAttribute("href").contains(projectInfo.getProjectId());
            }
        }, null);
        return overviewProject;
    }

    public void checkAllProjects() {
        waitForElementVisible(checkAllProjects).click();
        Graphene.waitGui().until().element(disableButton).is().enabled();
    }

    public void checkOnSelectedProjects(OverviewProjectDetails selectedProject) {
        WebElement overviewProject =
                getOverviewProjectWithAdminRole(selectedProject.getProjectInfo());
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
            WebElement overviewProcess =
                    Iterables.find(overviewProcesses, new Predicate<WebElement>() {

                        @Override
                        public boolean apply(WebElement process) {
                            return selectedProcess.getProcessName().equalsIgnoreCase(
                                    process.findElement(BY_OVERVIEW_PROCESS_TITLE).getText());
                        }
                    });
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
        waitForElementVisible(disableButton);
        assertTrue(disableButton.isEnabled());
        disableButton.click();
        waitForElementVisible(disableDialog);
        waitForElementVisible(negativeButton).click();
        waitForElementNotPresent(disableDialog);
    }

    public int getOverviewProjectNumber() {
        waitForCollectionIsNotEmpty(overviewProjects);
        return overviewProjects.size();
    }

    public void assertOverviewScheduleName(OverviewProjectStates overviewState,
            ProjectInfo projectInfo, String scheduleUrl, String scheduleName) {
        String overviewScheduleLink = scheduleUrl.substring(scheduleUrl.indexOf("#"));
        String overviewScheduleName =
                getOverviewScheduleName(overviewState, projectInfo, overviewScheduleLink)
                        .findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME).getText();
        assertEquals(overviewScheduleName, scheduleName);
    }

    private void assertProjectInfoWithOnlyOneSchedule(OverviewProjectStates projectState,
            WebElement overviewProjectDetail, OverviewSchedule expectedOverviewSchedule) {
        if (projectState != OverviewProjectStates.SCHEDULED) {
            assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_LOG).isEnabled());
            assertFalse(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                    .isEmpty());
            System.out.println("Project schedule runtime: "
                    + overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText());
            if (projectState != OverviewProjectStates.RUNNING) {
                assertEquals(expectedOverviewSchedule.getLastExecutionRunTime(),
                        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText(),
                        "Incorrect execution runtime!");
                assertEquals(expectedOverviewSchedule.getOverviewExecutionDateTime(),
                        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText(),
                        "Incorrect execution date!");
            } else
                assertTrue(expectedOverviewSchedule.getOverviewExecutionDateTime().contains(
                        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText()));
            if (projectState == OverviewProjectStates.FAILED) {
                assertEquals(expectedOverviewSchedule.getExecutionDescription(),
                        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                .getText(), "Incorrect error message!");
            } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                assertEquals("1 schedule",
                        overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO).getText(),
                        "Incorrect successful info!");
            }
        }
    }

    private void assertProjectInfoWithMultipleSchedule(OverviewProjectStates projectState,
            WebElement overviewProjectDetail, int projectScheduleNumber) {
        if (projectState != OverviewProjectStates.SCHEDULED) {
            assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                    .isEmpty());
            assertTrue(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText()
                    .isEmpty());
            if (projectState == OverviewProjectStates.FAILED) {
                String errorMessage = String.format("%d schedules", projectScheduleNumber);
                assertEquals(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                        .getText(), errorMessage);
            } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                String okInfo = String.format("%d schedules", projectScheduleNumber);
                assertEquals(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO)
                        .getText(), okInfo);
            }
        }
    }

    private void assertOverviewProcesses(OverviewProjectStates projectState,
            List<OverviewProcess> expectedOverviewProcesses) {
        assertEquals(expectedOverviewProcesses.size(), overviewProcesses.size());
        for (final OverviewProcess expectedProcess : expectedOverviewProcesses) {
            WebElement overviewProcess =
                    Iterables.find(overviewProcesses, new Predicate<WebElement>() {

                        @Override
                        public boolean apply(WebElement process) {
                            return expectedProcess.getProcessName().equalsIgnoreCase(
                                    process.findElement(BY_OVERVIEW_PROCESS_TITLE).getText());
                        }
                    });
            assertTrue(expectedProcess.getProcessName().equalsIgnoreCase(
                    overviewProcess.findElement(BY_OVERVIEW_PROCESS_TITLE).getText()));
            String processDetailUrl = expectedProcess.getProcessUrl();
            System.out.println("processDetailUrl: " + processDetailUrl);
            assertEquals(overviewProcess.findElement(BY_OVERVIEW_PROCESS_TITLE)
                    .getAttribute("href"), processDetailUrl);
            assertOverviewSchedules(projectState, expectedProcess.getOverviewSchedules(),
                    overviewProcess.findElements(BY_OVERVIEW_PROJECT_SCHEDULE_ITEM));
        }
    }

    private void assertOverviewSchedules(OverviewProjectStates state,
            List<OverviewSchedule> expectedSchedules, List<WebElement> overviewSchedules) {
        for (final OverviewSchedule expectedSchedule : expectedSchedules) {
            WebElement overviewSchedule =
                    Iterables.find(overviewSchedules, new Predicate<WebElement>() {
                        @Override
                        public boolean apply(WebElement input) {
                            return input.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME).getText()
                                    .equals(expectedSchedule.getScheduleName());
                        }
                    });
            By scheduleLink =
                    state == OverviewProjectStates.FAILED ? BY_OVERVIEW_PROJECT_ERROR_SCHEDULE_LINK
                            : BY_OVERVIEW_PROJECT_SCHEDULE_LINK;
            assertEquals(overviewSchedule.findElement(scheduleLink).getAttribute("href"),
                    expectedSchedule.getScheduleUrl());

            if (state == OverviewProjectStates.SCHEDULED)
                continue;
            assertTrue(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_LOG).isEnabled());
            assertFalse(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText()
                    .isEmpty());
            if (state != OverviewProjectStates.RUNNING) {
                assertEquals(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_DATE).getText(),
                        expectedSchedule.getOverviewExecutionDateTime());
                assertEquals(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText(),
                        expectedSchedule.getLastExecutionRunTime());
                if (state == OverviewProjectStates.FAILED)
                    assertEquals(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                            .getText(), expectedSchedule.getExecutionDescription());
            } else
                assertEquals(overviewSchedule.findElement(BY_OVERVIEW_PROJECT_DATE).getText(),
                        expectedSchedule.getOverviewStartTime());
        }
    }

    private void assertOverviewProjectWithoutAdminRole(OverviewProjectStates projectState,
            OverviewProjectDetails expectedOverviewProject) {
        WebElement overviewProjectDetail =
                getOverviewProjectWithoutAdminRole(expectedOverviewProject.getProjectName());
        assertNotNull(overviewProjectDetail);
        WebElement overviewProjectLogLinkElement =
                overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_LOG);
        int projectScheduleNumber = expectedOverviewProject.getProjectScheduleNumber();
        String overviewProjectRuntime =
                overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_RUNTIME).getText();
        String overviewProjectDate =
                overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_DATE).getText();
        if (projectScheduleNumber == 1) {
            OverviewSchedule expectedOverviewSchedule =
                    expectedOverviewProject.getOverviewProcesses().get(0).getOverviewSchedules()
                            .get(0);
            if (projectState != OverviewProjectStates.SCHEDULED)
                assertTrue(overviewProjectLogLinkElement.getAttribute("class").contains(
                        "action-unavailable-icon"));
            assertProjectInfoWithOnlyOneSchedule(projectState, overviewProjectDetail,
                    expectedOverviewSchedule);
        } else if (projectScheduleNumber > 1) {
            if (projectState != OverviewProjectStates.SCHEDULED) {
                assertTrue(overviewProjectRuntime.isEmpty());
                assertTrue(overviewProjectDate.isEmpty());
                if (projectState == OverviewProjectStates.FAILED) {
                    String errorMessage = String.format("%d schedules", projectScheduleNumber);
                    assertEquals(
                            overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_ERROR_MESSAGE)
                                    .getText(), errorMessage);
                } else if (projectState == OverviewProjectStates.SUCCESSFUL) {
                    String okInfo = String.format("%d schedules", projectScheduleNumber);
                    assertEquals(overviewProjectDetail.findElement(BY_OVERVIEW_PROJECT_OK_INFO)
                            .getText(), okInfo);
                }
            }
        }
    }

    private void checkOnSelectedSchedules(WebElement overviewProcess,
            List<OverviewSchedule> selectedSchedules) {
        for (final OverviewSchedule selectedSchedule : selectedSchedules) {
            Iterables
                    .find(overviewProcess.findElements(BY_OVERVIEW_PROJECT_SCHEDULE_ITEM),
                            new Predicate<WebElement>() {

                                @Override
                                public boolean apply(WebElement schedule) {
                                    return selectedSchedule.getScheduleName().equals(
                                            schedule.findElement(BY_OVERVIEW_PROJECT_SCHEDULE_NAME)
                                                    .getText());
                                }
                            }).findElement(BY_OVERVIEW_PROJECT_SCHEDULE_CHECKBOX).click();
        }
    }

    private WebElement getOverviewProjectWithoutAdminRole(final String projectName) {
        waitForCollectionIsNotEmpty(overviewProjects);
        return Iterables.find(overviewProjects, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement project) {
                waitForElementVisible(project);
                WebElement projectTitle = project.findElement(BY_OVERVIEW_PROJECT_TITLE_LINK);
                return !project.findElement(BY_OVERVIEW_PROJECT_CHECKBOX).isEnabled()
                        && projectTitle.getText().equals(projectName);
            }
        });
    }

    private String getOverviewEmptyStateMessage() {
        return waitForElementVisible(overviewEmptyState).getText();
    }

    private WebElement getOverviewScheduleName(OverviewProjectStates overviewState,
            ProjectInfo projectInfo, String scheduleUrl) {
        WebElement overviewProject = getOverviewProjectWithAdminRole(projectInfo);
        overviewProject.findElement(BY_OVERVIEW_PROJECT_EXPAND_BUTTON).click();
        String xpathScheduleTitle =
                overviewState == OverviewProjectStates.FAILED ? XPATH_OVERVIEW_ERROR_SCHEDULE_TITLE
                        : XPATH_OVERVIEW_SCHEDULE_TITLE;
        return overviewProject.findElement(By.xpath(xpathScheduleTitle.replace("${scheduleUrl}",
                scheduleUrl)));
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
