package com.gooddata.qa.graphene.fragments.greypages.md.etl.pull;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

public class PullFragment extends AbstractGreyPagesFragment {

    @FindBy(name = "pull")
    private WebElement inputPull;

    @FindBy
    private WebElement submit;

    public boolean invokePull(String container, int checkIterations) throws JSONException {
        waitForElementVisible(inputPull).sendKeys(container);
        Graphene.guardHttp(submit).click();
        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        return waitForPollState("OK", checkIterations);
    }

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getString("taskStatus");
    }
}
