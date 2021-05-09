package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.sdk.FilterComponentTest;
import com.gooddata.qa.graphene.indigo.sdk.EditorPermissionTest;
import com.gooddata.qa.graphene.indigo.sdk.EmbeddedHeadlineTest;
import com.gooddata.qa.graphene.indigo.sdk.FilterTest;
import com.gooddata.qa.graphene.indigo.sdk.HeadlineByBucketComponentTest;
import com.gooddata.qa.graphene.indigo.sdk.EmbeddedTreemapTest;
import com.gooddata.qa.graphene.indigo.sdk.VisualizationTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity-prod", new Object[] {
            FilterTest.class,
            VisualizationTest.class
        });

        suites.put("sanity-aws", new Object[] {
                EditorPermissionTest.class,
                FilterComponentTest.class,
                FilterTest.class
        });

        suites.put("sanity", new HashMap<String, Object>() {{
            put("sanity-aws", suites.get("sanity-aws"));
            put("sanity-extend", new Object[]{
                    EmbeddedHeadlineTest.class,
                    HeadlineByBucketComponentTest.class,
                    EmbeddedTreemapTest.class,
                    VisualizationTest.class
            });
        }});

        suites.put("boilerplate", new Object[] {
                VisualizationTest.class
        });

        suites.put("extended", new Object[] {
        });

        suites.put("all", new HashMap<String, Object>() {{
            put("sanity", suites.get("sanity"));
            put("extended", suites.get("extended"));
        }});

        TestsRegistry.getInstance()
                .register(args, suites)
                .toTextFile();
    }
}
