package com.gooddata.qa.graphene.fragments.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DateDimensionSelect extends AbstractReactDropDown {

    public boolean isEnabled() {
        return !getDropdownButton().getAttribute("class").contains("disabled");
    }

    public String getHiddenDescription() {
        ensureDropdownOpen();
        return waitForElementVisible(By.className("gd-list-footer"), getPanelRoot()).getText();
    }

    public DateDimensionGroup getDateDimensionGroup(final String name) {
        return getDateDimensionGroups()
                .stream()
                .filter(group -> name.equals(group.getName()))
                .findFirst()
                .get();
    }

    public List<DateDimensionGroup> getDateDimensionGroups() {
        ensureDropdownOpen();

        List<DateDimensionGroup> dateDimensionGroups = new ArrayList<>();
        List<WebElement> dateDimensions = getPanelRoot().findElements(By.cssSelector(".gd-list-item"));

        // Group all date dimensions to DEFAULT when there is not any Recommended for them
        if (!isHeaderItem(dateDimensions.get(0))) {
            dateDimensionGroups.add(new DateDimensionGroup("DEFAULT")
                    .addDateDimensions(dateDimensions
                            .stream()
                            .map(e -> e.getText())
                            .collect(toList())));
            return dateDimensionGroups;
        }

        // Collect all groups for date dimensions with given Recommended
        DateDimensionGroup currentGroup = null;
        for (WebElement dateDimension : dateDimensions) {
            if(isHeaderItem(dateDimension)) {
                if (currentGroup != null) dateDimensionGroups.add(currentGroup);
                currentGroup = new DateDimensionGroup(dateDimension.getText());
            } else {
                currentGroup.addDateDimension(dateDimension.getText());
            }
        }

        dateDimensionGroups.add(currentGroup);
        return dateDimensionGroups;
    }

    public boolean isScrollable() {
        return isElementPresent(By.cssSelector(".configuration-dropdown.dataSets-list .public_Scrollbar_face"),
                browser);
    }

    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.dropdown-body";
    }

    @Override
    protected boolean isDropdownOpen() {
        return getDropdownButton().getAttribute("class").contains("is-dropdown-open");
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header'])";
    }

    @Override
    protected WebElement getElementByName(final String name) {
        waitForPickerLoaded();
        return getElements()
                .stream()
                .filter(e -> name.equals(e.getText()))
                .findFirst()
                .get();
    }

    private boolean isHeaderItem(WebElement item) {
        return item.getAttribute("class").contains("item-header");
    }

    public class DateDimensionGroup {
        private String name;
        private List<String> dateDimensions = new ArrayList<>();

        private DateDimensionGroup(String name) {
            this.name = name;
        }

        private DateDimensionGroup addDateDimension(String dateDimension) {
            this.dateDimensions.add(dateDimension);
            return this;
        }

        private DateDimensionGroup addDateDimensions(List<String> dateDimensions) {
            this.dateDimensions.addAll(dateDimensions);
            return this;
        }

        public String getName() {
            return name;
        }

        public List<String> getDateDimensions() {
            return dateDimensions;
        }
    }
}
