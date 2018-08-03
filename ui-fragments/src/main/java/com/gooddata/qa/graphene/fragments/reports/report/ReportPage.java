package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.PermissionSettingDialog.PermissionType;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.filter.AbstractFilterFragment;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.FilterFragment;
import com.gooddata.qa.graphene.utils.WaitUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.REPORT_FILTER_LOCATOR;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_BLUE_BAR;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementAttributeNotContainValue;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.assertEquals;

public class ReportPage extends AbstractFragment {

    private static final String CSS_CREATE_BUTTON = ".s-btn-create,.s-btn-save:not(.gdc-hidden),.s-btn-saved";

    @FindBy(id = "analysisReportTitle")
    private WebElement reportName;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorWhatArea')]/button")
    private WebElement whatButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorHowArea')]/button")
    private WebElement howButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button[not (@disabled)]")
    private WebElement filterButton;

    @FindBy(className = "s-btn-hide_filters")
    private WebElement hideFilterButton;

    @FindBy(css = CSS_CREATE_BUTTON)
    private WebElement createReportButton;

    @FindBy(xpath = "//button[contains(@class, 'exportButton')]")
    private WebElement exportButton;

    @FindBy(css = ".s-btn-options")
    private WebElement optionsButton;

    @FindBy(className = "s-lockIcon")
    private WebElement lockIcon;

    @FindBy(className = "s-unlistedIcon")
    private WebElement eyeIcon;

    public static final By LOCATOR = By.id("p-analysisPage");

    private static final By SHOW_CONFIGURATION_LOCATOR =
            cssSelector(".s-btn-__show_configuration:not(.gdc-hidden)");

    private static final By HIDE_CONFIGURATION_LOCATOR = className("s-btn-hide__");

    private static final By CUSTOM_NUMBER_FORMAT_LOCATOR = className("s-btn-custom_number_formats");

    private static final By CUSTOM_METRIC_FORMAT_LOCATOR = className("customMetricFormatItem-format");

    private static final String XPATH_REPORT_VISUALIZATION_TYPE =
            "//div[contains(@class, 's-enabled') and contains(@class, 'yui3-c-charttype') and .//span[@title='${type}']]";

    private static final By EMBED_BUTTON_LOCATOR = cssSelector(".s-btn-embed:not(.disabled)");

    private static final By UNSAVED_WARNING_EMBED_LOCATOR = cssSelector(".unsavedWarning-share");

    private static final By METRIC_AXIS_CONFIGURATION_CONTENT_LOCATOR = By
            .cssSelector("div.yui3-c-metricaxisconfiguration-content:not(.gdc-hidden)");

