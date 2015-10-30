package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public abstract class AbstractFilterFragment extends AbstractFragment {

    private static final By BACK_BUTTON_LOCATOR = By.className("s-backButton");

    @FindBy(css = ".s-btn-apply")
    private WebElement applyButton;

    @FindBy(css = ".s-btn-cancel")
    private WebElement cancelButton;

    public void apply() {
        waitForElementVisible(applyButton).click();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }

    public void searchAndSelectItem(String item) {
        Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser))
                .searchAndSelectItem(item);
    }

    public abstract void addFilter(FilterItem filterItem);

    public void goBack() {
        waitForElementVisible(BACK_BUTTON_LOCATOR, browser).click();
    }
}
