package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.utils.CheckUtils.*;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.reports.report.AbstractReport;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.report.OneNumberReport;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.utils.CheckUtils;
import com.gooddata.qa.graphene.utils.frame.InFrameAction;

public class EmbeddedDashboardWidget extends AbstractFragment {

    private static final By REPORT_PAGE_ON_EMBEDDED_DASHBOARD_LOCATOR = By.id("p-analysisPage");
    private static final By DASHBOARDS_PAGE_LOCATOR = By.id("root");
    private static final By LOADED_DASHBOARD_LOCATOR = By.cssSelector(".s-dashboardLoaded");
    private static final By EDIT_EMBEDDED_DASHBOARD_BUTTON_LOCATOR = By.cssSelector(".s-editButton");

    public EmbeddedDashboardWidget checkRedBarInFrame() {
        return doActionInFrame(() -> {
            CheckUtils.checkRedBar(browser);
            return this;
        });
    }

    public EmbeddedDashboardWidget editDashboard() {
        waitForElementVisible(EDIT_EMBEDDED_DASHBOARD_BUTTON_LOCATOR, browser).click();
        return this;
    }

    public EmbeddedDashboardWidget editDashboardInFrame() {
        return doActionInFrame(() -> editDashboard());
    }

    public EmbeddedDashboardWidget setCustomRecipientsInFrame(List<String> recipients) {
        return doActionInFrame(() -> setCustomRecipients(recipients));
    }
    
    public EmbeddedDashboardWidget setCustomRecipients(List<String> recipients) {
        getDashboardScheduleDialog().setCustomRecipients(recipients);
        return this;
    }
    
    public EmbeddedDashboardWidget setCustomScheduleSubjectInFrame(String customSubject) {
        return doActionInFrame(() -> setCustomScheduleSubject(customSubject));
    }
    
    public EmbeddedDashboardWidget setCustomScheduleSubject(String customSubject) {
        getDashboardScheduleDialog().setCustomEmailSubject(customSubject);
        return this;
    }
    
    public EmbeddedDashboardWidget saveDashboardScheduleInFrame() {
        return doActionInFrame(() -> saveDashboardSchedule());
    }
    
    public EmbeddedDashboardWidget saveDashboardSchedule() {
        assertTrue(getDashboardScheduleDialog().schedule(), "Dashboard is not scheduled successfully!");
        return this;
    }
    
    public String getCustomScheduleSubjectInFrame() {
         return doActionInFrame(() -> getCustomScheduleSubject());
    }
    
    public String getCustomScheduleSubject() {
        return getDashboardScheduleDialog().getCustomEmailSubject();
    }
    
    public EmbeddedDashboardWidget showCustomScheduleFormInFrame() {
        return doActionInFrame(() -> showCustomScheduleForm());
    }
    
    public EmbeddedDashboardWidget showCustomScheduleForm() {
        getDashboardScheduleDialog().showCustomForm();
        return this;
    }
    
    public String getSelectedFrequencyInFrame() {
        return doActionInFrame(() -> getSelectedFrequency());
    }
    
    public String getSelectedFrequency() {
        return getDashboardScheduleDialog().getSelectedFrequency();
    }

    public String getSelectedTabToScheduleInFrame() {
        return doActionInFrame(() -> getSelectedTabToSchedule());
    }

    public String getSelectedTabToSchedule() {
        return getDashboardScheduleDialog().getSelectedTab();
    }
    
    public DashboardScheduleDialog getDashboardScheduleDialog() {
        return Graphene.createPageFragment(DashboardScheduleDialog.class,
                waitForElementVisible(DashboardScheduleDialog.LOCATOR, browser));
    }
    
    public DashboardScheduleDialog openDashboardScheduleDialogInFrame() {
        return doActionInFrame(() -> openDashboardScheduleDialog());
    }
    
    public DashboardScheduleDialog openDashboardScheduleDialog() {
        return getDashboardsPage().showDashboardScheduleDialog();
    }

