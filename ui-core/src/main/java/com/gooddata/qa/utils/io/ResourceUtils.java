package com.gooddata.qa.utils.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;

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

    public static File getResourceAsFile(final String resourceName) {
        final URL resourceUrl = ResourceUtils.class.getResource(resourceName);
        if (resourceUrl == null) {
            throw new IllegalStateException("Resource '" + resourceName + "' not found!");
        }

        // avoid URL encode paths from URL#getFile: 
        // http://stackoverflow.com/questions/8928661/how-to-avoid-getting-url-encoded-paths-from-url-getfile

        // When running from 2 builds in the same job in Jenkins, source directory will be created with suffix
        // @<number> from the second build. Ex: /workspace/MSF-Data-Section-Graphene-simple-test@2/
        // URL#getFile will encode this path from @<number> to %40<number> and this file path is not exists.
        try {
            return new File(new URI(resourceUrl.toString()).getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFilePathFromResource(final String resourceName) {
        return getResourceAsFile(resourceName).getAbsolutePath();
    }
}
