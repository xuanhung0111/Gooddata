package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static org.openqa.selenium.By.cssSelector;

public class PreviewCSVDialog extends AbstractFragment {

    private String ROW_COUNT = ".preview-csv-dialog div[aria-rowcount='%s']";
    @FindBy(className = "header")
    private WebElement header;

    @FindBy(className = "dataset-edit")
    private DatasetEdit datasetEdit;

    @FindBy(css = ".data-table .data-table")
    private WebElement tableDescription;

    @FindBy(className = "error")
    private WebElement errorMessage;

    @FindBy(className = "warning")
    private WebElement warningMessage;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-import")
    private WebElement importButton;

    @FindBy(css = ".public_fixedDataTable_main div[role='columnheader']")
    private List<WebElement> listHeader;

    public static PreviewCSVDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                PreviewCSVDialog.class, waitForElementVisible(className("preview-csv-dialog"), searchContext));
    }

    public boolean isShowCorrectRow (String rows) {
        return isElementVisible(this.getRoot().findElement(cssSelector(format(ROW_COUNT, rows))));
    }

    public String getErrorMessage () {
        return errorMessage.getText();
    }

    public String getWarningMessage () {
        return warningMessage.getText();
    }

    public void clickCancelButton() {
        cancelButton.click();
    }

    public void clickImportButton() {
        importButton.click();
    }

    public DatasetEdit getEditDatasetZone() {
        return datasetEdit;
    }

    public List<String> getListHeaders () {
        return listHeader.stream().map(WebElement::getText).collect(Collectors.toList());
    }
}