    public EmbeddedDashboardWidget modifyFirstAttributeFilterInFrame(String... values) {
        return doActionInFrame(() -> modifyFirstAttributeFilter(values));
    }

    public EmbeddedDashboardWidget modifyFirstAttributeFilter(String... values) {
        getDashboardsPage().getFirstFilter().changeAttributeFilterValue(values);
        return this;
    }

    public boolean isPrintButtonVisibleInFrame() {
        return doActionInFrame(() -> isPrintButtonVisible());
    }

    public boolean isPrintButtonVisible() {
        return getDashboardsPage().getPrintButton().isDisplayed();
    }

    public boolean isEditButtonVisibleInFrame() {
        return doActionInFrame(() -> isEditButtonVisible());
    }

    public boolean isEditButtonVisible() {
        return waitForElementPresent(EDIT_EMBEDDED_DASHBOARD_BUTTON_LOCATOR, browser).isDisplayed();
    }

    public boolean isTabBarVisibleInFrame() {
        return doActionInFrame(() -> isTabBarVisible());
    }

    public boolean isTabBarVisible() {
        return getDashboardsPage().isTabBarVisible();
    }

    public EmbeddedDashboardWidget closeDrillDialogInFrame() {
        return doActionInFrame(() -> closeDrillDialog());
    }

    public EmbeddedDashboardWidget closeDrillDialog() {
        getDrillDialog().closeDialog();
        return this;
    }

    public List<Float> getDrilledReportMetricsInFrame() {
        return doActionInFrame(() -> getDrilledReportMetrics());
    }

    public List<Float> getDrilledReportMetrics() {
        return getDrilledTableReport().getMetricElements();
    }

    public List<String> getDrilledReportAttributesInFrame() {
        return doActionInFrame(() -> getDrilledReportAttributes());
    }

    public List<String> getDrilledReportAttributes() {
        return getDrilledTableReport().getAttributeElements();
    }

    public TableReport getDrilledTableReport() {
        DashboardDrillDialog drillDialog = getDrillDialog();
        return drillDialog.getReport(TableReport.class);
    }

    public DashboardDrillDialog getDrillDialog() {
        return Graphene.createPageFragment(DashboardDrillDialog.class,
                waitForElementVisible(DashboardDrillDialog.LOCATOR, browser));
    }

    public EmbeddedDashboardWidget drillOnMetricInFrame(String reportTitle, String metricValue) {
        return doActionInFrame(() -> drillOnMetric(reportTitle, metricValue));
    }

    public EmbeddedDashboardWidget drillOnMetric(String reportTitle, String metricValue) {
        getReport(reportTitle, TableReport.class).drillOnMetricValue(metricValue);
        return this;
    }

    public EmbeddedDashboardWidget drillOnAttributeInFrame(String reportTitle, String attributeValue) {
        return doActionInFrame(() -> drillOnAttribute(reportTitle, attributeValue));
    }

    public EmbeddedDashboardWidget drillOnAttribute(String reportTitle, String attributeValue) {
        getReport(reportTitle, TableReport.class).drillOnAttributeValue(attributeValue);
        return this;
    }

    public String downloadEmbeddedDashboardInFrame(int tabIndex) {
        return doActionInFrame(() -> downloadEmbeddedDashboard(tabIndex));
    }

    public String downloadEmbeddedDashboard(int tabIndex) {
        return getDashboardsPage().printDashboardTab(tabIndex);
    }

    public EmbeddedDashboardWidget openTabInFrame(int tabIndex) {
        doActionInFrame(() -> openTab(tabIndex));
        return this;
    }

    public EmbeddedDashboardWidget openTab(int tabIndex) {
        getDashboardsPage().getTabs().openTab(tabIndex);
        waitForEmbeddedDashboardLoaded();
        return this;
    }

    public String getHeadlineReportDescriptionInFrame(String reportTitle) {
        return doActionInFrame(() -> getHeadlineReportDescription(reportTitle));
    }

    public String getHeadlineReportDescription(String reportTitle) {
        return getReport(reportTitle, OneNumberReport.class).getDescription();
    }

