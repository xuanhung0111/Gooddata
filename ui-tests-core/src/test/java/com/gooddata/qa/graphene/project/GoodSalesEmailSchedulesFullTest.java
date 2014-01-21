/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.http.ScheduleMailPssClient;
import com.gooddata.qa.utils.mail.ImapClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = { "GoodSalesSchedules" }, description = "Tests for GoodSales project (email schedules functionality) in GD platform")
public class GoodSalesEmailSchedulesFullTest extends GoodSalesAbstractTest {

	private static final String FROM = "noreply@gooddata.com";
	private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

	private String reportTitle = "UI-Graphene-core-Report";
	private String dashboardTitle = "UI-Graphene-core-Dashboard";

	private File attachmentsDirectory;

	@BeforeClass
	public void setUp() throws Exception {
		String identification = ": " + host + " - " + testIdentification;
		reportTitle = reportTitle + identification;
		dashboardTitle = dashboardTitle + identification;

		attachmentsDirectory = new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
	}

	@Test(dependsOnMethods = { "createProject" }, groups = { "schedules" })
	public void verifyEmptySchedules() {
		initEmailSchedulesPage();
		assertEquals(emailSchedulesPage.getNumberOfSchedules(), 0, "There are some not expected schedules");
		Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
	}

	@Test(dependsOnMethods = { "verifyEmptySchedules" }, groups = { "schedules" })
	public void createDashboardSchedule() {
		initEmailSchedulesPage();
		emailSchedulesPage.scheduleNewDahboardEmail(user, dashboardTitle, "Scheduled email test - dashboard.", "Outlook");
		checkRedBar();
		Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
	}

	@Test(dependsOnMethods = { "verifyEmptySchedules" }, groups = { "schedules" })
	public void createReportSchedule() {
		initEmailSchedulesPage();
		emailSchedulesPage.scheduleNewReportEmail(user, reportTitle, "Scheduled email test - report.", "Activities by Type", ExportFormat.ALL);
		checkRedBar();
		Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
	}

	@Test(dependsOnGroups = { "schedules" })
	public void verifyCreatedSchedules() {
		initEmailSchedulesPage();
		assertEquals(emailSchedulesPage.getNumberOfSchedules(), 2, "2 schedules weren't created properly");
		Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
	}

	@Test(dependsOnMethods = { "verifyCreatedSchedules" })
	public void updateScheduledMailRecurrency() throws Exception {
		initEmailSchedulesPage();

		String reportScheduleUri = emailSchedulesPage.getScheduleMailUriByName(reportTitle);
		String dashboardScheduleUri = emailSchedulesPage.getScheduleMailUriByName(dashboardTitle);
		updateRecurrencyString(reportScheduleUri);
		updateRecurrencyString(dashboardScheduleUri);
	}

	@Test(groups = { "tests" }, dependsOnMethods = { "updateScheduledMailRecurrency" })
	public void waitForMessages() throws Exception {
		ScheduleMailPssClient pssClient = new ScheduleMailPssClient(getRestApiClient(), projectId);
		ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
		try {
			System.out.println("ACCELERATE scheduled mails processing");
			pssClient.accelerate();
			checkMailbox(imapClient);
			successfulTest = true;
		} finally {
			System.out.println("DECELERATE scheduled mails processing");
			pssClient.decelerate();
			imapClient.close();
		}
	}

	private void initEmailSchedulesPage() {
		browser.get(getRootUrl() + PAGE_UI_PROJECT_PREFIX + projectId + "|emailSchedulePage");
		waitForSchedulesPageLoaded();
		waitForElementNotVisible(BY_SCHEDULES_LOADING);
		waitForElementVisible(emailSchedulesPage.getRoot());
	}

