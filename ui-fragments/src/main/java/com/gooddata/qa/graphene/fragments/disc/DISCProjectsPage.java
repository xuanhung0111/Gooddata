package com.gooddata.qa.graphene.fragments.disc;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.enums.disc.ProjectStateFilters;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

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

    public Select getProjectFilterSelect() {
        waitForElementVisible(projectFilter);
        return new Select(projectFilter);
    }

    public WebElement getSelectedFilterOption() {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        return select.getFirstSelectedOption();
    }

    public Select getProjectsPerPageSelect() {
        waitForElementVisible(projectsPerPageOptions);
        return new Select(projectsPerPageOptions);
    }

    public String getPagingBarLabel() {
        return waitForElementVisible(pagingBarLabel).getText();
    }

    public WebElement getPrevPageButton() {
        return waitForElementVisible(prevPageButton);
    }

    public WebElement getNextPageButton() {
        return waitForElementVisible(nextPageButton);
    }

    public List<WebElement> getProjectPageNumber() {
        return projectPageNumber;
    }

    public WebElement getSearchBox() {
        return waitForElementVisible(searchBox);
    }

    public void deleteSearchKey() {
        waitForElementVisible(deleteSearchKeyButton).click();
    }

    public WebElement getAllProjectLinkInEmptyState() {
        return discProjectsList.getEmptyState().findElement(BY_ALL_PROJECTS_LINK);
    }

    public void enterSearchKey(String searchKey) {
        waitForElementVisible(searchBox).clear();
        waitForElementVisible(searchBox).sendKeys(searchKey);
        System.out.println("Enter search key: " + searchBox.getAttribute("value"));
        waitForElementVisible(searchButton).click();
    }

    public void selectFilterOption(ProjectStateFilters option) {
        waitForElementVisible(projectFilter);
        Select select = new Select(projectFilter);
        select.selectByVisibleText(option.getOption());

        Graphene.waitGui().until()
                .element(By.xpath(XPATH_SELECTED_OPTION.replace("${selectedOption}", option.getOption()))).is()
                .selected();

        System.out.println("Selected filter option:" + option);
    }

    public void waitForSearchingProgress() {
        try {
            waitForElementVisible(searchingProgress);
        } catch (NoSuchElementException ex) {
            System.out.println("Searching progress doesn't display, please check the search result...");
        }
    }
}
