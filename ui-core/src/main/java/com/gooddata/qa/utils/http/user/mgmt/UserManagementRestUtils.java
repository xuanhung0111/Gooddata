package com.gooddata.qa.utils.http.user.mgmt;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.http.RestApiClient;

/**
 * REST utilities for user management task
 */
public final class UserManagementRestUtils {

    private static final Logger log = Logger.getLogger(UserManagementRestUtils.class.getName());

    private UserManagementRestUtils() {
    }

    private static final String USER_PROFILE_LINK = "/gdc/account/profile/";
    private static final String GROUPS_URI = "/gdc/internal/usergroups";
    private static final String DOMAIN_USER_LINK = "/gdc/account/domains/%s/users";
    private static final String USER_GROUP_MODIFY_MEMBERS_LINK = "/gdc/userGroups/%s/modifyMembers";
    private static final String USERS_LINK = "/gdc/projects/%s/users";
    private static final String ROLE_LINK = "/gdc/projects/%s/roles/%s";
    private static final String INVITATION_LINK = "/gdc/projects/%s/invitations";
    private static final String USER_FILTER_LINK = "/gdc/md/%s/userfilters";

    private static final Supplier<String> CREATE_USER_CONTENT_BODY = () -> {
        try {
            return new JSONObject() {{
                put("accountSetting", new JSONObject() {{
                    put("login", "${userEmail}");
                    put("password", "${userPassword}");
                    put("email", "${userEmail}");
                    put("verifyPassword", "${userPassword}");
                    put("firstName", "FirstName");
                    put("lastName", "LastName");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> ADD_USER_CONTENT_BODY = () -> {
        try {
            return new JSONObject() {{
                put("user", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("userRoles", new JSONArray().put("${userRoles}"));
                        put("status", "ENABLED");
                    }});
                    put("links", new JSONObject() {{
                        put("self", "/gdc/account/profile/${email}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> UPDATE_USER_INFO_CONTENT_BODY = () -> {
        try {
            return new JSONObject() {{
                put("accountSetting", new JSONObject() {{
                    put("firstName", "${firstName}");
                    put("lastName", "${lastName}");
                    put("old_password", "${old_password}");
                    put("password", "${password}");
                    put("verifyPassword", "${verifyPassword}");
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    private static final Supplier<String> INVITE_USER_WITH_MUF_CONTENT_BODY = () -> {
        try {
            return new JSONObject() {{
                put("invitations", new JSONArray() {{
                    put(new JSONObject() {{
                        put("invitation", new JSONObject() {{
                            put("content", new JSONObject() {{
                                put("email", "${email}");
                                put("userFilters", new JSONArray(){{
                                    put("${userFilter}");
                                }});
                                put("role", "${role}");
                                put("action", new JSONObject() {{
                                    put("setMessage", "${message}");
                                }});
                            }});
                        }});
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    /**
     * Create new user
     * 
     * @param restApiClient
     * @param username
     * @param password
     * @return new user uri or return existing uri if this user does exist in the system
     */
    public static String createUser(final RestApiClient restApiClient, final String userDomain,
            final String username, final String password) throws ParseException, JSONException, IOException {
        final Optional<JSONObject> userProfile = 
                Optional.ofNullable(getUserProfileByEmail(restApiClient, userDomain, username));
        if (userProfile.isPresent()) {
            log.info("the user " + username + " does exist in the server already. "
                    + "Please check deletion process to avoid this case");
            return userProfile.get().getJSONObject("links").getString("self");
        }

        final String contentBody = CREATE_USER_CONTENT_BODY.get()
                .replace("${userEmail}", username)
                .replace("${userPassword}", password);
        return getJsonObject(restApiClient, restApiClient.newPostMethod(format(DOMAIN_USER_LINK, userDomain),
                contentBody), HttpStatus.CREATED).getString("uri");
    }

    /**
     * Delete user base on user uri
     * 
     * @param restApiClient
     * @param userUri
     */
    public static void deleteUserByUri(final RestApiClient restApiClient, final String userUri) {
        executeRequest(restApiClient, restApiClient.newDeleteMethod(userUri), HttpStatus.OK);
    }

    /**
     *delete user based on email
     * 
     * @param restApiClient
     * @param userEmail
     */
    public static void deleteUserByEmail(final RestApiClient restApiClient, final String userDomain,
            final String userEmail) throws ParseException, IOException, JSONException {
        
        final JSONObject userProfile = getUserProfileByEmail(restApiClient, userDomain, userEmail);

        if (Objects.nonNull(userProfile)) {
            final String userProfileUri = userProfile.getJSONObject("links").getString("self");
            deleteUserByUri(restApiClient, userProfileUri);
            log.info("successfully deleted: " + userEmail);
        }
    }

    /**
     * Get user profile from user email
     * 
     * @param restApiClient
     * @param email
     * @return user profile in json object format
     */
    public static JSONObject getUserProfileByEmail(final RestApiClient restApiClient, final String userDomain,
            final String email) throws ParseException, IOException {
        if (email == null || email.trim().equals("")) {
            return null;
        }

        final String userUri = format(DOMAIN_USER_LINK, userDomain) + "?login=" + email.replace("@", "%40");

        try {
            return getJsonObject(restApiClient, userUri).getJSONObject("accountSettings")
                    .getJSONArray("items")
                    .getJSONObject(0)
                    .getJSONObject("accountSetting");

        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Update first name of current user
     * 
     * @param restApiClient
     * @param newFirstName
     * @return old first name of current user
     */
    public static String updateFirstNameOfCurrentAccount(final RestApiClient restApiClient, final String newFirstName)
            throws ParseException, JSONException, IOException {
        final JSONObject currentProfile = getJsonObject(restApiClient, "/gdc/account/profile/current")
                .getJSONObject("accountSetting");
        final String oldFirstName = currentProfile.getString("firstName");
        final String currentProfileUri = currentProfile.getJSONObject("links").getString("self");
        executeRequest(restApiClient,
                restApiClient.newPutMethod(currentProfileUri,
                        new JSONObject().put("accountSetting",
                                new JSONObject().put("country", currentProfile.get("country"))
                                .put("phoneNumber", currentProfile.get("phoneNumber"))
                                .put("timezone", currentProfile.get("timezone"))
                                .put("companyName", currentProfile.get("companyName"))
                                .put("lastName", currentProfile.get("lastName"))
                                .put("firstName", newFirstName)
                        ).toString()),
                HttpStatus.OK);
        return oldFirstName;
    }

    /**
     * Get current user profile
     * 
     * @param restApiClient
     * @return current user profile in json object format
     */
    public static JSONObject getCurrentUserProfile(final RestApiClient restApiClient) throws JSONException, IOException {
        return getJsonObject(restApiClient, USER_PROFILE_LINK + "current")
                .getJSONObject("accountSetting");
    }

    /**
     * Update password of current user
     * 
     * @param restApiClient
     * @param oldPassword
     * @param newPassword
     */
    public static void updateCurrentUserPassword(final RestApiClient restApiClient, final String oldPassword,
            final String newPassword) throws ParseException, JSONException, IOException {
        final JSONObject userProfile = getCurrentUserProfile(restApiClient);
        final String userProfileUri = userProfile.getJSONObject("links").getString("self");
        final String content = UPDATE_USER_INFO_CONTENT_BODY.get()
                .replace("${firstName}", userProfile.get("firstName").toString())
                .replace("${lastName}", userProfile.get("lastName").toString())
                .replace("${old_password}", oldPassword)
                .replace("${password}", newPassword)
                .replace("${verifyPassword}", newPassword);
        executeRequest(restApiClient, restApiClient.newPutMethod(userProfileUri, content), HttpStatus.OK);
    }

    /**
     * Invite an user and assign MUF
     * 
     * @param restApiClient
     * @param projectId
     * @param email
     * @param mufURI
     * @param userRole
     * @param message
     * @return invitation uri
     */
    public static String inviteUserWithMufObj(final RestApiClient restApiClient, final String projectId,
            final String email, final String mufURI, final UserRoles userRole, final String message)
                    throws ParseException, JSONException, IOException {
        final String roleUri = format(ROLE_LINK, projectId, userRole.getRoleId());
        final String invitationUri = format(INVITATION_LINK, projectId);
        final String contentBody = INVITE_USER_WITH_MUF_CONTENT_BODY.get()
                .replace("${email}", email)
                .replace("${userFilter}", mufURI)
                .replace("${role}", roleUri)
                .replace("${message}", message);
    
        return getJsonObject(restApiClient, restApiClient.newPostMethod(invitationUri, contentBody))
                .getJSONObject("createdInvitations").getJSONArray("uri").get(0).toString();
    }

    /**
     * get MUF uri from an invitation
     * 
     * @param restApiClient
     * @param invitationUri
     * @return muf uri
     */
    public static String getMufUriFromInvitation(final RestApiClient restApiClient,
            final String invitationUri) throws JSONException, IOException {
        return getInvitationContent(restApiClient, invitationUri)
                .getJSONArray("userFilters").get(0).toString();
    }

    /**
     * get role uri from an invitation
     * 
     * @param restApiClient
     * @param invitationUri
     * @return role uri
     */
    public static String getRoleUriFromInvitation(final RestApiClient restApiClient,
            final String invitationUri) throws JSONException, IOException {
        return getInvitationContent(restApiClient, invitationUri).getString("role");
    }

    /**
     * get users who are using a specified muf uri
     * 
     * @param restApiClient
     * @param projectId
     * @param mufUri
     * @return list of users
     */
    public static List<String> getUsersUsingMuf(final RestApiClient restApiClient, final String projectId,
            final String mufUri) throws JSONException, IOException {
        final JSONArray items = getJsonObject(restApiClient, format(USER_FILTER_LINK, projectId))
                .getJSONObject("userFilters").getJSONArray("items");

        final List<String> users = new ArrayList<String>();
        for (int i = 0, n = items.length(); i < n; i++) {
            final String json = items.getString(i);
            if (json.contains(mufUri))
                users.add(new JSONObject(json).getString("user"));
        }
        return users;
    } 

    /**
     * Add user to project with specific role
     * 
     * @param restApiClient
     * @param projectId
     * @param email
     * @param role
     */
    public static void addUserToProject(final RestApiClient restApiClient, final String projectId,
            final String email, final UserRoles role) throws ParseException, JSONException, IOException {
        final String usersUri = format(USERS_LINK, projectId);
        final String roleUri = format(ROLE_LINK, projectId, role.getRoleId());
        final String contentBody = ADD_USER_CONTENT_BODY.get()
                .replace("${userRoles}", roleUri)
                .replace("${email}", email);
        log.info("content of json: " + contentBody);

        final JSONObject result = getJsonObject(restApiClient, restApiClient.newPostMethod(usersUri, contentBody));
        assertFalse(result.getJSONObject("projectUsersUpdateResult")
                .getString("successful")
                .equals("[]"), "User isn't assigned properly into the project");
        log.info(format("Successfully assigned user %s to project %s", email, projectId));
    }

    /**
     * Add user to group of project
     * 
     * @param restApiClient
     * @param projectId
     * @param name
     * @return user group uri
     */
    public static String addUserGroup(final RestApiClient restApiClient, final String projectId, final String name)
            throws JSONException, IOException {
        final JSONObject payload = new JSONObject() {{
            put("userGroup", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("name", name);
                    put("project", format("/gdc/projects/%s", projectId));
                }});
            }});
        }};

        return getJsonObject(restApiClient, restApiClient.newPostMethod(GROUPS_URI, payload.toString()),
                HttpStatus.CREATED).getString("uri");
    }

    /**
     * Delete user group base on group uri
     * 
     * @param restApiClient
     * @param groupUri
     */
    public static void deleteUserGroup(final RestApiClient restApiClient, final String groupUri) {
        executeRequest(restApiClient, restApiClient.newDeleteMethod(groupUri), HttpStatus.NO_CONTENT);
    }

    /**
     * Add users to group
     * 
     * @param restApiClient
     * @param userGroupId
     * @param userURIs      array of user uri
     */
    public static void addUsersToUserGroup(final RestApiClient restApiClient, final String userGroupId,
            final String... userURIs) throws JSONException, ParseException, IOException {
        modifyUsersInGroup(restApiClient, userGroupId, "ADD", userURIs);
    }

    /**
     * Remove users from group
     * 
     * @param restApiClient
     * @param userGroupId
     * @param userURIs      array of user uri
     */
    public static void removeUsersFromUserGroup(final RestApiClient restApiClient, final String userGroupId,
            final String... userURIs) throws JSONException, ParseException, IOException {
        modifyUsersInGroup(restApiClient, userGroupId, "REMOVE", userURIs);
    }

    /**
     * Set users in group
     * 
     * @param restApiClient
     * @param userGroupId
     * @param userURIs      array of user uri
     */
    public static void setUsersInUserGroup(final RestApiClient restApiClient, final String userGroupId,
            final String... userURIs) throws JSONException, ParseException, IOException {
        modifyUsersInGroup(restApiClient, userGroupId, "SET", userURIs);
    }

    private static void modifyUsersInGroup(final RestApiClient restApiClient, final String userGroupId,
            final String operation, final String... userURIs) throws JSONException, ParseException, IOException {
        final String modifyMemberUri = format(USER_GROUP_MODIFY_MEMBERS_LINK, userGroupId);
        final String content = new JSONObject() {{
            put("modifyMembers", new JSONObject() {{
                put("operation", operation);
                put("items", new JSONArray(userURIs));
            }});
        }}.toString();

        executeRequest(restApiClient, restApiClient.newPostMethod(modifyMemberUri, content), HttpStatus.NO_CONTENT);
    }

    private static JSONObject getInvitationContent(final RestApiClient restApiClient, final String invitationUri)
            throws JSONException, IOException {
        return getJsonObject(restApiClient, invitationUri).getJSONObject("invitation").getJSONObject("content");
    }
}
