package com.gooddata.qa.graphene.entity.visualization;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class FilterDate {

    private String dateUri;
    private String from;
    private String to;

    private FilterDate(String dateUri, String from, String to) {
        this.dateUri = dateUri;
        this.from = from;
        this.to = to;
    }

    public static FilterDate createFilterDate(String dateUri, String from, String to) {
        return new FilterDate(dateUri, from, to);
    }

    public String getDateUri() {
        return this.dateUri;
    }

    public String getFrom() {
        return this.from;
    }

    public String getTo() {
        return this.to;
    }

    public static Object initDateFilter(FilterDate filterDate) throws JSONException {
        return new JSONObject() {{
            put("absoluteDateFilter", new JSONObject() {{
                put("dataSet", new JSONObject() {{
                    put("uri", filterDate.getDateUri());
                }});
                put("from", filterDate.getFrom());
                put("to", filterDate.getTo());
            }});
        }};
    }
}
