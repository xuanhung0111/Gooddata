package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class WarningAddUserPopUp extends AbstractFragment {
    private static final String OVERLAY_CUSTOM = "gd-message-overlay-custom";

    //Text : Data Source shared with only a subset of users.
    //Text2 : Data Source was shared only with some users because some usernames could not be found.
    @FindBy(css = ".gd-message-text-showmore .s-message-text-header-value")
    private WebElement headerValue;

    //Text: The Data source can not be shared to the Domain Admin 'qa+test@gooddata.com'
    //Text2: The following usernames could not be found: nmphong+stag23@lhv.vn.
    @FindBy(css = ".gd-message-text-showmore .on")
    private WebElement warningMessage;

    @FindBy(css = ".gd-message-text-showmorelink .s-message-text-showmorelink")
    private WebElement showBtn;

    @FindBy(className = "icon-cross")
    private WebElement closeBtn;


    public static WarningAddUserPopUp getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(WarningAddUserPopUp.class, waitForElementVisible(className(OVERLAY_CUSTOM), searchContext));
    }
}
