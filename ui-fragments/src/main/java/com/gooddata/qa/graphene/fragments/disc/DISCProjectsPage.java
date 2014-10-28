package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.DISCProjectFilters;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static org.testng.Assert.*;

public class DISCProjectsPage extends AbstractFragment {

    private By BY_ALL_PROJECTS_LINK = By.cssSelector(".all-projects-link");

    @FindBy(css = ".filter-combo .ember-select")
    private WebElement projectFilter;

    @FindBy(css = ".paging-bar select")
    private WebElement projectsPerPageOptions;

    @FindBy(css = ".project-list")
    protected DISCProjectsList discProjectsList;

    @FindBy(css = ".pages-bar .page-cell")
    protected List<WebElement> projectPageNumber;

    @FindBy(css = ".next-page-btn")
    private WebElement nextPageButton;

    @FindBy(css = ".prev-page-btn")
    private WebElement prevPageButton;

    @FindBy(css = ".paging-bar .label")
    private WebElement pagingBarLabel;

    @FindBy(css = ".search-field input")
    private WebElement searchBox;

    @FindBy(css = ".search-field .search-button")
    private WebElement searchButton;

    @FindBy(css = ".search-field .search-delete-button")
    private WebElement deleteSearchKeyButton;

    @FindBy(css = ".searching-progress")
    private WebElement searchingProgress;

    public void checkProjectFilterOptions() {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        List<WebElement> options = select.getOptions();
        System.out.println("Check filter options list...");
        assertEquals(DISCProjectFilters.ALL.getOption(), options.get(0).getText());
        assertEquals(DISCProjectFilters.FAILED.getOption(), options.get(1).getText());
        assertEquals(DISCProjectFilters.RUNNING.getOption(), options.get(2).getText());
        assertEquals(DISCProjectFilters.SCHEDULED.getOption(), options.get(3).getText());
        assertEquals(DISCProjectFilters.SUCCESSFUL.getOption(), options.get(4).getText());
        assertEquals(DISCProjectFilters.UNSCHEDULED.getOption(), options.get(5).getText());
        assertEquals(DISCProjectFilters.DISABLED.getOption(), options.get(6).getText());
    }

    public WebElement getSelectedFilterOption() {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        return select.getFirstSelectedOption();
    }

    public void selectFilterOption(String option) throws InterruptedException {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        select.selectByVisibleText(option);
        System.out.println("Selected filter option:" + option);
    }

    public void checkFilteredProjects(Map<String, String> filteredProjects,
            DISCProjectsList projectsList) throws InterruptedException {
        for (Entry<String, String> filteredProject : filteredProjects.entrySet()) {
            assertNotNull(projectsList.selectProject(filteredProject.getKey(),
                    filteredProject.getValue(), true));
            System.out.println("Project " + filteredProject.getKey() + "(id = "
                    + filteredProject.getValue() + ") is in filtered list.");
        }
    }

    public void checkFilteredOutProjects(Map<String, String> filteredOutProjects,
            DISCProjectsList projectsList) throws InterruptedException {
        for (Entry<String, String> filteredOutProject : filteredOutProjects.entrySet()) {
            assertNull(projectsList.selectProject(filteredOutProject.getKey(),
                    filteredOutProject.getValue(), true));
            System.out.println("Project " + filteredOutProject.getKey() + "(id = "
                    + filteredOutProject.getValue() + ") is filtered out.");
        }
    }

    public void checkProjectsPagingOptions() {
        waitForElementVisible(projectsPerPageOptions);
        Select select = new Select(projectsPerPageOptions);
        List<WebElement> pagingOptions = select.getOptions();
        System.out.println("Check paging options list...");
        assertEquals("20", pagingOptions.get(0).getText());
        assertEquals("50", pagingOptions.get(1).getText());
        assertEquals("100", pagingOptions.get(2).getText());
        assertEquals("200", pagingOptions.get(3).getText());
        assertEquals("500", pagingOptions.get(4).getText());
        assertEquals("1000", pagingOptions.get(5).getText());
        assertEquals("2000", pagingOptions.get(6).getText());
        assertEquals("5000", pagingOptions.get(7).getText());
        assertEquals("20", select.getFirstSelectedOption().getText());
    }

