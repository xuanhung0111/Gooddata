package com.gooddata.qa.utils.asserts;

import java.util.Collection;

import static java.util.stream.Collectors.toList;
import static org.testng.Assert.fail;

public class AssertUtils {

    /**
     * This method is used to compare collections applying for insensitive case
     * @param actual
     * @param expected
     */
    public static void assertIgnoreCase(Collection<String> actual, Collection<String> expected) {
        String[] actualElements = actual.toArray(new String[actual.size()]);
        String[] expectedElements = expected.toArray(new String[expected.size()]);

        if (actualElements.length != expectedElements.length) {
            throw new IllegalStateException("Compared collections do not have the same size!");
        }

        boolean result = true;

        for (int i = 0; i < actualElements.length; i++) {
            if (!actualElements[i].equalsIgnoreCase(expectedElements[i])) {
                result = false;
                break;
            }
        }

        if (!result) fail("Expected " + expected + " but found " + actual);
    }

    /**
     * This method is used to compare collections applying for insensitive order and insensitive case
     * @param actual
     * @param expected
     */
    public static void assertIgnoreCaseAndIndex(Collection<String> actual, Collection<String> expected) {
        Collection<String> actualElements = actual.stream().sorted().collect(toList());
        Collection<String> expectedElements = expected.stream().sorted().collect(toList());
        assertIgnoreCase(actualElements, expectedElements);
    }
}
