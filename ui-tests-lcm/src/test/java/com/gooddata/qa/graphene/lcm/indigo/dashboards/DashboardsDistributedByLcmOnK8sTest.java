package com.gooddata.qa.graphene.lcm.indigo.dashboards;

/**
 * Use k8s executor to execute lcm process
 * Extension is not good way to do
 * TODO: should use xml suite with proper input parameter instead but this also require change at ci-infra
 */
public class DashboardsDistributedByLcmOnK8sTest extends DashboardsDistributedByLcmTest {

    @Override
    protected void initProperties() {
        super.initProperties();
        useK8sExecutor = true;
    }
}
