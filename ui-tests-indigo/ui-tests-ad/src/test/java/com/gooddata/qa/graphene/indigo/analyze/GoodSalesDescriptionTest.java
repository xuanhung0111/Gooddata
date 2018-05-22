package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_STATUS;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;

import java.util.HashMap;
import java.util.List;

public class GoodSalesDescriptionTest extends AbstractAnalyseTest {

    private static final String ACTIVITY_DATE = "Activity (Date)";
    private HashMap<String, String> identifiersMap = new HashMap<>();

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Description-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();

        List<String> ids = getObjIdentifiers(asList(getMetricCreator().createAmountMetric().getUri(),
                getVariableCreator().createStatusVariable()));

        identifiersMap.put(ATTR_ACCOUNT, "attr.account.id");
        identifiersMap.put(FACT_AMOUNT, "attr.account.id");
        identifiersMap.put(ATTR_YEAR_SNAPSHOT, "attr.account.id");
        identifiersMap.put(METRIC_AMOUNT, ids.get(0));
        identifiersMap.put(VARIABLE_STATUS, ids.get(1));
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
            {"attrIdentifier", maql1, identifiersMap.get(ATTR_ACCOUNT)}, // Identifier of attribute account
            {"factIdentifier", maql2, identifiersMap.get(FACT_AMOUNT)}, // Identifier of fact amount
            {"dateIdentifier", maql3, identifiersMap.get(ATTR_YEAR_SNAPSHOT)}, // Identifier of date Year(snapshot)
            {"variableIdentifier", maql4, identifiersMap.get(VARIABLE_STATUS)}, // Identifier of variable Status
            {"metricIdentifier", maql5, identifiersMap.get(METRIC_AMOUNT)} // Identifier of metric amount
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "metricProvider")
    public void testMetricWithIdentifier(String name, String maql, String identifier) {
        String factAmountUri = getMdService().getObjUri(getProject(), Fact.class, title(FACT_AMOUNT));

        String accountAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title(ATTR_ACCOUNT));
        String accountValueUri1 = accountAttributeUri + "/elements?id=958075";
        String accountValueUri2 = accountAttributeUri + "/elements?id=958077";

        final String completedMaql = maql.replace("${factUri}", factAmountUri)
                .replace("${attributeValueUri1}", accountValueUri1)
                .replace("${attributeValueUri2}", accountValueUri2)
                .replaceAll("\\$.*Identifier\\}", identifier);

        createMetric(name, completedMaql, "#,##0");

        String metricDescription = initAnalysePage().getCataloguePanel().getMetricDescription(name);

        takeScreenshot(browser, "Metric with identifier " + name, getClass());
        assertThat(metricDescription, containsString(identifier));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exploreDate() {
        StringBuilder expected = new StringBuilder(DATE).append("\n")
                .append("Represents all your dates in project. Can group by Day, Week, Month, Quarter & Year.\n")
                .append("Field Type\n")
                .append("Date\n");
        assertEquals(initAnalysePage().getCataloguePanel().getDateDescription(), expected.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exploreAttribute() {
        StringBuilder expected = new StringBuilder(ATTR_DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(initAnalysePage().getCataloguePanel().getAttributeDescription(ATTR_DEPARTMENT), expected.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exploreMetric() {
        StringBuilder expected = new StringBuilder(METRIC_NUMBER_OF_ACTIVITIES).append("\n")
                .append("Field Type\n")
                .append("Calculated Measure\n")
                .append("Defined As\n")
                .append("SELECT COUNT(Activity)\n");
        assertEquals(initAnalysePage().getCataloguePanel().getMetricDescription(METRIC_NUMBER_OF_ACTIVITIES), expected.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exploreAttributeInMetricFilter() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        final MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.canAddAnotherFilter());

        StringBuilder expected = new StringBuilder(ATTR_DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(metricConfiguration.getAttributeDescription(ATTR_DEPARTMENT),
                expected.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exploreFact() {
        StringBuilder expected = new StringBuilder(ACTIVITY_DATE).append("\n")
                .append("Field Type\n")
                .append("Measure\n")
                .append("Dataset\n")
                .append("Activity\n");
        assertEquals(initAnalysePage().getCataloguePanel().getFactDescription(ACTIVITY_DATE), expected.toString());
    }
}
