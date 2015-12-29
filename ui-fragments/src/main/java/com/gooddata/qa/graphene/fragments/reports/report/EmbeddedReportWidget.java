package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;

import java.util.List;
import java.util.Set;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.ReportInfoViewPanel;
import com.gooddata.qa.graphene.utils.Sleeper;
import com.gooddata.qa.graphene.utils.frame.InFrameAction;

public class EmbeddedReportWidget extends AbstractFragment {

    private static final By REPORT_INFO_LOCATOR = By.cssSelector("#reportInfoContainer");
    private static final By REPORT_INFO_VIEW_PANEL_HANDLE_LOCATOR = By.cssSelector(".reportInfoPanelHandle");
    private static final By REPORT_INFO_VIEW_PANEL_LOCATOR = By.cssSelector(".reportInfoView");
    private static final By TABLE_REPORT_LOCATOR = By.cssSelector(".c-report-container");
    private static final By HEADLINE_REPORT_LOCATOR = By.cssSelector(".c-oneNumberReport");

    public WebElement getReportInfo() {
        return waitForElementVisible(REPORT_INFO_LOCATOR, browser);
    }

    public EmbeddedReportWidget drillMetricValueInFrame(String metricValue) {
        return doActionInFrame(() -> drillMetricValue(metricValue));
    }

    public EmbeddedReportWidget drillMetricValue(String metricValue) {
        getTableReport().drillOnMetricValue(metricValue);

        return this;
    }

    public EmbeddedReportWidget drillAttributeValueInFrame(String attributeValue) {
        return doActionInFrame(() -> drillAttributeValue(attributeValue));
    }

    public EmbeddedReportWidget drillAttributeValue(String attributeValue) {
        getTableReport().drillOnAttributeValue(attributeValue);

        return this;
    }

    public EmbeddedReportWidget downloadEmbeddedReportInFrame(ExportFormat format) {
        return doActionInFrame(() -> downloadEmbeddedReport(format));
    }

    public EmbeddedReportWidget downloadEmbeddedReport(ExportFormat format) {
        openReportInfoViewPanel().downloadReportAsFormat(format);
        Sleeper.sleepTightInSeconds(5); // To make sure that report is downloaded completely

        return this;
    }

    public EmbeddedReportWidget viewEmbeddedReportInFrame() {
        return doActionInFrame(() -> viewEmbeddedReport());
    }

    public EmbeddedReportWidget viewEmbeddedReport() {
        openReportInfoViewPanel().clickViewReportButton();

        return this;
    }

    public ReportInfoViewPanel openReportInfoViewPanel() {
        new Actions(browser).moveToElement(waitForElementVisible(REPORT_INFO_LOCATOR, browser)).perform();
        waitForElementVisible(REPORT_INFO_VIEW_PANEL_HANDLE_LOCATOR, browser).click();
        return Graphene.createPageFragment(ReportInfoViewPanel.class,
                waitForElementVisible(REPORT_INFO_VIEW_PANEL_LOCATOR, browser));
    }

    public TableReport getTableReport() {
        TableReport report =
                Graphene.createPageFragment(TableReport.class,
                        waitForElementVisible(TABLE_REPORT_LOCATOR, browser));
        report.waitForReportLoading();
        return report;
    }

    public boolean isEmptyReportInFrame() {
        return doActionInFrame(() -> isEmptyReport());
    }

    public boolean isEmptyReport() {
        return Graphene
                .createPageFragment(TableReport.class, waitForElementVisible(TABLE_REPORT_LOCATOR, browser))
                .isNoData();
    }

    public String getHeadlineDescriptionInFrame() {
        return doActionInFrame(() -> getHeadlineDescription());
    }

    public String getHeadlineDescription() {
        return getHeadLineReport().getDescription();
    }

    public String getHeadlineValueInFrame() {
        return doActionInFrame(() -> getHeadlineValue());
    }

    public String getHeadlineValue() {
        return getHeadLineReport().getValue();
    }

    public String getEmbeddedReportTitleInframe() {
        return doActionInFrame(() -> getReportTitle());
    }

    public String getReportTitle() {
        return getReportInfo().getText();
    }

    public List<String> getAttributeHeadersInFrame() {
        return doActionInFrame(() -> getAttributeHeaders());
    }

    public List<String> getAttributeHeaders() {
        return getTableReport().getAttributesHeader();
    }

    public Set<String> getMetricHeadersInFrame() {
        return doActionInFrame(() -> getMetricHeaders());
    }

    public Set<String> getMetricHeaders() {
        return getTableReport().getMetricsHeader();
    }

    public List<String> getAttributeElementsInFrame() {
        return doActionInFrame(() -> getAttributeElements());
    }

    public List<Float> getMetricElementsInFrame() {
        return doActionInFrame(() -> getMetricElements());
    }

    public List<String> getAttributeElements() {
        return getTableReport().getAttributeElements();
    }

    public List<Float> getMetricElements() {
        return getTableReport().getMetricElements();
    }

    private OneNumberReport getHeadLineReport() {
        return Graphene.createPageFragment(OneNumberReport.class,
                waitForElementVisible(HEADLINE_REPORT_LOCATOR, browser));
    }

    private <T> T doActionInFrame(InFrameAction<T> action) {
        return InFrameAction.Utils.doActionInFrame(this.getRoot(), action, browser);
    }
}
