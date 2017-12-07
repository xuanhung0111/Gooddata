package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;

import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;

public class EmbeddedReportPage extends ReportPage {

    public static final By LOCATOR = By.id("root");

    private static final By BY_PAGE_LOADED = By.cssSelector("#p-analysisPage.s-displayed");

    private static final By BY_SND_EDIT_METRIC_BUTTON = By.cssSelector(".s-btn-edit:not(.gdc-hidden)");
    private static final By BY_SND_DELETE_METRIC_BUTTON = By.cssSelector(".s-btn-delete:not(.gdc-hidden)");

    private static final By BY_CANCEL_BUTTON = By.cssSelector(".yui3-analysisembeddedheader .s-btn-cancel");

    public boolean isSndEditMetricButtonVisible() {
        return isElementVisible(BY_SND_EDIT_METRIC_BUTTON, getRoot());
    }

    public boolean isSndDeleteMetricButtonVisible() {
        return isElementVisible(BY_SND_DELETE_METRIC_BUTTON, getRoot());
    }

    public MetricEditorDialog clickEditInSndMetricDetail() {
        waitForElementVisible(BY_SND_EDIT_METRIC_BUTTON, getRoot()).click();

        return MetricEditorDialog.getInstance(browser);
    }

    public void clickDeleteInSndMetricDetail() {
        waitForElementVisible(BY_SND_DELETE_METRIC_BUTTON, getRoot()).click();
    }

    public void cancel() {
        waitForElementVisible(BY_CANCEL_BUTTON, browser).click();
    }

    public static void waitForPageLoaded(SearchContext searchContext) {
        waitForElementVisible(BY_PAGE_LOADED, searchContext);
    }

    @Override
    public ReportPage finishCreateReport() {
        clickSaveReport().confirmCreateReportInDialog();
        return this;
    }
}
