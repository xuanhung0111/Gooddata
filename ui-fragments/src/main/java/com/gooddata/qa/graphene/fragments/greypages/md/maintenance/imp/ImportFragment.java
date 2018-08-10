package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import java.util.stream.Stream;

public class ImportFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement token;

    @FindBy
    private WebElement submit;

    public boolean invokeImport(String exportToken, int checkIterations) throws JSONException {
        System.out.println("Using exportToken \""+exportToken+"\" to import project");
        waitForElementVisible(token).sendKeys(exportToken);
        Graphene.guardHttp(submit).click();
        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        waitForPollState(State.OK, checkIterations);
        return getPollState() == State.OK;
    }

    @Override
    protected State getPollState() throws JSONException {
        return Stream.of(State.values())
                .filter(state -> state.toString().equals(loadJSON().getJSONObject("taskState").getString("status")))
                .findFirst()
                .get();
    }
}
