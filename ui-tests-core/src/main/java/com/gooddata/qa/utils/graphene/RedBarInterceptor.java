package com.gooddata.qa.utils.graphene;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.proxy.Interceptor;
import org.jboss.arquillian.graphene.proxy.InvocationContext;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RedBarInterceptor implements Interceptor {

    @Drone
    private WebDriver browser;

    /**
     * to be added in parent test:
     * // register RedBasInterceptor - to check errors in UI
     * GrapheneProxyInstance proxy = (GrapheneProxyInstance) browser;
     * proxy.registerInterceptor(new RedBarInterceptor());
     */

    private static final By BY_RED_BAR = By.xpath("//div[@id='status']/div[contains(@class, 'box-error')]//div[@class='leftContainer']");

    public Object intercept(final InvocationContext context) throws Throwable {
        Method method = context.getMethod();
        System.out.println("Invoking method..." + method.getName());
        List<String> allowedMethods = new ArrayList<String>(Arrays.asList("get", "findElement", "getScreenshotAs"));
        if (allowedMethods.contains(method.getName())) {
            System.out.println("Checking for red bar....");
            if (browser.findElements(BY_RED_BAR).size() > 0) {
                String errorMessage = browser.findElement(BY_RED_BAR).getText();
                throw new IllegalStateException("RED BAR appeared - failing!!! Error: " + errorMessage);
            }
        }
        return context.invoke();
    }
}
