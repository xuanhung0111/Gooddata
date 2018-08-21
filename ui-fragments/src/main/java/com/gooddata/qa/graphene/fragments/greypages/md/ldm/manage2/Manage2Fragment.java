package com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import java.util.stream.Stream;

public class Manage2Fragment extends AbstractGreyPagesFragment {

    @FindBy(name = "maql")
    private WebElement maql;

    @FindBy
    private WebElement submit;

    public boolean postMAQL(String maql, int checkIterations) throws JSONException {
        waitForElementVisible(this.maql).sendKeys(maql);
        Graphene.guardHttp(submit).click();
        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        waitForPollState(State.OK, checkIterations);
        return getPollState() == State.OK;
    }

    @Override
    protected State getPollState() throws JSONException {
        return Stream.of(State.values())
                .filter(state -> state.toString().equals(loadJSON().getJSONObject("wTaskStatus").getString("status")))
                .findFirst()
                .get();
    }
}
