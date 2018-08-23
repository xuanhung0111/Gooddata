package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import java.util.stream.Stream;

public class PartialExportFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement uris;

    @FindBy
    private WebElement submit;

    public String invokeExport(String objectUri, int checkIterations)
            throws JSONException {
        waitForElementVisible(uris).sendKeys(objectUri);
        Graphene.guardHttp(submit).click();
        String exportToken = getExportToken();

        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        waitForPollState(State.OK, checkIterations);
        return exportToken;
    }

    @Override
    protected State getPollState() throws JSONException {
        Graphene.waitGui().until(browser -> !loadJSON().getJSONObject("wTaskStatus").getString("status").isEmpty());
        return Stream.of(State.values())
                .filter(state -> state.toString().equals(loadJSON().getJSONObject("wTaskStatus").getString("status")))
                .findFirst()
                .get();
    }

    protected String getExportToken() throws JSONException {
        return loadJSON().getJSONObject("partialMDArtifact").getString("token");
    }
}
