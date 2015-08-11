package com.gooddata.qa.utils.io;

import org.testng.annotations.Test;

import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static org.testng.Assert.assertEquals;

public class ResourceUtilsTest {

    @Test
    public void successfulGetResourceAsString() throws Exception {
        assertEquals(getResourceAsString("/test.json"), "{\"name\":\"value\"}");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void unknownGetResourceAsString_() throws Exception {
        getResourceAsString("/unknown.json");
    }
}
