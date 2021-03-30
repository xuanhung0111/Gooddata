package com.gooddata.qa.graphene.flow;
import com.gooddata.qa.graphene.enterprise.BasicEnterpriseTest;
import com.gooddata.qa.graphene.freemium.BasicFreemiumTest;
import com.gooddata.qa.graphene.growth.BasicGrowthTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("freemium", new Object[] {
//                BasicFreemiumTest.class // will enable after QA-12463
        });

        suites.put("growth", new Object[]{
                BasicGrowthTest.class
        });

        suites.put("enterprise", new Object[]{
                BasicEnterpriseTest.class
        });

        suites.put("all", new HashMap<String, Object>() {{
            put("freemium", suites.get("freemium"));
            put("growth", suites.get("growth"));

        }});

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
