package com.gooddata.qa.graphene.account;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createMufObjByUri;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getRoleUriFromInvitation;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUserProfileByEmail;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.getUsersUsingMuf;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.inviteUserWithMufObj;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.mail.ImapUtils.getLastEmail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.mail.MessagingException;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.AttributeElement;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.account.RegistrationForm;
import com.gooddata.qa.graphene.entity.mail.Email;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.account.RegistrationPage;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;
import com.gooddata.qa.utils.io.ResourceUtils;

public class InviteUserWithMufTest extends AbstractProjectTest {

    private static final String INVITATION_MESSAGE = "We invite you to our project";

    private static final By INVITATION_PAGE_LOCATOR = By.cssSelector(".s-invitationPage");

    private String invitationSubject;
    private RegistrationForm registrationForm;
    private String defaultMufUri;
    private String updatedMufUri;

    //    Use this variable to avoid connecting to inbox many times 
    private int expectedMessageCount;

    @BeforeClass
    public void initData() throws IOException, MessagingException {
        final String uniqueString = String.valueOf(System.currentTimeMillis());
        projectTitle += uniqueString;

        imapHost = testParams.loadProperty("imap.host");
        imapPassword = testParams.loadProperty("imap.password");
        imapUser = testParams.loadProperty("imap.user");

        invitationSubject = projectTitle + "-" + testParams.getProjectDriver().name() + " Invitation";

        registrationForm = new RegistrationForm()
                .withFirstName("FirstName ")
                .withLastName("LastName ")
                .withEmail(imapUser)
                .withPassword(imapPassword)
                .withPhone(uniqueString)
                .withCompany("Company ")
                .withJobTitle("Title ")
                .withIndustry("Tech");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void getExpectedMessage() {
        expectedMessageCount = doActionWithImapClient(imapClient ->
                imapClient.getMessagesCount(GDEmails.INVITATION, invitationSubject));
    }

    @Test(dependsOnMethods = {"getExpectedMessage"})
    public void setUpProject() throws IOException, JSONException {
        uploadCSV(ResourceUtils.getFilePathFromResource("/" + ResourceDirectory.PAYROLL_CSV + "/payroll.csv"));
        takeScreenshot(browser, "uploaded-payroll-file", getClass());

        defaultMufUri = createEducationMuf(Arrays.asList("Partial College", "Partial High School"), "Education user filter");
        updatedMufUri = createEducationMuf(Arrays.asList("Partial College"), "Education-Partial college user filter");
    }

    @Test(dependsOnMethods = {"setUpProject"})
    public void inviteUserWithMuf() throws IOException, JSONException {
        sendDefaultInvitation(imapUser);

        logoutAndopenActivationLink(imapUser, imapPassword,
                getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1));

        createSimpleReport();

        final List<String> attributes = reportPage.getTableReport().getAttributeElements();
        final long attributeCount = attributes.stream()
                .filter(s -> s.equals("Partial College") || s.equals("Partial High School"))
                .collect(Collectors.counting())
                .longValue();

        assertEquals(attributeCount, 2, "The MUF has been not applied");

        assertTrue(isUserUsingMuf(defaultMufUri, imapUser));

        ++expectedMessageCount;
    }

    @Test(dependsOnMethods = {"inviteUserWithMuf"})
    public void updateMufInInvitation() throws IOException, JSONException {
        final String nonRegistedUser = generateEmail(imapUser);
        sendDefaultInvitation(nonRegistedUser);

        final String previousActivitionLink = getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1);

        ++expectedMessageCount;

        final String invitationUri = inviteUserWithMufObj(getRestApiClient(),
                testParams.getProjectId(), nonRegistedUser, updatedMufUri, UserRoles.EDITOR, INVITATION_MESSAGE);

        final String mufUriInInvitation = UserManagementRestUtils
                .getMufUriFromInvitation(getRestApiClient(), invitationUri);
        assertEquals(updatedMufUri, mufUriInInvitation, "The MUF in invitation content has not been updated ");

