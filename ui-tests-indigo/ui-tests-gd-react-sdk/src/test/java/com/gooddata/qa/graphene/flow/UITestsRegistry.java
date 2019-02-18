package com.gooddata.qa.graphene.flow;

import com.gooddata.qa.graphene.indigo.sdk.EditorPermissionTest;
import com.gooddata.qa.graphene.indigo.sdk.EmbeddedHeadlineTest;
import com.gooddata.qa.graphene.indigo.sdk.HeadlineByBucketComponentTest;
import com.gooddata.qa.graphene.indigo.sdk.EmbeddedTreemapTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

import java.util.HashMap;
import java.util.Map;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("sanity", new Object[] {
                EmbeddedHeadlineTest.class,
                HeadlineByBucketComponentTest.class,
                EmbeddedTreemapTest.class,
                EditorPermissionTest.class
        });

        TestsRegistry.getInstance()
                .register(args, suites)
                .toTextFile();
    }
}
