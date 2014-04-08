package com.gooddata.qa.graphene.fragments.dashboards;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class DashboardFilter extends AbstractFragment {

    @FindBy(xpath = "//span[text()='Attribute']")
    private WebElement attributeFilter;

    @FindBy(xpath = "//div[contains(@class, 'attributes sliding')]/div[@class='input']/div/input")
    private WebElement attributeSearchInput;

    @FindBy(xpath = "//div[contains(@class, 'filter_prompts sliding')]/div[@class='input']/div/input")
    private WebElement promptSearchInput;

    @FindBy(xpath = "//div[contains(@class,'filter_prompts sliding')]")
    private WebElement lisPrompt;

    @FindBy(xpath = "//div[4]/div[9]/div/button[2]")
    private WebElement addFilterButton;

    @FindBy(xpath = "//div[7]/div/button[2]")
    private WebElement addVariableButton;

    @FindBy(xpath = "//span[text()='Variable']")
    private WebElement promptFilter;

    private String selectedAttributeLocator = "//div[contains(@class, 'attributes sliding')]/div[@class='c-AttributeFilterPicker afp-list']/div/div/div/div/div[contains(@class,'${attributeName}')]";

    private String selectedPromptLocator = "//div[contains(@class, 'filter_prompts sliding')]/div[@class='c-AttributeFilterPicker afp-list']/div/div/div/div/div[contains(@class,'${promptName}')]";

    //private String attributeFilterLocator = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'${attributeName}')]";

    @FindBy(xpath = "//div[@class='c-projectdashboard-items']/div[contains(@class,'s-active-tab')]/div/div[contains(@class,'s-date_dimension')]")
    private WebElement timeFilterLocator;

    //private String timeFilterWidgetLocator = "//div[contains(@class,'s-${timeLabel}')]";

    @FindBy(xpath = "//span[text()='Date']")
    private WebElement dateFilter;

    @FindBy(css = ".c-collectionWidget")
    private WebElement listDate;

    @FindBy(xpath = "//div[contains(@class,'yui3-c-collectionwidget-content')]/div[contains(@class,'c-label')]")
    private List<WebElement> dateFilterList;

    @FindBy(xpath = "//button[text()='next']")
    private WebElement nextButton;

    @FindBy(xpath = "//span[text()='Year']")
    private WebElement yearOption;

    private String timeLineLocator = "//div[text()='${time}']";

    @FindBy(xpath = "//div[2]/div[2]/button[2]")
    private WebElement addTimeButton;

    private String addTimeButtonLocator = "//div[2]/div[2]/button[2]";

    @FindBy(xpath = "//div[contains(@class,'yui3-toolbar-top')]/div[contains(@class,'yui3-toolbar-icon-edit')]")
    private WebElement editWidgetIcon;

    public void addListFilter(DashFilterTypes type, String name)
            throws InterruptedException {
        // DashboardFilter dashboardFilter = new DashboardFilter();
        waitForElementVisible(attributeFilter).click();
        if (type == DashFilterTypes.PROMPT) {
            waitForElementVisible(promptFilter).click();
            waitForElementVisible(lisPrompt);
            By selectedPrompt = By.xpath(selectedPromptLocator.replace(
                    "${promptName}", name.toLowerCase()));
            waitForElementVisible(promptSearchInput).sendKeys(name);
            waitForElementVisible(selectedPrompt).click();
            waitForElementVisible(addVariableButton).click();
        } else {
            By selectedAttribute = By.xpath(selectedAttributeLocator.replace(
                    "${attributeName}", "s-item-" + name.toLowerCase()));
            waitForElementVisible(attributeSearchInput).sendKeys(name);
            waitForElementVisible(selectedAttribute).click();

            waitForElementVisible(addFilterButton).click();
        }
        Thread.sleep(2000);
    }

    public void addTimeFilter(int dateDimensionIndex)
            throws InterruptedException {
        String selectedYear = "7 ago";
        waitForElementVisible(dateFilter).click();
        Thread.sleep(2000);
        if (browser.findElements(By.xpath(addTimeButtonLocator)).size() > 0) {
            waitForElementVisible(yearOption).click();
            WebElement selectYear = browser.findElement(By
                    .xpath(timeLineLocator.replace("${time}", selectedYear)));
            waitForElementVisible(selectYear).click();
            waitForElementVisible(addTimeButton).click();
        } else {
            dateFilterList.get(dateDimensionIndex).click();
            waitForElementVisible(nextButton).click();
            waitForElementVisible(yearOption).click();
            WebElement selectYear = browser.findElement(By
                    .xpath(timeLineLocator.replace("${time}", selectedYear)));
            waitForElementVisible(selectYear).click();
            waitForElementVisible(addTimeButton).click();
        }
        Thread.sleep(2000);
    }

}
