package com.gooddata.qa.graphene.fragments.account;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.makeSureNoPopupVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class AccountCard extends AbstractFragment {

    private static final By LOCATOR = By.className("c-accountCard");

    @FindBy(css = ".fullName")
    private WebElement fullName;

    @FindBy(css = ".contact")
    private WebElement contactInfo;

    public static final AccountCard getInstance(SearchContext context) {
        return Graphene.createPageFragment(AccountCard.class, waitForElementVisible(LOCATOR, context));
    }

    public static void makeDismiss() {
        makeSureNoPopupVisible(LOCATOR);
    }

    public PersonalInfo getUserInfo() {
        String contacts = waitForElementVisible(contactInfo).getText();
        PersonalInfo info = new PersonalInfo()
                .withFullName(waitForElementVisible(fullName).getText())
                .withEmail(contactInfo.findElement(By.tagName("span")).getAttribute("title"));

        if (contacts.contains("phone:")) {
            info.withPhoneNumber(contacts.substring(contacts.lastIndexOf(":") + 2));
        }

        return info;
    }

}
