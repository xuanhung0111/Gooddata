package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.browser.DragAndDropUtils.dragAndDropWithCustomBackend;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.convertCSSClassTojQuerySelector;
import static org.openqa.selenium.By.id;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.google.common.collect.Iterables;

public class IndigoDashboardsPage extends AbstractFragment {

    @FindBy(css = Kpi.KPI_CSS_SELECTOR)
    private List<Kpi> kpis;

    @FindBy(css = Visualization.MAIN_SELECTOR)
    private List<Visualization> visualizations;

    @FindBy(className = "splashscreen")
    private SplashScreen splashScreen;

    @FindBy(className = "gd-header")
    private Header header;

    @FindBy(className = EDIT_BUTTON_CLASS_NAME)
    private WebElement editButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = SAVE_BUTTON_CLASS_NAME)
    private WebElement saveButton;

    @FindBy(css = "." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)")
    private WebElement enabledSaveButton;

    @FindBy(css = ".dash-nav-right .configuration-panel")
    private ConfigurationPanel configurationPanel;

    @FindBy(className = ADD_KPI_PLACEHOLDER_CLASS_NAME)
    private WebElement addKpiPlaceholder;

    @FindBy(xpath = "//*[contains(concat(' ', normalize-space(@class), ' '), ' s-dialog ')]")
    private ConfirmDialog dialog;

    @FindBy(className = "dash-filters-date")
    private DateFilter dateFilter;

    @FindBy(className = DELETE_BUTTON_CLASS_NAME)
    private WebElement deleteButton;

    @FindBy(css = ".dash-filters-attribute.are-loaded")
    private AttributeFiltersPanel attributeFiltersPanel;

    @FindBy(className = VISUALIZATIONS_LIST_CLASS_NAME)
    private VisualizationsList visualizationsList;

    private static final String DASHBOARD_BODY = "dash-section-kpis";
    private static final String EDIT_BUTTON_CLASS_NAME = "s-edit_button";
    private static final String SAVE_BUTTON_CLASS_NAME = "s-save_button";
    private static final String DELETE_BUTTON_CLASS_NAME = "s-delete_dashboard";
    private static final String ALERTS_LOADED_CLASS_NAME = "alerts-loaded";
    private static final String VISUALIZATIONS_LIST_CLASS_NAME = "gd-visualizations-list";
    private static final String ADD_KPI_PLACEHOLDER_CLASS_NAME = "add-kpi-placeholder";
    private static final String LAST_DROPZONE_CLASS_NAME = "s-last-drop-position";

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector("." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)");

    public static final String MAIN_ID = "app-dashboards";

    public static final IndigoDashboardsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(IndigoDashboardsPage.class, waitForElementVisible(id(MAIN_ID), context));
    }

    public SplashScreen getSplashScreen() {
        return waitForFragmentVisible(splashScreen);
    }

    public ConfigurationPanel getConfigurationPanel() {
        return configurationPanel;
    }

    public IndigoDashboardsPage switchToEditMode() {
        waitForElementVisible(editButton).click();
        waitForElementVisible(cancelButton);

        // There's an animation switching to edit mode,
        // so wait until the css transition is finished
        Sleeper.sleepTight(500);

        // wait until editing is allowed
        return waitForKpiEditable();
    }

    public IndigoDashboardsPage cancelEditMode() {
        waitForElementVisible(cancelButton).click();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithWidgets() {
        waitForElementVisible(enabledSaveButton).click();
        waitForElementVisible(editButton);
        waitForAllKpiWidgetContentLoaded();
        waitForAllInsightWidgetContentLoaded();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithoutKpis() {
        waitForElementVisible(enabledSaveButton).click();
        this.waitForDialog().submitClick();
        waitForFragmentVisible(splashScreen);

        return this;
    }

    public boolean isSaveEnabled() {
        return isElementPresent(SAVE_BUTTON_ENABLED, browser);
    }

    // if save is disabled, use cancel. But leave edit mode in any case
    public IndigoDashboardsPage leaveEditMode() {
        if (isSaveEnabled()) {
            return saveEditModeWithWidgets();
        }

        return cancelEditMode();
    }

    public ConfirmDialog waitForDialog() {
        waitForElementVisible(dialog.getRoot());
        return dialog;
    }

    public int getKpisCount() {
        return kpis.size();
    }

    public int getVisualizationsCount() {
        return visualizations.size();
    }

    public Kpi selectKpi(int index) {
        Kpi tempKpi = getKpiByIndex(index);
        waitForElementPresent(tempKpi.getRoot());
        tempKpi.clickKpiContent();

        waitForFragmentVisible(configurationPanel).waitForButtonsLoaded();
        return tempKpi;
    }

    public Kpi selectLastKpi() {
        return selectKpi(kpis.size() - 1);
    }

    public Kpi getFirstKpi() {
        return getKpiByIndex(0);
    }

    public Kpi getLastKpi() {
        return getKpiByIndex(kpis.size() - 1);
    }

    public Visualization getLastVisualization() {
        return getVisualizationByIndex(visualizations.size() - 1);
    }

    public Visualization selectVisualization(int index) {
        Visualization visualization = getVisualizationByIndex(index);
        waitForElementPresent(visualization.getRoot()).click();
        return visualization;
    }

    public Visualization selectLastVisualization() {
        return selectVisualization(visualizations.size() - 1);
    }

    public IndigoDashboardsPage clickLastKpiDeleteButton() {
        selectLastKpi().clickDeleteButton();

        return this;
    }

    public IndigoDashboardsPage deleteKpi(int index) {
        selectKpi(index).clickDeleteButton();

        return this;
    }

    public IndigoDashboardsPage deleteKpi(Kpi kpi) {
        kpi.clickDeleteButton();

        return this;
    }

    public String getValueFromKpi(final String name) {
        return Iterables.find(kpis, input -> name.equals(input.getHeadline())).getValue();
    }

    public Kpi getKpiByIndex(int index) {
        final Kpi kpi = waitForCollectionIsNotEmpty(kpis).get(index);

        // Indigo DashBoard can load only one Kpi on mobile screen, and the others will present in DOM
        // and just visible when scrolling down, so the Kpi we need to work with may not be visible and interact.
        // And selenium script cannot do anything when an element is not visible.
        // In this stage, java script will be good solution when it can scroll to an element
        // although the element just present and not visible.

        // And the way java script makes element visible is not like when doing manual,
        // it just scroll and calculate until it think the element visible.
        // So in reality view, the element may show full or just a small part of it.
        if (!isElementVisible(kpi.getRoot())) {
            scrollElementIntoView(kpi.getRoot(), browser);
        }

        return waitForFragmentVisible(kpi);
    }

    public String getKpiTitle(int index) {
        return getKpiByIndex(index).getHeadline();
    }

    public Visualization getVisualizationByIndex(int index) {
        return visualizations.get(index);
    }

    public Kpi getKpiByHeadline(final String headline) {
        return kpis.stream()
            .filter(kpi -> headline.equals(kpi.getHeadline()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find Kpi with headline: " + headline));
    }

    public IndigoDashboardsPage waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetsLoaded() {
        waitForElementNotPresent(Kpi.IS_WIDGET_LOADING);

        return this;
    }

    public IndigoDashboardsPage waitForAllInsightWidgetsLoaded() {
        waitForElementNotPresent(Visualization.IS_WIDGET_LOADING);
        return this;
    }

    public IndigoDashboardsPage waitForAnyKpiWidgetContentLoading() {
        int KPI_INITIATE_LOAD_TIMEOUT_SECONDS = 2;
        // wait until the loading is initiated. If not, assume kpi widget
        // was already reloaded, even before we started waiting
        try {
            waitForElementVisible(Kpi.IS_CONTENT_LOADING, browser, KPI_INITIATE_LOAD_TIMEOUT_SECONDS);
        } catch (TimeoutException ex) { }

        return this;
    }

    public IndigoDashboardsPage waitForAllKpiWidgetContentLoaded() {
        waitForAllKpiWidgetsLoaded();
        waitForElementNotPresent(Kpi.IS_CONTENT_LOADING);

        return this;
    }

    public IndigoDashboardsPage waitForAllInsightWidgetContentLoaded() {
        waitForAllInsightWidgetsLoaded();
        waitForElementNotPresent(Visualization.IS_CONTENT_LOADING);
        return this;
    }

    public IndigoDashboardsPage waitForKpiEditable() {
        waitForElementNotPresent(Kpi.IS_NOT_EDITABLE);
        return this;
    }

    public IndigoDashboardsPage dragAddKpiPlaceholder() {
        waitForElementPresent(addKpiPlaceholder);
        addKpiWidget();
        return this;
    }

    public IndigoDashboardsPage addWidget(KpiConfiguration config) {
        dragAddKpiPlaceholder();
        configurationPanel
            .selectMetricByName(config.getMetric())
            .selectDataSetByName(config.getDataSet());

        if (config.hasComparison()) {
            configurationPanel.selectComparisonByName(config.getComparison());
        }

        if (config.hasDrillTo()) {
            configurationPanel.selectDrillToByName(config.getDrillTo());
        }

        return waitForAllKpiWidgetContentLoaded();
    }

    public boolean isEditButtonVisible() {
        By buttonVisible = By.className(EDIT_BUTTON_CLASS_NAME);
        return isElementPresent(buttonVisible, browser);
    }

    public DateFilter waitForDateFilter() {
        return waitForFragmentVisible(dateFilter);
    }

    public IndigoDashboardsPage selectDateFilterByName(String dateFilterName) {
        waitForAllKpiWidgetContentLoaded();

        waitForFragmentVisible(dateFilter)
            .selectByName(dateFilterName);

        return waitForAllKpiWidgetContentLoaded();
    }

    public String getDateFilterSelection() {
        return waitForFragmentVisible(dateFilter).getSelection();
    }

    public AttributeFiltersPanel waitForAttributeFilters() {
        return waitForFragmentVisible(attributeFiltersPanel);
    }

    public IndigoDashboardsPage waitForAlertsLoaded() {
        waitForElementPresent(By.className(ALERTS_LOADED_CLASS_NAME), browser);
        return this;
    }

    public IndigoDashboardsPage deleteDashboard(boolean confirm) {
        waitForElementVisible(deleteButton).click();
        ConfirmDialog deleteConfirmDialog = waitForDialog();

        if (confirm) {
            deleteConfirmDialog.submitClick();
            waitForElementNotPresent(deleteButton);
        } else {
            deleteConfirmDialog.cancelClick();
        }

        return this;
    }

    public boolean isDeleteButtonVisible() {
        return isElementPresent(By.className(DELETE_BUTTON_CLASS_NAME), this.getRoot());
    }

    public IndigoDashboardsPage waitForSplashscreenMissing() {
        waitForFragmentNotVisible(splashScreen);
        return this;
    }

    public IndigoDashboardsPage waitForEditingControls() {
        waitForElementVisible(saveButton);
        waitForElementVisible(cancelButton);
        return this;
    }

    public void logout() {
        waitForFragmentVisible(header).logout();
    }

    public boolean isHamburgerMenuLinkPresent() {
        return waitForFragmentVisible(header).isHamburgerMenuLinkPresent();
    }

    public HamburgerMenu openHamburgerMenu() {
        return waitForFragmentVisible(header).openHamburgerMenu();
    }

    public IndigoDashboardsPage closeHamburgerMenu() {
        waitForFragmentVisible(header).closeHamburgerMenu();
        return this;
    }

    public IndigoDashboardsPage switchProject(String name) {
        System.out.println("Switching to project: " + name);
        waitForFragmentVisible(header).switchProject(name);

        return this;
    }

    public String getCurrentProjectName() {
        return waitForFragmentVisible(header).getCurrentProjectName();
    }

    public void waitForVisualizationsListAbsent() {
        waitForFragmentNotVisible(visualizationsList);
    }

    public VisualizationsList getVisualizationsList() {
        return waitForFragmentVisible(visualizationsList);
    }

    public boolean isVisualizationsListPresent() {
        return isElementPresent(By.className(VISUALIZATIONS_LIST_CLASS_NAME), this.getRoot());
    }

    public IndigoInsightSelectionPanel getInsightSelectionPanel() {
        return IndigoInsightSelectionPanel.getInstance(browser);
    }

    public IndigoDashboardsPage addInsightToLastPosition(final String insight) {
        getInsightSelectionPanel().addInsightToLastPosition(insight);
        return this;
    }

    public IndigoDashboardsPage addKpiWidget() {
        final String sourceCssSelector = convertCSSClassTojQuerySelector(ADD_KPI_PLACEHOLDER_CLASS_NAME);
        final String targetCssSelector = convertCSSClassTojQuerySelector(DASHBOARD_BODY);
        final String dropZoneCssSelector = convertCSSClassTojQuerySelector(LAST_DROPZONE_CLASS_NAME);

        dragAndDropWithCustomBackend(browser, sourceCssSelector, targetCssSelector, dropZoneCssSelector);
        return this;
    }

    public IndigoDashboardsPage reoderWidget(final Widget source, final Widget target, DropZone dropZone) {
        final String sourceCssSelector = convertCSSClassTojQuerySelector(source.getRoot().getAttribute("class"));
        final String targetCssSelector = convertCSSClassTojQuerySelector(target.getRoot().getAttribute("class"));
        final String dropZoneCssSelector = targetCssSelector + " " + dropZone.getCss();

        dragAndDropWithCustomBackend(browser, sourceCssSelector, targetCssSelector, dropZoneCssSelector);
        return this;
    }

    public boolean searchInsight(final String insight) {
        return getInsightSelectionPanel().searchInsight(insight);
    }
}
