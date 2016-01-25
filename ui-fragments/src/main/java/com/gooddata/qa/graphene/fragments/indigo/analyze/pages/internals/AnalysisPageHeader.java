package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AnalysisPageHeader extends AbstractFragment {

    @FindBy(className = "s-reset")
    private WebElement resetButton;

    @FindBy(className = "s-export-to-report")
    private WebElement exportToReportButton;

    @FindBy(className = "s-undo")
    private WebElement undoButton;

    @FindBy(className = "s-redo")
    private WebElement redoButton;

    public void resetToBlankState() {
        waitForElementVisible(resetButton).click();
    }

    public void exportReport() {
        waitForElementVisible(exportToReportButton).click();
    }

    public boolean isExportButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(exportToReportButton));
    }

    public void undo() {
        waitForElementVisible(undoButton).click();
    }

    public void redo() {
        waitForElementVisible(redoButton).click();
    }

    public boolean isUndoButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(undoButton));
    }

    public boolean isRedoButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(redoButton));
    }

    public boolean isResetButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(resetButton));
    }

    public WebElement getResetButton() {
        return waitForElementVisible(resetButton);
    }

    public String getExportButtonTooltipText() {
        getActions().moveToElement(exportToReportButton).perform();
        return waitForElementVisible(cssSelector(".bubble-overlay .content"), browser).getText().trim();
    }

    private boolean isElementDisabled(WebElement element) {
        return element.getAttribute("class").contains("disabled");
    }
}
