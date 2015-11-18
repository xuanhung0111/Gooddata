package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel.LOCATOR;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public abstract class AbstractFilterFragment extends AbstractFragment {

    @FindBy(css = ".s-btn-apply")
    private WebElement applyButton;

    @FindBy(css = " s-btn-cancel")
    private WebElement cancelButton;

    public void apply() {
        waitForElementVisible(applyButton).click();
    }

    public void cancel() {
        waitForElementVisible(cancelButton).click();
    }

    public void searchAndSelectItem(String item) {
        Graphene.createPageFragment(SelectItemPopupPanel.class, waitForElementVisible(LOCATOR, browser))
                .searchAndSelectItem(item);
    }

    public abstract void addFilter(FilterItem filterItem);
}
