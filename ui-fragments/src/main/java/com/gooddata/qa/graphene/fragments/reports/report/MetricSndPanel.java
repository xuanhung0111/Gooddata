package com.gooddata.qa.graphene.fragments.reports.report;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;

import com.gooddata.qa.graphene.entity.report.WhatItem;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Collection;
import java.util.List;

public class MetricSndPanel extends AbstractSndPanel {

    @FindBy(className = "s-btn-add_new_metric")
    private WebElement createNewMetricButton;

    @FindBy(className = "s-btn-_advanced_")
    private WebElement createAdvanceMetricButton;

    @Override
    protected WebElement getViewGroupsContainerRoot() {
        return getRoot().findElement(className("s-snd-MetricFoldersContainer"));
    }

    @Override
    protected WebElement getItemContainerRoot() {
        return getRoot().findElement(className("s-snd-MetricsContainer")).findElement(BY_PARENT);
    }

    @Override
    protected WebElement getItemDetailContainerRoot() {
        return getRoot().findElement(className("s-snd-metricDetail"));
    }

    public SimpleMetricEditor clickAddNewMetric() {
        createNewMetricButton.click();
        return SimpleMetricEditor.getInstance(browser);
    }

    public MetricEditorDialog clickAddAdvanceMetric() {
        createAdvanceMetricButton.click();
        return MetricEditorDialog.getInstance(browser);
    }

    public MetricSndPanel selectMetrics(Collection<WhatItem> whats) {
        whats.forEach(what -> {
            selectItem(what.getMetric());

            if (nonNull(what.getDrillStep())) {
                openMetricDetail(what.getMetric()).addDrillStep(what.getDrillStep());
            }
        });
        return this;
    }

    public MetricSndPanel openMetricDetail(String metric) {
        findItemElement(metric).click();
        waitForElementVisible(getItemDetailContainerRoot());
        return this;
    }

    public List<String> getSelectedMetrics() {
        return getItemContainerRoot().findElements(className("sndInReport"))
                .stream()
                .map(WebElement::getText)
                .collect(toList());
    }

    public MetricSndPanel addDrillStep(String toAttribute) {
        getItemDetailContainerRoot().findElement(className("s-btn-add_drill_step")).click();
        SelectItemPopupPanel.getInstance(browser).searchAndSelectItem(toAttribute).submitPanel();
        return this;
    }

    public MetricEditorDialog editAdvanceMetric() {
        getItemDetailContainerRoot().findElement(className("c-metricDetailEditButton")).click();
        return MetricEditorDialog.getInstance(browser);
    }

    public boolean isEditable() {
        return isElementVisible(className("is-editable"),getItemContainerRoot());
    }
}
