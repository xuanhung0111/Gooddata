package com.gooddata.qa.boilerplate.fragments;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginBoilerPlateFragment extends AbstractFragment {

    @FindBy
    private WebElement email;

    @FindBy
    private WebElement password;

    @FindBy(xpath = "//button[contains(@type, 'submit')]")
    private WebElement submit;

    private static LoginBoilerPlateFragment instance = null;

    public static final LoginBoilerPlateFragment getInstance(SearchContext context) {
        if (instance == null) {
            instance = Graphene.createPageFragment(LoginBoilerPlateFragment.class,
                    waitForElementVisible(cssSelector(".s-login-form"), context));
        }
        return waitForFragmentVisible(instance);
    }

    public static final LoginBoilerPlateFragment waitForPageLoaded(SearchContext context) {
        return getInstance(context);
    }

    public void login(String username, String password) {
        waitForElementVisible(email).clear();
        waitForElementVisible(this.password).clear();
        email.sendKeys(username);
        this.password.sendKeys(password);
        waitForElementVisible(submit).click();
        waitForElementNotVisible(this.getRoot());
        waitForElementNotVisible(email);
    }
}
