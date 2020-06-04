package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static org.openqa.selenium.By.className;

public class OutputStage extends AbstractFragment {
    private static final String OUTPUT_STAGE_DIALOG = "create-output-stage-dialog";

    @FindBy(className = "warning")
    private WebElement warning;

    @FindBy(className = "initial-page-select-data-source-detail-item")
    private List<WebElement> listDatasourceItem;

    @FindBy(css= ".initial-page-sql-properties-detail-dropdown")
    private WebElement dropdownProperties;

    @FindBy(className = "gd-list-item")
    private List<WebElement> listOption;

    @FindBy(className = "s-create")
    private WebElement createButton;

    @FindBy(className = "s-copy_to_clipboard")
    private WebElement copyButton;

    @FindBy(className = "s-cancel-file")
    private WebElement cancelButton;

    @FindBy(className = "gd-spinner")
    private WebElement spinnerLoading;

    @FindBy(className = "result-page-result")
    private WebElement resultContent;

    public static OutputStage getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                OutputStage.class, waitForElementVisible(className(OUTPUT_STAGE_DIALOG), searchContext));
    }

    public String getWarningText() {
        return warning.getText();
    }

    public OutputStage selectDatasource(String datasourceName) {
        for (WebElement item : listDatasourceItem) {
            if(item.getText().equals(datasourceName)) {
                WebElement checkbox = item.findElement(By.className("input-radio"));
                scrollElementIntoView(item,browser);
                checkbox.click();
            }
        }
        return this;
    }

    public void selectProperty() {
        dropdownProperties.click();
    }

    public String createOutputStage() {
        waitForElementEnabled(createButton).click();
        waitForElementNotVisible(spinnerLoading);
        waitForElementVisible(resultContent);
        return resultContent.getText();
    }

    public void copyOutputStage() {
        waitForElementEnabled(copyButton);
        copyButton.click();
    }

    public void cancelOutputStage() {
        waitForElementEnabled(cancelButton);
        cancelButton.click();
    }
}
