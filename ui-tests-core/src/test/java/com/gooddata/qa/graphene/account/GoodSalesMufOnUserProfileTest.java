package com.gooddata.qa.graphene.account;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static java.util.Collections.singletonList;

import java.io.IOException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;

import static com.gooddata.md.Restriction.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.profile.UserProfilePage;
import com.gooddata.qa.utils.http.RestApiClient;

public class GoodSalesMufOnUserProfileTest extends GoodSalesAbstractTest {

    private static final String NON_MUF_USER = "non-Muf-User";
    private static final String MUF_USER = "muf-User";

    private static final String NON_MUF_USER_USERDESCRIPTION = "User .* can see all data. How to set Data Permissions";
    private static final String MUF_USER_DESCRIPTION = "Following filters define what .* can see. How to set Data Permissions";
    private static final String SET_DATA_PERMISSIONS_LINK = "https://help.gooddata.com/display/developer/Data+Permissions+for+Beginners";

    private static final String MUF_NAME = "Muf";
    private static final String EXPRESSION = "[%s] = [%s]";

    private Attribute stageNameAttribute;
    private AttributeElement stageNameValue;
    private DashboardRestRequest dashboardRequest;
    private UserManagementRestRequest userManagementRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"init"})
    public void addMufForUser() throws ParseException, JSONException, IOException {
        stageNameAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_STAGE_NAME));
        stageNameValue = getMdService().getAttributeElements(stageNameAttribute).stream()
                .filter(e -> "Interest".equals(e.getTitle())).findFirst().get();

        final String expression = format(EXPRESSION, stageNameAttribute.getUri(), stageNameValue.getUri());
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRequest.addMufToUser(userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(),
                testParams.getEditorUser()), dashboardRequest.createMufObjectByUri(MUF_NAME, expression));
    }

    @DataProvider(name = "userProvider")
    public Object[][] getUserProvider() {
        return new Object[][] {
            {testParams.getUser(), NON_MUF_USER, NON_MUF_USER_USERDESCRIPTION},
            {testParams.getEditorUser(), MUF_USER, MUF_USER_DESCRIPTION}
        };
    }

    @Test(dependsOnGroups = {"init"}, dataProvider = "userProvider")
    public void checkMufOnUserProfilePage(String user, String type, String description) {
        UserProfilePage userProfilePage = initProjectsAndUsersPage().openUserProfile(user);

        takeScreenshot(browser, "Data-permissions-description-for-" + type, getClass());
        assertTrue(userProfilePage
                .getMufDescription()
                .matches(description),
                "Description for non-muf user not correctly");
        assertEquals(userProfilePage.getSetMufLink(), SET_DATA_PERMISSIONS_LINK);

        if (MUF_USER.equals(type)) {
            takeScreenshot(browser, "Related-mufs-show-for-user", getClass());
            assertEquals(userProfilePage.getAvailableMufs(), singletonList(MUF_NAME));
            assertEquals(userProfilePage.getAvailableMufExpressions(),
                    singletonList(format(EXPRESSION, stageNameAttribute.getTitle(), stageNameValue.getTitle())));
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkMufIsHiddenForEditor() throws JSONException, ParseException, IOException {
        String editorProfileUri = userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(),
                testParams.getEditorUser());

        logoutAndLoginAs(true, UserRoles.EDITOR);

        try {
            boolean mufSectionDisplayed = initUserProfilePage(editorProfileUri).isMufSectionDisplayed();
            takeScreenshot(browser, "Muf-section-hidden-for-editor", getClass());
            assertFalse(mufSectionDisplayed, "Muf-section-displays-for-editor");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }
}
