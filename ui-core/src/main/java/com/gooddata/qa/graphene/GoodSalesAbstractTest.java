package com.gooddata.qa.graphene;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.fixture.utils.GoodSales.Reports;
import com.gooddata.qa.fixture.utils.GoodSales.Variables;
import com.gooddata.qa.utils.http.RestClient;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.GOODSALES;

public class GoodSalesAbstractTest extends AbstractProjectTest {

    @Override
    protected void initProperties() {
        projectTitle = "GoodSales ";
        appliedFixture = GOODSALES;
    }

    protected Metrics getMetricCreator() {
        return new Metrics(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    protected Reports getReportCreator() {
        return new Reports(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }

    protected Variables getVariableCreator() {
        return new Variables(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
    }
}