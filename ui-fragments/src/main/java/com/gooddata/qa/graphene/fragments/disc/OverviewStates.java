package com.gooddata.qa.graphene.fragments.disc;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import static org.testng.Assert.*;

public class OverviewStates extends AbstractFragment {

    private static final By BY_OVERVIEW_STATE = By.cssSelector(".ait-overview-state");
    private static final By BY_OVERVIEW_STATE_NUMBER = By.cssSelector(".ait-overview-state-count");

    public void selectOverviewState(OverviewProjectStates state) {
        WebElement stateField = waitForElementVisible(state.getOverviewFieldBy(), browser);
        stateField.findElement(BY_OVERVIEW_STATE).click();
        waitForStateNumber(stateField.findElement(BY_OVERVIEW_STATE_NUMBER));
    }

    public void assertOverviewStateNumber(OverviewProjectStates state, int number) {
        assertTrue(state.getOption().equalsIgnoreCase(getState(state)));
        assertEquals(getStateNumber(state), String.valueOf(number));
    }

    public String getStateNumber(OverviewProjectStates state) {
        WebElement stateNumber =
                waitForElementVisible(state.getOverviewFieldBy(), browser).findElement(
                        BY_OVERVIEW_STATE_NUMBER);
        waitForStateNumber(stateNumber);
        return stateNumber.getText();
    }

    public boolean isActive(OverviewProjectStates state) {
        return waitForElementPresent(state.getOverviewFieldBy(), browser).getAttribute("class")
                .contains("active");
    }

    private void waitForStateNumber(final WebElement stateNumber) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return !stateNumber.getText().isEmpty();
            }
        });
    }

    private String getState(OverviewProjectStates state) {
        return waitForElementVisible(state.getOverviewFieldBy(), browser).findElement(
                BY_OVERVIEW_STATE).getText();
    }
}
