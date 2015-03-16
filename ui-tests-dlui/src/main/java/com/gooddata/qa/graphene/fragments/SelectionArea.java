package com.gooddata.qa.graphene.fragments;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.Field;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class SelectionArea extends AbstractFragment {

    private By BY_ICON_CROSS = By.cssSelector(".icon-cross");

    private String XPATH_SELECTED_FIELD =
            "//.[contains(@class, 'checked-item') and text()='${selectedFieldTitle}']/..";

    @FindBy(xpath = "//.[contains(@class, 'checked-item-title')]")
    private List<WebElement> selectedFields;

    public void deselectFields(Field... fields) {
        for (Field field : fields) {
            WebElement selectedField =
                    waitForElementVisible(
                            By.xpath(XPATH_SELECTED_FIELD.replace("${selectedFieldTitle}",
                                    field.getName())), getRoot());
            waitForElementVisible(BY_ICON_CROSS, selectedField).click();
            waitForElementNotPresent(selectedField);
        }
    }

    public void checkSelectedFields(Collection<Field> expectedFields) {
        waitForCollectionIsNotEmpty(selectedFields);
        assertEquals(selectedFields.size(), expectedFields.size(),
                "The number of selected fields is incorrect!");
        List<String> selectedFieldTitles = Lists.newArrayList();
        for (final WebElement selectedField : selectedFields) {
            assertTrue(Iterables.any(expectedFields, new Predicate<Field>() {

                @Override
                public boolean apply(Field arg0) {
                    return arg0.getName().equals(selectedField.getText());
                }
            }), "The field " + selectedField.getText() + "is not selected!");
            selectedFieldTitles.add(selectedField.getText());
        }
        assertTrue(Ordering.natural().isOrdered(selectedFieldTitles),
                "Selected fields are not sorted!");
    }
}
