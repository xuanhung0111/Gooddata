package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.DISCProjectFilters;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static org.testng.Assert.*;

public class DISCProjectsPage extends AbstractFragment {

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

    public void checkPagingProjectsPage(String selectedProjectsPerPageOption, int projectsNumber) {
        waitForElementVisible(projectsPerPageOptions);
        Select select = new Select(projectsPerPageOptions);
        select.selectByVisibleText(selectedProjectsPerPageOption);
        assertEquals(selectedProjectsPerPageOption, select.getFirstSelectedOption().getText());
        waitForElementVisible(discProjectsList.getRoot());
        int projectsPerPageNumber = Integer.parseInt(selectedProjectsPerPageOption);
        assertEquals(projectsPerPageNumber, discProjectsList.getNumberOfRows());
        String pagingBarLabelSubString =
                String.format("Showing 1-%d of %d projects", projectsPerPageNumber, projectsNumber);
        System.out.println("Paging bar label: " + pagingBarLabelSubString);
        assertTrue(pagingBarLabel.getText().contains(pagingBarLabelSubString));
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
                assertEquals(projectsNumber - projectsPerPageNumber * (i),
                        discProjectsList.getNumberOfRows());
        }
    }
}
