package com.gooddata.qa.utils.testng.listener;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.gooddata.qa.utils.testng.TestInfo;

public class FailureLoggingListener extends TestListenerAdapter {

    private WebDriver driver = GrapheneContext.getProxyForInterfaces(TakesScreenshot.class);
    private File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
    private File failuresOutputDir = new File(mavenProjectBuildDirectory, "failures");

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (driver == null) {
            return;
        }

        Throwable throwable = result.getThrowable();
        String stacktrace = null;

        if (throwable != null) {
            stacktrace = ExceptionUtils.getStackTrace(throwable);
        }

        String filenameIdentification = getFilenameIdentification(result);

        String htmlSource = driver.getPageSource();

        File stacktraceOutputFile = new File(failuresOutputDir, filenameIdentification + "/stacktrace.txt");
        File imageOutputFile = new File(failuresOutputDir, filenameIdentification + "/screenshot.png");
        File htmlSourceOutputFile = new File(failuresOutputDir, filenameIdentification + "/html-source.html");

        try {
            File directory = imageOutputFile.getParentFile();
            FileUtils.forceMkdir(directory);

            FileUtils.writeStringToFile(stacktraceOutputFile, stacktrace);
            FileUtils.writeStringToFile(htmlSourceOutputFile, htmlSource);

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, imageOutputFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }

    private String getFilenameIdentification(ITestResult result) {
        return TestInfo.getClassMethodName(result);
    }
}
