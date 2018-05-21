package com.gooddata.qa.utils.http.user.mgmt;

import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.qa.graphene.enums.user.UserRoles;

/**
 * REST request for user management task
 */
public final class UserManagementRestRequest extends CommonRestRequest {

    private static final Logger log = Logger.getLogger(UserManagementRestRequest.class.getName());

    public UserManagementRestRequest(final RestClient restClient, final String projectId) {
        super(restClient, projectId);
    }

    private static final String USER_PROFILE_LINK = "/gdc/account/profile/";
    private static final String GROUPS_URI = "/gdc/internal/usergroups";
    private static final String DOMAIN_USER_LINK = "/gdc/account/domains/%s/users";
    private static final String USER_GROUP_MODIFY_MEMBERS_LINK = "/gdc/userGroups/%s/modifyMembers";
    private static final String USERS_LINK = "/gdc/projects/%s/users";
    private static final String ROLE_LINK = "/gdc/projects/%s/roles/%s";
    private static final String INVITATION_LINK = "/gdc/projects/%s/invitations";
    private static final String USER_FILTER_LINK = "/gdc/md/%s/userfilters";
    private static final String LIST_USERS_USING_FILTER_LINK = USER_FILTER_LINK + "?userFilters=%s";

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

    private static final Supplier<String> UPDATE_USER_STATUS_CONTENT = () -> {
        try {
            return new JSONObject() {{
                put("user", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("status", "${status}");
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

    private final Supplier<String> UPDATE_USER_INFO_CONTENT_BODY = () -> {
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

    private final Supplier<String> INVITE_USER_WITH_MUF_CONTENT_BODY = () -> {
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

    public boolean isDomainUser(final String userDomain) {
        return HttpStatus.OK.value() == executeRequest(RestRequest.initGetRequest(format(DOMAIN_USER_LINK, userDomain)));
    }

    /**
     * Create new user
     * @param username
     * @param password
     * @return new user uri or return existing uri if this user does exist in the system
     */
    public String createUser(final String userDomain, final String username, final String password)
            throws ParseException, JSONException, IOException {
        final Optional<JSONObject> userProfile = 
                Optional.ofNullable(getUserProfileByEmail(userDomain, username));
        if (userProfile.isPresent()) {
            log.info("the user " + username + " does exist in the server already. "
                    + "Please check deletion process to avoid this case");
            return userProfile.get().getJSONObject("links").getString("self");
        }

        final String contentBody = CREATE_USER_CONTENT_BODY.get()
                .replace("${userEmail}", username)
                .replace("${userPassword}", password);
        return getJsonObject(RestRequest.initPostRequest(format(DOMAIN_USER_LINK, userDomain),
                contentBody), HttpStatus.CREATED).getString("uri");
    }

    /**
     * Delete user base on user uri
     *
     * @param userUri
     */
    public void deleteUserByUri(final String userUri) {
        executeRequest(RestRequest.initDeleteRequest(userUri), HttpStatus.OK);
    }

    /**
     *delete user based on email
     *
     * @param userEmail
     */
    public void deleteUserByEmail(final String userDomain,
            final String userEmail) throws ParseException, IOException, JSONException {
        
        final JSONObject userProfile = getUserProfileByEmail(userDomain, userEmail);

        if (Objects.nonNull(userProfile)) {
            final String userProfileUri = userProfile.getJSONObject("links").getString("self");
            deleteUserByUri(userProfileUri);
            log.info("successfully deleted: " + userEmail);
        }
    }

    /**
     * Get user profile from user email
     *
     * @param email
     * @return user profile in json object format
     */
    public JSONObject getUserProfileByEmail(final String userDomain, final String email)
            throws ParseException, IOException {
        if (email == null || email.trim().equals("")) {
            return null;
        }

        final String userUri = format(DOMAIN_USER_LINK, userDomain) + "?login=" + email.replace("@", "%40");

        try {
            return getJsonObject(userUri).getJSONObject("accountSettings")
                    .getJSONArray("items")
                    .getJSONObject(0)
                    .getJSONObject("accountSetting");

        } catch (JSONException e) {
            return null;
        }
    }

    public String getUserProfileUri(final String userDomain, final String email)
            throws ParseException, JSONException, IOException {
        return getUserProfileByEmail(userDomain, email).getJSONObject("links").getString("self");
    }

    /**
     * Update first name of current user
     *
     * @param newFirstName
     * @return old first name of current user
     */
    public String updateFirstNameOfCurrentAccount(final String newFirstName)
            throws ParseException, JSONException, IOException {
        final JSONObject currentProfile = getJsonObject("/gdc/account/profile/current")
                .getJSONObject("accountSetting");
        final String oldFirstName = currentProfile.getString("firstName");
        final String currentProfileUri = currentProfile.getJSONObject("links").getString("self");
        executeRequest(
                RestRequest.initPutRequest(currentProfileUri,
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
     * Update email of user
     *
     * @param userEmail
     * @param userDomain
     * @param email
     * @return email
     */
    public String updateEmailOfAccount(final String userDomain, final String userEmail, final String email)
            throws ParseException, JSONException, IOException {
        final JSONObject userProfile = getUserProfileByEmail(userDomain, userEmail);
        final String profileUri = userProfile.getJSONObject("links").getString("self");
        executeRequest(
                RestRequest.initPutRequest(profileUri,
                        new JSONObject().put("accountSetting",
                                new JSONObject().put("country", userProfile.get("country"))
                                        .put("phoneNumber", userProfile.get("phoneNumber"))
                                        .put("timezone", userProfile.get("timezone"))
                                        .put("companyName", userProfile.get("companyName"))
                                        .put("lastName", userProfile.get("lastName"))
                                        .put("firstName", userProfile.get("firstName"))
                                        .put("email", email)
                        ).toString()),
                HttpStatus.OK);
        return email;
    }

    /**
     * Get current user profile
     * @return current user profile in json object format
     */
    public JSONObject getCurrentUserProfile() throws JSONException, IOException {
        return getJsonObject(USER_PROFILE_LINK + "current")
                .getJSONObject("accountSetting");
    }

    /**
     * Get current user profile uri
     *
     * @return current user profile uri
     */
    public String getCurrentUserProfileUri() throws JSONException, IOException {
        return getCurrentUserProfile().getJSONObject("links").getString("self");
    }

    /**
     * Update password of an user
     *
     * @param userDomain
     * @param email
     * @param oldPassword
     * @param newPassword
     * @throws ParseException
     * @throws JSONException
     * @throws IOException
     */
    public void updateUserPassword(final String userDomain, final String email, final String oldPassword,
                                   final String newPassword) throws ParseException, JSONException, IOException {
        final JSONObject userProfile = getUserProfileByEmail(userDomain, email);
        final String userProfileUri = userProfile.getJSONObject("links").getString("self");
        final String content = UPDATE_USER_INFO_CONTENT_BODY.get()
                .replace("${firstName}", userProfile.get("firstName").toString())
                .replace("${lastName}", userProfile.get("lastName").toString())
                .replace("${old_password}", oldPassword)
                .replace("${password}", newPassword)
                .replace("${verifyPassword}", newPassword);
        executeRequest(RestRequest.initPutRequest(userProfileUri, content), HttpStatus.OK);
    }

    /**
     * Invite an user and assign MUF
     *
     * @param email
     * @param mufURI
     * @param userRole
     * @param message
     * @return invitation uri
     */
    public String inviteUserWithMufObj(final String email, final String mufURI, final UserRoles userRole,
                                       final String message)
                    throws ParseException, JSONException, IOException {
        final String roleUri = format(ROLE_LINK, projectId, userRole.getRoleId());
        final String invitationUri = format(INVITATION_LINK, projectId);
        final String contentBody = INVITE_USER_WITH_MUF_CONTENT_BODY.get()
                .replace("${email}", email)
                .replace("${userFilter}", mufURI)
                .replace("${role}", roleUri)
                .replace("${message}", message);
    
        return getJsonObject(RestRequest.initPostRequest(invitationUri, contentBody))
                .getJSONObject("createdInvitations").getJSONArray("uri").get(0).toString();
    }

    /**
     * get MUF uri from an invitation
     *
     * @param invitationUri
     * @return muf uri
     */
    public String getMufUriFromInvitation(final String invitationUri) throws JSONException, IOException {
        return getInvitationContent(invitationUri)
                .getJSONArray("userFilters").get(0).toString();
    }

    /**
     * get role uri from an invitation
     *
     * @param invitationUri
     * @return role uri
     */
    public String getRoleUriFromInvitation(final String invitationUri) throws JSONException, IOException {
        return getInvitationContent(invitationUri).getString("role");
    }

    /**
     * get users who are using a specified muf uri
     *
     * @param mufUri
     * @return list of users
     */
    public List<String> getUsersUsingMuf(final String mufUri) throws JSONException, IOException {
        final JSONArray items = getJsonObject(format(LIST_USERS_USING_FILTER_LINK, projectId, mufUri))
                .getJSONObject("userFilters").getJSONArray("items");

        final List<String> users = new ArrayList<String>();
        for (int i = 0, n = items.length(); i < n; i++) {
            users.add( items.getJSONObject(i).getString("user"));
        }
        return users;
    }

    /**
     * Disable or enable user to project
     *
     * @param email
     * @param status (ENABLED or DISABLE)
     */
    public void updateUserStatusInProject(final String email, final UserStatus status)
            throws ParseException, JSONException, IOException {
        final String usersUri = format(USERS_LINK, projectId);
        final String contentBody = UPDATE_USER_STATUS_CONTENT.get()
                .replace("${status}", status.toString())
                .replace("${email}", email);
        final JSONObject result = getJsonObject(RestRequest.initPostRequest(usersUri, contentBody));
        if (result.getJSONObject("projectUsersUpdateResult").getJSONArray("successful").length() == 0) {
                throw new RuntimeException("Update user status failed");
        }
    }

    /**
     * Add user to project with specific role
     *
     * @param email
     * @param role
     */
    public void addUserToProject(final String email, final UserRoles role)
            throws ParseException, JSONException, IOException {
        final String usersUri = format(USERS_LINK, projectId);
        final String roleUri = format(ROLE_LINK, projectId, role.getRoleId());
        final String contentBody = ADD_USER_CONTENT_BODY.get()
                .replace("${userRoles}", roleUri)
                .replace("${email}", email);
        log.info("content of json: " + contentBody);

        final JSONObject result = getJsonObject(RestRequest.initPostRequest(usersUri, contentBody));
        assertTrue(result.getJSONObject("projectUsersUpdateResult")
                .getJSONArray("successful").length() > 0, "User isn't assigned properly into the project");
        log.info(format("Successfully assigned user %s to project %s", email, projectId));
    }

    /**
     * Add user to group of project
     *
     * @param name
     * @return user group uri
     */
    public String addUserGroup(final String name)
            throws JSONException, IOException {
        final JSONObject payload = new JSONObject() {{
            put("userGroup", new JSONObject() {{
                put("content", new JSONObject() {{
                    put("name", name);
                    put("project", format("/gdc/projects/%s", projectId));
                }});
            }});
        }};

        return getJsonObject(RestRequest.initPostRequest(GROUPS_URI, payload.toString()),
                HttpStatus.CREATED).getString("uri");
    }

    /**
     * Delete user group base on group uri
     *
     * @param groupUri
     */
    public void deleteUserGroup(final String groupUri) {
        executeRequest(RestRequest.initDeleteRequest(groupUri), HttpStatus.NO_CONTENT);
    }

    /**
     * Add users to group
     *
     * @param userGroupId
     * @param userURIs      array of user uri
     */
    public void addUsersToUserGroup(final String userGroupId,
            final String... userURIs) throws JSONException, ParseException, IOException {
        modifyUsersInGroup(userGroupId, "ADD", userURIs);
    }

    private void modifyUsersInGroup(final String userGroupId,
            final String operation, final String... userURIs) throws JSONException, ParseException, IOException {
        final String modifyMemberUri = format(USER_GROUP_MODIFY_MEMBERS_LINK, userGroupId);
        final String content = new JSONObject() {{
            put("modifyMembers", new JSONObject() {{
                put("operation", operation);
                put("items", new JSONArray(userURIs));
            }});
        }}.toString();

        executeRequest(RestRequest.initPostRequest(modifyMemberUri, content), HttpStatus.NO_CONTENT);
    }

    private JSONObject getInvitationContent(final String invitationUri)
            throws JSONException, IOException {
        return getJsonObject(invitationUri).getJSONObject("invitation").getJSONObject("content");
    }

    public enum UserStatus {
        ENABLED, DISABLED
    }
}
