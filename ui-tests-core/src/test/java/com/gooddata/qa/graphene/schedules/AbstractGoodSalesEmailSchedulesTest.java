/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static java.util.Arrays.asList;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.qa.utils.http.RestRequest;
import com.gooddata.qa.utils.http.scheduleEmail.ScheduleEmailRestRequest;
import org.apache.commons.lang.math.IntRange;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;

public class AbstractGoodSalesEmailSchedulesTest extends GoodSalesAbstractTest {

    protected static final String CANNOT_DELETE_DASHBOARD_MESSAGE = "Dashboard cannot be deleted"
            + " because it is linked from a scheduled email or KPI dashboard. Remove all links and retry.";

    protected File attachmentsDirectory;

    @Override
    protected void initProperties() {
        super.initProperties();
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    protected Part findPartByContentType(List<Part> parts, String contentType) throws MessagingException {
        for (Part part : parts) {
            if (part.getContentType().contains(contentType.toUpperCase())) {
                return part;
            }
        }
        return null;
    }

    protected ScheduleEmailRestRequest initScheduleEmailRestRequest() {
        return new ScheduleEmailRestRequest(new RestClient(
                new RestClient.RestProfile(testParams.getHost(), imapUser, imapPassword, true)), testParams.getProjectId());
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

    // The different here is schedule email use accelerate engine to force email sent immediately
    // instead of waiting until the exactly period (xx:00, xx:30). So user can save too much time with this technique.

    // But sometimes the schedule is set so close with the default schedule time (xx:29, xx:59).
    // And when getting email, the time has passed over xx:00, xx:30,
    // that means the same message will be sent twice (one from the system due to the default
    // schedule time reached, and one from the accelerate engine).

    // In this situation, ImapUtils#waitForMessages will not adapt because the email number is greater than the expected now.
    // So use this method as a replacement.
    protected List<Message> waitForMessages(ImapClient imapClient, GDEmails from, String subject, int expectedMessages)
            throws MessagingException {
        List<Message> messages =  ImapUtils.waitForMessages(imapClient, from, subject);

        if (messages.size() == expectedMessages) {
            return messages;
        }

        if (messages.size() == expectedMessages + 1) {
            if (messages.stream()
                    .map(ImapUtils::getMessageReceiveDate)
                    .map(DateTime::getMinuteOfHour)
                    .anyMatch(m -> new IntRange(0, 10).containsInteger(m) || new IntRange(30, 40).containsInteger(m))) {
                log.info(format("scheduled email is set up so close to the default scheduled time. So we get %s emails.",
                        messages.size()));
                return messages;
            }
        }

        throw new RuntimeException("There are too many messages than expected: " + expectedMessages);
    }

    /**
     * Get scheduledMail
     */
    private InputStream getScheduleInputStream(String scheduleUri) throws IOException {
        RestClient restClient = new RestClient(getProfile(Profile.ADMIN));

        System.out.println("Get scheduledMail: " + scheduleUri);
        HttpRequestBase getRequest = RestRequest.initGetRequest(scheduleUri);
        HttpResponse getResponse = restClient.execute(getRequest);
        System.out.println(" - status: " + getResponse.getStatusLine().getStatusCode());
        return getResponse.getEntity().getContent();
    }

    /**
     * Update scheduledMail
     */
    private void setSchedule(String scheduleUri, String schedule) {
        System.out.println("Update scheduledMail: " + scheduleUri);
        final CommonRestRequest restRequest = new CommonRestRequest(new RestClient(
                new RestProfile(testParams.getHost(), imapUser, imapPassword, true)),
                testParams.getProjectId());
        restRequest.executeRequest(RestRequest.initPostRequest(scheduleUri, schedule));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String getResetRecurrencySchedule(InputStream stream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Map rootNode = mapper.readValue(stream, Map.class);
        Map scheduledMail = (Map) rootNode.get("scheduledMail");
        Map content = (Map) scheduledMail.get("content");
        Map when = (Map) content.get("when");

        String timeZone = (String) when.get("timeZone");
        System.out.println(" - timezone of object: " + timeZone);
        System.out.println(" - current recurrency: " + when.get("recurrency"));

        content.remove("lastSuccessfull");
        DateTime dateTime = new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone(timeZone)));
        DateTimeFormatter fmt = DateTimeFormat.forPattern("*Y:M:0:d:H:m:s");
        System.out.println(" - current time: " + fmt.print(dateTime));

        // plusSeconds(1) - to be meta.updated <= recurrency (cannot be older)
        when.put("recurrency", fmt.print(dateTime.plusSeconds(1)));
        System.out.println(" - set recurrency to: " + when.get("recurrency"));

        return mapper.writeValueAsString(rootNode);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
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
}
