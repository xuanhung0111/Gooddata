package com.gooddata.qa.graphene.fragments.disc.overview;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.Collection;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.ConfirmationDialog;

public class OverviewProjects extends AbstractFragment {

    @FindBy(css = ".empty-state .title")
    private WebElement emptyState;

    @FindBy(className = "s-btn-restart")
    private WebElement restartButton;

    @FindBy(className = "s-btn-run")
    private WebElement runButton;

    @FindBy(className = "s-btn-stop")
    private WebElement stopButton;

    @FindBy(className = "s-btn-disable")
    private WebElement disableButton;

    @FindBy(className = "overview-project-item")
    private Collection<OverviewProjectItem> projectItems;

    public String getEmptyStateMessage() {
        return waitForElementVisible(emptyState).getText();
    }

    public OverviewProjectItem getProject(String title) {
        return projectItems.stream()
                .filter(project -> title.equals(project.getTitle()))
                .findFirst()
                .get();
    }

    public OverviewProjects markOnProject(String title) {
        getProject(title).markOn();
        return this;
    }

    public OverviewProjects executeSchedule() {
        waitForElementVisible(runButton).click();
        ConfirmationDialog.getInstance(browser).confirm();
        return this;
    }

    public OverviewProjects restartScheduleExecution() {
        waitForElementVisible(restartButton).click();
        ConfirmationDialog.getInstance(browser).confirm();
        return this;
    }

    public OverviewProjects stopScheduleExecution() {
        waitForElementVisible(stopButton).click();
        ConfirmationDialog.getInstance(browser).confirm();
        return this;
    }

    public OverviewProjects disableScheduleExecution() {
        waitForElementVisible(disableButton).click();
        ConfirmationDialog.getInstance(browser).confirm();
        return this;
    }

    public class OverviewProjectItem extends AbstractFragment {

        @FindBy(className = "ait-overview-project-list-item-title")
        private WebElement title;

        @FindBy(tagName = "input")
        private WebElement checkbox;

        @FindBy(className = "expand-arrow")
        private WebElement expandArrow;

        @FindBy(className = "overview-schedule-item")
        private Collection<WebElement> schedules;

        public void openDetailPage() {
            waitForElementVisible(title).click();
        }

        public boolean isDisabled() {
            return getRoot().getAttribute("class").contains("non-admin");
        }

        public OverviewProjectItem expand() {
            if (isCollapsed()) {
                expandArrow.click();
            }
            return this;
        }

        public boolean hasSchedule(String name) {
            return findSchedule(name).isPresent();
        }

        public String getScheduleExecutable(String scheduleName) {
            return findSchedule(scheduleName).get().findElement(By.className("label")).getText();
        }

        private String getTitle() {
            return waitForElementVisible(title).getText();
        }

        private OverviewProjectItem markOn() {
            waitForElementVisible(checkbox);

            if (!checkbox.isSelected()) {
                checkbox.click();
            }
            return this;
        }

        private boolean isCollapsed() {
            return waitForElementVisible(expandArrow).getAttribute("class").contains("icon-directright");
        }

        private Optional<WebElement> findSchedule(String name) {
            return schedules.stream().filter(s -> name.equals(getScheduleName(s))).findFirst();
        }

        private String getScheduleName(WebElement schedule) {
            return waitForElementVisible(By.className("title"), schedule).getText();
        }
    }
}
