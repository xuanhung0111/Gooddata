package com.gooddata.qa.graphene.fragments.disc;

import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.fragments.AbstractTable;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.*;

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

    @FindBy(css = ".page-cell")
    private List<WebElement> projectPages;

    @FindBy(css = ".ait-project-detail-fragment")
    private WebElement projectDetail;

    @FindBy(css = ".ait-projects-empty-state")
    private WebElement projectsEmptyState;

    public String getProcessesLabel(ProjectInfo project) {
        return selectProjectWithAdminRole(project).findElement(BY_DISC_PROJECT_DATA_LOADING_PROCESSES).getText();
    }

    public void clickOnProjectWithNonAdminRole(WebElement selectedProject) {
        selectedProject.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).click();
    }

    public String getLastSuccessfulExecutionInfo(ProjectInfo project) {
        return waitForElementPresent(BY_DISC_PROJECT_LAST_SUCCESSFUL_EXECUTION,
                selectProjectWithAdminRole(project)).getText();
    }

    public WebElement selectProjectWithAdminRole(final ProjectInfo project) {
        Predicate<WebElement> predicate = new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement row) {
                return row.findElement(BY_PROJECT_CHECKBOX).isEnabled()
                        && row.findElement(BY_DISC_PROJECT_NAME).getText().equals(project.getProjectName())
                        && row.findElement(BY_DISC_PROJECT_NAME).getAttribute("href")
                                .contains(project.getProjectId());
            }
        };
        return selectProject(predicate);
    }

    public boolean isCorrectSearchResultByName(String searchKey) {
        for (WebElement project : getRows()) {
            if (project.findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
                if (!project.findElement(BY_DISC_PROJECT_NAME).getText().contains(searchKey))
                    return false;
            } else {
                if (!project.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).getText().contains(searchKey))
                    return false;
            }
        }

        return true;
    }

    public boolean isCorrectSearchedProjectByUnicodeName(String searchKey) {
        for (WebElement project : getRows()) {
            if (project.findElement(BY_PROJECT_CHECKBOX).isEnabled()) {
                if (project.findElements(By.xpath(XPATH_PROJECT_NAME.replace("${searchKey}", searchKey)))
                        .isEmpty())
                    return false;
            } else {
                if (project
                        .findElements(By.xpath(XPATH_PROJECT_NAME_NOT_ADMIN.replace("${searchKey}", searchKey)))
                        .isEmpty())
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

    public void checkOnProjects(List<ProjectInfo> projects) {
        for (ProjectInfo project : projects) {
            WebElement selectedProject = selectProjectWithAdminRole(project);
            assertNotNull(selectedProject, "Project is not found!");
            selectedProject.findElement(BY_PROJECT_CHECKBOX).click();
        }
    }

    public void clickOnProjectTitle(ProjectInfo project) {
        WebElement selectedProject = selectProjectWithAdminRole(project);
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
        Predicate<WebElement> predicate = new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement row) {
                return !row.findElement(BY_PROJECT_CHECKBOX).isEnabled()
                        && row.findElement(BY_DISC_PROJECT_NAME_NOT_ADMIN).getText().equals(projectName);
            }
        };
        return selectProject(predicate);
    }

    private WebElement selectProject(Predicate<WebElement> predicate) {
        Iterator<WebElement> iterator = projectPages.iterator();
        do {
            if (getRoot().findElements(BY_EMPTY_STATE).isEmpty())
                waitForCollectionIsNotEmpty(rows);
            Optional<WebElement> project = Iterables.tryFind(rows, predicate);
            if (project.isPresent())
                return project.get();
            if (iterator.hasNext()) {
                iterator.next().click();
                waitForElementVisible(getRoot());
            } else
                return null;
        } while (projectPages.size() > 0);
        return null;
    }
}
