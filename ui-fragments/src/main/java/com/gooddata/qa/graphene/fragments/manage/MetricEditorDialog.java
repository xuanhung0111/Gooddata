package com.gooddata.qa.graphene.fragments.manage;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class MetricEditorDialog extends AbstractFragment {

    @FindBy(css = "h4")
    private WebElement shareMetric;

    @FindBy(css = "div.metricTemplate.differenceTemplate")
    private WebElement differenceMetric;

    @FindBy(css = "div.metricTemplate.ratioTemplate")
    private WebElement ratioMetric;

    @FindBy(css = "div.customMetric")
    private WebElement customMetric;

    @FindBy(css = "h6")
    private WebElement elementList;

    private String selectedMetricLocator = "//ul[@class='elementList']/li[text()='${metricName}']";

    private String selectedAttrFolderLocator = "//ul[@class='elementList']/li[@class='category' and text()='${attrFolder}']";

    private String selectedAttrLocator = "//ul[@class='elementList']/li[text()='${attr}']";

    private String selectedAttrValueLocator = "//ul[@class='elementList']/li[text()='${attrValue}']";

    @FindBy(css = "input.metricName.text")
    private WebElement metricNameInput;

    @FindBy(xpath = "//form/div[@class='primary controls']/button[contains(@class,'add')]")
    private WebElement addButton;

    @FindBy(xpath = "//form/div[@class='primary controls']/button[contains(@class,'cancel')]")
    private WebElement cancelButton;

    @FindBy(linkText = "AVG")
    private WebElement customAVGMetric;

    @FindBy(css = "div.maqlReferenceGuideHelp")
    private WebElement maqlReferenceGuideHelp;

    @FindBy(linkText = "...")
    private WebElement customSelectedMetric;

    @FindBy(xpath = "//div[@class='listContainer']")
    private WebElement customElementList;

    private String selectedFactLocator = "//ul[@class='elementList']/li[text()='${fact}']";

    @FindBy(xpath = "//div[contains(@class,'MAQLEditorElementsMenu')]/div[@class='header']/button[@class='confirm']")
    private WebElement addSelectedButton;

    @FindBy(xpath = "//label[@class='metricName']/input")
    private WebElement customMetricNameInput;

    @FindBy(xpath = "//button[contains(@class,'editor')]/span[text()='Add']")
    private WebElement customAddButton;

    @FindBy(id = "p-objectPage")
    private WebElement metricDetailsPage;

    @FindBy(xpath = "//button[text()='Create Metric']")
    private WebElement createMetricButton;

    @FindBy(css = ".s-btn-edit")
    private WebElement editButton;

    @FindBy(xpath = "//a[@class='interpolateProject']")
    private WebElement dataLink;
    
    public void createShareMetric(String metricName, String usedMetric, String attrFolder, String attr) throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(By.tagName("iframe")));
	waitForElementVisible(shareMetric).click();
	waitForElementVisible(elementList);
	By selectedMetric = By.xpath(selectedMetricLocator.replace("${metricName}", usedMetric));
	waitForElementVisible(selectedMetric).click();
	waitForElementVisible(elementList);
	By selectedAttrFolder = By.xpath(selectedAttrFolderLocator.replace("${attrFolder}", attrFolder));
	waitForElementVisible(selectedAttrFolder).click();
	waitForElementVisible(elementList);
	By selectedAttr = By.xpath(selectedAttrLocator.replace("${attr}", attr));
	waitForElementVisible(selectedAttr).click();
	waitForElementVisible(metricNameInput).clear();
	metricNameInput.sendKeys(metricName);
	waitForElementVisible(addButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createDifferentMetric(String metricName, String usedMetric, String attrFolder, String attr, String attrValue) throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(By.tagName("iframe")));
	waitForElementVisible(differenceMetric).click();
	waitForElementVisible(elementList);
	By selectedMetric = By.xpath(selectedMetricLocator.replace("${metricName}", usedMetric));
	waitForElementVisible(selectedMetric).click();
	waitForElementVisible(elementList);
	By selectedAttrFolder = By.xpath(selectedAttrFolderLocator.replace("${attrFolder}", attrFolder));
	waitForElementVisible(selectedAttrFolder).click();
	waitForElementVisible(elementList);
	By selectedAttr = By.xpath(selectedAttrLocator.replace("${attr}", attr));
	waitForElementVisible(selectedAttr).click();
	waitForElementVisible(elementList);
	By selectedAttrValue = By.xpath(selectedAttrValueLocator.replace("${attrValue}", attrValue));
	waitForElementVisible(selectedAttrValue).click();
	waitForElementVisible(metricNameInput).clear();
	metricNameInput.sendKeys(metricName);
	waitForElementVisible(addButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createRatioMetric(String metricName, String usedMetric1, String usedMetric2) throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(By.tagName("iframe")));
	waitForElementVisible(ratioMetric).click();
	waitForElementVisible(elementList);
	By selectedMetric1 = By.xpath(selectedMetricLocator.replace("${metricName}", usedMetric1));
	waitForElementVisible(selectedMetric1).click();
	waitForElementVisible(elementList);
	By selectedMetric2 = By.xpath(selectedMetricLocator.replace("${metricName}", usedMetric2));
	waitForElementVisible(selectedMetric2).click();
	waitForElementVisible(metricNameInput).clear();
	metricNameInput.sendKeys(metricName);
	waitForElementVisible(addButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

    public void createCustomAVGMetric(String metricName, String fact) throws InterruptedException {
	waitForElementVisible(createMetricButton).click();
	String parentWindowHandle = browser.getWindowHandle();
	browser.switchTo().frame(waitForElementVisible(By.tagName("iframe")));
	waitForElementVisible(customMetric).click();
	waitForElementVisible(maqlReferenceGuideHelp);
	customAVGMetric.click();
	waitForElementVisible(customSelectedMetric).click();
	By selectedFact = By.xpath(selectedFactLocator.replace("${fact}", fact));
	waitForElementVisible(selectedFact).click();
	waitForElementVisible(addSelectedButton).click();
	waitForElementVisible(customMetricNameInput).sendKeys(metricName);
	waitForElementVisible(customAddButton).click();
	Thread.sleep(3000);
	browser.switchTo().window(parentWindowHandle);
	waitForElementVisible(editButton);
	waitForElementVisible(dataLink).click();
    }

}
