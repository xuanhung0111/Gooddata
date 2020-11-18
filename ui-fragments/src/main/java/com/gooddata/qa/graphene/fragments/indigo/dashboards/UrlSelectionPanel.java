package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import java.util.List;

public class UrlSelectionPanel extends AbstractReactDropDown {
    
    public static By ROOT = className("s-gd-drill-to-url-body");

    @FindBy(className = "gd-parameter-title")
        private List<WebElement> titleHyperlink;
    
    @FindBy(className = "s-drill-to-custom-url-button")
        private WebElement addCustomUrlButton;

    @Override
    public AbstractReactDropDown selectByName(String name) {
        titleHyperlink.stream().filter(element -> waitForElementVisible(element).getText().contains(name)).findFirst().get().click();
        return this;
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-parameter-title";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.gd-dropdown";
    }

    public static UrlSelectionPanel getInstance(SearchContext searchContext) {
        WebElement root = waitForElementVisible(ROOT, searchContext);
        return Graphene.createPageFragment(UrlSelectionPanel.class, root);
    }

    public UrlSelectionPanel addCustomUrl() {
        waitForElementVisible(addCustomUrlButton).click();
        return this;
    }

    public UrlSelectionPanel getWarningTexr() {
        waitForElementVisible(addCustomUrlButton).click();
        return this;
    }
}
