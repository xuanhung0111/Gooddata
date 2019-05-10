package com.gooddata.qa.graphene.fragments.indigo.analyze.dialog;

import com.gooddata.qa.graphene.fragments.common.AbstractDialog;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.testng.Assert.assertFalse;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotSelected;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;

public class ExportXLSXDialog extends AbstractDialog {

    private static final By ROOT_LOCATOR = By.className("gd-export-dialog");

    @FindBy(className = "s-dialog-submit-button")
    private WebElement exportButton;

    public static final ExportXLSXDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(ExportXLSXDialog.class, waitForElementVisible(ROOT_LOCATOR, context));
    }

    public void confirmExport() {
        waitForElementVisible(exportButton).click();
    }

    public ExportXLSXDialog uncheckOption(OptionalExport optional) {
        if (isOptionCheck(optional)) {
            waitForElementVisible(By.cssSelector(optional.getOptionLabel()), getRoot()).click();
        }
        waitForElementNotSelected(By.cssSelector(optional.toString()), getRoot(), 3);
        assertFalse(isOptionCheck(optional), "Option should be not checked");
        return this;
    }

    public enum OptionalExport {
        CELL_MERGED("input[name*='mergeHeaders']", "input[name*='mergeHeaders'] + span.input-label-text");

        private String option;
        private String optionLabel;

        OptionalExport(String option, String optionLabel) {
            this.option = option;
            this.optionLabel = optionLabel;
        }

        @Override
        public String toString() {
            return option;
        }

        public String getOptionLabel() { return optionLabel; }
    }

    private Boolean isOptionCheck(OptionalExport optional) {
        return waitForElementPresent(By.cssSelector(optional.toString()), getRoot()).isSelected();
    }
}
