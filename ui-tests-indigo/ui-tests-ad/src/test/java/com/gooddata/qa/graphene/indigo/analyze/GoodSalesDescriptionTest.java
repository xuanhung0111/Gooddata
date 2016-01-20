package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;

public class GoodSalesDescriptionTest extends AnalyticalDesignerAbstractTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Metric-With-Identifier-Test";
    }

    @DataProvider(name = "metricProvider")
    public Object[][] metricProvider() {
        String maql1 = "SELECT SUM([${factUri}]) WHERE {${attributeIdentifier}}"
                + " IN ([${attributeValueUri1}], [${attributeValueUri2}])";

        String maql2 = "SELECT SUM({${factIdentifier}})";
        String maql3 = "SELECT AVG([${factUri}]) WHERE {${dateIdentifier}} > 2011";
        String maql4 = "SELECT SUM([${factUri}]) WHERE {${variableIdentifier}}";
        String maql5 = "SELECT {${otherMetricIdentifier}}";

        return new Object[][] {
            {"attrIdentifier", maql1, "attr.account.id"}, // Identifier of attribute account 
            {"factIdentifier", maql2, "fact.opportunitysnapshot.amount"}, // Identifier of fact amount
            {"dateIdentifier", maql3, "snapshot.year"}, // Identifier of date Year(snapshot)
            {"variableIdentifier", maql4, "ae1zQEAYe8gT"}, // Identifier of variable Status 
            {"metricIdentifier", maql5, "ah1EuQxwaCqs"} // Identifier of metric amount
        };
    }

    @Test(dependsOnGroups = {"init"}, dataProvider = "metricProvider")
    public void createMetricWithIdentifier(String name, String maql, String identifier) {
        String factAmountUri = getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));

        String accountAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title(ACCOUNT));
        String accountValueUri1 = accountAttributeUri + "/elements?id=958075";
        String accountValueUri2 = accountAttributeUri + "/elements?id=958077";

        final String completedMaql = maql.replace("${factUri}", factAmountUri)
                .replace("${attributeValueUri1}", accountValueUri1)
                .replace("${attributeValueUri2}", accountValueUri2)
                .replaceAll("\\$.*Identifier\\}", identifier);

        createMetric(name, completedMaql, "#,##0");

        initAnalysePage();
        String metricDescription = analysisPage.getCataloguePanel().getMetricDescription(name);

        takeScreenshot(browser, "Metric with identifier " + name, getClass());
        assertThat(metricDescription, containsString(identifier));
    }
}
