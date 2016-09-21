package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.REPORT_FILTER_LOCATOR;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_BLUE_BAR;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;
import static org.openqa.selenium.By.xpath;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.report.ReportTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.gooddata.qa.graphene.fragments.reports.filter.AbstractFilterFragment;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter.FilterFragment;
import com.google.common.base.Predicate;

public class ReportPage extends AbstractFragment {

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

    @FindBy(css = ".s-btn-create,.s-btn-save:not(.gdc-hidden),.s-btn-saved")
    private WebElement createReportButton;

    @FindBy(xpath = "//button[contains(@class, 'exportButton')]")
    private WebElement exportButton;

    @FindBy(css = ".s-btn-options")
    private WebElement optionsButton;

    public static final By LOCATOR = By.id("p-analysisPage");

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    private static final By METRIC_ATTRIBUTE_CONTAINER_LOCATOR =
            cssSelector(".sndPanel1:not([style^='display: none']) .dataContainer .cell");

    private static final By ATTRIBUTES_CONTAINER_LOCATOR = cssSelector(".s-snd-AttributesContainer .gridTile");

    private static final By METRICS_CONTAINER_LOCATOR = cssSelector(".s-snd-MetricsContainer .gridTile");

    private static final By SHOW_CONFIGURATION_LOCATOR =
            cssSelector(".s-btn-__show_configuration:not(.gdc-hidden)");

    private static final By HIDE_CONFIGURATION_LOCATOR = className("s-btn-hide__");

    private static final By CUSTOM_NUMBER_FORMAT_LOCATOR = className("s-btn-custom_number_formats");

    private static final By CUSTOM_METRIC_FORMAT_LOCATOR = className("customMetricFormatItem-format");

    private static final String XPATH_REPORT_VISUALIZATION_TYPE =
            "//div[contains(@class, 's-enabled')]/div[contains(@class, 'c-chartType') and"
            + " ./span[@title='${type}']]";

    private static final By VISIBILITY_CHECKBOX_LOCATOR = id("settings-visibility");

    private static final By REPORT_SETTINGS_SAVE_BUTTON_LOCATOR = cssSelector(".s-btn-save:not(.gdc-hidden)");

    private static final By ADD_TAGS_BUTTON_LOCATOR = className("s-btn-add_tags");

    private static final By EMBED_BUTTON_LOCATOR = cssSelector(".s-btn-embed:not(.disabled)");

    private static final By UNSAVED_WARNING_EMBED_LOCATOR = cssSelector(".unsavedWarning-share");

    private static final By ADD_SIMPLE_METRIC_LOCATOR = xpath("//button[contains(@class,'s-sme-addButton')]");

    private static final By ADD_TO_GLOBAL_METRICS_LOCATOR = xpath("//input[contains(@class,'s-sme-global')]");

    private static final By NO_MATCHING_METRIC = By.cssSelector(".sndPanel .s-snd-MetricsContainer + .noMatch");

    private static final By NO_MATCHING_ATTRIBUTE = 
            By.cssSelector(".sndPanel .s-snd-AttributesContainer + .noMatch");

    private static final By METRIC_AXIS_CONFIGURATION_CONTENT_LOCATOR = By
            .cssSelector("div.yui3-c-metricaxisconfiguration-content:not(.gdc-hidden)");

    private static final By SND_DIALOG_LOADING = By.cssSelector("form.sndFooterForm > .progress.s-loading");

    private static final By DONE_BUTTON_LOCATOR = By.cssSelector("form.sndFooterForm > button.s-btn-done:not([disabled])");

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

    public ReportPage openWhatPanel() {
        waitForElementVisible(whatButton).click();
        return this;
    }