    public void checkProjectFilter(String filterOption, Map<String, String> projectsMap)
            throws InterruptedException {
        List<String> filterOutOptions =
                Arrays.asList(DISCProjectFilters.DISABLED.getOption(),
                        DISCProjectFilters.FAILED.getOption(),
                        DISCProjectFilters.RUNNING.getOption(),
                        DISCProjectFilters.SCHEDULED.getOption(),
                        DISCProjectFilters.SUCCESSFUL.getOption(),
                        DISCProjectFilters.UNSCHEDULED.getOption());
        System.out.println("Check filter option:" + filterOption);
        selectFilterOption(filterOption);
        Thread.sleep(2000);
        waitForElementVisible(discProjectsList.getRoot());
        checkFilteredProjects(projectsMap, discProjectsList);
        if (filterOption == DISCProjectFilters.DISABLED.getOption()) {
            selectFilterOption(DISCProjectFilters.UNSCHEDULED.getOption());
            waitForElementVisible(discProjectsList.getRoot());
            checkFilteredProjects(projectsMap, discProjectsList);
            for (String filterOutOption : filterOutOptions) {
                if (filterOutOption != filterOption
                        && filterOutOption != DISCProjectFilters.UNSCHEDULED.getOption()) {
                    selectFilterOption(filterOutOption);
                    Thread.sleep(2000);
                    waitForElementVisible(discProjectsList.getRoot());
                    checkFilteredOutProjects(projectsMap, discProjectsList);
                }
            }
        } else {
            for (String filterOutOption : filterOutOptions) {
                if (filterOutOption != filterOption) {
                    selectFilterOption(filterOutOption);
                    Thread.sleep(2000);
                    waitForElementVisible(discProjectsList.getRoot());
                    checkFilteredOutProjects(projectsMap, discProjectsList);
                }
            }
        }
        selectFilterOption(DISCProjectFilters.ALL.getOption());
        waitForElementVisible(discProjectsList.getRoot());
        checkFilteredProjects(projectsMap, discProjectsList);
    }

    public void checkPagingProjectsPage(String selectedProjectsPerPageOption) {
        waitForElementVisible(projectsPerPageOptions);
        Select select = new Select(projectsPerPageOptions);
        select.selectByVisibleText(selectedProjectsPerPageOption);
        assertEquals(selectedProjectsPerPageOption, select.getFirstSelectedOption().getText());
        waitForElementVisible(discProjectsList.getRoot());
        int projectsPerPageNumber = Integer.parseInt(selectedProjectsPerPageOption);
        assertEquals(projectsPerPageNumber, discProjectsList.getNumberOfRows());
        String pagingBarLabelSubString = String.format("Showing 1-%d", projectsPerPageNumber);
        System.out.println("Paging bar label: " + pagingBarLabelSubString);
        assertTrue(pagingBarLabel.getText().contains(pagingBarLabelSubString), "Paging bar label:"
                + pagingBarLabel.getText());
        assertTrue(prevPageButton.getAttribute("class").contains("disabled"));
        assertFalse(nextPageButton.getAttribute("class").contains("disabled"));
        assertTrue(projectPageNumber.get(0).getAttribute("class").contains("active-cell"));
        System.out.println("Click on Next button...");
        nextPageButton.click();
        waitForElementVisible(discProjectsList.getRoot());
        assertFalse(projectPageNumber.get(0).getAttribute("class").contains("active-cell"));
        assertTrue(projectPageNumber.get(1).getAttribute("class").contains("active-cell"));
        assertFalse(prevPageButton.getAttribute("class").contains("disabled"));
        System.out.println("Click on Prev button...");
        prevPageButton.click();
        waitForElementVisible(discProjectsList.getRoot());
        assertFalse(projectPageNumber.get(1).getAttribute("class").contains("active-cell"));
        assertTrue(projectPageNumber.get(0).getAttribute("class").contains("active-cell"));
        assertTrue(prevPageButton.getAttribute("class").contains("disabled"));
        for (int i = 1; i < projectPageNumber.size() && i < 4; i++) {
            System.out.println("Click on page " + i);
            projectPageNumber.get(i).click();
            waitForElementVisible(discProjectsList.getRoot());
            assertTrue(projectPageNumber.get(i).getAttribute("class").contains("active-cell"));
            if (i != projectPageNumber.size() - 1)
                assertEquals(projectsPerPageNumber, discProjectsList.getNumberOfRows());
            else
                assertFalse(
                        discProjectsList.getNumberOfRows() > projectsPerPageNumber,
                        "The project number in the last page: "
                                + discProjectsList.getNumberOfRows());
        }
    }

