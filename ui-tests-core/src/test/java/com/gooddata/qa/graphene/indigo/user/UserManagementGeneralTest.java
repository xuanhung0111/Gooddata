package com.gooddata.qa.graphene.indigo.user;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.http.RestUtils.FeatureFlagOption;

public class UserManagementGeneralTest extends AbstractUITest {

    private boolean canAccessUserManagementByDefault;

    private static final String FEATURE_FLAG = ProjectFeatureFlags.DISPLAY_USER_MANAGEMENT.getFlagName();

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
    }

    @Test(groups = {"projectInit"})
    public void init() throws JSONException, IOException {
        signIn(false, UserRoles.ADMIN);
        enableUserManagementFeature();
    }

    @Test(dependsOnGroups = {"projectInit"})
    public void verifyUserManagementUI() throws IOException, JSONException {
        try {
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
