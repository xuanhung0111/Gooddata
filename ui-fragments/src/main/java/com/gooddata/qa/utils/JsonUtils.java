package com.gooddata.qa.utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public class JsonUtils {

    private JsonUtils() {
    }

    public static JSONObject getJsonObjectFromFile(File exportFile) throws IOException, IOException {
        InputStream is = new FileInputStream(exportFile);
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        JSONObject json = new JSONObject(jsonTxt);
        return json;
    }
}
