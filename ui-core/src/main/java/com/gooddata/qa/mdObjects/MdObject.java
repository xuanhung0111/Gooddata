package com.gooddata.qa.mdObjects;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public interface MdObject {
    JSONObject getMdObject();

    default void handleJSONException(JSONException e) {
        throw new RuntimeException("Invalid JSON: " + e.getMessage());
    }

    default String generateIdentifier() {
        return UUID.randomUUID().toString().substring(0, 10);
    }
}