    public ReportPage openHowPanel() {
        waitForElementVisible(howButton).click();
        return this;
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

    
    public void tryOpenFilterPanel() {
        selectFilterButton();
    }

    public ReportPage selectFolderLocation(String folder) {
        waitForCollectionIsNotEmpty(browser.findElements(METRIC_ATTRIBUTE_CONTAINER_LOCATOR))
            .stream()

            // date dimension has title like this: Date dimension (Activity)-Date dimension (Activity)
            // so using startsWith() instead of equals() for general case
            .filter(e -> e.getAttribute("title").startsWith(folder))

            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Cannot find folder: " + folder))
            .click();
        return this;
    }

    public ReportPage selectMetric(String metric) {
        return selectMetric(metric, e -> {
            e.click();
            //wait for metric details displayed in third column of SND dialog
            //or warning dialog of "You have reached the limit of metrics in report"
            waitForSndMetricDetail();
        });
    }

    public void waitForSndMetricDetail() {
        Predicate<WebDriver> sndMetricDetailVisible = browser ->
                isElementVisible(By.className("c-metricDetailViewButton"), browser)
                || isElementVisible(By.className("t-reportEditorMessage"), browser);

        Graphene.waitGui().until(sndMetricDetailVisible);
    }

    public ReportPage selectInapplicableMetric(String metric) {
        return selectMetric(metric, e -> new Actions(browser).keyDown(Keys.SHIFT).click(e).keyUp(Keys.SHIFT).
                perform());
    }

    public ReportPage selectInapplicableAttribute(String attribute) {
        return selectAttribute(attribute, e -> new Actions(browser).keyDown(Keys.SHIFT).click(e).keyUp(Keys.SHIFT).
                perform());
    }

    public ReportPage selectAttribute(String attribute) {
        return selectAttribute(attribute, WebElement::click);
    }

    public ReportPage deselectAttribute(String attribute) {
        searchAttribute(attribute);
        findAttribute(attribute).findElement(tagName("input")).click();
        return this;
    }

    public ReportPage searchAttribute(String attribute) {
        WebElement filterInput = waitForElementVisible(xpath("//label[@class='sndAttributeFilterLabel']/../input"),
                browser);

        filterInput.clear();
        filterInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        sleepTightInSeconds(1);
        waitForElementVisible(NO_MATCHING_ATTRIBUTE, browser);

        filterInput.clear();
        filterInput.sendKeys(attribute);
        sleepTightInSeconds(1);
        waitForElementVisible(ATTRIBUTES_CONTAINER_LOCATOR, browser);

        return this;
    }

    public ReportPage doneSndPanel() {
        // When webapp do a lot of CRUD things, its rendering job will work slowly,
        // so need a short time to wait in case like this
        sleepTightInSeconds(2);

        waitForElementNotPresent(SND_DIALOG_LOADING);
        waitForElementVisible(DONE_BUTTON_LOCATOR, browser).click();
        waitForElementNotVisible(DONE_BUTTON_LOCATOR);

        Predicate<WebDriver> predicate = input -> !waitForElementVisible(filterButton)
                .getAttribute("class")
                .contains("disabled");
        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).until(predicate);
        return this;
    }

    public ReportPage selectReportVisualisation(ReportTypes type) {
        By icon = xpath(XPATH_REPORT_VISUALIZATION_TYPE.replace("${type}", type.getName()));
        waitForElementVisible(icon, browser);
        waitForElementVisible(id("reportVisualizationContainer"), browser).findElement(icon).click();
        waitForElementVisible(id(type.getContainerTabId()), browser);
        return this;
    }

    public ReportPage confirmCreateReportInDialog() {
        WebElement confirmDialogCreateButton = waitForElementVisible(xpath(
                "//div[contains(@class, 's-saveReportDialog')]//footer[@class='buttons']"
                + "//button[contains(@class, 's-btn-create')]"), browser);
        waitForElementVisible(confirmDialogCreateButton).click();
        waitForElementNotVisible(confirmDialogCreateButton);

        return this;
    }

    public ReportPage cancelCreateReportInDialog() {
        WebElement cancelDialogCreateButton = waitForElementVisible(cssSelector(
                ".s-saveReportDialog .s-btn-cancel"), browser);
        waitForElementVisible(cancelDialogCreateButton).click();
        waitForElementNotVisible(cancelDialogCreateButton);

        return this;
    }

    public ReportPage showConfiguration() {
        waitForElementVisible(SHOW_CONFIGURATION_LOCATOR, browser).click();
        return this;
    }

    public ReportPage hideConfiguration() {
        waitForElementVisible(HIDE_CONFIGURATION_LOCATOR, browser).click();
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

    public ReportPage createSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact){
        initSimpleMetric(metricOperation, metricOnFact);
        waitForElementVisible(ADD_SIMPLE_METRIC_LOCATOR, browser).click();
        return this;
    }

