package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class ToolBar extends AbstractFragment {
    private static final By TOOLBAR =  By.className("gdc-ldm-toolbar");

    @FindBy(className = "gd-button-text")
    private List<WebElement> buttons;

    public static final ToolBar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(ToolBar.class, waitForElementVisible(TOOLBAR, searchContext));
    }

    public boolean isButtonsVisible() {
        return buttons.size() == 1 ? true : false;
    }
}
