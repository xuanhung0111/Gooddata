package com.gooddata.qa.graphene.fragments.greypages.datawarehouse;

import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;

import com.gooddata.qa.graphene.fragments.greypages.AbstractGreyPagesFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;

public class AbstractDatawarehouseFragment extends AbstractGreyPagesFragment {

    protected String waitTaskSucceed(int checkIterations, String resultLink) throws JSONException {
        waitTaskFinished(checkIterations);
        JSONObject json = loadJSON();
        String taskResultUrl = json.getJSONObject("asyncTask").getJSONObject("links").getString(resultLink);
        System.out.println("Object created on URL " + taskResultUrl);
        return taskResultUrl;
    }

    protected void waitTaskFinished(int checkIterations) throws JSONException {
        String executionUrl = browser.getCurrentUrl();
        System.out.println("Related execution URL is " + executionUrl);
        Assert.assertTrue(executionUrl.contains("executions"),
                String.format("Task creation didn't redirect to /executions/* page but to %s instead", executionUrl));
        int i = 0;
        boolean asyncTaskPoll = !isAsyncTaskError() && isAsyncTaskPoll();
        while (!isAsyncTaskError() && asyncTaskPoll && i < checkIterations) {
            System.out.println("Current task execution is polling");
            sleepTightInSeconds(5);
            browser.get(executionUrl);
            asyncTaskPoll = !isAsyncTaskError() && isAsyncTaskPoll();
            i++;
        }
    }

    private boolean isAsyncTaskError() throws JSONException {
        final JSONObject json = loadJSON();
        return json.has("error");
    }

    private boolean isAsyncTaskPoll() throws JSONException {
        JSONObject json = loadJSON();
        return json.getJSONObject("asyncTask").getJSONObject("links").has("poll");
    }

}
