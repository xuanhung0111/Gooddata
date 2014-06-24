package com.gooddata.qa.graphene.common;

import org.openqa.selenium.WebDriver;

public class CommonUtils {

    protected WebDriver browser;
    protected CheckUtils checkUtils;
    protected TestParameters testParameters;

    public CommonUtils(WebDriver browser, CheckUtils checkUtils, TestParameters testParameters) {
        this.browser = browser;
        this.checkUtils = checkUtils;
        this.testParameters = testParameters;
    }

    public void openUrl(String url) {
        String pageURL = getRootUrl() + url;
        System.out.println("Loading page ... " + pageURL);
        browser.get(pageURL);
    }

    public String getRootUrl() {
        return "https://" + testParameters.getHost() + "/";
    }

    public String getBasicRootUrl() {
        String rootUrl = getRootUrl();
        return getRootUrl().substring(0, rootUrl.length() - 1);
    }
}