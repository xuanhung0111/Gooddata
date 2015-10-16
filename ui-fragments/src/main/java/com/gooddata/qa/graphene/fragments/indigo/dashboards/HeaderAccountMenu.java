package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

public class HeaderAccountMenu extends AbstractFragment {

    public static final String CLASS_NAME = "gd-header-account-dropdown";

    @FindBy(className = "s-logout")
    private WebElement logoutLink;

    public void logout() {
        waitForElementVisible(logoutLink).click();
    }
}
