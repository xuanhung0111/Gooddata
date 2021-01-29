package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollBarIconToViewElement;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static java.lang.String.format;
import static org.openqa.selenium.By.xpath;

public class ViewDetailDialog extends AbstractFragment {

    @FindBy(className = "label")
    List<WebElement> listLabels;

    @FindBy(className = "attribute")
    List<WebElement> listAttributes;

    @FindBy(className = "fact")
    List<WebElement> listFacts;

    @FindBy(className= "data-type")
    List<WebElement> listDatatypes;

    @FindBy(className = "s-add_label")
    WebElement addLabelButton;

    @FindBy(className = "s-delete")
    WebElement deleteButton;

    @FindBy(className = "selected-row")
    WebElement selectedRow;

    @FindBy(className = "s-navigatedown")
    WebElement navigateDownButton;

    private static final String VIEW_DETAIL_DIALOG = "indigo-table-component";
    private static final String ATTRIBUTE_NAME = "//div[@class='title attribute'][contains(text(),'%s')]";
    private static final String ATTRIBUTE_PRIMARY = "//div[@class='title primary-key'][contains(text(),'%s')]";
    private static final String FACT_NAME = "//div[@class='title fact'][contains(text(),'%s')]";
    private static final String LABEL_NAME = "//div[@class='title label label-text'][contains(text(),'%s')]";
    private static final String LABEL_LINK_NAME = "//div[@class='title label label-link'][contains(text(),'%s')]";
    private static final String DATATYPE_CHANGE = "//div[@class='gd-list-item %s']";

