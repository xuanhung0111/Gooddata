package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class DatasetDetailPage extends AbstractFragment {

    @FindBy(className = "s-dataset-name")
    private WebElement datasetName;

    @FindBy(className = "s-dataset-detail-back-button")
    private WebElement backButton;

    @FindBy(className = "s-dataset-columns-table")
    private DatasetColumnsTable datasetColumns;

    public void clickBackButton() {
        waitForElementVisible(backButton).click();
    }

    public String getDatasetName() {
        return waitForElementVisible(datasetName).getText();
    }

    public List<String> getColumnNames() {
        return waitForFragmentVisible(datasetColumns).getColumnNames();
    }

    public List<String> getColumnTypes() {
        return waitForFragmentVisible(datasetColumns).getColumnTypes();
    }
}
