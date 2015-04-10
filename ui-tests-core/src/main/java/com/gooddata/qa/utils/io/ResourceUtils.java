package com.gooddata.qa.utils.io;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

/**
 * Convenient utility methods for working with resource files.
 */
public class ResourceUtils {

    private ResourceUtils() {
    }

    /**
     * Reads resource file and returns it's content as a string in UTF-8 charset.
     *
     * @param resourceName name of the desired resource. See {@link java.lang.Class#getResource(String)} for more details.
     * @return resource content as a string in UTF-8 charset
     * @throws IllegalStateException if no resource with this name is found or an I/O exception occurs
     */
    public static String getResourceAsString(final String resourceName) {
        try {
            final URL resourceUrl = ResourceUtils.class.getResource(resourceName);
            if (resourceUrl == null) {
                throw new IllegalStateException("Resource '" + resourceName + "' not found!");
            }
            return IOUtils.toString(resourceUrl, "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException("I/O exception occurred when reading the resource file!", e);
        }
    }

}
