package com.gooddata.qa.utils.http.report;

import com.gooddata.md.report.Report;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.apache.commons.lang.BooleanUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static com.gooddata.md.Restriction.title;

public class ReportRestRequest extends CommonRestRequest {

    public ReportRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    /**
     * Lock/ unlock report
     * note: if there are dashboards contain this report
     * report cannot unlock
     *
     * @param reportTitle title of report
     * @param isLocked lock(true) / unlock(false)
     */
    public void setLockedReport(String reportTitle, boolean isLocked) throws JSONException, IOException {
        String reportUri = getReportByTitle(reportTitle).getUri();
        final JSONObject json = getJsonObject(reportUri);
        json.getJSONObject("report").getJSONObject("meta").put("locked", BooleanUtils.toInteger(isLocked));
        executeRequest(RestRequest.initPutRequest(reportUri, json.toString()), HttpStatus.OK);
    }

    public Report getReportByTitle(String title) {
        return getMdService().getObj(getProject(), Report.class, title(title));
    }
}
