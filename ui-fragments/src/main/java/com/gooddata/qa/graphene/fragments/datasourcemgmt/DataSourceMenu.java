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

import static com.gooddata.qa.graphene.utils.WaitUtils.*;

public class DataSourceMenu extends AbstractFragment {
    private static final By MENU_CLASS = By.className("navigation");

    @FindBy(className = "navigation-add-datasource")
    private WebElement addButton;

    @FindBy(className = "navigation-list-item")
    private List<WebElement> datasources;

    @FindBy(className = "navigation-list")
    private WebElement datasourceList;

    private static String navigationList = ".navigation-list";

    public static final DataSourceMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(DataSourceMenu.class, waitForElementVisible(MENU_CLASS, searchContext));
    }

    public void selectSnowflakeResource() {
        waitForElementVisible(addButton).click();
        OverlayWrapper.getInstance(browser).selectSnowflakeItem();
    }

    public void selectRedshiftResource() {
        waitForElementVisible(addButton).click();
        OverlayWrapper.getInstance(browser).selectRedshiftItem();
    }

    public void selectBigQueryResource() {
        waitForElementVisible(addButton).click();
        OverlayWrapper.getInstance(browser).selectBigqueryItem();
    }

    public void selectS3DataSource() {
        waitForElementVisible(addButton).click();
        OverlayWrapper.getInstance(browser).selectS3Item();
    }

    public void selectGenericDataSource() {
        waitForElementVisible(addButton).click();
        OverlayWrapper.getInstance(browser).selectGenericItem();
    }

    public List<String> getListDataSources() {
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
        waitForElementVisible(By.className("navigation-list-item"), browser);
        WebElement element = datasources.stream()
                .filter(items -> items.getText().equals(title))
                .findFirst()
                .get();
        ElementUtils.scrollElementIntoView(element, browser);
        actions.moveToElement(element).click().perform();
        return this;
    }

    public DataSourceMenu waitForDatasourceNotVisible(String title) {
        waitForElementVisible(datasourceList);
        if (isDataSourceExist(title)) {
            waitForElementNotVisible(datasources.stream()
                    .filter(items -> items.getText().equals(title))
                    .findFirst()
                    .get());
        }
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
