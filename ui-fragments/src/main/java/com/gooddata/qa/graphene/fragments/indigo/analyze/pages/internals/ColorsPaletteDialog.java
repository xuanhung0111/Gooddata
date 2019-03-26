package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ColorsPaletteDialog extends AbstractFragment {

    @FindBy(className = "s-custom_color")
    private WebElement customColor;

    public static final By ROOT_LOCATOR = className("gd-color-drop-down");

    public static ColorsPaletteDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ColorsPaletteDialog.class,
                waitForElementVisible(ROOT_LOCATOR, context));
    }

    public boolean isActiveColor(String color) {
        WebElement element = waitForCollectionIsNotEmpty(getRoot().findElements(className("gd-color-list-item")))
                .stream().filter(colorItem -> colorItem.getAttribute("style").contains(color)).findFirst().get();
        return element.getAttribute("class").contains("gd-color-list-item-active");
    }

    public boolean isListColorItemsVisible() {
        return isElementVisible(className("s-color-drop-down-list"), getRoot());
    }

    public boolean isBoxShadowVisible() {
        return waitForElementVisible(getRoot().findElement(className("gd-color-list-item-active")))
                .getCssValue("box-shadow").contains("rgba(0, 0, 0, 0.15) 0px 0px 1px 1px");
    }

    public String getCustomColorButtonText() {
        return waitForElementVisible(customColor).getText();
    }

    public String getColor() {
        return waitForElementVisible(getRoot().findElement(className("gd-color-list-item-active"))).getAttribute("style")
                .replaceAll(".*background-color: ([^;]*);.*", "$1").replace(" ", "");
    }

    public ColorsPaletteDialog selectColor(String color) {
        waitForCollectionIsNotEmpty(getRoot().findElements(className("gd-color-list-item")))
                .stream().filter(colorItem -> colorItem.getAttribute("style").contains(color)).findFirst().get().click();
        return this;
    }

    public ColorsPaletteDialog openCustomColorPalette() {
        waitForElementVisible(customColor).click();
        return this;
    }

    public CustomColorsPaletteDialog getCustomColorsPaletteDialog() {
        return CustomColorsPaletteDialog.getInstance(browser);
    }
}
