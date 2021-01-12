package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataSourceDropDownBar extends AbstractFragment {
    private static final String DATASOURCE_DROP_DOWN_BAR = "datasource-dropdown-bar";

    @FindBy(className = "dropdown-button")
    private WebElement dropdownButton;

    public static DataSourceDropDownBar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourceDropDownBar.class, waitForElementVisible(className(DATASOURCE_DROP_DOWN_BAR), searchContext));
    }

    public String getTextButton() {
        return dropdownButton.getText();
    }

    public void  selectDatasource(String dsName) {
        dropdownButton.click();
        OverlayWrapper.getInstanceByIndex(browser, 1).getDSDropdown().selectDatasource(dsName);
    }
}
