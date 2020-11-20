package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class TextEditorWrapper extends AbstractFragment {
    private static final By TEXT_EDITOR_WRAPPER = By.className("add-label-menu");

        @FindBy(className = "s-add-text-label")
        private WebElement addLabelOption;

        @FindBy(className = "s-add-link-label")
        private WebElement addLinkOption;

        public static final TextEditorWrapper getInstance(SearchContext searchContext) {
            return Graphene.createPageFragment(TextEditorWrapper.class, waitForElementVisible(TEXT_EDITOR_WRAPPER, searchContext));
        }

        public void addLabel(String labelName) {
            Actions driverActions = new Actions(browser);
            driverActions.moveToElement(addLabelOption).click().sendKeys(labelName).sendKeys(Keys.ENTER).build().perform();
        }

        public void addLink(String labelName) {
            Actions driverActions = new Actions(browser);
            driverActions.moveToElement(addLinkOption).click().sendKeys(labelName).sendKeys(Keys.ENTER).build().perform();
        }

        public void clickAddTextLabel() {
            waitForElementVisible(addLabelOption).click();
        }

         public void clickAddLinkLabel() {
            waitForElementVisible(addLinkOption).click();
         }
}
