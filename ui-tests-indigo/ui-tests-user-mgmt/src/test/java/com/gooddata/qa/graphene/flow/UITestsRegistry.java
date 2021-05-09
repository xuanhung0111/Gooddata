package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity-prod", new Object[] {
            "testng-imap-user-mgmt-sanity.xml"
        });
        suites.put("all", new Object[] {
            "testng-imap-user-mgmt-general.xml"
        });

        suites.put("sanity-aws", new Object[]{
                "testng-imap-user-mgmt-sanity.xml"
        });

        suites.put("sanity", new HashMap<String, Object>() {{
            put("sanity-aws", suites.get("sanity-aws"));
        }});

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
