package com.gooddata.qa.utils.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import com.gooddata.qa.graphene.enums.UserRoles;
import static org.testng.Assert.*;


public class RestUtils {
    
    private static final String usersLink = "/gdc/projects/%s/users";
    private static final String roleUriLink = "/gdc/projects/%s/roles/%s";
    private static final String addUserContentBody = "{\"user\":{\"content\":{\"userRoles\":[\"%s\"],\"status\":\"ENABLED\"},\"links\":{\"self\":\"%s\"}}}";
    
    public static void addUserToProject(String host, String projectId, String domainUser,
	    String domainPassword, String inviteeProfile, UserRoles role) {
	RestApiClient restApiClient = new RestApiClient(host, domainUser, domainPassword, true, false);
	String usersUri = String.format(usersLink, projectId);
	String roleUri = String.format(roleUriLink, projectId, role.getRoleId());
	String contentBody =  String.format(addUserContentBody, roleUri, inviteeProfile);
	HttpRequestBase postRequest = restApiClient.newPostMethod(usersUri, contentBody);
	HttpResponse postResponse = restApiClient.execute(postRequest);
	assertEquals(postResponse.getStatusLine().getStatusCode(), 200, "Invalid status code");
    }
}