    public String getHeadlineReportValueInFrame(String reportTitle) {
        return doActionInFrame(() -> getHeadlineReportValue(reportTitle));
    }

    public String getHeadlineReportValue(String reportTitle) {
        return getReport(reportTitle, OneNumberReport.class).getValue();
    }

    public EmbeddedDashboardWidget exportChartReportInFrame(String reportTitle) {
        return doActionInFrame(() -> exportChartReport(reportTitle));
    }

    public EmbeddedDashboardWidget exportChartReport(String reportTitle) {
        getReport(reportTitle, ChartReport.class).openReportInfoViewPanel().downloadReportAsFormat(
                ExportFormat.PDF);
        return this;
    }

    public ChartReport getChartReport(String reportTitle) {
        return getReport(reportTitle, ChartReport.class);
    }

    public List<Float> getMetricsOnReportPageInFrame() {
        return doActionInFrame(() -> getMetricsOnReportPage());
    }

    public List<Float> getMetricsOnReportPage() {
        return getReportPageOnEmbeddedDashboard().getTableReport().getMetricElements();
    }

    public List<String> getAttributesOnReportPageInFrame() {
        return doActionInFrame(() -> getAttributesOnReportPage());
    }

    public List<String> getAttributesOnReportPage() {
        return getReportPageOnEmbeddedDashboard().getTableReport().getAttributeElements();
    }

    public ReportPage getReportPageOnEmbeddedDashboardInFrame() {
        return doActionInFrame(() -> getReportPageOnEmbeddedDashboard());
    }

    public ReportPage getReportPageOnEmbeddedDashboard() {
        return Graphene.createPageFragment(ReportPage.class,
                waitForElementVisible(REPORT_PAGE_ON_EMBEDDED_DASHBOARD_LOCATOR, browser));
    }

    public EmbeddedDashboardWidget openTabularReportInFrame(String reportTitle) {
        return doActionInFrame(() -> openTabularReport(reportTitle));
    }

    public EmbeddedDashboardWidget openTabularReport(String reportTitle) {
        getReport(reportTitle, TableReport.class).openReportInfoViewPanel().clickViewReportButton();
        return this;
    }

    public List<String> getTableReportAttributesInFrame(String reportTitle) {
        return doActionInFrame(() -> getTableReportAttributes(reportTitle));
    }

    public List<String> getTableReportAttributes(String reportTitle) {
        return getReport(reportTitle, TableReport.class).getAttributeElements();
    }

    public List<Float> getTableReportMetricsInFrame(String reportTitle) {
        return doActionInFrame(() -> getTableReportMetrics(reportTitle));
    }

    public List<Float> getTableReportMetrics(String reportTitle) {
        return getReport(reportTitle, TableReport.class).getMetricElements();
    }

    public <T extends AbstractReport> T getReport(final String name, Class<T> clazz) {
        return getDashboardsPage().getContent().getReport(name, clazz);
    }

    public <T extends AbstractReport> T getReportInFrame(final String name, Class<T> clazz) {
        return doActionInFrame(() -> getReport(name, clazz));
    }

    public boolean isReportTitleVisibleInFrame(final TableReport report) {
        return doActionInFrame(() -> report.isReportTitleVisible());
    }

    public int getNumberOfTabsInFrame() {
        return doActionInFrame(() -> getNumberOfTabs());
    }

    public int getNumberOfTabs() {
        return getDashboardsPage().getTabs().getNumberOfTabs();
    }

    public DashboardsPage getDashboardsPage() {
        return Graphene.createPageFragment(DashboardsPage.class,
                waitForElementVisible(DASHBOARDS_PAGE_LOCATOR, browser));
    }

    public EmbeddedDashboardWidget waitForEmbeddedDashboardLoaded() {
        waitForElementVisible(LOADED_DASHBOARD_LOCATOR, browser);
        return this;
    }

    private <T> T doActionInFrame(InFrameAction<T> action) {
        return InFrameAction.Utils.doActionInFrame(this.getRoot(), action, browser);
    }
}
