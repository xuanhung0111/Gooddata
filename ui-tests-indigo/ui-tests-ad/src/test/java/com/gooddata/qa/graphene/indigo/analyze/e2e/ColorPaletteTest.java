package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ColorPaletteTest extends AbstractAdE2ETest {

    private static final List<String> COLOR_PALETTE = asList(
            "rgb(00,131,255)",
            "rgb(00,192,142)",
            "rgb(241,35,61)",
            "rgb(239,134,00)",
            "rgb(188,90,178)",

            "rgb(250,205,8)",
            "rgb(148,161,173)",
            "rgb(93,188,255)",
            "rgb(216,141,206)",
            "rgb(242,115,115)",

            "rgb(254,178,92)",
            "rgb(137,216,187)",
            "rgb(0,107,184)",
            "rgb(0,131,96)",
            "rgb(173,11,33)",

            "rgb(177,100,0)",
            "rgb(133,54,125)",
            "rgb(194,229,255)",
            "rgb(201,238,225)",
            "rgb(250,208,211)"
    );

    private static final String LEGEND_COLOR_ATTRIBUTE = "fill";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Color-Palette-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_series_order_in_bar_and_column_chart_in_stacked_charts() {
        List<String> expectedLegend = asList("Email", "In Person Meeting", "Phone Call", "Web Meeting");
        assertEquals(analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        List<String> legendColors = newArrayList(COLOR_PALETTE.subList(0, expectedLegend.size()));

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        expectElementAttributes(".highcharts-legend-item path", LEGEND_COLOR_ATTRIBUTE, legendColors);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_series_colors_in_line_chart_which_has_attribute_in_segment_by() {
        List<String> expectedLegend = asList("Email", "In Person Meeting", "Phone Call", "Web Meeting");
        assertEquals(analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        List<String> legendColors = newArrayList(COLOR_PALETTE.subList(0, expectedLegend.size()));

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        expectElementAttributes(".highcharts-legend-item path", LEGEND_COLOR_ATTRIBUTE, legendColors);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_series_order_in_bar_and_column_chart_in_non_stacked_charts() {
        List<String> expectedLegend = asList("# of Activities", "# of Lost Opps.");
        assertEquals(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        List<String> legendColors = newArrayList(COLOR_PALETTE.subList(0, expectedLegend.size()));

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);
        expectElementAttributes(".highcharts-legend-item path", LEGEND_COLOR_ATTRIBUTE, legendColors);

        // For bar chart, highcharts has series in DOM bottom-up, so check that
        // if we reverse series and check what"s in DOM, it matches
        Collections.reverse(legendColors);
        expectElementAttributes(".highcharts-series rect", LEGEND_COLOR_ATTRIBUTE, legendColors);
    }

    private void expectElementAttributes(String elementCssLocators, String attribute, List<String> values) {
        assertEquals(browser.findElements(cssSelector(elementCssLocators))
            .stream()
            .map(e -> e.getAttribute(attribute))
            .collect(toList()), values);
    }
}
