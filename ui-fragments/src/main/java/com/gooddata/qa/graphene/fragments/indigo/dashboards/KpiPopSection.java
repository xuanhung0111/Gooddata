package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class KpiPopSection extends AbstractFragment {

    @FindBy(css = ".kpi-pop-change dd")
    protected WebElement changeTitle;

    @FindBy(css = ".kpi-pop-period dd")
    protected WebElement periodTitle;

    public String getChangeTitle() {
        return waitForElementVisible(changeTitle).getText();
    }

    public String getPeriodTitle() {
        return waitForElementVisible(periodTitle).getText();
    }

}
