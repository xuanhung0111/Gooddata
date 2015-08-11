package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class FilterPanelRow extends AbstractFragment {

    @FindBy(className = "selectOnly")
    private WebElement selectOnly;

    @FindBy(tagName = "label")
    private WebElement label;

    @FindBy(css = "input[type=checkbox]")
    private WebElement checkbox;

    public WebElement getSelectOnly() {
    	return waitForElementVisible(selectOnly);
    }
    
    public boolean isSelectOnlyDisplayed() {
    	return selectOnly.isDisplayed();
    }

    public WebElement getCheckbox() {
        return checkbox;
    }

    public WebElement getLabel() {
        return label;
    }

    public boolean isSelected() {
        String checked = getCheckbox().getAttribute("checked");
        return checked != null && checked.contains("true");
    }

    public String getText() {
        return waitForElementVisible(By.tagName("span"), getRoot()).getText().trim();
    }
}
