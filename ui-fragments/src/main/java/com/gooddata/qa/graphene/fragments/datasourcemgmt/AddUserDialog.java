package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import java.util.List;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;

public class AddUserDialog extends AbstractFragment {

    public static final String ADD_USER_DIALOG = "data-source-user-add-dialog";

    @FindBy(className = "s-test_connection")
    private WebElement validateButton;

    @FindBy(css = ".data-source-user-add-dialog .ant-select-selector .ant-tag")
    private List<WebElement> listTag;

    @FindBy(css = ".data-source-user-add-dialog .ant-select-selector .ant-select-selection-search-input")
    private WebElement editZone;

    @FindBy(css = ".data-source-user-add-dialog .ant-select")
    private WebElement inputArea;

    @FindBy(className = "data-source-acl-dialog-header")
    private WebElement header;

    @FindBy(className = "input-radio-label")
    private List<WebElement> inputPermission;

    @FindBy(className = "s-cancel-add-user")
    private WebElement cancelBtn;

    @FindBy(className = "s-share")
    private WebElement shareBtn;

    @FindBy(className = "s-dialog-close-button")
    private WebElement closeBtn;

    public static final AddUserDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(AddUserDialog.class, waitForElementVisible(className(ADD_USER_DIALOG), searchContext));
    }

    public void inputUser(String userText) {
        Actions actions = getActions();
        actions.moveToElement(editZone).click().sendKeys(userText).sendKeys(Keys.SPACE).build().perform();
    }

    public WebElement getUserTag(String userText) {
        return listTag.stream().filter(tag -> tag.getText().equals(userText)).findFirst().get();
    }

    public void clickOnUserTag(String userText) {
        Actions actions = getActions();
        //workaround unstable open popup, click header before click tag
        actions.moveToElement(header).click().moveToElement(getUserTag(userText)).click().build().perform();
        sleepTightInSeconds(6);
    }

    public String getErrorMessageOnUserTag(String userText) {
        clickOnUserTag(userText);
        return OverlayWrapper.getInstanceByIndex(browser, 2).getErrorAddUserMessage();
    }

    public String getWarningMessageOnUserTag(String userText) {
        clickOnUserTag(userText);
        return OverlayWrapper.getInstanceByIndex(browser, 2).getWarningAddUserMessage();
    }

    public void closeUserTag(String userText) {
        Actions actions = getActions();
        actions.moveToElement(getUserTag(userText).findElement(By.className("anticon-close"))).click().build().perform();
    }

    public void clickShareButton() {
        Actions actions = getActions();
        actions.moveToElement(shareBtn).click().build().perform();
        waitForFragmentNotVisible(this);
    }

    public int getNumberOfTag() {
        return listTag.size();
    }

    public WebElement getPermission(String name) {
       WebElement label = inputPermission.stream().filter(el -> el.findElement(By.tagName("span")).getText().equals(name)).findFirst().get();
       return label.findElement(By.tagName("input"));
    }

    public void setUsePermission() {
        Actions actions = getActions();
        actions.moveToElement(getPermission("Use")).click();
    }

    public void setManagePermission() {
        Actions actions = getActions();
        actions.moveToElement(getPermission("Manage")).click();
    }
}
