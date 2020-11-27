package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class LdmControlLoad extends AbstractFragment {
    private static final By LDM_CONTROL_LOAD = By.className("ldm-control-load");

    @FindBy(css = ".distributed-load input")
    private WebElement checkBoxDistributedLoad;

    @FindBy(className = "ldm-control-load-button")
    private WebElement controlLoadButton;

    public static LdmControlLoad getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                LdmControlLoad.class, waitForElementVisible(LDM_CONTROL_LOAD, searchContext));
    }

    public LdmControlLoad toogleDistributedLoad() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(checkBoxDistributedLoad).click().perform();
        return this;
    }

    public LdmControlLoad toogleIncrementalLoad() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(controlLoadButton).click().perform();
        ControlLoadMenu.getInstance(browser).toogleIncrementalLoad();
        driverActions.moveToElement(controlLoadButton).click().perform();
        return this;
    }

    public LdmControlLoad toogleDeletedRowsLoad() {
        Actions driverActions = new Actions(browser);
        driverActions.moveToElement(controlLoadButton).click().perform();
        ControlLoadMenu.getInstance(browser).toogleDeletedRows();
        driverActions.moveToElement(controlLoadButton).click().perform();
        return this;
    }
}
