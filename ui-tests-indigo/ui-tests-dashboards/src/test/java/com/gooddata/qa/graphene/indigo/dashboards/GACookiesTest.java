package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import org.openqa.selenium.Cookie;
import org.testng.annotations.Test;

import java.util.Set;

import static org.testng.Assert.assertFalse;

public class GACookiesTest extends GoodSalesAbstractTest {

    @Test(dependsOnGroups = {"createProject"})
    public void cookieKpiPage() {
        initIndigoDashboardsPage();
        checkCookies();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cookieAnalyzePage() {
        initAnalysePage();
        checkCookies();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cookieDashboardPage() {
        initDashboardsPage();
        checkCookies();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cookieReportPage() {
        initReportsPage();
        checkCookies();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cookieManagePage() {
        initManagePage();
        checkCookies();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void cookieModelDataPage() {
        initModelDataPage();
        checkCookies();
    }

    public void checkCookies() {
        Set<Cookie> cookies = browser.manage().getCookies();
        System.out.println("cookies:   " + cookies);
        for (Cookie cookie : cookies) {
            String cookieGA = cookie.toString();
            assertFalse(cookieGA.contains("_ga"), "Ga cookie is still not disable yet");
            assertFalse(cookieGA.contains("_gid"), "Gid cookie is still not disable yet");
            assertFalse(cookieGA.contains("_gat"), "Gat cookie is still not disable yet");
        }
    }

}
