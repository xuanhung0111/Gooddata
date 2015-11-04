/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.drone.browserstack.webdriver;

import java.net.URL;
import java.util.logging.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class BrowserStackDriver extends RemoteWebDriver {

    public static final String READABLE_NAME = "browserstack";

    private static final Logger log = Logger.getLogger(BrowserStackDriver.class.getName());

    private static final long DECREASE_TEST_SPEED_IN_MILISECONDS = 200;

    public BrowserStackDriver(URL url, Capabilities capabilities) {
        super(url, capabilities);
        log.info("Session ID: " + this.getSessionId());
        log.info("Browser capabilities: " + capabilities);
    }

    @Override
    public String getCurrentUrl() {
        try {
            return super.getCurrentUrl();
        } catch (Exception e) {
            log.info(e.getMessage());
            return "";
        }
    }

    @Override
    public WebElement findElement(By by) {
        // decrease test speed when running in browserstack to make sure tests are more stable
        try {
            Thread.sleep(DECREASE_TEST_SPEED_IN_MILISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return super.findElement(by);
    }
}
