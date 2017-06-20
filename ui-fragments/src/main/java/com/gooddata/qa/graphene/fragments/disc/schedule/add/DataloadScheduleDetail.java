package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Objects.nonNull;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.disc.schedule.add.ExecuteADDConfirmDialog.LoadMode;
import com.gooddata.qa.graphene.fragments.disc.schedule.common.AbstractScheduleDetail;
import com.google.common.base.Predicate;

public class DataloadScheduleDetail extends AbstractScheduleDetail {

    @FindBy(className = "dataset-selection")
    private DatasetUploadSection datasetUploadSection;

    public static final DataloadScheduleDetail getInstance(SearchContext searchContext) {
        return getInstance(searchContext, DataloadScheduleDetail.class);
    }

    public DataloadScheduleDetail executeSchedule() {
        return executeSchedule(null);
    }

    public DataloadScheduleDetail selectCustomDatasetsOption() {
        waitForFragmentVisible(datasetUploadSection).selectCustomDatasetsOption();
        return this;
    }

    public boolean isAllDatasetsOptionSelected() {
        return waitForFragmentVisible(datasetUploadSection).isAllDatasetsOptionSelected();
    }

    public boolean isCustomDatasetsOptionSelected() {
        return waitForFragmentVisible(datasetUploadSection).isCustomDatasetsOptionSelected();
    }

    public DatasetDropdown getDatasetDropdown() {
        return waitForFragmentVisible(datasetUploadSection).getDatasetDropdown();
    }

    public Collection<String> getSelectedDatasets() {
        return waitForFragmentVisible(datasetUploadSection).getSelectedDatasets();
    }

    private DataloadScheduleDetail executeSchedule(LoadMode mode) {
        int executionItems = executionHistoryItems.size();

        waitForElementVisible(runButton).click();

        ExecuteADDConfirmDialog dialog = ExecuteADDConfirmDialog.getInstance(browser);
        if (nonNull(mode)) {
            dialog.setMode(mode);
        }
        dialog.confirm();

        Predicate<WebDriver> scheduleExecuted = browser -> executionHistoryItems.size() == executionItems + 1;
        Graphene.waitGui().until(scheduleExecuted);
        return this;
    }
}
