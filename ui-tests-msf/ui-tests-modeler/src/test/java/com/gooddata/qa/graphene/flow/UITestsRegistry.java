package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity", new Object[]{
                "testng-msf-web-modeler-ii.xml"
        });

        suites.put("extend", new Object[]{
                "testng-msf-web-modeler-extend.xml"
        });

        suites.put("sanity-aws", new Object[]{
                "testng-msf-web-modeler-sanity-aws.xml"
        });

        suites.put("all", new HashMap<String, Object>() {{
            put("sanity", suites.get("sanity"));
            put("extend", suites.get("extend"));
        }
        });

        TestsRegistry.getInstance()
                .register(args, suites)
                .toTextFile();
    }
}
