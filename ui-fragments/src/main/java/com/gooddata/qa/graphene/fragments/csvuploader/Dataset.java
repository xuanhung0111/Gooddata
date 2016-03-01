package com.gooddata.qa.graphene.fragments.csvuploader;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class Dataset extends AbstractFragment {

    private static final By BY_DETAIL_BUTTON = By.className("s-dataset-detail-button");
    private static final By BY_UPDATE_BUTTON = By.className("s-dataset-update-button");
    private static final By BY_DELETE_BUTTON = By.className("s-dataset-delete-button");
    private static final By BY_ANALYZE_LINK = By.cssSelector(".icon-analyze.button-link");

    @FindBy(className = "s-dataset-status")
    private WebElement status;

    @FindBy(className = "datasets-col-created")
    private WebElement createdDate;

    @FindBy(css = "td.datasets-col-created + td")
    private WebElement updatedDate;

    public String getStatus() {
        return waitForElementVisible(status).getText();
    }

    public String getCreatedDate() {
        return waitForElementVisible(createdDate).getText();
    }

    public String getUpdatedDate() {
        return waitForElementVisible(updatedDate).getText();
    }

    public String getAnalyzeLink() {
        return waitForElementVisible(getRoot()).findElement(BY_ANALYZE_LINK).getAttribute("href");
    }

    public DatasetDetailPage openDetailPage() {
        waitForElementVisible(getRoot().findElement(BY_DETAIL_BUTTON)).click();

        return Graphene.createPageFragment(DatasetDetailPage.class,
                waitForElementVisible(By.className("s-dataset-detail"), browser));
    }

    public void clickUpdateButton() {
        waitForElementVisible(getRoot().findElement(BY_UPDATE_BUTTON)).click();
    }

    public void clickDeleteButton() {
        waitForElementVisible(getRoot().findElement(BY_DELETE_BUTTON)).click();
    }

    public boolean isDetailButtonVisible() {
        return isElementVisible(BY_DETAIL_BUTTON, getRoot()); 
    }

    public boolean isUpdateButtonVisible() {
        return isElementVisible(BY_UPDATE_BUTTON, getRoot());
    }

    public boolean isDeleteButtonVisible() {
        return isElementVisible(BY_DELETE_BUTTON, getRoot());
    }

    public boolean isAnalyzeLinkDisabled() {
        return waitForElementVisible(getRoot().findElement(BY_ANALYZE_LINK))
                .getAttribute("class")
                .contains("disabled");
    }

    public static WebElement waitForProgressLoadingItem(SearchContext searchContext) {
        return waitForElementVisible(By.className("item-in-progress"), searchContext);
    }
}
