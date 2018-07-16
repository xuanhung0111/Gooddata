package com.gooddata.qa.graphene.fragments.manage;

import static com.gooddata.qa.graphene.entity.metric.CustomMetricUI.extractAttributeValue;
import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static java.lang.String.format;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import com.gooddata.qa.graphene.entity.metric.CustomMetricUI;
import com.gooddata.qa.graphene.enums.metrics.MetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.utils.Sleeper;

public class MetricEditorDialog extends AbstractFragment {

    @FindBy(css = ".metricEditor > div:not([style^='display: none']) input.text.metricName")
    private WebElement metricInput;

    @FindBy(css = "div.maql-editor")
    private WebElement maqlEditor;

    @FindBy(css = ".primary.controls button.add:not([style^='display: none'])")
    private WebElement addButton;

    @FindBy(xpath =
            "//div[contains(@class,'MAQLEditorElementsMenu')]/div[@class='header']/button[@class='confirm']")
    private WebElement addSelectedButton;

    @FindBy(xpath = "//div[@class='listContainer']/div/ul[@class='elementList']/li[@class='category']")
    private WebElement customCategoryList;
    
    @FindBy(css = ".elementList .c-label:not(.gdc-hidden) span")
    private List<WebElement> attributeElelements;

    @FindBy(css = ".question input.savePermanently")
    private WebElement addToGlobalCheckbox;
    
    @FindBy(css = QUESTION_ACTIVE_LOCATOR + " select.folder")
    private Select folderDropdown;
    
    @FindBy(css = QUESTION_ACTIVE_LOCATOR + " .newFolder")
    private WebElement newFolderField;

    public static final By IFRAME = By.className("metricEditorFrame");
    public static final By LOCATOR = By.className("s-metricEditor");

    private static final String METRIC_LINK_LOCATOR = "${metricType}";
    private static final String METRIC_TEMPLATE_TAB_LOCATOR = "//ul[@role='tablist']//em[text()='${tab}']";
    private static final String SELECTION_LOCATOR = "//a[contains(@class,'%s') and text()='...']";
    private static final String QUESTION_ACTIVE_LOCATOR = ".question.active";
    private static final String ELEMENT_VALUES_LOCATOR = ".es_body:not(.gdc-hidden)";
    private static final String ELEMENT_HEADERS_LOCATOR = ".es_head:not(.gdc-hidden)";

    private static final String CREATE_NEW_FOLDER = "Create New Folder";

    private static final By ATTRIBUTE_SELECTION_LOCATOR = By.xpath(format(SELECTION_LOCATOR, "attributes"));
    private static final By ATTRIBUTE_ELEMENT_SELECTION_LOCATOR =
            By.xpath(format(SELECTION_LOCATOR, "attributeElements"));
    private static final By FACT_SELECTION_LOCATOR = By.xpath(format(SELECTION_LOCATOR, "facts"));
    private static final By METRIC_SELECTION_LOCATOR = By.xpath(format(SELECTION_LOCATOR, "metrics"));

    private static final By BY_SAVE_BUTTON = By.cssSelector("button.save:not([style^='display: none'])");
    private static final By BY_BACK_BUTTON = By.cssSelector(".button.back:not([style^='display: none'])");
    private static final By BY_CANCEL_BUTTON = By.cssSelector(".button.cancel:not([style^='display: none'])");
    private static final By SEARCH_FIELD_LOCATOR = By.className("s-afp-input");

    public static MetricEditorDialog getInstance(WebDriver driver) {
        driver.switchTo().frame(waitForElementVisible(IFRAME, driver));

        return Graphene.createPageFragment(MetricEditorDialog.class, waitForElementVisible(LOCATOR, driver));
    }

    public MetricEditorDialog clickShareMetricLink() {
        waitForElementVisible(By.cssSelector("div.shareTemplate"), browser).click();
        return this;
    }

    public void clickDifferentMetricLink() {
        waitForElementVisible(By.cssSelector("div.differenceTemplate"), browser).click();
    }

    public void clickRatioMetricLink() {
        waitForElementVisible(By.cssSelector("div.ratioTemplate"), browser).click();
    }

    public MetricEditorDialog clickCustomMetricLink() {
        waitForElementVisible(By.cssSelector("div.customMetric"), browser).click();
        return this;
    }

    public void createShareMetric(String metricName, String usedMetric, String attr) {
        configureShareMetric(metricName, usedMetric, attr).submit();
    }

    public MetricEditorDialog configureShareMetric(String metricName, String usedMetric, String attr) {
        clickShareMetricLink();
        selectElement(usedMetric);
        selectElement(attr);
        enterMetricName(metricName);

        return this;
    }

    public MetricEditorDialog addToGlobalMetrics() {
        if (!waitForElementVisible(addToGlobalCheckbox).isSelected())
            addToGlobalCheckbox.click();
        return this;
    }

