package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.lcm.indigo.dashboards.DashboardsDistributedByLcmTest;
import com.gooddata.qa.graphene.lcm.indigo.dashboards.MsfProcessTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
            DashboardsDistributedByLcmTest.class,
            MsfProcessTest.class
        });

        TestsRegistry.getInstance()
                .register(args, suites)
                .toTextFile();
    }
}
