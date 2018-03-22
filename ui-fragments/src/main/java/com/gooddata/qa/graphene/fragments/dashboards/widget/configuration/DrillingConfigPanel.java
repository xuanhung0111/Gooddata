package com.gooddata.qa.graphene.fragments.dashboards.widget.configuration;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.isNull;

public class DrillingConfigPanel extends AbstractFragment {

    @FindBy(css = ".addMoreDrill:not(.gdc-hidden) button, .drillInfoText:not(.gdc-hidden) button")
    private WebElement addDrillingButton;

    @FindBy(className = "yui3-drillitempanel")
    private List<WebElement> drillItemPanelList;

    private static final By HELP_ICON = By.xpath("//div[contains(@class, 'yui3-c-container-content') and " +
            "not(contains(@class,'gdc-hidden'))]//*[contains(@class, 'pickerHelp')]//*[contains(@class, 'inlineBubbleHelp')]");

    private static final By TABS = By.xpath("//*[contains(@class, 'yui3-attributeorreportpickerpanel-content')]" +
            "//*[contains(@class, 'button-primary')]");

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

        SelectItemPopupPanel rightPopupPanel = openRightPopupPanel(group);
        try {
            result = rightPopupPanel.searchItem(value).getItems().stream().anyMatch(value::equals);
            rightPopupPanel.cancelPanel();
        } finally {
            getLastItemPanel().delete();
        }

        return result;
    }

    public Pair<List<String>, List<String>> getAllValueLists(int indexOfPanel) {
        List<String> listRightItems = null;
        ItemPanel itemPanel = getItemPanelByIndex(indexOfPanel);
        SelectItemPopupPanel leftPopupPanel = itemPanel.openLeftPopup();
        List<String> listLeftItems = leftPopupPanel.getItems();
        leftPopupPanel.cancelPanel();

        if(itemPanel.getLeftItemValue() != "Select Metric / Attribute...") {
            SelectItemPopupPanel rightPopupPanel = itemPanel.openRightPopup();
            listRightItems = rightPopupPanel.getItems();
            rightPopupPanel.cancelPanel();    
        }
        return Pair.of(listLeftItems, listRightItems);
    }

    public Pair<String, String> getSettingsOnLastItemPanel() {
        ItemPanel panel = getLastItemPanel();
        return Pair.of(panel.getLeftItemValue(), panel.getRightItemValue());
    }

    public DrillingConfigPanel openNewInnerDrillPanel(int index) {
        getItemPanelByIndex(index).openNewInnerDrillPanel();
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
        SelectItemPopupPanel rightPopupPanel = openRightPopupPanel(group);
        String toolTip = rightPopupPanel.getRoot().findElement(HELP_ICON).getAttribute("title");
        rightPopupPanel.cancelPanel();
        getLastItemPanel().delete();
        return toolTip;
    }

    public boolean canAddInnerDrill() {
        return getLastItemPanel().addInnerDrillButton().isDisplayed();
    }

    public List<String> getRightItemGroups(String group) {
        SelectItemPopupPanel rightPopupPanel = openRightPopupPanel(group);
        List<String> tabs = getElementTexts(rightPopupPanel.getRoot().findElements(TABS));
        rightPopupPanel.cancelPanel();
        getLastItemPanel().delete();
        return tabs;
    }

    /**
     * Adding a new item panel to  get information from right popup panel
     * so that should delete it after using
     */
    private SelectItemPopupPanel openRightPopupPanel(String group) {
        ItemPanel itemPanel = addNewDrillingPanel();
        //item on left does not affect to right item values
        SelectItemPopupPanel leftPopupPanel = getLastItemPanel().openLeftPopup();
        leftPopupPanel.searchAndSelectItem(leftPopupPanel.getItemElements().get(0).getText()).submitPanel();
        return itemPanel.openRightPopup().changeGroup(group);
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

        @FindBy(className = "deleteButton")
        private WebElement deleteButton;

        @FindBy(className = "innerDrills")
        private List<ItemPanel> innerDrillItemPanelList;

        @FindBy(className = "addMoreDetail")
        private WebElement addNewInnerDrill;

        @FindBy(className = "spinnerDrilling")
        private WebElement drillingSpinner;

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
            waitForElementNotVisible(drillingSpinner);

            return this;
        }

        private SelectItemPopupPanel getPopupInstance() {
            WebElement root = browser.findElements(SelectItemPopupPanel.LOCATOR).get(1);
            List<WebElement> elements = root.findElements(By.cssSelector(".gdc-overlay-simple:not(.hidden)" +
                    ":not(.yui3-overlay-hidden):not(.ember-view) div.yui3-c-container-content:not(.m9):not" +
                    "(footer)"));

            for (WebElement element : elements) {
                if (isElementVisible(element)) {
                    waitForElementVisible(By.cssSelector(".yui3-c-simpleColumn-window.loaded"), element);
                    break;
                }
            }

            // SelectItemPopupPanel.getInstance does not work in this case
            return Graphene.createPageFragment(SelectItemPopupPanel.class, root);
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

        private WebElement addInnerDrillButton() {
            return waitForElementPresent(addNewInnerDrill);
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
