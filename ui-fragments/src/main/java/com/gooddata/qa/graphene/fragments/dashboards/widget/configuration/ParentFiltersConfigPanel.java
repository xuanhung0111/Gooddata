package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import org.apache.commons.lang3.text.WordUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ParentFiltersConfigPanel extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class, 's-Parent') and contains(@class,'s-enabled')]")
    private WebElement parentFilterTab;

    @FindBy(xpath = "//button[contains(@class, 's-btn-add_parent_filter')]")
    private WebElement addParentFilterButton;

    @FindBy(xpath = "//button[contains(@class, 's-btn-apply')]")
    private WebElement applyButton;

    private static final String PARENT_FILTER_LOCATOR =
            "div.picker-item-content:not(.yui3-overlay-hidden) div.yui3-widget-stdmod span[title='${parentFilter}']";

    public void addParentsFilter(String... parentFilterNames) {
        for (String parentFilterName : parentFilterNames) {
            waitForElementVisible(parentFilterTab).click();
            waitForElementVisible(addParentFilterButton).click();
            By parentFilter =
                    By.cssSelector(PARENT_FILTER_LOCATOR.replace("${parentFilter}",
                            WordUtils.capitalizeFully(parentFilterName)));
            waitForElementVisible(parentFilter, browser).click();
            waitForElementVisible(applyButton).click();
        }
    }
}
