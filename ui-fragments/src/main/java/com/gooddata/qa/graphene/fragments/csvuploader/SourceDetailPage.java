package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class SourceDetailPage extends AbstractFragment {

    @FindBy(className = "s-source-name")
    private WebElement sourceName;

    @FindBy(className = "s-source-detail-back-button")
    private WebElement backButton;

    @FindBy(className = "s-source-columns-table")
    private SourceColumnsTable sourceColumns;

    public void clickBackButton() {
        waitForElementVisible(backButton).click();
    }

    public String getSourceName() {
        return waitForElementVisible(sourceName).getText();
    }

    public List<String> getColumnNames() {
        return waitForFragmentVisible(sourceColumns).getColumnNames();
    }

    public List<String> getColumnTypes() {
        return waitForFragmentVisible(sourceColumns).getColumnTypes();
    }
}
