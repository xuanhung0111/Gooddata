package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.isNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.google.common.base.Predicate;

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
            selectRandomLeftItem();
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

//    public boolean isPrivateItemOnRightButton(String item, String group, Predicate<String> howToCheck) {
//        boolean result;
//        ItemPanel itemPanel = addNewDrillingPanel();
//        try {
//            selectRandomLeftItem();
//            result = rightPopupPanel.getItems().stream().filter(item::equals).anyMatch(item::is);
//            rightPopupPanel.cancelPanel();
//        } finally {
//            itemPanel.delete();
//        }
//        return result;
//    }

    public Pair<List<String>, List<String>> getAllValueLists(int indexOfPanel) {
        ItemPanel itemPanel = getItemPanelByIndex(indexOfPanel);
        SelectItemPopupPanel leftPopupPanel = itemPanel.openLeftPopup();
        List<String> listLeftItems = leftPopupPanel.getItems();
        leftPopupPanel.cancelPanel();

        SelectItemPopupPanel rightPopupPanel = itemPanel.openRightPopup();
        List<String> listRightItems = rightPopupPanel.getItems();
        rightPopupPanel.cancelPanel();
        return Pair.of(listLeftItems, listRightItems);
    }

    public Pair<String, String> getSettingsOnLastItemPanel() {
        ItemPanel panel = getLastItemPanel();
        return Pair.of(panel.getLeftItemValue(), panel.getRightItemValue());
    }


    public DrillingConfigPanel openNewInnerDrillPanel() {
        getLastItemPanel().openNewInnerDrillPanel();
        return this;
    }

    public List<Pair<String, String>> getAllInnerDrillSettingsOnLastPanel() {
        return getLastItemPanel().getAllInnerDrillSettings();
    }

    public DrillingConfigPanel addInnerDrillToLastItemPanel(Pair<List<String>, String> innerDrillSetting) {
        addInnerDrill(getLastItemPanel(), innerDrillSetting);
        return this;
    }

    public String getTooltipFromHelpIcon(String group) {
        String toolTip;
        ItemPanel itemPanel = addNewDrillingPanel();

        try {
            selectRandomLeftItem();
            toolTip = itemPanel.getToolTipHelpIcon(group);
        } finally {
            itemPanel.delete();
        }
        return toolTip;
    }

    public boolean canAddInnerDrill() {
        return getLastItemPanel().canAddInnerDrill();
    }

    public List<String> getTabs() {
        List<String> tabs;
        ItemPanel itemPanel = addNewDrillingPanel();

        try {
            selectRandomLeftItem();
            tabs = itemPanel.getTabs();
        } finally {
            itemPanel.delete();
        }
        return tabs;
    }

    // Using in cases which item on left does not affect to right item values
    private DrillingConfigPanel selectRandomLeftItem() {
        SelectItemPopupPanel leftPopupPanel = getLastItemPanel().openLeftPopup();
        leftPopupPanel.searchAndSelectItem(leftPopupPanel.getItemElements().get(0).getText()).submitPanel();
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

        return getLastItemPanel();
    }

    private ItemPanel getItemPanelByIndex(int index) {
        return Graphene.createPageFragment(ItemPanel.class, drillItemPanelList.get(index));
    }

    private ItemPanel getLastItemPanel() {
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

        private final By helpIcon = By.cssSelector(".overlayPlugin-plugged:not(.gdc-hidden) " +
                ".yui3-c-container-content:not(.gdc-hidden) .pickerHelp .inlineBubbleHelp)");
        private final By tabs = By.cssSelector(".yui3-attributeorreportpickerpanel-content .button-primary");

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

        private boolean canAddInnerDrill() {
            return waitForElementPresent(addNewInnerDrill).isDisplayed();
        }

        private List<Pair<String, String>> getAllInnerDrillSettings() {
            return innerDrillItemPanelList.stream()
                    .map(panel -> Pair.of(panel.getLeftItemValue(), panel.getRightItemValue()))
                    .collect(Collectors.toList());
        }

        private String getToolTipHelpIcon(String group) {
            String toolTip = openRightPopup().changeGroup(group).getRoot().findElement(helpIcon).getAttribute("title");
            getPopupInstance().cancelPanel();
            return toolTip;
        }

        private List<String> getTabs() {
            List<String> listTab = getElementTexts(openRightPopup().getRoot().findElements(tabs));
            getPopupInstance().cancelPanel();
            return listTab;
        }

        private boolean isPrivateItem(String item, String group) {
            boolean result = findItemFrom(item, openRightPopup().changeGroup(group).getItemElements())
                    .get().getAttribute("class").contains("is-unlisted");
            getPopupInstance().cancelPanel();
            return result;
        }

        private Optional<WebElement> findItemFrom(final String item, final Collection<WebElement> collection) {
            return waitForCollectionIsNotEmpty(collection)
                    .stream()
                    .filter(e -> item.equals(e.findElement(By.cssSelector("label,.label")).getText()))
                    .findFirst();
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
