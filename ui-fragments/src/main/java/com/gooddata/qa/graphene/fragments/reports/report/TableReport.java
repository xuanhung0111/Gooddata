package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.utils.Sleeper;
import org.apache.commons.lang3.tuple.Pair;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.google.common.collect.Iterables.getLast;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;

/**
 * Fragment represents table report in
 *  - Dashboard page
 *  - Report page
 *  - Drill dialog
 */
public class TableReport extends AbstractDashboardReport {

    private By BY_BOTTOM_RIGHT_RESIZE_BUTTON = By.className("yui3-selectionbox-resize-br");
    private By BY_TOP_LEFT_RESIZE_BUTTON = By.className("yui3-selectionbox-resize-tl");

    /**
     * Get number of data row
     *
     * @return number of rows
     */
    public int getNumberOfDataRows() {
        return browser.findElements(By.cssSelector(".cell.rows.even,.cell.rows.odd")).size();
    }

    /**
     * Get height
     *
     * @return height in pixel of a row
     */
    public int getRowHeight() {
        WebElement firstRowElement = browser.findElement(By.cssSelector(".cell.rows.even,.cell.rows.odd"));
        if (firstRowElement == null) {
            return 0;
        }
        return firstRowElement.getSize().getHeight();
    }

    public TableReport sortBy(String value, CellType type, Sort howToSort) {
        WebElement cell = getCellElement(value, type);

        getActions().moveToElement(cell).moveByOffset(1, 1).perform();
        waitForElementVisible(howToSort.getLocator(), cell).click();
        return this;
    }

    public boolean isSortableAt(String value, CellType type) {
        WebElement cellElement = getCellElement(value, type);

        getActions().moveToElement(cellElement).moveByOffset(1, 1).perform();
        return isElementPresent(By.className("sort"), cellElement);
    }

    public List<String> getAttributeHeaders() {
        return getCellValues(CellType.ATTRIBUTE_HEADER);
    }

    public Set<String> getMetricHeaders() {
        return new HashSet<>(getCellValues(CellType.METRIC_HEADER));
    }

    public List<String> getTotalHeaders() {
        return getCellValues(CellType.TOTAL_HEADER);
    }

    public List<Float> getTotalValues() {
        return getCellValuesAsNumber(CellType.TOTAL_VALUE);
    }

    public List<String> getAttributeValues() {
        return getCellValues(CellType.ATTRIBUTE_VALUE);
    }

    public List<List<String>> getDataContent() {
        List<List<String>> dataContent = new ArrayList<>();
        Collection<Integer> rowPositions = getDataRowPositions();

        List<WebElement> dataCellElements =
                Stream.of(getCellElements(CellType.ATTRIBUTE_VALUE), getCellElements(CellType.METRIC_VALUE))
                        .flatMap(Collection::stream)
                        .collect(toList());

        for (int rowPosition : rowPositions) {
            dataContent.add(dataCellElements.stream()
                    .filter(cell -> getCellRowPositions(cell).contains(rowPosition))
                    .sorted((cell1, cell2) -> getCellColumnPosition(cell1) - getCellColumnPosition(cell2))
                    .map(WebElement::getText)
                    .collect(toList()));
        }
        return dataContent;
    }

    public List<Float> getMetricValues() {
        return getCellValuesAsNumber(CellType.METRIC_VALUE);
    }

    public List<String> getRawMetricValues() {
        return getCellValues(CellType.METRIC_VALUE);
    }

    public TableReport drillOn(String value, CellType type) {
        return drillOn(getCellElement(value, type));
    }

    public TableReport drillOnFirstValue(CellType type) {
        return drillOn(getCellElements(type).get(0));
    }

    public DashboardDrillDialog openDrillDialogFrom(String value, CellType type) {
        drillOn(value, type);
        return DashboardDrillDialog.getInstance(browser);
    }

    public DashboardDrillDialog hoverAndOpenDrillDialogFrom(String value, CellType type) {
        getActions().moveToElement(getCellElement(value, type).findElement(By.tagName("span"))).click().perform();
        return DashboardDrillDialog.getInstance(browser);
    }

    public boolean isDrillable(String value, CellType type) {
        return isDrillable(getCellElement(value, type));
    }

    public boolean isDrillableToExternalPage(CellType type) {
        return getCellElements(type).stream().allMatch(this::isDrillableToExternalPage);
    }

    public boolean isImageDisplayed(String imageSource, CellType type) {
        return isImageDisplayed(getCellElement(imageSource, type).findElement(By.tagName("img")));
    }

    public boolean isErrorImageDisplayed(String imageSource, CellType type) {
        WebElement image = getCellElement(imageSource, type);
        return isImageDisplayed(image.findElement(By.tagName("img"))) &&
                isElementPresent(By.className("imageError"), image);
    }

    public boolean hasValue(String value, CellType type) {
        return findCellElement(value, type).isPresent();
    }

    public boolean hasNoData() {
        return !isElementPresent(By.className("containerBody"), getRoot());
    }

