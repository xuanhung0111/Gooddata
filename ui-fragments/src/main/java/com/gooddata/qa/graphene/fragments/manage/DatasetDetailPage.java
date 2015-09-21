package com.gooddata.qa.graphene.fragments.manage;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static java.util.stream.Collectors.toList;

public class DatasetDetailPage extends AbstractFragment {

    private By UPLOAD_LIST_DATE_LOCATOR = By.cssSelector(".uploadsList .date");
    private By ATTRIBUTE_LOCATOR = By.cssSelector(".contentAttribute td.title a");
    private By FACT_LOCATOR = By.cssSelector(".contentFact td.title a");
    
    @FindBy(css = "button.s-btn-delete")
    private WebElement datasetDeleteButton;

    private static final By confirmDeleteButtonLocator = By.cssSelector(".yui3-d-modaldialog:not(.gdc-hidden) .c-modalDialog .s-btn-delete");

    public void deleteDataset() {
        waitForElementVisible(datasetDeleteButton).click();
        waitForElementVisible(confirmDeleteButtonLocator, browser).click();
        waitForDataPageLoaded(browser);
    }
    
    public String getLatestUploadDate() {
        waitForElementVisible(UPLOAD_LIST_DATE_LOCATOR, browser);
        return getRoot().findElements(UPLOAD_LIST_DATE_LOCATOR).get(0).getText();
    }
    
    public List<String> getAttributes() {
        waitForElementVisible(ATTRIBUTE_LOCATOR, browser);
        return getRoot().findElements(ATTRIBUTE_LOCATOR).stream().map(WebElement::getText).collect(toList());
    }
    
    public List<String> getFacts() {
        waitForElementVisible(FACT_LOCATOR, browser);
        return getRoot().findElements(FACT_LOCATOR).stream().map(WebElement::getText).collect(toList());
    }
}
