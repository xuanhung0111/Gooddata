package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class DataSourceManagementPage extends AbstractFragment {
    public static final String URI = "/admin/connect/";

    @FindBy(className = "can-create-new-datasource")
    private DataSourceMenu datasourceMenu;

    @FindBy(className = "overview-content")
    private WebElement overviewInfo;

    public static final String MAIN_CLASS = "App";

    public static final DataSourceManagementPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(DataSourceManagementPage.class, waitForElementVisible(className(MAIN_CLASS), context));
    }

    public DataSourceMenu getMenuBar() {
        return DataSourceMenu.getInstance(browser);
    }

    public ContentWrapper getContentWrapper() {
        return ContentWrapper.getInstance(browser);
    }

    public boolean isCreateS3DatasourceButtonDisplayed() {
        waitForElementVisible(overviewInfo);
        return isElementVisible(By.className("s-create-datasource-button-s3"), getRoot());
    }
}
