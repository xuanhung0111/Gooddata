package com.gooddata.qa.utils.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.AbstractTest;

/**
 * A wrapper for test class or test suite in flow.
 * Provide ability to define parameter which is used to override the one in flow
 */
public class GdcTest {

    private Class<? extends AbstractTest> clazz;
    private String suite;
    private Map<String, String> predefinedParams;

    public GdcTest(Class<? extends AbstractTest> clazz) {
        this.clazz = clazz;
        predefinedParams = new HashMap<>();
    }

    public GdcTest(String suite) {
        this.suite = suite;
        predefinedParams = new HashMap<>();
    }

    public GdcTest param(String key, String value) {
        predefinedParams.put(key, value);
        return this;
    }

    public Class<? extends AbstractTest> getClazz() {
        return clazz;
    }

    public String getSuite() {
        return suite;
    }

    public Map<String, String> getPredefinedParams() {
        return predefinedParams;
    }
}
