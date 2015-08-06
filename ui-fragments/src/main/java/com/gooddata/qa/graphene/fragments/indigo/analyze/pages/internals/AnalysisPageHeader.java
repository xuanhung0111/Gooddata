package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class AnalysisPageHeader extends AbstractFragment {

    @FindBy(css = ".s-btn-reset")
    private WebElement resetButton;

    @FindBy(css = ".s-export-to-report")
    private WebElement exportToReportButton;

    @FindBy(css = ".s-undo")
    private WebElement undoButton;

    @FindBy(css = ".s-redo")
    private WebElement redoButton;

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

    public void undo() {
        waitForElementVisible(undoButton).click();
    }

    public void redo() {
        waitForElementVisible(redoButton).click();
    }

    public boolean isUndoButtonEnabled() {
        return !waitForElementVisible(undoButton).getAttribute("class").contains(DISABLED);
    }

    public boolean isRedoButtonEnabled() {
        return !waitForElementVisible(redoButton).getAttribute("class").contains(DISABLED);
    }

    public String getExportToReportButtonTooltipText() {
        new Actions(browser).moveToElement(exportToReportButton).perform();
        return waitForElementVisible(By.cssSelector(".bubble-overlay .content"), browser).getText().trim();
    }
}
