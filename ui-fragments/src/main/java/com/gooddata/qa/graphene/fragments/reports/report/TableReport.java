package com.gooddata.qa.graphene.fragments.reports.report;

import com.gooddata.qa.graphene.fragments.dashboards.DashboardDrillDialog;
import com.gooddata.qa.graphene.fragments.reports.filter.ContextMenu;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.utils.browser.BrowserUtils;
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
import static com.google.common.collect.Iterables.getLast;
import static java.util.stream.Collectors.toList;

/**
 * Fragment represents table report in
 *  - Dashboard page
 *  - Report page
 *  - Drill dialog
 */
public class TableReport extends AbstractDashboardReport {

    public TableReport sortBy(String value, CellType type, Sort howToSort) {
        WebElement cell = getCellElement(value, type);

        getActions().moveToElement(cell).perform();
        waitForElementVisible(howToSort.getLocator(), cell).click();
        return this;
    }

    public boolean isSortableAt(String value, CellType type) {
        WebElement cellElement = getCellElement(value, type);

        getActions().moveToElement(cellElement).perform();
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

    public void copyMetricValue(String value) {
        WebElement metricValue = getCellElement(value, CellType.METRIC_VALUE);
        metricValue.click();

        Function<WebDriver, Boolean> isSelected = browser -> metricValue.getAttribute("class").contains("highlight");
        Graphene.waitGui().until(isSelected);

        getActions().sendKeys(Keys.chord(Keys.CONTROL, "c")).perform();
    }

    public TableReport waitForLoaded() {
        WebElement loadingElement = findLoadingElement();

        try {
            waitForElementVisible(loadingElement, 1);
            waitForElementNotVisible(loadingElement);

        } catch (TimeoutException e) {
            // Report already loaded so WebDriver unable to catch the loading indicator
        }
        return this;
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

    private List<WebElement> getCellElements(CellType type) {
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
        if (!isDrillable(element)) {
            throw new RuntimeException("Could not drill on undrillable element");
        }
        ElementUtils.moveToElementActions(element.findElement(By.tagName("span")), 5, 5).click().perform();
        return this;
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
                return isElementPresent(By.id("analysisAttributesContainer"), BrowserUtils.getBrowserContext())
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
                return isElementPresent(By.id("analysisMetricsContainer"), BrowserUtils.getBrowserContext())
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
