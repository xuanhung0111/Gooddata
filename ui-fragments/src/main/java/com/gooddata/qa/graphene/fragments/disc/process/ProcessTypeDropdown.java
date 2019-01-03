package com.gooddata.qa.graphene.fragments.disc.process;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import java.util.concurrent.TimeUnit;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
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
        JavascriptExecutor js = (JavascriptExecutor) browser;
        By selector = By.cssSelector(".s-" + simplifyText(processType));

        Graphene.waitGui().withTimeout(60, TimeUnit.SECONDS).pollingEvery(1, TimeUnit.SECONDS).until(browser -> {
            js.executeScript("arguments[0].scrollTop = arguments[1];",
                    browser.findElement(By.cssSelector(".ember-view.ember-list-view")), 200);
            return isElementPresent(selector, browser);
        });

        waitForElementVisible(By.cssSelector(".s-" + simplifyText(processType)), browser).click();
        return this;
    }

    private boolean isCollapsed() {
        return !isElementVisible(By.cssSelector(getDropdownCssSelector()), browser);
    }

}
