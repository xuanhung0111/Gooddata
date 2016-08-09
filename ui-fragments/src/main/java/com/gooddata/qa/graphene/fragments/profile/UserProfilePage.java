package com.gooddata.qa.graphene.fragments.profile;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.AbstractTable;

public class UserProfilePage extends AbstractFragment {

    public static final By USER_PROFILE_PAGE_LOCATOR = By.cssSelector("#p-profilePage");
    private static final By PHONE_LOCATOR = cssSelector(".phone div.value");
    private static final By COMPANY_LOCATOR = cssSelector(".company div.value");

    @FindBy(css = ".fullname")
    private WebElement fullname;

    @FindBy(css = ".email a")
    private WebElement email;

    @FindBy(css = ".usersTable")
    private UserVariableTable userVariableTable;

    @FindBy(css = ".item")
    private List<WebElement> recentActivityItems;

    @FindBy(css = ".role")
    private WebElement role;

    public PersonalInfo getUserInfo() {
        PersonalInfo info = new PersonalInfo()
                .withFullName(waitForElementVisible(fullname).getText())
                .withEmail(waitForElementVisible(email).getText());

        if (isElementVisible(COMPANY_LOCATOR, getRoot())) {
            info.withCompany(waitForElementVisible(COMPANY_LOCATOR, getRoot()).getText());
        }

        if (isElementVisible(PHONE_LOCATOR, getRoot())) {
            info.withPhoneNumber(waitForElementVisible(PHONE_LOCATOR, getRoot()).getText());
        }

        return info;
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
