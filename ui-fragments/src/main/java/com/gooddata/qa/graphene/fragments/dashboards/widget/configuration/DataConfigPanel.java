package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.function.Function;

public class DataConfigPanel extends AbstractFragment {

    @FindBy(xpath = "//*[contains(@class,'metricRow')]/button")
    private WebElement metricSelect;

    public boolean isMoreInfoPresent() {
        return isElementPresent(cssSelector(".s-activeConfigTab a:not(.inlineBubbleHelp)"), getRoot());
    }
}
