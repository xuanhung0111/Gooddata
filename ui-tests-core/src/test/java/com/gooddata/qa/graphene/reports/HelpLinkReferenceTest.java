package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.enums.dashboard.WidgetTypes.GEO_CHART;
import static com.gooddata.qa.graphene.enums.dashboard.WidgetTypes.KEY_METRIC;
import static com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab.DATA;
import static com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab.METRIC_STYLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DIMENSION_CLOSED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_AMOUNT_BY_PRODUCT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardAddWidgetPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.DataConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.StyleConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.fragments.reports.filter.SubFilterContainer;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import java.util.function.Function;

public class HelpLinkReferenceTest extends GoodSalesAbstractTest {

    @Override
    protected void customizeProject() throws Throwable {
        getReportCreator().createAmountByProductReport();
    }

    @Test(dependsOnGroups = "createProject")
    public void checkGDCLabelingOnSaveViewPopup() throws JSONException {
        initDashboardsPage().addNewDashboard(generateHashString()).editDashboard().turnSavedViewOption(true);
        boolean isLearnMorePresent = dashboardsPage.saveDashboard().getSavedViewWidget()
                .openSavedViewMenu().getSavedViewPopupMenu().isLearnMorePresent();
        assertTrue(testParams.isWhitelabeledEnvironment() != isLearnMorePresent,
                "GDC Labeling should be not mentioned in whitelabel sever");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkGDCLabelingOnTimeFilterPanel() throws JSONException {
        initDashboardsPage().addNewDashboard(generateHashString()).editDashboard()
                .addTimeFilterToDashboard(DATE_DIMENSION_CLOSED, TimeFilterPanel.DateGranularity.QUARTER, "this");
        boolean isMoreInfoPresent = dashboardsPage.getFirstFilter().openEditPanel().getTimeFilterPanel().isMoreInfoPresent();
        assertTrue(testParams.isWhitelabeledEnvironment() != isMoreInfoPresent,
                "GDC Labeling should be not mentioned in whitelabel sever");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkGDCLabelingOnStylePanel() throws JSONException {
        initDashboardsPage().addNewDashboard(generateHashString()).editDashboard();
        StyleConfigPanel styleConfigPanel = initWidgetConfigPanel(KEY_METRIC).getTab(METRIC_STYLE, StyleConfigPanel.class);
        int windowHandles = browser.getWindowHandles().size();
        styleConfigPanel.clickMoreInfo();
        Function<WebDriver, Boolean> newTabOpened = browser -> browser.getWindowHandles().size() > windowHandles;
        Graphene.waitGui().until(newTabOpened);
        try {
            BrowserUtils.switchToLastTab(browser);
            boolean isDirectToGoodData = browser.getCurrentUrl().contains("gooddata.com");
            assertTrue(testParams.isWhitelabeledEnvironment() != isDirectToGoodData,
                    "GDC Labeling should be not mentioned in whitelabel sever");
        } finally {
            BrowserUtils.closeCurrentTab(browser);
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = "createProject")
    public void checkGDCLabelingOnDataPanel() throws JSONException {
        initDashboardsPage().addNewDashboard(generateHashString()).editDashboard();
        boolean isMoreInfoPresent = initWidgetConfigPanel(GEO_CHART)
                .getTab(DATA, DataConfigPanel.class).isMoreInfoPresent();
        assertTrue(testParams.isWhitelabeledEnvironment() != isMoreInfoPresent,
                "GDC Labeling should be not mentioned in whitelabel sever");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkGDCLabelingOnRankingFilter() throws JSONException {
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT).openFilterPanel().openRankingFilterFragment();
        boolean isMoreInfoPresent = Graphene.createPageFragment(SubFilterContainer.class,
                waitForElementVisible(SubFilterContainer.LOCATOR, browser))
                .openFloatingRangePanel(DATE_DIMENSION_CLOSED).isMoreInfoPresent();
        assertTrue(testParams.isWhitelabeledEnvironment() != isMoreInfoPresent,
                "GDC Labeling should be not mentioned in whitelabel sever");
    }

    @Test(dependsOnGroups = "createProject")
    public void checkGDCLabelingOnRangeFilter() throws JSONException {
        initReportsPage().openReport(REPORT_AMOUNT_BY_PRODUCT).openFilterPanel().openRangeFilterFragment();
        boolean isMoreInfoPresent = Graphene.createPageFragment(SubFilterContainer.class,
                waitForElementVisible(SubFilterContainer.LOCATOR, browser))
                .openFloatingRangePanel(DATE_DIMENSION_CLOSED).isMoreInfoPresent();
        assertTrue(testParams.isWhitelabeledEnvironment() != isMoreInfoPresent,
                "GDC Labeling should be not mentioned in whitelabel sever");
    }

    private WidgetConfigPanel initWidgetConfigPanel(WidgetTypes type) {
        waitForElementVisible(className("s-btn-widget"), browser).click();
        Graphene.createPageFragment(DashboardAddWidgetPanel.class,
                waitForElementVisible(DashboardAddWidgetPanel.LOCATOR, browser)).initWidget(type);
        return  Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
    }
}
