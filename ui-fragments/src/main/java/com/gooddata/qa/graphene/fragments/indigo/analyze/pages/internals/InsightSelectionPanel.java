package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.google.common.base.Predicate;

public class InsightSelectionPanel extends AbstractFragment {

    private static final By NO_DATA_LOCATOR = className("gd-no-data");
    private static final By CLEAR_ICON_LOCATOR = className("icon-clear");
    private static final By SEARCH_TEXTBOX_LOCATOR = className("searchfield-input");
    private static final By ROOT_LOCATOR = className("open-visualizations");

    public static InsightSelectionPanel getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(InsightSelectionPanel.class,
                waitForElementVisible(ROOT_LOCATOR, searchContext));
    }
    public List<InsightItem> getInsightItems() {
        waitForLoading();
        return getRoot().findElements(className("gd-visualizations-list-item")).stream()
                .map(element -> Graphene.createPageFragment(InsightItem.class, element)).collect(toList());
    }

    public InsightItem getInsightItem(final String insightName) {
        if (!searchInsight(insightName)) {
            throw new RuntimeException("Can't find insight: " + insightName);
        }

        return getInsightItems().stream()
                .filter(e -> insightName.equals(e.getName())).findFirst().get();
    }

    public boolean searchInsight(final String insight) {
        if (isEmpty())
            return false;

        clearInputText();
        waitForElementVisible(getRoot().findElement(SEARCH_TEXTBOX_LOCATOR))
                .sendKeys(insight);

        return !isEmpty();
    }

    public void openInsight(final String insight) {
        if (!getRoot().isDisplayed())
            throw new RuntimeException("The insight selection panel is collapsed");
        getInsightItem(insight).open();
    }

    public boolean isEmpty() {
        waitForLoading();
        return isElementVisible(NO_DATA_LOCATOR, getRoot());
    }

    public InsightSelectionPanel switchFilter(FilterType type) {
        getFilterElement(type).click();
        waitForLoading();
        return this;
    }

    public boolean isFilterActive(FilterType type) {
        return getFilterElement(type).getAttribute("class").contains("is-active");
    }

    public InsightSelectionPanel clearInputText() {
        if (isElementPresent(CLEAR_ICON_LOCATOR, getRoot())) {
            WebElement clearIcon = getRoot().findElement(CLEAR_ICON_LOCATOR);
            waitForElementVisible(clearIcon).click();
            waitForElementNotVisible(clearIcon);
            waitForLoading();
        }
        return this;
    }

    public InsightSelectionPanel waitForLoading() {
        Predicate<WebDriver> isDataLoaded = browser ->
                !isElementPresent(cssSelector(".gd-spinner.large"), getRoot());
        Graphene.waitGui().until(isDataLoaded);
        return this;
    }

    private WebElement getFilterElement(FilterType type) {
        waitForLoading();
        return getRoot().findElements(className("gd-tab"))
                .stream()
                .filter(e -> type.toString().equals(e.getText()))
                .findFirst()
                .get();
    }

    public static enum FilterType {
        BY_ME("created by me"),
        ALL("all");

        private String type;

        private FilterType(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    public static class InsightItem extends AbstractFragment {
        @FindBy(className = "gd-visualizations-list-item-content-name")
        private WebElement nameLabel;

        @FindBy(className = "gd-visualizations-list-item-action-delete")
        private WebElement deleteIcon;

        @FindBy(className = "gd-vis-type")
        private WebElement vizTypeIcon;

        public String getName() {
            return waitForElementVisible(nameLabel).getText();
        }

        public void delete() {
            getActions().moveToElement(getRoot()).perform();
            waitForElementVisible(deleteIcon).click();
            SaveInsightDialog.getInstance(browser).clickSubmitButton();
        }

        public void open() {
            getRoot().click();
        }

        public String getVizType() {
            return waitForElementVisible(vizTypeIcon)
                    .getAttribute("class")
                    .replaceAll("(gd-vis-type|-)", "")
                    .trim();
        }
    }
}
