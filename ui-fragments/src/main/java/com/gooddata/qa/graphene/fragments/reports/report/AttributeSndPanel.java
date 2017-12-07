package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.Collection;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.nonNull;

public class AttributeSndPanel extends AbstractSndPanel {

    @Override
    protected WebElement getViewGroupsContainerRoot() {
        return getRoot().findElement(By.className("s-snd-DimensionsContainer"));
    }

    @Override
    protected WebElement getItemContainerRoot() {
        return getRoot().findElement(By.className("s-snd-AttributesContainer")).findElement(BY_PARENT);
    }

    @Override
    protected WebElement getItemDetailContainerRoot() {
        return getRoot().findElement(By.className("sndAttributeDetailContainer")).findElement(BY_PARENT);
    }

    public AttributeSndPanel selectAttribtues(Collection<HowItem> hows) {
        hows.forEach(how -> {
            selectItem(how.getAttribute().getName());

            if (nonNull(how.getPosition())) {
                selectAttributePosition(how.getAttribute().getName(), how.getPosition());
            }

            if (!how.getFilterValues().isEmpty()) {
                filterValues(how.getFilterValues());
            }
        });
        return this;
    }

    public AttributeSndPanel selectAttributePosition(String attribute, Position position) {
        WebElement posElement = findItemElement(attribute).findElement(By.className("sndAttributePosition"));

        if (!posElement.getAttribute("class").contains(position.getDirection())) {
            posElement.click();
            waitForElementAttributeContainValue(posElement, "class", position.getDirection());
        }

        return this;
    }

    public boolean isUnReachable(String... attributes) {
        return Stream.of(attributes).allMatch(attribute ->
                findItemElement(attribute).getAttribute("class").contains("sndUnReachable"));
    }

    public String getUnReachableAttributeDescription(String attribute) {
        return findItemElement(attribute).getAttribute("title");
    }

    public AttributeSndPanel openAttributeDetail(String attribute) {
        findItemElement(attribute).click();
        waitForElementVisible(getItemDetailContainerRoot());
        return this;
    }

    public AttributeSndPanel changeDisplayLabel(String label) {
        new Select(getItemDetailContainerRoot().findElement(By.tagName("select"))).selectByVisibleText(label);
        return this;
    }

    public AttributeSndPanel filterValues(Collection<String> values) {
        getItemDetailContainerRoot().findElement(By.className("s-btn-filter_this_attribute")).click();
        SelectItemPopupPanel.getInstance(By.className("c-attributeElementsFilterEditor"), browser)
                .searchAndSelectItems(values);
        return this;
    }

    public AttributeSndPanel deleteFilter() {
        WebElement deleteButton = getItemDetailContainerRoot().findElement(By.className("deleteFilter"));
        deleteButton.click();
        waitForElementNotVisible(deleteButton);
        return this;
    }
}