	private void checkMailbox(ImapClient imapClient) throws Exception {
		Message[] reportMessages = new Message[0];
		Message[] dashboardMessages = new Message[0];
		int loops = 0;

		while (!bothEmailsArrived(reportMessages, dashboardMessages) && (loops < 15)) {  // 2,5 min
			System.out.println("Waiting for messages, try " + (loops + 1));
			reportMessages = imapClient.getMessagesFromInbox(FROM, reportTitle);
			dashboardMessages = imapClient.getMessagesFromInbox(FROM, dashboardTitle);

			if (bothEmailsArrived(reportMessages, dashboardMessages)) {
				System.out.println("Both export messages arrived");
				break;
			}

			Thread.sleep(10000);
			loops++;
		}

		System.out.println("Email checks ...");
		assertEquals(reportMessages.length, 1, "Expected one report message");
		assertEquals(dashboardMessages.length, 1, "Expected one dashboard message");

		// REPORT EXPORT
		List<Part> reportAttachmentParts = ImapClient.getAttachmentParts(reportMessages[0]);
		assertEquals(reportAttachmentParts.size(), 3, "Expected 3 attachments for report");

		Part pdfPart = findPartByContentType(reportAttachmentParts, "application/pdf");
		assertTrue(pdfPart.getSize() > 32000, "PDF is greater than 32kB");

		Part xlsPart = findPartByContentType(reportAttachmentParts, "application/vnd.ms-excel");
		assertTrue(xlsPart.getSize() > 7700, "XLS is greater than 7.7kB");

		Part csvPart = findPartByContentType(reportAttachmentParts, "text/csv");
		assertTrue(csvPart.getSize() > 120, "CSV is greater than 120B");

		System.out.println("Saving report messages ...");
		ImapClient.saveMessageAttachments(reportMessages[0], attachmentsDirectory);


		// DASHBOARD EXPORT
		List<Part> dashboardAttachmentParts = ImapClient.getAttachmentParts(dashboardMessages[0]);
		assertEquals(dashboardAttachmentParts.size(), 1, "Expected 1 attachment for dashboard");
		assertTrue(dashboardAttachmentParts.get(0).getContentType().contains("application/pdf".toUpperCase()),
				"Dashboard attachment has PDF content type");
		assertTrue(dashboardAttachmentParts.get(0).getSize() > 67000, "PDF is greater than 67kB");

		System.out.println("Saving dashboard message ...");
		ImapClient.saveMessageAttachments(dashboardMessages[0], attachmentsDirectory);
	}

	private Part findPartByContentType(List<Part> parts, String contentType) throws MessagingException {
		for (Part part : parts) {
			if (part.getContentType().contains(contentType.toUpperCase())) {
				return part;
			}
		}
		return null;
	}

	private boolean bothEmailsArrived(Message[] reportMessages, Message[] dashboardMessages) {
		return reportMessages.length > 0 && dashboardMessages.length > 0;
	}

	private void updateRecurrencyString(String scheduleUri) throws IOException {
		RestApiClient restApiClient = getRestApiClient();

		// get scheduledMail
		System.out.println("Get scheduledMail: " + scheduleUri);
		HttpRequestBase getRequest = restApiClient.newGetMethod(scheduleUri);
		HttpResponse getResponse = restApiClient.execute(getRequest);
		System.out.println(" - status: " + getResponse.getStatusLine().getStatusCode());
		InputStream scheduleStream = getResponse.getEntity().getContent();

		// change recurrency to current
		String schedule = resetRecurrencyToNow(scheduleStream);

		// update scheduledMail
		System.out.println("Update scheduledMail: " + scheduleUri);
		HttpRequestBase postRequest = restApiClient.newPostMethod(scheduleUri, schedule);
		HttpResponse postResponse = restApiClient.execute(postRequest);
		System.out.println(" - status: " + postResponse.getStatusLine().getStatusCode());
		EntityUtils.consumeQuietly(postResponse.getEntity());
	}

	private String resetRecurrencyToNow(InputStream stream) throws IOException {
		ObjectMapper mapper = new ObjectMapper();

		Map rootNode = mapper.readValue(stream, Map.class);
		Map scheduledMail = (Map) rootNode.get("scheduledMail");
		Map content = (Map) scheduledMail.get("content");
		Map when = (Map) content.get("when");

		String timeZone = (String) when.get("timeZone");
		content.remove("lastSuccessfull");

		DateTime dateTime = new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone)));
		DateTimeFormatter fmt = DateTimeFormat.forPattern("*Y:M:0:d:H:m:s");
		// plusSeconds(1) - to be meta.updated <= recurrency (cannot be older)
		when.put("recurrency", fmt.print(dateTime.plusSeconds(1)));
		System.out.println(" - set recurrency to: " + when.get("recurrency"));

		return mapper.writeValueAsString(rootNode);
	}
}
