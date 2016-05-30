package com.gooddata.qa.graphene.fragments.i18n;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.tagName;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class LocalizationPage extends AbstractFragment {

    private static final String LANGUAGE_BUTTON_LOCATOR = ".s-loc-button-enable[data-locale='${code}']";

    @FindBy(className = "s-loc-button-disable")
    private WebElement disableLocaleButton;

    public static LocalizationPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(LocalizationPage.class, waitForElementPresent(tagName("body"), context));
    }

    public void selectLanguge(String code) {
        WebElement button = waitForElementVisible(cssSelector(LANGUAGE_BUTTON_LOCATOR.replace("${code}", code)), browser);
        button.click();
        waitForElementVisible(disableLocaleButton);

        Predicate<WebDriver> buttonSelected = browser -> button.getAttribute("class").contains("button-positive");
        Graphene.waitGui().until(buttonSelected);
    }
}
