package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class AddUserDialog extends AbstractFragment {

    public static final String ADD_USER_DIALOG = "data-source-user-add-dialog";

    @FindBy(className = "s-test_connection")
    private WebElement validateButton;

    @FindBy(css = ".data-source-user-add-dialog .ant-select-selector .ant-tag")
    private List<WebElement> listTag;

    @FindBy(css = ".data-source-user-add-dialog .ant-select-selector .ant-select-selection-search")
    private WebElement editZone;

    @FindBy(className = "input-radio-label")
    private List<WebElement> inputRadioList;

    @FindBy(className = "s-cancel-add-user")
    private WebElement cancelBtn;

    @FindBy(className = "s-share")
    private WebElement shareBtn;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeBtn;

    public static final AddUserDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(AddUserDialog.class, waitForElementVisible(className(ADD_USER_DIALOG), searchContext));
    }
}