    public ReportPage createGlobalSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact) {
        initSimpleMetric(metricOperation, metricOnFact);
        waitForElementVisible(ADD_TO_GLOBAL_METRICS_LOCATOR, browser).click();
        waitForElementVisible(ADD_SIMPLE_METRIC_LOCATOR, browser).click();
        return this;
    }

    public ReportPage createGlobalSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact,
            String folder) {
        initSimpleMetric(metricOperation, metricOnFact);
        waitForElementVisible(ADD_TO_GLOBAL_METRICS_LOCATOR, browser).click();
        new Select(waitForElementVisible(xpath("//select[contains(@class,'s-sme-folder')]"), browser))
            .selectByVisibleText("Create New Folder");
        waitForElementVisible(xpath("//input[contains(@class,'newFolder')]"), browser).sendKeys(folder);

        waitForElementVisible(ADD_SIMPLE_METRIC_LOCATOR, browser).click();
        By snDFolder = By.xpath("//div[@title='${SnDFolderName}']".replace("${SnDFolderName}", folder));
        waitForElementVisible(snDFolder, browser);
        return this;
    }

    public String exportReport(ExportFormat format) {
        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        sleepTightInSeconds(3);

        String reportName = getReportName();
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
            case EXCEL_XLS:
                exportXpath = format(exportXpath, "excel_xls");
                break;
            case EXCEL_XLSX:
                exportXpath = format(exportXpath, "excel_xlsx");
                break;
            case CSV:
                exportXpath = format(exportXpath, "csv");
                break;
            case RAW_CSV:
                exportXpath = format(exportXpath, "raw_data__csv_");
                break;
            default:
                break;
        }
        waitForElementVisible(xpath(exportXpath), browser).click();

        sleepTightInSeconds(5);
        final int reportExportingTimeoutInSeconds = 300;
        waitForElementVisible(exportButton, reportExportingTimeoutInSeconds);

        sleepTightInSeconds(3);
        System.out.println("Report " + reportName + " exported to " + format.getName());
        return reportName;
    }

    public ReportPage addFilter(FilterItem filterItem) {
        String textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountBefore = getNumber(textOnFilterButton);

        openFilterPanel().addFilter(filterItem);

        textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountAfter = getNumber(textOnFilterButton);
        assertEquals(filterCountAfter, filterCountBefore + 1, "Filter wasn't added");
        sleepTightInSeconds(2);
        return this;
    }

    public ReportPage saveReport() {
        return clickSaveReport().waitForReportSaved();
    }

    public ReportPage finishCreateReport() {
        return clickSaveReport().confirmCreateReportInDialog().waitForReportSaved();
    }

    public ReportPage clickSaveReport() {
        waitForElementVisible(createReportButton).click();
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
        Predicate<WebDriver> createReportPredicate = input -> "Saved".equals(createReportButton.getText());
        Graphene.waitGui().until(createReportPredicate);

        // When Create button change its name to Saving, and then Saved, the create report process is not finished.
        // Report have to refresh some parts, e.g. the What button have to enable, then disable, then enable.
        // If we navigate to another url when create report is not finished, unsaved change dialog will appear.
        // Use sleep here to make sure that process is finished
        sleepTightInSeconds(1);
        return this;
    }

    public ReportPage saveAsReport() {
        openOptionsMenu().select("Save as...");
        waitForElementVisible(className("s-btn-save_as"), browser).click();
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
        setReportVisibleSettings(true);
        return this;
    }

    public ReportPage setReportInvisible() {
        setReportVisibleSettings(false);
        return this;
    }

    public ReportPage addTag(String tag) {
        openOptionsMenu().select("Settings");

        WebElement addTagButton = waitForElementVisible(ADD_TAGS_BUTTON_LOCATOR, browser);
        addTagButton.click();
        waitForElementNotVisible(addTagButton);

        WebElement input = waitForElementVisible(cssSelector(".c-ipeEditorIn input"), browser);
        input.clear();
        input.sendKeys(tag);
        waitForElementVisible(cssSelector(".c-ipeEditorControls .s-btn-add"), browser).click();
        waitForElementVisible(addTagButton);

        waitForElementVisible(REPORT_SETTINGS_SAVE_BUTTON_LOCATOR, browser).click();
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

    public ReportPage openVersion(int version) {
        openOptionsMenu().openSubMenu("Versions");
        getVersionsMenu().select(e -> e.findElement(BY_LINK).getText().trim().startsWith("Version #" + version));
        return this;
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

    public ReportPage addDrillStep(String attribute) {
        if (isNull(attribute)) {
            return this;
        }

        waitForElementVisible(cssSelector(".c-metricDetailDrillStep button"), browser).click();
        WebElement popupElement = waitForElementVisible(SelectItemPopupPanel.LOCATOR, browser);
        SelectItemPopupPanel popupPanel = Graphene.createPageFragment(SelectItemPopupPanel.class, popupElement);
        popupPanel.searchAndSelectItem(attribute).submitPanel();
        return this;
    }

    public void createReport(UiReportDefinition reportDefinition) {
        initPage()
        .setReportName(reportDefinition.getName())
        .openWhatPanel()
        .selectMetrics(reportDefinition.getWhats())
        .openHowPanel()
        .selectAttributes(reportDefinition.getHows())
        .doneSndPanel()
        .addFilters(reportDefinition.getFilters())
        .selectReportVisualisation(reportDefinition.getType());
        waitForAnalysisPageLoaded(browser);
        finishCreateReport();
    }

    public boolean isGreyedOutAttribute(String attribute) {
        return findAttribute(attribute).findElement(BY_PARENT)
                .getAttribute("class").contains("sndUnReachable");
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

    public ReportPage switchViewToTags() {
        new Select(waitForElementVisible(cssSelector(".sndPanelTitle[style='display: inline;'] .sndViewSelector"),
                browser)).selectByValue("tags");
        return this;
    }

    public ReportPage changeDisplayLabel(String label) {
        new Select(waitForElementVisible(cssSelector(".sndAttributeDetailDisplayLabelChangerContainer .selection"),
                browser)).selectByVisibleText(label);
        return this;
    }

    public List<String> loadAllViewGroups() {
        return browser.findElements(cssSelector(".sndPanel1[style='display: block;'] .element > span")).stream()
            .skip(1)
            .map(WebElement::getText)
            .collect(toList());
    }

    public String getTooltipMessageOfAttribute(String attribute) {
        searchAttribute(attribute);
        return findAttribute(attribute).findElement(BY_PARENT).getAttribute("title");
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

    public ReportPage waitForReportExecutionProgress() {
        sleepTightInSeconds(1);
        final WebElement progress = waitForElementPresent(id("progressOverlay"), browser);
        Predicate<WebDriver> waitForProgress = browser -> progress.getCssValue("display").equals("none");
        Graphene.waitGui().until(waitForProgress);
        return this;
    }

    public boolean isRangeFilterApplied(RangeFilterItem filterItem) {
        return getTableReport().getMetricElements()
                    .stream()
                    .allMatch(metricValue -> isMetricValueInRange(metricValue, filterItem));
    }

    public boolean isRankingFilterApplied(List<Float> expectedMetricList) {
        List<Float> listOfMetrics = getTableReport()
                .getMetricElements()
                .stream()
                .filter(metricValue -> !metricValue.equals(0f))
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        return listOfMetrics.equals(expectedMetricList);
    }

    public boolean isReportContains(List<String> expectedAttributeList) {
        final List<String> attributeValuesList = getTableReport().getAttributeElements();

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

    public ReportPage deleteFilterInSndDialog() {
        waitForElementVisible(By.className("deleteFilter"), browser).click();
        waitForElementNotVisible(By.className("deleteFilter"));
        return this;
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

    public ReportPage deselectMetric(final String metric) {
        searchMetric(metric);
        findMetric(metric).findElement(tagName("input")).click();
        return this;
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

    private ReportPage selectAttribute(String attribute, Consumer<WebElement> howToSelect) {
        searchAttribute(attribute);
        howToSelect.accept(findAttribute(attribute));
        return this;
    }

    private ReportPage addFilters(Collection<FilterItem> filters) {
        filters.stream().forEach(this::addFilter);
        return this;
    }

    private ReportPage selectMetrics(Collection<WhatItem> metrics) {
        if (metrics.isEmpty()) {
            return this;
        }

        metrics.stream().forEach(what -> {
            selectMetric(what.getMetric()).addDrillStep(what.getDrillStep());
        });
        return this;
    }

    public ReportPage selectAttributes(Collection<HowItem> attributes) {
        if (attributes.isEmpty()) {
            return this;
        }

        attributes.stream().forEach(how -> {
            selectAttribute(how.getAttribute().getName())
            .selectAttributePosition(how)
            .filterAttribute(how.getFilterValues());
        });
        return this;
    }

    public ReportPage selectAttributePosition(HowItem attribute) {
        sleepTight(500);
        WebElement attributeElement = findAttribute(attribute.getAttribute().getName());
        sleepTightInSeconds(2);

        WebElement attributePositionElement = waitForElementVisible(
                attributeElement.findElement(cssSelector("div")));
        String attributeClass =  attributePositionElement.getAttribute("class");

        if (!attributeClass.contains(attribute.getPosition().getCssClass())) {
            attributePositionElement.click();
        }
        return this;
    }

    private WebElement findAttribute(String attribute) {
        WebElement attributesContainer = waitForElementVisible(ATTRIBUTES_CONTAINER_LOCATOR, browser);
        return waitForElementVisible(cssSelector(".s-grid-" + simplifyText(attribute) + " .metricName"),
                attributesContainer);
    }

    private ReportPage filterAttribute(Collection<String> values) {
        if (values.isEmpty()) {
            return this;
        }
        waitForElementNotPresent(cssSelector(".busyMask[style='display: block;]"));
        waitForElementVisible(cssSelector(".s-btn-filter_this_attribute"), browser).click();
        SelectItemPopupPanel panel = Graphene.createPageFragment(SelectItemPopupPanel.class,
                waitForElementVisible(cssSelector(".c-attributeElementsFilterEditor"), browser));
        values.stream().forEach(panel::searchAndSelectItem);
        return this;
    }

    public void clickAddNewSimpleMetric() {
        waitForElementVisible(xpath("//button[contains(@class,'sndCreateMetric')]"), browser).click();
    }

    public MetricEditorDialog clickAddAdvanceMetric() {
        waitForElementVisible(className("sndCreateAdvancedMetric"), browser).click();

        return MetricEditorDialog.getInstance(browser);
    }

    private void initSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact) {
        clickAddNewSimpleMetric();
        new Select(waitForElementVisible(xpath("//select[contains(@class,'s-sme-fnSelect')]"), browser))
            .selectByVisibleText(metricOperation.name());

        Select operation = new Select(
                waitForElementVisible(xpath("//select[contains(@class,'s-sme-objSelect')]"), browser));
        waitForCollectionIsNotEmpty(operation.getOptions());
        operation.selectByVisibleText(metricOnFact);
    }

    public SimpleMenu openOptionsMenu() {
        waitForElementVisible(optionsButton).click();
        return SimpleMenu.getInstance(browser);
    }

    public MetricEditorDialog editMetric(String metric) {
        // waitForSndMetricDetail() does not work on local metric
        selectMetric(metric, WebElement::click);
        waitForElementVisible(By.className("c-metricDetailEditButton"), getRoot()).click();

        return MetricEditorDialog.getInstance(browser);
    }

    private void setReportVisibleSettings(boolean isVisible) {
        openOptionsMenu().select("Settings");
        WebElement visibleCheckbox = waitForElementVisible(VISIBILITY_CHECKBOX_LOCATOR, browser);
        if (isVisible != visibleCheckbox.isSelected()) {
            visibleCheckbox.click();
        }
        waitForElementVisible(REPORT_SETTINGS_SAVE_BUTTON_LOCATOR, browser).click();
        waitForElementNotVisible(visibleCheckbox);
    }

    private SimpleMenu getVersionsMenu() {
        return Graphene.createPageFragment(SimpleMenu.class, waitForElementVisible(id("undefined"), browser));
    }

    private boolean isMetricValueInRange(float metricValue, RangeFilterItem filterItem) {
        return filterItem.getRangeType().isMetricValueInRange(metricValue, filterItem.getRangeNumber());
    }

    private void waitForSaveButtonEnabled() {
        Predicate<WebDriver> saveButtonEnabled = browser -> !waitForElementVisible(createReportButton)
                .getAttribute("class")
                .contains("disabled");
        Graphene.waitGui().until(saveButtonEnabled);
    }
    
    private void selectFilterButton() {
        Optional.of(waitForElementVisible(filterButton))
                .filter(e -> !e.getAttribute("class").contains("editorBtnEditorSadHighlight"))
                .ifPresent(WebElement::click);
    }

    private List<WebElement> getMetricAxisConfigurationRows() {
        return waitForElementVisible(METRIC_AXIS_CONFIGURATION_CONTENT_LOCATOR, browser).findElements(
                cssSelector(".c-collectionWidget .content.yui3-c-metricaxisconfigurationrow"));
    }

    private List<WebElement> getCustomFormatElements() {
        return waitForElementVisible(className("customMetricFormatContainer"), browser)
                .findElements(By.className("customMetricFormatItem"));
    }

    private ReportPage searchMetric(final String metric) {
        final WebElement filterInput = waitForElementVisible(By.cssSelector(".gdc-input.metFilter"), browser);

        filterInput.clear();
        filterInput.sendKeys(WEIRD_STRING_TO_CLEAR_ALL_ITEMS);
        sleepTightInSeconds(1);
        waitForElementVisible(NO_MATCHING_METRIC, browser);
        sleepTightInSeconds(1);
        filterInput.clear();
        filterInput.sendKeys(metric);
        sleepTightInSeconds(1);
        waitForElementNotVisible(NO_MATCHING_METRIC);
        return this;
    }

    private ReportPage selectMetric(String metric, Consumer<WebElement> howToSelect) {
        searchMetric(metric);
        howToSelect.accept(findMetric(metric));

        return this;
    }

    private WebElement findMetric(final String metric) {
        final WebElement metricContainer = waitForElementVisible(METRICS_CONTAINER_LOCATOR, browser);
        return waitForElementVisible(cssSelector(".s-grid-" + simplifyText(metric) + " .metricName"),
                metricContainer);
    }
}
