package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.modeler.MainModelContent;
import com.gooddata.qa.graphene.fragments.modeler.Modeler;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.openqa.selenium.By.className;

public class DataSourceSchemaContent extends AbstractFragment {
    private static final String DATASOURCE_SCHEMA_CONTENT = "datasource-schema-content";

    @FindBy(className = "datasource-schema-item")
    private List<WebElement> tables;

    public static DataSourceSchemaContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourceSchemaContent.class, waitForElementVisible(className(DATASOURCE_SCHEMA_CONTENT), searchContext));
    }

    public WebElement getTablesByName(String name) {
        return tables.stream().filter(el -> el.findElement(By.className("type-name")).getText().equals(name)).findFirst().get();
    }

    public BubleContent openPopUpTable(String name) {
        WebElement table = getTablesByName(name);
        Actions action = new Actions(browser);
        action.moveToElement(table.findElement(By.className("type-name"))).click().build().perform();
        waitForElementVisible(table.findElement(By.className("icon-circle-question")));
        action.moveToElement(table.findElement(By.className("icon-circle-question"))).click().build().perform();
        return OverlayWrapper.getInstance(browser).getPopUpTable();
    }

    public void dragdropTableToCanvas(String name, String jsFile) {
        WebElement table = getTablesByName(name);
        dragDropUseJS(table, jsFile);
    }

    public boolean isTableExisting(String name) {
        long size = tables.stream().filter(el -> el.findElement(By.className("type-name")).getText().equals(name)).count();
        return size == 0 ? false : true;
    }

    public int getNumOfTables() {
        return tables.size();
    }

    public void dragDropUseJS(WebElement source, String jsFile) {
        try {
            WebElement target = waitForElementPresent(By.id("paper-container"), browser);
            BrowserUtils.runScript(browser, jsFile, source, target);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                Thread.sleep(5000);
            }catch (Exception ex) {

            }
        }
    }
}
