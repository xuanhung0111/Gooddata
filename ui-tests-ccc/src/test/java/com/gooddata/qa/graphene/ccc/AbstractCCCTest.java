package com.gooddata.qa.graphene.ccc;

import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.AbstractTest;
import com.gooddata.qa.graphene.fragments.ccc.LoginFragment;
import com.gooddata.qa.utils.graphene.Screenshots;

public class AbstractCCCTest extends AbstractTest {

    protected static final By BY_DIV_LOGGED_USER = By.cssSelector("div.user");
    protected static final By BY_DIV_PAGE_PROCESSES = By.cssSelector("div.page-processes");

    @FindBy(xpath = "//div[contains(@class, 'login-page')]")
    protected LoginFragment loginFragment;

    @BeforeClass
    public void initStartPage() {
        startPage = "admin/dataload/";
    }

    protected void login(String username, String password) {
        waitForElementVisible(loginFragment.getRoot());
        loginFragment.login(username, password);
        waitForElementVisible(BY_DIV_LOGGED_USER);
        waitForElementVisible(BY_DIV_PAGE_PROCESSES);
        Screenshots.takeScreenshot(browser, "CCC-login", this.getClass());
    }

}
