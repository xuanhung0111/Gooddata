package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class ColorPaletteTest extends AbstractAdE2ETest {

    private static final List<String> COLOR_PALETTE = asList(
            "rgb(20,178,226)",
            "rgb(0,193,141)",
            "rgb(229,77,66)",
            "rgb(241,134,0)",
            "rgb(171,85,163)",

            "rgb(250,218,35)",
            "rgb(148,161,174)",
            "rgb(107,191,216)",
            "rgb(181,136,177)",
            "rgb(238,135,128)",

            "rgb(241,171,84)",
            "rgb(133,209,188)",
            "rgb(41,117,170)",
            "rgb(4,140,103)",
            "rgb(181,60,51)",

            "rgb(163,101,46)",
            "rgb(140,57,132)",
            "rgb(136,219,244)",
            "rgb(189,234,222)",
            "rgb(239,197,194)"
    );

    private static final String LEGEND_COLOR_ATTRIBUTE = "fill";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Color-Palette-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_series_order_in_bar_and_column_chart_in_stacked_charts() {
        List<String> expectedLegend = asList("Email", "In Person Meeting", "Phone Call", "Web Meeting");
        assertEquals(analysisPageReact.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        List<String> legendColors = newArrayList(COLOR_PALETTE.subList(0, expectedLegend.size()));

        assertEquals(analysisPageReact.changeReportType(ReportType.BAR_CHART)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        expectElementAttributes(".highcharts-legend-item [fill^='rgb']", LEGEND_COLOR_ATTRIBUTE, legendColors);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_series_colors_in_line_chart_which_has_attribute_in_segment_by() {
        List<String> expectedLegend = asList("Email", "In Person Meeting", "Phone Call", "Web Meeting");
        assertEquals(analysisPageReact.addStack(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        List<String> legendColors = newArrayList(COLOR_PALETTE.subList(0, expectedLegend.size()));

        assertEquals(analysisPageReact.changeReportType(ReportType.LINE_CHART)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        expectElementAttributes(".highcharts-legend-item [fill^='rgb']", LEGEND_COLOR_ATTRIBUTE, legendColors);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_have_correct_series_order_in_bar_and_column_chart_in_non_stacked_charts() {
        List<String> expectedLegend = asList("# of Activities", "# of Lost Opps.");
        assertEquals(analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);

        List<String> legendColors = newArrayList(COLOR_PALETTE.subList(0, expectedLegend.size()));

        assertEquals(analysisPageReact.changeReportType(ReportType.BAR_CHART)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), expectedLegend);
        expectElementAttributes(".highcharts-legend-item [fill^='rgb']", LEGEND_COLOR_ATTRIBUTE, legendColors);
    }

    private void expectElementAttributes(String elementCssLocators, String attribute, List<String> values) {
        assertEquals(browser.findElements(cssSelector(elementCssLocators))
            .stream()
            .map(e -> e.getAttribute(attribute))
            .collect(toList()), values);
    }
}