    public void searchProjectInSpecificState(DISCProjectFilters projectFilter, String projectTitle,
            String projectId) throws InterruptedException {
        selectFilterOption(projectFilter.getOption());
        searchProjectByName(projectTitle);
        searchProjectById(projectId, projectTitle);
    }

    public void searchProjectByName(String searchKey) throws InterruptedException {
        enterSearchKey(searchKey);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertSearchProjectsByName(searchKey);
    }
    
    public void searchProjectByUnicodeName(String unicodeSearchKey) {
        enterSearchKey(unicodeSearchKey);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertSearchProjectByUnicodeName(unicodeSearchKey);
    }

    public void searchProjectById(String projectId, String projectTitle)
            throws InterruptedException {
        enterSearchKey(projectId);
        waitForElementVisible(discProjectsList.getRoot());
        assertEquals(discProjectsList.getNumberOfRows(), 1,
                "Actual project number in search result: " + discProjectsList.getNumberOfRows());
        assertNotNull(discProjectsList.selectProject(projectTitle, projectId, true));
    }

    public void checkEmptySearchResult(String searchKey) throws InterruptedException {
        for (DISCProjectFilters projectFilter : DISCProjectFilters.values()) {
            selectFilterOption(projectFilter.getOption());
            String expectedEmptySearchResultMessage =
                    projectFilter.getEmptySearchResultMessage().replace("${searchKey}", searchKey);
            enterSearchKey(searchKey);
            System.out.println("Empty Search Result Message: "
                    + discProjectsList.getEmptyStateMessage().trim());
            String actualEmptySearchResultMessage = discProjectsList.getEmptyStateMessage();
            if (projectFilter.equals(DISCProjectFilters.ALL))
                assertEquals(actualEmptySearchResultMessage.trim(),
                        expectedEmptySearchResultMessage);
            else {
                assertTrue(actualEmptySearchResultMessage
                        .contains(expectedEmptySearchResultMessage));
                String emtySearchResultMessageInSpecificState = "Search in all projects";
                assertTrue(actualEmptySearchResultMessage
                        .contains(emtySearchResultMessageInSpecificState));
                discProjectsList.getEmptyState().findElement(BY_ALL_PROJECTS_LINK).click();
                assertEquals(getSelectedFilterOption().getText(),
                        DISCProjectFilters.ALL.getOption());
            }
        }
    }

    public void enterSearchKey(String searchKey) {
        waitForElementVisible(searchBox).clear();
        waitForElementVisible(searchBox).sendKeys(searchKey);
        System.out.println("Enter search key: " + searchBox.getAttribute("value"));
        waitForElementVisible(searchButton).click();
        try {
            waitForElementVisible(searchingProgress);
        } catch (NoSuchElementException ex) {
            System.out
                    .println("Searching progress doesn't display, please check the search result...");
        }
    }

    public void checkDeleteSearchKey(String noResultSearchKey) {
        enterSearchKey(noResultSearchKey);
        System.out.println("Empty state message: " + discProjectsList.getEmptyStateMessage());
        waitForElementVisible(deleteSearchKeyButton).click();
        assertTrue(searchBox.getAttribute("value").isEmpty());
        waitForElementVisible(discProjectsList.getRoot());
    }

    public void checkDefaultSearchBox() {
        waitForElementVisible(searchBox);
        assertTrue(searchBox.getAttribute("value").isEmpty());
        assertEquals(searchBox.getAttribute("placeholder"), "Search in project names and ids ...");
    }
}
