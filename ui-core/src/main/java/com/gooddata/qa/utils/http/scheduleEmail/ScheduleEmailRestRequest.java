package com.gooddata.qa.utils.http.scheduleEmail;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static com.gooddata.qa.utils.http.RestRequest.initDeleteRequest;
import static com.gooddata.qa.utils.http.RestRequest.initPutRequest;
import static java.lang.String.format;

/**
 * Simple control over fast and standard mode of scheduled emails
 * processing in PSS.<br/><br/>
 * <p/>
 * Class calls PUT on /accelerate resource to speed up scheduled
 * emails processing {@link #accelerate()}<br/>
 * Class calls DELETE to /accelerate resource to slow down
 * scheduled emails processing {@link #decelerate()}
 */
public class ScheduleEmailRestRequest extends CommonRestRequest{

    private static final String ACCELERATE_URI = "/gdc/internal/projects/%s/scheduledMails/accelerate";
    private static final String OBJ_LINK = "/gdc/md/%s/obj/";

    public ScheduleEmailRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    // Speed up scheduled emails processing
    public void accelerate() {
        executeRequest(initPutRequest(format(ACCELERATE_URI, projectId), "{\"accelerate\":{}}"));
    }

    // Slow down scheduled emails processing
    public void decelerate() {
        executeRequest(initDeleteRequest(format(ACCELERATE_URI, projectId)));
    }

    /**
     * Add executionContext for scheduled mail
     *
     * @param scheduleID
     * @param executionContextID
     */
    public void addExecutionContext(int scheduleID, int executionContextID) throws ParseException,
            JSONException, IOException {
        JSONObject scheduleEmail = getJsonObject(format(OBJ_LINK, projectId) + scheduleID);
        scheduleEmail.getJSONObject("scheduledMail").getJSONObject("content")
                .getJSONArray("attachments").getJSONObject(0)
                .getJSONObject("dashboardAttachment")
                .put("executionContext", format(OBJ_LINK, projectId) + executionContextID);

        executeRequest(initPutRequest(
                format(OBJ_LINK, projectId) + scheduleID, scheduleEmail.toString()), HttpStatus.OK);
    }
}
