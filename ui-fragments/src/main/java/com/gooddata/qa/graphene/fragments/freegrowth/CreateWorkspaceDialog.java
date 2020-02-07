package com.gooddata.qa.graphene.fragments.freegrowth;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

public class CreateWorkspaceDialog extends AbstractFragment {

    @FindBy(id = "projectName")
    private WebElement nameText;

    @FindBy(id = "projectAuthor")
    private WebElement tokenText;

    @FindBy(className = "s-btn-create")
    private WebElement createButton;

    @FindBy(className = "s-btn-cancel")
    private WebElement cancelButton;

    public static CreateWorkspaceDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(CreateWorkspaceDialog.class,
                waitForElementVisible(cssSelector(".yui3-d-modaldialog-focused .c-modalDialog"), context));
    }

    public void createNewWorkspace(final String projectName, final String projectToken) {
        nameText.sendKeys(projectName);
        tokenText.sendKeys(projectToken);
        createButton.click();
        waitForElementNotPresent(this.getRoot());
    }
}
