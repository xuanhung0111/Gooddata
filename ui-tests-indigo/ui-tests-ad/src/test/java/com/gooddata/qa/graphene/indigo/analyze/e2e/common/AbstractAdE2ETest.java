package com.gooddata.qa.graphene.indigo.analyze.e2e.common;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeClass;

import com.gooddata.qa.graphene.indigo.analyze.AnalyticalDesignerAbstractTest;
import com.google.common.base.Predicate;

public abstract class AbstractAdE2ETest extends AnalyticalDesignerAbstractTest {

    protected static final String DATE = ".s-date";

    protected static final String METRICS_BUCKET = ".s-bucket-metrics";
    protected static final String CATEGORIES_BUCKET = ".s-bucket-categories";
    protected static final String STACKS_BUCKET = ".s-bucket-stacks";
    protected static final String FILTERS_BUCKET = ".s-bucket-filters";
    protected static final String EMPTY_BUCKET = ".s-bucket-empty";
    protected static final String NOT_EMPTY_BUCKET = ".s-bucket-not-empty";

    protected static final String TRASH = ".s-trash";

    @BeforeClass(alwaysRun = true)
    public void speedUpTestRun() {
        validateAfterClass = false;
    }

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = "";
    }

    protected void visitEditor() {
        initAnalysePageByUrl();
    }

    protected void expectElementAttributes(String elementCssLocators, String attribute, List<String> values) {
        assertEquals(browser.findElements(cssSelector(elementCssLocators))
            .stream()
            .map(e -> e.getAttribute(attribute))
            .collect(toList()), values);
    }

    protected void expectFind(String cssLocator, String context) {
        WebElement contextElement = waitForElementPresent(cssSelector(context), browser);
        try {
            waitForElementPresent(cssSelector(cssLocator), contextElement);
        } catch (Exception e) {
            assertTrue(isElementPresent(cssSelector(cssLocator), contextElement));
        }
    }

    protected void expectFind(String cssLocator) {
        try {
            waitForElementPresent(cssSelector(cssLocator), browser);
        } catch (Exception e) {
            assertTrue(isElementPresent(cssSelector(cssLocator), browser), "Cannot find locator: " + cssLocator);
        }
    }

    protected void expectMissing(String cssLocator) {
        try {
            waitForElementNotPresent(cssSelector(cssLocator));
        } catch (Exception e) {
            assertFalse(isElementPresent(cssSelector(cssLocator), browser));
        }
    }

    protected void expectError(String cssLocator) {
        waitForReportComputing();
        expectFind(cssLocator);
    }

    protected void dragFromCatalogue(String sourceCssLocator, String targetCssLocator) {
        drag(".s-catalogue " + sourceCssLocator, targetCssLocator + " .s-bucket-dropzone");
    }

    protected void expectChartLegend(List<String> legends) {
        waitForReportComputing();

        if (legends.isEmpty()) {
            assertFalse(isElementPresent(className("highcharts-legend"), browser));
            return;
        }

        assertEquals(waitForCollectionIsNotEmpty(browser.findElements(cssSelector(".highcharts-legend tspan")))
            .stream()
            .map(WebElement::getText)
            .collect(toList()), legends);
    }

    protected void toggleBucketItemConfig(String cssLocator) {
        click(cssLocator + " .adi-bucket-item-header");
    }

    protected void click(String cssLocator) {
        waitForElementVisible(cssSelector(cssLocator), browser).click();
    }

    protected void click(String cssLocator, String context) {
        waitForElementVisible(cssSelector(cssLocator),
                waitForElementVisible(cssSelector(context), browser)).click();
    }

    protected void drag(String sourceCssLocator, String targetCssLocator) {
        startDrag(sourceCssLocator);

        Actions action = new Actions(browser);
        try {
            action.moveToElement(waitForElementPresent(cssSelector(targetCssLocator), browser)).perform();
        } finally {
            action.release().perform();
        }
    }

    protected void startDrag(String sourceCssLocator) {
        WebElement source = waitForElementVisible(cssSelector(sourceCssLocator), browser);
        WebElement editor = waitForElementVisible(className("adi-editor"), browser);

        Point location = editor.getLocation();
        Dimension dimension = editor.getSize();
        new Actions(browser).clickAndHold(source)
            .moveByOffset(location.x + dimension.width/2, location.y + dimension.height/2)
            .perform();
    }

    protected void stopDrag(int... x) {
        Actions action = new Actions(browser);
        if (x.length >= 2) {
            action.moveByOffset(x[0], x[1]);
        }
        action.release().perform();
    }

    protected void expectElementCount(String cssLocator, int count) {
        waitForReportComputing();
        assertThat(browser.findElements(cssSelector(cssLocator)).size(), equalTo(count));
    }

    protected void fillIn(String inputCssLocator, String text) {
        waitForElementVisible(cssSelector(inputCssLocator), browser).sendKeys(text);
    }

    protected void clearFilter(String context) {
        waitForElementVisible(className("s-clear"), waitForElementPresent(cssSelector(context), browser)).click();
    }

    protected void switchTabCatalogue(String tabCssLocator) {
        click(tabCssLocator);
    }

    protected void resetReport() {
        click(".s-reset-report");
    }

    protected void undo() {
        click(".s-undo:not(.disabled)");
    }

    protected void redo() {
        click(".s-redo:not(.disabled)");
    }

    protected void searchCatalogue(String data) {
        waitForElementVisible(cssSelector(".catalogue-search .searchfield-input"), browser).sendKeys(data);
        waitForItemLoaded();
    }

    protected void select(String cssLocator, String value) {
        new Select(waitForElementVisible(cssSelector(cssLocator), browser)).selectByValue(value);
    }

    protected void select(String cssLocator, String value, int index) {
        new Select(waitForElementVisible(browser.findElements(cssSelector(cssLocator)).get(index)))
            .selectByValue(value);
    }

    protected void expectElementTexts(String cssLocator, List<String> texts) {
        assertEquals(browser.findElements(cssSelector(cssLocator))
            .stream()
            .map(WebElement::getText)
            .collect(toList()), texts);
    }

    protected void expectClean() {
        expectFind(METRICS_BUCKET + EMPTY_BUCKET);
        expectFind(CATEGORIES_BUCKET + EMPTY_BUCKET);
        expectFind(STACKS_BUCKET + EMPTY_BUCKET);
        expectFind(FILTERS_BUCKET + EMPTY_BUCKET);
        expectFind(".adi-blank");
    }

    protected void cancelSearch() {
        click(".searchfield-clear");
        waitForItemLoaded();
    }

    protected void switchVisualization(String visual) {
        click(".s-visualization.vis-type-" + visual);
    }

    protected void expectExportDisabled() {
        expectFind(".s-export-to-report.disabled");
    }

    protected void hover(String cssLocator, String parentCssLocator) {
        new Actions(browser)
            .moveToElement(waitForElementVisible(cssSelector(parentCssLocator + " " + cssLocator), browser))
            .perform();
    }

    protected void hover(String cssLocator) {
        hover(cssLocator, "");
    }

    protected void createAttributeFilter(String attrCssLocator) {
        click(".s-btn-add_attribute_filter");
        click(attrCssLocator);
    }

    protected void selectFirstElementFromAttributeFilter() {
        click(".s-filter-picker .s-clear");
        click(".s-filter-item input");
        click(".s-filter-picker .s-apply:not(.disabled)");
    }

    protected void selectAllElementsFromAttributeFilter() {
        click(".s-filter-picker .s-select_all");
        click(".s-filter-picker .s-apply:not(.disabled)");
    }

    private void waitForReportComputing() {
        try {
            sleepTightInSeconds(1);
            if (isElementPresent(className("adi-computing"), browser)) {
                WebElement computingElement = browser.findElement(className("adi-computing"));
                waitForElementNotVisible(computingElement);
            }
        } catch(Exception e) {
            // in case report is rendered so fast, computing label is not shown.
            // Ignore the exception.
        }
    }

    private void waitForItemLoaded() {
        Predicate<WebDriver> itemsLoaded = browser -> !isElementPresent(cssSelector(".gd-spinner.small"),
                browser);
        Graphene.waitGui().until(itemsLoaded);
    }
}
