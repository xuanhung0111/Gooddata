package com.gooddata.qa.utils.testng.listener;

import static com.gooddata.qa.utils.graphene.Screenshots.takeFailureScreenshot;
import static java.util.Objects.isNull;

import org.jboss.arquillian.drone.api.annotation.Default;
import org.jboss.arquillian.graphene.context.GrapheneContext;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class AuxiliaryFailureScreenshotListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod() || testResult.isSuccess()) {
            return;
        }

        WebDriver driver = GrapheneContext.getContextFor(Default.class).getWebDriver(TakesScreenshot.class);
        if (isNull(driver)) {
            return;
        }

        takeFailureScreenshot(driver, testResult.getTestClass().getRealClass(), testResult.getName());
    }

}
