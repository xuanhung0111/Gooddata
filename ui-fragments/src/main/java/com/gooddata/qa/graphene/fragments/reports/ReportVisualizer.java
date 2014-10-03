package com.gooddata.qa.graphene.fragments.reports;

import com.gooddata.qa.graphene.entity.Attribute;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.entity.HowItem;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;

public class ReportVisualizer extends AbstractFragment {

    private static final By BY_WHAT_AREA_METRICS_HEADER = By.xpath("//div[contains(@class, 'sndPanel1')]//div[@title='All Metrics']");
    private static final By BY_HOW_AREA_ATTRIBUTES_HEADER = By.xpath("//div[contains(@class, 'sndPanel1')]//div[@title='All Attributes']");
    private static final By BY_FILTER_TAB = By.id("filterTab");

    private static final String XPATH_REPORT_VISUALIZATION_TYPE = "//div[contains(@class, 's-enabled')]/div[contains(@class, 'c-chartType') and ./span[@title='${type}']]";

    private static final String XPATH_METRIC_CHECKBOX = "//div[contains(@class, 'sndMetric')]/span[text()='${metric}']/input[@type='checkbox']";
    private static final String XPATH_METRIC_CHECKBOX_CHECKED = "//div[contains(@class, 'sndMetric')]/span[text()='${metric}']/input[@type='checkbox' and @checked='checked']";

    private static final String XPATH_ATTRIBUTE_CHECKBOX = "//div[contains(@class, 's-snd-AttributesContainer')]//div[contains(@class, 'element') and contains(@title, '${attribute}')]//input[@type='checkbox']";
    private static final String XPATH_ATTRIBUTE_CHECKBOX_CHECKED = "//div[contains(@class, 'AttributesContainer')]//div[contains(@class, 'element') and contains(@title, '${attribute}')]//input[@type='checkbox' and @checked='checked']";
    private static final String XPATH_ATTRIBUTE_POSITION = "//div[contains(@class, 's-snd-AttributesContainer')]//div[contains(@class, 'element') and contains(@title, '${attribute}')]//div[contains(@title, 'Toggle attribute')]";
    private static final String XPATH_ATTRIBUTE_POSITION_TOGGLED = "//div[contains(@class, 's-snd-AttributesContainer')]//div[contains(@class, 'element') and contains(@title, '${attribute}')]//div[contains(@title, 'Toggle attribute') and contains(@class, '${position}')]";
    private static final String XPATH_SND_FOLDER = "//div[@title='${SnDFolderName}']";

    private static final String ATTRIBUTE_LEFT_CLASS = "sndAttributePosition_rows";
    private static final String ATTRIBUTE_TOP_CLASS = "sndAttributePosition_columns";

    @FindBy(xpath = "//div[contains(@class, 'reportEditorWhatArea')]/button")
    private WebElement whatButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorHowArea')]/button")
    private WebElement howButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button")
    private WebElement filterButton;

    @FindBy(xpath = "//form[@class='sndFooterForm']/button[text()='Done']")
    private WebElement doneButton;

    @FindBy(xpath = "//label[@class='sndMetricFilterLabel']/../input")
    private WebElement metricFilterInput;

    @FindBy(xpath = "//label[@class='sndAttributeFilterLabel']/../input")
    private WebElement attributeFilterInput;

    @FindBy
    private WebElement reportVisualizationContainer;

    @FindBy(xpath = "//button[contains(@class,'sndCreateMetric')]")
    private WebElement createMetricButton;

    @FindBy(xpath = "//select[contains(@class,'s-sme-fnSelect')]")
    private Select metricOperationSelect;

    @FindBy(xpath = "//select[contains(@class,'s-sme-objSelect')]")
    private Select performOperationSelect;

    @FindBy(xpath = "//input[contains(@class,'s-sme-global')]")
    private WebElement addToGlobalInput;

    @FindBy(xpath = "//input[contains(@class,'s-sme-title')]")
    private WebElement metricTitleInput;

    @FindBy(xpath = "//button[contains(@class,'s-sme-addButton')]")
    private WebElement addMetricButton;

    @FindBy(xpath="//input[contains(@class,'newFolder')]") 
    private WebElement snDFolderNameInput;

    @FindBy(xpath="//select[contains(@class,'s-sme-folder')]") 
    private Select folderOption;

    private String selectedFactLocator ="//select[contains(@class,'s-sme-objSelect')]/option[text()='${factName}']";

