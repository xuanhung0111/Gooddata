package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DropDown;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.GroupConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel.DateGranularity;
import com.google.common.base.Predicate;

public class DashboardEditBar extends AbstractFragment {

    @FindBy(css = ".s-btn-save")
    private WebElement saveButton;

    @FindBy(css = ".s-btn-cancel:not(.disabled)")
    private WebElement cancelButton;

    @FindBy(css = ".s-btn-actions")
    private WebElement actionsMenu;

    @FindBy(css = ".s-delete")
    private WebElement deleteButton;

    @FindBy(css = ".s-settings___")
    private WebElement settingsButton;

    @FindBy(xpath = "//div[contains(@class,'dashboardSettingsDialogView')]")
    private DashboardSettingsDialog dashboardSettingsDialog;

    @FindBy(xpath = "//div[@class='savedFiltersTeaser']")
    private WebElement savedViewDisabledNotification;

    @FindBy(css = ".s-btn-report")
    private WebElement reportMenuButton;

    @FindBy(css = ".s-btn-widget")
    private WebElement widgetMenuButton;

    @FindBy(xpath = "//div[contains(@class,'gdc-overlay-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
    private DashboardAddWidgetPanel dashboardAddWidgetPanel;

    @FindBy(className = "s-btn-web_content")
    private WebElement webContentButton;

    @FindBy(xpath = "//button[contains(@class,'s-btn-text')]")
    private WebElement addText;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
    private DashboardTextObject dashboardTextObject;

    @FindBy(xpath = "//span[text()='Line']")
    private WebElement addLine;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
    private DashboardLineObject dashboardLineObject;

    @FindBy(xpath = "//div[contains(@class,'c-confirmDeleteDialog')]//button[contains(@class,'s-btn-delete')]")
    private WebElement deleteDashboardDialogButton;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-dashboardcollectionwidget-content')]/div[contains(@class,'yui3-c-dashboardwidget')]")
    private List<WebElement> listDashboardWidgets;

    @FindBy(className = "s-btn-filter")
    private WebElement addFilterMenu;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]//span[text()='Attribute']")
    private WebElement attributeFilter;

