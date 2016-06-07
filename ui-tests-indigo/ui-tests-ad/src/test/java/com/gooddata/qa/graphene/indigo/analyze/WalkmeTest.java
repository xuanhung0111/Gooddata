package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.fragments.common.WalkmeDialog;

public class WalkmeTest extends AbstractUITest {

    private static final String REGISTRATION_USER = "gd.accregister@gmail.com";
    private static final String REGISTRATION_USER_PASSWORD = "changeit";

    private static final String GOODDATA_PRODUCT_TOUR_PROJECT = "GoodData Product Tour";

    @SuppressWarnings("serial")
    private static final Map<String, String> WALKME_CONTENTS = new HashMap<String, String>() {{
        put("Welcome to the Analytical Designer",
                "This interactive environment allows you to explore your data and create "
                + "visualizations quickly and easily. Intelligent on-screen recommendations help you "
                + "discover new and surprising insights. Let's get started!");

        put("Begin by exploring your data", "Measures represent quantitative data (values).\n\n"
                + "Attributes represent qualitative data (categories).\n\n"
                + "Date is a special item which represents all the dates in your project.");

        put("Create a new visualization",
                "Drag data from the list onto the canvas and watch as your visualization takes shape!");

        put("Remove data", "Drag data items from these zones back to the list to remove them from your visualization.");

        put("Change visualization type", "Choose how to visualize your data.");

        put("Filter your visualization", "Drag the Date field or any attribute here.");

        put("Save your visualization as a report", "When you are ready, open your visualization in the "
                + "Report Editor. From there you can save it and add it to a dashboard.");

        put("Clear your canvas", "Restart your exploration at any time.");

        put("You're ready.", "Go ahead. Start discovering the insights that await in your data!");
    }};

    @Test(groups = PROJECT_INIT_GROUP)
    public void registerNewUser() throws ParseException, JSONException, IOException {
        deleteUserByEmail(getRestApiClient(), REGISTRATION_USER);

        initRegistrationPage();

        String registrationString = String.valueOf(System.currentTimeMillis());
        registrationPage.registerNewUser(new RegistrationForm()
                .withFirstName("FirstName " + registrationString)
                .withLastName("LastName " + registrationString)
                .withEmail(REGISTRATION_USER)
                .withPassword(REGISTRATION_USER_PASSWORD)
                .withPhone(registrationString)
                .withCompany("Company " + registrationString)
                .withJobTitle("Title " + registrationString)
                .withIndustry("Government"));

        waitForFragmentNotVisible(registrationPage);
        waitForDashboardPageLoaded(browser);
    }

    @Test(dependsOnGroups = PROJECT_INIT_GROUP)
    public void testWalkme() {
        testParams.setProjectId(getProjectId(GOODDATA_PRODUCT_TOUR_PROJECT));

        initAnalysePageByUrl();

        WalkmeDialog walkmeDialog = WalkmeDialog.getInstance(browser);

        takeScreenshot(browser, walkmeDialog.getTitle(), getClass());
        assertEquals(walkmeDialog.getContent(), WALKME_CONTENTS.get(walkmeDialog.getTitle()));

        while (true) {
            walkmeDialog.goNextStep();

            takeScreenshot(browser, walkmeDialog.getTitle(), getClass());
            assertEquals(walkmeDialog.getContent(), WALKME_CONTENTS.get(walkmeDialog.getTitle()));

            if (walkmeDialog.canFinish()) {
                walkmeDialog.finish();
                break;
            }

            walkmeDialog.goPreviousStep();
            assertEquals(walkmeDialog.getContent(), WALKME_CONTENTS.get(walkmeDialog.getTitle()));

            walkmeDialog.goNextStep();
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws ParseException, JSONException, IOException {
        deleteUserByEmail(getRestApiClient(), REGISTRATION_USER);
    }

    private String getProjectId(String name) {
        initProjectsPage();
        return waitForFragmentVisible(projectsPage).getProjectsIds(name).get(0);
    }
}
