package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.stream.Stream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class CatalogueFilterTest extends AbstractAdE2ETest {

    private String metrics = ".type-metric";
    private String attributes = ".type-attribute";
    private String facts = ".type-fact";
    private String dates = ".type-date";
    private String header = ".adi-catalogue-header";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Catalogue-Filter-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void shows_all_items_for_all_data_filter() {
        initAnalysePageByUrl();

        analysisPage.getCataloguePanel().filterCatalog(CatalogFilterType.ALL);
        expectVisible(dates, attributes, metrics, facts, header);
    }

    @Test(dependsOnGroups = {"init"})
    public void shows_only_metrics_and_facts_for_metrics_filter() {
        initAnalysePageByUrl();

        analysisPage.getCataloguePanel().filterCatalog(CatalogFilterType.MEASURES);
        expectVisible(metrics, facts, header);
        expectHidden(dates, attributes);
    }

    @Test(dependsOnGroups = {"init"})
    public void shows_only_date_and_attributes_for_attributes_filter() {
        initAnalysePageByUrl();

        analysisPage.getCataloguePanel().filterCatalog(CatalogFilterType.ATTRIBUTES);
        expectVisible(dates, attributes, header);
        expectHidden(metrics, facts);
    }

    private void expectVisible(String... fields) {
        Stream.of(fields).forEach(field ->
            assertTrue(isElementPresent(cssSelector(".s-catalogue " + field), browser)));
    }

    private void expectHidden(String... fields) {
        Stream.of(fields).forEach(field ->
            assertFalse(isElementPresent(cssSelector(".s-catalogue " + field), browser)));
    }
}