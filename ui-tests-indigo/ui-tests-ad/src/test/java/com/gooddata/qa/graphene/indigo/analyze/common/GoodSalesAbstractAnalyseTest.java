package com.gooddata.qa.graphene.indigo.analyze.common;

import org.testng.annotations.BeforeClass;

public abstract class GoodSalesAbstractAnalyseTest extends AbstractAnalyseTest {

    protected static final String NUMBER_OF_ACTIVITIES = "# of Activities";
    protected static final String NUMBER_OF_LOST_OPPS = "# of Lost Opps.";
    protected static final String NUMBER_OF_OPEN_OPPS = "# of Open Opps.";
    protected static final String NUMBER_OF_OPPORTUNITIES = "# of Opportunities";
    protected static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    protected static final String SNAPSHOT_BOP = "_Snapshot [BOP]";
    protected static final String PERCENT_OF_GOAL = "% of Goal";
    protected static final String IS_WON = "Is Won?";
    protected static final String QUOTA = "Quota";
    protected static final String PRODUCT = "Product";
    protected static final String ACTIVITY_TYPE = "Activity Type";
    protected static final String AMOUNT = "Amount";
    protected static final String STAGE_NAME = "Stage Name";
    protected static final String ACCOUNT = "Account";
    protected static final String DEPARTMENT = "Department";

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = "/projectTemplates/GoodSalesDemo/2";
        projectTitle += "GoodSales-";
    }

    @Override
    public void prepareSetupProject() throws Throwable {
        // do nothing because we're in GoodSales project
    }
}
