package com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

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
        Graphene.waitGui().until(browser -> !loadJSON().getJSONObject("wTaskStatus").getString("status").isEmpty());
        return State.valueOf(loadJSON().getJSONObject("wTaskStatus").getString("status"));
    }
}
