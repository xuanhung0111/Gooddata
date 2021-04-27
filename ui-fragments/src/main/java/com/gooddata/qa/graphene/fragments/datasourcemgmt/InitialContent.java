package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class InitialContent extends AbstractFragment {
    private static final By INITIAL_CLASS = By.cssSelector(".overview-container .overview-content");

    @FindBy(className = "gd-button-primary")
    private List<WebElement> cloudResourceButton;

    @FindBy(className = "overview-info")
    private WebElement overviewInfo;

    public static final InitialContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(InitialContent.class, waitForElementVisible(INITIAL_CLASS, searchContext));
    }

    public ConnectionConfiguration openSnowflakeEdit() {
        waitForElementVisible(cloudResourceButton.get(0)).click();
        return ConnectionConfiguration.getInstance(browser);
    }

    public ConnectionConfiguration openPostgresEdit() {
        waitForElementVisible(cloudResourceButton.get(3)).click();
        return ConnectionConfiguration.getInstance(browser);
    }

    public ConnectionConfiguration openBigQueryEdit() {
        waitForElementVisible(cloudResourceButton.get(1)).click();
        return ConnectionConfiguration.getInstance(browser);
    }

    public ConnectionConfiguration openRedshiftEdit() {
        waitForElementVisible(cloudResourceButton.get(2)).click();
        return ConnectionConfiguration.getInstance(browser);
    }

    public String getInitialContentText() {
        return overviewInfo.getText();
    }

    public String getTextOnCloudResourceButton(int index) {
        return cloudResourceButton.get(index).getText();
    }

    public int getNumberOfCloudResourceButton() {
        return cloudResourceButton.size();
    }

}
