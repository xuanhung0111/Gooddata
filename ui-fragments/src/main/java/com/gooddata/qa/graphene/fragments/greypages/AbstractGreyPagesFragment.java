package com.gooddata.qa.graphene.fragments.greypages;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;

import static org.testng.Assert.*;

public abstract class AbstractGreyPagesFragment extends AbstractFragment {

    protected static final By BY_GP_FORM = By.tagName("form");
    protected static final By BY_GP_FORM_SECOND = By.xpath("//div[@class='form'][2]/form");
    protected static final By BY_GP_PRE_JSON = By.tagName("pre");
    protected static final By BY_GP_LINK = By.tagName("a");
    protected static final By BY_GP_BUTTON_SUBMIT = By.xpath("//div[@class='submit']/input");

    protected JSONObject loadJSON() throws JSONException {
        return new JSONObject(waitForElementPresent(BY_GP_PRE_JSON).getText());
    }

    protected boolean waitForPollState(String expectedValidState, int maxIterations) throws JSONException, InterruptedException {
        int i = 0;
        String state = "";
        while (!expectedValidState.equals(state = getPollState())) {
            System.out.println("Current polling state is: " + state);
            assertTrue(i < maxIterations, "Maximum attempts to get " + expectedValidState + " status reached. Exiting.");
            assertNotEquals(state, "ERROR", "Error state appeared");
            assertNotEquals(state, "DELETED", "Deleted status appeared");
            assertNotEquals(state, "CANCELED", "Canceled status appeared");
            Thread.sleep(5000);
            browser.navigate().refresh();
            i++;
        }
        assertEquals(state, expectedValidState, "Have expected state");
        System.out.println("Succeed at +- " + (i * 5) + "seconds");
        return true;
    }

    protected String getPollState() throws JSONException {
        throw new JSONException("Override this to set proper JSON path");
    }
}
