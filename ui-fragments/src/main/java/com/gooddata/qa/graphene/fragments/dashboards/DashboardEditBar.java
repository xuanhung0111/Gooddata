package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.common.Sleeper;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.DropDown;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;

public class DashboardEditBar extends AbstractFragment {

    @FindBy(css = ".s-btn-save")
    private WebElement saveButton;

    @FindBy(css = ".s-btn-cancel")
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

    @FindBy(xpath = "//iframe[contains(@src,'iaa/scatter')]")
    private DashboardScatterExplorer dashboardScatterExplorer; 

    @FindBy(xpath = "//span[text()='Web Content']")
    private WebElement addwebContent;

    @FindBy(xpath = "//div[contains(@class,'yui3-d-modaldialog')]")
    private DashboardWebContent dashboardWebContent;

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

    @FindBy(xpath = "//span[text()='Filter']")
    private WebElement addFilterMenu;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]//span[text()='Attribute']")
    private WebElement attributeFilter;

    @FindBy(xpath = "//div[contains(@class,'gdc-overlay-simple') and not(contains(@class,'yui3-overlay-hidden'))]")
    private DashboardFilter dashboardFilter;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]//span[text()='Date']")
    private WebElement dateFilter;

    @FindBy(xpath = "//div[contains(@class,'gdc-menu-simple')]//span[text()='Group']")
    private WebElement groupFilter;

    @FindBy(xpath = "//div[contains(@class,'s-active-tab')]")
    private DashboardEditFilter dashboardEditFilter;

    @FindBy(xpath = "//div[contains(@class,'reportPicker')]")
    private DropDown reportPicker;

    public DashboardEditFilter getDashboardEditFilter() {
        return dashboardEditFilter;
    }

    public void addReportToDashboard(String reportName) {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(reportMenuButton).click();
        waitForElementVisible(reportPicker.getRoot());
        reportPicker.searchAndSelectItem(reportName);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void addWidgetToDashboard(WidgetTypes widgetType, String metricLabel)
            throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.addWidget(widgetType, metricLabel);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void addScatterWidgetToDashboard(Map<String, String> data, boolean invalidConfiguration)
            throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.initWidget(WidgetTypes.SCATTER_EXPLORER);
        waitForElementVisible(DashboardScatterExplorer.BY_IFRAME_SCATTER,browser);
        dashboardScatterExplorer.addScatterWidget(data, invalidConfiguration);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1, "Widget wasn't added");
    }

    public void addScatterWidgetToDashboard(Map<String, String> data)
            throws InterruptedException {
        addScatterWidgetToDashboard(data, false);
    }

    public void addColorToScatterWidget(Map<String, String> data)
            throws InterruptedException {
        waitForElementVisible(DashboardScatterExplorer.BY_IFRAME_SCATTER,browser);
        dashboardScatterExplorer.addColorToScatterWidget(data);
    }

    public void disableColorInScatterWidget()
            throws InterruptedException {
        waitForElementVisible(DashboardScatterExplorer.BY_IFRAME_SCATTER,browser);
        dashboardScatterExplorer.disableColorToScatterWidget();
    }

    public void addColumnsToScatterWidget(Map<String, ArrayList<HashMap <String,String>>> data)
            throws InterruptedException {
        waitForElementVisible(DashboardScatterExplorer.BY_IFRAME_SCATTER,browser);
        dashboardScatterExplorer.addColumnsToScatterWidget(data);
    }

    public void removeColumnsFromScatterWidget()
            throws InterruptedException {
        waitForElementVisible(DashboardScatterExplorer.BY_IFRAME_SCATTER,browser);
        dashboardScatterExplorer.removeColumnsFromScatterWidget();
    }

    public void verifyGeoLayersList(String metricLabel, List<String> layersList) throws InterruptedException{
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.verifyLayersList(metricLabel, layersList);
    }

    public void addGeoChart(String metricLabel, String attributeLayer) throws InterruptedException{
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.addGeoChart(metricLabel, attributeLayer);
    }

    public void addWebContentToDashboard() throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addwebContent).click();
        waitForElementVisible(dashboardWebContent.getRoot());
        dashboardWebContent.addWebContent();
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void addListFilterToDashboard(DashFilterTypes type, String name)
            throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addFilterMenu).click();
        waitForElementVisible(attributeFilter).click();
        waitForElementVisible(dashboardFilter.getRoot());
        dashboardFilter.addListFilter(type, name);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void addTimeFilterToDashboard(int dateDimensionIndex, String dateRange)
            throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addFilterMenu).click();
        waitForElementVisible(dateFilter).click();
        Thread.sleep(2000);
        waitForElementVisible(dashboardFilter.getRoot());
        dashboardFilter.addTimeFilter(dateDimensionIndex, dateRange);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public WidgetConfigPanel openGroupConfigPanel() {
        waitForElementVisible(addFilterMenu).click();
        waitForElementVisible(groupFilter).click();
        return Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
    }

    public void addTextToDashboard(TextObject textObject, String text,
                                   String link) throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addText).click();
        waitForElementVisible(dashboardTextObject.getRoot());
        dashboardTextObject.addText(textObject, text, link);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void addLineToDashboard() throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(addLine).click();
        waitForElementVisible(dashboardLineObject.getRoot());
        dashboardLineObject.addLineHorizonalToDashboard();
        waitForElementVisible(addLine).click();
        waitForElementVisible(dashboardLineObject.getRoot());
        dashboardLineObject.addLineVerticalToDashboard();
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 2,
                "Widget wasn't added");
    }

    public void saveDashboard() {
        waitForElementVisible(saveButton);
        Graphene.guardAjax(saveButton).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void cancelDashboard() {
        waitForElementVisible(cancelButton);
        Graphene.guardAjax(cancelButton).click();
        waitForElementNotVisible(this.getRoot());
    }

    public void deleteDashboard() {
        waitForElementVisible(actionsMenu).click();
        waitForElementVisible(deleteButton).click();
        waitForElementVisible(deleteDashboardDialogButton).click();
        waitForElementNotPresent(this.getRoot());
        Sleeper.sleepTightInSeconds(3); // take sometime for saving current dashboard into user profile settings
    }

    public void moveWidget(WebElement widget, int x, int y) {
        waitForElementVisible(widget);
        Actions builder = new Actions(browser);
        builder.dragAndDropBy(widget, x, y).build().perform();
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

    public void initInteractiveReportWidget() throws InterruptedException {
        int widgetCountBefore = listDashboardWidgets.size();
        waitForElementVisible(widgetMenuButton).click();
        waitForElementVisible(dashboardAddWidgetPanel.getRoot());
        dashboardAddWidgetPanel.initWidget(WidgetTypes.INTERACTIVE_REPORT);
        Assert.assertEquals(listDashboardWidgets.size(), widgetCountBefore + 1,
                "Widget wasn't added");
    }

    public void setParentsFilter(String filter, String... parentFilterNames) {
        dashboardEditFilter.addParentFilters(filter, parentFilterNames);
    }

    public void setParentsFilterUsingDataset(String filter, String linkedDataset, String... parentFilterNames) {
        dashboardEditFilter.addParentFiltersUsingDataset(filter, linkedDataset, parentFilterNames);
    }
}
