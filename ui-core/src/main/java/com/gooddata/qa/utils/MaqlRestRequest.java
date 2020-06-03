package com.gooddata.qa.utils;

import com.gooddata.sdk.model.project.Project;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;

public class MaqlRestRequest {
    private static String DROP_DATASET_STRING = "DROP {dataset.%s} CASCADE";
    private Project project;
    private RestProfile profile;

    public MaqlRestRequest(Project project, RestProfile profile) {
        super();
        this.project = project;
        this.profile = profile;
    }

    public void setupMaqlRemoveDefaultLabel(String dataset, String label, String attribute) {
        String maql = new StringBuilder().append(
                "ALTER ATTRIBUTE {attr.${dataset}.${attribute}} DROP LABELS {label.${dataset}.${attribute}.${label}}; ALTER ATTRIBUTE {attr.${dataset}.${attribute}} ")
                .append("ADD LABELS {label.${dataset}.${attribute}.${label}} VISUAL(TITLE \"${label}\") AS {f_${dataset}.nm_${label}}")
                .toString()
                .replace("${dataset}", dataset)
                .replace("${attribute}", attribute)
                .replace("${label}", label);
        setupMaqlToSpecificProject(maql);
    }

    public void setupMaqlDropDataset(String dataset) {
        String maql = String.format(DROP_DATASET_STRING, dataset);
        setupMaqlToSpecificProject(maql);
    }

    private void setupMaqlToSpecificProject(String maql) {
        new RestClient(profile).getModelService().updateProjectModel(project, maql).get();
    }
}
