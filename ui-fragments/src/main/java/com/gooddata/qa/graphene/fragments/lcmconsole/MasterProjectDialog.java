package com.gooddata.qa.graphene.fragments.lcmconsole;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class MasterProjectDialog extends AbstractFragment {

    private static final By DIALOG_CLASS = By.className("change-master-project-dialog");

    @FindBy(className = "dialog-input-field")
    private WebElement projectInputField;

    public static MasterProjectDialog getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(MasterProjectDialog.class, waitForElementVisible(DIALOG_CLASS, searchContext));
    }

    public void changeMasterProject(String newProjectId) {
        waitForElementVisible(projectInputField).clear();
        projectInputField.sendKeys(newProjectId);
        projectInputField.submit();
    }
}
