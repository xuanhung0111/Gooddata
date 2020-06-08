package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.md.Restriction;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CatalogPanel;
import com.gooddata.qa.graphene.fragments.manage.MetricPage;

public class GoodSalesCatalogueTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Catalogue-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        getMetricCreator().createPercentOfGoalMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testFilteringFieldsInCatalog() {
        final CatalogPanel catalogPanel = initAnalysePage().getCatalogPanel();

        catalogPanel.filterCatalog(CatalogFilterType.MEASURES)
            .search("am");

        assertTrue(catalogPanel.getFieldsInViewPort()
            .stream()
            .allMatch(input -> {
                final String cssClass = input.getAttribute("class");
                return cssClass.contains(FieldType.METRIC.toString()) ||
                        cssClass.contains(FieldType.FACT.toString());
            }), "Catalogue panel should contain metric and fact");

        catalogPanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
            .search("am");

        assertTrue(
            catalogPanel.getFieldsInViewPort()
                .stream()
                .allMatch(input -> input.getAttribute("class").contains(FieldType.ATTRIBUTE.toString())),
            "Catalogue panel should contain metric and fact");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testCreateReportWithFieldsInCatalogFilter() {
        final CatalogPanel catalogPanel = initAnalysePage().getCatalogPanel();

        catalogPanel.filterCatalog(CatalogFilterType.MEASURES);
        analysisPage.addMetric(METRIC_AMOUNT);
        catalogPanel.filterCatalog(CatalogFilterType.ATTRIBUTES);
        analysisPage.addAttribute(ATTR_STAGE_NAME)
            .waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1,
                "Number of Trackers should be greater or equal 1");
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
            analysisPage.getCatalogPanel().search("aaaa");
            assertEquals(analysisPage.getCatalogPanel().getFieldNamesInViewPort(),
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
        Function<WebDriver, Boolean> renameSuccess = browser -> !getMdService()
                .findUris(getProject(), Metric.class, title(xssMetric)).isEmpty();
        Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(renameSuccess);
        try {
            initAnalysePage();
            final CatalogPanel catalogPanel = analysisPage.getCatalogPanel();

            catalogPanel.search("<button> test XSS </button>");
            assertTrue(catalogPanel.isEmpty(), "Catalogue Panel should be empty");
            catalogPanel.search("<script> alert('test'); </script>");
            assertTrue(catalogPanel.isEmpty(), "Catalogue Panel should be empty");
            catalogPanel.search("<button>");
            assertEquals(catalogPanel.getFieldNamesInViewPort(), asList(xssMetric, xssAttribute));

            StringBuilder expected = new StringBuilder(xssMetric).append("\n")
                    .append("Field Type\n")
                    .append("Calculated Measure\n")
                    .append("Defined As\n")
                    .append("select Won/Quota\n");
            assertEquals(catalogPanel.getMetricDescription(xssMetric), expected.toString());

            expected = new StringBuilder(xssAttribute).append("\n")
                    .append("Field Type\n")
                    .append("Attribute\n")
                    .append("Values\n")
                    .append("false\n")
                    .append("true\n");
            assertEquals(catalogPanel.getAttributeDescription(xssAttribute), expected.toString());

            analysisPage.addMetric(xssMetric).addAttribute(xssAttribute)
                .waitForReportComputing();
            assertEquals(analysisPage.getMetricsBucket().getItemNames(), singletonList(xssMetric));
            assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(xssAttribute));
            assertTrue(analysisPage.getFilterBuckets().isFilterVisible(xssAttribute),
                    xssAttribute + "filter should display");
            assertEquals(analysisPage.getChartReport().getTooltipTextOnTrackerByIndex(0, 0),
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
        final CatalogPanel catalogPanel = initAnalysePage()
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .getCatalogPanel()
                .clearInputText();
        assertThat(catalogPanel.getUnrelatedItemsHiddenCount(), equalTo(21));

        assertThat(catalogPanel.filterCatalog(CatalogFilterType.MEASURES)
            .getUnrelatedItemsHiddenCount(), equalTo(12));
        assertThat(catalogPanel.filterCatalog(CatalogFilterType.ATTRIBUTES)
                .getUnrelatedItemsHiddenCount(), equalTo(9));

        catalogPanel.filterCatalog(CatalogFilterType.ALL).search("Amo");
        assertThat(catalogPanel.getUnrelatedItemsHiddenCount(), equalTo(2));

        catalogPanel.filterCatalog(CatalogFilterType.MEASURES).search("Amo");
        assertThat(catalogPanel.getUnrelatedItemsHiddenCount(), equalTo(2));

        catalogPanel.filterCatalog(CatalogFilterType.ATTRIBUTES).search("Amo");
        assertThat(catalogPanel.getUnrelatedItemsHiddenCount(), equalTo(0));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchNonExistent() {
        CatalogPanel catalogPanel = initAnalysePage().getCatalogPanel();

        Stream.of(CatalogFilterType.values()).forEach(type -> {
            catalogPanel.filterCatalog(type).search("abcxyz");
            assertTrue(catalogPanel.isEmpty(), "Catalogue panel should be empty");
            assertEquals(catalogPanel.getEmptyMessage(), "No data matching\n\"abcxyz\"");
        });

        catalogPanel.filterCatalog(CatalogFilterType.ALL);
        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE);
        Stream.of(CatalogFilterType.values()).forEach(type -> {
            catalogPanel.filterCatalog(type).search("Am");
            assertTrue(catalogPanel.isEmpty(), "Catalogue panel should be empty");

            assertTrue(waitForElementVisible(className("s-unavailable-items-matched"), browser)
                    .getText().matches("^\\d unrelated data item[s]? hidden$"), "Be wrong format text");
        });
    }

    private void deleteMetric(String metric) {
        initMetricPage()
            .openMetricDetailPage(metric)
            .deleteObject();
        assertFalse(MetricPage.getInstance(browser).isMetricVisible(metric), "Deleted metric shouldn't be visible");
    }
}
