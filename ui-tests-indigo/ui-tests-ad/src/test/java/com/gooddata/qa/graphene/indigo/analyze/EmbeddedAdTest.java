package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.common.ApplicationHeaderBar;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;

public class EmbeddedAdTest extends GoodSalesAbstractTest {

    private static final String EMBEDDED_URI = "analyze/embedded/#/%s/reportId/edit";
    private static final String PERMISSION_ERROR_MESSAGE = "Sorry, you don't have access to this page.";

    @BeforeClass(alwaysRun = true)
    public void initializeProject() {
        projectTitle += "Embedded-Ad-Test";
        addUsersWithOtherRoles = true;
    }

    @Test(dependsOnGroups = { "createProject" })
    public void viewEnbeddedAdUsingEditorRoles() throws JSONException {
        logoutAndLoginAs(true, UserRoles.EDITOR);
        try {
            openUrl(getEmbeddedAdUrl());
            assertTrue(EmbeddedAnalysisPage.getInstance(browser).isEmbeddedPage(),
                    "Embeded AD page was not loaded");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void viewEnbeddedAdUsingViewerRoles() throws JSONException {
        logoutAndLoginAs(true, UserRoles.VIEWER);
        try {
            openUrl(getEmbeddedAdUrl());
            takeScreenshot(browser, "Test-Embedded-Ad-Using-Viewer-Role", getClass());
            assertEquals(EmbeddedAnalysisPage.getErrorMessage(browser), PERMISSION_ERROR_MESSAGE,
                    "Expected error messege was not displayed");
        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = { "createProject" })
    public void testBlankEmbeddedAd() {
        openUrl(getEmbeddedAdUrl());
        final EmbeddedAnalysisPage embeddedPage = EmbeddedAnalysisPage.getInstance(browser);

        takeScreenshot(browser, "Test-Blank-Embedded-Ad", getClass());
        assertFalse(isElementPresent(className(ApplicationHeaderBar.ROOT_LOCATOR), browser),
                "Header bar was displayed");
        assertFalse(embeddedPage.getPageHeader().isExportButtonPresent(), "Export button was added to embedded AD");
        assertFalse(embeddedPage.isAddDataButtonPresent(), "Add Data button was added to embedded AD");
    }

    private String getEmbeddedAdUrl() {
        return format(EMBEDDED_URI, testParams.getProjectId());
    }
}
