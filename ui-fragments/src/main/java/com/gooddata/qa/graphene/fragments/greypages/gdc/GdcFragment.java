package com.gooddata.qa.graphene.fragments.greypages.gdc;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class GdcFragment extends AbstractGreyPagesFragment {

    @FindBy(linkText = "user-uploads")
    private WebElement user_uploads;

    public String getUserUploadsURL() {
        return user_uploads.getAttribute("href");
    }
}
