package com.gooddata.qa.graphene.fragments.greypages;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

public abstract class AbstractGreyPagesFragment extends AbstractFragment {

    protected static final By BY_GP_FORM = By.tagName("form");
    protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
    protected static final By BY_GP_PRE_JSON = By.tagName("pre");
    protected static final By BY_GP_LINK = By.tagName("a");
    protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");

    protected JSONObject loadJSON() throws JSONException {
        WebElement content = waitForElementPresent(BY_GP_PRE_JSON, browser);
        Graphene.waitGui().until(browser -> content.getText().startsWith("{"));
        return new JSONObject(content.getText());
    }

    protected void waitForPollState(State expectedValidState, int maxIterations) throws JSONException {
        int i = 0;
        State state = getPollState();
        while ((state == State.RUNNING || state == State.PREPARING) &&
                !expectedValidState.equals(state = getPollState())) {
            System.out.println("Current polling state is: " + state);
            if (i >= maxIterations) {
                throw new TimeoutException("Maximum attempts to get " + expectedValidState + " status reached. Exiting.");
            }
            sleepTightInSeconds(5);
            browser.navigate().refresh();
            i++;
        }
        System.out.println("Succeed at +- " + (i * 5) + "seconds");
    }

    protected State getPollState() throws JSONException {
        throw new JSONException("Override this to set proper JSON path");
    }

    public enum State {
        OK, ENABLED, ERROR, DELETED, CANCELED, RUNNING, PREPARING
    }
}
