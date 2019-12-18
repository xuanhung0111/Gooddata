package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.id;

public class DataSourceManagementPage extends AbstractFragment {
    public static final String URI = "/admin/connect/";

    @FindBy(className = "overview-content")
    private WebElement initialContent;

    @FindBy(className = "gd-button-primary")
    private List<WebElement> cloudResourceButton;

    @FindBy(className = "s-navigation-add-datasource")
    private WebElement addButton;

    @FindBy(className = "create-or-edit-connection-container")
    private ConnectionDetail connectionDetail;

    @FindBy(className = "can-create-new-datasource")
    private DataSourceMenu datasourceMenu;

    public static final String MAIN_CLASS = "App";

    public DataSourceMenu getMenuBar() {
        return DataSourceMenu.getInstance(browser);
    }

    public static final DataSourceManagementPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(DataSourceManagementPage.class, waitForElementVisible(className(MAIN_CLASS), context));
    }

    public SnowflakeEdit openSnowflakeEdit() {
        waitForElementVisible(cloudResourceButton.get(0)).click();
        return SnowflakeEdit.getInstance(browser);
    }

    public String getInitialContent() {
        return waitForElementVisible(initialContent).getText();
    }

    public String getTextOnCloudResourceButton(int index) {
        return cloudResourceButton.get(index).getText();
    }

    public int getNumberOfCloudResourceButton() {
        waitForElementVisible(initialContent);
        return cloudResourceButton.size();
    }
}
