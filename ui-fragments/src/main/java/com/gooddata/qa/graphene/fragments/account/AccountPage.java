package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForProjectsPageLoaded;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AccountPage extends AbstractFragment {

    private static final By PERSONAL_INFORMATION_DIALOG_LOCATOR = By.cssSelector(".userAccountForm");
    private static final By CHANGE_PASSWORD_DIALOG_LOCATOR = By.cssSelector(".changePasswordDialog");
    private static final By REGIONAL_NUMBER_FORMATTING_DIALOG_LOCATOR = By
            .cssSelector(".c-regionalNumberFormattingDialog");

    @FindBy(css = ".personalInformation a")
    private WebElement personalInformationLink;

    @FindBy(css = ".password a")
    private WebElement changePasswordLink;

    @FindBy(css = ".regionalNumberFormatting a")
    private WebElement regionalNumberFormattingLink;

    @FindBy(css = ".projects a")
    private WebElement activeProjectsLink;

    public PersonalInfoDialog openPersonalInfoDialog() {
        waitForElementVisible(personalInformationLink).click();
        return Graphene.createPageFragment(PersonalInfoDialog.class,
                waitForElementVisible(PERSONAL_INFORMATION_DIALOG_LOCATOR, browser));
    }

    public ChangePasswordDialog openChangePasswordDialog() {
        waitForElementVisible(changePasswordLink).click();
        return Graphene.createPageFragment(ChangePasswordDialog.class,
                waitForElementVisible(CHANGE_PASSWORD_DIALOG_LOCATOR, browser));
    }

    public RegionalNumberFormattingDialog openRegionalNumberFormattingDialog() {
        waitForElementVisible(regionalNumberFormattingLink).click();
        return Graphene.createPageFragment(RegionalNumberFormattingDialog.class,
                waitForElementVisible(REGIONAL_NUMBER_FORMATTING_DIALOG_LOCATOR, browser));
    }

    public void openActiveProjectsPage() {
        waitForElementVisible(activeProjectsLink).click();
        waitForProjectsPageLoaded(browser);
    }

}
