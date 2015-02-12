package com.gooddata.qa.graphene.fragments.dashboards;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardEmbedDialog extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='Auto']")
    private WebElement autoOption;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='Custom']")
    private WebElement customOption;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='Web Tab']")
    private WebElement webTabOption;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-radiowidgetitem-content')]/label[text()='VisualForce Page']")
    private WebElement visualForcePageOption;

    @FindBy(xpath = "//div[contains(@class,'embedUriArea')]/textarea")
    private WebElement embedCode;

    @FindBy(xpath = "//div[contains(@class,'embedUriPlainArea')]/input")
    private WebElement previewURIInBrowser;

    public String getEmbedCode() {
        return embedCode.getText();
    }

    public String getPreviewURI() {
        return previewURIInBrowser.getAttribute("value");
    }
}
