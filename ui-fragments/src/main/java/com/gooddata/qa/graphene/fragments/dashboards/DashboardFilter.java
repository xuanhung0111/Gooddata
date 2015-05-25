package com.gooddata.qa.graphene.fragments.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.CssUtils;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardFilter extends AbstractFragment {

    @FindBy(xpath = "//div[contains(@class, 'attributes sliding')]/div[@class='input']/div/input")
    private WebElement attributeSearchInput;

    @FindBy(xpath = "//div[contains(@class, 'filter_prompts sliding')]/div[@class='input']/div/input")
    private WebElement promptSearchInput;

    @FindBy(xpath = "//div[contains(@class,'filter_prompts sliding')]")
    private WebElement lisPrompt;

    @FindBy(xpath = "//div[contains(@class,'c-mdObjectsPicker') and not (contains(@class,'gdc-hidden'))]//button[text()='Add']")
    private WebElement addFilterButton;

    @FindBy(xpath = "//span[text()='Variable']")
    private WebElement promptFilter;

    private String selectedAttributeLocator =
            "div.attributes.sliding div.${attributeName}.s-enabled:not(.gdc-hidden):not(.hidden)";

    private String selectedPromptLocator =
            ".filter_prompts .s-item-${promptName}:not(.gdc-hidden):not(.hidden)";

    private String addButton =
            "//div[contains(@class,'yui3-c-tabtimefiltereditor')]//button[text()='Add']";

    @FindBy(xpath = "//div[contains(@class,'yui3-c-collectionwidget-content')]/div[contains(@class,'c-label')]/span")
    private List<WebElement> dateFilterList;

    @FindBy(xpath = "//button[text()='next']")
    private WebElement nextButton;

    @FindBy(xpath = "//span[text()='Year']")
    private WebElement yearOption;

    private String timeLineLocator = "//div[text()='${time}']";

    @FindBy(xpath = "//div[contains(@class,'yui3-c-tabtimefiltereditor')]//button[text()='Add']")
    private WebElement addTimeButton;

    public void addListFilter(DashFilterTypes type, String name) throws InterruptedException {
        if (type == DashFilterTypes.PROMPT) {
            waitForElementVisible(promptFilter).click();
            waitForElementVisible(lisPrompt);
            By selectedPrompt = By.cssSelector(selectedPromptLocator.replace("${promptName}", CssUtils.simplifyText(name)));
            waitForElementVisible(promptSearchInput).sendKeys(name);
            waitForElementVisible(selectedPrompt, browser).click();
        } else {
            By attributeToAddLocator = By.cssSelector(selectedAttributeLocator.replace(
                    "${attributeName}", "s-item-" + CssUtils.simplifyText(name)));
            waitForElementVisible(attributeSearchInput).sendKeys(name);
            waitForElementVisible(attributeToAddLocator, browser).click();
        }
        waitForElementVisible(addFilterButton).click();
        Thread.sleep(2000);
    }

    public void addTimeFilter(int dateDimensionIndex, String dataRange) throws InterruptedException {
        if (browser.findElements(By.xpath(addButton)).size() > 0 && addTimeButton.isDisplayed()) {
            waitForElementVisible(yearOption).click();
            WebElement selectYear =
                    browser.findElement(By.xpath(timeLineLocator.replace("${time}", dataRange)));
            waitForElementVisible(selectYear).click();
            waitForElementVisible(addTimeButton).click();
        } else {
            waitForElementVisible(dateFilterList.get(dateDimensionIndex)).click();
            waitForElementVisible(nextButton).click();
            waitForElementVisible(yearOption).click();
            WebElement selectYear =
                    browser.findElement(By.xpath(timeLineLocator.replace("${time}", dataRange)));
            waitForElementVisible(selectYear).click();
            waitForElementVisible(addTimeButton).click();
        }
        Thread.sleep(2000);
    }

}
