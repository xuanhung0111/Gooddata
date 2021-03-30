package com.gooddata.qa.graphene.fragments.indigo.analyze.pickerDropDown;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.DrillModalDialog;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;

import java.util.List;

public class Drilling extends AbstractFragment {

    @FindBy(className = "s-drill-down")
    private List<WebElement> drillDownItem;

    @FindBy(className = "s-drill-to-dashboard")
    private WebElement drillToDashboard;

    @FindBy(className = "s-drill-to-insight")
    private WebElement drillToInsight;

    @FindBy(className = "s-drill-to-url")
    private WebElement drillToURL;

    public static final String ROOT_CLASS = "gd-drill-modal-picker-dropdown";

    public static Drilling getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(Drilling.class,
                waitForElementVisible(className(ROOT_CLASS), searchContext));
    }

    public IndigoDashboardsPage drillToDashboard() {
        waitForElementVisible(drillToDashboard).click();
        waitForFragmentNotVisible(this);
        return IndigoDashboardsPage.getInstance(browser);
    }

    public DrillModalDialog drillToInsight() {
        waitForElementVisible(drillToInsight).click();
        waitForFragmentNotVisible(this);
        return DrillModalDialog.getInstance(browser);
    }

    public DrillModalDialog drillDown(String drillAttribute) {
        drillDownItem.stream().filter(element -> waitForElementVisible(element).getAttribute("title").equals(drillAttribute)).findFirst().get().click();
        waitForFragmentNotVisible(this);
        return DrillModalDialog.getInstance(browser);
    }

    public void drillToUrl() {
        waitForElementVisible(drillToURL).click();
        waitForFragmentNotVisible(this);
    }
}
