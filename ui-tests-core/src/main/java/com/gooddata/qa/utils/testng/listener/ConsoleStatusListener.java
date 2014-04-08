package com.gooddata.qa.utils.testng.listener;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.gooddata.qa.utils.testng.TestLoggingUtils;

public class ConsoleStatusListener extends TestListenerAdapter {

    @Override
    public void onTestStart(ITestResult result) {
        logStatus(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logStatus(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logStatus(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logStatus(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logStatus(result);
    }

    /**
     * This method will output method name and status on the standard output
     *
     * @param result from the fine-grained listener's method such as onTestFailure(ITestResult)
     */
    private void logStatus(ITestResult result) {
        String message = getMessage(result);
        System.out.println(message);
        if (result.getStatus() != ITestResult.STARTED) {
            System.out.println();
        }
    }

    protected String getMessage(ITestResult result) {
        return TestLoggingUtils.getTestDescription(result);
    }
}
