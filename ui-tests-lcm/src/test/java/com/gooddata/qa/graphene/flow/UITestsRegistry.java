package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.lcm.indigo.dashboards.DashboardsDistributedByLcmOnK8sTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.DashboardsDistributedByLcmTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.MsfProcessOnK8sTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.MsfProcessTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.OrganisingCatalogueUsingClientIdTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.RenderingInsightUsingClientIdTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.RenderingAnalyticalDashboardUsingClientIdTest;
import com.gooddata.qa.graphene.lcm.dashboards.EmbeddedDashboardUsingClientIdTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[]{
                DashboardsDistributedByLcmTest.class,
                DashboardsDistributedByLcmOnK8sTest.class,
                MsfProcessTest.class,
                MsfProcessOnK8sTest.class,
                RenderingInsightUsingClientIdTest.class,
                RenderingAnalyticalDashboardUsingClientIdTest.class,
                OrganisingCatalogueUsingClientIdTest.class,
                EmbeddedDashboardUsingClientIdTest.class
        });

        TestsRegistry.getInstance()
                .register(args, suites)
                .toTextFile();
    }
}
