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

public class AbstractGoodSalesEmailSchedulesTest extends GoodSalesAbstractTest {

    private static final By BY_SCHEDULES_LOADING = By.cssSelector(".loader");

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

    protected String resetRecurrencyToNow(InputStream stream) throws IOException {
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
