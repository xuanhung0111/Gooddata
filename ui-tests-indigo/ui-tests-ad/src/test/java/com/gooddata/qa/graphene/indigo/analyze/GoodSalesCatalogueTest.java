package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.google.common.base.Predicate;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.graphene.findby.ByJQuery;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CataloguePanel;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;

public class GoodSalesCatalogueTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Catalogue-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createNumberOfActivitiesMetric();
        createAmountMetric();
        createPercentOfGoalMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilteringFieldsInCatalog() {
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

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

    @Test(dependsOnGroups = {"createProject"})
    public void testCreateReportWithFieldsInCatalogFilter() {
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

        cataloguePanel.filterCatalog(CatalogFilterType.MEASURES);
        analysisPage.addMetric(METRIC_AMOUNT);
        cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES);
        analysisPage.addAttribute(ATTR_STAGE_NAME)
            .waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "https://jira.intgdc.com/browse/CL-6942")
    public void testCaseSensitiveSortInAttributeMetric() {
        initManagePage();
        String attribute = getMdService().getObjUri(getProject(), Attribute.class,
                Restriction.identifier("attr.product.id"));
        getMdService().createObj(getProject(), new Metric("aaaaA1", "SELECT COUNT([" + attribute + "])", "#,##0"));
        getMdService().createObj(getProject(), new Metric("AAAAb2", "SELECT COUNT([" + attribute + "])", "#,##0"));

        try {
            initAnalysePage();
            analysisPage.getCataloguePanel().search("aaaa");
            assertEquals(analysisPage.getCataloguePanel().getFieldNamesInViewPort(),
                    asList("aaaaA1", "AAAAb2"));
        } finally {
            deleteMetric("aaaaA1");
            deleteMetric("AAAAb2");
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkXssInMetricAttribute() {
        String xssAttribute = "<button>" + ATTR_IS_WON + "</button>";
        String xssMetric = "<button>" + METRIC_PERCENT_OF_GOAL + "</button>";

        initAttributePage()
            .initAttribute(ATTR_IS_WON)
            .changeName(xssAttribute);

        initMetricPage()
            .openMetricDetailPage(METRIC_PERCENT_OF_GOAL)
            .changeName(xssMetric);

        // It takes 3s to complete updated requests after saving
        // but next steps is initAnalysePage which load all attributes/metrics/facts immediately
        // so sometimes, the new name is not updated in Analyse page (see bug QA-6981).
        // Using the following wait to handle this bug
        Predicate<WebDriver> renameSuccess = browser -> !getMdService()
                .findUris(getProject(), Metric.class, title(xssMetric)).isEmpty();
        Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(renameSuccess);
        try {
            initAnalysePage();
            final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

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

            analysisPage.addMetric(xssMetric).addAttribute(xssAttribute)
                .waitForReportComputing();
            assertEquals(analysisPage.getMetricsBucket().getItemNames(), asList(xssMetric));
            assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(xssAttribute));
            assertTrue(analysisPage.getFilterBuckets().isFilterVisible(xssAttribute));
            assertEquals(analysisPage.getChartReport().getTooltipTextOnTrackerByIndex(0),
                    asList(asList(xssAttribute, "true"), asList(xssMetric, "1,160.9%")));
        } finally {
            initAttributePage()
                .initAttribute(xssAttribute)
                .changeName(ATTR_IS_WON);

            initMetricPage()
                .openMetricDetailPage(xssMetric)
                .changeName(METRIC_PERCENT_OF_GOAL);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testHiddenUnrelatedObjects() {
        final CataloguePanel cataloguePanel = analysisPage.getCataloguePanel();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addAttribute(ATTR_ACTIVITY_TYPE);
        assertTrue(cataloguePanel.search(""));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(21));

        assertThat(cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
            .getUnrelatedItemsHiddenCount(), equalTo(12));
        assertThat(cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getUnrelatedItemsHiddenCount(), equalTo(9));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.ALL)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(2));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.MEASURES)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(2));

        assertFalse(cataloguePanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .search("Amo"));
        assertThat(cataloguePanel.getUnrelatedItemsHiddenCount(), equalTo(0));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchNonExistent() {
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            assertFalse(analysisPage.getCataloguePanel().filterCatalog(type)
                    .search("abcxyz"));
            assertTrue(isElementPresent(ByJQuery.selector(
                    ".adi-no-items:contains('No data matching\"abcxyz\"')"), browser));
        });

        analysisPage.getCataloguePanel().filterCatalog(CatalogFilterType.ALL);
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            assertFalse(analysisPage.getCataloguePanel().filterCatalog(type).search("Am"));
            assertTrue(isElementPresent(ByJQuery.selector(
                    ".adi-no-items:contains('No data matching\"Am\"')"), browser));

            assertTrue(waitForElementVisible(className("s-unavailable-items-matched"), browser)
                    .getText().matches("^\\d unrelated data item[s]? hidden$"));
        });
    }

    private void deleteMetric(String metric) {
        initMetricPage()
            .openMetricDetailPage(metric)
            .deleteObject();
        assertFalse(MetricPage.getInstance(browser).isMetricVisible(metric));
    }
}
