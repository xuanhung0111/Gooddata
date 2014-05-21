package com.gooddata.qa.graphene.fragments.manage;

import static org.testng.Assert.*;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;

public class FactDetailPage extends AbstractFragment {
    @FindBy(xpath = "//table[@class = 'factAggregations']")
    private WebElement factAggregationTable;

    @FindBy(id = "objectUsedInContainer")
    private WebElement objectUsedInContainer;

    @FindBy(xpath = "//div[@id = 'objectUsedInContainer']/div[@class = 'mezzo']/a")
    private List<WebElement> usedInMetricList;

    @FindBy(xpath = "//div[contains(@class,'MAQLDocumentationContainer')]/a")
    private WebElement factLink;

    @FindBy(xpath = "//button[contains(@class, 's-btn-change_folder')]")
    private WebElement changeFolderButton;

    @FindBy(xpath = "//span[contains(@class,'loadingWheel') and not(contains(@class,'hidden'))]")
    private WebElement loadingWheelFolder;

    @FindBy(xpath = "//p[@class = 'folderText']/a")
    private WebElement locatedInFolder;

    @FindBy(css = ".s-name")
    private WebElement factNameIpe;

    @FindBy(xpath = "//div[contains(@class,'s-name-ipe-editor')]//input[@class = 'ipeEditor']")
    private WebElement factNameInput;

    @FindBy(xpath = "//div[contains(@class,'s-name-ipe-editor')]//button[text() = 'Save']")
    private WebElement factNameSave;

    @FindBy(css = ".s-description-ipe-placeholder")
    private WebElement descriptionIpePlaceholder;

    @FindBy(css = ".s-description")
    private WebElement descriptionIpe;

    @FindBy(xpath = "//div[contains(@class,'s-description-ipe-editor')]//input[@class = 'ipeEditor']")
    private WebElement descriptionInput;

    @FindBy(xpath = "//div[contains(@class,'s-description-ipe-editor')]//button[text() = 'Save']")
    private WebElement descriptionSave;

    @FindBy(xpath = "//span[text() = 'Add Tags']")
    private WebElement addTagButton;

    @FindBy(xpath = "//div[contains(@class,'s-btn-ipe-editor')]//input[@class = 'ipeEditor']")
    private WebElement tagInput;

    @FindBy(xpath = "//div[contains(@class,'s-btn-ipe-editor')]//button[text() = 'Add']")
    private WebElement tagAddButton;

    @FindBy(xpath = "//div[@class = 'tag']")
    private List<WebElement> tagList;

    private final String folderLocator = "//div[@class = 'autocompletion']/div[@class = 'suggestions']/ul/li[text() = '${folder}']";
    private final String metricButtonLocator = "//td[@class='fceName' and text()='${metricType}']/../td[@class='fceList']/button";
    private final String createdMetricLocator = "//td[@class='fceName' and text()='${metricType}']/../td[@class='fceList']/a";
    private final String metricLinkLocator = "//div[@id = 'objectUsedInContainer']/a[text() = '${metricName}']";

    @FindBy(id = "p-objectPage")
    private MetricDetailsPage metricDetailPage;

    public void createSimpleMetric(SimpleMetricTypes metricType, String factName) {
	waitForObjectPageLoaded();
	String operation = metricType.getLabel();
	By metricButton = By.xpath(metricButtonLocator.replace("${metricType}",
		operation));
	waitForElementVisible(metricButton).click();
	waitForElementNotPresent(metricButton);
	String operationInElement = operation.substring(0, 1).toUpperCase()
		+ operation.substring(1).toLowerCase();
	String expectedMetricName = String.format("%s [%s]", factName,
		operationInElement);
	By createdMetric = By.xpath(createdMetricLocator.replace(
		"${metricType}", operation));
	waitForElementVisible(createdMetric);
	assertEquals(browser.findElement(createdMetric).getText(),
		expectedMetricName, "Metric is not created properly");
	verifyUsedInMetric(metricType, factName, expectedMetricName);
	waitForElementVisible(factLink).click();
    }

    public void verifyUsedInMetric(SimpleMetricTypes metricType,
	    String factName, String metricName) {
	waitForElementVisible(objectUsedInContainer);
	for (WebElement elem : usedInMetricList) {
	    assertTrue(elem.getText() == metricName,
		    "Metric is not created properly and not listed on Used in Metrics");
	}
	By metricLink = By.xpath(metricLinkLocator.replace("${metricName}",
		metricName));
	waitForElementVisible(metricLink).click();
	String expectedMaql = String.format("SELECT %s(%s)", metricType,
		factName);
	String expectedFormat = "#,##0.00";
	metricDetailPage.checkCreatedMetric(metricName, expectedMaql,
		expectedFormat);
    }

    public void changeFactFolder(String newFolderName)
	    throws InterruptedException {
	waitForObjectPageLoaded();
	waitForElementVisible(factAggregationTable);
	waitForElementVisible(changeFolderButton).click();
	By factFolder = By.xpath(folderLocator.replace("${folder}",
		newFolderName));
	waitForElementVisible(factFolder).click();
	waitForElementVisible(loadingWheelFolder);
	waitForElementNotPresent(loadingWheelFolder);
	assertEquals(locatedInFolder.getText(), newFolderName,
		"Change folder for Fact doesn't work properly");
    }

    public String changeFactName(String newFactName) {
	waitForObjectPageLoaded();
	waitForElementVisible(factAggregationTable);
	waitForElementVisible(factNameIpe).click();
	waitForElementVisible(factNameInput).clear();
	factNameInput.sendKeys(newFactName);
	waitForElementVisible(factNameSave).click();
	waitForElementNotVisible(factNameInput);
	assertEquals(factNameIpe.getText(), newFactName,
		"Change name for Fact doesn't work properly");
	return newFactName;
    }

    public void addDescription(String description) {
	waitForObjectPageLoaded();
	waitForElementVisible(factAggregationTable);
	waitForElementVisible(descriptionIpePlaceholder).click();
	waitForElementVisible(descriptionInput).sendKeys(description);
	waitForElementVisible(descriptionSave).click();
	waitForElementNotVisible(descriptionInput);
	assertEquals(descriptionIpe.getText(), description,
		"Change name for Fact doesn't work properly");
    }

    public void addTag(String tagName) throws InterruptedException {
	waitForObjectPageLoaded();
	waitForElementVisible(factAggregationTable);
	int tagCountBefore = tagList.size();
	waitForElementVisible(addTagButton).click();
	waitForElementVisible(tagInput).sendKeys(tagName);
	waitForElementVisible(tagAddButton).click();
	waitForElementNotVisible(tagInput);
	int tagWords = 1;
	for (int i = 0; i < tagName.trim().length(); i++) {
	    if (tagName.charAt(i) == ' ' && tagName.charAt(i + 1) != ' ') {
		tagWords++;
	    }
	}
	int tagCountAfter = tagList.size();
	assertEquals(tagCountAfter, tagCountBefore + tagWords,
		"Add tag for Fact doesn't work properly");
	String[] tagNameList = tagName.split("\\s+");
	boolean tagVisible = false;
	int matchingTag = 0;
	for (int i = 0; i < tagNameList.length; i++) {
	    for (WebElement elem : tagList) {
		if (waitForElementVisible(elem).getAttribute("title")
			.equalsIgnoreCase(tagNameList[i])) {
		    matchingTag++;
		}
	    }
	    if (matchingTag == tagNameList.length) {
		tagVisible = true;
	    }
	}
	assertTrue(tagVisible, "Add tag for Fact doesn't work properly");
    }
}
