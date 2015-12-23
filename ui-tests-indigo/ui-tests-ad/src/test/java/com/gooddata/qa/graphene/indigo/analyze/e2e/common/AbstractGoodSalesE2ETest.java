package com.gooddata.qa.graphene.indigo.analyze.e2e.common;

import static com.gooddata.md.Restriction.title;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;

public abstract class AbstractGoodSalesE2ETest extends AbstractAdE2ETest {

    protected String activitiesMetric;
    protected String lostOppsMetric;
    protected String quotaMetric;

    protected String activityTypeAttr;
    protected String activityTypeAttrLabel;
    protected String accountAttr;
    protected String accountAttrLabel;
    protected String departmentAttr;
    protected String departmentAttrLabel;
    protected String activityAttr;

    protected String amountFact;

    protected String yearActivityLabel;
    protected String monthYearActivityLabel;
    protected String quarterYearActivityLabel;

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
    }

    @Test(dependsOnGroups = {"setupProject"}, groups = {"turnOffWalkme"})
    public void loadDataForProject() {
        if (projectTemplate.isEmpty())
            throw new SkipException("Skip loading data because it's not GoodSales Demo");

        activitiesMetric = ".s-id-" + getMetricIdentifier(NUMBER_OF_ACTIVITIES);
        lostOppsMetric = ".s-id-" + getMetricIdentifier(NUMBER_OF_LOST_OPPS);
        quotaMetric = ".s-id-" + getMetricIdentifier(QUOTA);

        activityTypeAttr = ".s-id-" + getAttributeIdentifier(ACTIVITY_TYPE);
        activityTypeAttrLabel = ".s-id-" + getAttributeDisplayFormIdentifier(ACTIVITY_TYPE);

        accountAttr = ".s-id-" + getAttributeIdentifier(ACCOUNT);
        accountAttrLabel = ".s-id-" + getAttributeDisplayFormIdentifier(ACCOUNT);

        departmentAttr = ".s-id-" + getAttributeIdentifier(DEPARTMENT);
        departmentAttrLabel =  ".s-id-" + getAttributeDisplayFormIdentifier(DEPARTMENT);

        activityAttr = ".s-id-" + getAttributeIdentifier("Activity");

        amountFact = ".s-id-" + getFactIdentifier(AMOUNT);

        yearActivityLabel = ".s-id-" + getAttributeDisplayFormIdentifier("Year (Activity)");

        monthYearActivityLabel = ".s-id-" + getAttributeDisplayFormIdentifier("Month/Year (Activity)", "Short");

        quarterYearActivityLabel = ".s-id-" + getAttributeDisplayFormIdentifier("Quarter/Year (Activity)", "Short");
    }

    private String getMetricIdentifier(String title) {
        return getMdService().getObj(getProject(), Metric.class, title(title))
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }

    private String getFactIdentifier(String title) {
        return getMdService().getObj(getProject(), Fact.class, title(title))
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }

    private String getAttributeIdentifier(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title))
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }

    private String getAttributeDisplayFormIdentifier(String title) {
        return getMdService().getObj(getProject(), Attribute.class, title(title))
                .getDefaultDisplayForm()
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }

    private String getAttributeDisplayFormIdentifier(String title, final String partialSummary) {
        return getMdService().getObj(getProject(), Attribute.class, title(title))
                .getDisplayForms()
                .stream()
                .filter(form -> form.getSummary().contains(partialSummary))
                .findFirst()
                .get()
                .getIdentifier()
                .toLowerCase()
                .replace(".", "_");
    }
}
