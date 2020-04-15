package com.gooddata.qa.graphene.fragments.greypages.exporter;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ReportExporterExecutorFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement result;

    @FindBy
    private WebElement submit;

    @FindBy(id = "xlsx")
    private WebElement xlsxRadio;

    @FindBy(id = "mergeHeaders")
    private WebElement mergeHeadersCheckbox;

    public ReportExporterExecutorFragment chooseXLSXRadio() throws JSONException {
        waitForElementVisible(xlsxRadio).click();
        return this;
    }

    public ReportExporterExecutorFragment unCheckMergeHeadersCheckbox() {
        return toggle(false);
    }

    public void submit(String input) throws JSONException {
        waitForElementVisible(result).sendKeys(input);
        waitForElementVisible(submit).click();
        waitForElementVisible(By.tagName("strong"), browser).click();
    }

    public ReportExporterExecutorFragment toggle(Boolean isChecked) {
        if (isMergeHeadersCheck() != isChecked) {
            waitForElementVisible(mergeHeadersCheckbox).click();
        }
        return this;
    }

    private Boolean isMergeHeadersCheck() {
        return waitForElementPresent(mergeHeadersCheckbox).isSelected();
    }
}
