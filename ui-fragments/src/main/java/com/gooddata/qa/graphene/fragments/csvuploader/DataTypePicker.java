package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DataTypePicker extends AbstractFragment {

    @FindBy(tagName = "div")
    private DataTypeSelect typeSelect;

    @FindBy(className = "format-dropdown-reference")
    private DateFormatSelect dateFormatSelect;

    public DataTypeSelect getDataTypeSelect() {
        // Root of this fragment is not visible in all cases. Just make sure it appears in DOM
        waitForElementPresent(typeSelect.getRoot());
        return typeSelect;
    }

    public DateFormatSelect getDateFormatSelect() {
        return waitForFragmentVisible(dateFormatSelect);
    }
}
