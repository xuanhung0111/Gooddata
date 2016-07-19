package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;

public class GoodSalesCatalogueTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Catalogue-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilteringFieldsInCatalog() {
        final CataloguePanel cataloguePanel = analysisPageReact.getCataloguePanel();

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
            .search("am");

        assertTrue(cataloguePanel.getFieldsInViewPort()
            .stream()
            .allMatch(input -> {
                final String cssClass = input.getAttribute("class");
                return cssClass.contains(FieldType.METRIC.toString()) ||
                        cssClass.contains(FieldType.FACT.toString());
            })
        );

        cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
            .search("am");

        assertTrue(cataloguePanel.getFieldsInViewPort()
            .stream()
            .allMatch(input -> input.getAttribute("class").contains(FieldType.ATTRIBUTE.toString()))
        );
    }

    @Test(dependsOnGroups = {"init"})
    public void testCreateReportWithFieldsInCatalogFilter() {
        final CataloguePanel cataloguePanel = analysisPageReact.getCataloguePanel();

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES);
        analysisPageReact.addMetric(METRIC_AMOUNT);
        cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES);
        analysisPageReact.addAttribute(ATTR_STAGE_NAME)
            .waitForReportComputing();
        assertTrue(analysisPageReact.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6942")
    public void testCaseSensitiveSortInAttributeMetric() {
        initManagePage();
        String attribute = getMdService().getObjUri(getProject(), Attribute.class,
                Restriction.identifier("attr.product.id"));
        getMdService().createObj(getProject(), new Metric("aaaaA1", "SELECT COUNT([" + attribute + "])", "#,##0"));
        getMdService().createObj(getProject(), new Metric("AAAAb2", "SELECT COUNT([" + attribute + "])", "#,##0"));

        try {
            initAnalysePage();
            analysisPageReact.getCataloguePanel().search("aaaa");
            assertEquals(analysisPageReact.getCataloguePanel().getFieldNamesInViewPort(),
                    asList("aaaaA1", "AAAAb2"));
        } finally {
            deleteMetric("aaaaA1");
            deleteMetric("AAAAb2");
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricAttribute() {
        String xssAttribute = "<button>" + ATTR_IS_WON + "</button>";
        String xssMetric = "<button>" + METRIC_PERCENT_OF_GOAL + "</button>";

        initAttributePage();
        waitForFragmentVisible(attributePage).initAttribute(ATTR_IS_WON);
        waitForFragmentVisible(attributeDetailPage).renameAttribute(xssAttribute);

        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(METRIC_PERCENT_OF_GOAL);
        waitForFragmentVisible(metricDetailPage).renameMetric(xssMetric);

        try {
            initAnalysePage();
            final CataloguePanel cataloguePanel = analysisPageReact.getCataloguePanel();

            assertFalse(cataloguePanel.search("<button> test XSS </button>"));
            assertFalse(cataloguePanel.search("<script> alert('test'); </script>"));
            assertTrue(cataloguePanel.search("<button>"));
            assertEquals(cataloguePanel.getFieldNamesInViewPort(), asList(xssMetric, xssAttribute));

            StringBuilder expected = new StringBuilder(xssMetric).append("\n")
                    .append("Field Type\n")
                    .append("Calculated Measure\n")
                    .append("Defined As\n")
                    .append("select Won/Quota\n");
            assertEquals(cataloguePanel.getMetricDescription(xssMetric), expected.toString());

            expected = new StringBuilder(xssAttribute).append("\n")
                    .append("Field Type\n")
                    .append("Attribute\n")
                    .append("Values\n")
                    .append("false\n")
                    .append("true\n");
            assertEquals(cataloguePanel.getAttributeDescription(xssAttribute), expected.toString());

            analysisPageReact.addMetric(xssMetric).addAttribute(xssAttribute)
                .waitForReportComputing();
            assertEquals(analysisPageReact.getMetricsBucket().getItemNames(), asList(xssMetric));
            assertEquals(analysisPageReact.getAttributesBucket().getItemNames(), asList(xssAttribute));
            assertTrue(analysisPageReact.getFilterBuckets().isFilterVisible(xssAttribute));
            assertEquals(analysisPageReact.getChartReport().getTooltipTextOnTrackerByIndex(0),
                    asList(asList(xssAttribute, "true"), asList(xssMetric, "1,160.9%")));
        } finally {
            initAttributePage();
            waitForFragmentVisible(attributePage).initAttribute(xssAttribute);
            waitForFragmentVisible(attributeDetailPage).renameAttribute(ATTR_IS_WON);

            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(xssMetric);
            waitForFragmentVisible(metricDetailPage).renameMetric(METRIC_PERCENT_OF_GOAL);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void testHiddenUnrelatedObjects() {
        final CataloguePanel cataloguePanel = analysisPageReact.getCataloguePanel();

        analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(cataloguePanel.search(""));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(48));

        assertThat(cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
            .getUnrelatedItemsHiddenCount(), equalTo(39));
        assertThat(cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getUnrelatedItemsHiddenCount(), equalTo(9));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.ALL)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(4));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(4));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(0));
    }

    @Test(dependsOnGroups = {"init"})
    public void searchNonExistent() {
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            assertFalse(analysisPageReact.getCataloguePanel().filterCatalog(type)
                    .search("abcxyz"));
            assertTrue(isElementPresent(ByJQuery.selector(
                    ".adi-no-items:contains('No data matching\"abcxyz\"')"), browser));
        });

        analysisPageReact.getCataloguePanel().filterCatalog(CatalogFilterType.ALL);
        analysisPageReact.addAttribute(ATTR_ACTIVITY_TYPE);
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            assertFalse(analysisPageReact.getCataloguePanel().filterCatalog(type).search("Am"));
            assertTrue(isElementPresent(ByJQuery.selector(
                    ".adi-no-items:contains('No data matching\"Am\"')"), browser));

            assertTrue(waitForElementVisible(cssSelector(".adi-no-items .s-unavailable-items-matched"), browser)
                    .getText().matches("^\\d unrelated data item[s]? hidden$"));
        });
    }

    private void deleteMetric(String metric) {
        initMetricPage();
        metricPage.openMetricDetailPage(metric);
        waitForFragmentVisible(metricDetailPage).deleteMetric();
        assertFalse(metricPage.isMetricVisible(metric));
    }
}
