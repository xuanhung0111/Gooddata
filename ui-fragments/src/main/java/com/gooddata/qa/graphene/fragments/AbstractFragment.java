package com.gooddata.qa.graphene.fragments;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class AbstractFragment {

    @Root
    protected WebElement root;

    @Drone
    protected WebDriver browser;

    public WebElement getRoot() {
        return root;
    }

    protected static final By BY_LINK = By.tagName("a");

    protected static final By BY_PARENT = By.xpath("..");
}
