package com.gooddata.qa.graphene.fragments.disc;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class ProjectDetailPage extends AbstractFragment {

    private static final String PROCESS_METADATA_ID = "Process ID";
    private static final By BY_PROCESS_TITLE = By.cssSelector(".ait-process-title");
    private final static By BY_PROCESS_DELETE_BUTTON = By.cssSelector(".ait-process-delete-btn");
    private final static By BY_PROCESS_DOWNLOAD_BUTTON = By.cssSelector(".ait-process-download-btn");
    private final static By BY_PROCESS_REDEPLOY_BUTTON = By.cssSelector(".ait-process-redeploy-btn");
    private final static By BY_CREATE_NEW_SCHEDULE_LINK = By.cssSelector(".action-important-link");
    private final static By BY_PROCESS_SCHEDULE_TAB = By.cssSelector(".ait-process-schedules-btn");
    private final static By BY_PROCESS_EXECUTABLE_TABLE = By.cssSelector(".ait-process-executable-list");
    private final static By BY_PROCESS_EXECUTABLE_TAB = By.cssSelector(".ait-process-executables-btn");
    private final static By BY_PROCESS_METADATA_TAB = By.cssSelector(".ait-process-metadata-btn");
    private final static By BY_BROKEN_SCHEDULE_MESSAGE = By.cssSelector(".broken-schedules-section .message");
    private final static By BY_PROCESS_METADATA_KEY = By.cssSelector(".ait-process-metadata-key");
    private final static By BY_PROCESS_METADATA_VALUE = By.cssSelector(".ait-process-metadata-value");
    private final static By BY_PROCESS_NOTIFICATION_RULE_BUTTON = By
            .cssSelector(".ait-process-notification-rules-btn");

    private final static By BY_PROJECT_METADATA_KEY = By.cssSelector(".ait-metadata-key");
    private final static By BY_PROJECT_METADATA_VALUE = By.cssSelector(".ait-metadata-value");
    private final static By BY_DEPLOY_ERROR_DIALOG = By.cssSelector(".error_dialog .dialog-body");

    @FindBy(css = ".ait-project-title")
    private WebElement displayedProjectTitle;

    @FindBy(css = ".ait-project-metadata-list-item")
    private List<WebElement> projectMetadataItems;

    @FindBy(css = ".ait-process-metadata-list-item")
    private List<WebElement> processMetadataItems;

    @FindBy(xpath = "//a/span[text()='Go to Dashboards']")
    private WebElement goToDashboardsLink;

    @FindBy(css = ".ait-project-empty-state .title")
    private WebElement projectEmptyStateTitle;

    @FindBy(css = ".ait-project-empty-state .message")
    private WebElement projectEmptyStateMessage;

    @FindBy(css = ".ait-project-deploy-process-btn")
    private WebElement deployProcessButton;


    @FindBy(xpath = "//.[contains(@class, 'ait-process-list-item')]")
    private List<WebElement> processes;

    @FindBy(css = ".ait-process-list-item.active")
    private WebElement activeProcess;

    @FindBy(css = ".ait-process-list-item.active .ait-process-executable-list")
    private ExecutablesTable executablesTable;

    @FindBy(css = ".error_dialog .s-btn-ok")
    private WebElement deployErrorDialogOKButton;

    @FindBy(css = ".ait-project-new-schedule-btn")
    private WebElement newScheduleButton;

    @FindBy(css = ".ait-process-list-item.active")
    private SchedulesTable scheduleTable;

    @FindBy(css = ".ait-process-delete-fragment")
    private WebElement processDeleteDialog;

    @FindBy(css = ".dialog-title")
    private WebElement processDeleteDialogTitle;

    @FindBy(css = ".dialog-body")
    private WebElement processDeleteDialogMessage;

    @FindBy(css = ".ait-process-delete-confirm-btn")
    private WebElement processDeleteConfirmButton;

    @FindBy(css = ".ait-process-delete-cancel-btn")
    private WebElement processDeleteCancelButton;

    public String getDisplayedProjectTitle() {
        return waitForElementVisible(displayedProjectTitle).getText();
    }

    public String getProjectMetadata(final String metadataKey) {
        return Iterables.find(projectMetadataItems, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement projectMetadataItem) {
                return projectMetadataItem.findElement(BY_PROJECT_METADATA_KEY).getText().equals(metadataKey);
            }
        }).findElement(BY_PROJECT_METADATA_VALUE).getText();
    }

    public void goToDashboards() {
        waitForElementVisible(goToDashboardsLink).click();
    }

    public String getProjectEmptyStateTitle() {
        return waitForElementVisible(projectEmptyStateTitle).getText();
    }

    public String getProjectEmptyStateMessage() {
        return waitForElementVisible(projectEmptyStateMessage).getText();
    }

    public WebElement getDeployErrorDialog() {
        return waitForElementVisible(BY_DEPLOY_ERROR_DIALOG, browser);
    }

    public boolean isErrorDialogVisible() {
        return getRoot().findElements(BY_DEPLOY_ERROR_DIALOG).size() > 0;
    }

    public void closeDeployErrorDialogButton() {
        waitForElementVisible(deployErrorDialogOKButton).click();
    }

    public void clickOnDeployProcessButton() {
        waitForElementPresent(deployProcessButton).click();
    }

    public void clickOnNewScheduleButton() {
        waitForElementVisible(newScheduleButton).click();
    }

    public int getNumberOfProcesses() {
        if (processes == null) {
            throw new NullPointerException();
        }
        return processes.size();
    }

    public void deleteProcess() {
        final int processNumberBeforeDelete = processes.size();
        clickOnDeleteButton();
        waitForElementVisible(processDeleteConfirmButton).click();
        waitForElementNotPresent(processDeleteDialog);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return processes.size() == processNumberBeforeDelete - 1;
            }
        });
        waitForElementVisible(getRoot());
    }

    public String getDeleteDialogTitle() {
        return waitForElementVisible(processDeleteDialogTitle).getText();
    }

    public String getDeleteDialogMessage() {
        return waitForElementVisible(processDeleteDialogMessage).getText();
    }

    public void clickOnProcessDeleteCancelButton() {
        waitForElementVisible(processDeleteCancelButton).click();
        waitForElementNotPresent(processDeleteDialog);
    }

    public void deleteAllProcesses() {
        Sleeper.sleepTightInSeconds(3);
        for (int i = processes.size() - 1; i >= 0; i--) {
            waitForElementVisible(BY_PROCESS_SCHEDULE_TAB, processes.get(i)).click();
            deleteProcess();
        }
        waitForCollectionIsEmpty(processes);
    }

    public boolean isExistingProcess(String processName) {
        waitForElementVisible(getRoot());
        Optional<WebElement> existingProcess = tryToFindProcess(processName);
        return existingProcess.isPresent();
    }

    public boolean isCorrectScheduleInList(final SchedulesTable schedulesTable,
            final ScheduleBuilder scheduleBuilder) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return schedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()) != null;
            }
        });

        return scheduleBuilder.getCronTimeBuilder().getCronFormatInProjectDetailPage()
                .equals(schedulesTable.getScheduleCron(scheduleBuilder.getScheduleName()).getText());
    }

    public ProjectDetailPage activeProcess(WebElement process) {
        waitForElementVisible(BY_PROCESS_SCHEDULE_TAB, process).click();
        return this;
    }

    public ProjectDetailPage activeProcess(String processName) {
        activeProcess(getProcess(processName));

        return this;
    }

    public List<WebElement> getProcesses() {
        return processes;
    }

    public WebElement getProcess(final String processName) {
        return Iterables.find(processes, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement process) {
                return waitForElementVisible(BY_PROCESS_TITLE, process).getText().equals(processName);
            }
        });
    }

    public void clickOnScheduleTab() {
        getScheduleTab().click();
    }

    public void clickOnExecutableTab() {
        getExecutableTab().click();
    }

    public void clickOnMetadataTab() {
        getMetadataTab().click();
    }

    public void clickOnNotificationRuleButton() {
        getNotificationRuleButton().click();
    }

    public void clickOnExecutableScheduleLink(String executableName) {
        getExecutableTable().getExecutableScheduleLink(executableName).click();
    }

    public void clickOnNewScheduleLinkInScheduleTab() {
        waitForElementVisible(BY_CREATE_NEW_SCHEDULE_LINK, activeProcess).click();
    }

    public void clickOnRedeployButton() {
        waitForElementVisible(BY_PROCESS_REDEPLOY_BUTTON, activeProcess).click();
    }

    public String getNotificationRuleNumber() {
        return getNotificationRuleButton().getText();
    }

    public ExecutablesTable getExecutableTable() {
        return waitForFragmentVisible(executablesTable);
    }

    public WebElement getNotificationRuleButton() {
        return waitForElementVisible(BY_PROCESS_NOTIFICATION_RULE_BUTTON, activeProcess);
    }

    public void clickOnDownloadButton() {
        waitForElementVisible(BY_PROCESS_DOWNLOAD_BUTTON, activeProcess).click();
    }

    public void clickOnDeleteButton() {
        waitForElementVisible(BY_PROCESS_DELETE_BUTTON, activeProcess).click();
    }

    public String getScheduleTabTitle() {
        return getScheduleTab().getText();
    }

    public WebElement getScheduleTab() {
        return waitForElementVisible(BY_PROCESS_SCHEDULE_TAB, activeProcess);
    }

    public String getExecutableTabTitle() {
        return getExecutableTab().getText();
    }

    public String getMetadata() {
        return Iterables.find(processMetadataItems, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement processMetadataItem) {
                return processMetadataItem.findElement(BY_PROCESS_METADATA_KEY).getText()
                        .equals(PROCESS_METADATA_ID);
            }
        }).findElement(BY_PROCESS_METADATA_VALUE).getText();
    }

    public String getBrokenScheduleMessage() {
        return waitForElementVisible(BY_BROKEN_SCHEDULE_MESSAGE, activeProcess).getText();
    }

    public WebElement getExecutableTab() {
        return waitForElementVisible(BY_PROCESS_EXECUTABLE_TAB, activeProcess);
    }

    public WebElement getMetadataTab() {
        return waitForElementVisible(BY_PROCESS_METADATA_TAB, activeProcess);
    }

    public String getProcessTitle() {
        return waitForElementVisible(BY_PROCESS_TITLE, activeProcess).getText();
    }

    public boolean isEmptyScheduleList() {
        return activeProcess.findElements(BY_CREATE_NEW_SCHEDULE_LINK).size() > 0;
    }

    public boolean isCorrectExecutableList(List<Executables> executables) {
        waitForElementVisible(BY_PROCESS_EXECUTABLE_TABLE, browser);
        return executablesTable.isCorrectExecutableList(executables);
    }

    public WebElement getSchedule(String name) {
        return waitForFragmentVisible(scheduleTable).getSchedule(name);
    }

    public void openScheduleDetail(String name) {
        waitForFragmentVisible(scheduleTable).getScheduleTitle(name).click();
    }

    public boolean isScheduleTabActive() {
        return getScheduleTab().getAttribute("class").contains("active");
    }

    public boolean isExecutableTabActive() {
        return getExecutableTab().getAttribute("class").contains("active");
    }

    public boolean isMetadataTabActive() {
        return getMetadataTab().getAttribute("class").contains("active");
    }

    private Optional<WebElement> tryToFindProcess(final String processName) {
        Optional<WebElement> existingProcess = Iterables.tryFind(processes, new Predicate<WebElement>() {
    
            @Override
            public boolean apply(WebElement process) {
                return activeProcess(process).getProcessTitle().equals(processName);
            }
        });
        return existingProcess;
    }
}
