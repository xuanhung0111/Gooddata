package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem;
import com.gooddata.qa.graphene.utils.CheckUtils;

public class RankingFilterFragment extends AbstractFilterFragment {

    private static final By RANK_SIZE_INPUT_LOCATOR = By.cssSelector(".s-input-number");

    @FindBy(css = ".s-input-top")
    private WebElement topRadio;

    @FindBy(css = ".s-input-bottom")
    private WebElement bottomRadio;

    @FindBy(css = ".selection")
    private Select rankSizeSelect;

    @FindBy(css = ".s-btn-select_attribute")
    private WebElement selectAttributeButton;

    @FindBy(css = ".s-btn-select_metric")
    private WebElement selectMetricButton;

    @FindBy(css = "button[class*='s-btn-add_another_attribute']")
    private WebElement addAnotherAttributeButton;

    @Override
    public void addFilter(FilterItem filterItem) {
        RankingFilterItem rankingFilterItem = (RankingFilterItem) filterItem;

        selectRankingSize(rankingFilterItem)
                .selectAttributes(rankingFilterItem.getAttributes())
                .selectMetric(rankingFilterItem.getMetric())
                .apply();
        waitForFragmentNotVisible(this);
    }

    private RankingFilterFragment selectAttributes(List<String> attribute) {
        attribute.stream().forEach(this::selectAttribute);
        return this;
    }

    private RankingFilterFragment selectAttribute(String attribute) {
        Stream.of(selectAttributeButton, addAnotherAttributeButton)
                .filter(e -> !e.getAttribute("class").contains("gdc-hidden"))
                .findFirst()
                .get()
                .click();

        searchAndSelectItem(attribute);
        return this;
    }

    private RankingFilterFragment selectMetric(String metric) {
        waitForElementVisible(selectMetricButton).click();
        searchAndSelectItem(metric);
        return this;
    }

    private RankingFilterFragment selectRankingSize(final RankingFilterItem filterItem) {
        Stream.of(topRadio, bottomRadio)
                .map(CheckUtils::waitForElementVisible)
                .filter(e -> filterItem.getRanking().toString().equals(e.getAttribute("value")))
                .findFirst()
                .get()
                .click();

        if(Arrays.asList(1, 3, 5, 10).contains(filterItem.getSize())) {
            waitForElementVisible(rankSizeSelect).selectByVisibleText(String.valueOf(filterItem.getSize()));

        } else {
          waitForElementVisible(rankSizeSelect).selectByVisibleText("custom");
          WebElement rankSizeInput = waitForElementVisible(RANK_SIZE_INPUT_LOCATOR, browser);
          rankSizeInput.clear();
          rankSizeInput.sendKeys(String.valueOf(filterItem.getSize()));
        }

        return this;
    }
}
