package com.gooddata.qa.graphene.flow;
import com.gooddata.qa.graphene.snowflake.SegmentForceLoadTest;
import com.gooddata.qa.graphene.snowflake.SegmentLoadTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
                SegmentForceLoadTest.class,
                SegmentLoadTest.class
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
