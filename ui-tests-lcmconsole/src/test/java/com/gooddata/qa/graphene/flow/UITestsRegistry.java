package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.lcmconsole.tests.DataproductDetailTest;
import com.gooddata.qa.graphene.lcmconsole.tests.DataproductsTest;
import com.gooddata.qa.graphene.lcmconsole.tests.DomainsTest;
import com.gooddata.qa.graphene.lcmconsole.tests.SegmentDetailTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
                DataproductsTest.class,
                DomainsTest.class,
                DataproductDetailTest.class,
                SegmentDetailTest.class
        });

        TestsRegistry.getInstance()
                .register(args, suites)
                .toTextFile();
    }
}
