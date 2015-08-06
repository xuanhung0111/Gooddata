package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.report.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.NumericRangeFilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem;
import com.gooddata.qa.graphene.entity.filter.SelectFromListValuesFilterItem;
import com.gooddata.qa.graphene.entity.filter.VariableFilterItem;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SimpleMenu;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog.Formatter;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class ReportPage extends AbstractFragment {

    @FindBy(id = "analysisReportTitle")
    private WebElement reportName;

    @FindBy(xpath = "//input[@class='ipeEditor']")
    private WebElement reportNameInput;

    @FindBy(xpath = "//div[@class='c-ipeEditorControls']/button")
    private WebElement reportNameSaveButton;

    @FindBy(xpath = "//div[@id='reportSaveButtonContainer']/button")
    private WebElement createReportButton;

    @FindBy(xpath = "//div[contains(@class, 's-saveReportDialog')]//footer[@class='buttons']//button[contains(@class, 's-btn-create')]")
    private WebElement confirmDialogCreateButton;

    @FindBy(id = "reportVisualizer")
    private ReportVisualizer visualiser;

    @FindBy(xpath = "//button[contains(@class, 'exportButton')]")
    private WebElement exportButton;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-pdf')]//a")
    private WebElement exportToPDF;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-pdf__portrait_')]//a")
    private WebElement exportToPDFPortrait;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-pdf__landscape_')]//a")
    private WebElement exportToPDFLandscape;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-image__png_')]//a")
    private WebElement exportToPNG;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-excel_xls')]//a")
    private WebElement exportToXLS;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-excel_xlsx')]//a")
    private WebElement exportToXLSX;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-csv')]//a")
    private WebElement exportToCSV;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-raw_data__csv_')]//a")
    private WebElement exportToRawCSV;

    @FindBy(css = "div.report")
    private List<WebElement> reportsList;

    @FindBy(css = ".s-btn-save")
    private WebElement saveReportButton;

    @FindBy(css = ".s-btn-saved")
    private WebElement alreadySavedButton;

    @FindBy(xpath = "//div[contains(@class,'c-dashboardUsageWarningDialog')]")
    private WebElement confirmSaveDialog;

    private String confirmSaveDialogLocator = "//div[contains(@class,'c-dashboardUsageWarningDialog')]";

    private static final By VISIBILITY_CHECKBOX_LOCATOR = By.id("settings-visibility");

    private static final By REPORT_SETTINGS_SAVE_BUTTON_LOCATOR = By.cssSelector(".s-btn-save:not(.gdc-hidden)");

    @FindBy(xpath = "//span[2]/button[3]")
    private WebElement confirmSaveButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button[not (@disabled)]")
    private WebElement filterButton;

    @FindBy(xpath = "//div[@id='filtersContainer']")
    private ReportFilter reportFilter;

    @FindBy(id = "p-analysisPage")
    private TableReport tableReport;

    @FindBy(css = ".s-unlistedIcon")
    private WebElement unlistedIcon;

    @FindBy(css = ".s-btn-options")
    private WebElement optionsButton;

    @FindBy(css = ".s-btn-__show_configuration:not(.gdc-hidden)")
    private WebElement showConfigurationButton;

    private static final By CUSTOM_NUMBER_FORMAT_LOCATOR = By.className("s-btn-custom_number_formats");
    private static final By CUSTOM_METRIC_FORMAT_LOCATOR = By.className("customMetricFormatItem-format");
    private static final By APPLY_CONFIG_FORMAT_LOCATOR = By.cssSelector(".s-btn-apply:not(.disabled)");
    private static final By ADD_TAGS_BUTTON_LOCATOR = By.className("s-btn-add_tags");

    private static final By TAG_INPUT_LOCATOR = By.cssSelector(".c-ipeEditorIn input");
    private static final By OK_BUTTON_LOCATOR = By.cssSelector(".c-ipeEditorControls .s-btn-add");

    public ReportPage showConfiguration() {
        waitForElementVisible(showConfigurationButton).click();
        return this;
    }

    private ReportPage hideConfiguration() {
        waitForElementVisible(By.className("s-btn-hide__"), browser).click();
        return this;
    }

    public String getCustomNumberFormat() {
        waitForElementVisible(CUSTOM_NUMBER_FORMAT_LOCATOR, browser).click();
        return waitForElementVisible(CUSTOM_METRIC_FORMAT_LOCATOR, browser).getText();
    }

    public ReportPage changeNumberFormat(Formatter format) {
        waitForElementVisible(CUSTOM_NUMBER_FORMAT_LOCATOR, browser).click();
        waitForElementVisible(CUSTOM_METRIC_FORMAT_LOCATOR, browser).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormat(format);
        waitForElementVisible(APPLY_CONFIG_FORMAT_LOCATOR, browser).click();
        hideConfiguration();
        return this;
    }

    public ReportPage changeNumberFormatButDiscard(Formatter format) {
        waitForElementVisible(CUSTOM_NUMBER_FORMAT_LOCATOR, browser).click();
        waitForElementVisible(CUSTOM_METRIC_FORMAT_LOCATOR, browser).click();
        Graphene.createPageFragment(MetricFormatterDialog.class,
                waitForElementVisible(MetricFormatterDialog.LOCATOR, browser)).changeFormatButDiscard(format);
        waitForElementVisible(APPLY_CONFIG_FORMAT_LOCATOR, browser).click();
        hideConfiguration();
        return this;
    }

    public TableReport getTableReport() {
        return tableReport;
    }

    public ReportVisualizer getVisualiser() {
        return visualiser;
    }

    public ReportPage setReportName(String reportName) {
        waitForElementVisible(this.reportName).click();
        waitForElementVisible(reportNameInput).clear();
        reportNameInput.sendKeys(reportName);
        waitForElementVisible(reportNameSaveButton).click();
        waitForElementNotVisible(reportNameInput);
        assertEquals(this.reportName.getText(), reportName, "Report name wasn't updated");
        return this;
    }

    public String getReportName() {
        return reportName.getAttribute("title");
    }

    public void createReport(ReportDefinition reportDefinition) {
        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        sleepTightInSeconds(3);

        setReportName(reportDefinition.getName());

        if (reportDefinition.shouldAddWhatToReport())
            visualiser.selectWhatArea(reportDefinition.getWhats());

        if (reportDefinition.shouldAddHowToReport())
            visualiser.selectHowArea(reportDefinition.getHows());

        visualiser.finishReportChanges();

        if (reportDefinition.shouldAddFilterToReport()) {
            for (FilterItem filter : reportDefinition.getFilters()) {
                addFilter(filter);
            }
        }

        visualiser.selectReportVisualisation(reportDefinition.getType());
        waitForAnalysisPageLoaded(browser);
        createReport();
    }

    public void createReport() {
        waitForElementVisible(createReportButton).click();
        waitForElementVisible(confirmDialogCreateButton).click();
        waitForElementNotVisible(confirmDialogCreateButton);

        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return "Saved".equals(createReportButton.getText().trim());
            }
        });
        // When Create button change its name to Saving, and then Saved, the create report process is not finished.
        // Report have to refresh some parts, e.g. the What button have to enable, then disable, then enable.
        // If we navigate to another url when create report is not finished, unsaved change dialog will appear.
        // Use sleep here to make sure that process is finished
        sleepTightInSeconds(1);
    }

    public void createSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact, String metricName, boolean addToGlobal){
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(this.getRoot());
        visualiser.addSimpleMetric(metricOperation, metricOnFact, metricName, addToGlobal);
    }

    public String exportReport(ExportFormat format) {
        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        sleepTightInSeconds(3);

        String reportName = getReportName();
        waitForElementVisible(exportButton).click();
        WebElement currentExportLink = null;
        switch (format) {
            case PDF:
                currentExportLink = exportToPDF;
                break;
            case PDF_PORTRAIT:
                currentExportLink = exportToPDFPortrait;
                break;
            case PDF_LANDSCAPE:
                currentExportLink = exportToPDFLandscape;
                break;
            case IMAGE_PNG:
                currentExportLink = exportToPNG;
                break;
            case EXCEL_XLS:
                currentExportLink = exportToXLS;
                break;
            case EXCEL_XLSX:
                currentExportLink = exportToXLSX;
                break;
            case CSV:
                currentExportLink = exportToCSV;
                break;
            case RAW_CSV:
                currentExportLink = exportToRawCSV;
                break;
            default:
                break;
        }
        waitForElementVisible(currentExportLink).click();
        sleepTightInSeconds(5);
        // waitForElementVisible(BY_EXPORTING_STATUS); //this waiting is causing
        // some unexpected issues in tests when the export (xls/csv) is too fast
        waitForElementVisible(exportButton);
        sleepTightInSeconds(3);
        System.out.println("Report " + reportName + " exported to "
                + format.getName());
        return reportName;
    }

    public ReportPage addFilter(FilterItem filterItem) {
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(filterButton).click();
        waitForElementVisible(reportFilter.getRoot());
        String textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountBefore = getNumber(textOnFilterButton);

        if (filterItem instanceof SelectFromListValuesFilterItem) {
            reportFilter.addFilterSelectList((SelectFromListValuesFilterItem) filterItem);

        } else if (filterItem instanceof RankingFilterItem) {
            reportFilter.addRankFilter((RankingFilterItem) filterItem);

        } else if (filterItem instanceof NumericRangeFilterItem) {
            reportFilter.addRangeFilter((NumericRangeFilterItem) filterItem);

        } else if (filterItem instanceof VariableFilterItem) {
            reportFilter.addPromtFiter((VariableFilterItem) filterItem);

        } else {
            throw new IllegalArgumentException("Unknow filter item: " + filterItem);
        }

        textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountAfter = getNumber(textOnFilterButton);
        assertEquals(filterCountAfter, filterCountBefore + 1, "Filter wasn't added");
        sleepTightInSeconds(2);
        return this;
    }

    public void saveReport() {
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(createReportButton).click();
        if (browser.findElements(By.xpath(confirmSaveDialogLocator)).size() > 0) {
            waitForElementVisible(confirmSaveButton).click();
        }
        sleepTightInSeconds(3);
        assertEquals(createReportButton.getText(), "Saved", "Report wasn't saved");
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

    public List<String> getFilters() throws InterruptedException {
        String textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCount = getNumber(textOnFilterButton);
        if (filterCount == 0)
            return Collections.emptyList();

        // Need to sleep here. If we go too fast, action click is still successful
        // but nothing happen
        Thread.sleep(3000);
        filterButton.click();
        waitForElementVisible(reportFilter.getRoot());
        return Lists.newArrayList(Collections2.transform(reportFilter.getRoot()
                .findElements(By.cssSelector(".filterLinesContainer li span.text")),
                new Function<WebElement, String>() {
            @Override
            public String apply(WebElement element) {
                return element.getText().trim();
            }
        }));
    }

    public void setReportVisible() {
        setReportVisibleSettings(true);
    }

    public void setReportInvisible() {
        setReportVisibleSettings(false);
    }

    public ReportPage addTag(String tag) {
        waitForAnalysisPageLoaded(browser);
        openOptionsMenu().select("Settings");

        WebElement addTagButton = waitForElementVisible(ADD_TAGS_BUTTON_LOCATOR, browser);
        addTagButton.click();
        waitForElementNotVisible(addTagButton);

        WebElement input = waitForElementVisible(TAG_INPUT_LOCATOR, browser);
        input.clear();
        input.sendKeys(tag);
        waitForElementVisible(OK_BUTTON_LOCATOR, browser).click();
        waitForElementVisible(addTagButton);

        waitForElementVisible(REPORT_SETTINGS_SAVE_BUTTON_LOCATOR, browser).click();
        return this;
    }

    public void deleteCurrentReport() {
        waitForAnalysisPageLoaded(browser);
        openOptionsMenu().select("Delete");
        waitForElementVisible(By.cssSelector(".c-confirmDeleteDialog .s-btn-delete"), browser).click();
    }

    private void setReportVisibleSettings(boolean isVisible) {
        waitForAnalysisPageLoaded(browser);
        openOptionsMenu().select("Settings");
        WebElement visibleCheckbox = waitForElementVisible(VISIBILITY_CHECKBOX_LOCATOR, browser);
        if (isVisible != visibleCheckbox.isSelected()) {
            visibleCheckbox.click();
        }
        waitForElementVisible(REPORT_SETTINGS_SAVE_BUTTON_LOCATOR, browser).click();
        waitForElementNotVisible(visibleCheckbox);
    }

    private SimpleMenu openOptionsMenu() {
        waitForElementVisible(optionsButton).click();
        SimpleMenu menu = Graphene.createPageFragment(SimpleMenu.class,
                waitForElementVisible(SimpleMenu.LOCATOR, browser));
        return menu;
    }
}
