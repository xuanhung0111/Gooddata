package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class CompareApplyMeasure extends AbstractReactDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".s-compare-apply-measures-list";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return ".s-compare-apply-measures-button";
    }

    public CompareApplyMeasure selectByNames(String... names) {
        clearAllCheckedValues();
        addByNames(names);
        return this;
    }

    public CompareApplyMeasure selectAllValues() {
        ensureDropdownOpen();
        waitForElementVisible(className("s-select_all"), getPanelRoot()).click();
        return this;
    }

    public CompareApplyMeasure clearAllCheckedValues() {
        ensureDropdownOpen();
        waitForElementVisible(className("s-clear"), getPanelRoot()).click();
        return this;
    }

    public void apply() {
        waitForElementVisible(className("s-apply"), getPanelRoot()).click();
    }

    public void cancel() {
        waitForElementVisible(className("s-cancel"), getPanelRoot()).click();
    }
}
