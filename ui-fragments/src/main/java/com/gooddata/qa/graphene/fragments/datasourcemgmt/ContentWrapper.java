package com.gooddata.qa.graphene.fragments.datasourcemgmt;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ContentWrapper extends AbstractFragment {
    public static final String CONTENT_WRAPPER_CLASS = "content-wrapper";
    private static final int TIMEOUT_WAIT_CONTENT_LOADED = 5 * 60;

    @FindBy(className = "loader-wraper")
    private static WebElement loading;

    public static final ContentWrapper getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ContentWrapper.class, waitForElementVisible(className(CONTENT_WRAPPER_CLASS), searchContext));

    }

    public InitialContent getInitialContent() {
        return InitialContent.getInstance(browser);
    }

    public ContentDatasourceContainer getContentDatasourceContainer() {
        return ContentDatasourceContainer.getInstance(browser);
    }

    public ContentWrapper waitLoadingManagePage() {
        waitForElementNotVisible(loading, TIMEOUT_WAIT_CONTENT_LOADED);
        return this;
    }
}
