package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.gooddata.qa.graphene.common.frame.InFrameAction;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class InteractiveReportWidget extends AbstractFragment{

    private static final By propertySelectionPanelLocator        =
            By.xpath("//div[contains(@class,'gd-dropdown') and not(contains(@class,'searchfield'))]");

    private static final By searchPropertyFieldLocator           =
            By.cssSelector("div.searchfield input.searchfield-input");

    private static final By colorLinkLocator                     =
            By.xpath("//div[contains(@class,'edit-panel-block-link')]/a[contains(text(),'color')]");

    private static final By addTableAttributeLinkLocator         =
            By.xpath("//div[contains(@class,'edit-panel-block-link')]/a[contains(text(),'table')]");

    // Just support add ONE table attribute at this moment
    private static final By configureTableAttributeButtonLocator =
            By.cssSelector(".edit-panel-block-row button");

    private static final By reportTitleLocator                   =
            By.cssSelector(".title");

    private static final By reportSubtitleLocator                =
            By.cssSelector(".subtitle");

    // ********************** chart locators ********************** //
    private static final By chartTrackersLocator                 =
            By.cssSelector(".highcharts-tracker *");

    private static final By chartLegendsLocator                  =
            By.cssSelector(".highcharts-legend-item");

    private static final By chartAlertTitleLocator               =
            By.cssSelector(".alert-title");

    private static final By chartErrorTitleLocator               =
            By.cssSelector(".explorer-message-title");

    private static final By chartTooltipLocator                  =
            By.cssSelector("div.highcharts-tooltip");

    // ********************** table locators ********************** //
    private static final By tableHeadersLocator                  =
            By.cssSelector(".ember-table-header-row .ember-table-content");

    private static final By tableRowsLocator                     =
            By.xpath("//div[contains(@class,'ember-table-body-container')]"
                   + "//div[contains(@class,'ember-table-table-row') and not(contains(@style,'display:none'))]");

    private static final By tableFooterLocator                   =
            By.cssSelector(".ember-table-footer-container .ember-table-content");

    private static final String CONFIGURE_ATTRIBUTE_BUTTON_XPATH    = "//div[contains(@class,'edit-panel-block') and ./label[.='%s']]//button";

    private static final String ATTRIBUTE_XPATH                     = "//div[./div[contains(@class,'is-collapsible')]/span[.='%s']]"
                                                                    + "/following-sibling::div/div[contains(@class,'type-attribute') and "
                                                                                                + "not(contains(@class, 'item-disabled'))]"
                                                                    + "/span[.='%s']";

    private static final String INVALID_ATTRIBUTE_XPATH             = "//div[./div[contains(@class,'is-collapsible')]/span[.='%s']]"
                                                                    + "/following-sibling::div/div[contains(@class,'type-attribute') and "
                                                                                                + "contains(@class, 'item-disabled')]"
                                                                    + "/span[.='%s']";

    private static final String METRIC_XPATH                        = "//div[./div[contains(@class,'is-collapsible')]/span[.='%s']]"
                                                                    + "/following-sibling::div/div[contains(@class,'type-metric')]"
                                                                    + "/span[.='%s']";

    private static final String DELETE_TABLE_ATTRIBUTE_BUTTON_XPATH = "//div[contains(@class,'edit-panel-block-row') and"
                                                                    + "//button[@title='%s']]/a";

    private static final String INVALID_CONFIGURATION     = "Invalid configuration";
    private static final String TOO_MANY_DATA_POINTS      = "Too many data points";

    private static final String DISABLE_BAR_CHART_COLOR             = "rgb(216,216,216)";
    private static final String SELECTED_LINE_OR_AREA_COLOR         = "black";

    public InteractiveReportWidget selectChartType(final ChartType type) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                WebElement chart = waitForElementVisible(By.cssSelector(String.format(".%s", type.toString())), browser);
                chart.click();
                assertTrue(chart.getAttribute("class").contains("active"),
                           String.format("%s chart is not selected.", StringUtils.capitalize(type.toString())));

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget configureColor(String folder, String colorAttribute) {
        return configureAttribute(By.xpath(String.format(CONFIGURE_ATTRIBUTE_BUTTON_XPATH, "Color")),
                                  folder,
                                  colorAttribute,
                                  ATTRIBUTE_XPATH);
    }

    public InteractiveReportWidget configureYAxis(String folder, String metric) {
        return configureAttribute(By.xpath(String.format(CONFIGURE_ATTRIBUTE_BUTTON_XPATH, "Y Axis")),
                                  folder,
                                  metric,
                                  METRIC_XPATH);
    }

    public InteractiveReportWidget configureXAxisWithValidAttribute(String folder, String attribute) {
        return configureXAxis(folder, attribute, false);
    }

    public InteractiveReportWidget configureXAxisWithInvalidAttribute(String folder, String attribute) {
        return configureXAxis(folder, attribute, true);
    }

    public InteractiveReportWidget enableChartColor() {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                WebElement colorLink = waitForElementVisible(colorLinkLocator, browser);
                if (colorLink.getText().startsWith("disable"))
                    // do nothing
                    return InteractiveReportWidget.this;

                colorLink.click();
                waitForElementVisible(By.xpath(String.format(CONFIGURE_ATTRIBUTE_BUTTON_XPATH, "Color")), browser);
                assertTrue(colorLink.getText().startsWith("disable"));

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget disableChartColor() {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                WebElement colorLink = waitForElementVisible(colorLinkLocator, browser);
                if (colorLink.getText().startsWith("enable"))
                    // do nothing
                    return InteractiveReportWidget.this;

                WebElement colorAttributeButton = waitForElementVisible(By.xpath(String.format(CONFIGURE_ATTRIBUTE_BUTTON_XPATH, "Color")), browser);
                colorLink.click();
                waitForElementNotVisible(colorAttributeButton);
                assertTrue(colorLink.getText().startsWith("enable"));

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget enableAbilityToAddMoreTableAttributes() {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                waitForElementVisible(addTableAttributeLinkLocator, browser).click();
                waitForElementVisible(configureTableAttributeButtonLocator, browser);

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget addMoreTableAttributes(String folder, String attribute) {
        return configureAttribute(configureTableAttributeButtonLocator, folder, attribute, ATTRIBUTE_XPATH);
    }

    public InteractiveReportWidget deleteTableAttribute(final String attribute) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                WebElement deleteButton = waitForElementVisible(By.xpath(String.format(DELETE_TABLE_ATTRIBUTE_BUTTON_XPATH, attribute)), browser);
                deleteButton.click();
                waitForElementNotVisible(deleteButton);

                return InteractiveReportWidget.this;
            }
        });
    }

    public String getReportTitle(boolean inEditMode) {
        return getReportTitleOrSubtitle(reportTitleLocator, inEditMode);
    }

    public String getReportSubtitle(boolean inEditMode) {
        return getReportTitleOrSubtitle(reportSubtitleLocator, inEditMode);
    }

    public InteractiveReportWidget changeReportTitle(String newTitle) {
        return changeReportTitleOrSubtitle(reportTitleLocator, newTitle, true);
    }

    public InteractiveReportWidget changeReportSubtitle(String newSubtitle) {
        return changeReportTitleOrSubtitle(reportSubtitleLocator, newSubtitle, false);
    }

    public InteractiveReportWidget clickOnTracker(final int index) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                getChartTrackers().get(index).click();

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget clickOnSeries(final int index) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                getChartLegends().get(index).click();

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget resetTrackerSelection() {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                waitForElementVisible(By.cssSelector(".highcharts-grid *"), browser).click();

                return InteractiveReportWidget.this;
            }
        });
    }

    public InteractiveReportWidget hoverOnTracker(final int index) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                new Actions(browser).moveToElement(getChartTrackers().get(index)).perform();

                return InteractiveReportWidget.this;
            }
        });
    }

    public int getTotalTableRows() {
        return doActionInFrame(new InFrameAction<Integer>() {
            @Override public Integer doAction() {

                return getTableRows().size();
            }
        });
    }

    public String getTotalTableRowsFromTableFooter() {
        return doActionInFrame(new InFrameAction<String>() {
            @Override public String doAction() {

                waitForTableContainer();
                return browser.findElement(tableFooterLocator).getText().trim();
            }
        });
    }

    public boolean isColorAppliedOnChart() {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return getAllChartLegendColors().containsAll(getAllChartTrackerColors());
            }
        });
    }

    public boolean isChartTableContainsHeader(final String header) {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return getTableHeaders().contains(header.toUpperCase());
            }
        });
    }

    public boolean areTableValuesInSpecificRowMatchedChartLegendNames(final String header) {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return getAllChartLegendNames().containsAll(getAllValuesFromTableRow(header));
            }
        });
    }

    public boolean areTrackersSelectedWhenClickOnSeries(final ChartType type, final int index) {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                if (type == ChartType.BAR_CHART)
                    return areBarTrackersSelectedWhenClickOnSeries(index);

                return areLineOrAreaTrackersSelectedWhenClickOnSeries(index);
            }
        });
    }

    public boolean isTrackerSelectionReset(final ChartType type) {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                if (type == ChartType.BAR_CHART)
                    return isBarTrackerSelectionReset();

                return isLineOrAreaTrackerSelectionReset();
            }
        });
    }

    public boolean isChartSeriesReset() {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return getAllChartLegendNames().contains("Series 1");
            }
        });
    }

    public boolean isTooltipVisible() {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return !waitForElementVisible(chartTooltipLocator, browser).getAttribute("style").contains("visibility: hidden");
            }
        });
    }

    public boolean isTrackerSelected(final ChartType type, final int index) {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                if (type == ChartType.BAR_CHART)
                    return isBarTrackerSelected(index);

                return isLineOrAreaTrackerSelected(index);
            }
        });
    }

    public boolean isChartAlertMessageVisible() {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return TOO_MANY_DATA_POINTS.equals(waitForElementVisible(chartAlertTitleLocator, browser).getText().trim());
            }
        });
    }

    public boolean isChartErrorMessageVisible() {
        return doActionInFrame(new InFrameAction<Boolean>() {
            @Override public Boolean doAction() {

                return INVALID_CONFIGURATION.equals(waitForElementVisible(chartErrorTitleLocator, browser).getText().trim());
            }
        });
    }

    public enum ChartType {
        BAR_CHART("bar"),
        LINE_CHART("line"),
        AREA_CHART("area");

        private String cssClass;

        private ChartType(String cssClass) {
            this.cssClass = cssClass;
        }

        @Override public String toString() {
            return this.cssClass;
        }
    }

    // *********************** private methods *********************** //

    private <T> T doActionInFrame(InFrameAction<T> action) {
        return InFrameAction.Utils.doActionInFrame(this.getRoot(), action, browser);
    }

    // support Y_Axis, X_Axis, Color, Table
    private InteractiveReportWidget configureAttribute(final By byAttributeButton,
                                                       final String folderName,
                                                       final String attributeName,
                                                       final String attributeTemplatePath) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                waitForElementVisible(byAttributeButton, browser).click();
                WebElement propertySelectionPanel = waitForElementVisible(propertySelectionPanelLocator, browser);
                waitForElementVisible(searchPropertyFieldLocator, browser).sendKeys(attributeName);
                waitForElementVisible(By.xpath(String.format(attributeTemplatePath, folderName, attributeName)), browser).click();
                waitForElementNotVisible(propertySelectionPanel);

                return InteractiveReportWidget.this;
            }
        });
    }

    private InteractiveReportWidget configureXAxis(String folder, String attribute, boolean isInvalidAttribute) {
        return configureAttribute(By.xpath(String.format(CONFIGURE_ATTRIBUTE_BUTTON_XPATH, "X Axis")),
                                  folder,
                                  attribute,
                                  isInvalidAttribute ? INVALID_ATTRIBUTE_XPATH : ATTRIBUTE_XPATH);
    }

    private String getReportTitleOrSubtitle(final By byLocator, final boolean inEditMode) {
        return doActionInFrame(new InFrameAction<String>() {
            @Override public String doAction() {

                WebElement element = waitForElementVisible(byLocator, browser);
                if (inEditMode)
                    return element.findElement(By.cssSelector("label")).getText().trim();

                return element.getText().trim();
            }
        });
    }

    private InteractiveReportWidget changeReportTitleOrSubtitle(final By byLocator, final String newTitle, final boolean isTitle) {
        return doActionInFrame(new InFrameAction<InteractiveReportWidget>() {
            @Override public InteractiveReportWidget doAction() {

                WebElement title = waitForElementVisible(byLocator, browser);
                WebElement titleLabel = title.findElement(By.cssSelector("label"));
                titleLabel.click();

                WebElement titleInput = waitForElementVisible(title.findElement(By.cssSelector("input")));
                titleInput.clear();
                titleInput.sendKeys(newTitle);

                // bug id: https://jira.intgdc.com/browse/GS-79 (Editable label value is not changed by pressing Enter key)
                // work around: click another element
                waitForElementVisible(By.cssSelector(".edit-panel-divider-first"), browser).click();
                waitForElementNotVisible(titleInput);
                assertEquals(waitForElementVisible(titleLabel).getText().trim(), newTitle,
                             "Report " + (isTitle ? "" : "sub") + "title is not changed!");

                return InteractiveReportWidget.this;
            }
        });
    }

    private List<String> getAllChartLegendColors() {
        return Lists.newArrayList(Collections2.transform(getChartLegends(), new Function<WebElement, String>() {
            @Override public String apply(WebElement input) {

                return input.findElement(By.cssSelector("path")).getAttribute("fill").trim();
            }
        }));
    }

    private Collection<String> getAllChartLegendNames() {
        return Collections2.transform(getChartLegends(), new Function<WebElement, String>() {
            @Override public String apply(WebElement input) {

                return input.findElement(By.cssSelector("tspan")).getText().trim();
            }
        });
    }

    private Collection<String> getAllChartTrackerColors() {
        return Sets.newHashSet(Collections2.transform(getChartTrackers(), new Function<WebElement, String>() {
            @Override public String apply(WebElement input) {

                return input.getAttribute("fill").trim();
            }
        }));
    }

    private List<WebElement> getChartTrackers() {
        waitForChartTracker();
        return browser.findElements(chartTrackersLocator);
    }

    private List<WebElement> getChartLegends() {
        waitForChartLegend();
        return browser.findElements(chartLegendsLocator);
    }

    private List<WebElement> getTableRows() {
        waitForTableContainer();
        return browser.findElements(tableRowsLocator);
    }

    private List<String> getTableHeaders() {
        waitForTableHeader();
        return Lists.newArrayList(Collections2.transform(browser.findElements(tableHeadersLocator), new Function<WebElement, String>() {
            @Override public String apply(WebElement input) {

                return input.getText().trim();
            }
        }));
    }

    private Collection<String> getAllValuesFromTableRow(String header) {
        waitForTableHeader();
        waitForTableContainer();

        final int index = getTableHeaders().indexOf(header.toUpperCase());
        if (index == -1) 
            throw new IllegalArgumentException(String.format("Table does not contain '%s' header!", header));

        return Sets.newHashSet(Collections2.transform(getTableRows(), new Function<WebElement, String>() {
            @Override public String apply(WebElement input) {

                return input.findElements(By.cssSelector(".ember-table-content")).get(index).getText().trim();
            }
        }));
    }

    private boolean areBarTrackersSelectedWhenClickOnSeries(int index) {
        return Sets.newHashSet(getAllChartLegendColors().get(index), DISABLE_BAR_CHART_COLOR).containsAll(getAllChartTrackerColors());
    }

    private boolean areLineOrAreaTrackersSelectedWhenClickOnSeries(int index) {
        String selectedColor = getAllChartLegendColors().get(index);
        for (WebElement e : getChartTrackers()) {
            if (selectedColor.equals(e.getAttribute("fill"))) {
                if (!SELECTED_LINE_OR_AREA_COLOR.equals(e.getAttribute("stroke")))
                    return false;
                continue;
            }

            if (e.getAttribute("stroke") != null)
                return false;
        }
        return true;
    }

    private boolean isBarTrackerSelectionReset() {
        for (WebElement e : getChartTrackers()) {
            if (DISABLE_BAR_CHART_COLOR.equals(e.getAttribute("fill")))
                return false;
        }
        return true;
    }

    private boolean isLineOrAreaTrackerSelectionReset() {
        for (WebElement e : getChartTrackers()) {
            if (e.getAttribute("stroke") != null)
                return false;
        }
        return true;
    }

    private boolean isBarTrackerSelected(int index) {
        for (ListIterator<WebElement> it = getChartTrackers().listIterator(); it.hasNext();) {
            if (it.nextIndex() == index) {
                if (DISABLE_BAR_CHART_COLOR.equals(it.next().getAttribute("fill")))
                    return false;
                continue;
            }
            if (!DISABLE_BAR_CHART_COLOR.equals(it.next().getAttribute("fill")))
                return false;
        }
        return true;
    }

    private boolean isLineOrAreaTrackerSelected(int index) {
        for (ListIterator<WebElement> it = getChartTrackers().listIterator(); it.hasNext();) {
            if (it.nextIndex() == index) {
                if (!SELECTED_LINE_OR_AREA_COLOR.equals(it.next().getAttribute("stroke")))
                    return false;
                continue;
            }
            if (SELECTED_LINE_OR_AREA_COLOR.equals(it.next().getAttribute("fill")))
                return false;
        }
        return true;
    }

    private void waitForTableContainer() {
        waitForElementVisible(By.cssSelector(".ember-table-tables-container"), browser);
    }

    private void waitForChartLegend() {
        waitForElementVisible(By.cssSelector(".highcharts-legend"), browser);
    }

    private void waitForChartTracker() {
        waitForElementVisible(By.cssSelector(".highcharts-series-group"), browser);
    }

    private void waitForTableHeader() {
        waitForElementVisible(tableHeadersLocator, browser);
    }
}
