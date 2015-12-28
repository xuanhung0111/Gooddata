package com.gooddata.qa.graphene.fragments.greypages.gdc;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class GdcFragment extends AbstractGreyPagesFragment {

    @FindBy(linkText = "user-uploads")
    private WebElement user_uploads;

    public String getUserUploadsURL() {
        return user_uploads.getAttribute("href");
    }
}
