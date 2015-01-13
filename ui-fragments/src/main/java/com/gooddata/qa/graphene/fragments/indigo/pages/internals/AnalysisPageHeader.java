package com.gooddata.qa.graphene.fragments.indigo.pages.internals;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AnalysisPageHeader extends AbstractFragment {

    @FindBy(css = ".s-btn-reset")
    private WebElement resetButton;

    @FindBy(css = ".s-btn-open_as_report")
    private WebElement exportToReportButton;

    private static final String DISABLED = "disabled";

    public void resetToBlankState() {
        waitForElementVisible(resetButton).click();
    }

    public void exportReport() {
        waitForElementVisible(exportToReportButton).click();
    }

    public boolean isExportToReportButtonEnable() {
        return !waitForElementVisible(exportToReportButton).getAttribute("class").contains(DISABLED);
    }
}
