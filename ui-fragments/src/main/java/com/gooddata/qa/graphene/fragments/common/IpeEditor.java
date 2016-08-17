package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class IpeEditor extends AbstractFragment {

    @FindBy(tagName = "input")
    private WebElement input;

    @FindBy(className = "s-ipeSaveButton")
    private WebElement saveButton;

    public static final IpeEditor getInstance(SearchContext context) {
        return Graphene.createPageFragment(IpeEditor.class,
                waitForElementVisible(cssSelector(".c-ipeEditor[style*='display: block']"), context));
    }

    public void setText(String text) {
        waitForElementVisible(input).clear();
        input.sendKeys(text);
        waitForElementVisible(saveButton).click();
    }
}
