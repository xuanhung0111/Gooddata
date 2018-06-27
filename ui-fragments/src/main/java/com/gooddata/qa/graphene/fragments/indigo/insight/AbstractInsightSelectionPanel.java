package com.gooddata.qa.graphene.fragments.indigo.insight;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.dialog.SaveInsightDialog;
import com.gooddata.qa.graphene.utils.Sleeper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForIndigoMessageDisappear;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

@SuppressWarnings("unchecked")
public abstract class AbstractInsightSelectionPanel extends AbstractFragment {

    @FindBy(css = ".gd-input-search input")
    private WebElement searchTextBox;

    protected static final By NO_DATA_LOCATOR = className("gd-no-data");
    protected static final By CLEAR_ICON_LOCATOR = className("icon-clear");

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
                .filter(e -> {
                    // handle shortened Isight name
                    String insightItem = e.getName();
                    if (isShortenedTitle(insightItem)) {
                        return compareShortenedTitle(insightItem,insightName);
                    } else {
                        return e.getName().equals(insightName) || e.matchesTitle(insightName);
                    }
                })
                .findFirst()
                .get();
    }

    private boolean compareShortenedTitle(String insightItem, String searchString) {
        return searchString.startsWith(insightItem.split("…")[0]) && searchString.endsWith(insightItem.split("…")[1]);
    }

    private boolean isShortenedTitle(String insightName) {
        if (insightName.contains("…")) {
            log.info("shortened Insight name: " + insightName);
            return true;
        }

        return false;
    }

    public boolean searchInsight(final String insight) {
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
        // Make some sleep here to wait for spinner element appear
        Sleeper.sleepTight(500);

        waitForElementNotPresent(cssSelector(".gd-spinner.large"));
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
            waitForIndigoMessageDisappear(browser);
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

        public boolean hasLockedIcon() {
            return isElementVisible(By.className("icon-lock"), getRoot());
        }

        public boolean hasVisibleDeleteButton() {
            return isElementVisible(By.className("gd-visualizations-list-item-action-delete"), getRoot());
        }

    }
}
