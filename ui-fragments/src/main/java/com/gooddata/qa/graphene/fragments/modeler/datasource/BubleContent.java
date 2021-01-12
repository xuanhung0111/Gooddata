package com.gooddata.qa.graphene.fragments.modeler.datasource;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class BubleContent extends AbstractFragment {
    private static final String BUBLE_CONTENT = "bubble-content";

    @FindBy(css = ".content .item-name")
    private WebElement itemName;

    @FindBy(css = ".content .item-group")
    private WebElement itemGroup;

    public static BubleContent getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                BubleContent.class, waitForElementVisible(className(BUBLE_CONTENT), searchContext));
    }

    public boolean isPopUpContainsList(List<String> listColumns) {
        return listColumns.stream().anyMatch(itemGroup.getText()::contains);
    }

    public String getItemName() {
        return itemName.getText();
    }
}
