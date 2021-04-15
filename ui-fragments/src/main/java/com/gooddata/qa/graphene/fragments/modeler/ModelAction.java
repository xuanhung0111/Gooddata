package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ModelAction extends AbstractFragment {
    private static final String MODEL_ACTION = "actions";
    private static final By BUTTON_ADD_ATTRIBUTE = By.className("add-attribute");
    private static final By BUTTON_ADD_FACT = By.className("add-fact");
    private static final By BUTTON_MORE = By.className("add-more");
    private static final By BUTTON_VIEW_DETAIL = By.className("view-detail");

    public static ModelAction getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ModelAction.class, waitForElementVisible(className(MODEL_ACTION), searchContext));
    }

    @FindBy(className = "add-attribute")
    private WebElement btnAddAttribute;

    @FindBy(className = "add-fact")
    private WebElement btnAddFact;

    @FindBy(className = "add-more")
    private WebElement btnMore;

    @FindBy(className = "edit")
    private WebElement btnEdit;

    @FindBy(className = "delete")
    private WebElement btnDelete;

    @FindBy(className = "view-detail")
    private WebElement btnViewDetail;

    public WebElement addAttribute() {
        return btnAddAttribute;
    }

    public WebElement addFact() {
        return btnAddFact;
    }

    public WebElement addMore() {
        return btnMore;
    }

    public WebElement editDate() {
        return btnEdit;
    }

    public WebElement deleteDate() {
        return btnDelete;
    }

    public boolean isAddAttributeExist() {
        return isElementVisible(BUTTON_ADD_ATTRIBUTE, getRoot());
    }

    public boolean isAddFactExist() {
        return isElementVisible(BUTTON_ADD_FACT, getRoot());
    }

    public boolean isAddMoreExist() {
        return isElementVisible(BUTTON_MORE, getRoot());
    }

    public boolean isViewDetailsExist() {
       return isElementVisible(BUTTON_VIEW_DETAIL, getRoot());
    }

    public EditDatasetDialog openEditDatasetDialog() {
        btnViewDetail.click();
        return EditDatasetDialog.getInstance(browser);
    }
}
