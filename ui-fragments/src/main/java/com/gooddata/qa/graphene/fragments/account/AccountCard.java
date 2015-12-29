package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.account.PersonalInfo;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AccountCard extends AbstractFragment {

    @FindBy(css = ".fullName")
    private WebElement fullName;

    @FindBy(css = ".contact")
    private WebElement contactInfo;

    public PersonalInfo getUserInfo() {
        String contacts = waitForElementVisible(contactInfo).getText();
        return new PersonalInfo()
                .withFullName(waitForElementVisible(fullName).getText())
                .withEmail(contactInfo.findElement(By.tagName("span")).getAttribute("title"))
                .withPhoneNumber(contacts.substring(contacts.lastIndexOf(":") + 2));
    }

}
