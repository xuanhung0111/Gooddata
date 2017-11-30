package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;

public class DomainProjectsDialog extends AbstractFragment {

    private static final By DIALOG_CLASS = By.className("gd-dialog");

    @FindBy(className = "table-row")
    private WebElement tableRow;

    @FindBy(className = "table-container")
    private WebElement tableContainer;

    @FindBy(className = "gd-input-field")
    private WebElement searchInputField;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeButton;

    @FindBy(xpath = "//h2[starts-with(., 'Projects')]")
    private WebElement header;

    public static final DomainProjectsDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DomainProjectsDialog.class, waitForElementVisible(DIALOG_CLASS, searchContext));
    }

    public int getNumberOfProjects() {
        final String headerText = waitForElementVisible(header).getText();
        return Integer.parseInt(StringUtils.substringBetween(headerText, "(", ")"));
    }

    public void closeDialog() {
        waitForElementVisible(closeButton).click();
    }

    public void filterProject(String searchValue) {
        waitForElementVisible(searchInputField).sendKeys(searchValue);
    }

    public boolean isProjectPresent(String projectName) {
        waitForElementVisible(tableRow);
        return ElementUtils.isElementPresent(getCssSelectorForProject(projectName), tableContainer);
    }

    public void waitForProjectIsNotPresent(String projectName) {
        waitForElementNotPresent(getCssSelectorForProject(projectName));
    }

    private By getCssSelectorForProject(String projectName) {
        return By.cssSelector(format("[title = '%s']", projectName));
    }
}
