package com.gooddata.qa.graphene.fragments.indigo.insight;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.google.common.base.Predicate;

@SuppressWarnings("unchecked")
public abstract class AbstractInsightSelectionPanel extends AbstractFragment {

    @FindBy(className = "searchfield-input")
    private WebElement searchTextBox;

    protected static final By NO_DATA_LOCATOR = className("gd-no-data");
    protected static final By CLEAR_ICON_LOCATOR = className("icon-clear");
    protected static final By SEARCH_TEXTBOX_LOCATOR = className("searchfield-input");

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
                // find by either non-shortened title or fallback to s-class if not found (item shoretened)
                .filter(e -> e.getName().equals(insightName) || e.matchesTitle(insightName))
                .findFirst()
                .get();
    }

    public boolean searchInsight(final String insight) {
        if (isEmpty())
            return false;

        clearInputText();
        waitForElementVisible(searchTextBox).sendKeys(insight);

        return !isEmpty();
    }

    public boolean isEmpty() {
        waitForLoading();
        return isElementVisible(NO_DATA_LOCATOR, getRoot());
    }

    public <T extends AbstractInsightSelectionPanel> T switchFilter(FilterType type) {
        getFilterElement(type).click();
        waitForLoading();
        return (T) this;
    }

    public boolean isFilterActive(FilterType type) {
        return getFilterElement(type).getAttribute("class").contains("is-active");
    }

    public <T extends AbstractInsightSelectionPanel> T clearInputText() {
        if (isElementPresent(CLEAR_ICON_LOCATOR, getRoot())) {
            WebElement clearIcon = getRoot().findElement(CLEAR_ICON_LOCATOR);
            waitForElementVisible(clearIcon).click();
            waitForElementNotVisible(clearIcon);
            waitForLoading();
        }
        return (T) this;
    }


    public <T extends AbstractInsightSelectionPanel> T waitForLoading() {
        Predicate<WebDriver> isDataLoaded = browser ->
                !isElementPresent(cssSelector(".gd-spinner.large"), getRoot());
        Graphene.waitGui().until(isDataLoaded);
        return (T) this;
    }

    public boolean isFilterVisible(FilterType type) {
        return isElementVisible(getFilterElement(type));
    }

    public boolean isSearchTextBoxEmpty() {
        return waitForElementVisible(searchTextBox).getText().isEmpty();
    }

    public <T extends AbstractInsightSelectionPanel> T waitForInsightListVisible() {
        waitForElementVisible(className("gd-infinite-list"), getRoot());
        return (T) this;
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

        public String getCSSClass() {
            return getRoot().getAttribute("class");
        }

        public boolean matchesTitle(String title) {
            String titleSelector = "s-" + simplifyText(title);
            return getCSSClass().contains(titleSelector);
        }

        public void delete() {
            // hover on type icon to avoid tooltip which is only displayed with long name
            getActions().moveToElement(vizTypeIcon).perform();
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
