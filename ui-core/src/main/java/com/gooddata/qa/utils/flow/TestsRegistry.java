package com.gooddata.qa.utils.flow;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.gooddata.qa.graphene.AbstractTest;

public class TestsRegistry {

    public static final String TESTS_REGISTRY_FILE = "tests_registry.txt";

    private List<String> tests;

    private TestsRegistry() {
        tests = new ArrayList<>();
    }

    public static TestsRegistry getInstance() {
        return new TestsRegistry();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public TestsRegistry register(Collection<Object> tests) {
        tests.stream().forEach(test -> {
            if (test instanceof GdcTest) {
                register((GdcTest) test);
            } else if (test instanceof Class) {
                register((Class) test);
            } else {
                register((String) test);
            }
        });
        return this;
    }

    public TestsRegistry register(String suite) {
        tests.add(suite);
        return this;
    }

    public TestsRegistry register(Class<? extends AbstractTest> testClass) {
        tests.add(testClass.getSimpleName());
        return this;
    }

    public TestsRegistry register(GdcTest test) {
        String clazz = test.getSuite();
        if (clazz == null || clazz.isEmpty()) {
            clazz = test.getClazz().getSimpleName();
        }

        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String> entry: test.getPredefinedParams().entrySet()) {
            if (params.length() > 0) {
                params.append("&&&");
            }

            params.append(entry.getKey())
                .append("=")
                .append(entry.getValue());
        }

        tests.add(clazz + "->" + params.toString());
        return this;
    }

    public File toTextFile(File dir) throws IOException {
        File flowFile = new File(dir, TESTS_REGISTRY_FILE);

        try (FileWriter writer = new FileWriter(flowFile)) {
            writer.append("TESTS_REGISTRY=")
                .append(tests.stream().collect(joining(",")));
        }

        System.out.println(TESTS_REGISTRY_FILE + " path: " + flowFile.getAbsolutePath());
        System.out.println("Content: ");
        FileUtils.readLines(flowFile).stream().forEach(System.out::println);

        return flowFile;
    }

    public File toTextFile() throws IOException {
        return toTextFile(new File(System.getProperty("user.dir")));
    }
}
