/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.utils.http.RestApiClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openqa.selenium.By;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import com.gooddata.qa.utils.graphene.Screenshots;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AbstractGoodSalesEmailSchedulesTest extends GoodSalesAbstractTest {

    protected static final int MAILBOX_TIMEOUT_MINUTES = 6;
    // mailbox polling interval in miliseconds
    protected static final int MAILBOX_POLL_INTERVAL_MILISECONDS = 30000;

    protected static final String FROM = "noreply@gooddata.com";

    private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

    @BeforeClass
    public void setUpImap() throws Exception {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"schedules"})
    public void verifyEmptySchedules() {
        initEmailSchedulesPage();
        assertEquals(emailSchedulesPage.getNumberOfSchedules(), 0, "There is no schedule.");
        Screenshots.takeScreenshot(browser, "Goodsales-no-schedules", this.getClass());
    }

    protected void initEmailSchedulesPage() {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|emailSchedulePage");
        waitForSchedulesPageLoaded(browser);
        waitForElementNotVisible(BY_SCHEDULES_LOADING);
        waitForElementVisible(emailSchedulesPage.getRoot());
    }

    protected Part findPartByContentType(List<Part> parts, String contentType) throws MessagingException {
        for (Part part : parts) {
            if (part.getContentType().contains(contentType.toUpperCase())) {
                return part;
            }
        }
        return null;
    }

    protected void updateRecurrencyString(String scheduleUri) throws IOException {
        InputStream scheduleStream = getScheduleInputStream(scheduleUri);
        String schedule = getResetRecurrencySchedule(scheduleStream);
        setSchedule(scheduleUri, schedule);
    }

    protected void setBcc(String scheduleUri, String[] bccEmails) throws IOException {
        InputStream scheduleStream = getScheduleInputStream(scheduleUri);
        String schedule = getBccSchedule(scheduleStream, bccEmails);
        setSchedule(scheduleUri, schedule);
    }

    private InputStream getScheduleInputStream(String scheduleUri) throws IOException {
        RestApiClient apiClient = getRestApiClient();

        // get scheduledMail
        System.out.println("Get scheduledMail: " + scheduleUri);
        HttpRequestBase getRequest = apiClient.newGetMethod(scheduleUri);
        HttpResponse getResponse = apiClient.execute(getRequest);
        System.out.println(" - status: " + getResponse.getStatusLine().getStatusCode());
        return getResponse.getEntity().getContent();
    }

    private void setSchedule(String scheduleUri, String schedule) {
        // update scheduledMail
        System.out.println("Update scheduledMail: " + scheduleUri);
        HttpRequestBase postRequest = restApiClient.newPostMethod(scheduleUri, schedule);
        HttpResponse postResponse = restApiClient.execute(postRequest);
        System.out.println(" - status: " + postResponse.getStatusLine().getStatusCode());
        EntityUtils.consumeQuietly(postResponse.getEntity());
    }

    private String getResetRecurrencySchedule(InputStream stream) throws IOException {
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

    private String getBccSchedule(InputStream scheduleStream, String[] bccEmails) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map rootNode = mapper.readValue(scheduleStream, Map.class);
        Map scheduledMail = (Map) rootNode.get("scheduledMail");
        Map content = (Map) scheduledMail.get("content");

        List<String> bccEmailsList = asList(bccEmails);
        if (bccEmails != null) {
            content.put("bcc", bccEmailsList);
        }

        System.out.println(" - added bcc to schedule: " + bccEmailsList);
        return mapper.writeValueAsString(rootNode);
    }

    protected int getMailboxMaxPollingLoops() {
        return 60000 / MAILBOX_POLL_INTERVAL_MILISECONDS * MAILBOX_TIMEOUT_MINUTES;
    }

}
