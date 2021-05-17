package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

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
        scrollElementIntoView(table, browser);
        action.moveToElement(table.findElement(By.className("type-name"))).click().build().perform();
        waitForElementVisible(table.findElement(By.className("icon-circle-question")));
        action.moveToElement(table.findElement(By.className("icon-circle-question"))).click().build().perform();
        return OverlayWrapper.getInstanceByIndex(browser, 1).getPopUpTable();
    }

    public void dragdropTableToCanvas(String name, String jsFile) {
//        dragDropUseJS(table, jsFile) -> Using in cases can not drag and drop by Selenium and JS
        WebElement from = getTablesByName(name);
        waitForElementVisible(from);
        scrollElementIntoView(from, browser);
        Actions driverActions = new Actions(browser);
        driverActions.clickAndHold(from).moveByOffset(100, 100).perform();
        try {
            WebElement dropZone = waitForElementPresent(By.id("paper-container"), browser);
            driverActions.moveToElement(dropZone).perform();
        } finally {
            driverActions.release().perform();
        }
        waitForPreviewDialogLoadingData();
    }

    public static void waitForPreviewDialogLoadingData() {
        final By loadingIcon = cssSelector(".loading-scroll-wrapper .gd-spinner");
        try {
            Function<WebDriver, Boolean> isLoadingIconVisible = browser -> !isElementVisible(loadingIcon, browser);
            Graphene.waitGui().withTimeout(10, TimeUnit.SECONDS).until(isLoadingIconVisible);
        } catch (TimeoutException e) {
            //do nothing
        }
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
