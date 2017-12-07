package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.util.Objects.nonNull;

public class SimpleMetricEditor extends AbstractFragment {

    @FindBy(className = "s-sme-fnSelect")
    private Select operationSelect;

    @FindBy(className = "s-sme-objSelect")
    private Select factSelect;

    @FindBy(className = "s-sme-global")
    private WebElement globalRadioInput;

    @FindBy(className = "s-sme-folder")
    private Select folderSelect;

    @FindBy(className = "s-sme-addButton")
    private WebElement addButton;

    public static final SimpleMetricEditor getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(SimpleMetricEditor.class,
                waitForElementVisible(By.className("c-metricSimpleEditor"), searchContext));
    }

    public void createSimpleMetric(SimpleMetricTypes type, String fact) {
        createSimpleMetric(type, fact, false, null);
    }

    public void createGlobalSimpleMetric(SimpleMetricTypes type, String fact) {
        createSimpleMetric(type, fact, true, null);
    }

    public void createGlobalSimpleMetric(SimpleMetricTypes type, String fact, String folder) {
        createSimpleMetric(type, fact, true, folder);
    }

    private void createSimpleMetric(SimpleMetricTypes type, String fact, boolean addGlobal, String folder) {
        selectOperation(type).selectFact(fact);

        if (addGlobal) {
            addToGlobalMetric();
            if (nonNull(folder)) selectFolder(folder);
        }
        add();
    }

    private SimpleMetricEditor selectOperation(SimpleMetricTypes type) {
        operationSelect.selectByVisibleText(type.name());
        return this;
    }

    private SimpleMetricEditor selectFact(String fact) {
        Predicate<WebDriver> isLoaded = browser -> factSelect.getOptions().size() > 1;
        Graphene.waitGui().until(isLoaded);

        factSelect.selectByVisibleText(fact);
        return this;
    }

    private SimpleMetricEditor addToGlobalMetric() {
        globalRadioInput.click();
        return this;
    }

    private SimpleMetricEditor selectFolder(String folder) {
        if (folderSelect.getOptions().stream().noneMatch(option -> option.getText().equals(folder))) {
            folderSelect.selectByVisibleText("Create New Folder");
            waitForElementVisible(By.className("newFolder"), getRoot()).sendKeys(folder);
            return this;
        }

        folderSelect.selectByVisibleText(folder);
        return this;
    }

    private void add() {
        addButton.click();
        waitForFragmentNotVisible(this);
    }
}
