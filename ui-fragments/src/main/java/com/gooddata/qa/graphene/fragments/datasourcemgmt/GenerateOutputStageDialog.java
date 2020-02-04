package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;

public class GenerateOutputStageDialog extends AbstractFragment {
    @FindBy(className = "s-copy_to_clipboard")
    private WebElement copyButton;

    @FindBy(className = "s-close")
    private WebElement closeButton;

    @FindBy(css = ".overlay pre")
    private WebElement maqlText;

    @FindBy(className = "s-loading-spinner")
    private WebElement loadingGenerate;

    public static GenerateOutputStageDialog getInstance(SearchContext context) {
        return Graphene.createPageFragment(GenerateOutputStageDialog.class,
                waitForElementVisible(className("generate-output-dialog"), context));
    }

    public void clickCopy() {
        waitForElementVisible(copyButton).click();
    }

    public void clickClose() {
        waitForElementVisible(closeButton).click();
        waitForFragmentNotVisible(this);
    }

    public String getMessage() {
        waitForElementNotVisible(loadingGenerate);
        return waitForElementVisible(maqlText).getText();
    }
}
