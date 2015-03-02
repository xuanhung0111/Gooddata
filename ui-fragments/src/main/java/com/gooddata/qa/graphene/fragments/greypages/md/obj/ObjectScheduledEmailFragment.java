package com.gooddata.qa.graphene.fragments.greypages.md.obj;

import org.json.JSONException;
import org.json.JSONObject;

public class ObjectScheduledEmailFragment extends ObjectFragment {

    public final static int WRONG_ID = -1;

    public boolean hasExecutionContext() throws JSONException, InterruptedException {
        return !getDashboardAttachment().isNull("executionContext");
    }

    public int getExecutionContextId() throws JSONException, InterruptedException {
        if (hasExecutionContext()) {
            String executionContextUri = getDashboardAttachment().getString("executionContext");
            String[] uriParts = executionContextUri.split("/");

            return Integer.parseInt(uriParts[uriParts.length - 1]);
        } else {
            return ObjectScheduledEmailFragment.WRONG_ID;
        }
    }

    private JSONObject getDashboardAttachment() throws JSONException, InterruptedException {
        JSONObject attachment = getObject()
                .getJSONObject("scheduledMail")
                .getJSONObject("content")
                .getJSONArray("attachments")
                .getJSONObject(0);

        return attachment.getJSONObject("dashboardAttachment");
    }
}
