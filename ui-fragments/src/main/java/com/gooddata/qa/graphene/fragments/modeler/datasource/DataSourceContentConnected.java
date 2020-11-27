package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataSourceContentConnected extends AbstractFragment {
    private static final String DATASOURCE_CONTENT_CONNECTED = "datasource-content-connected";

    @FindBy(css = ".datasource-search-bar input")
    private WebElement searchBar;

    @FindBy(css = ".datasource-search-bar .s-input-clear")
    private WebElement clearIcon;

    public static DataSourceContentConnected getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourceContentConnected.class, waitForElementVisible(className(DATASOURCE_CONTENT_CONNECTED), searchContext));
    }

    public DataSourceSchema getDatasourceSchema() {
        return DataSourceSchema.getInstance(browser);
    }

    public void searchTable(String text) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(searchBar).click().sendKeys(text).perform();
    }

    public void clearSearchText() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(searchBar).click().perform();
        driverActions.moveToElement(clearIcon).click().perform();
    }
}
