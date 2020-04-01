package com.gooddata.qa.utils.cloudresources;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ProjectViewRestRequest extends CommonRestRequest {
    public static final String PROJECT_VIEW_REST_URI = "/gdc/dataload/internal/projectsView";

    public ProjectViewRestRequest(RestClient restClient, String projectId) {
        super(restClient, projectId);
    }

    private static final Logger log = Logger.getLogger(DataSourceRestRequest.class.getName());

    public JSONObject getListProjectView() throws IOException {
        return getJsonObject(
                RestRequest.initGetRequest(PROJECT_VIEW_REST_URI + "?searchString=&scheduleStatus=&offset=0&limit=1000"));
    }

    public List<String> listProcessesUsingATTDatasources(List<String> dataSources) throws IOException {
        List<String> listProcessesUsed = new ArrayList<String>();
        JSONObject listProjectView = getListProjectView();
        JSONArray items = listProjectView.getJSONObject("projectsView").getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
            JSONArray processes = items.getJSONObject(i).getJSONObject("projectView").getJSONArray("processes");
            // get process in array json processes
            for (int j = 0; j < processes.length(); j++) {
                JSONObject process = processes.getJSONObject(j).getJSONObject("process");
                // check that if process has type ADDv2 , it has component object and config object
                if (hasComponent(process)) {
                    JSONObject component = process.getJSONObject("component");
                    if (hasConfig(component)) {
                        String datasource = process.getJSONObject("component").getJSONObject("config").getJSONObject("dataDistribution")
                                .getString("dataSource");
                        // check that if datasource which used by process in list old dataSources
                        // we will add uri of process to list deleted processes
                        if (dataSources.contains(datasource)) {
                            listProcessesUsed.add(process.getJSONObject("links").getString("self"));
                        }
                    }
                }
            }
        }
        log.info("list process : " + listProcessesUsed);
        return listProcessesUsed;
    }

    private boolean hasComponent(JSONObject process) {
        return process.has("component");
    }

    private boolean hasConfig(JSONObject component) {
        return component.has("config");
    }
}
