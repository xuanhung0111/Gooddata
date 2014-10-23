package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static org.testng.Assert.*;

public class DISCProjectsPage extends AbstractFragment {

    private static final String XPATH_SELECTED_OPTION = "//option[text()='${selectedOption}']";

    private By BY_ALL_PROJECTS_LINK = By.cssSelector(".all-projects-link");

    @FindBy(css = ".filter-combo .ember-select")
    private WebElement projectFilter;

    @FindBy(css = ".paging-bar select")
    private WebElement projectsPerPageOptions;

    @FindBy(css = ".project-list")
    private ProjectsList discProjectsList;

    @FindBy(css = ".pages-bar .page-cell")
    private List<WebElement> projectPageNumber;

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
        assertEquals(ProjectStateFilters.ALL.getOption(), options.get(0).getText());
        assertEquals(ProjectStateFilters.FAILED.getOption(), options.get(1).getText());
        assertEquals(ProjectStateFilters.RUNNING.getOption(), options.get(2).getText());
        assertEquals(ProjectStateFilters.SCHEDULED.getOption(), options.get(3).getText());
        assertEquals(ProjectStateFilters.SUCCESSFUL.getOption(), options.get(4).getText());
        assertEquals(ProjectStateFilters.UNSCHEDULED.getOption(), options.get(5).getText());
        assertEquals(ProjectStateFilters.DISABLED.getOption(), options.get(6).getText());
    }

    public WebElement getSelectedFilterOption() {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        return select.getFirstSelectedOption();
    }

    public void checkProjectFilter(final ProjectStateFilters filterOption,
            List<ProjectInfo> projects) {
        List<ProjectStateFilters> filters =
                Arrays.asList(ProjectStateFilters.DISABLED, ProjectStateFilters.FAILED,
                        ProjectStateFilters.RUNNING, ProjectStateFilters.SCHEDULED,
                        ProjectStateFilters.SUCCESSFUL, ProjectStateFilters.UNSCHEDULED);
        Iterable<ProjectStateFilters> filterOutOptions =
                Iterables.filter(filters, new Predicate<ProjectStateFilters>() {

                    @Override
                    public boolean apply(ProjectStateFilters filter) {
                        if (filter == filterOption)
                            return false;
                        if (filterOption == ProjectStateFilters.DISABLED)
                            return filter != ProjectStateFilters.UNSCHEDULED;
                        return true;
                    }
                });

        checkFilteredProjects(filterOption, projects);
        if (filterOption == ProjectStateFilters.DISABLED)
            checkFilteredProjects(ProjectStateFilters.UNSCHEDULED, projects);

        for (ProjectStateFilters filterOutOption : filterOutOptions) {
            checkFilteredOutProjects(filterOutOption, projects);
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

    public void checkDefaultSearchBox() {
        waitForElementVisible(searchBox);
        assertTrue(searchBox.getAttribute("value").isEmpty());
        assertEquals(searchBox.getAttribute("placeholder"), "Search in project names and ids ...");
    }

    public void checkDeleteSearchKey(String noResultSearchKey) {
        enterSearchKey(noResultSearchKey);
        System.out.println("Empty state message: " + discProjectsList.getEmptyStateMessage());
        waitForElementVisible(deleteSearchKeyButton).click();
        assertTrue(searchBox.getAttribute("value").isEmpty());
        waitForElementVisible(discProjectsList.getRoot());
    }

    public void searchProjectByName(String searchKey) {
        enterSearchKey(searchKey);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertSearchProjectsByName(searchKey);
    }

    public void searchProjectByUnicodeName(String unicodeSearchKey) {
        enterSearchKey(unicodeSearchKey);
        waitForElementVisible(discProjectsList.getRoot());
        discProjectsList.assertSearchProjectByUnicodeName(unicodeSearchKey);
    }

    public void searchProjectById(ProjectInfo project) {
        enterSearchKey(project.getProjectId());
        waitForElementVisible(discProjectsList.getRoot());
        assertEquals(discProjectsList.getNumberOfRows(), 1,
                "Actual project number in search result: " + discProjectsList.getNumberOfRows());
        assertNotNull(discProjectsList.selectProjectWithAdminRole(project));
    }

    public void checkEmptySearchResult(String searchKey) {
        for (ProjectStateFilters projectFilter : ProjectStateFilters.values()) {
            selectFilterOption(projectFilter);
            String expectedEmptySearchResultMessage =
                    projectFilter.getEmptySearchResultMessage().replace("${searchKey}", searchKey);
            enterSearchKey(searchKey);
            System.out.println("Empty Search Result Message: "
                    + discProjectsList.getEmptyStateMessage().trim());
            String actualEmptySearchResultMessage = discProjectsList.getEmptyStateMessage();
            if (projectFilter.equals(ProjectStateFilters.ALL))
                assertEquals(actualEmptySearchResultMessage.trim(),
                        expectedEmptySearchResultMessage);
            else {
                assertTrue(actualEmptySearchResultMessage
                        .contains(expectedEmptySearchResultMessage));
                String emtySearchResultMessageInSpecificState = "Search in all projects";
                assertTrue(actualEmptySearchResultMessage
                        .contains(emtySearchResultMessageInSpecificState));
                discProjectsList.getEmptyState().findElement(BY_ALL_PROJECTS_LINK).click();
                assertEquals(ProjectStateFilters.ALL.getOption(), getSelectedFilterOption()
                        .getText());
            }
        }
    }

    public void searchProjectInSpecificState(ProjectStateFilters projectFilter, ProjectInfo project) {
        selectFilterOption(projectFilter);
        searchProjectByName(project.getProjectName());
        searchProjectById(project);
    }

    private void enterSearchKey(String searchKey) {
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

    private void selectFilterOption(ProjectStateFilters option) {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        select.selectByVisibleText(option.getOption());

        Graphene.waitGui()
                .until()
                .element(
                        By.xpath(XPATH_SELECTED_OPTION.replace("${selectedOption}",
                                option.getOption()))).is().selected();

        System.out.println("Selected filter option:" + option);
    }

    private void checkFilteredOutProjects(ProjectStateFilters filterOutOption,
            List<ProjectInfo> filteredOutProjects) {
        selectFilterOption(filterOutOption);
        waitForElementVisible(discProjectsList.getRoot());
        for (ProjectInfo filteredOutProject : filteredOutProjects) {
            assertNull(discProjectsList.selectProjectWithAdminRole(filteredOutProject),
                    "Project isn't filtered out!");
            System.out.println("Project " + filteredOutProject.getProjectName() + "(id = "
                    + filteredOutProject.getProjectId() + ") is filtered out.");
        }
    }

    private void checkFilteredProjects(ProjectStateFilters filterOption,
            List<ProjectInfo> filteredProjects) {
        System.out.println("Check filter option:" + filterOption);
        selectFilterOption(filterOption);
        waitForElementVisible(discProjectsList.getRoot());
        for (ProjectInfo filteredProject : filteredProjects) {
            assertNotNull(discProjectsList.selectProjectWithAdminRole(filteredProject),
                    "Project doesn't present in filtered list!");
            System.out.println("Project " + filteredProject.getProjectName() + " (id = "
                    + filteredProject.getProjectId() + ") is in filtered list.");
        }
    }
}
