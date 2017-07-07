package com.gooddata.qa.graphene.entity.add;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;

public class SyncDatasets {

    private String[] datasets;

    private SyncDatasets() {
    }

    private SyncDatasets(String... datasets) {
        this.datasets = datasets;
    }

    public static final SyncDatasets ALL = new SyncDatasets();

    public static SyncDatasets custom(String... datasets) {
        return new SyncDatasets(datasets);
    }

    public String[] getDatasets() {
        return datasets;
    }

    public Pair<String, String> getParameter() {
        if (isNull(datasets)) return Pair.of("GDC_DE_SYNCHRONIZE_ALL", "true");

        String value = Stream.of(datasets)
                .map(d -> {
                        try {
                            return new JSONObject() {{
                                put("dataset", "dataset." + d);
                            }};
                        } catch (JSONException e) {
                            throw new RuntimeException("Error on initial JSON object for dataset: " + d);
                        }
                    })
                .map(obj -> obj.toString())
                .collect(joining(",", "[", "]"));
        return Pair.of("GDC_DATALOAD_DATASETS", value);
    }
}
