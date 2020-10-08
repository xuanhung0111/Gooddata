package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class DashboardSelectionPanel extends AbstractReactDropDown {

    public static By ROOT = className("s-dashboards-dropdown-body");
    
    @Override
    public AbstractReactDropDown selectByName(String name) {
        getElementByName(name).click();
        return this;
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return "s-choose_dashboard_";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item";
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.dropdown-body";
    }

    public static DashboardSelectionPanel getInstance(SearchContext searchContext) {
        WebElement root = waitForElementVisible(ROOT, searchContext);
        return Graphene.createPageFragment(DashboardSelectionPanel.class, root);
    }

}
