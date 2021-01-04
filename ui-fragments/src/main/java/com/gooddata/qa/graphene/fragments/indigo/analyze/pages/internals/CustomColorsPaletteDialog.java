package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class CustomColorsPaletteDialog extends AbstractFragment {

    @FindBy(className = "s-cancel")
    private WebElement cancel;

    @FindBy(className = "s-ok")
    private WebElement apply;

    @FindBy(className = "color-picker-component")
    private WebElement colorPalettePicker;

    @FindBy(className = "s-color-picker-hex")
    private WebElement hexColor;

    public static final By ROOT_LOCATOR = className("color-picker-container");
    public static final By HEX_COLOR = cssSelector(".s-color-picker-hex input");

    public static CustomColorsPaletteDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(CustomColorsPaletteDialog.class,
                waitForElementVisible(ROOT_LOCATOR, context));
    }

    public boolean isInputHexColorVisible() {
        return isElementVisible(hexColor);
    }

    public boolean isCancelButtonVisible() {
        return isElementVisible(cancel);
    }

    public boolean isOkButtonVisible() {
        return isElementVisible(apply);
    }

    public boolean isRGBPickerVisible() {
        return isElementVisible(colorPalettePicker);
    }

    public String getCurrentOption() {
        return waitForElementVisible(getRoot().findElement(className("s-current-color"))).getAttribute("style")
                .replaceAll(".*background-color: ([^;]*);.*", "$1").replace(" ", "");
    }

    public String getNewOption() {
        return waitForElementVisible(getRoot().findElement(className("s-new-color"))).getAttribute("style")
                .replaceAll(".*background-color: ([^;]*);.*", "$1").replace(" ", "");
    }

    public AnalysisPage apply() {
        waitForElementPresent(apply).click();
        return AnalysisPage.getInstance(browser).waitForReportComputing();
    }

    public String getCancelButtonText() {
        return waitForElementVisible(cancel).getText();
    }

    public String getOkButtonText() {
        return waitForElementVisible(apply).getText();
    }

    public String getHexText() {
        return waitForElementVisible(cssSelector(".s-color-picker-hex + p"), getRoot()).getText();
    }

    public String getCurrentText() {
        return waitForElementVisible(cssSelector(".s-current-color + span"), getRoot()).getText();
    }

    public String getNewText() {
        return waitForElementVisible(cssSelector(".s-new-color + span"), getRoot()).getText();
    }

    public CustomColorsPaletteDialog setColorCustomPicker(String hexColor) {
        waitForElementPresent(HEX_COLOR, getRoot()).clear();
        getRoot().findElement(HEX_COLOR).sendKeys(hexColor);
        return this;
    }

    public CustomColorsPaletteDialog cancel() {
        waitForElementPresent(cancel).click();
        return this;
    }
}
