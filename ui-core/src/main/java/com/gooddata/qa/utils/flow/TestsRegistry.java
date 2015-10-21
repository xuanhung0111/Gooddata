package com.gooddata.qa.utils.flow;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.gooddata.qa.graphene.AbstractTest;

public class TestsRegistry {

    public static final String TESTS_FLOW_FILE = "suites.txt";

    private List<String> tests;

    private TestsRegistry() {
        tests = new ArrayList<>();
    }

    public static TestsRegistry getInstance() {
        return new TestsRegistry();
    }

    public TestsRegistry register(String suite) {
        tests.add(suite);
        return this;
    }

    public TestsRegistry register(Class<? extends AbstractTest> testClass) {
        tests.add(testClass.toString().split(" ")[1]);
        return this;
    }

    public File toTextFile(File dir) throws IOException {
        File flowFile = new File(dir, TESTS_FLOW_FILE);

        try (FileWriter writer = new FileWriter(flowFile)) {
            writer.append(tests.stream().collect(joining(",")));
        }

        System.out.println(TESTS_FLOW_FILE + " path: " + flowFile.getAbsolutePath());
        System.out.println("Content: ");
        FileUtils.readLines(flowFile).stream().forEach(System.out::println);

        return flowFile;
    }

    public File toTextFile() throws IOException {
        return toTextFile(new File(System.getProperty("user.dir")));
    }
}
