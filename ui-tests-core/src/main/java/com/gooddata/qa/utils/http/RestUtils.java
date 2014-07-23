package com.gooddata.qa.utils.http;

import java.io.IOException;

import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.gooddata.qa.graphene.enums.UserRoles;
import static org.testng.Assert.*;


public class RestUtils {
    
    private static final String usersLink = "/gdc/projects/%s/users";
    private static final String roleUriLink = "/gdc/projects/%s/roles/%s";
    private static final String addUserContentBody = "{\"user\":{\"content\":{\"userRoles\":[\"%s\"],\"status\":\"ENABLED\"},\"links\":{\"self\":\"%s\"}}}";
    
    public static void addUserToProject(String host, String projectId, String domainUser,
	    String domainPassword, String inviteeProfile, UserRoles role) throws ParseException, IOException, JSONException {
	RestApiClient restApiClient = new RestApiClient(host, domainUser, domainPassword, true, false);
	String usersUri = String.format(usersLink, projectId);
	String roleUri = String.format(roleUriLink, projectId, role.getRoleId());
	String contentBody =  String.format(addUserContentBody, roleUri, inviteeProfile);
	HttpRequestBase postRequest = restApiClient.newPostMethod(usersUri, contentBody);
	HttpResponse postResponse = restApiClient.execute(postRequest);
	JSONObject json = new JSONObject(EntityUtils.toString(postResponse.getEntity()));
	assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");
	assertFalse(json.getJSONObject("projectUsersUpdateResult").getString("successful").equals("[]"), "User isn't assigned properly into the project");
	System.out.println(String.format("Successfully assigned user %s to project %s by domain admin %s", inviteeProfile, projectId, domainUser ));
    }
}
