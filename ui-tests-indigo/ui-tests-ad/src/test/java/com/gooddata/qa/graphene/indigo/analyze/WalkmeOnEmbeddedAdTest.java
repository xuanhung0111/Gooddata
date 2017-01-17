package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;
import static java.lang.String.format;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.fragments.common.WalkmeDialog;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.EmbeddedAnalysisPage;

public class WalkmeOnEmbeddedAdTest extends AbstractUITest {

    private static final String GOODDATA_PRODUCT_TOUR_PROJECT = "GoodData Product Tour";
    private static final String EMBEDDED_URI = "analyze/embedded/#/%s/reportId/edit";

    @Test
    public void testNoWalkmeOnEmbeddedAd() throws ParseException, IOException, JSONException {
        browser.manage().deleteAllCookies();
        String newUserEmail = generateEmail(testParams.getUser());
        try {
            registerNewUser(newUserEmail);

            testParams.setProjectId(getProductTourProjectId());
            openUrl(format(EMBEDDED_URI, testParams.getProjectId()));
            assertTrue(EmbeddedAnalysisPage.getInstance(browser).isEmbeddedPage(), "Embedded AD page was not loaded");
            assertFalse(WalkmeDialog.isPresent(browser), "Walkme dialog was loaded");
        } finally {
            deleteUserByEmail(getRestApiClient(), testParams.getUserDomain(), newUserEmail);
        }
    }

    private void registerNewUser(String email) {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        initRegistrationPage().registerNewUserSuccessfully(new RegistrationForm()
                .withFirstName("FirstName " + uniqueSuffix)
                .withLastName("LastName " + uniqueSuffix)
                .withEmail(email)
                .withPassword(testParams.getPassword())
                .withPhone(uniqueSuffix)
                .withCompany("Company " + uniqueSuffix)
                .withJobTitle("Title " + uniqueSuffix)
                .withIndustry("Government"));

        waitForDashboardPageLoaded(browser);
    }

    private String getProductTourProjectId() {
        return initProjectsPage().getProjectItem(GOODDATA_PRODUCT_TOUR_PROJECT).getId();
    }
}
