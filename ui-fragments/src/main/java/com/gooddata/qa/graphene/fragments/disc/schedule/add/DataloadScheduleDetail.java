package com.gooddata.qa.graphene.fragments.disc.schedule.add;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Objects.nonNull;

import java.util.Collection;

import com.gooddata.qa.graphene.entity.add.IncrementalPeriod;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog.LoadMode;
import com.gooddata.qa.graphene.fragments.disc.schedule.common.AbstractScheduleDetail;
import com.google.common.base.Predicate;

public class DataloadScheduleDetail extends AbstractScheduleDetail {

    @FindBy(className = "dataset-selection")
    private DatasetUploadSection datasetUploadSection;

    public static final DataloadScheduleDetail getInstance(SearchContext searchContext) {
        return getInstance(searchContext, DataloadScheduleDetail.class);
    }

    public RunOneOffDialog triggerRunOneOffDialog() {
        waitForElementVisible(runButton).click();
        return RunOneOffDialog.getInstance(browser);
    }

    public DataloadScheduleDetail executeSchedule(LoadMode mode, SyncDatasets syncDatasets) {
        return executeSchedule(mode, null, syncDatasets);
    }

    public DataloadScheduleDetail executeSchedule(IncrementalPeriod period) {
        return executeSchedule(LoadMode.INCREMENTAL, period, null);
    }

    public DataloadScheduleDetail executeSchedule(LoadMode mode) {
        return executeSchedule(mode, null);
    }

    public DataloadScheduleDetail executeSchedule() {
        return executeSchedule(null, null);
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

    public String getNonExistingDatasetsMessage() {
        return waitForElementVisible(By.cssSelector(".dataset-nonexisting .message"), getRoot()).getText();
    }

    public DataloadScheduleDetail removeNonExistingDatasets() {
        waitForElementVisible(By.cssSelector(".remove-dataset-button button"), getRoot()).click();
        return this;
    }

    private DataloadScheduleDetail executeSchedule(LoadMode mode, IncrementalPeriod period, SyncDatasets syncDatasets) {
        int executionItems = executionHistoryItems.size();

        RunOneOffDialog dialog = triggerRunOneOffDialog();
        if (nonNull(mode)) {
            dialog.setMode(mode);
        }

        if (nonNull(period)) {
            if (nonNull(period.getFrom())) {
                dialog.setIncrementalStartTime(period.getFrom());
            }
            if (nonNull(period.getTo())) {
                dialog.setIncrementalEndTime(period.getTo());
            }
        }

        if (nonNull(syncDatasets)) {
            DatasetDropdown dropdown = dialog.getDatasetDropdown().expand();
            if (nonNull(syncDatasets.getDatasets())) {
                dropdown.selectDatasets(syncDatasets.getDatasets());
            } else {
                dropdown.selectAllDatasets();
            }
            dropdown.submit();
        }
        dialog.confirm();

        Predicate<WebDriver> scheduleExecuted = browser -> executionHistoryItems.size() == executionItems + 1;
        Graphene.waitGui().until(scheduleExecuted);
        return this;
    }
}
