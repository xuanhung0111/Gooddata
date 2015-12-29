package com.gooddata.qa.graphene.fragments.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.fragments.common.AbstractDialog;

public class RegionalNumberFormattingDialog extends AbstractDialog {

    @FindBy(css = ".c-selectBox select")
    private Select regionalNumberFormatSelect;

    public RegionalNumberFormattingDialog selectNumberFormat(String numberFormat) {
        waitForElementVisible(regionalNumberFormatSelect).selectByVisibleText(numberFormat);
        return this;
    }

    public String getSelectedNumberFormat() {
        return waitForElementVisible(regionalNumberFormatSelect).getFirstSelectedOption()
                .getText();
    }

}
