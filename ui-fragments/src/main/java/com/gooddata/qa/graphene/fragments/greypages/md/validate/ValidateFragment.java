package com.gooddata.qa.graphene.fragments.greypages.md.validate;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class ValidateFragment extends AbstractGreyPagesFragment {

    private static final By BY_VALIDATE_STATUS = By.xpath("//*[local-name() = 'p'][3]//*[local-name() = 'span']");

    @FindBy
    private WebElement submit;

    public String validate(int timeoutInSecond) {
        waitForElementVisible(submit).click();
        waitForElementNotVisible(submit, timeoutInSecond);
        waitForElementVisible(BY_GP_LINK, browser, timeoutInSecond).click();
        waitForElementNotPresent(BY_GP_PRE_JSON, timeoutInSecond);
        return waitForElementVisible(BY_VALIDATE_STATUS, browser, timeoutInSecond).getText();
    }
}