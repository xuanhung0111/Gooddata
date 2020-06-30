package com.gooddata.qa.graphene.fragments.disc.process;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

public class ProcessTypeDropdown extends AbstractDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".process-selection-dropdown";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-view-item";
    }

    @Override
    protected void waitForPickerLoaded() {
        // Picker is loaded instantly and no need to wait more
    }

    public ProcessTypeDropdown expand() {
        if (isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public ProcessTypeDropdown selectProcessType(String processType) {
        scrollToProcessTypeInTime(processType, 50, 60000);
        By selector = By.cssSelector(".s-" + simplifyText(processType));
        waitForElementVisible(selector, browser).click();
        return this;
    }

    public ProcessTypeDropdown scrollToProcessTypeInTime(String processType, int range, int time) {
        By selector = By.cssSelector(".s-" + simplifyText(processType));
        ElementUtils.scrollElementIntoViewInTime(By.cssSelector(".ember-view.ember-list-view"), selector, browser, range, time);
        return this;
    }

    public ProcessTypeDropdown selectLatestProcessTypeVersion(String processType, int range) {
        try {
            scrollToProcessTypeInTime(processType, range, 30000);
        } catch (Exception noSuchElement){
            log.info("Scroll down the bottom of the list to select latest version");
        } finally {
            List<WebElement> elements = browser.findElements(By.className("dataset-title"));
            elements.stream().filter(el -> el.getText().contains(processType)).findFirst().get().click();
        }
        return this;
    }

    private boolean isCollapsed() {
        return !isElementVisible(By.cssSelector(getDropdownCssSelector()), browser);
    }
}
