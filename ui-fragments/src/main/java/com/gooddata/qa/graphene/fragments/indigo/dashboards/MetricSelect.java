package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.utils.ElementUtils.getTooltipFromElement;

public class MetricSelect extends AbstractReactDropDown {

    private static final By TOOLTIP_LOCATOR = By.className("bubble-content");

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay .metrics-list";
    }

    public boolean isNameShortened(String name) {
        return getElementByName(name)
                .findElement(By.className("shortened"))
                .getAttribute("class")
                .contains("is-shortened");
    }

    public String getTooltip(String name) {
        return getTooltipFromElement(getElementByName(name), browser);
    }
}
