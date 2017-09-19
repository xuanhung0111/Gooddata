package com.gooddata.qa.utils.flow;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.gooddata.qa.graphene.AbstractTest;

public class TestsRegistry {

    public static final String TESTS_REGISTRY_FILE = "tests_registry.txt";

    private List<Set<String>> testPhases;

    private TestsRegistry() {
        testPhases = new ArrayList<>();
    }

    public static TestsRegistry getInstance() {
        return new TestsRegistry();
    }

    @SuppressWarnings("unchecked")
    public TestsRegistry register(String[] expectedSuites, Map<String, Object> suites) {
        for (String suite: expectedSuites) {
            Object tests = suites.get(suite);

            if (tests instanceof Map) {
                registerTests((Map<String, Object[]>) tests);
            } else {
                registerTests((Object[]) tests);
            }
        }
        return this;
    }

    /**
     * Build tests_registry.txt file, the value of file is a json that contains all test phases.
     * The phase name defined in UITestRegistry is skipped.
     * Each phase is defined in as following [TestA, TestB, TestC].
     * Many phases are separated by comma.
     * If any test is duplicated in test phases, it will be removed in priority from the first and keep in the next phase.
     * Json format: 
     *  + For one phase in a suite : {phases: [[testA, testB, ...]]}
     *  + For many phases in a suite : {phases: [[testA, testB, ...], [testC, testD, ...], ...]}
     * 
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public File toTextFile() throws IOException, JSONException {
        return toTextFile(new File(System.getProperty("user.dir")));
    }

    private File toTextFile(File dir) throws IOException, JSONException {
        File flowFile = new File(dir, TESTS_REGISTRY_FILE);

        try (FileWriter writer = new FileWriter(flowFile)) {
            writer.append("TESTS_REGISTRY=")
                .append(new JSONObject() {{
                    put("phases", removeDuplicatedTests(testPhases));
                }}.toString());
        }

        System.out.println(TESTS_REGISTRY_FILE + " path: " + flowFile.getAbsolutePath());
        System.out.println("Content: ");
        FileUtils.readLines(flowFile).stream().forEach(System.out::println);

        return flowFile;
    }

    private void registerTests(Map<String, Object[]> phases) {
        for (String phase : phases.keySet()) {
            registerTests(phases.get(phase));
        }
    }

    private void registerTests(Object[] tests) {
        testPhases.add(parseTests(tests));
    }

    // The implementation of this method change from using lambda to loop due to
    // https://bugs.openjdk.java.net/browse/JDK-8145964 (sometimes the NoClassDefFoundError exception thrown)
    // We will have a refactor when the migration of JDK 9 is done
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Set<String> parseTests(Object[] tests) {
        Set<String> listAfterParsing = new HashSet<>();

        for (int i = 0; i < tests.length; i++) {
            if (tests[i] instanceof Class) {
                listAfterParsing.add(parseTest((Class) tests[i]));
            } else if (tests[i] instanceof PredefineParameterTest) {
                listAfterParsing.add(parseTest((PredefineParameterTest) tests[i]));
            } else {
                listAfterParsing.add(parseTest((String) tests[i]));
            }
        }
        return listAfterParsing;
    }

    private String parseTest(String testSuite) {
        return testSuite;
    }

    private String parseTest(Class<? extends AbstractTest> testClass) {
        return testClass.getSimpleName();
    }

    private String parseTest(PredefineParameterTest test) {
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

        return clazz + "->" + params.toString();
    }

    private List<Set<String>> removeDuplicatedTests(List<Set<String>> testPhases) {
        for (Set<String> testPhase : testPhases) {
            Set<String> duplicatedList = testPhases.stream()
                    .filter(p -> !p.equals(testPhase))
                    .flatMap(p -> p.stream())
                    .collect(toSet());

            for (String test : duplicatedList) {
                if (testPhase.contains(test)) {
                    testPhase.remove(test);
                }
            }
        }
        return testPhases.stream()
                .filter(phase -> !phase.isEmpty())
                .collect(toList());
    }
}
