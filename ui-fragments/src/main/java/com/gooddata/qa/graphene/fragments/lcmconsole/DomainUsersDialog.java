package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.*;

public class DomainUsersDialog extends AbstractFragment {

    private static final By DIALOG_CLASS = By.className("gd-dialog");

    @FindBy(className = "table-row")
    private WebElement tableRow;

    @FindBy(className = "table-container")
    private WebElement tableContainer;


    public static final DomainUsersDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DomainUsersDialog.class, waitForElementVisible(DIALOG_CLASS, searchContext));
    }

    public boolean isUserPresent(String userLogin) {
        //we should wait until some row is loaded, it is not enough to wait for table-container
        waitForElementVisible(tableRow);
        return ElementUtils.isElementPresent(getCssSelectorForUserLogin(userLogin), tableContainer);
    }

    private By getCssSelectorForUserLogin(String userLogin) {
        return By.cssSelector(format("[title = '%s']", userLogin));
    }
}
