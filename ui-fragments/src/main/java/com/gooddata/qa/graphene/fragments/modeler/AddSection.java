package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class AddSection extends AbstractFragment {
    private static final By ADD_SECTION = By.className("gdc-ldm-add-section");

    @FindBy(className = "gdc-ldm-add-dataset")
    private WebElement addDataset;

    @FindBy(className = "gdc-ldm-add-date")
    private WebElement addDate;

    public static AddSection getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(AddSection.class, waitForElementVisible(ADD_SECTION, searchContext));
    }

    public boolean isDatasetButtonVisible() {
        return isElementVisible(addDataset);
    }

    public boolean isDateButtonVisible() {
        return isElementVisible(addDate);
    }

    public WebElement getButtonDataset() {
        return addDataset;
    }

    public WebElement getButtonDate() {
        return addDate;
    }
}
