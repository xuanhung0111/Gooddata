package com.gooddata.qa.graphene.indigo.analyze.e2e;

import java.util.stream.Stream;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractGoodSalesE2ETest;

public class CatalogueFilterTest extends AbstractGoodSalesE2ETest {

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
        visitEditor();

        click(".s-catalogue .s-filter-all");
        expectVisible(dates, attributes, metrics, facts, header);
    }

    @Test(dependsOnGroups = {"init"})
    public void shows_only_metrics_and_facts_for_metrics_filter() {
        visitEditor();

        click(".s-catalogue .s-filter-metrics");
        expectVisible(metrics, facts, header);
        expectHidden(dates, attributes);
    }

    @Test(dependsOnGroups = {"init"})
    public void shows_only_date_and_attributes_for_attributes_filter() {
        visitEditor();

        click(".s-catalogue .s-filter-attributes");
        expectVisible(dates, attributes, header);
        expectHidden(metrics, facts);
    }

    private void expectVisible(String... fields) {
        Stream.of(fields).forEach(field -> expectFind(".s-catalogue " + field));
    }

    private void expectHidden(String... fields) {
        Stream.of(fields).forEach(field -> expectMissing(".s-catalogue " + field));
    }
}