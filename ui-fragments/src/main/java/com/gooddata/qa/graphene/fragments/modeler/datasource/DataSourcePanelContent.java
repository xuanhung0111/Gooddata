package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataSourcePanelContent extends AbstractFragment {
    private static final String DATASOURCE_PANEL_CONTENT = "datasource-panel-content";

    @FindBy(className = "datasource-content-connecting")
    private WebElement datasourceConnecting;

    public static DataSourcePanelContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourcePanelContent.class, waitForElementVisible(className(DATASOURCE_PANEL_CONTENT), searchContext));
    }

    public DataSourceDropDownBar  getDropdownDatasource() {
        return DataSourceDropDownBar.getInstance(browser);
    }
}
