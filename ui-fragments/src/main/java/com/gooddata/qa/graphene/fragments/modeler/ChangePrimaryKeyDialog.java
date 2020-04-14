package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static org.openqa.selenium.By.className;

public class ChangePrimaryKeyDialog extends AbstractFragment {
    @FindBy(className = "s-set_key")
    WebElement setKeyButton;

    @FindBy(className = "s-list-status-bar")
    WebElement setKeyText;

    @FindBy(className = "primary-key-list")
    PrimaryKeyList primaryKeyList;

    private static final String CHANGE_PRIMARY_KEY_DIALOG = "dataset-change-primary-key-dialog";

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

    public String getPrimaryKeyText() {
        return setKeyText.getText();
    }

    public void setPrimaryKey(String datasetName, String attributeName) {
        PrimaryKeyList keyList = getPrimaryKeyList();
        keyList.searchAttribute(attributeName);
        keyList.clickAttribute(datasetName, attributeName);
        clickSetKey();
        waitForFragmentNotVisible(this);
    }
}
