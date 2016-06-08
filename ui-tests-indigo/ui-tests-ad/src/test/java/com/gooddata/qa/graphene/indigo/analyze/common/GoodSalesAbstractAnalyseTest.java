package com.gooddata.qa.graphene.indigo.analyze.common;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.GOODSALES_TEMPLATE;

import org.testng.annotations.BeforeClass;

public abstract class GoodSalesAbstractAnalyseTest extends AbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        projectTemplate = GOODSALES_TEMPLATE;
        projectTitle += "GoodSales-";
    }

    @Override
    public void prepareSetupProject() throws Throwable {
        // do nothing because we're in GoodSales project
    }
}
