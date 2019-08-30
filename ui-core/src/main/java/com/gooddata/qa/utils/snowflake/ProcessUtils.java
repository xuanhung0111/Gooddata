package com.gooddata.qa.utils.snowflake;

import static org.apache.commons.lang.Validate.notNull;

import com.gooddata.GoodDataException;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecution;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

public class ProcessUtils {

    private RestClient restClient;
    private DataloadProcess dataloadProcess;
    private String executable;

    public ProcessUtils(RestClient restClient, DataloadProcess dataloadProcess) {
        this.restClient = restClient;
        this.executable = "main.rb";
        this.dataloadProcess = dataloadProcess;
    }

    public ProcessService getProcessService() {
        return restClient.getProcessService();
    }

    public ProcessExecutionDetail execute(final Parameters params) {
        notNull(params, "Parameter cannot be null");
        return restClient.getProcessService().executeProcess(
                new ProcessExecution(dataloadProcess, executable, params.getParameters(), params.getSecureParameters())).get();
    }

    public String executeError(final Parameters params) {
        try {
            notNull(params, "Parameter cannot be null");
            return restClient.getProcessService().executeProcess(
                    new ProcessExecution(dataloadProcess, executable, params.getParameters(), params.getSecureParameters()))
                    .get().getUri();
        } catch (GoodDataException e) {
            return e.getCause().getMessage();
        }
    }

    public JSONObject setModeDefaultDataset(String dataset) {
        Pair<String, String> datasetPair = Pair.of("dataset", "dataset." + dataset);
        Pair<String, String> uploadMode = Pair.of("uploadMode", "DEFAULT");
        List<Pair<String, String>>  values = new ArrayList<Pair<String, String>>();
        values.add(datasetPair);
        values.add(uploadMode);
        return getValue(values);
    }
    
    public JSONObject setModeFullDataset(String dataset) {
        Pair<String, String> datasetPair = Pair.of("dataset", "dataset." + dataset);
        Pair<String, String> uploadMode = Pair.of("uploadMode", "FULL");
        List<Pair<String, String>>  values = new ArrayList<Pair<String, String>>();
        values.add(datasetPair);
        values.add(uploadMode);
        return getValue(values);
    }
    
    public JSONObject setModeIncrementalDataset(String dataset) {
        Pair<String, String> datasetPair = Pair.of("dataset", "dataset." + dataset);
        Pair<String, String> uploadMode = Pair.of("uploadMode", "INCREMENTAL");
        List<Pair<String, String>>  values = new ArrayList<Pair<String, String>>();
        values.add(datasetPair);
        values.add(uploadMode);
        return getValue(values);
    }

    //This function returns value for Json Dataset , ex: {"dataset":"datasetA","uploadMode":"DEFAULT"}
    public JSONObject getValue(List<Pair<String, String>>  values) {
        JSONObject valueJson = new JSONObject();
        for (Pair <String,String> value  : values) { 
            valueJson.put(value.getKey(), value.getValue());
        }
        return valueJson;
    }

    //This function returns value for multi Dataset
    public String getDatasets(List<JSONObject> listDataset) {
        return listDataset
        .stream()
        .map(JSONObject::toString)
        .collect(Collectors.joining(", "));
    }
    
    public String getDataset(JSONObject dataset) {
        return dataset.toString();
    }

    public String getExecutionLog(final String logUri, final String projectId) {
        try {
            return new CommonRestRequest(restClient, projectId)
                    .getResource(logUri, HttpStatus.OK);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
