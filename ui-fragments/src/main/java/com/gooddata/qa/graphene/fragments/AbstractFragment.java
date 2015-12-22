package com.gooddata.qa.graphene.fragments;

import static java.util.Objects.isNull;

import java.util.logging.Logger;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.fragment.Root;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public abstract class AbstractFragment {

    @Root
    protected WebElement root;

    @Drone
    protected WebDriver browser;

    private Actions actions;

    protected static final Logger log = Logger.getLogger(AbstractFragment.class.getName());

    public WebElement getRoot() {
        return root;
    }

    protected Actions getActions() {
        if (isNull(actions)) {
            actions = new Actions(browser);
        }
        return actions;
    }

    protected static final By BY_LINK = By.tagName("a");

    protected static final By BY_PARENT = By.xpath("..");
}
