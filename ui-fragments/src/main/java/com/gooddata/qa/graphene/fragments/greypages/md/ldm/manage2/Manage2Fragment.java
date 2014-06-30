package com.gooddata.qa.graphene.fragments.greypages.md.ldm.manage2;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class Manage2Fragment extends AbstractGreyPagesFragment {

    @FindBy(name = "maql")
    private WebElement maql;

    @FindBy
    private WebElement submit;

    public boolean postMAQL(String maql, int checkIterations) throws JSONException, InterruptedException {
        waitForElementVisible(this.maql).sendKeys(maql);
        Graphene.guardHttp(submit).click();
        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        return waitForPollState("OK", checkIterations);
    }

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getJSONObject("wTaskStatus").getString("status");
    }
}
