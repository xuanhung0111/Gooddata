package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

public class Header extends AbstractFragment {

    @FindBy(className = "gd-header-account")
    private WebElement accountMenuButton;

    public HeaderAccountMenu openAccountMenu() {
        waitForElementVisible(accountMenuButton).click();

        return Graphene.createPageFragment(
                HeaderAccountMenu.class,
                waitForElementVisible(By.className(HeaderAccountMenu.CLASS_NAME), browser));
    }

    public void logout() {
        openAccountMenu()
            .logout();
    }
}
