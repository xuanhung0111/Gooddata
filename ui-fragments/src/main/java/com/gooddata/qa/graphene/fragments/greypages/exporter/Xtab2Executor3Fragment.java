package com.gooddata.qa.graphene.fragments.greypages.exporter;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

public class Xtab2Executor3Fragment extends AbstractGreyPagesFragment {

    @FindBy
    private WebElement reportUri;

    @FindBy
    private WebElement submit;

    public Xtab2Executor3Fragment fillReportUri(String uri) {
        waitForElementVisible(reportUri).sendKeys(uri);
        return this;
    }

    public Xtab2Executor3Fragment submit() {
        waitForElementVisible(submit).click();
        return this;
    }

    public String getResult() {
        return waitForElementVisible(By.tagName("pre"), browser).getText();
    }
}
