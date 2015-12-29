package com.gooddata.qa.graphene.fragments.profile;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class UserProfilePage extends AbstractFragment {

    public static final By USER_PROFILE_PAGE_LOCATOR = By.cssSelector("#p-profilePage");

    @FindBy(css = ".fullname")
    private WebElement fullname;

    @FindBy(css = ".email a")
    private WebElement email;

    @FindBy(css = ".phone div.value")
    private WebElement phone;

    @FindBy(css = ".company div.value")
    private WebElement company;

    @FindBy(css = ".usersTable")
    private UserVariableTable userVariableTable;

    @FindBy(css = ".item")
    private List<WebElement> recentActivityItems;

    @FindBy(css = ".role")
    private WebElement role;

    public PersonalInfo getUserInfo() {
        return new PersonalInfo()
                .withFullName(waitForElementVisible(fullname).getText())
                .withEmail(waitForElementVisible(email).getText())
                .withCompany(waitForElementVisible(company).getText())
                .withPhoneNumber(waitForElementVisible(phone).getText());
    }

    public List<String> getAllUserVariables() {
        return waitForFragmentVisible(userVariableTable).getAllItems();
    }

    public int getRecentActivityItems() {
        return recentActivityItems.size();
    }

    public boolean isItemDisplayedInRecentActivity(final String itemName) {
        return recentActivityItems.stream()
                .map(e -> e.findElement(By.cssSelector(".title")))
                .map(WebElement::getText)
                .filter(e -> e.equals(itemName))
                .findFirst()
                .isPresent();
    }

    public String getUserRole() {
        return waitForElementPresent(role).getText();
    }

    public class UserVariableTable extends AbstractTable {
        private List<String> getAllItems() {
            return getElementTexts(rows, e -> e.findElement(BY_LINK));
        }
    }
}
