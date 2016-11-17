package com.gooddata.qa.graphene.fragments.disc.projects;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class __ProjectsPage extends AbstractFragment {

    private static final By BY_PROJECT_TITLE = By.cssSelector("[class*='project-name']");

    @FindBy(css = ".filter-combo select")
    private Select filterOptionSelect;

    @FindBy(className = "search-field-input")
    private WebElement searchField;

    @FindBy(className = "search-delete-button")
    private WebElement searchDeleteButton;

    @FindBy(className = "search-button")
    private WebElement searchButton;

    @FindBy(css = ".empty-state .title")
    private WebElement emptyStateMessage;

    @FindBy(className = "all-projects-link")
    private WebElement allProjectsLink;

    @FindBy(className = "ait-project-list-item")
    private Collection<WebElement> projectItems;

    @FindBy(css = ".paging-bar select")
    private Select pagingOptionSelect;

    @FindBy(className = "s-btn-prev")
    private WebElement previousPageButton;

    @FindBy(className = "s-btn-next")
    private WebElement nextPageButton;

    @FindBy(css = ".paging-bar .left")
    private WebElement pagingDescription;

    @FindBy(className = "page-cell")
    private List<WebElement> pages;

    public __ProjectsPage waitForPageLoaded() {
        waitForElementNotPresent(By.className("loading"));
        return this;
    }

    public Collection<String> getFilterOptions() {
        return getElementTexts(waitForElementVisible(filterOptionSelect).getOptions());
    }

    public String getSelectedFilterOption() {
        return waitForElementVisible(filterOptionSelect).getFirstSelectedOption().getText();
    }

    public __ProjectsPage selectFilterOption(FilterOption option) {
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

    public __ProjectsPage searchProject(String nameOrId) {
        inputSearchField(nameOrId);

        waitForElementVisible(searchButton).click();
        waitForElementNotPresent(By.className("searching-progress"));

        return this;
    }

    public __ProjectsPage inputSearchField(String value) {
        waitForElementVisible(searchField).clear();
        searchField.sendKeys(value);
        return this;
    }

    public __ProjectsPage deleteSearchValue() {
        waitForElementVisible(searchDeleteButton).click();
        return this;
    }

    public String getEmptyStateMessage() {
        return waitForElementVisible(emptyStateMessage).getText();
    }

    public __ProjectsPage clickSearchInAllProjectsLink() {
        waitForElementVisible(allProjectsLink).click();
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

    public Collection<String> getPagingOptions() {
        return getElementTexts(waitForElementVisible(pagingOptionSelect).getOptions());
    }

    public String getSelectedPagingOption() {
        return waitForElementVisible(pagingOptionSelect).getFirstSelectedOption().getText();
    }

    public __ProjectsPage goToNextPage() {
        waitForElementVisible(nextPageButton).click();
        waitForPageLoaded();

        return this;
    }

    public boolean hasNextPage() {
        return !waitForElementVisible(nextPageButton).getAttribute("class").contains("disabled");
    }

    public __ProjectsPage goToPreviousPage() {
        waitForElementVisible(previousPageButton).click();
        waitForPageLoaded();

        return this;
    }

    public boolean hasPreviousPage() {
        return !waitForElementVisible(previousPageButton).getAttribute("class").contains("disabled");
    }

    public String getPagingDescription() {
        return waitForElementVisible(pagingDescription).getText();
    }

    public boolean isOnPage(int pageNumber) {
        return getPageLinkElement(pageNumber).getAttribute("class").contains("active");
    }

    public __ProjectsPage goToLastPage() {
        return goToPage(pages.size());
    }

    private __ProjectsPage goToPage(int pageNumber) {
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
        return projectItems.stream()
                .filter(p -> title.equals(p.findElement(BY_PROJECT_TITLE).getText()))
                .findFirst();
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
