package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.isNull;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class DrillingConfigPanel extends AbstractFragment {

    @FindBy(css = ".addMoreDrill:not(.gdc-hidden) button, .drillInfoText:not(.gdc-hidden) button")
    private WebElement addDrillingButton;

    @FindBy(className = "yui3-drillitempanel")
    private List<WebElement> drillItemPanelList;

    public DrillingConfigPanel addDrilling(Pair<List<String>, String> pairs, String group) {
        addNewDrillingPanel().selectLeftItem(pairs.getLeft()).selectRightItem(pairs.getRight(), group);

        return this;
    }

    public DrillingConfigPanel editDrilling(Pair<List<String>, String> oldDrilling,
                                            Pair<List<String>, String> newDrilling, String group) {
        getItemPanelBySelectedValues(oldDrilling.getLeft(), oldDrilling.getRight())
                .selectLeftItem(newDrilling.getLeft())
                .selectRightItem(newDrilling.getRight(), group);

        return this;
    }

    public DrillingConfigPanel deleteDrillingByLeftValues(List<String> leftValues) {
        getItemPanelBySelectedValues(leftValues, null).delete();

        return this;
    }

    public boolean isValueOnRightButton(String value, String group) {
        boolean result;

        ItemPanel itemPanel = addNewDrillingPanel();

        try {
            // choosing #1 item on left item does not affect to right item values
            SelectItemPopupPanel leftPopupPanel = itemPanel.openLeftPopup();
            leftPopupPanel.searchAndSelectItem(leftPopupPanel.getItemElements().get(0).getText()).submitPanel();

            SelectItemPopupPanel rightPopupPanel = itemPanel.openRightPopup()
                    .changeGroup(group)
                    .searchItem(value);

            result = rightPopupPanel.getItems().stream().anyMatch(value::equals);
            rightPopupPanel.cancelPanel();
        } finally {
            itemPanel.delete();
        }

        return result;
    }

    public List<String> getRightItemValues() {
        SelectItemPopupPanel rightPopupPanel = getLasItemPanel().openRightPopup();
        List<String> listItems = rightPopupPanel.getItems();
        rightPopupPanel.cancelPanel();
        return listItems;
    }

    public Pair<String, String> getSettingsOnLastItemPanel() {
        ItemPanel panel = getLasItemPanel();
        return Pair.of(panel.getLeftItemValue(), panel.getRightItemValue());
    }

    public List<Pair<String, String>> getAllInnerDrillSettingsOnLastPanel() {
        return getLasItemPanel().getAllInnerDrillSettings();
    }

    public DrillingConfigPanel addInnerDrillToLastItemPanel(Pair<List<String>, String> innerDrillSetting) {
        addInnerDrill(getLasItemPanel(), innerDrillSetting);
        return this;
    }

    private ItemPanel addInnerDrill(ItemPanel selectedPanel, Pair<List<String>, String> innerDrillSetting) {
        return selectedPanel.openNewInnerDrillPanel()
                .selectLeftItem(innerDrillSetting.getLeft())
                .selectRightItem(innerDrillSetting.getRight(), null);
    }

    private ItemPanel addNewDrillingPanel() {
        waitForElementVisible(addDrillingButton).click();

        // handle case having more than 2 drilling panels
        scrollElementIntoView(
                waitForElementVisible(By.className("s-btn-select_metric___attribute___"), getRoot()), browser);

        return getLasItemPanel();
    }

    private ItemPanel getItemPanelByIndex(int index) {
        return Graphene.createPageFragment(ItemPanel.class, drillItemPanelList.get(index));
    }

    private ItemPanel getLasItemPanel() {
        return getItemPanelByIndex(drillItemPanelList.size() - 1);
    }

    private ItemPanel getItemPanelBySelectedValues(List<String> leftValues, String rightValue) {
        if (isNull(leftValues) && isNull(rightValue))
            throw new IllegalArgumentException("left & right values can't be null");

        return drillItemPanelList.stream()
                .map(element -> Graphene.createPageFragment(ItemPanel.class, element))
                .filter(panel -> {
                    boolean result = true;
                    if (!isNull(leftValues))
                        result = leftValues.stream().collect(Collectors.joining(", ")).equals(panel.getLeftItemValue());


                    if (!isNull(rightValue) && !result)
                        result = rightValue.equals(panel.getRightItemValue());

                    return result;
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Can't find item panel having given values"));
    }

    private class ItemPanel extends AbstractFragment {
        @FindBy(css = "button.leftCol")
        private WebElement leftButton;

        @FindBy(css = "button.rightCol")
        private WebElement rightButton;

        @FindBy(className = "deleteButton ")
        private WebElement deleteButton;

        @FindBy(className = "innerDrills")
        private List<ItemPanel> innerDrillItemPanelList;

        @FindBy(className = "addMoreDetail")
        private WebElement addNewInnerDrill;

        private ItemPanel selectLeftItem(List<String> names) {
            openLeftPopup().searchAndSelectItems(names).submitPanel();

            return this;
        }

        private ItemPanel selectRightItem(String name, String group) {
            SelectItemPopupPanel panel = openRightPopup();

            if (!isNull(group)) {
                panel.changeGroup(group);
            }

            panel.searchAndSelectItem(name).submitPanel();

            return this;
        }

        private SelectItemPopupPanel getPopupInstance() {
            Predicate<WebDriver> isPanelDisplayed = driver -> driver.findElements(SelectItemPopupPanel.LOCATOR).size() > 1;
            Graphene.waitGui().until(isPanelDisplayed);

            // always get #2 found element
            return Graphene.createPageFragment(SelectItemPopupPanel.class,
                    browser.findElements(SelectItemPopupPanel.LOCATOR).get(1));
        }

        private SelectItemPopupPanel openLeftPopup() {
            waitForElementVisible(leftButton).click();
            return getPopupInstance();
        }

        private SelectItemPopupPanel openRightPopup() {
            waitForElementVisible(rightButton).click();
            return getPopupInstance();
        }

        private String getLeftItemValue() {
            return waitForElementVisible(leftButton).getText();
        }

        private String getRightItemValue() {
            return waitForElementVisible(rightButton).getText();
        }

        private void delete() {
            waitForElementVisible(deleteButton).click();
        }

        private ItemPanel openNewInnerDrillPanel() {
            waitForElementVisible(addNewInnerDrill).click();
            return innerDrillItemPanelList.get(innerDrillItemPanelList.size() -1);
        }

        private List<Pair<String, String>> getAllInnerDrillSettings() {
            return innerDrillItemPanelList.stream()
                    .map(panel -> Pair.of(panel.getLeftItemValue(), panel.getRightItemValue()))
                    .collect(Collectors.toList());
        }
    }

    public enum DrillingGroup {
        ATTRIBUTES("Attributes"),
        REPORTS("Reports"),
        DASHBOARDS("Dashboards");

        private String group;

        DrillingGroup(String group) {
            this.group = group;
        }

        public String getName() {
            return this.group;
        }
    }
}
