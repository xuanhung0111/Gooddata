package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertTrue;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

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
        assertTrue(waitForPollState("OK", checkIterations));
        return exportToken;
    }

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getJSONObject("wTaskStatus").getString("status");
    }

    protected String getExportToken() throws JSONException {
        return loadJSON().getJSONObject("partialMDArtifact").getString("token");
    }
}
