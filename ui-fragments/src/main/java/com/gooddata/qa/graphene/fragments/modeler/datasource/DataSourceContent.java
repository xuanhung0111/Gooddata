package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static org.testng.Assert.assertTrue;

public class DataSourceContent extends AbstractFragment {
    private static final String DATASOURCE_CONTENT = "datasource-content";

    @FindBy(className = "datasource-content-connecting")
    private WebElement datasourceConnecting;

    @FindBy(css = ".datasource-content-connecting .waiting-message")
    private WebElement connectingMessage;

    @FindBy(className = "datasource-content-not-connected")
    private WebElement dataSourceNotConnected;

    @FindBy(className = "datasource-search-no-results")
    private WebElement dataSourceNoResult;

    public static DataSourceContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                DataSourceContent.class, waitForElementVisible(className(DATASOURCE_CONTENT), searchContext));
    }

    public DataSourceContentConnected getDatasourceConnected () {
        waitForElementNotVisible(datasourceConnecting);
        return DataSourceContentConnected.getInstance(browser);
    }

    public String getDatasourceNotConnectedText() {
        waitForElementNotVisible(datasourceConnecting);
        return dataSourceNotConnected.getText();
    }

    public String getDatasourceNoResultText() {
        return dataSourceNoResult.getText();
    }

    public DataSourceContentConnected clickButtonConnect() {
        waitForElementVisible(dataSourceNotConnected);
        dataSourceNotConnected.findElement(By.className("s-connect")).click();
        return DataSourceContentConnected.getInstance(browser);
    }

    public String getConnectingMessage() {
        return connectingMessage.getText();
    }

    public boolean isWaitingMessageVisible() {
        return isElementVisible(By.cssSelector(".datasource-content-connecting .waiting-message"), this.getRoot());
    }

    public boolean verifyConnectingMessage() {
        if (isWaitingMessageVisible()) {
            return connectingMessage.getText().contains("Still trying to connect...This may take up to " +
                    "several minutes");
        }
        return true;
    }
}