    public static final ReportPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(ReportPage.class, waitForElementVisible(id("p-analysisPage"), context));
    }

    public ReportPage initPage() {
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(this);

        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        sleepTightInSeconds(3);
        return this;
    }

    public ReportPage setReportName(String name) {
        waitForElementVisible(reportName).click();

        WebElement reportNameInput = waitForElementVisible(xpath("//input[@class='ipeEditor']"), browser);
        reportNameInput.clear();
        reportNameInput.sendKeys(name);

        waitForElementVisible(xpath("//div[@class='c-ipeEditorControls']/button"), browser).click();
        waitForElementNotVisible(reportNameInput);
        assertEquals(getReportName(), name, "Report name wasn't updated");
        return this;
    }

    public String getReportName() {
        return waitForElementVisible(reportName).getAttribute("title");
    }

    public MetricSndPanel openWhatPanel() {
        waitForElementVisible(whatButton).click();
        return getPanel(MetricSndPanel.class);
    }

    public AttributeSndPanel openHowPanel() {
        waitForElementVisible(howButton).click();
        return getPanel(AttributeSndPanel.class);
    }

    public <T extends AbstractSndPanel> T getPanel(Class<T> clazz) {
        WebElement panelRoot = waitForElementVisible(AbstractSndPanel.LOCATOR, getRoot());
        panelRoot.findElements(By.className("sndWheel")).forEach(WaitUtils::waitForElementNotVisible);

        return Graphene.createPageFragment(clazz, panelRoot);
    }

    public ReportFilter openFilterPanel() {
        selectFilterButton();
        return Graphene.createPageFragment(ReportFilter.class,
                waitForElementVisible(REPORT_FILTER_LOCATOR, browser));
    }

    public ReportPage hideFilterPanel() {
        waitForElementVisible(hideFilterButton).click();
        waitForElementNotVisible(hideFilterButton);
        return this;
    }

    public void tryOpenFilterPanelInDisabledState() {
        if(!waitForElementVisible(filterButton).getAttribute("class").contains("editorBtnEditorSadDisabled"))
            throw new RuntimeException("Filter button is not disabled");

        filterButton.click();
    }

    public ReportPage selectReportVisualisation(ReportTypes type) {
        By icon = xpath(XPATH_REPORT_VISUALIZATION_TYPE.replace("${type}", type.getName()));
        waitForElementVisible(icon, browser);
        waitForElementVisible(id("reportVisualizationContainer"), browser).findElement(icon).click();
        waitForElementVisible(id(type.getContainerTabId()), browser);
        return this;
    }

    public String getSelectedChartType() {
        WebElement selectedElement = waitForElementVisible(
                cssSelector(".yui3-c-charttype-selected .c-chartType"), browser);
        return selectedElement.findElement(cssSelector("span.label")).getAttribute("title");
    }

    public ReportPage confirmCreateReportInDialog() {
        CreatedReportDialog.getInstance(browser).saveReport();
        return this;
    }

    public ReportPage cancelCreateReportInDialog() {
        CreatedReportDialog.getInstance(browser).cancelCreateReport();
        return this;
    }

    public ReportPage showConfiguration() {
        waitForElementEnabled(waitForElementVisible(SHOW_CONFIGURATION_LOCATOR, getRoot())).click();
        waitForElementEnabled(waitForElementVisible(HIDE_CONFIGURATION_LOCATOR, getRoot()));
        return this;
    }

    public ReportPage showCustomNumberFormat() {
        waitForElementVisible(CUSTOM_NUMBER_FORMAT_LOCATOR, browser).click();
        return this;
    }

    public String getCustomNumberFormat() {
        return waitForElementVisible(CUSTOM_METRIC_FORMAT_LOCATOR, browser).getText();
    }

    public MetricFormatterDialog openMetricFormatterDialogFromConfiguration() {
        waitForElementVisible(CUSTOM_METRIC_FORMAT_LOCATOR, browser).click();
        return Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser));
    }

    public ReportPage changeNumberFormat(Formatter format) {
        openMetricFormatterDialogFromConfiguration().changeFormat(format);
        return this;
    }

    public ReportPage changeNumberFormatButDiscard(Formatter format) {
        openMetricFormatterDialogFromConfiguration().changeFormatButDiscard(format);
        return this;
    }

    public TableReport getTableReport() {
        return Graphene.createPageFragment(TableReport.class,
                waitForElementVisible(id("gridContainerTab"), browser));
    }

    public OneNumberReport getHeadlineReport() {
        return Graphene.createPageFragment(OneNumberReport.class,
                waitForElementVisible(cssSelector(".c-oneNumberReport"), browser));
    }

    public String exportReport(ExportFormat format) {
        doExporting(format);

        if (format == ExportFormat.EXCEL_XLSX) {
            getExportXLSXDialog().confirmExport();
        }

        sleepTightInSeconds(5);
        final int reportExportingTimeoutInSeconds = 300;
        waitForElementVisible(exportButton, reportExportingTimeoutInSeconds);

        sleepTightInSeconds(3);
        String reportName = getReportName();

        System.out.println("Report " + reportName + " exported to " + format.getName());
        return reportName;
    }

    public ExportXLSXDialog getExportXLSXDialog() {
        return ExportXLSXDialog.getInstance(browser);
    }

    public void doExporting(ExportFormat format) {
        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        sleepTightInSeconds(3);
        waitForElementVisible(exportButton).click();

        String exportXpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-%s')]//a";
        switch (format) {
            case PDF:
                exportXpath = format(exportXpath, "pdf");
                break;
            case PDF_PORTRAIT:
                exportXpath = format(exportXpath, "pdf__portrait_");
                break;
            case PDF_LANDSCAPE:
                exportXpath = format(exportXpath, "pdf__landscape_");
                break;
            case IMAGE_PNG:
                exportXpath = format(exportXpath, "image__png_");
                break;
            case EXCEL_XLSX:
                exportXpath = format(exportXpath, "xlsx___");
                break;
            case CSV:
                exportXpath = format(exportXpath, "csv__formatted_");
                break;
            case RAW_CSV:
                exportXpath = format(exportXpath, "csv__raw_data_");
                break;
            default:
                break;
        }
        waitForElementVisible(xpath(exportXpath), browser).click();
    }

    public ReportPage addFilter(FilterItem filterItem) {
        String textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountBefore = getNumber(textOnFilterButton);

        openFilterPanel().addFilter(filterItem);

        textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountAfter = getNumber(textOnFilterButton);
        assertEquals(filterCountAfter, filterCountBefore + 1, "Filter wasn't added");
        waitForReportExecutionProgress();
        return this;
    }

    public ReportPage saveReport() {
        return clickSaveReport().waitForReportSaved();
    }

    public ReportPage finishCreateReport() {
        return clickSaveReport().confirmCreateReportInDialog().waitForReportSaved();
    }

    public ReportPage clickSaveReport() {
        waitForElementAttributeNotContainValue(createReportButton, "class", "disabled");
        createReportButton.click();
        return this;
    }

    public ReportPage confirmSaveReport() {
        waitForElementVisible(xpath("//span[2]/button[3]"), browser).click();
        return this;
    }

    public ReportPage cancelSaveReport() {
        waitForElementVisible(cssSelector(".yui3-c-button-focused.s-btn-cancel"), browser).click();
        return this;
    }

    public ReportPage waitForReportSaved() {
        Function<WebDriver, Boolean> createReportPredicate = input -> "Saved".equals(createReportButton.getText());
        Graphene.waitGui().until(createReportPredicate);

        // When Create button change its name to Saving, and then Saved, the create report process is not finished.
        // Report have to refresh some parts, e.g. the What button have to enable, then disable, then enable.
        // If we navigate to another url when create report is not finished, unsaved change dialog will appear.
        // Use sleep here to make sure that process is finished
        sleepTightInSeconds(1);
        return this;
    }

    public ReportPage saveAsReport() {
        return saveAsReport(null);
    }

    public ReportPage saveAsReport(String name) {
        openOptionsMenu().select("Save as...");
        CreatedReportDialog createdReportDialog = CreatedReportDialog.getInstance(browser);
        if (name != null) {
            createdReportDialog.setReportName(name);
        }
        createdReportDialog.saveReport();
        return this;
    }

    public List<String> getFilters() {
        String textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCount = getNumber(textOnFilterButton);
        if (filterCount == 0)
            return emptyList();

        openFilterPanel();

        return getElementTexts(waitForElementVisible(REPORT_FILTER_LOCATOR, browser)
            .findElements(cssSelector(".filterLinesContainer li span.text")));
    }

    public ReportPage setReportVisible() {
        openOptionsMenu().select("Settings");
        CreatedReportDialog.getInstance(browser).setReportVisibleSettings(true).saveReport();
        return this;
    }

    public ReportPage addTag(String tag) {
        openOptionsMenu().select("Settings");
        CreatedReportDialog.getInstance(browser).addTag(tag).saveReport();
        return this;
    }

    public ReportPage setPermission(PermissionType permissionType) {
        openOptionsMenu().select("Settings");
        CreatedReportDialog.getInstance(browser).setPermissionSetting(permissionType).saveReport();
        return this;
    }

    public ReportPage setVisibilityPermission(boolean visible) {
        openOptionsMenu().select("Settings");
        CreatedReportDialog.getInstance(browser).setReportVisibleSettings(visible).saveReport();
        return this;
    }

    public void deleteCurrentReport() {
        openOptionsMenu().select("Delete");
        waitForElementVisible(cssSelector(".c-confirmDeleteDialog .s-btn-delete"), browser).click();
    }

    public ReportEmbedDialog openReportEmbedDialog() {
        WebElement embedButton = browser.findElement(EMBED_BUTTON_LOCATOR);
        new Actions(browser).moveToElement(embedButton).perform();
        waitForElementVisible(embedButton).click();
        return Graphene.createPageFragment(ReportEmbedDialog.class,
                waitForElementVisible(cssSelector(".c-embedDialog"), browser));
    }

    public WebElement embedUnsavedReport() {
        waitForElementVisible(EMBED_BUTTON_LOCATOR, browser).click();
        return waitForElementVisible(UNSAVED_WARNING_EMBED_LOCATOR, browser);
    }

    public void createReportFromUnsavedWarningEmbed() {
        WebElement unsavedWarningEmbed = waitForElementVisible(UNSAVED_WARNING_EMBED_LOCATOR, browser);
        waitForElementVisible(cssSelector(".unsavedWarning-share .save"), unsavedWarningEmbed).click();
        confirmCreateReportInDialog().waitForReportSaved();
    }

    public void closeEmbedUnsavedWarning() {
        WebElement unsavedWarningEmbed = waitForElementVisible(UNSAVED_WARNING_EMBED_LOCATOR, browser);
        waitForElementVisible(cssSelector(".unsavedWarning-share .close"), unsavedWarningEmbed).click();
    }

    public int getVersionsCount() {
        waitForAnalysisPageLoaded(browser);
        openOptionsMenu().openSubMenu("Versions");
        return getVersionsMenu().getItemsCount();
    }

    public boolean isReportTooLarge() {
        return isElementPresent(cssSelector("#tooBigReportHelp:not([style*='display: none'])"), browser);
    }

    public boolean isVisibleLockIcon() {
        return isElementVisible(lockIcon);
    }

    public boolean isVisibleEyeIcon() {
        return isElementVisible(eyeIcon);
    }

    public boolean isVisibleSaveButton() {
        //In embedded mode, save button is outside report page
        return isElementVisible(cssSelector(CSS_CREATE_BUTTON), browser);
    }

    public String getTooltipFromLockIcon() {
        new Actions(browser).moveToElement(lockIcon).moveByOffset(1, 1).perform();
        return waitForElementVisible(cssSelector(".bubble-overlay .content"), browser).getText();
    }

    public String getTooltipFromEyeIcon() {
        new Actions(browser).moveToElement(eyeIcon).moveByOffset(1, 1).perform();
        return waitForElementVisible(cssSelector(".bubble-overlay .content"), browser).getText();
    }

    public CreatedReportDialog clickLockIcon() {
        waitForElementVisible(lockIcon).click();
        return CreatedReportDialog.getInstance(browser);
    }

    public CreatedReportDialog clickEyeIcon() {
        waitForElementVisible(eyeIcon).click();
        return CreatedReportDialog.getInstance(browser);
    }

    public ReportPage openVersion(int version) {
        openOptionsMenu().openSubMenu("Versions");
        getVersionsMenu().select(e -> e.findElement(BY_LINK).getText().trim().startsWith("Version #" + version));
        return this;
    }

    public CreatedReportDialog saveAs() {
        openOptionsMenu().select("Save as...");
        return CreatedReportDialog.getInstance(browser);
    }

    public ReportPage revertToCurrentVersion() {
        if (!isElementPresent(BY_BLUE_BAR, browser)) {
            System.out.println("Report is not in old version.");
            return this;
        }
        browser.findElement(cssSelector(".restore > button")).click();
        return this;
    }

    public boolean hasUnsavedVersion() {
        openOptionsMenu().openSubMenu("Versions");
        return getVersionsMenu().contains("Unsaved Version");
    }

    public boolean tryCancelComputing() {
        try {
            waitForElementVisible(cssSelector("#executionProgress > .s-btn-cancel"), browser).click();
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            return false;
        }
    }

    public String getExecuteProgressStatus() {
        return waitForElementVisible(cssSelector("#executionProgress > span"), browser).getText();
    }

    public ReportPage recompute() {
        waitForElementVisible(cssSelector("#executionProgress > .s-btn-recompute"), browser).click();
        return this;
    }

    public void createReport(UiReportDefinition reportDefinition) {
        setReportName(reportDefinition.getName());

        openWhatPanel().selectMetrics(reportDefinition.getWhats());
        openHowPanel().selectAttribtues(reportDefinition.getHows()).done();
        waitForAnalysisPageLoaded(browser);

        addFilters(reportDefinition.getFilters()).selectReportVisualisation(reportDefinition.getType());
        waitForAnalysisPageLoaded(browser);
        finishCreateReport();
    }

    public String getDataReportHelpMessage() {
        return waitForElementVisible(id("emptyDataReportHelp"), browser)
                .findElement(By.className("alert")).getText();
    }

    public boolean verifyOldVersionState() {
        return browser.findElement(BY_BLUE_BAR).getText().startsWith("You are currently viewing an older version")
                && waitForElementVisible(whatButton).getAttribute("class").contains("disabled")
                && waitForElementVisible(howButton).getAttribute("class").contains("disabled")
                && waitForElementVisible(filterButton).getAttribute("class").contains("disabled");
    }

    public String getInvalidDataReportMessage() {
        return waitForElementVisible(cssSelector("#invalidDataReportHelp > em"), browser).getText().trim();
    }

    public boolean isInvalidDataReportMessageVisible() {
        return isElementPresent(cssSelector("#invalidDataReportHelp:not([style*='display: none'])"), browser);
    }

    public static float getNumber(String text) {
        String tmp = "";
        float number = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 44 && text.charAt(i) < 58 && text.charAt(i) != 47) {
                tmp += text.charAt(i);
            }
        }
        if (tmp.length() > 0) {
            number = Float.parseFloat(tmp);
        }
        return number;
    }

    public ReportPage forceRenderChartReport() {
        try {
            WebElement renderChartButton = waitForElementVisible(
                    cssSelector(".chartGenerationCancelDialog .s-btn-render_chart"), browser);
            renderChartButton.click();
            waitForElementNotVisible(renderChartButton);
        } catch (Exception e) {
            // do nothing
        }
        return this;
    }

    public ReportPage removeDrillStepInConfigPanel(final String metric, final String attribute) {
        waitForElementVisible(className("s-btn-drill_in_settings"), browser).click();
        WebElement container = waitForElementVisible(className("drillAcrossStepContainer"), browser);
        container.findElements(className("drillAcrossStep"))
            .stream()
            .filter(e -> metric.equals(e.findElement(className("metricTitle")).getText()))
            .map(e -> e.findElement(className("s-remove-" + simplifyText(attribute))))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Cannot find metric: " + metric))
            .click();
        return this;
    }

    public ReportPage waitForProgressOverlayNotVisible() {
        sleepTightInSeconds(1);
        final WebElement progress = waitForElementPresent(id("progressOverlay"), browser);
        Function<WebDriver, Boolean> waitForProgress = browser -> progress.getCssValue("display").equals("none");
        Graphene.waitGui().until(waitForProgress);
        return this;
    }
    
    public ReportPage waitForReportExecutionProgress() {
        waitForProgressOverlayNotVisible();
        Function<WebDriver, Boolean> waitForFilterButton = browser -> !filterButton.getAttribute("class").contains("disabled");
        Graphene.waitGui().until(waitForFilterButton);
        return this;
    }

    public boolean isRangeFilterApplied(RangeFilterItem filterItem) {
        return getTableReport().getMetricValues()
                    .stream()
                    .allMatch(metricValue -> isMetricValueInRange(metricValue, filterItem));
    }

    public boolean isRankingFilterApplied(List<Float> expectedMetricList) {
        List<Float> listOfMetrics = getTableReport()
                .getMetricValues()
                .stream()
                .filter(metricValue -> !metricValue.equals(0f))
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        return listOfMetrics.equals(expectedMetricList);
    }

    public boolean isReportContains(List<String> expectedAttributeList) {
        final List<String> attributeValuesList = getTableReport().getAttributeValues();

        return expectedAttributeList.stream()
                .allMatch(attributeValue -> attributeValuesList.contains(attributeValue));
    }

    public <T extends AbstractFilterFragment> T openExistingFilter(String filterName, FilterFragment fragment) {
        return openFilterPanel().openExistingFilter(filterName, fragment);
    }

    public boolean hoverMouseToExistingFilter(String filterName) {
        return openFilterPanel().hoverMouseToFilter(filterName);
    }

    public void deleteExistingFilter(String filterName) {
        openFilterPanel().deleteFilter(filterName);
        waitForSaveButtonEnabled();
    }

    public ReportPage exchangeColAndRowHeaders() {
        waitForElementVisible(By.className("iconBtnSwapheaders"), browser).click();
        waitForReportExecutionProgress();
        return this;
    }

    public ReportPage displayMetricsInDifferentRows() {
        waitForElementVisible(By.className("iconBtnMetricsvertically"), browser).click();
        waitForReportExecutionProgress();
        return this;
    }

    public ReportPage openMetricAxisConfiguration() {
        waitForElementVisible(cssSelector(".bucketsContainer .metric .dropdown"), browser).click();
        return this;
    }

    public List<String> getMetricAxisConfigurationNames() {
        openMetricAxisConfiguration();
        return getElementTexts(getMetricAxisConfigurationRows(), e -> e.findElement(tagName("label")));
    }

    public String getReportStatistic() {
        return waitForElementVisible(className("statistics"), browser).getText();
    }

    public List<String> getCustomFormatItemTitles() {
        return getElementTexts(getCustomFormatElements(), e -> e.findElement(className("metricTitle")));
    }

    public ReportPage showMoreReportInfo() {
        waitForElementVisible(className("s-btn-more_report_info"), browser).click();
        return this;
    }

    public void openUsedData(final String data) {
        browser.findElements(cssSelector(".moreInfo .report")).stream()
                .map(e -> e.findElement(By.tagName("a")))
                .filter(e -> data.equals(e.getText()))
                .findFirst()
                .get()
                .click();
    }

    public List<WebElement> getReportUsageLinks() {
        return waitForElementVisible(className("yui3-c-reportusageinfo-content"), browser)
                .findElements(cssSelector(".content a"));
    }

    public SimpleMenu openOptionsMenu() {
        waitForElementVisible(optionsButton).click();
        return SimpleMenu.getInstance(browser);
    }

    public ReportPage closeUnlistedBubble() {
        By closeBubbleButton = cssSelector(".unlistedBubble button");
        waitForElementVisible(closeBubbleButton, browser).click();
        waitForElementNotVisible(closeBubbleButton);
        return this;
    }
    private ReportPage addFilters(Collection<FilterItem> filters) {
        filters.stream().forEach(this::addFilter);
        return this;
    }

    private SimpleMenu getVersionsMenu() {
        return Graphene.createPageFragment(SimpleMenu.class, waitForElementVisible(id("undefined"), browser));
    }

    private boolean isMetricValueInRange(float metricValue, RangeFilterItem filterItem) {
        return filterItem.getRangeType().isMetricValueInRange(metricValue, filterItem.getRangeNumber());
    }

    private void waitForSaveButtonEnabled() {
        Function<WebDriver, Boolean> saveButtonEnabled = browser -> !waitForElementVisible(createReportButton)
                .getAttribute("class")
                .contains("disabled");
        Graphene.waitGui().until(saveButtonEnabled);
    }

    private void selectFilterButton() {
        Optional.of(waitForElementVisible(filterButton))
                .filter(e -> !e.getAttribute("class").contains("editorBtnEditorSadHighlight"))
                .ifPresent(e -> waitForElementEnabled(e).click());
    }

    private List<WebElement> getMetricAxisConfigurationRows() {
        return waitForElementVisible(METRIC_AXIS_CONFIGURATION_CONTENT_LOCATOR, browser).findElements(
                cssSelector(".c-collectionWidget .content.yui3-c-metricaxisconfigurationrow"));
    }

    private List<WebElement> getCustomFormatElements() {
        return waitForElementVisible(className("customMetricFormatContainer"), browser)
                .findElements(By.className("customMetricFormatItem"));
    }

}
