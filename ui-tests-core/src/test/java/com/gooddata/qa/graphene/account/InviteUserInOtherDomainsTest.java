package com.gooddata.qa.graphene.account;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;

public class InviteUserInOtherDomainsTest extends AbstractProjectTest {

    private static final String INVITATION_MESSAGE = "We invite you to our project";
    private static final String ERROR_MESSAGE = 
            "User %s could not be invited because they are a member of another GoodData organization.";
    private static final String ERROR_MESSAGE_FOR_INVITATION_IN_TWO_DOMAINS = ERROR_MESSAGE +
            " %d other user(s?) (was|were) successfully invited.";
    private static final String USER_IN_STAGING_DOMAIN = "qa+test@gooddata.com";
    private static final String USER_IN_OTHER_DOMAIN = "qa+test-wl@gooddata.com";

    @Test(dependsOnGroups = {"createProject"})
    public void inviteUserInTwoDomains() throws ParseException, IOException, JSONException {
        final String userA = testParams.getEditorUser();
        final String userB = testParams.getViewerUser();
        final String userC = getUserInOtherDomain(testParams.getHost());

        initProjectsAndUsersPage().openInvitedUserTab();
        inviteUsersAsViewerRole(userA);
        checkInvitedUser("Invitation was successfully sent.", "invite-user-A", userA + " has not been invited");

        projectAndUsersPage.dismissStatusBar().cancelAllInvitations();

        inviteUsersAsViewerRole(userA, userB);
        checkInvitedUser("2 invitations were successfully sent.", 
                "invite-user-A-B", userA + " " + userB + "have not been invited");

        projectAndUsersPage.dismissStatusBar().cancelAllInvitations();

        inviteUsersAsViewerRole(userC);
        checkInvitedUser(format(ERROR_MESSAGE, userC.replace("+", "\\+").replace("-", "\\-")), "invite-user-C",
                "The error message is not displayed");

        projectAndUsersPage.dismissStatusBar();

        inviteUsersAsViewerRole(userA, userC);
        checkInvitedUser(format(ERROR_MESSAGE_FOR_INVITATION_IN_TWO_DOMAINS,
                userC.replace("+", "\\+").replace("-", "\\-"), 1), "invite-user-A-C", "The error message is not displayed");

        projectAndUsersPage.dismissStatusBar().cancelAllInvitations();

        inviteUsersAsViewerRole(userA, userB, userC);
        checkInvitedUser(String.format(ERROR_MESSAGE_FOR_INVITATION_IN_TWO_DOMAINS,
                userC.replace("+", "\\+").replace("-", "\\-"),2), "invite-user-A-B-C",
                "The expected message is not displayed");
    }

    private String getUserInOtherDomain(String host) {
        if (host.startsWith("staging")) return USER_IN_OTHER_DOMAIN;
        return USER_IN_STAGING_DOMAIN;
    }

    private void inviteUsersAsViewerRole(String... user) {
        waitForFragmentVisible(projectAndUsersPage.openInviteUserDialog())
                .inviteUsers(UserRoles.VIEWER, INVITATION_MESSAGE, user);
    }

    private void checkInvitedUser(String expectedMessage, String screenshotName, String errorMessage) {
        assertTrue(projectAndUsersPage.getStatusMessage().matches(expectedMessage), errorMessage);
        takeScreenshot(browser, screenshotName, getClass());
    }

}