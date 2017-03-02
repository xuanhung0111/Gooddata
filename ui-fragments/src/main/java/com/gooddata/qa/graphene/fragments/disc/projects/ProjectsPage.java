package com.gooddata.qa.graphene.fragments.disc.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;

public class ProjectsPage extends AbstractFragment {

    public static final String URI = "admin/disc/#/projects";

    private static final By BY_PROJECT_TITLE = By.cssSelector("[class*='project-name']");

    @FindBy(css = ".filter-combo select")
    private Select filterOptionSelect;

    @FindBy(className = "search-field-input")
    private WebElement searchField;

    @FindBy(className = "search-button")
    private WebElement searchButton;

    @FindBy(className = "s-btn-deploy_process")
    private WebElement deployProcessButton;

    @FindBy(className = "ait-project-list-item")
    private Collection<WebElement> projectItems;

    @FindBy(css = ".paging-bar select")
    private Select pagingOptionSelect;

    @FindBy(className = "s-btn-prev")
    private WebElement previousPageButton;

    @FindBy(className = "s-btn-next")
    private WebElement nextPageButton;

    @FindBy(className = "page-cell")
    private List<WebElement> pages;

    public ProjectsPage waitForPageLoaded() {
        waitForElementNotPresent(By.className("loading"));
        return this;
    }

    public Collection<String> getFilterOptions() {
        return getElementTexts(waitForElementVisible(filterOptionSelect).getOptions());
    }

    public String getSelectedFilterOption() {
        return waitForElementVisible(filterOptionSelect).getFirstSelectedOption().getText();
    }

    public ProjectsPage selectFilterOption(FilterOption option) {
        waitForElementVisible(filterOptionSelect).selectByVisibleText(option.toString());
        waitForPageLoaded();
        return this;
    }

    public String getSearchFieldValue() {
        return waitForElementVisible(searchField).getAttribute("value");
    }

    public String getSearchFieldHint() {
        return waitForElementVisible(searchField).getAttribute("placeholder");
    }

    public ProjectsPage searchProject(String nameOrId) {
        inputSearchField(nameOrId);

        waitForElementVisible(searchButton).click();
        waitForElementNotPresent(By.className("searching-progress"));

        return this;
    }

    public ProjectsPage inputSearchField(String value) {
        waitForElementVisible(searchField).clear();
        searchField.sendKeys(value);
        return this;
    }

    public ProjectsPage deleteSearchValue() {
        waitForElementVisible(By.className("search-delete-button"), getRoot()).click();
        return this;
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(By.cssSelector(".empty-state .title"), getRoot()).getText();
    }

    public ProjectsPage clickSearchInAllProjectsLink() {
        waitForElementVisible(By.className("all-projects-link"), getRoot()).click();
        return this;
    }

    public int getProjectNumber() {
        return projectItems.size();
    }

    public boolean hasProject(String title) {
        return findProjectElement(title).isPresent();
    }

    public String getProcessesInfoFrom(String projectTitle) {
        return findProjectElement(projectTitle).get()
                .findElement(By.className("processes-cell")).getText();
    }

    public String getLastSuccessfulExecutionFrom(String projectTitle) {
        return findProjectElement(projectTitle).get()
                .findElement(By.className("last-load-cell")).getText();
    }

    public boolean isProjectDisabled(String projectTitle) {
        return findProjectElement(projectTitle).get()
                .findElement(BY_PROJECT_TITLE)
                .getAttribute("class")
                .contains("not-admin");
    }

    public ProjectsPage markProjectCheckbox(String title) {
        WebElement checkbox = findProjectElement(title).get().findElement(By.tagName("input"));

        if (!checkbox.isSelected()) {
            checkbox.click();
        }
        return this;
    }

    public ProjectsPage deployProcessWithZipFile(String processName, ProcessType processType, File packageFile) {
        clickDeployButton().deployProcessWithZipFile(processName, processType, packageFile);
        return this;
    }

    public ProjectsPage deployProcessWithGitStorePath(String processName, String gitStorePath) {
        clickDeployButton().deployProcessWithGitStorePath(processName, gitStorePath);
        return this;
    }

    public Collection<String> getPagingOptions() {
        return getElementTexts(waitForElementVisible(pagingOptionSelect).getOptions());
    }

    public String getSelectedPagingOption() {
        return waitForElementVisible(pagingOptionSelect).getFirstSelectedOption().getText();
    }

    public ProjectsPage goToNextPage() {
        waitForElementVisible(nextPageButton).click();
        waitForPageLoaded();

        return this;
    }

    public boolean hasNextPage() {
        return !pages.isEmpty() && !waitForElementVisible(nextPageButton).getAttribute("class").contains("disabled");
    }

    public ProjectsPage goToPreviousPage() {
        waitForElementVisible(previousPageButton).click();
        waitForPageLoaded();

        return this;
    }

    public boolean hasPreviousPage() {
        return !pages.isEmpty() && !waitForElementVisible(previousPageButton).getAttribute("class").contains("disabled");
    }

    public String getPagingDescription() {
        return waitForElementVisible(By.cssSelector(".paging-bar .left"), getRoot()).getText();
    }

    public boolean isOnPage(int pageNumber) {
        return getPageLinkElement(pageNumber).getAttribute("class").contains("active");
    }

    public ProjectsPage goToLastPage() {
        return goToPage(pages.size());
    }

    public String getErrorBarMessage() {
        return waitForElementVisible(By.className("error-bar-title"), getRoot()).getText();
    }

    private DeployProcessForm clickDeployButton() {
        waitForElementVisible(deployProcessButton).sendKeys(Keys.ENTER);
        return DeployProcessForm.getInstance(By.className("gd-dialog"), browser);
    }

    private ProjectsPage goToPage(int pageNumber) {
        getPageLinkElement(pageNumber).click();
        waitForPageLoaded();
        return this;
    }

    private WebElement getPageLinkElement(int pageNumber) {
        return IntStream.rangeClosed(1, pages.size())
                .filter(i -> i == pageNumber)
                .mapToObj(i -> pages.get(i - 1))
                .findFirst()
                .get();
    }

    private Optional<WebElement> findProjectElement(String title) {
        while (true) {
            Optional<WebElement> project = projectItems.stream()
                    .filter(p -> title.equals(p.findElement(BY_PROJECT_TITLE).getText()))
                    .findFirst();

            if (project.isPresent()) {
                return project;
            }

            if (!hasNextPage()) break;
            goToNextPage();
        }

        return Optional.ofNullable(null);
    }

    public enum FilterOption {
        ALL,
        FAILED,
        RUNNING,
        SCHEDULED,
        SUCCESSFUL,
        UNSCHEDULED,
        DISABLED;

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    }
}
