package com.gooddata.qa.graphene.entity.visualization;

import com.gooddata.sdk.model.md.Attribute;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FilterAttribute {

    private Attribute attribute;
    private List<String> uriElementsNotIn;

    private FilterAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    private FilterAttribute(Attribute attribute, List<String> uriElementsNotIn) {
        this.attribute = attribute;
        this.uriElementsNotIn = uriElementsNotIn;
    }

    public static FilterAttribute createFilter(Attribute attribute, List<String> uriElementsNotIn) {
        return new FilterAttribute(attribute, uriElementsNotIn);
    }

    public static FilterAttribute createFilter(Attribute attribute) {
        return new FilterAttribute(attribute);
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public List<String> getUriElementsNotIn() {
        return this.uriElementsNotIn;
    }

    public static JSONArray initFilters(final List<FilterAttribute> filters) throws JSONException {
        return new JSONArray() {{
                filters.forEach((filter) -> {
                    put(new JSONObject() {{
                        put("negativeAttributeFilter", new JSONObject() {{
                            put("displayForm", new JSONObject() {{
                                put("uri", filter.getAttribute().getDefaultDisplayForm().getUri());
                            }});
                                put("notIn", new JSONArray(filter.getUriElementsNotIn()));
                        }});
                    }});
                });
            }
        };
    }
}
