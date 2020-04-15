package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class Sidebar extends AbstractFragment {
    private static final By SIDEBAR = By.className("gdc-ldm-sidebar");

    @FindBy(className = "gdc-ldm-add-section")
    private AddSection addSection;

    public static final Sidebar getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(Sidebar.class, waitForElementVisible(SIDEBAR, searchContext));
    }

    public AddSection getAddSection() {
        return addSection;
    }
}
