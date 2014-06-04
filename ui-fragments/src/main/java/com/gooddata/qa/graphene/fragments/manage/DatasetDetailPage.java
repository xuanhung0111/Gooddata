package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DatasetDetailPage extends AbstractFragment {
    
    @FindBy(css = "button.s-btn-delete")
    private WebElement datasetDeleteButton;
    
    @FindBy(xpath = "//div[contains(@class,'c-modalDialog') and contains(@class,'t-confirmDelete')]//button[contains(@class,'s-btn-delete')]")
    private WebElement confirmDeleteButton;

    public void deleteDataset() throws InterruptedException {
    	waitForElementVisible(datasetDeleteButton).click();
    	Thread.sleep(3000);
    	waitForElementVisible(confirmDeleteButton).click();
    	waitForDataPageLoaded();
    }
}