    public static ViewDetailDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ViewDetailDialog.class, waitForElementVisible(className(VIEW_DETAIL_DIALOG), searchContext));
    }

    public void editAttributeName(String attribute, String newName) {
        WebElement attributeName = this.getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attribute)));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(attributeName).click().pause(1000).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                .pause(1000).sendKeys(Keys.DELETE).sendKeys(newName).sendKeys(Keys.ENTER).build().perform();
    }

    public boolean isAttributeExist(String attribute) {
        List<WebElement> attributeList = this.getRoot().findElements(xpath(format(ATTRIBUTE_NAME, attribute)));
        if(attributeList.isEmpty()) return false;
        return true;
    }

    public boolean isFactExist(String fact) {
        List<WebElement> factList = this.getRoot().findElements(xpath(format(FACT_NAME, fact)));
        if(factList.isEmpty()) return false;
        return true;
    }

    public boolean isLabelAttributeExist(String attribute) {
        List<WebElement> attributeList = this.getRoot().findElements(xpath(format(LABEL_NAME, attribute)));
        if (attributeList.isEmpty()) return false;
        return true;
    }

    public boolean isRowSelected(String label) {
        return selectedRow.findElements(By.xpath(format(LABEL_NAME, label))).size() != 0? true : false;
    }

    public void editLabelName(String label, String newName) {
        WebElement labelName = this.getRoot().findElement(xpath(format(LABEL_NAME, label)));
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(labelName).click().pause(1000).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE)
                .sendKeys(newName).sendKeys(Keys.ENTER).build().perform();
    }

    public void editDatatypeOfLabel(String attribute, String dataTypeClass) {
        int index = getIndexByLabel(attribute);
        Actions driverActions = new Actions(browser);
        WebElement datatype = getDatatypeElementByIndex(index);
        scrollElementIntoView(datatype, browser);
        driverActions.moveToElement(datatype).click().perform();
        WebElement datatypeChange = this.getRoot().findElement(xpath(format(DATATYPE_CHANGE, dataTypeClass)));
        scrollElementIntoView(datatypeChange, browser);
        driverActions.moveToElement(datatypeChange).click().perform();
    }

    public void addNewLabel(String attribute, String labelName){
        WebElement attributeName = this.getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attribute)));
        processAddNewLabel(labelName, attributeName);
    }

    public void addNewLabelTypeHyperlink(String attribute, String labelName) {
        WebElement attributeName = this.getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attribute)));
        hoverOnElementByJS(attributeName);
        waitForElementVisible(addLabelButton);
        getActions().moveToElement(addLabelButton).click().build().perform();
        OverlayWrapper.getInstanceByIndex(browser,1).getTextEditorWrapper().addLink(labelName);
    }

    public void processAddNewLabel(String labelName, WebElement attributeElement){
        hoverOnElementByJS(attributeElement);
        waitForElementVisible(addLabelButton);
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(addLabelButton).click().build().perform();
        OverlayWrapper.getInstanceByIndex(browser,1).getTextEditorWrapper().addLabel(labelName);
    }

    public void addNewLabelForPrimaryKey(String attribute, String labelName){
        WebElement attributeName = this.getRoot().findElement(xpath(format(ATTRIBUTE_PRIMARY, attribute)));
        processAddNewLabel(labelName, attributeName);
    }

    public void deleteLabel(String labelName) {
        WebElement label = this.getRoot().findElement(xpath(format(LABEL_NAME, labelName)));
        hoverOnElementByJS(label);
        waitForElementVisible(deleteButton);
        deleteButton.click();
    }

    public void deleteAttribute(String attributeName) {
        WebElement attribute = this.getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attributeName)));
        hoverOnElementByJS(attribute);
        waitForElementVisible(deleteButton);
        deleteButton.click();
    }

    public String getTextLabel(String label) {
        WebElement labelName = this.getRoot().findElement(xpath(format(LABEL_NAME, label)));
        return labelName.getText();
    }

    public String getTextDataType(String attribute) {
        int index = getIndexByLabel(attribute);
        WebElement datatype = getDatatypeElementByIndex(index);
        return datatype.getText();
    }

    public int getIndexByLabel(String labelName) {
        int index = -1;
        for(WebElement label: listLabels) {
            if(label.getText().equals(labelName)) {
                index =  listLabels.indexOf(label);
            }
        }
        return index;
    }

    public WebElement getDatatypeElementByIndex(int index) {
        return listDatatypes.get(index);
    }

    public int getSizeDatatype() {
        return listDatatypes.size();
    }

    public int getNumberOfAttributes() {
        return listAttributes.size();
    }

    public int getNumberOfFacts() {
        return listFacts.size();
    }

    private void hoverOnElementByJS(WebElement element) {
        String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents')" +
                ";evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} " +
                "else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        JavascriptExecutor executor = (JavascriptExecutor) browser;
        executor.executeScript(mouseOverScript, element);
    }

    public String getIdentifierOfAttribute(String attribute) {
        WebElement attributeName = browser.findElement(xpath(format(ATTRIBUTE_NAME, attribute)));
        scrollBarIconToViewElement(attributeName, browser, 50, 15000);
        getActions().moveToElement(attributeName).click().build().perform();
        return selectedRow.getText().split("\n")[1];
    }

    public String getIdentifierOfFact(String fact) {
        WebElement factName = browser.findElement(xpath(format(FACT_NAME, fact)));
        scrollBarIconToViewElement(factName, browser, 100, 15000);
        getActions().moveToElement(factName).click().build().perform();
        return selectedRow.getText().split("\n")[1];
    }

    public String getTypeOfLabelLink(String label) {
        WebElement labelLink = getRoot().findElement(xpath(format(LABEL_LINK_NAME, label)));
        scrollBarIconToViewElement(labelLink, browser, 50, 15000);
        getActions().moveToElement(labelLink).click().build().perform();
        return selectedRow.getText().split("\n")[0];
    }

    public String getTyeOfAttribute(String attributeName) {
        WebElement element = getRoot().findElement(xpath(format(ATTRIBUTE_NAME, attributeName)));
        getActions().moveToElement(element).click().build().perform();
        return selectedRow.getText();
    }

    public String getTypeOfLabel(String labelName) {
        WebElement labelEl = getRoot().findElement(xpath(format(LABEL_NAME, labelName)));
        scrollBarIconToViewElement(labelEl, browser, 20, 15000);
        getActions().moveToElement(labelEl).click().build().perform();
        return selectedRow.getText().split("\n")[0];
    }

    public boolean isChangeLabelTypeIconVisible() {
        return isElementVisible(By.className("s-navigatedown"), getRoot());
    }

    public void clickChangeLabelType() {
        waitForElementVisible(navigateDownButton).click();
    }

    public ViewDetailDialog moveToLabelLink(String labelName) {
        WebElement element = getRoot().findElement(xpath(format(LABEL_LINK_NAME, labelName)));
        getActions().moveToElement(element).click().build().perform();
        return this;
    }
}
