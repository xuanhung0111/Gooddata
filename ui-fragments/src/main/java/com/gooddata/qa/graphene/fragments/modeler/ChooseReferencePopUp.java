package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.cssSelector;

public class ChooseReferencePopUp extends AbstractFragment {
    private static final String CHOOSE_REFERENCE = ".choose-reference .choose-reference-content";

    @FindBy(css = ".gdc-ldm-search-dataset-list .gd-list .dataset-list-item")
    private List<WebElement> listItems;

    public static ChooseReferencePopUp getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ChooseReferencePopUp.class, waitForElementVisible(cssSelector(CHOOSE_REFERENCE), searchContext));
    }

    public List<String> getlistReferenceText() {
        return listItems.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public DatasetEdit selectReferenceByName(String referenceName) {
        WebElement item = listItems.stream()
                .filter(input -> input.getText().equals(referenceName))
                .findFirst()
                .get();
        item.click();
        return DatasetEdit.getInstance(browser);
    }
}
