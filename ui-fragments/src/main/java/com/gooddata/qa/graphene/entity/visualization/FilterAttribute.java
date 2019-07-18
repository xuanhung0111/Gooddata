package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.md.Attribute;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FilterAttribute {

    private Attribute attribute;

    private FilterAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public static FilterAttribute createFilter(Attribute attribute) {
        return new FilterAttribute(attribute);
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public static JSONArray initFilters(final List<FilterAttribute> filters) throws JSONException {
        return new JSONArray() {
            {
                filters.forEach((filter) -> {
                    put(new JSONObject() {{
                        put("negativeAttributeFilter", new JSONObject() {{
                            put("displayForm", new JSONObject() {{
                                put("uri", filter.getAttribute().getUri());
                            }});
                            put("notIn", new JSONArray());
                        }});
                    }});
                });
            }
        };
    }
}
