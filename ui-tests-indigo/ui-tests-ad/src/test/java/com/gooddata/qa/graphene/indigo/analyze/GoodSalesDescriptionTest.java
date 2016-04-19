package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;

public class GoodSalesDescriptionTest extends AnalyticalDesignerAbstractTest {

    private static final String ACTIVITY_DATE = "Activity (Date)";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Description-Test";
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
    public void testMetricWithIdentifier(String name, String maql, String identifier) {
        String factAmountUri = getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT));

        String accountAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title(ACCOUNT));
        String accountValueUri1 = accountAttributeUri + "/elements?id=958075";
        String accountValueUri2 = accountAttributeUri + "/elements?id=958077";

        final String completedMaql = maql.replace("${factUri}", factAmountUri)
                .replace("${attributeValueUri1}", accountValueUri1)
                .replace("${attributeValueUri2}", accountValueUri2)
                .replaceAll("\\$.*Identifier\\}", identifier);

        createMetric(name, completedMaql, "#,##0");

        String metricDescription = analysisPage.getCataloguePanel().getMetricDescription(name);

        takeScreenshot(browser, "Metric with identifier " + name, getClass());
        assertThat(metricDescription, containsString(identifier));
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreDate() {
        StringBuilder expected = new StringBuilder(DATE).append("\n")
                .append("Represents all your dates in project. Can group by Day, Week, Month, Quarter & Year.\n")
                .append("Field Type\n")
                .append("Date\n");
        assertEquals(analysisPage.getCataloguePanel().getDateDescription(), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreAttribute() {
        StringBuilder expected = new StringBuilder(DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(analysisPage.getCataloguePanel().getAttributeDescription(DEPARTMENT), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreMetric() {
        StringBuilder expected = new StringBuilder(NUMBER_OF_ACTIVITIES).append("\n")
                .append("Field Type\n")
                .append("Calculated Measure\n")
                .append("Defined As\n")
                .append("SELECT COUNT(Activity)\n");
        assertEquals(analysisPage.getCataloguePanel().getMetricDescription(NUMBER_OF_ACTIVITIES), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreAttributeInMetricFilter() {
        analysisPage.addMetric(NUMBER_OF_ACTIVITIES);
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.canAddAnotherFilter());

        StringBuilder expected = new StringBuilder(DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(metricConfiguration.getAttributeDescription(DEPARTMENT),
                expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreFact() {
        StringBuilder expected = new StringBuilder(ACTIVITY_DATE).append("\n")
                .append("Field Type\n")
                .append("Measure\n")
                .append("Dataset\n")
                .append("Activity\n");
        assertEquals(analysisPage.getCataloguePanel().getFactDescription(ACTIVITY_DATE), expected.toString());
    }
}