    public MetricEditorDialog addNewFolder(String newFolder) {
        waitForElementVisible(folderDropdown).selectByVisibleText(CREATE_NEW_FOLDER);
        waitForElementVisible(newFolderField).sendKeys(newFolder);

        return this;
    }

    public boolean isEmptyMessagePresent() {
        waitForElementsLoading();
        return isElementPresent(By.className("emptyMessage"), browser);
    }

    public String getNoDataMessage() {
        return waitForElementVisible(By.className("nodata"), browser).getText();
    }

    public void createDifferentMetric(String metricName, String usedMetric, String attr, String attrValue) {
        clickDifferentMetricLink();
        selectElement(usedMetric);
        selectElement(attr);
        selectElement(attrValue);
        enterMetricNameAndSubmit(metricName);
    }

    public void createRatioMetric(String metricName, String usedMetric1, String usedMetric2) {
        clickRatioMetricLink();
        selectElement(usedMetric1);
        selectElement(usedMetric2);
        enterMetricNameAndSubmit(metricName);
    }

    public void createAggregationMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        if (metricType == MetricTypes.COUNT) {
            selectAttributes(metricUI);
        } else if (metricType == MetricTypes.FORECAST) {
            selectMetrics(metricUI);
        } else {
            selectFacts(metricUI);
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createNumericMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createGranularityMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        if (metricType != MetricTypes.BY_ALL) {
            selectAttributes(metricUI);
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createLogicalMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        if (metricType != MetricTypes.CASE && metricType != MetricTypes.IF) {
            selectAttributes(metricUI);
            selectAttrElements(metricUI);
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public void createFilterMetric(MetricTypes metricType, CustomMetricUI metricUI) {
        createTemplateMetricTab(metricType);
        selectMetrics(metricUI);
        selectAttributes(metricUI);
        switch (metricType) {
            case EQUAL:
            case DOES_NOT_EQUAL:
            case GREATER:
            case GREATER_OR_EQUAL:
            case LESS:
            case LESS_OR_EQUAL:
            case IN:
            case NOT_IN:
                selectAttrElements(metricUI);
                break;
            default:
                break;
        }
        enterMetricNameAndSubmit(metricUI.getName());
    }

    public MetricEditorDialog enterMetricName(String name) {
        waitForElementVisible(metricInput).clear();
        metricInput.sendKeys(name);

        return this;
    }

    public void save() {
        waitForElementVisible(BY_SAVE_BUTTON, getRoot()).click();
        try {
            waitForFragmentNotVisible(this);

        // According to https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Errors/Dead_object,
        // any closed iframe will become DEAD_OBJECT and trying to access these will make WebDriver
        // thrown WebDriverException. In this case, we should ignore and consider the iframe is closed completely.
        } catch (WebDriverException e) {
            log.info("Metric editor saved and closed");
        }
    }

    public void back() {
        waitForElementVisible(BY_BACK_BUTTON, getRoot()).click();
    }

    public void cancel() {
        waitForElementVisible(BY_CANCEL_BUTTON, getRoot()).click();
    }

    public MetricEditorDialog selectElementType(ElementType type) {
        waitForElementVisible(By.cssSelector(format(".elementList li[title='%s']", type.toString())), browser)
                .click();

        return this;
    }

    public MetricEditorDialog search(String value) {
        waitForElementVisible(SEARCH_FIELD_LOCATOR, getRoot()).sendKeys(value);
        waitForElementsLoading();
        return this;
    }

    public boolean isSearchFieldPresent() {
        return isElementPresent(SEARCH_FIELD_LOCATOR, getRoot());
    }

    public String getElementListTitle() {
        return waitForElementVisible(By.cssSelector(".leftArrow.back+h6"), browser).getText();
    }

    public List<String> getElementValues() {
        waitForElementsLoading();
        return getElementTexts(By.cssSelector(ELEMENT_VALUES_LOCATOR), getRoot());
    }

    public List<String> getPrivateElementValues() {
        waitForElementsLoading();
        return getElementTexts(By.cssSelector(ELEMENT_VALUES_LOCATOR + ".is-unlisted"), getRoot());
    }

    public MetricEditorDialog selectElement(String element) {
        SelectItemPopupPanel
                .getInstance(By.cssSelector(".s-metricEditor > [style='display: block;'] .listContainer"), browser)
                .searchAndSelectItem(element);

        return this;
    }

    public MetricEditorDialog addAttributeLabelToEditor(String selectedElement) {
        // there is no better option for now, be careful when using att has many labels
        Sleeper.sleepTightInSeconds(2);

        List<WebElement> items =
                waitForCollectionIsNotEmpty(browser.findElements(By.cssSelector(".leftArrow+h6+.elementList li")));

        WebElement selectedItem = waitForElementVisible(
                items.stream().filter(e -> selectedElement.equals(e.getAttribute("title"))).findFirst().get());

        selectedItem.click();

        if (!selectedItem.getAttribute("class").contains("selected"))
            throw new RuntimeException("Can't select " + selectedElement);

        waitForElementVisible(addSelectedButton).click();
        return this;
    }

    public List<String> getSelectedValues() {
        waitForElementsLoading();
        return getElementTexts(
                getRoot().findElements(By.cssSelector(ELEMENT_VALUES_LOCATOR)).stream()
                    .filter(e -> e.getAttribute("class").contains("selected"))
                    .collect(Collectors.toList()));
    }

    public String getHeaderTextColor(String header) {
        return waitForElementsLoading().getElementHeader(header).getCssValue("color");
    }

    public MetricEditorDialog hoverOnHeader(String header) {
        waitForElementsLoading().getActions().moveToElement(getElementHeader(header)).perform();

        if (isHeaderHovered(header))
            return this;

        throw new RuntimeException("Can't hover on " + header);
    }

    public MetricEditorDialog waitForElementsLoading() {
        // because .loaded is always displayed when the element finishes loading
        // we need a short break to ensure that the state is actually changed, then start waiting
        Sleeper.sleepTightInSeconds(1);

        Function<WebDriver, Boolean> waitForLoadedState = browser -> isElementPresent(By.className("loaded"),
                waitForElementVisible(By.className("yui3-c-simplecolumn-content"), browser));
        Graphene.waitGui().until(waitForLoadedState);
        return this;
    }

    public void submit() {
        waitForElementVisible(addButton).click();
    }

    public MetricEditorDialog selectCodeMirrorWidget(String displayedText) {

        WebElement codeMirrorEditor = waitForElementVisible(By.className("CodeMirror"), browser);
        codeMirrorEditor.click();

        if (!codeMirrorEditor.getAttribute("class").contains("focused"))
            throw new RuntimeException("Can't focus on code mirror editor");

        waitForElementVisible(getCodeMirrorWidget(displayedText)).click();
        return this;
    }

    private void selectAttributes(CustomMetricUI metricUI) {
        for (String attribute : metricUI.getAttributes()) {
            waitForElementVisible(ATTRIBUTE_SELECTION_LOCATOR, browser)
                .click();
            selectElement(attribute);
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectAttrElements(CustomMetricUI metricUI) {
        Pair<String, String> attributeValueInfo;
        for (String value : metricUI.getAttributeValues()) {
            attributeValueInfo = extractAttributeValue(value);
            waitForElementVisible(ATTRIBUTE_ELEMENT_SELECTION_LOCATOR, browser)
                .click();
            selectElement(attributeValueInfo.getLeft());
            selectElement(attributeValueInfo.getRight());
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectFacts(CustomMetricUI metricUI) {
        for (String fact : metricUI.getFacts()) {
            waitForElementVisible(FACT_SELECTION_LOCATOR, browser).click();
            selectElement(fact);
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void selectMetrics(CustomMetricUI metricUI) {
        for (String metric : metricUI.getMetrics()) {
            waitForElementVisible(METRIC_SELECTION_LOCATOR, browser).click();
            selectElement(metric);
            waitForElementVisible(addSelectedButton).click();
            waitForElementVisible(customCategoryList);
        }
    }

    private void createTemplateMetricTab(MetricTypes metric) {
        clickCustomMetricLink();
        waitForElementVisible(maqlEditor);
        waitForElementVisible(By.xpath(METRIC_TEMPLATE_TAB_LOCATOR.replace("${tab}", metric.getType())), browser).click();
        waitForElementVisible(By.linkText(METRIC_LINK_LOCATOR.replace("${metricType}", metric.getLabel())),
                browser).click();
    }

    private void enterMetricNameAndSubmit(String name) {
        enterMetricName(name).submit();
    }

    private WebElement getElementHeader(String header) {
        waitForElementsLoading();
        return getRoot().findElements(By.cssSelector(ELEMENT_HEADERS_LOCATOR)).stream()
                .filter(e -> header.equalsIgnoreCase(e.getText()))// handle difference between UI text & getText()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find any header named " + header));
    }

    private WebElement getCodeMirrorWidget(String displayedText) {
        return waitForCollectionIsNotEmpty(browser.findElements(By.cssSelector(".CodeMirror-widget span"))).stream()
                .filter(e -> displayedText.equals(e.getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Cannot find code mirror widget containing " + displayedText));
    }

    private boolean isHeaderHovered(String header) {
        // handle difference between UI text & getText()
        return header.equalsIgnoreCase(
                waitForElementPresent(By.cssSelector(ELEMENT_HEADERS_LOCATOR + ":hover"), browser).getText());
    }

    public enum ElementType {
        FACTS("Facts"),
        METRICS("Metrics"),
        ATTRIBUTES("Attributes"),
        ATTRIBUTE_VALUES("Attribute Values"),
        ATTRIBUTE_LABELS("Attribute Labels"),
        VARIABLES("Variables");

        private String type;

        private ElementType(final String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }
}
