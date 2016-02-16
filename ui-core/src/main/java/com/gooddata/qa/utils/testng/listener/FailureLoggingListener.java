package com.gooddata.qa.utils.testng.listener;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.util.logging.Level;
import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.gooddata.qa.utils.testng.TestInfo;

public class FailureLoggingListener extends TestListenerAdapter {

    private File mavenProjectBuildDirectory = new File(System.getProperty("maven.project.build.directory", "./target/"));
    private File failuresOutputDir = new File(mavenProjectBuildDirectory, "failures");

    @Override
    public void onTestStart(ITestResult result) {
    }

    @Override
    public void onTestFailure(ITestResult result) {
        WebDriver driver = GrapheneContext.getContextFor(Default.class).getWebDriver(TakesScreenshot.class);
        if (driver == null) {
            return;
        }

        Throwable throwable = result.getThrowable();
        String stacktrace = null;
        String consolelog = "";

        if (throwable != null) {
            stacktrace = ExceptionUtils.getStackTrace(throwable);
        }

        String filenameIdentification = getFilenameIdentification(result);

        String htmlSource = driver.getPageSource();

        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

        for (LogEntry entry : logEntries) {
            if (entry.getLevel() == Level.SEVERE) {
                consolelog += (new Date(entry.getTimestamp()) + " " + entry.getMessage() + "\n");
            }
        }

        File consoleLogOutputFile = new File(failuresOutputDir, filenameIdentification + "/console.log");
        File stacktraceOutputFile = new File(failuresOutputDir, filenameIdentification + "/stacktrace.txt");
        File imageOutputFile = new File(failuresOutputDir, filenameIdentification + "/screenshot.png");
        File htmlSourceOutputFile = new File(failuresOutputDir, filenameIdentification + "/html-source.html");

        try {
            File directory = imageOutputFile.getParentFile();
            FileUtils.forceMkdir(directory);

            FileUtils.writeStringToFile(consoleLogOutputFile, consolelog);
            FileUtils.writeStringToFile(stacktraceOutputFile, stacktrace);
            FileUtils.writeStringToFile(htmlSourceOutputFile, htmlSource);

            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, imageOutputFile);

        } catch(WebDriverException e) {
            System.out.println("Unable to take screenshot of the page, see the error below");
            e.printStackTrace();
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
