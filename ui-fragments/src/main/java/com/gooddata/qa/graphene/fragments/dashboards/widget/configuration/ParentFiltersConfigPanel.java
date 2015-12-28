package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ParentFiltersConfigPanel extends AbstractFragment {

    @FindBy(className = "s-btn-add_parent_filter")
    private WebElement addParentFilterButton;

    @FindBy(xpath = "//button[contains(@class, 's-btn-apply')]")
    private WebElement applyButton;

    private static final String PARENT_FILTER_LOCATOR =
            "div.picker-item-content:not(.yui3-overlay-hidden) span[title='${parentFilter}']";
    private static final String LINKED_DATASET_LOCATOR =
            ".yui3-widget-stacked:not(.yui3-overlay-hidden) span[title='${dataset}']";

    public void addParentsFilterUsingDataset(String dataSet, String... parentFilterNames) {
        for (String parentFilterName : parentFilterNames) {
            waitForElementVisible(addParentFilterButton).click();
            By parentFilter = By.cssSelector(PARENT_FILTER_LOCATOR.replace("${parentFilter}", parentFilterName));
            waitForElementVisible(parentFilter, browser).click();

            if (dataSet != null) {
                waitForElementVisible(By.className("s-btn-select_dataset____"), getRoot()).click();
                By linkedDataset = By.cssSelector(LINKED_DATASET_LOCATOR.replace("${dataset}", dataSet));
                waitForElementVisible(linkedDataset, browser).click();
            }
        }

        waitForElementVisible(applyButton).click();
    }

    public void addParentsFilter(String... parentFilterNames) {
        addParentsFilterUsingDataset(null, parentFilterNames);
    }
}
