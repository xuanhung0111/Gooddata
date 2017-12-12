package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertTrue;

public class ClientDetailDialog extends AbstractFragment {

    private static final By DIALOG_CLASS = By.className("gd-dialog");
    private static final By USER_TABLE_CLASS = By.className("table-body");
    private static final By USER_EMAIL_CSS = By.cssSelector(".table-row span:nth-child(1)");

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeButton;

    @FindBy(className = "gd-input-field")
    private WebElement searchInputField;

    @FindBy(className = "table-row")
    private WebElement tableRow;

    @FindBy(className = "table-container")
    private WebElement tableContainer;

    @FindBy(css = ".table-filter h3")
    private WebElement userCountHeader;

    public static ClientDetailDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ClientDetailDialog.class, waitForElementVisible(DIALOG_CLASS, searchContext));
    }

    public void close() {
        waitForElementVisible(closeButton).click();
    }

    public void filterUsers(String searchValue) {
        waitForElementVisible(searchInputField).sendKeys(searchValue);
    }

    public boolean isUserPresent(String userEmail) {
        waitForElementVisible(tableRow);
        return isElementPresent(getCssSelectorForUser(userEmail), tableContainer);
    }

    public void waitForUserIsNotPresent(String userEmail) {
        waitForElementNotPresent(getCssSelectorForUser(userEmail));
    }

    public int getUserCount() {
        waitForElementVisible(tableRow);
        final String usersString = waitForElementVisible(userCountHeader).getText();
        final Matcher usersMatcher = Pattern.compile("Users \\(([0-9]+)\\)").matcher(usersString);
        assertTrue(usersMatcher.matches(), "Cannot get user count, header pattern not matched");

        return Integer.valueOf(usersMatcher.group(1));
    }

    public List<String> getAllUserEmails() {
        waitForElementVisible(tableRow);
        final List<WebElement> usersElements = waitForElementVisible(USER_TABLE_CLASS, tableContainer).findElements(USER_EMAIL_CSS);
        return usersElements.stream()
                .map(WebElement::getText)
                .collect(toList());
    }

    private By getCssSelectorForUser(String userEmail) {
        return By.cssSelector(format("[title = '%s']", userEmail));
    }
}
