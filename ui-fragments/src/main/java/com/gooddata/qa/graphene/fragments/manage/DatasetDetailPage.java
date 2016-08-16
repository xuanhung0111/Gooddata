package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.By;

public class DatasetDetailPage extends ObjectPropertiesPage {

    private By UPLOAD_LIST_DATE_LOCATOR = By.cssSelector(".uploadsList .date");
    private By ATTRIBUTE_LOCATOR = By.cssSelector(".contentAttribute td.title a");
    private By FACT_LOCATOR = By.cssSelector(".contentFact td.title a");

    public String getLatestUploadDate() {
        waitForElementVisible(UPLOAD_LIST_DATE_LOCATOR, browser);
        return getRoot().findElements(UPLOAD_LIST_DATE_LOCATOR).get(0).getText();
    }

    public List<String> getAttributes() {
        waitForElementVisible(ATTRIBUTE_LOCATOR, browser);
        return getElementTexts(getRoot().findElements(ATTRIBUTE_LOCATOR));
    }

    public List<String> getFacts() {
        waitForElementVisible(FACT_LOCATOR, browser);
        return getElementTexts(getRoot().findElements(FACT_LOCATOR));
    }
}
