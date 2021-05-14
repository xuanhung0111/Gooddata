package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
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

public class DataMapping extends AbstractFragment {
    private static final By DATA_MAPPING = By.className("model-mapping");
    private static final String SOURCE_COLUMN_WARNING = "s-source-column-warning";

    @FindBy(className = "fixedDataTableRowLayout_rowWrapper")
    List<WebElement> rows;

    @FindBy(className = SOURCE_COLUMN_WARNING)
    private WebElement sourceColumnWarning;

    @FindBy(className = "column-not-exist")
    private WebElement columnNotExist;

    public static DataMapping getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataMapping.class, waitForElementVisible(DATA_MAPPING, searchContext));
    }

    public WebElement getRowByName(String labelName, String type) {
        for(WebElement row: rows) {
            if (row.findElements(By.className(type)).size() > 0 && row.findElement(By.className(type)).getText().equals(labelName)) {
                scrollElementIntoView(row, browser);
                return row;
            }
        }
        return null;
    }

    public String getSourceColumnByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        WebElement sourceColumn = row.findElement(By.className("s-editable-label"));
        return sourceColumn.getText();
    }

    public String getWarningSourceColumnByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        WebElement sourceColumn = row.findElement(By.cssSelector(".sourceColumnWarning .s-editable-label"));
        return sourceColumn.getText();
    }

    public String getSourceTypeByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        if (type == "date") {
            return row.findElement(By.cssSelector(".model-mapping-source-type .s-editable-label")).getText();
        } else {
           return row.findElement(By.className("model-mapping-source-type")).getText();
        }
    }

    public  DataMapping editDateFormatByName(String name, String newFormat) {
        WebElement row = getRowByName(name, SOURCE_TYPE.REFERENCE.getName());
        WebElement format = row.findElement(By.cssSelector(".model-mapping-source-type .s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(format).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newFormat).sendKeys(Keys.ENTER).build().perform();
        return this;
    }

    public DataMapping editSourceColumnByName(String name, String type, String newName, boolean isMapping) {
        WebElement row = getRowByName(name, type);
        WebElement sourceColumn = (isMapping == true) ? row.findElement(By.className("s-editable-label"))
                : row.findElement(By.cssSelector(".sourceColumnWarning .s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(sourceColumn).moveByOffset(5,5).click().pause(1000).sendKeys(Keys.DELETE)
                .sendKeys(newName).pause(1000).sendKeys(Keys.ENTER).pause(1000).build().perform();
        return this;
    }

    public DataMapping editDistributedLoadMapping(String newName, boolean isMapping) {
        WebElement row = getRowByName("Distributed Load", SOURCE_TYPE.DISTRIBUTED_LOAD.getName());
        WebElement sourceColumn = (isMapping == true) ? row.findElement(By.className("s-editable-label"))
                : row.findElement(By.cssSelector(".sourceColumnWarning .s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(sourceColumn).moveByOffset(5,5).click().pause(1000).sendKeys(Keys.DELETE)
                .sendKeys(newName).pause(1000).sendKeys(Keys.ENTER).build().perform();
        return this;
    }

    public String getTextWarning() {
        WebElement warningIcon = waitForElementPresent(columnNotExist);
        Actions action = new Actions(browser);
        action.moveToElement(warningIcon).build().perform();
        waitingTooltipDisplay();
        return browser.findElement(By.className(SOURCE_COLUMN_WARNING)).getText();
    }

    public void waitingTooltipDisplay() {
        final By tooltip = className(SOURCE_COLUMN_WARNING);
        try {
            Function<WebDriver, Boolean> isDisplayTooltip = browser -> isElementVisible(tooltip, browser);
            Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(isDisplayTooltip);
        } catch (TimeoutException e) {
            //do nothing
        }
    }

    public DataMapping editIncrementalLoadMapping(String newName, boolean isMapping) {
        WebElement row = getRowByName("Incremental Load", SOURCE_TYPE.INCREMENTAL_LOAD.getName());
        WebElement sourceColumn = (isMapping == true) ? row.findElement(By.className("s-editable-label"))
                : row.findElement(By.cssSelector(".sourceColumnWarning .s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(sourceColumn).click().pause(1000).sendKeys(Keys.DELETE)
                .sendKeys(newName).pause(1000).sendKeys(Keys.ENTER).build().perform();
        return this;
    }

    public DataMapping editDeletedRowsMapping(String newName, boolean isMapping) {
        WebElement row = getRowByName("Deleted rows", SOURCE_TYPE.DELETED_ROWS.getName());
        WebElement sourceColumn = (isMapping == true) ? row.findElement(By.className("s-editable-label"))
                : row.findElement(By.cssSelector(".sourceColumnWarning .s-editable-label"));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(sourceColumn).click().pause(1000).sendKeys(Keys.DELETE)
                .sendKeys(newName).pause(1000).sendKeys(Keys.ENTER).build().perform();
        return this;
    }

    public List<String> getDropdownSourceColumnByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        row.findElement(By.className(type)).click();
        Actions driverActions = new Actions(browser);
        WebElement sourceType = row.findElement(By.className("s-editable-label"));
        driverActions.moveToElement(sourceType).click().build().perform();
        return OverlayWrapper.getInstanceByIndex(browser, 2).getIndigoTableDropDown().getListDropdownOption();
    }

    public List<String> getDropdownDateFormatByName(String name, String type) {
        WebElement row = getRowByName(name, type);
        Actions driverActions = new Actions(browser);
        WebElement sourceType = row.findElement(By.cssSelector(".model-mapping-source-type .s-editable-label"));
        driverActions.moveToElement(sourceType).click().build().perform();
        return OverlayWrapper.getInstanceByIndex(browser, 2).getIndigoTableDropDown().getListDropdownOption();
    }

    public String getWarningMessage(String labelName, String type) {
        WebElement row = getRowByName(labelName, type);
        WebElement warningIcon = row.findElement(By.className("icon-warning"));
        getActions().moveToElement(warningIcon).pause(2000).build().perform();
        return browser.findElement(By.cssSelector(".bubble-content .content  span")).getText();
    }

    public enum SOURCE_TYPE{
        ATTRIBUTE("attribute"),
        LABEL("label"),
        FACT("fact"),
        REFERENCE("reference"),
        DISTRIBUTED_LOAD("distributed-load"),
        INCREMENTAL_LOAD("incremental-load"),
        DELETED_ROWS("deleted-rows");

        private final String type;

        private SOURCE_TYPE(String type) {
            this.type = type;
        }

        public String getName() {
            return type;
        }
    }
}