    public void selectWhatArea(List<String> what) throws InterruptedException {
        waitForElementVisible(whatButton).click();
        waitForElementVisible(BY_WHAT_AREA_METRICS_HEADER, browser);
        for (String metric : what) {
            waitForElementVisible(metricFilterInput).clear();
            metricFilterInput.sendKeys(metric);
            By metricCheckbox = By.xpath(XPATH_METRIC_CHECKBOX.replace("${metric}", metric));
            waitForElementVisible(metricCheckbox, browser);
            Thread.sleep(2000);
            root.findElement(metricCheckbox).click();
            waitForElementVisible(By.xpath(XPATH_METRIC_CHECKBOX_CHECKED.replace("${metric}", metric)), browser);
        }
    }

    public void selectHowAreaWithPosition(List<HowItem> how) throws InterruptedException {
        waitForElementVisible(howButton).click();
        waitForElementVisible(BY_HOW_AREA_ATTRIBUTES_HEADER, browser);
        if (how != null) {
            for (HowItem howItem : how) {
                selectAttributeWithPosition(howItem.getAttribute(), howItem.getPosition());
            }
        }
    }

    private void selectAttributeWithPosition(Attribute attribute, HowItem.Position position) throws InterruptedException {
        selectAttribute(attribute.getName());
        WebElement attributePositionElement = root.findElement(By.xpath(XPATH_ATTRIBUTE_POSITION.replace("${attribute}", attribute.getName())));
        String attributeClass =  attributePositionElement.getAttribute("class");

        if (position == HowItem.Position.LEFT && !attributeClass.contains(ATTRIBUTE_LEFT_CLASS)) {
            attributePositionElement.click();
            waitForElementVisible(By.xpath(XPATH_ATTRIBUTE_POSITION_TOGGLED.replace("${attribute}",
                    attribute.getName()).replace("${position}", ATTRIBUTE_LEFT_CLASS)), browser);
        } else if (position == HowItem.Position.TOP && !attributeClass.contains(ATTRIBUTE_TOP_CLASS)) {
            attributePositionElement.click();
            waitForElementVisible(By.xpath(XPATH_ATTRIBUTE_POSITION_TOGGLED.replace("${attribute}",
                    attribute.getName()).replace("${position}", ATTRIBUTE_TOP_CLASS)), browser);
        }
    }

    private void selectAttribute(String attribute) throws InterruptedException {
        waitForElementVisible(attributeFilterInput).clear();
        attributeFilterInput.sendKeys(attribute);
        By attributeCheckbox = By.xpath(XPATH_ATTRIBUTE_CHECKBOX.replace("${attribute}", attribute));
        waitForElementVisible(attributeCheckbox, browser);
        Thread.sleep(2000);
        root.findElement(attributeCheckbox).click();
        waitForElementVisible(By.xpath(XPATH_ATTRIBUTE_CHECKBOX_CHECKED.replace("${attribute}", attribute)), browser);
    }

    public void addSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact, String metricName, boolean addToGlobal) {
        waitForElementVisible(whatButton).click();
        waitForElementVisible(BY_WHAT_AREA_METRICS_HEADER, browser);

        waitForElementVisible(createMetricButton).click();
        waitForElementVisible(metricOperationSelect).selectByVisibleText(metricOperation.name());
        waitForElementVisible(performOperationSelect).selectByVisibleText(metricOnFact);

        if (metricName!=null) {
            waitForElementVisible(metricTitleInput).clear();
            metricTitleInput.sendKeys(metricName);
        }
        if (addToGlobal) waitForElementVisible(addToGlobalInput).click();
        waitForElementVisible(addMetricButton).click();
    }

    public void finishReportChanges() {
        waitForElementVisible(doneButton).click();
        waitForElementNotVisible(doneButton);
    }

    public void selectReportVisualisation(ReportTypes reportVisualizationType) {
        By icon = By.xpath(XPATH_REPORT_VISUALIZATION_TYPE.replace("${type}", reportVisualizationType.getName()));
        waitForElementVisible(icon, browser);
        reportVisualizationContainer.findElement(icon).click();
        waitForElementVisible(By.id(reportVisualizationType.getContainerTabId()), browser);
    }
    
    public void createSnDFolder(String metricOnFact, String folderName)
            throws InterruptedException {
        waitForElementVisible(whatButton).click();
        waitForElementVisible(createMetricButton).click();
        By selectedFactOption = By.xpath(selectedFactLocator.replace(
                "${factName}", metricOnFact));
        waitForElementVisible(selectedFactOption, browser);
        waitForElementVisible(performOperationSelect).selectByVisibleText(
                metricOnFact);
        waitForElementVisible(addToGlobalInput).click();
        waitForElementVisible(folderOption).selectByVisibleText(
                "Create New Folder");
        waitForElementVisible(snDFolderNameInput).sendKeys(folderName);
        waitForElementVisible(addMetricButton).click();
        By snDFolder = By.xpath(XPATH_SND_FOLDER.replace("${SnDFolderName}",
                folderName));
        waitForElementVisible(snDFolder, browser);
    }
}