package com.gooddata.qa.graphene.fragments.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.testng.Assert;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.FilterTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.reports.ReportFilter;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class ReportPage extends AbstractFragment {

    @FindBy(id = "analysisReportTitle")
    private WebElement reportName;

    @FindBy(xpath = "//input[@class='ipeEditor']")
    private WebElement reportNameInput;

    @FindBy(xpath = "//div[@class='c-ipeEditorControls']/button")
    private WebElement reportNameSaveButton;

    @FindBy(xpath = "//div[@id='reportSaveButtonContainer']/button")
    private WebElement createReportButton;

    @FindBy(xpath = "//div[contains(@class, 's-saveReportDialog')]//div[@class='bd_controls']//button[text()='Create']")
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

    @FindBy(xpath = "//span[2]/button[3]")
    private WebElement confirmSaveButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button[not (@disabled)]")
    private WebElement filterButton;

    @FindBy(xpath = "//div[@id='filtersContainer']")
    private ReportFilter reportFilter;

    public ReportVisualizer getVisualiser() {
        return visualiser;
    }

    public void setReportName(String reportName) {
        waitForElementVisible(this.reportName).click();
        waitForElementVisible(reportNameInput).clear();
        reportNameInput.sendKeys(reportName);
        waitForElementVisible(reportNameSaveButton).click();
        waitForElementNotVisible(reportNameInput);
        Assert.assertEquals(this.reportName.getText(), reportName,
                "Report name wasn't updated");
    }

    public String getReportName() {
        return reportName.getAttribute("title");
    }

    public void createReport(String reportName, ReportTypes reportType,
                             List<String> what, List<String> how) throws InterruptedException {
        setReportName(reportName);
        // select what - metrics
        visualiser.selectWhatArea(what);

        // select how - attributes
        visualiser.selectHowArea(how);

        visualiser.finishReportChanges();

        // visualiser.selectFilterArea();
        // TODO

        visualiser.selectReportVisualisation(reportType);
        waitForAnalysisPageLoaded();
        waitForElementVisible(createReportButton);
        Thread.sleep(2000);
        createReportButton.click();
        waitForElementVisible(confirmDialogCreateButton).click();
        waitForElementNotVisible(confirmDialogCreateButton);
        Assert.assertEquals(createReportButton.getText(), "Saved",
                "Report wasn't saved");
    }

    public String exportReport(ExportFormat format) throws InterruptedException {
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
        Thread.sleep(5000);
        // waitForElementVisible(BY_EXPORTING_STATUS); //this waiting is causing
        // some unexpected issues in tests when the export (xls/csv) is too fast
        waitForElementVisible(exportButton);
        Thread.sleep(3000);
        System.out.println("Report " + reportName + " exported to "
                + format.getName());
        return reportName;
    }

    public void addFilter(FilterTypes filterType, Map<String, String> data)
            throws InterruptedException {
        waitForAnalysisPageLoaded();
        waitForElementVisible(filterButton).click();
        waitForElementVisible(reportFilter.getRoot());
        String textOnFilterButton = waitForElementVisible(filterButton)
                .getText();
        float filterCountBefore = getNumber(textOnFilterButton);
        switch (filterType) {
            case ATTRIBUTE:
                reportFilter.addFilterSelectList(data);
                break;
            case RANK:
                reportFilter.addRankFilter(data);
                break;
            case RANGE:
                reportFilter.addRangeFilter(data);
                break;
            case PROMPT:
                reportFilter.addPromtFiter(data);
                break;
            default:
                break;
        }
        textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountAfter = getNumber(textOnFilterButton);
        Assert.assertEquals(filterCountAfter, filterCountBefore + 1,
                "Filter wasn't added");
        Thread.sleep(2000);
    }

    public void saveReport() throws InterruptedException {
        waitForAnalysisPageLoaded();
        waitForElementVisible(createReportButton).click();
        if (browser.findElements(By.xpath(confirmSaveDialogLocator)).size() > 0) {
            waitForElementVisible(confirmSaveButton).click();
        }
        Thread.sleep(3000);
        Assert.assertEquals(createReportButton.getText(), "Saved",
                "Report wasn't saved");
    }

    public static float getNumber(String text) {
        String tmp = "";
        float number = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 45 && text.charAt(i) < 58 && text.charAt(i) != 47) {
                tmp += text.charAt(i);
            }
        }
        if (tmp.length() > 0) {
            number = Float.parseFloat(tmp);
        }
        return number;
    }
}
