package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static java.lang.String.format;
import static org.openqa.selenium.By.xpath;

public class ViewDetailDialog extends AbstractFragment {

    private static final String VIEW_DETAIL_DIALOG = "indigo-table-component";
    private static final String ATTRIBUTE_NAME = "//div[@class='title attribute'][contains(text(),'%s')]";
    private static final String LABEL_NAME = "//div[@class='title label'][contains(text(),'%s')]";
    private static final String ID_ATTRIBUTE_NAME = "//div[contains(text(),'label.%s.%s')]";
    private static final String ID_ATTRIBUTE_OPTIONAL_NAME = "//div[contains(text(),'label.%s.%s.%s')]";
    private static final String DATATYPE_SELECTED = "//div[contains(@class,'selected-row')]//div[@class='title'][contains(text(),'%s')]";
    private static final String DATATYPE_CHANGE = "//div[@class='gd-list-item %s']";
    private static final String DROP_DOWN_DATATYPE = "edit-datatype-dropdown";

    public static ViewDetailDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ViewDetailDialog.class, waitForElementVisible(className(VIEW_DETAIL_DIALOG), searchContext));
    }

    public void editAttributeName(String attribute, String newName) {
        WebElement attributeName = this.getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attribute)));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(attributeName).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newName).sendKeys(Keys.ENTER).build().perform();
    }

    public void editLabelName(String label, String newName) {
        WebElement labelName = this.getRoot().findElement(xpath(format(LABEL_NAME, label)));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(labelName).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newName).sendKeys(Keys.ENTER).build().perform();
    }

    public void editDatatypeOfMainLabel(String dataset, String attribute, String dataTypeText, String dataTypeClass) {
        WebElement attributeId = this.getRoot().findElement(xpath(format(ID_ATTRIBUTE_NAME, dataset, attribute)));
        Actions driverActions = new Actions(browser);
        scrollElementIntoView(attributeId, browser);
        driverActions.moveToElement(attributeId).click().perform();
        WebElement datatype = this.getRoot().findElement(xpath(format(DATATYPE_SELECTED, dataTypeText)));
        scrollElementIntoView(datatype, browser);
        driverActions.moveToElement(datatype).click().perform();
        WebElement datatypeChange = this.getRoot().findElement(xpath(format(DATATYPE_CHANGE, dataTypeClass)));
        scrollElementIntoView(datatypeChange, browser);
        driverActions.moveToElement(datatypeChange).click().perform();
    }

    public void editDatatypeOfOptionalLabel(String dataset, String attribute, String label
            , String dataTypeTextSelected, String dataTypeClass) {
        WebElement labelId = this.getRoot().findElement(xpath(format(ID_ATTRIBUTE_OPTIONAL_NAME,
                dataset, attribute, label)));
        Actions driverActions = new Actions(browser);
        scrollElementIntoView(labelId, browser);
        driverActions.moveToElement(labelId).click().perform();
        WebElement datatype = this.getRoot().findElement(xpath(format(DATATYPE_SELECTED, dataTypeTextSelected)));
        scrollElementIntoView(datatype, browser);
        driverActions.moveToElement(datatype).click().perform();
        waitForElementVisible(this.getRoot().findElement(className(DROP_DOWN_DATATYPE)));
        WebElement datatypeChange = this.getRoot().findElement(xpath(format(DATATYPE_CHANGE, dataTypeClass)));
        scrollElementIntoView(datatypeChange, browser);
        driverActions.moveToElement(datatypeChange).click().perform();
    }

    public void clickOnAttribute(String attribute) {
        WebElement attributeName = this.getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attribute)));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(attributeName).click().build().perform();
    }

    public void clickOnLabel(String label) {
        WebElement labelName = this.getRoot().findElement(xpath(format(LABEL_NAME, label)));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(labelName).click().build().perform();
    }

    public String getTextLabel(String label) {
        WebElement labelName = this.getRoot().findElement(xpath(format(LABEL_NAME, label)));
        return labelName.getText();
    }

    public String getTextDataType(String dataset, String attribute, String dataTypeText) {
        WebElement attributeId = this.getRoot().findElement(xpath(format(ID_ATTRIBUTE_NAME, dataset, attribute)));
        Actions driverActions = new Actions(browser);
        scrollElementIntoView(attributeId, browser);
        driverActions.moveToElement(attributeId).click().perform();
        WebElement datatype = this.getRoot().findElement(xpath(format(DATATYPE_SELECTED, dataTypeText)));
        return datatype.getText();
    }
}
