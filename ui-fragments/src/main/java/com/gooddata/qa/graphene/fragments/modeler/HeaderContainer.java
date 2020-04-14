package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class HeaderContainer extends AbstractFragment {
    private static final String HEADER_CONTAINER = "gd-header-container";

    @FindBy(css = ".s-menu-modeler .active")
    private WebElement buttonModeler;

    @FindBy(className = "s-dic")
    private WebElement buttonDic;

    @FindBy(className = "project-title")
    private WebElement buttonProject;

    @FindBy(className = "gd-header-account-user")
    private WebElement buttonAccount;

    public static HeaderContainer getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                HeaderContainer.class, waitForElementVisible(className(HEADER_CONTAINER), searchContext));
    }

    public void checkHeaderItems() {
        waitForElementVisible(buttonModeler);
        waitForElementVisible(buttonDic);
        waitForElementVisible(buttonProject);
        waitForElementVisible(buttonAccount);
    }

    public WebElement getButtonDic() {
        return buttonDic;
    }
}