    @FindBy(xpath = "//div[contains(@class,'gdc-overlay-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
    private AddDashboardFilterPanel dashboardFilter;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]//span[text()='Date']")
    private WebElement dateFilter;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]//span[text()='Group']")
    private WebElement groupFilter;

    @FindBy(xpath = "//div[contains(@class,'s-active-tab')]")
    private DashboardEditFilter dashboardEditFilter;

    public DashboardEditFilter getDashboardEditFilter() {
        return dashboardEditFilter;
    }

    public DashboardEditBar addReportToDashboard(String reportName) {
        int widgetCountBefore = listDashboardWidgets.size();
        clickReportMenuButton();
        DropDown.getInstance(By.className("reportPicker"), browser).searchAndSelectItem(reportName);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");

        return this;
    }

    public void addWidgetToDashboard(WidgetTypes widgetType, String metricLabel) {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.addWidget(widgetType, metricLabel);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void verifyGeoLayersList(String metricLabel, List<String> layersList) {
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.verifyLayersList(metricLabel, layersList);
    }

    public void addGeoChart(String metricLabel, String attributeLayer) {
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.addGeoChart(metricLabel, attributeLayer);
    }

    public DashboardEditBar addWebContentToDashboard(String urlOrEmbedCode) {
        waitForElementVisible(webContentButton).click();
        waitForElementVisible(By.className("addUrl"), browser).sendKeys(urlOrEmbedCode);

        WebElement saveButton = waitForElementVisible(By.cssSelector(".c-addLinkDialog .s-btn-save"), browser);
        saveButton.click();
        waitForElementNotPresent(saveButton);

        return this;
    }

    public DashboardEditBar addAttributeFilterToDashboard(DashAttributeFilterTypes type, String name, String... label) {
        if (type == DashAttributeFilterTypes.PROMPT && label.length != 0)
            throw new UnsupportedOperationException("Label does not support for Prompt filter type");

        openFilterMenu().select("Attribute");
        waitForFragmentVisible(dashboardFilter).addAttributeFilter(type, name, label);
        return this;
    }

    public DashboardEditBar addTimeFilterToDashboard(String dateDimension, DateGranularity dateGranularity,
            String timeLine) {
        if (dateGranularity == DateGranularity.DAY) {
            throw new UnsupportedOperationException(
                    "Date granularity type: " + dateGranularity + " is not supported");
        }

        openFilterMenu().select("Date");
        waitForFragmentVisible(dashboardFilter).addTimeFilter(dateDimension, dateGranularity, timeLine);

        return this;
    }

    public DashboardEditBar addTimeFilterToDashboard(DateGranularity dateGranularity, String timeLine) {
        return addTimeFilterToDashboard(null, dateGranularity, timeLine);
    }

    public DashboardEditBar groupFiltersOnDashboard(String... filters) {
        openFilterMenu().select("Group");

        WidgetConfigPanel configPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        configPanel.getTab(WidgetConfigPanel.Tab.GROUP, GroupConfigPanel.class).selectFilters(filters);
        configPanel.saveConfiguration();

        return this;
    }

    public WidgetConfigPanel openGroupConfigPanel() {
        waitForElementVisible(addFilterMenu).click();
        waitForElementVisible(groupFilter).click();
        return Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
    }

    public DashboardEditBar addTextToDashboard(TextObject textObject, String text, String link) {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addText).click();
        waitForElementVisible(dashboardTextObject.getRoot());
        dashboardTextObject.addText(textObject, text, link);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
        return this;
    }

    public void addVariableStatusToDashboard(String variable) {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addText).click();
        waitForElementVisible(dashboardTextObject.getRoot());
        dashboardTextObject.addVariableStatus(variable);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public DashboardEditBar addLineToDashboard() {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addLine).click();
        waitForElementVisible(dashboardLineObject.getRoot());
        dashboardLineObject.addLineHorizonalToDashboard();
        waitForElementVisible(addLine).click();
        waitForElementVisible(dashboardLineObject.getRoot());
        dashboardLineObject.addLineVerticalToDashboard();
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 2,
                "Widget wasn't added");
        return this;
    }

    public void saveDashboard() {
        waitForElementVisible(saveButton);
        Graphene.guardAjax(saveButton).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void cancelDashboard() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void deleteDashboard() {
        tryToDeleteDashboard();
        waitForElementNotPresent(this.getRoot());
        sleepTightInSeconds(3); // take sometime for saving current dashboard into user profile settings
    }

    public void tryToDeleteDashboard() {
        waitForElementVisible(actionsMenu).click();
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(deleteDashboardDialogButton).click();
    }

    public DashboardEditBar clickReportMenuButton() {
        waitForElementVisible(reportMenuButton).click();
        return this;
    }

    /**
     * Use to turn on or off saved view mode
     * 
     * @param   on 
     */
    public void turnSavedViewOption(boolean on) {
        openDashboardSettingsDialog();
        dashboardSettingsDialog.turnSavedViewOption(on);
    }

    public void openDashboardSettingsDialog() {
        waitForElementVisible(actionsMenu).click();
        waitForElementVisible(settingsButton).click();
        waitForElementVisible(dashboardSettingsDialog.getRoot());
    }

    public DashboardSettingsDialog getDashboardSettingsDialog() {
        return dashboardSettingsDialog;
    }

    public WebElement getSavedViewDisabledNotification() {
        return savedViewDisabledNotification;
    }

    public DashboardEditBar setParentsFilter(String filter, String... parentFilterNames) {
        dashboardEditFilter.addParentFilters(filter, parentFilterNames);
        return this;
    }

    public void setParentsFilterUsingDataset(String filter, String linkedDataset, String... parentFilterNames) {
        dashboardEditFilter.addParentFiltersUsingDataset(filter, linkedDataset, parentFilterNames);
    }

    public int getDashboardWidgetsCount() {
        return listDashboardWidgets.size();
    }

    private SimpleMenu openFilterMenu() {
        Predicate<WebDriver> menuOpened = browser -> {
            waitForElementVisible(addFilterMenu).click();
            return isElementVisible(SimpleMenu.LOCATOR, browser);
        };

        Graphene.waitGui().until(menuOpened);
        return SimpleMenu.getInstance(browser);
    }
}
