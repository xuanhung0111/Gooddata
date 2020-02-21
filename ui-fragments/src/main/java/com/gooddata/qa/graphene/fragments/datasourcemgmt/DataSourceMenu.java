package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class DataSourceMenu extends AbstractFragment {
    private static final By MENU_CLASS = By.className("navigation");

    @FindBy(className = "navigation-add-datasource")
    private WebElement addButton;

    @FindBy(className = "create-datasource-dialog")
    private WebElement popupResource;

    @FindBy(className = "s-create-datasource-dialog-item-snowflake")
    private WebElement resource1;

    @FindBy(className = "s-create-datasource-dialog-item-redshift")
    private WebElement resource2;

    @FindBy(className = "s-create-datasource-dialog-item-bigquery")
    private WebElement resource3;

    @FindBy(className = "navigation-list-item")
    private List<WebElement> datasources;

    @FindBy(className = "navigation-list")
    private WebElement datasourceList;

    private static String  navigationList = ".navigation-list";

    public static final DataSourceMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DataSourceMenu.class, waitForElementVisible(MENU_CLASS, searchContext));
    }

    public void selectSnowflakeResource() {
        waitForElementVisible(addButton).click();
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(resource1).click().build().perform();
    }


    public void selectRedshiftResource() {
        waitForElementVisible(addButton).click();
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(resource2).click().build().perform();
    }

    public void selectBigQueryResource() {
        waitForElementVisible(addButton).click();
        waitForElementVisible(popupResource);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(resource3).click().build().perform();
    }

    public List<String> getListDataSources () {
        List<String> originalList = new ArrayList<String>();
        for (WebElement e : datasources) {
            System.out.print(e.getText());
            originalList.add(e.getText());
        }
        return originalList;
    }

    public DataSourceMenu selectDataSource(String title) {
        Actions actions = new Actions(browser);
        waitForElementVisible(datasourceList);
        WebElement element = datasources.stream()
                .filter(items -> items.getText().equals(title))
                .findFirst()
                .get();
        ElementUtils.scrollElementIntoView(element,browser);
        actions.moveToElement(element).click().perform();
        return this;
    }

    public boolean isDataSourceExist(String dataSourceTitle) {
        return getListDataSources().contains(dataSourceTitle);
    }

    public List<String> sortDataSource() {
        List<String> orginalList = new ArrayList<String>();
        orginalList = getListDataSources();
        List<String> tempList = orginalList;
        Collections.sort(tempList);
        return orginalList;
    }
}
