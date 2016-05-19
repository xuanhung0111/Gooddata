package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertNotNull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ProjectsList extends AbstractTable {

    private String XPATH_PROJECT_NAME =
            "//.[contains(@class, 'ait-project-list-item-title') and contains(text(),'${searchKey}')]";
    private String XPATH_PROJECT_NAME_NOT_ADMIN =
            "//span[@class = 'project-not-admin-title ait-project-list-item-title' and contains(text(), '${searchKey}')]";

    private static final By BY_DISC_PROJECT_NAME = By.cssSelector(".ait-project-list-item-title");
    private static final By BY_PROJECT_CHECKBOX = By.cssSelector("td.project-checkbox-cell input");
    private static final By BY_DISC_PROJECT_NAME_NOT_ADMIN = By
            .cssSelector(".project-name-user-not-admin-cell .ait-project-list-item-title");
    private static final By BY_DISC_PROJECT_DATA_LOADING_PROCESSES = By
            .cssSelector(".ait-project-list-item-processes-label");
    private static final By BY_DISC_PROJECT_LAST_SUCCESSFUL_EXECUTION = By
            .cssSelector(".ait-project-list-item-last-loaded-label");
    private static final By BY_EMPTY_STATE = By.cssSelector(".ait-projects-empty-state");

    @FindBy(xpath = "//button[contains(@class, 's-btn-deploy_process')]")
    private WebElement deployProcessButton;

    @FindBy(xpath = "//div[@class='error-bar']/div[@class='error-bar-title']")
    private WebElement errorBar;

    @FindBy(css = ".ait-project-detail-fragment")
    private WebElement projectDetail;

    @FindBy(css = ".ait-projects-empty-state")
    private WebElement projectsEmptyState;

    public String getProcessesLabel(String projectId) {
        return selectProjectWithAdminRole(projectId).findElement(BY_DISC_PROJECT_DATA_LOADING_PROCESSES).getText();
    }

    public void clickOnProjectWithNonAdminRole(WebElement selectedProject) {
        selectedProject.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).click();
    }

    public String getLastSuccessfulExecutionInfo(String projectId) {
        return waitForElementPresent(BY_DISC_PROJECT_LAST_SUCCESSFUL_EXECUTION,
                selectProjectWithAdminRole(projectId)).getText();
    }

    public WebElement selectProjectWithAdminRole(final String projectId) {
        Predicate<WebElement> predicate = row -> row.findElement(BY_PROJECT_CHECKBOX).isEnabled()
              && row.findElement(BY_DISC_PROJECT_NAME).getAttribute("href").contains(projectId);
        return selectProject(predicate);
    }

    public boolean isCorrectSearchResultByName(String searchKey) {
        for (WebElement project : getRows()) {
            By projectNameLocator  = project.findElement(BY_PROJECT_CHECKBOX).isEnabled()?
                    BY_DISC_PROJECT_NAME : BY_DISC_PROJECT_NAME_NOT_ADMIN;
            if (!project.findElement(projectNameLocator).getText().contains(searchKey)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCorrectSearchedProjectByUnicodeName(String searchKey) {
        for (WebElement project : getRows()) {
            String projectNameLocator = project.findElement(BY_PROJECT_CHECKBOX).isEnabled()?
                    XPATH_PROJECT_NAME : XPATH_PROJECT_NAME_NOT_ADMIN;
            if (project.findElements(By.xpath(projectNameLocator.replace("${searchKey}", searchKey))).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(projectsEmptyState).getText();
    }

    public WebElement getEmptyState() {
        return waitForElementVisible(projectsEmptyState);
    }

    public void checkOnProjects(String... projectIds) {
        for (String projectId : projectIds) {
            WebElement selectedProject = selectProjectWithAdminRole(projectId);
            assertNotNull(selectedProject, "Project is not found!");
            selectedProject.findElement(BY_PROJECT_CHECKBOX).click();
        }
    }

    public void clickOnProjectTitle(String projectId) {
        WebElement selectedProject = selectProjectWithAdminRole(projectId);
        assertNotNull(selectedProject, String.format("Project is not found!"));
        selectedProject.findElement(BY_DISC_PROJECT_NAME).click();
    }

    public void clickOnDeployProcessButton() {
        waitForElementVisible(deployProcessButton).click();
    }

    public WebElement getDeployProcessButton() {
        return deployProcessButton;
    }

    public WebElement getErrorBar() {
        return errorBar;
    }

    public WebElement selectProjectWithNonAdminRole(final String projectName) {
        Predicate<WebElement> predicate = row -> !row.findElement(BY_PROJECT_CHECKBOX).isEnabled()
              && row.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).getText().equals(projectName);
        return selectProject(predicate);
    }

    private WebElement selectProject(Predicate<WebElement> predicate) {
        final By nextButtonLocator = className("s-btn-next");

        while (true) {
            if (!isElementPresent(BY_EMPTY_STATE, getRoot()))
                waitForCollectionIsNotEmpty(rows);

            Optional<WebElement> project = Iterables.tryFind(rows, predicate);
            if (project.isPresent())
                return project.get();

            if (!isElementPresent(nextButtonLocator, getRoot())) {
                break;
            }

            final WebElement nextButton = waitForElementVisible(nextButtonLocator, getRoot());
            if (nextButton.getAttribute("class").contains("disabled")) {
                break;
            }

            nextButton.click();
            waitForElementVisible(getRoot());
        }

        return null;
    }
}