        final String activitionLink = getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1);
        assertTrue(activitionLink.equals(previousActivitionLink), "The invitation link is not the same as previous email");

        logout();
        openUrl(activitionLink);

        try {
            final RegistrationPage invitationPage = Graphene.createPageFragment(RegistrationPage.class,
                    waitForElementVisible(INVITATION_PAGE_LOCATOR, browser));

            invitationPage.registerNewUser(registrationForm);
            waitForDashboardPageLoaded(browser);

            createSimpleReport();
            final List<String> attributes = reportPage.getTableReport().getAttributeElements();
            final long attributeCount = attributes.stream()
                    .filter(s -> s.equals("Partial College")).collect(Collectors.counting()).longValue();
            assertTrue(attributeCount == 1, "The MUF has been not applied");

            ++expectedMessageCount;
        } finally {
            UserManagementRestUtils.deleteUserByEmail(getRestApiClient(), nonRegistedUser);
        }
    }

    @Test(dependsOnMethods = {"inviteUserWithMuf"})
    public void updateRoleInInvitation() throws JSONException, IOException {
        final String nonRegistedUser = generateEmail(imapUser);

        final String previousRoleUri = getRoleUriFromInvitation(getRestApiClient(),
                sendDefaultInvitation(nonRegistedUser));
        final String previousActivitionLink = getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1);

        ++expectedMessageCount;

        final String invitationUri = inviteUserWithMufObj(getRestApiClient(), 
                testParams.getProjectId(), nonRegistedUser,
                defaultMufUri, UserRoles.VIEWER, INVITATION_MESSAGE);

        final String roleUri = getRoleUriFromInvitation(getRestApiClient(), invitationUri);
        assertTrue(!previousRoleUri.equals(roleUri) && roleUri.contains("roles/" + UserRoles.VIEWER.getRoleId()),
                "The role in invitation content has not been updated ");
        assertTrue(previousActivitionLink.equals(getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1)),
                "The invitation link is not the same as previous email");

        ++expectedMessageCount;
    }

    @Test(dependsOnMethods = {"inviteUserWithMuf"})
    public void updateMessageInInvitation() throws IOException, JSONException {
        final String nonRegistedUser = generateEmail(imapUser);
        sendDefaultInvitation(nonRegistedUser);

        Email lastEmail = doActionWithImapClient(imapClient ->
                getLastEmail(imapClient, GDEmails.INVITATION, invitationSubject, expectedMessageCount + 1));
        
        final String previousEmailBody = lastEmail.getBody();
        final String previousLink = getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1);

        ++expectedMessageCount;

        final String updatedMessage = "The message has been updated";
        inviteUserWithMufObj(getRestApiClient(), testParams.getProjectId(), nonRegistedUser,
                defaultMufUri, UserRoles.VIEWER, updatedMessage);

        lastEmail = doActionWithImapClient(imapClient ->
                getLastEmail(imapClient, GDEmails.INVITATION, invitationSubject, expectedMessageCount + 1));

        assertFalse(previousEmailBody.contains(updatedMessage),
                "The previous invitation contains updated message");
        assertTrue(lastEmail.getBody().contains(updatedMessage),
                "The invitation has not been updated ");
        assertTrue(previousLink.equals(getLinkInLastInvitation(invitationSubject, expectedMessageCount + 1)),
                "The invitation link is not the same as previous email");

        ++expectedMessageCount;
    }

    private boolean isUserUsingMuf(String mufUri, String userEmail) throws JSONException, IOException {
        final JSONObject userProfile = getUserProfileByEmail(restApiClient, userEmail);
        if(Objects.isNull(userProfile))
            return false;
        final String userProfileUri = userProfile.getJSONObject("links").getString("self");

        final List<String> users = getUsersUsingMuf(getRestApiClient(), testParams.getProjectId(), mufUri);
        return users.stream().anyMatch(u -> u.equals(userProfileUri));
    }

    private String getLinkInLastInvitation(String emailSubject, int expectedMessageCount) {
        final String messageBody = doActionWithImapClient(imapClient ->
                getLastEmail(imapClient, GDEmails.INVITATION, invitationSubject, expectedMessageCount).getBody());
        int beginIndex = messageBody.indexOf("/p/");
        return messageBody.substring(beginIndex, messageBody.indexOf("\n", beginIndex));
    }

    private void createSimpleReport() {
        initReportsPage();
        reportsPage.startCreateReport();
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(reportPage.getRoot());
        reportPage.createReport(new UiReportDefinition().withName("SimpleReport").withHows("Education"));
    }

    private String sendDefaultInvitation(String userEmail) throws IOException, JSONException {
        final String uri = inviteUserWithMufObj(getRestApiClient(), testParams.getProjectId(),
                userEmail, defaultMufUri, UserRoles.EDITOR, INVITATION_MESSAGE);

        assertEquals(defaultMufUri, UserManagementRestUtils.getMufUriFromInvitation(getRestApiClient(), uri),
                "The value in invitation content is different from created MUFUri");
        return uri;
    }

    private void logoutAndopenActivationLink(String user, String password, String activationLink) {
        logout();
        openUrl(activationLink);
        signInAtUI(imapUser, imapPassword);
    }

    private String createEducationMuf(List<String> expectedEducationElements, String mufTitle)
            throws ParseException, JSONException, IOException {
        final Attribute education = getMdService().getObj(getProject(), Attribute.class, title("Education"));
        final List<AttributeElement> educationElements = getMdService().getAttributeElements(education);

        final List<AttributeElement> filteredElements = educationElements.stream()
                .filter(e -> expectedEducationElements.contains(e.getTitle()))
                .collect(Collectors.toList());

        final List<String> filteredElementUris = (List<String>) filteredElements.stream()
                .map(e -> e.getUri()).collect(Collectors.toList());

        final Map<String, Collection<String>> conditions = new HashMap<>();
        conditions.put(education.getUri(), filteredElementUris);

        return createMufObjByUri(getRestApiClient(), testParams.getProjectId(), mufTitle, conditions);
    }
}