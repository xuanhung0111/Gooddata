package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class UserField extends AbstractFragment {

    @FindBy(css = "div[role='row']")
    private List<WebElement> rowUsers;

    @FindBy(className = "s-edit-user")
    private WebElement editButton;

    @FindBy(className = "s-delete-user")
    private WebElement deleteButton;

    public static UserField getInstance(SearchContext context) {
        return Graphene.createPageFragment(UserField.class,
                waitForElementVisible(className("user-field"), context));
    }

    public WebElement getCellOfChosenRow(WebElement row, int index) {
        return row.findElements(By.className("public_fixedDataTableCell_main"))
                .get(index);
    }

    public WebElement getChosenUserRow(String userText) {
         return rowUsers.stream().filter(row -> getCellOfChosenRow(row, 1).getText()
                 .equals(userText)).findFirst().get();
    }

    public List<String> getListSharedUser() {
        List<String> listUser = new ArrayList<String>();
        rowUsers.stream().forEach(row -> listUser.add(getCellOfChosenRow(row, 1).getText()));
        listUser.remove(0);
        return listUser;
    }

    public String getRoleOfChosenUser(String userText) {
        return getCellOfChosenRow(getChosenUserRow(userText), 2).getText();
    }

    public String getNameOfChosenUser(String userText) {
        return getCellOfChosenRow(getChosenUserRow(userText), 0).getText();
    }

    public void clickButtonOnActionField(String userText, WebElement element) {
        WebElement actionCell = getCellOfChosenRow(getChosenUserRow(userText), 3);
        Actions action = getActions();
        action.moveToElement(actionCell).build().perform();
        waitForElementVisible(element);
        action.moveToElement(element).click().build().perform();
    }

    public EditUserDialog openEditUseDialog(String userText) {
        clickButtonOnActionField(userText, editButton);
        return EditUserDialog.getInstance(browser);
    }

    public DeleteUserDialog openDeleteUseDialog(String userText) {
        clickButtonOnActionField(userText, deleteButton);
        return DeleteUserDialog.getInstance(browser);
    }

    public boolean isCurrentUser(String userText) {
        return getCellOfChosenRow(getChosenUserRow(userText), 0).getText().contains("(You)");
    }
}
