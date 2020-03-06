package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ContentDatasourceContainer extends AbstractFragment {
    public static final String CONTENT_CLASS = "create-or-edit-connection-container";

    @FindBy(className = "s-save")
    private WebElement saveButton;

    @FindBy(className = "s-cancel")
    private WebElement cancelButton;

    @FindBy(className = "report-title")
    private WebElement connectionTitle;

    @FindBy(className = "datasource-heading-container-id-value")
    private WebElement dataSourceId;

    @FindBy(className = "datasource-heading-name")
    private WebElement dataSourceName;

    public static final ContentDatasourceContainer getInstance(SearchContext context) {
        return Graphene.createPageFragment(ContentDatasourceContainer.class, waitForElementVisible(className(CONTENT_CLASS), context));
    }

    public ConnectionDetail getConnectionDetail() {
        return ConnectionDetail.getInstance(browser);
    }

    public ConnectionConfiguration getConnectionConfiguration() {
        return ConnectionConfiguration.getInstance(browser);
    }

    public DatasourceHeading getDatasourceHeading() {
        return DatasourceHeading.getInstance(browser);
    }

    public void addConnectionTitle(String title) {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(connectionTitle).click().keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL)
                .sendKeys(Keys.DELETE).sendKeys(title).build().perform();
    }

    public void clickSavebutton() {
        waitForElementVisible(saveButton).click();
    }

    public void clickCancelButton () {
        waitForElementVisible(cancelButton).click();
    }

    public String getDataSourceId() {
        return waitForElementVisible(dataSourceId).getText();
    }

    public String getDataSourceName() {
        return waitForElementVisible(dataSourceName).getText();
    }
}
