package com.gooddata.qa.utils.graphene;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Screenshots {

    private static final String SCREENSHOT_NAME_SEPARATOR = "-";

    private static File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
    private static File screenshotsOutputDir = new File(mavenProjectBuildDirectory, "screenshots");

    public static void takeScreenshot(WebDriver driver, String screenshotName, Class<?> testClass) {
        File imageOutputFile = new File(screenshotsOutputDir, testClass.getSimpleName() + "/" + screenshotName + "-screenshot.png");
        try {
            File directory = imageOutputFile.getParentFile();
            FileUtils.forceMkdir(directory);

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, imageOutputFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns concatenated and normalized screenshot name. This means that:
     * <ul>
     *     <li>contatenation takes all name parts together and joins them with separator '-'</li>
     *     <li>normalization takes name parts one by one and replaces empty spaces with separator '-'</li>
     * </ul>
     *
     * @param screenshotNameParts name parts of the screenshot
     * @return concatenated and normalized name
     */
    public static String toScreenshotName(String... screenshotNameParts) {
        return Arrays.stream(screenshotNameParts)
                .map(String::toLowerCase)
                .map(s -> s.replaceAll("\\s", SCREENSHOT_NAME_SEPARATOR))
                .collect(Collectors.joining(SCREENSHOT_NAME_SEPARATOR));
    }
}
