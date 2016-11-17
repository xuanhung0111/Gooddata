package com.gooddata.qa.graphene.fragments.disc.overview;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.Collection;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.projects.__DiscProjectDetailPage;

public class __OverviewProjects extends AbstractFragment {

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
    private Collection<__OverviewProjectItem> projectItems;

    public String getEmptyStateMessage() {
        return waitForElementVisible(emptyState).getText();
    }

    public __OverviewProjectItem getProject(String title) {
        return projectItems.stream()
                .filter(project -> title.equals(project.getTitle()))
                .findFirst()
                .get();
    }

    public __OverviewProjects markOnProject(String title) {
        getProject(title).markOn();
        return this;
    }

    public __OverviewProjects executeSchedule() {
        waitForElementVisible(runButton).click();
        submitConfirmDialog();
        return this;
    }

    public __OverviewProjects restartScheduleExecution() {
        waitForElementVisible(restartButton).click();
        submitConfirmDialog();
        return this;
    }

    public __OverviewProjects stopScheduleExecution() {
        waitForElementVisible(stopButton).click();
        submitConfirmDialog();
        return this;
    }

    public __OverviewProjects disableScheduleExecution() {
        waitForElementVisible(disableButton).click();
        submitConfirmDialog();
        return this;
    }

    private void submitConfirmDialog() {
        final WebElement dialog = waitForElementVisible(By.className("gd-dialog"), browser);

        waitForElementVisible(By.cssSelector(".button-positive,.button-negative"), dialog).click();
        waitForElementNotPresent(dialog);
    }

    public class __OverviewProjectItem extends AbstractFragment {

        @FindBy(className = "ait-overview-project-list-item-title")
        private WebElement title;

        @FindBy(tagName = "input")
        private WebElement checkbox;

        public __DiscProjectDetailPage openDetailPage() {
            waitForElementVisible(title).click();
            return __DiscProjectDetailPage.getInstance(browser);
        }

        public boolean isDisabled() {
            return getRoot().getAttribute("class").contains("non-admin");
        }

        private String getTitle() {
            return waitForElementVisible(title).getText();
        }

        private __OverviewProjectItem markOn() {
            waitForElementVisible(checkbox);

            if (!checkbox.isSelected()) {
                checkbox.click();
            }
            return this;
        }
    }
}
