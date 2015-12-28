package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class FileUploadProgressDialog extends AbstractFragment {

    @FindBy(className = "s-cancel-file")
    private WebElement cancelButton;

    public void clickCancel() {
        waitForElementVisible(cancelButton).click();
    }
}
