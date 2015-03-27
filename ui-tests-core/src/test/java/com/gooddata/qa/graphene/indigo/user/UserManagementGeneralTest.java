package com.gooddata.qa.graphene.indigo.user;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

public class UserManagementGeneralTest extends AbstractProjectTest {

    private boolean canAccessUserManagementByDefault;

    private static final String FEATURE_FLAG = ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT.getFlagName();

    @BeforeClass
    public void initStartPage() {
        projectTitle = "User-management-general";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void initialize() throws JSONException, IOException {
        enableUserManagementFeature();
    }

    @Test(dependsOnMethods = {"initialize"})
    public void verifyUserManagementUI() throws IOException, JSONException {
        try {
            // Go to Dashboard page of new created project to use User management page of that project
            initProjectsPage();
            initDashboardsPage();
            initUserManagementPage();
            userManagementPage.openInviteUserDialog().cancelInvitation();
            assertEquals(userManagementPage.getUsersCount(), 1);
            userManagementPage.selectUsers(testParams.getUser());
        } finally {
            disableUserManagementFeature();
        }
    }

    private void enableUserManagementFeature() throws IOException, JSONException {
        canAccessUserManagementByDefault = RestUtils.isFeatureFlagEnabled(getRestApiClient(), FEATURE_FLAG);
        if (!canAccessUserManagementByDefault) {
            RestUtils.setFeatureFlags(getRestApiClient(),
                    FeatureFlagOption.createFeatureClassOption(FEATURE_FLAG, true));
        }
    }

    private void disableUserManagementFeature() throws IOException, JSONException {
        if (!canAccessUserManagementByDefault) {
            RestUtils.setFeatureFlags(getRestApiClient(),
                    FeatureFlagOption.createFeatureClassOption(FEATURE_FLAG, false));
        }
    }
}
