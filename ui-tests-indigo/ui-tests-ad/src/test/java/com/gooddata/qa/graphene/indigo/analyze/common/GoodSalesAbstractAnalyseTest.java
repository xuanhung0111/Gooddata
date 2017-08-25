package com.gooddata.qa.graphene.indigo.analyze.common;

import com.gooddata.fixture.ResourceManagement.ResourceTemplate;
import org.testng.annotations.BeforeClass;

public abstract class GoodSalesAbstractAnalyseTest extends AbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initProperties() {
        super.initProperties();
        appliedFixture = ResourceTemplate.GOODSALES;
        projectTitle += appliedFixture.getPath() + "-";
    }

    @Override
    public void prepareSetupProject() throws Throwable {
        // do nothing because we're in GoodSales project
    }
}
