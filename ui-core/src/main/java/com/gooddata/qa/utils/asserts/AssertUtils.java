package com.gooddata.qa.utils.asserts;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AssertUtils {

    //This method is used to compare Report Headers with expectation 
    //applying for insensitive case
    public static void assertHeadersEqual(List<String> actual, List<String> expected ) {
        for(int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i), equalToIgnoringCase(expected.get(i)));
        }
    }

    //This method is used to compare Report Headers with expectation 
    //applying for insensitive order and insensitive case 
    public static void assertHeadersEqual(Set<String> actual, Set<String> expected, String message) {
        if (actual.size() != expected.size())
            fail(message);
        Set<String> actualsort = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        Set<String> expectedsort = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        actualsort.addAll(actual);
        expectedsort.addAll(expected);
        assertTrue(actualsort.equals(expectedsort), message);
    }
}
