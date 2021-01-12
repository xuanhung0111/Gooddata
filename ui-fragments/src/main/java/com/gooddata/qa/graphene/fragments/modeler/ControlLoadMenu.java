package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.List;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class ControlLoadMenu extends AbstractFragment {

    private static final String CONTROL_LOAD_MENU = "ldm-control-load-menu";

    @FindBy(className = "input-checkbox-toggle")
    private List<WebElement> checkboxToogles;

    public static ControlLoadMenu getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                ControlLoadMenu.class, waitForElementVisible(className(CONTROL_LOAD_MENU), searchContext));
    }

    public void toogleLoad(String typeLoading) {
        Actions driverActions = getActions();
        WebElement option = checkboxToogles.stream().filter(el -> el.findElement(By.className("input-label-text")).getText()
                .contains(typeLoading)).findFirst().get();
        log.info("---Option---" + option.getText());
        driverActions.moveToElement(option.findElement(By.tagName("input"))).click().perform();
    }

    public void toogleIncrementalLoad() {
        toogleLoad("Incremental Load");
    }

    public void toogleDeletedRows() {
        toogleLoad("Deleted rows");
    }
}
