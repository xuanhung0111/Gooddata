package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class ChangePrimaryKeyDialog extends AbstractFragment {
    @FindBy(className = "s-set_key")
    WebElement setKeyButton;

    @FindBy(className = "s-cancel")
    WebElement cancelButton;

    @FindBy(className = "s-list-status-bar")
    WebElement setKeyText;

    @FindBy(className = "primary-key-list")
    PrimaryKeyList primaryKeyList;

    @FindBy(className = "dataset-change-primary-key-error-message")
    WebElement keyErrorMessage;

    private static final String CHANGE_PRIMARY_KEY_DIALOG = "dataset-change-primary-key-dialog";
    private static final By CHANGE_PRIMARY_KEY_ERROR = className("dataset-change-primary-key-error-message");
    private static final String PRIMARY_KEY_ERROR_MESSAGE = "Dataset is connected to another dataset, primary key must be single attribute.";

    public static ChangePrimaryKeyDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ChangePrimaryKeyDialog.class, waitForElementVisible(className(CHANGE_PRIMARY_KEY_DIALOG), searchContext));
    }

    public PrimaryKeyList getPrimaryKeyList() {
        waitForElementVisible(primaryKeyList.getRoot());
        return primaryKeyList;
    }

    public void clickSetKey() {
        setKeyButton.click();
    }

    public void clickCancel() {
        cancelButton.click();
    }

    public String getPrimaryKeyText() {
        return setKeyText.getText();
    }

    public void setPrimaryKey(String datasetName, String attributeName) {
        searchPrimaryKey(datasetName, attributeName);
        clickSetKey();
        sleepTightInSeconds(1);
//        waitForFragmentNotVisible(this);
    }

    public void searchPrimaryKey(String datasetName, String attributeName) {
        PrimaryKeyList keyList = getPrimaryKeyList();
        keyList.searchAttribute(attributeName);
        keyList.clickAttribute(datasetName, attributeName);
    }
    
    public boolean isErrorMessageVisible() {
        return (isElementVisible(CHANGE_PRIMARY_KEY_ERROR, getRoot()) && keyErrorMessage.getText().equals(PRIMARY_KEY_ERROR_MESSAGE));
    }
}