    public ContextMenu openContextMenuFrom(String value, CellType type) {
        getActions().contextClick(getCellElement(value, type)).perform();
        return ContextMenu.getInstance(browser);
    }

    public TableReport selectContentArea(Pair<Integer, Integer> beginningCoordinates, Pair<Integer, Integer> endingCoordinates) {
        int startPoint = getCellElements(CellType.METRIC_VALUE)
                .lastIndexOf(getCellElementBy(beginningCoordinates.getLeft(), beginningCoordinates.getRight()));
        int endPoint = getCellElements(CellType.METRIC_VALUE)
                .lastIndexOf(getCellElementBy(endingCoordinates.getLeft(), endingCoordinates.getRight()));
        getActions().clickAndHold(getCellElements(CellType.METRIC_VALUE).get(startPoint))
                .moveToElement(getCellElements(CellType.METRIC_VALUE).get(endPoint)).release().perform();
        return this;
    }

    public void copyMetricValue(String value) {
        WebElement metricValue = getCellElement(value, CellType.METRIC_VALUE);
        metricValue.click();

        Function<WebDriver, Boolean> isSelected = browser -> metricValue.getAttribute("class").contains("highlight");
        Graphene.waitGui().until(isSelected);

        getActions().keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();
    }

    public TableReport clickOnCellElement(String value , CellType cellType) {
        getCellElement(value, cellType).click();
        return this;
    }

    public TableReport waitForLoaded() {
        WebElement loadingElement = findLoadingElement();

        try {
            waitForElementVisible(loadingElement, 5);
            waitForElementNotVisible(loadingElement);

        } catch (TimeoutException e) {
            // Report already loaded so WebDriver unable to catch the loading indicator
        }
        return this;
    }

    public TableReport resizeFromTopLeftButton(int xOffset, int yOffset) {
        return resize(BY_TOP_LEFT_RESIZE_BUTTON, xOffset, yOffset);
    }

    public TableReport resizeFromBottomRightButton(int xOffset, int yOffset) {
        return resize(BY_BOTTOM_RIGHT_RESIZE_BUTTON, xOffset, yOffset);
    }

    private WebElement getCellElementBy(int row, int column) {
        row = row -1;
        column = column -1;
        String region = format("%d,%d,%d,%d", row, column, row, column);
        return getCellElements(CellType.METRIC_VALUE).stream()
                .filter(element -> element.getAttribute("gdc:region").equals(region))
                .findFirst()
                .get();
    }

