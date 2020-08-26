package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;


public class IndigoTableDropDown extends AbstractFragment {
    private static final By INDIGO_TABLE_DROPDOWN = By.className("indigo-table-dropdown-body");

    public static final IndigoTableDropDown getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(IndigoTableDropDown.class, waitForElementVisible(INDIGO_TABLE_DROPDOWN, searchContext));
    }

    @FindBy(className = "gd-list-item")
    List<WebElement> dropdownOption;

    public List<String> getListDropdownOption() {
        List<String> listOption = new ArrayList<String>();
        for(WebElement option: dropdownOption) {
            scrollElementIntoView(option, browser);
            listOption.add(option.getText());
        }
        return listOption;
    }
}
