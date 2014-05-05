package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.exp;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.testng.Assert.assertTrue;

public class ExportFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement exportData;

    @FindBy
    private WebElement exportUsers;

    @FindBy
    private WebElement submit;

    public String invokeExport(boolean expUsers, boolean expData, int checkIterations) throws JSONException, InterruptedException {
        if (expData) waitForElementVisible(exportData).click();
        if (expUsers) waitForElementVisible(exportUsers).click();
        Graphene.guardHttp(submit).click();
        String exportToken = getExportToken();

        waitForElementVisible(BY_GP_LINK);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        assertTrue(waitForPollState("OK", checkIterations));
        return exportToken;
    }

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getJSONObject("taskState").getString("status");
    }

    protected String getExportToken() throws JSONException {
        return loadJSON().getJSONObject("exportArtifact").getString("token");
    }
}