package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.AbstractDialog;
import com.gooddata.qa.graphene.fragments.common.DropDown;

public class ChangeLanguageDialog extends AbstractDialog {

    @FindBy(css = "#selectLanguage button")
    private WebElement selectLanguageButton;

    public static final ChangeLanguageDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ChangeLanguageDialog.class,
                waitForElementVisible(className("changeLanguageDialog"), context));
    }

    public ChangeLanguageDialog selectLanguage(String lang) {
        waitForElementVisible(selectLanguageButton).click();
        DropDown.getInstance(className("language-dropdown"), browser).selectItem(lang);
        return this;
    }

    @Override
    public void saveChange() {
        waitForElementVisible(className("submit-button"), getRoot()).click();
    }
}
