package com.gooddata.qa.graphene.fragments.greypages.md.maintenance.imp;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.*;

public class ImportFragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement token;

    @FindBy
    private WebElement submit;

    public boolean invokeImport(String exportToken, int checkIterations) throws JSONException, InterruptedException {
        System.out.println("Using exportToken \""+exportToken+"\" to import project");
        waitForElementVisible(token).sendKeys(exportToken);
        Graphene.guardHttp(submit).click();
        waitForElementVisible(BY_GP_LINK, browser);
        Graphene.guardHttp(browser.findElement(BY_GP_LINK)).click();
        return waitForPollState("OK", checkIterations);
    }

    @Override
    protected String getPollState() throws JSONException {
        return loadJSON().getJSONObject("taskState").getString("status");
    }
}
