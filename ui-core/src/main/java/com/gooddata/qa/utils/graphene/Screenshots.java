package com.gooddata.qa.utils.graphene;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Screenshots {

    private static File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory",
            "./target/"));
    private static File screenshotsOutputDir = new File(mavenProjectBuildDirectory, "screenshots");
    private static File failuresOutputDir = new File(mavenProjectBuildDirectory, "failures");

    public static void takeScreenshot(WebDriver driver, String screenshotName, Class<?> testClass) {
        File imageOutputFile = new File(screenshotsOutputDir,
                testClass.getSimpleName() + "/" + screenshotName + "-screenshot.png");
        takeScreenshot(driver, imageOutputFile);
    }

    public static void takeFailureScreenshot(WebDriver driver, Class<?> testClass, String methodName) {
        File imageOutputFile = new File(failuresOutputDir,
                testClass.getSimpleName() + "." + methodName + "/screenshot.png");
        takeScreenshot(driver, imageOutputFile);
    }

    private static void takeScreenshot(WebDriver driver, File imageOutputFile) {
        try {
            File directory = imageOutputFile.getParentFile();
            FileUtils.forceMkdir(directory);

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, imageOutputFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
