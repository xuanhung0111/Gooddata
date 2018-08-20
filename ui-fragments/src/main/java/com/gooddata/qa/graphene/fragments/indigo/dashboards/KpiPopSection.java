package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class KpiPopSection extends AbstractFragment {

    @FindBy(css = ".kpi-pop-change dd")
    protected WebElement changeTitle;

    @FindBy(css = ".kpi-pop-period dd")
    protected WebElement periodTitle;

    @FindBy(css = ".kpi-pop-change dt")
    private WebElement changeValue;

    public String getChangeTitle() {
        return waitForElementVisible(changeTitle).getText();
    }

    public String getPeriodTitle() {
        return waitForElementVisible(periodTitle).getText();
    }

    public void clickPeriodTilte() {
        waitForElementVisible(periodTitle).click();
    }

    public String getChangeValue() {
        return waitForElementVisible(changeValue).getText();
    }
}