    private WebElement findLoadingElement() {
        return Stream.of(By.className("c-report-overlay"), By.id("progressOverlay"))
                .filter(by -> isElementPresent(by, browser))
                .map(by -> getLast(browser.findElements(by)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find report loading element"));
    }

    private WebElement getCellElement(String value, CellType type) {
        return findCellElement(value, type)
                .orElseThrow(() -> new NoSuchElementException("Not found cell type: " + type + " with value: " + value));
    }

    public List<WebElement> getCellElements(CellType type) {
        waitForLoaded();
        return getRoot().findElements(type.getLocator());
    }

    private Optional<WebElement> findCellElement(String value, CellType type) {
        return getCellElements(type).stream()
                .filter(e -> {
                    if (isDrillableToExternalPage(e)) {
                        return value.equals(e.getText());
                    }
                    return value.equals(e.getAttribute("title").trim());
                })
                .findFirst();
    }

    private List<String> getCellValues(CellType type) {
        return getCellElements(type).stream()
                .map(e -> {
                    if (isDrillableToExternalPage(e)) {
                        return e.getText();
                    }
                    return e.getAttribute("title").trim();
                })
                .collect(toList());
    }

    private List<Float> getCellValuesAsNumber(CellType type) {
        return getCellValues(type).stream()
                .map(ReportPage::getNumber)
                .collect(toList());
    }

    private List<Integer> getCellRowPositions(WebElement cell) {
        List<Integer> positions = Stream.of(cell.getAttribute("gdc:region").split(","))
                .map(Integer::valueOf).collect(toList());
        return IntStream.rangeClosed(positions.get(0), positions.get(2))
                .mapToObj(Integer::new).collect(toList());
    }

    private int getCellColumnPosition(WebElement cell) {
        return Integer.valueOf(cell.getAttribute("gdc:region").split(",")[1]);
    }

    private List<Integer> getDataRowPositions() {
        return getCellElements(CellType.ATTRIBUTE_VALUE).stream()
                .map(this::getCellRowPositions)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList());
    }

    private boolean isDrillable(WebElement element) {
        return element.getAttribute("class").contains("drillable");
    }

    private boolean isDrillableToExternalPage(WebElement element) {
        return isDrillable(element) && element.getAttribute("title").contains("Open external link in a new window");
    }

    private TableReport drillOn(WebElement element) {
        Graphene.waitGui().until().element(element).attribute("class").contains("drillable");

        if (!isDrillable(element)) {
            throw new RuntimeException("Could not drill on undrillable element");
        }
        ElementUtils.moveToElementActions(element
                .findElement(By.tagName(isElementPresent(By.tagName("img"), element) ? "img" : "span")), 5, 5).click().perform();
        return this;
    }

    /**
     * this method use scroll into viewport (scroll horizontal or scroll vertical)
     *
     * @param value the value is found after scrolled
     * @param scrolltype scroll horizontal or scroll vertical
     * @return true if found and false if did not find
     */
    public boolean scrollIntoViewAndCheckValue(String value, ScrollType scrolltype) {
        long startTime = System.currentTimeMillis();
        int scroll = 0;
        String cssSelector = "s-grid-" + simplifyText((value.replace(">", "_")));
        while ((System.currentTimeMillis() - startTime) < 60000) {
            sleepTight(500);
            if (isElementVisible(className(cssSelector), getRoot())) {
                return true;
            }
            scroll += 50;
            if (scrolltype.getLocator().equals("horizScrollbar")) {
                WebElement editor = waitForElementVisible(By.className("horizScrollbar"), getRoot());
                getActions().clickAndHold(editor).moveByOffset(scroll, 0).release().perform();
            } else if (scrolltype.getLocator().equals("vertScrollbar")) {
                WebElement editor = waitForElementVisible(By.className("vertScrollbar"), getRoot());
                getActions().clickAndHold(editor).moveByOffset(0, scroll).release().perform();
            }
        }
        throw new TimeoutException("Tried for 60 second(s), element doesn't find by selector " + cssSelector);
    }

    // Check Metric or Attribute Header
    public boolean checkValue(String name) {
        Sleeper.sleepTightInSeconds(3);
        String cssSelector = "s-grid-" + simplifyText(name);
        return isElementVisible(className(cssSelector), getRoot());
    }

    public enum ScrollType {
        HORI("horizScrollbar"),
        VERT("vertScrollbar");

        private String locator;

        ScrollType(String locator) {
            this.locator = locator;
        }

        public String getLocator() {
            return locator;
        }
    }

    private boolean isImageDisplayed(WebElement image) {
        //have 3 scenarios when loading image
        //1.Image is loaded successfully
        //      src attribute = https://api.monosnap.com/image/download?id=987dH7kckbVcKcv8UFK5jdoKXCJ8wk
        //2.An alternative img is loaded when browser encounters error
        //      src attribute =
        //                  /gdc/app/projects/s03a3h7ru03dli7iq3o4xk4fl2xlgqmn/images?source=http:
        //                  //samsuria.com/wp-content/uploads/2014/10/wallpaper-nature-3d.jpg
        //                  &errorPage=/images/2011/il-no-image.png
        //3.A broken image symbol is displayed
        //      src attribute = /gdc/app/projects/s03a3h7ru03dli7iq3o4xk4fl2xlgqmn/images?source=web&url
        //                  =http://hdwallweb.com/wp-content/uploads/2014/10/01/nature-home-drawing.jpg&errorPage=/images
        //                  /2011/il-no-image.png
        //
        //Generally, calling restAPI with src attribute only handles case 2&3 by checking body response
        //due to src attribute differences.
        //Using JS to cover 3 above cases. To check an image is displayed completely, JS code works as below
        //1.wait for image is loaded
        //2.image's width must be defined and > 0

        final String js = "return arguments[0].complete && "
                + "typeof arguments[0].naturalWidth != \"undefined\" && "
                + "arguments[0].naturalWidth > 0";

        return (Boolean) BrowserUtils.runScript(browser, js, image);
    }

    private TableReport resize(By button, int xOffset, int yOffset) {
        getActions()
                .clickAndHold(waitForElementVisible(button, browser))
                .moveByOffset(xOffset, yOffset)
                .release()
                .perform();
        return this;
    }

    public enum Sort {
        ASC("ascendingArrow"),
        DESC("descendingArrow");

        private String locator;

        Sort(String locator) {
            this.locator = locator;
        }

        public By getLocator() {
            return By.className(locator);
        }
    }

    public enum CellType {
        ATTRIBUTE_HEADER {
            @Override
            public By getLocator() {
                return isElementVisible(By.id("analysisAttributesContainer"), BrowserUtils.getBrowserContext())
                        ? By.cssSelector("#analysisAttributesContainer .attribute")
                        : By.className("attribute");
            }
        },
        ATTRIBUTE_VALUE {
            @Override
            public By getLocator() {
                return By.cssSelector(".gridTile .element");
            }
        },
        METRIC_HEADER {
            @Override
            public By getLocator() {
                return isElementVisible(By.id("analysisMetricsContainer"), BrowserUtils.getBrowserContext())
                        ? By.cssSelector("#analysisMetricsContainer .metric")
                        : By.className("metric");
            }
        },
        METRIC_VALUE {
            @Override
            public By getLocator() {
                return By.cssSelector(".data:not(.total)");
            }
        },
        TOTAL_HEADER {
            @Override
            public By getLocator() {
                return By.className("totalHeader");
            }
        },
        TOTAL_VALUE {
            @Override
            public By getLocator() {
                return By.cssSelector(".data.total");
            }
        };

        public abstract By getLocator();
    }
}
